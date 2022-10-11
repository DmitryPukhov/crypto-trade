#############################################################
# Hadoop cluster
#############################################################
resource "yandex_dataproc_cluster" "cryptotrade-hadoop" {
  count =  var.is_hadoop
  bucket             = local.bucket_name
  name               = "cryptotrade-hadoop"
  description        = "Crypto trade hadoop cluster"
  service_account_id = yandex_iam_service_account.sa-hadoop.id
  zone_id            = local.hadoop_zone_id
  ui_proxy           = true
  security_group_ids = [yandex_vpc_security_group.sg-hadoop-cluster.id]
  cluster_config {
    version_id = "2.0"

    hadoop {
      # пример списка: ["HDFS", "YARN", "SPARK", "TEZ", "MAPREDUCE", "HIVE"]
      services = ["SPARK", "YARN", "LIVY", "HIVE"]

      ssh_public_keys = [file(var.hadoop_public_key_file)]
    }
    # Single node cluster
    subcluster_spec {
      name = "hadoop-master"
      role = "MASTERNODE"

      resources {
        resource_preset_id = "b2.small"
        disk_type_id       = "network-hdd"
        disk_size          = 20

      }
      subnet_id   = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
      hosts_count = 1
    }
    subcluster_spec {
      name = "hadoop-compute-node"
      role = "COMPUTENODE"
      resources {
        resource_preset_id = "b2.small" # 2 vCPU, 8 GB RAM
        disk_type_id       = "network-hdd"
        disk_size          = 20 # GB
      }
      subnet_id   = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
      hosts_count = 1
    }
  }
}
