resource "kubernetes_deployment" "this" {
  metadata {
    name = var.name
    labels = var.labels
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = var.labels
    }

    template {
      metadata {
        labels = var.labels
      }

      spec {
        container {
          name  = var.name
          image = var.image

          port {
            container_port = var.container_port
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "this" {
  metadata {
    name = var.name
  }

  spec {
    selector = var.labels
    port {
      port        = var.service_port
      target_port = var.container_port
    }

    type = var.service_type
  }
}
