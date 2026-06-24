package mx.unam.validoc.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="documents")
public class DocumentRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String folio;
 @Column(nullable=false) private String title;
 @Column(nullable=false) private String documentType;
 @Column(nullable=false) private String issuerArea;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private DocumentStatus status=DocumentStatus.VIGENTE;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private QrPosition qrPosition;
 @Column(nullable=false,length=700) private String originalPath;
 @Column(nullable=false,length=700) private String qrPdfPath;
 @Column(nullable=false,length=700) private String qrImagePath;
 @Column(nullable=false,length=700) private String validationUrl;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(nullable=false) private String registeredBy;
 public DocumentRecord(){}
 public Long getId(){return id;} public String getFolio(){return folio;} public String getTitle(){return title;} public String getDocumentType(){return documentType;} public String getIssuerArea(){return issuerArea;} public DocumentStatus getStatus(){return status;} public QrPosition getQrPosition(){return qrPosition;} public String getOriginalPath(){return originalPath;} public String getQrPdfPath(){return qrPdfPath;} public String getQrImagePath(){return qrImagePath;} public String getValidationUrl(){return validationUrl;} public LocalDateTime getCreatedAt(){return createdAt;} public String getRegisteredBy(){return registeredBy;}
 public void setFolio(String x){folio=x;} public void setTitle(String x){title=x;} public void setDocumentType(String x){documentType=x;} public void setIssuerArea(String x){issuerArea=x;} public void setStatus(DocumentStatus x){status=x;} public void setQrPosition(QrPosition x){qrPosition=x;} public void setOriginalPath(String x){originalPath=x;} public void setQrPdfPath(String x){qrPdfPath=x;} public void setQrImagePath(String x){qrImagePath=x;} public void setValidationUrl(String x){validationUrl=x;} public void setRegisteredBy(String x){registeredBy=x;}
}
