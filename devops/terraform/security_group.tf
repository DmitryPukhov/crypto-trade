######################################################
# Hadoop sg
######################################################

resource "yandex_vpc_security_group" "sg-hadoop-cluster" {
  description = "Security group for the Yandex Data Proc cluster"
  name        = "sg-hadoop-cluster"
  network_id  = yandex_vpc_network.hadoop-network.id

  ingress {
    description       = "Allow any traffic within one security group"
    protocol          = "ANY"
    from_port         = 0
    to_port           = 65535
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

resource "yandex_vpc_security_group" "sg-internet" {
  description = "Allow any outgoing traffic to the Internet"
  name        = "sg-internet"
  network_id  = yandex_vpc_network.hadoop-network.id

  egress {
    description    = "Allow any outgoing traffic to the Internet"
    protocol       = "ANY"
    from_port      = 0
    to_port        = 65535
    v4_cidr_blocks = [local.cidr_internet]
  }
}

resource "yandex_vpc_security_group" "sg-nat-instance" {
  description = "Security group for the NAT instance"
  name        = "sg-nat-instance"
  network_id  = yandex_vpc_network.hadoop-network.id

  ingress {
    description    = "Allow any outgoing traffic from the Yandex Data Proc cluster"
    protocol       = "ANY"
    from_port      = 0
    to_port        = 65535
    v4_cidr_blocks = [local.cidr_internet]
  }

  ingress {
    description    = "Allow any connections to the NAT instance"
    protocol       = "ANY"
    from_port         = 0
    to_port           = 65535
    v4_cidr_blocks = [local.cidr_internet]
  }
}

######################################################
# Postgresql sg
######################################################

resource "yandex_vpc_security_group" "sg-psql" {
  count = var.is_pgsql

  name       = "sg-psql"
  network_id = yandex_vpc_network.hadoop-network.id

  ingress {
    description    = "PostgreSQL"
    port           = 6432
    protocol       = "TCP"
    v4_cidr_blocks = [ "0.0.0.0/0" ]
  }
}