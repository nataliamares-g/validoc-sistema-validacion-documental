package mx.unam.validoc.controller;

import java.security.Principal;
import java.util.List;
import mx.unam.validoc.model.DocumentRecord;
import mx.unam.validoc.model.DocumentStatus;
import mx.unam.validoc.model.QrPosition;
import mx.unam.validoc.repository.DocumentRepository;
import mx.unam.validoc.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private final DocumentRepository docs;
    private final DocumentService service;

    public WebController(DocumentRepository docs, DocumentService service) {
        this.docs = docs;
        this.service = service;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/")
    String home(Model model) {
        model.addAttribute("total", docs.count());
        model.addAttribute("vigentes", docs.findAll().stream()
                .filter(document -> document.getStatus() == DocumentStatus.VIGENTE)
                .count());
        return "dashboard";
    }

    @GetMapping("/documentos/nuevo")
    String form(Model model) {
        model.addAttribute("positions", List.of(
                QrPosition.SUPERIOR_DERECHA,
                QrPosition.SUPERIOR_IZQUIERDA,
                QrPosition.INFERIOR_DERECHA,
                QrPosition.INFERIOR_IZQUIERDA));
        return "document-form";
    }

    @PostMapping("/documentos")
    String save(
            @RequestParam String title,
            @RequestParam String documentType,
            @RequestParam String issuerArea,
            @RequestParam QrPosition qrPosition,
            @RequestParam MultipartFile pdf,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            DocumentRecord document = service.create(
                    title,
                    documentType,
                    issuerArea,
                    qrPosition,
                    pdf,
                    principal.getName());
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Documento registrado. Se guardaron el PDF original, el QR y el PDF con QR. Folio: "
                            + document.getFolio());
            return "redirect:/documentos";
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/documentos/nuevo";
        }
    }

    @GetMapping("/documentos")
    String list(Model model) {
        model.addAttribute("documents", docs.findAll().stream()
                .sorted((first, second) -> second.getCreatedAt().compareTo(first.getCreatedAt()))
                .toList());
        return "documents";
    }

    @PostMapping("/documentos/{id}/estado")
    String status(
            @PathVariable Long id,
            @RequestParam DocumentStatus status,
            RedirectAttributes redirectAttributes) {
        DocumentRecord document = docs.findById(id).orElseThrow();
        document.setStatus(status);
        docs.save(document);
        redirectAttributes.addFlashAttribute(
                "success",
                "Estado actualizado a " + status.getLabel()
                        + ". Abre la página pública para comprobar el cambio.");
        return "redirect:/documentos";
    }

    @PostMapping("/documentos/{id}/eliminar")
    String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("success", "Documento eliminado.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "No fue posible eliminar el documento.");
        }
        return "redirect:/documentos";
    }

    @GetMapping("/documentos/{id}/original")
    ResponseEntity<byte[]> downloadOriginal(@PathVariable Long id) throws Exception {
        DocumentRecord document = docs.findById(id).orElseThrow();
        return pdfDownload(document.getFolio() + "-original.pdf", service.readOriginalPdf(id));
    }

    @GetMapping("/documentos/{id}/descargar")
    ResponseEntity<byte[]> downloadQrPdf(@PathVariable Long id) throws Exception {
        DocumentRecord document = docs.findById(id).orElseThrow();
        return pdfDownload(document.getFolio() + "-QR.pdf", service.readQrPdf(id));
    }

    @GetMapping("/documentos/{id}/qr")
    ResponseEntity<byte[]> downloadQrImage(@PathVariable Long id) throws Exception {
        DocumentRecord document = docs.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFolio() + "-QR.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(service.readQrImage(id));
    }

    private ResponseEntity<byte[]> pdfDownload(String filename, byte[] content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    /** Esta ruta es pública y nunca requiere inicio de sesión. */
    @GetMapping("/validar/{folio}")
    String validate(@PathVariable String folio, Model model) {
        var document = docs.findByFolio(folio);
        model.addAttribute("found", document.isPresent());
        document.ifPresent(value -> model.addAttribute("document", value));
        return "validation";
    }
}
