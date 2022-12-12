############################################################
# IAM, security
#############################################################
resource "yandex_vpc_security_group" "sg-airflow" {
  description = "Security group for the orchestrator"
  name        = "sg-airflow"
  network_id  = yandex_vpc_network.hadoop-network.id

  ingress {
    description    = "Allow any traffic within one security group"
    protocol       = "ANY"
    from_port      = 0
    to_port        = 65535
    v4_cidr_blocks = [local.cidr_internet]
  }
  egress {
    description    = "Allow any outgoing traffic to the Internet"
    protocol       = "ANY"
    from_port      = 0
    to_port        = 65535
    v4_cidr_blocks = [local.cidr_internet]
  }
}

# Airflow account
resource "yandex_iam_service_account" "sa-airflow" {
  name        = "cryptotrade-airflow"
  description = "Airflow service account"
}

# Create airflow account static access key
resource "yandex_iam_service_account_static_access_key" "key-sa-airflow" {
  service_account_id = yandex_iam_service_account.sa-airflow.id
  description        = "Static access key for airflow"
}

resource "yandex_resourcemanager_folder_iam_binding" "iam-airflow-admin" {
  folder_id = var.folder_id
  role      = "admin"
  members   = [
    "serviceAccount:${yandex_iam_service_account.sa-airflow.id}"
  ]
}

#############################################################
# Virtual machine
#############################################################
resource "yandex_compute_instance" "vm-airflow" {
  name        = "airflow"
  platform_id = "standard-v1"
  zone        = local.hadoop_zone_id

  resources {
    cores  = 2
    memory = 4
  }

  boot_disk {
    initialize_params {

      #  image_id:  fd8829m2l6d5lu2qip10
      #  family_id:  airflow-2x
      image_id = "fd8829m2l6d5lu2qip10"
    }
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.hadoop-subnet-ru-central-1b.id
    nat = true
  }

  metadata = {

    ssh-keys = "ubuntu:${file(var.airflow_public_key_file)}"
  }
}

