data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [ data.aws_vpc.default.id ]
  }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "19.0.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.33"
  vpc_id          = data.aws_vpc.default.id
  subnet_ids      = data.aws_subnets.default.ids

  eks_managed_node_groups = {
    app = {
      desired_capacity = 3
      max_capacity     = 5
      min_capacity     = 2
      instance_types   = ["t3.large"]

      subnet_ids = ["subnet-0fd75995cb5c79867"]

      additional_security_group_ids = var.extra_node_security_groups
    }
  }

  enable_irsa = true

  cluster_endpoint_public_access        = true
  cluster_endpoint_public_access_cidrs  = ["0.0.0.0/0"]
}

data "aws_eks_cluster_auth" "cluster" {
  name = module.eks.cluster_name

# ───────────────────────────────
# ALB Ingress Controller 관련 리소스
# ───────────────────────────────

resource "aws_iam_policy" "aws_lb_controller" {
  count = var.create_iam_for_lb_controller ? 1 : 0
  name   = "AWSLoadBalancerControllerIAMPolicy"
  policy = file("${path.module}/policies/aws-lb-controller.json")
}

resource "aws_iam_role_policy_attachment" "aws_lb_controller_attach" {
  count      = var.create_iam_for_lb_controller ? 1 : 0
  policy_arn = aws_iam_policy.aws_lb_controller[0].arn
  role       = aws_iam_role.aws_lb_controller[0].name
}

# 2. Kubernetes Service Account (IRSA)
resource "kubernetes_service_account" "aws_lb_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.aws_lb_controller[0].arn
    }
  }
}

# 3. Helm Release for ALB Ingress Controller (v2.7.0+)
resource "helm_release" "aws_lb_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.7.2"  # 2024년 6월 기준 최신 chart 확인

  values = [
    yamlencode({
      clusterName    = var.cluster_name
      region         = var.aws_region
      vpcId          = data.aws_vpc.default.id
      ingressClass   = "alb"
      serviceAccount = {
        create = false
        name   = kubernetes_service_account.aws_lb_controller.metadata[0].name
      }
      image = {
        tag = "v2.7.1"
      }
    })
  ]

  depends_on = [
    aws_iam_role.aws_lb_controller,
    aws_iam_role_policy_attachment.aws_lb_controller_attach,
    kubernetes_service_account.aws_lb_controller,
  ]
}
