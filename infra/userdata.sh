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

# --- Clone repo ---
git clone ${repo_url} /opt/app
chown -R ec2-user:ec2-user /opt/app

# --- Write production docker-compose override ---
cat > /opt/app/hottakeranker/docker-compose.prod.yml <<COMPOSEEOF
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: "${db_password}"
    restart: unless-stopped

  redis:
    restart: unless-stopped

  backend:
    environment:
      DB_PASSWORD: "${db_password}"
      JWT_SECRET: "${jwt_secret}"
    restart: unless-stopped

  frontend:
    ports:
      - "80:80"
    restart: unless-stopped
COMPOSEEOF

# --- Start services ---
cd /opt/app/hottakeranker
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
