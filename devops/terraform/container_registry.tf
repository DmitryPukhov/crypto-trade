resource "yandex_container_registry" "cryptotrade_container_registry" {
  count     = var.is_container_registry
  folder_id = var.folder_id
  name      = "cryptotrade-container-registry"
}

resource yandex_container_repository cryptotrade_repo {
  count     = var.is_container_registry
  name = "${yandex_container_registry.cryptotrade_container_registry[count.index].id}/cryptotrade-repo"
}
#resource "docker_registry_image" "cryptotrade-kafka" {
#  name = "cryptotrade-kafka"
#  depends_on = [yandex_container_repository.cryptotrade_repo]
#  build {
#    context = "${path.cwd}/../deploy/kafka"
#  }
#}