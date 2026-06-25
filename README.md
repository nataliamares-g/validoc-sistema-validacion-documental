# VALIDOC — Sistema de Validación Documental con Código QR

Aplicación web individual para registrar documentos PDF, generar un folio único, crear e insertar un código QR y validar públicamente el estado de un documento.

## Funciones

- Inicio de sesión para usuarios internos.
- Registro de documentos PDF con título, tipo y área emisora.
- Folio único y código QR por documento.
- PDF original, imagen QR y PDF con QR guardados.
- Página final de validación con QR; el contenido original del PDF no se modifica.
- Repositorio de documentos con descargas, liga de validación, cambio de estado y eliminación.
- Estados: Vigente, Revocado y Cancelado.
- Validación pública sin inicio de sesión: Válido, Revocado, Cancelado o No encontrado.
- Ejecución con Docker Compose: aplicación, base de datos y túnel temporal.

## Tecnologías

- Java 21 y Spring Boot.
- Spring Security.
- Thymeleaf, HTML y CSS.
- MySQL 8.4.
- PDFBox.
- ZXing.
- Docker Compose.
- Cloudflare Tunnel.

## Ejecutar

Desde la carpeta del proyecto:

```bash
bash iniciar.sh
```

El script inicia Docker Compose, obtiene la dirección pública temporal de Cloudflare y la configura internamente. El usuario que registra un PDF no tiene que escribir ninguna URL.

La aplicación se abre en:

```text
http://localhost:8080
```

Para detenerla:

```bash
docker compose down
```

No uses `docker compose down -v` si deseas conservar la base de datos y los documentos.

## Usuarios de prueba

| Usuario | Contraseña |
|---|---|
| admin@validoc.mx | Admin123* |
| capturista@validoc.mx | Captura123* |


