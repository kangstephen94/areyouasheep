variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "key_name" {
  description = "Name of an existing EC2 key pair for SSH access"
  type        = string
}

variable "db_password" {
  description = "PostgreSQL password for production"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret for production"
  type        = string
  sensitive   = true
}

variable "repo_url" {
  description = "Public Git repository URL to clone"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH into the instance"
  type        = string
  default     = "0.0.0.0/0"
}
