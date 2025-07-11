output "cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "cluster_ca_certificate" {
  value = module.eks.cluster_certificate_authority_data
}

output "cluster_name" {
  value = module.eks.cluster_id
}

output "node_group_role_arn" {
  value = aws_iam_role.eks_worker_node_role.arn
}

output "eks_node_ids" {
  description = "List of EKS worker node IDs found by data source"
  value       = data.aws_instances.eks_nodes.ids
}
