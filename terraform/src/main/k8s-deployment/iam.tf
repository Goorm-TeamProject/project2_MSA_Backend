// iam.tf
# EKS 클러스터/노드 그룹용 IAM 역할/정책은 eks 모듈에서 자동 생성

# EKS Cluster IAM Role
data "aws_iam_policy_document" "eks_cluster_assume_role_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["eks.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "eks_cluster_role" {
  name               = "${var.cluster_name}-cluster-role"
  assume_role_policy = data.aws_iam_policy_document.eks_cluster_assume_role_policy.json
}


#EKS Worker Node IAM Role
data "aws_iam_policy_document" "eks_worker_assume_role_policy" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "eks_worker_node_role" {
  name               = "${var.cluster_name}-node-role"
  assume_role_policy = data.aws_iam_policy_document.eks_worker_assume_role_policy.json
}

resource "aws_iam_role_policy_attachment" "eks_worker_node_attach" {
  for_each = {
    "AmazonEKSWorkerNodePolicy"        = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
    "AmazonEKS_CNI_Policy"              = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
    "AmazonEC2ContainerRegistryReadOnly" = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  }
  role       = aws_iam_role.eks_worker_node_role.name
  policy_arn = each.value
}

#AWS LoadBalancer Controller OIDC Provider
data "tls_certificate" "lb_controller_thumbprint" {
  url = module.eks.cluster_oidc_issuer_url
}

resource "aws_iam_openid_connect_provider" "eks" {
  count          = var.create_iam_for_lb_controller ? 1 : 0
  url            = module.eks.cluster_oidc_issuer_url
  client_id_list = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.lb_controller_thumbprint.certificates[0].sha1_fingerprint]
}

# AWS LB Controller IAM Role
data "aws_iam_policy_document" "lb_controller_trust" {
  statement {
    effect = "Allow"
    principals {
      type        = "Federated"
      identifiers = [ aws_iam_openid_connect_provider.eks[0].arn ]
    }
    actions = ["sts:AssumeRoleWithWebIdentity"]
    condition {
      test     = "StringEquals"
      variable = "${replace(aws_iam_openid_connect_provider.eks[0].url, "https://", "")}:sub"
      values   = ["system:serviceaccount:kube-system:aws-load-balancer-controller"]
    }
  }
}

#IAM Role
resource "aws_iam_role" "aws_lb_controller" {
  count               = var.create_iam_for_lb_controller ? 1 : 0
  name                = "${var.cluster_name}-aws-lb-controller"
  assume_role_policy  = data.aws_iam_policy_document.lb_controller_trust.json
}

#IAM
resource "aws_iam_policy" "aws_lb_controller_policy" {
  count       = var.create_iam_for_lb_controller ? 1 : 0
  name        = "${var.cluster_name}-aws-lb-controller-policy"
  description = "IAM policy for AWS Load Balancer Controller"
  policy      = file("${path.module}/policies/aws-lb-controller.json")
}

# IAM Policy
resource "aws_iam_policy_attachment" "aws_lb_controller_attach" {
  count      = var.create_iam_for_lb_controller ? 1 : 0
  name       = "${var.cluster_name}-aws-lb-controller-attach"
  policy_arn = aws_iam_policy.aws_lb_controller_policy[0].arn
  roles      = [ aws_iam_role.aws_lb_controller[0].name ]
}
