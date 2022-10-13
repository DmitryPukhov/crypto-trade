resource "yandex_mdb_clickhouse_cluster" "cryptotrade-clickhouse" {
  count       = var.is_pgsql
  name        = "cryptotrade-clickhouse"
  environment = "PRESTABLE"
  network_id  = yandex_vpc_network.hadoop-network.id

  security_group_ids = [
    yandex_vpc_security_group.sg-clickhouse[count.index].id, yandex_vpc_security_group.sg-hadoop-cluster.id
  ]

  deletion_protection = false
  clickhouse {
    resources {
      resource_preset_id = "b3-c1-m4"
      disk_type_id       = "network-hdd"
      disk_size          = 10
    }
  }

  database {
    name  = "cryptotrade"
  }
  user {
    name     = "cryptotrade"
    password = var.psql_pwd
    permission {
      database_name = "cryptotrade"
    }
  }

  host {
    type             = "CLICKHOUSE"
    zone             = local.hadoop_zone_id
    subnet_id        = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    assign_public_ip = true
  }
}
