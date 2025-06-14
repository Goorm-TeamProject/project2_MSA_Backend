# Terraform configuration to create an ALB for EKS NodePort (30080)

# Variables
variable "certificate_arn" {
  description = "ACM Certificate ARN for ALB HTTPS listener"
  type        = string
}

# Use existing default VPC and subnets from eks.tf
# Assumes data "aws_vpc" "default" and data "aws_subnets" "default" are declared in eks.tf

# Security Group for the ALB
resource "aws_security_group" "alb" {
  name        = "eouil-alb-sg"
  description = "Security group for EOUIL ALB"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "Allow HTTPS from anywhere"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow HTTP (health checks)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Application Load Balancer
resource "aws_lb" "eouil" {
  name               = "eouil-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = data.aws_subnets.default.ids
}

# 수정: Health check 경로 오타 및 구조 정리
resource "aws_lb_target_group" "nodes" {
  name        = "eouil-tg"
  port        = 30080
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "instance"

  health_check {
    path                = "/actuator/health"
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
    matcher             = "200-399"
  }
}

# Discover EC2 instance IDs for the EKS worker nodes
data "aws_instances" "eks_nodes" {
  filter {
    name   = "tag:eks:cluster-name"
    values = ["Eouil-eks-cluster"]
  }
  filter {
    name   = "tag:eks:nodegroup-name"
    values = ["app-20250607142833687100000001"]
  }
}

# Target group attachment
resource "aws_lb_target_group_attachment" "nodes" {
  for_each = toset(data.aws_instances.eks_nodes.ids)

  target_group_arn = aws_lb_target_group.nodes.arn
  target_id        = each.key
  port             = 30080
}

# HTTPS Listener (443) → forward to NodePort TG
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.eouil.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"  # Updated to a valid TLS 1.2 policy
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.nodes.arn
  }
}

# HTTP Listener (80) → redirect to HTTPS
resource "aws_lb_listener" "http_redirect" {
  load_balancer_arn = aws_lb.eouil.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}
