#############################################################
# Bucket with some data
#############################################################
# Bucket with access for hadoop user
resource "yandex_storage_bucket" "cryptotrade-bucket" {
  count = var.is_bucket
  access_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.access_key
  secret_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.secret_key
  bucket = local.bucket_name
  force_destroy = true
}
#
## Put spark jar to the bucket
#resource "yandex_storage_object" "spark-jar" {
#  count = var.is_bucket
#  access_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.access_key
#  secret_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.secret_key
#  bucket     = yandex_storage_bucket.cryptotrade[count.index].bucket
#  key        = "app/${local.spark_jar_name}"
#  source     = "./../deploy/${local.spark_jar_name}"
#}

# btcusdt test data
resource "yandex_storage_object" "data-btcusdt" {
  count = var.is_bucket
  access_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.access_key
  secret_key = yandex_iam_service_account_static_access_key.key-sa-hadoop.secret_key
  bucket     = yandex_storage_bucket.cryptotrade-bucket[count.index].bucket
  key        = "external/btcusdt/btcusdt_kline_1m.csv"
  source     = "./../../data/external/btcusdt/btcusdt_kline_1m.csv"
}