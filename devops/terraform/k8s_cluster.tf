resource "yandex_kubernetes_cluster" "cryptotrade-k8s" {
  count = var.is_k8s

  network_id = yandex_vpc_network.hadoop-network.id
  name = "cryptotrade-k8s"
  master {
    zonal {
      zone      = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.zone
      subnet_id = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    }
    public_ip = true
  }
  service_account_id      = yandex_iam_service_account.sa-hadoop.id
  node_service_account_id = yandex_iam_service_account.sa-hadoop.id
  depends_on              = [
    yandex_resourcemanager_folder_iam_binding.iam-editor,
    yandex_resourcemanager_folder_iam_binding.iam-k8s-admin,
    yandex_resourcemanager_folder_iam_binding.iam-k8s-images-puller
  ]
}