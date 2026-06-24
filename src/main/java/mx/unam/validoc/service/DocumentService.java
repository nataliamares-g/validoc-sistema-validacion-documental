package mx.unam.validoc.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.imageio.ImageIO;
import mx.unam.validoc.model.DocumentRecord;
import mx.unam.validoc.model.QrPosition;
import mx.unam.validoc.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
    private static final float PAGE_MARGIN = 54f;
    private static final float HEADER_SPACE = 72f;
    private static final float QR_SIZE = 165f;

    private final DocumentRepository repo;
    private final Path storage;
    private final String defaultBaseUrl;

    public DocumentService(
            DocumentRepository repo,
            @Value("${app.storage-dir}") String storageDir,
            @Value("${app.base-url}") String baseUrl) throws IOException {
        this.repo = repo;
        this.storage = Paths.get(storageDir).toAbsolutePath();
        this.defaultBaseUrl = normalizeBaseUrl(baseUrl);
        Files.createDirectories(storage.resolve("original"));
        Files.createDirectories(storage.resolve("with-qr"));
        Files.createDirectories(storage.resolve("qr"));
    }

    public DocumentRecord create(
            String title,
            String type,
            String area,
            QrPosition position,
            MultipartFile file,
            String registeredBy) throws Exception {

        if (file.isEmpty()
                || file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Debes seleccionar un archivo PDF válido.");
        }

        String folio = "VD-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Path original = storage.resolve("original").resolve(folio + ".pdf");
        Files.copy(file.getInputStream(), original, StandardCopyOption.REPLACE_EXISTING);

        String validationUrl = defaultBaseUrl + "/validar/" + folio;

        Path qr = storage.resolve("qr").resolve(folio + ".png");
        createQr(validationUrl, qr);

        Path qrPdf = storage.resolve("with-qr").resolve(folio + "-QR.pdf");
        appendValidationPage(original, qr, qrPdf, position, folio);

        DocumentRecord document = new DocumentRecord();
        document.setFolio(folio);
        document.setTitle(title);
        document.setDocumentType(type);
        document.setIssuerArea(area);
        document.setQrPosition(position);
        document.setOriginalPath(original.toString());
        document.setQrPdfPath(qrPdf.toString());
        document.setQrImagePath(qr.toString());
        document.setValidationUrl(validationUrl);
        document.setRegisteredBy(registeredBy);
        return repo.save(document);
    }

    private String normalizeBaseUrl(String value) {
        String normalized = value == null ? "" : value.trim();
        if (!normalized.matches("https?://.+")) {
            throw new IllegalArgumentException("La dirección de validación debe iniciar con http:// o https://");
        }
        return normalized.replaceAll("/+$", "");
    }

    private void createQr(String content, Path output) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400);
        MatrixToImageWriter.writeToPath(matrix, "PNG", output);
    }

    private void appendValidationPage(
            Path source,
            Path qrPath,
            Path output,
            QrPosition position,
            String folio) throws Exception {

        try (PDDocument pdf = PDDocument.load(source.toFile())) {
            if (pdf.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("El PDF no contiene páginas.");
            }

            PDPage referencePage = pdf.getPage(pdf.getNumberOfPages() - 1);
            PDRectangle referenceBox = referencePage.getMediaBox();
            float width = referenceBox.getWidth();
            float height = referenceBox.getHeight();
            PDPage validationPage = new PDPage(new PDRectangle(width, height));
            pdf.addPage(validationPage);

            BufferedImage image = ImageIO.read(qrPath.toFile());
            PDImageXObject qrImage = LosslessFactory.createFromImage(pdf, image);

            boolean left = position == QrPosition.SUPERIOR_IZQUIERDA
                    || position == QrPosition.INFERIOR_IZQUIERDA;
            boolean top = position == QrPosition.SUPERIOR_IZQUIERDA
                    || position == QrPosition.SUPERIOR_DERECHA;

            float qrX = left ? PAGE_MARGIN : width - PAGE_MARGIN - QR_SIZE;
            // Las posiciones superiores comienzan debajo del encabezado para no cubrir el título.
            float qrY = top
                    ? height - PAGE_MARGIN - HEADER_SPACE - QR_SIZE
                    : PAGE_MARGIN + 30f;

            try (PDPageContentStream content = new PDPageContentStream(pdf, validationPage)) {
                content.setNonStrokingColor(new Color(244, 248, 252));
                content.addRect(0, 0, width, height);
                content.fill();

                content.setNonStrokingColor(new Color(16, 42, 67));
                String heading = "VALIDACION DOCUMENTAL";
                float headingSize = 20f;
                float headingWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(heading) / 1000f * headingSize;
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, headingSize);
                content.newLineAtOffset((width - headingWidth) / 2f, height - PAGE_MARGIN);
                content.showText(heading);
                content.endText();

                content.setStrokingColor(new Color(22, 102, 176));
                content.setLineWidth(1.2f);
                content.addRect(qrX - 8f, qrY - 8f, QR_SIZE + 16f, QR_SIZE + 16f);
                content.stroke();
                content.drawImage(qrImage, qrX, qrY, QR_SIZE, QR_SIZE);

                float labelY = top ? qrY - 22f : qrY + QR_SIZE + 22f;
                content.setNonStrokingColor(new Color(16, 42, 67));
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                content.newLineAtOffset(qrX, labelY);
                content.showText("Folio: " + folio);
                content.endText();

            }

            pdf.save(output.toFile());
        }
    }

    public void delete(Long id) {
        DocumentRecord document = repo.findById(id).orElseThrow();
        repo.delete(document);
        deleteFile(document.getOriginalPath());
        deleteFile(document.getQrPdfPath());
        deleteFile(document.getQrImagePath());
    }

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException ignored) {
        }
    }

    public byte[] readOriginalPdf(Long id) throws IOException {
        DocumentRecord document = repo.findById(id).orElseThrow();
        return Files.readAllBytes(Path.of(document.getOriginalPath()));
    }

    public byte[] readQrPdf(Long id) throws IOException {
        DocumentRecord document = repo.findById(id).orElseThrow();
        return Files.readAllBytes(Path.of(document.getQrPdfPath()));
    }

    public byte[] readQrImage(Long id) throws IOException {
        DocumentRecord document = repo.findById(id).orElseThrow();
        return Files.readAllBytes(Path.of(document.getQrImagePath()));
    }
}
