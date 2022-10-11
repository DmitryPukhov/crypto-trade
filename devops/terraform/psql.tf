resource "yandex_mdb_postgresql_cluster" "cryptotrade-psql" {
  count              = var.is_pgsql
  name               = "cryptotrade-psql"
  environment        = "PRESTABLE"
  network_id         = yandex_vpc_network.hadoop-network.id

  security_group_ids = [
    yandex_vpc_security_group.sg-psql[count.index].id, yandex_vpc_security_group.sg-hadoop-cluster.id
  ]

  deletion_protection = false

  database {
    name  = "cryptotrade"
    owner = "cryptotrade"
  }
  user {
    name     = "cryptotrade"
    password = var.psql_pwd
  }

  config {
    version = 14
    access {
      data_lens = true
      web_sql = true
      serverless = true
      data_transfer = true
    }
    resources {
      resource_preset_id = "b2.nano"
      disk_type_id       = "network-hdd"
      disk_size          = "10"
    }
  }

  host {
    name      = "cryptotrade-psql"
    zone      = local.hadoop_zone_id
    subnet_id = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    assign_public_ip = true

  }
}
