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
      subnet_ids       = data.aws_subnets.default.ids

      additional_security_group_ids = var.extra_node_security_groups
    }
  }

  enable_irsa = true

  cluster_endpoint_public_access        = true
  cluster_endpoint_public_access_cidrs  = ["218.235.249.173/32"]
}
#218.235.249.173/32 - 집 아이피

data "aws_eks_cluster_auth" "cluster" {
  name = module.eks.cluster_name

resource "kubernetes_service_account" "aws_lb_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.aws_lb_controller[0].arn
    }
  }
}

resource "helm_release" "aws_lb_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.4.8"

  values = [
    yamlencode({
      clusterName    = var.cluster_name
      serviceAccount = {
        create = false
        name   = kubernetes_service_account.aws_lb_controller.metadata[0].name
      }
      region       = var.aws_region
      vpcId        = data.aws_vpc.default.id
      ingressClass = "alb"
      image        = { tag = "v2.5.1" }
    })
  ]

  depends_on = [
    aws_iam_role.aws_lb_controller,
    aws_iam_policy_attachment.aws_lb_controller_attach,
    kubernetes_service_account.aws_lb_controller,
  ]
}
