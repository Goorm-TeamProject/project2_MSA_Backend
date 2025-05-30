variable "name" {}
variable "labels" { type = map(string) }
variable "replicas" { default = 2 }
variable "image" {}
variable "container_port" { default = 8080 }
variable "service_port" { default = 80 }
variable "service_type" { default = "ClusterIP" }
