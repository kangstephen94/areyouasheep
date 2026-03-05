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

# --- Install Certbot ---
dnf install -y certbot

# --- Get SSL certificate (before Docker starts, so port 80 is free) ---
certbot certonly --standalone \
  -d ${domain} -d www.${domain} \
  --non-interactive --agree-tos \
  --register-unsafely-without-email

# --- Clone repo ---
git clone ${repo_url} /opt/app
chown -R ec2-user:ec2-user /opt/app

# --- Write SSL nginx config for production ---
cat > /opt/app/hottakeranker-frontend/nginx.ssl.conf <<'NGINXEOF'
server {
    listen 80;
    server_name ${domain} www.${domain};

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://${domain}$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name www.${domain};

    ssl_certificate /etc/letsencrypt/live/${domain}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${domain}/privkey.pem;

    return 301 https://${domain}$request_uri;
}

server {
    listen 443 ssl;
    server_name ${domain};

    ssl_certificate /etc/letsencrypt/live/${domain}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${domain}/privkey.pem;

    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript image/svg+xml;
    gzip_min_length 256;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "ok";
        add_header Content-Type text/plain;
    }

    # Proxy API requests to backend
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    # Vite content-hashed assets — long cache
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINXEOF

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
      - "443:443"
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt:ro
      - /var/www/certbot:/var/www/certbot:ro
      - /opt/app/hottakeranker-frontend/nginx.ssl.conf:/etc/nginx/conf.d/default.conf:ro
    restart: unless-stopped
COMPOSEEOF

# --- Create certbot webroot directory ---
mkdir -p /var/www/certbot

# --- Start services ---
cd /opt/app/hottakeranker
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build --no-deps redis backend frontend

# --- Set up automatic certificate renewal ---
cat > /etc/cron.d/certbot-renew <<'CRONEOF'
0 3 * * * root certbot renew --webroot -w /var/www/certbot --quiet && cd /opt/app/hottakeranker && docker compose -f docker-compose.yml -f docker-compose.prod.yml exec frontend nginx -s reload
CRONEOF
