resource "yandex_mdb_kafka_cluster" "cryptotrade-kafka" {
  count               = var.is_kafka
  environment         = "PRESTABLE"
  name                = "cryptotrade-kafka"
  network_id          = yandex_vpc_network.hadoop-network.id
  security_group_ids  = [yandex_vpc_security_group.sg-hadoop-cluster.id, yandex_vpc_security_group.sg-kafka[count.index].id]
  deletion_protection = false

  user {
    name = var.kafka_user
    password = var.kafka_pwd
    permission {
      topic_name = "*"
      role       = "ACCESS_ROLE_ADMIN"
    }
  }
  config {
    assign_public_ip = true
    brokers_count    = 1
    version          = "3.0"
    kafka {
      resources {
        disk_size          = 11
        disk_type_id       = "network-ssd"
        resource_preset_id = "b3-c1-m4"
      }
      kafka_config {
        auto_create_topics_enable = true
      }
    }
#    zookeeper {
#      resources {
#        disk_size          = 1
#        disk_type_id       = "network-ssd"
#        resource_preset_id = "b3-c1-m4"
#      }
#    }

    zones = [
      local.hadoop_zone_id
    ]
  }
}