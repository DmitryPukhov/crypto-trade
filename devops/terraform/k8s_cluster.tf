resource "yandex_kubernetes_cluster" "cryptotrade-k8s" {
  network_id = yandex_vpc_network.hadoop-network.id
  name = "cryptotrade-k8s"
  master {
    zonal {
      zone      = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.zone
      subnet_id = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    }
  }
  service_account_id      = yandex_iam_service_account.sa-hadoop.id
  node_service_account_id = yandex_iam_service_account.sa-hadoop.id
  depends_on              = [
    yandex_resourcemanager_folder_iam_binding.editor,
    yandex_resourcemanager_folder_iam_binding.images-puller
  ]
}

#resource "yandex_iam_service_account" "<имя сервисного аккаунта>" {
#name = "<имя сервисного аккаунта>"
#description = "<описание сервисного аккаунта>"
#}

resource "yandex_resourcemanager_folder_iam_binding" "editor" {
  # Сервисному аккаунту назначается роль "editor".
  folder_id = var.folder_id
  role      = "editor"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}"
  ]
}

resource "yandex_resourcemanager_folder_iam_binding" "images-puller" {
  # Сервисному аккаунту назначается роль "container-registry.images.puller".
  folder_id = var.folder_id
  role      = "container-registry.images.puller"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}"
  ]
}

