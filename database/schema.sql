-- Esquema de referencia para la base de datos VALIDOC.
-- La aplicación también crea/actualiza las tablas mediante JPA/Hibernate al iniciar.

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS documents (
  id BIGINT NOT NULL AUTO_INCREMENT,
  folio VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  document_type VARCHAR(255) NOT NULL,
  issuer_area VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  qr_position VARCHAR(255) NOT NULL,
  original_path VARCHAR(700) NOT NULL,
  qr_pdf_path VARCHAR(700) NOT NULL,
  qr_image_path VARCHAR(700) NOT NULL,
  validation_url VARCHAR(700) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  registered_by VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_documents_folio (folio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
