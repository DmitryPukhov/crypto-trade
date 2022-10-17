resource "yandex_mdb_mongodb_cluster" "cryptotrade-mongodb" {
  count       = var.is_mongodb
  name        = "cryptotrade-mongodb"
  environment = "PRESTABLE"
  network_id  = yandex_vpc_network.hadoop-network.id

  security_group_ids = [
    yandex_vpc_security_group.sg-mongodb[count.index].id, yandex_vpc_security_group.sg-hadoop-cluster.id
  ]

  deletion_protection = false
  cluster_config { version = "5.0" }

  database { name = "cryptotrade" }
  user {
    name     = "cryptotrade"
    password = var.psql_pwd
    permission {
      database_name = "cryptotrade"
      roles         = ["readWrite"]
    }
  }
  resources {
    resource_preset_id = "b3-c1-m4"
    disk_size          = 10
    disk_type_id       = "network-hdd"
  }
  host {
    zone_id          = local.hadoop_zone_id
    subnet_id        = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    assign_public_ip = true
  }
}
