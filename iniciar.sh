#!/usr/bin/env bash
set -euo pipefail

# Inicia los servicios, obtiene la URL temporal de Cloudflare y configura la aplicación.
docker compose up --build -d

echo "Preparando validación pública..."
PUBLIC_URL=""
for intento in $(seq 1 30); do
  PUBLIC_URL=$(docker compose logs tunnel --no-log-prefix 2>&1 \
    | grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' \
    | tail -n 1 || true)
  if [ -n "$PUBLIC_URL" ]; then
    break
  fi
  sleep 2
done

if [ -z "$PUBLIC_URL" ]; then
  echo "No se pudo obtener la URL pública de Cloudflare. Revisa: docker compose logs tunnel"
  exit 1
fi

printf 'APP_BASE_URL=%s\n' "$PUBLIC_URL" > .env
# Reinicia solo la aplicación para que los nuevos QR usen la URL pública encontrada.
docker compose up -d --force-recreate app

echo "Sistema listo en http://localhost:8080"
echo "La validación pública fue configurada automáticamente."
