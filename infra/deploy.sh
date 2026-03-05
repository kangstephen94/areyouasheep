#!/bin/bash
set -euo pipefail

# Usage: ./deploy.sh <path-to-key.pem>
# SSHs into the EC2 instance, pulls latest code, and restarts services.

if [ $# -lt 1 ]; then
  echo "Usage: $0 <path-to-key.pem>"
  exit 1
fi

KEY_FILE="$1"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Get the public IP from Terraform output
IP=$(cd "$SCRIPT_DIR" && terraform output -raw public_ip)

if [ -z "$IP" ]; then
  echo "Error: Could not get public IP from terraform output."
  echo "Make sure you have run 'terraform apply' first."
  exit 1
fi

echo "==> Redeploying on $IP ..."

ssh -o StrictHostKeyChecking=no -i "$KEY_FILE" ec2-user@"$IP" bash -s <<'REMOTE'
cd /opt/app
git pull

cd hottakeranker
sudo docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build --no-deps redis backend frontend
REMOTE

echo ""
echo "=== Redeploy complete! ==="
echo "App URL: http://$IP"
echo "SSH:     ssh -i $KEY_FILE ec2-user@$IP"
echo "Logs:    ssh -i $KEY_FILE ec2-user@$IP 'cd /opt/app/hottakeranker && sudo docker compose logs -f'"
