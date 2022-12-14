resource "yandex_mdb_clickhouse_cluster" "cryptotrade-clickhouse" {
  count       = var.is_clickhouse
  name        = "cryptotrade-clickhouse"
  environment = "PRESTABLE"
  network_id  = yandex_vpc_network.hadoop-network.id
  access {
    data_lens = true
    serverless = true
    web_sql = true
    metrika = true

  }
  security_group_ids = [
    yandex_vpc_security_group.sg-clickhouse[count.index].id, yandex_vpc_security_group.sg-hadoop-cluster.id
  ]

  deletion_protection = false
  clickhouse {
    config {
      kafka {
        security_protocol = "SECURITY_PROTOCOL_SASL_SSL"
        sasl_mechanism    = "SASL_MECHANISM_SCRAM_SHA_512"
        sasl_username     = var.kafka_user
        sasl_password     = var.kafka_pwd
      }
    }
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
