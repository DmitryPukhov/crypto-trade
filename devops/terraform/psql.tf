resource "yandex_mdb_postgresql_cluster" "pgsql" {
  count              = var.is_pgsql
  name               = "cryptotrade"

  environment        = "PRESTABLE"
  network_id         = yandex_vpc_network.hadoop-network.id
  security_group_ids = [
    yandex_vpc_security_group.pgsql-sg[count.index].id, yandex_vpc_security_group.sg-hadoop-cluster.id
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

    resources {
      resource_preset_id = "s2.micro"
      disk_type_id       = "network-ssd"
      disk_size          = "20"
    }
  }

  host {
    name      = "cryptotrade"
    zone      = local.hadoop_zone_id
    subnet_id = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    assign_public_ip = true
  }
}
