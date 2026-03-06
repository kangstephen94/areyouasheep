#!/bin/bash
set -euxo pipefail

# --- Install Docker ---
dnf install -y docker git
systemctl enable docker
systemctl start docker
usermod -aG docker ec2-user

# --- Install Docker Compose plugin ---
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# --- Install Docker Buildx plugin ---
curl -SL "https://github.com/docker/buildx/releases/download/v0.19.3/buildx-v0.19.3.linux-amd64" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx
chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx

# --- Clone repo ---
git clone ${repo_url} /opt/app
chown -R ec2-user:ec2-user /opt/app

# --- Write production docker-compose override ---
cat > /opt/app/hottakeranker/docker-compose.prod.yml <<'COMPOSEEOF'
services:
  redis:
    restart: unless-stopped

  backend:
    environment:
      DB_URL: "${db_url}"
      DB_USERNAME: "hottakeranker"
      DB_PASSWORD: "${db_password}"
      JWT_SECRET: "${jwt_secret}"
    depends_on:
      redis:
        condition: service_healthy
    restart: unless-stopped

  frontend:
    ports:
      - "80:80"
    restart: unless-stopped
COMPOSEEOF

# --- Start services ---
cd /opt/app/hottakeranker
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
