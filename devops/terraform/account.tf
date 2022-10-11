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

resource "yandex_resourcemanager_folder_iam_binding" "iam-hadoop-mdb-dataproc-agent" {
  folder_id = var.folder_id
  role      = "mdb.dataproc.agent"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-hadoop-dataproc-agent" {
  folder_id = var.folder_id
  role      = "dataproc.agent"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}"
  ]
}
resource "yandex_resourcemanager_folder_iam_binding" "iam-hadoop-editor" {
  folder_id = var.folder_id
  role      = "editor"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-hadoop.id}"
  ]
}