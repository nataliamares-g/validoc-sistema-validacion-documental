package mx.unam.validoc.model;

public enum DocumentStatus {
    VIGENTE("Vigente", "VÁLIDO"),
    REVOCADO("Revocado", "REVOCADO"),
    CANCELADO("Cancelado", "CANCELADO");

    private final String label;
    private final String publicLabel;

    DocumentStatus(String label, String publicLabel) {
        this.label = label;
        this.publicLabel = publicLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getPublicLabel() {
        return publicLabel;
    }
}
