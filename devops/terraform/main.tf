variable "oauth_token" {}
variable "cloud_id" {}
variable "folder_id" {}
variable "psql_pwd" {}
variable hadoop_public_key_file {}
# On/off components creation for dev
variable is_hadoop { default = 1 }
variable is_bucket { default = 1 }
variable is_pgsql { default = 1 }

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
  }
  required_version = ">= 0.13"
}

provider "yandex" {
  token     = var.oauth_token
  cloud_id  = var.cloud_id
  folder_id = var.folder_id
}