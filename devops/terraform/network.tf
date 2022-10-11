#############################################################
# Hadoop network with subnets
#############################################################

resource "yandex_vpc_network" "hadoop-network" {
  name        = "hadoop-network"
  description = "Network for crypto-trade cluster and NAT instance"
}

resource "yandex_vpc_subnet" "hadoop-subnet-ru-central-1b" {
  description    = "Subnet for crypto trade cluster"
  name           = "hadoop-ru-central-1b"
  zone           = local.hadoop_zone_id
  network_id     = yandex_vpc_network.hadoop-network.id
  v4_cidr_blocks = ["192.168.1.0/24"]
  route_table_id = yandex_vpc_route_table.route-table-nat.id
}

resource "yandex_vpc_gateway" "nat_gateway" {
  name = "nat-gateway"
  shared_egress_gateway  {}
}

resource "yandex_vpc_route_table" "route-table-nat" {
  description = "Route table for Data Proc cluster subnet"
  # All requests can be forwarded to the NAT instance IP address.
  name        = "route-table-nat"

  network_id = yandex_vpc_network.hadoop-network.id

  static_route {
    destination_prefix = local.cidr_internet
    gateway_id = yandex_vpc_gateway.nat_gateway.id
  }
}