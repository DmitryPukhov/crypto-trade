import logging
import yandex.cloud.dataproc.v1.cluster_service_pb2 as cluster_service_pb
import yandex.cloud.dataproc.v1.cluster_service_pb2_grpc as cluster_service_grpc_pb
import yandexcloud
from yandex.cloud.iam.v1.service_account_service_pb2 import ListServiceAccountsRequest
from yandex.cloud.iam.v1.service_account_service_pb2_grpc import ServiceAccountServiceStub
from yandex.cloud.resourcemanager.v1.cloud_service_pb2 import ListCloudsRequest
from yandex.cloud.resourcemanager.v1.cloud_service_pb2_grpc import CloudServiceStub
from yandex.cloud.resourcemanager.v1.folder_service_pb2 import ListFoldersRequest
from yandex.cloud.resourcemanager.v1.folder_service_pb2_grpc import FolderServiceStub
from AppTool import AppTool


class CloudTool:
    """   A tool to grab info of specific cloud resources   """

    def __init__(self, token):
        # Default log level is info. todo: parameterize from config
        logging.RootLogger.setLevel(logging.RootLogger.root, level=logging.INFO)

        # todo: parameterize, remove hard code
        self.token = token
        self.user_agent = "USER_AGENT = 'cryptotrade-cloud-tool'"
        self.sdk = yandexcloud.SDK(token=self.token, user_agent=self.user_agent)
        self.folder_name = "cryptotrade"
        self.cloud_name = "cloud-dmitry-pukhov-cloud"
        self.hadoop_cluster_name = "cryptotrade-hadoop"

        # Init global context: cloud_id, folder_id
        self.cloud_id = self.get_cloud_id(self.cloud_name)
        self.folder_id = self.get_folder_id(cloud_id=self.cloud_id, folder_name=self.folder_name)
        logging.info(f"Got cloud_name: {self.cloud_name}, cloud_id: {self.cloud_id}, "
                     f"folder_name: {self.folder_name}, folder_id: {self.folder_id}")

    def get_cloud_id(self, cloud_name: str) -> str:
        """ @:return cloud id for given name"""
        clouds = self.sdk.client(CloudServiceStub).List(ListCloudsRequest()).clouds
        return next(iter([c.id for c in clouds if c.name == cloud_name]), None)

    def get_folder_id(self, cloud_id: str, folder_name: str) -> str:
        """@:return folder id for given name"""
        folders = self.sdk.client(FolderServiceStub).List(ListFoldersRequest(cloud_id=cloud_id)).folders
        return next(iter([f.id for f in folders if f.name == folder_name]), None)

    def get_cluster_id(self, cluster_name: str) -> str:
        request = cluster_service_pb.ListClustersRequest(folder_id=self.folder_id)
        clusters = self.sdk.client(cluster_service_grpc_pb.ClusterServiceStub).List(request).clusters
        return next(iter([c.id for c in clusters if c.name == cluster_name]), None)

    def get_sa_id(self, name: str) -> str:
        request = ListServiceAccountsRequest(folder_id=self.folder_id)
        accounts = self.sdk.client(ServiceAccountServiceStub).List(request).service_accounts
        return next(iter([a.id for a in accounts if a.name == name]), None)


if __name__ == "__main__":
    cfg = AppTool.read_config()
    tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
    cluster_id = tool.get_cluster_id(cluster_name=tool.hadoop_cluster_name)
    print(f"Cluster id: {cluster_id}")
    sa_id = tool.get_sa_id("cryptotrade-hadoop")
    print(f"cryptotrade-hadoop service account id: {sa_id}")

    # print(f"Cloud id: {CloudTool().get_cloud_id()}")
    # print(f"Folder id: {CloudTool().get_folder_id()}")
