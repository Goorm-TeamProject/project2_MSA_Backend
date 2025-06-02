variable "aws_region"       { type = string }
variable "cluster_name"     { type = string }


variable "vpc_cidr" {
  type    = string
  default = ""
}

variable "public_subnets" {
  type    = list(string)
  default = []
}

variable "private_subnets" {
  type    = list(string)
  default = []
}

variable "extra_node_security_groups" {
  type    = list(string)
  default = []
}

variable "domain_name"      { type = string }
variable "hosted_zone_id"   { type = string }
variable "static_bucket"    { type = string }

# ★ 신규 추가: AWS LB Controller IAM 생성 여부
variable "create_iam_for_lb_controller" {
  type        = bool
  default     = true
  description = "AWS Load Balancer Controller 설치를 위한 IAM 리소스를 생성할지 여부"
}
