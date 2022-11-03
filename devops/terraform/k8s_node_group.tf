resource "yandex_kubernetes_node_group" "cryptotrade_k8s_node_group" {
  cluster_id = yandex_kubernetes_cluster.cryptotrade-k8s.id
  instance_template {
    platform_id = "standard-v2"
    resources {
      core_fraction = 20
    }
    container_runtime {
      type = "containerd"
    }
  }
  scale_policy {
    fixed_scale {
      size = 1
    }
  }
}