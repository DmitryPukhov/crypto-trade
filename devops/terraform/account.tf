############################################################
# IAM, accounts
#############################################################
# Hadoop account
resource "yandex_iam_service_account" "sa-hadoop" {
  name        = "cryptotrade-hadoop"
  description = "Hadoop service account"
}

# Create hadoop account static access key
resource "yandex_iam_service_account_static_access_key" "key-sa-hadoop" {
  service_account_id = yandex_iam_service_account.sa-hadoop.id
  description        = "Static access key for object storage"
}

resource "yandex_resourcemanager_folder_iam_binding" "iam-mdb-dataproc-agent" {
  folder_id = var.folder_id
  role      = "mdb.dataproc.agent"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}",
    "serviceAccount:${yandex_iam_service_account.sa-airflow.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-dataproc-agent" {
  folder_id = var.folder_id
  role      = "dataproc.agent"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}",
    "serviceAccount:${yandex_iam_service_account.sa-airflow.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-editor" {
  folder_id = var.folder_id
  role      = "editor"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}",
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}",
    "serviceAccount:${yandex_iam_service_account.sa-airflow.id}"
  ]
}

########################## k8s
resource "yandex_iam_service_account" "sa-k8s" {
  name        = "cryptotrade-k8s"
  description = "K8s service account"
}

# Roles for K8s
resource "yandex_resourcemanager_folder_iam_binding" "iam-k8s-admin" {
  folder_id = var.folder_id
  role      = "k8s.admin"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-k8s-cluster-api-cluster-admin" {
  folder_id = var.folder_id
  role      = "k8s.cluster-api.cluster-admin"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}

resource "yandex_resourcemanager_folder_iam_binding" "iam-vpc-public-admin" {
  folder_id = var.folder_id
  role      = "vpc.publicAdmin"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-k8s-images-puller" {
  # Сервисному аккаунту назначается роль "container-registry.images.puller".
  folder_id = var.folder_id
  role      = "container-registry.images.puller"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-k8s-alb-editor" {
  # Сервисному аккаунту назначается роль "container-registry.images.puller".
  folder_id = var.folder_id
  role      = "alb.editor"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-k8s-certificate-downloader" {
  # Сервисному аккаунту назначается роль "container-registry.images.puller".
  folder_id = var.folder_id
  role      = "certificate-manager.certificates.downloader"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-k8s.id}"
  ]
}