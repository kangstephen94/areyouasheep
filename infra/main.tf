Oterraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
}

# --- AMI ---

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# --- Security Group ---

resource "aws_security_group" "app" {
  name        = "hottakeranker-sg"
  description = "Allow SSH and HTTP"

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "hottakeranker-sg"
  }
}

# --- EC2 Instance ---

resource "aws_instance" "app" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.app.id]

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  user_data = templatefile("${path.module}/userdata.sh", {
    db_password = var.db_password
    db_url      = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/hottakeranker"
    jwt_secret  = var.jwt_secret
    repo_url    = var.repo_url
  })

  depends_on = [aws_db_instance.postgres]

  tags = {
    Name = "hottakeranker"
  }
}

# --- Default VPC & Subnets (for RDS) ---

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# --- RDS Security Group ---

resource "aws_security_group" "rds" {
  name        = "hottakeranker-rds-sg"
  description = "Allow PostgreSQL from app EC2 only"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description     = "PostgreSQL from EC2"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "hottakeranker-rds-sg"
  }
}

# --- RDS Subnet Group ---

resource "aws_db_subnet_group" "default" {
  name       = "hottakeranker-db-subnets"
  subnet_ids = data.aws_subnets.default.ids

  tags = {
    Name = "hottakeranker-db-subnets"
  }
}

# --- RDS PostgreSQL ---

resource "aws_db_instance" "postgres" {
  identifier     = "hottakeranker"
  engine         = "postgres"
  engine_version = "17"
  instance_class = var.db_instance_class

  allocated_storage     = 20
  max_allocated_storage = 50
  storage_type          = "gp3"

  db_name  = "hottakeranker"
  username = "hottakeranker"
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.default.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false
  publicly_accessible = false
  skip_final_snapshot = true

  backup_retention_period = 0

  tags = {
    Name = "hottakeranker-db"
  }
}

# --- Elastic IP ---

resource "aws_eip" "app" {
  instance = aws_instance.app.id

  tags = {
    Name = "hottakeranker-eip"
  }
}
