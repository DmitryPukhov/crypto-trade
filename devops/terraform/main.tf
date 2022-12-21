#############################################################
# Variables to be set in main.auto.tfvars file
#############################################################
variable "oauth_token" { default = "<please set the value in file main.auto.tfvars>" }
variable "cloud_id" { default = "<please set the value in file main.auto.tfvars>" }
variable "folder_id" { default = "<please set the value in file main.auto.tfvars>" }
variable "psql_pwd" { default = "<please set the value in file main.auto.tfvars>" }
variable "mongodb_pwd" { default = "<please set the value in file main.auto.tfvars>" }
variable "clickhouse_pwd" { default = "<please set the value in file main.auto.tfvars>" }
variable hadoop_public_key_file { default = "<please set the value in file main.auto.tfvars>" }
variable airflow_public_key_file { default = "<please set the value in file main.auto.tfvars>" }
variable kafka_user {default = "<please set kafka user in file main.auto.tfvars"}
variable kafka_pwd {default = "<please set kafka user password in file main.auto.tfvars"}

#############################################################
# Global vars
#############################################################
# On/off components creation for dev
variable is_hadoop { default = 1}
variable is_bucket { default = 1 }
variable is_pgsql { default = 1 }
variable is_clickhouse { default = 1 }
variable is_mongodb { default = 1 }
variable is_kafka { default = 1 }
variable is_k8s {default = 1}
variable is_container_registry{default=1}

locals {
  bucket_name           = "dmitrypukhov-cryptotrade"
  spark_jar_name        = "crypto-trade-assembly-0.1.0-SNAPSHOT.jar"
  hadoop_zone_id        = "ru-central1-b"
  # An image ID for a NAT instance. See https://cloud.yandex.ru/marketplace/products/yc/nat-instance-ubuntu-18-04-lts for details.
  nat_instance_image_id = "fd82fnsvr0bgt1fid7cl"
  cidr_internet         = "0.0.0.0/0"
}

#############################################################
# General cloud parameters
#############################################################
terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
    docker = {
      source = "kreuzwerker/docker"
      version = "2.11.0"
    }
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  token     = var.oauth_token
  cloud_id  = var.cloud_id
  folder_id = var.folder_id
}