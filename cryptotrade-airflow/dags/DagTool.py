import logging
from typing import List
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocCreatePysparkJobOperator,
    DataprocCreateSparkJobOperator
)
from yandex.cloud.dataproc.v1.cluster_pb2 import Cluster
from yandex.cloud.dataproc.v1.cluster_service_pb2 import GetClusterRequest
from yandex.cloud.dataproc.v1.cluster_service_pb2 import StartClusterRequest, StopClusterRequest
from yandex.cloud.dataproc.v1.cluster_service_pb2_grpc import ClusterServiceStub
from AppTool import AppTool
from CloudTool import CloudTool


class DagTool:
    """   Reusable dag elements   """

    # Read config
    cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
    cfg = AppTool.read_config(*cfg_path)
    logging.info(f"Loaded config: {cfg}")
    cloud_tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
    app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]
    cluster_name = "cryptotrade-hadoop"

    @staticmethod
    def ensure_cluster_running(**kwargs):
        logging.info(f"Got context {kwargs}")
        logging.info(f"Ensure cluster {DagTool.cluster_name} is running")
        cluster_id = DagTool.cloud_tool.get_cluster_id("cryptotrade-hadoop")
        if not DagTool.is_cluster_running(cluster_id):
            logging.info(f"Cluster {DagTool.cluster_name} is not running, starting")
            cluster_id = DagTool.cloud_tool.get_cluster_id(DagTool.cluster_name)
            logging.info(f"Starting cluster with name: {DagTool.cluster_name}, id:{cluster_id}")
            DagTool.cloud_tool.sdk.client(ClusterServiceStub).Start(StartClusterRequest(cluster_id=cluster_id))
        else:
            logging.info(f"Cluster {DagTool.cluster_name} is already running.")
        kwargs['task_instance'].xcom_push(key='cluster_id', value=cluster_id)
        # context['task_instance'].xcom_push(key='yandexcloud_connection_id', value=self.yandex_conn_id)

    @staticmethod
    def stop_cluster():
        logging.info(f"Stopping cluster {DagTool.cluster_name}")
        cluster_id = DagTool.cloud_tool.get_cluster_id(DagTool.cluster_name)
        DagTool.cloud_tool.sdk.client(ClusterServiceStub).Stop(StopClusterRequest(cluster_id=cluster_id))

    @staticmethod
    def is_cluster_running(cluster_id):
        cluster: Cluster = DagTool.cloud_tool.sdk.client(ClusterServiceStub).Get(
            GetClusterRequest(cluster_id=cluster_id))
        return cluster.status == cluster.RUNNING

    @staticmethod
    def create_cluster_operator(services: List[str], compute_node_count=1, data_node_count=0):
        return DataprocCreateClusterOperator(
            task_id='create_hadoop_cluster',
            cluster_name=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"],
            zone=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"],
            s3_bucket=DagTool.cfg["dmitrypukhov.cryptotrade.bucket"],
            computenode_count=compute_node_count,
            datanode_count=data_node_count,
            services=services,
            service_account_id=DagTool.cloud_tool.get_sa_id("cryptotrade-hadoop"),
            ssh_public_keys=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
        )

    @staticmethod
    def spark_currency_job(args: str, properties: {} = None):
        return DataprocCreateSparkJobOperator(
            task_id=args,
            main_jar_file_uri=f"{DagTool.app_dir}/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar",
            main_class="dmitrypukhov.cryptotrade.process.CurrencyJob",
            properties=properties,
            args=[args]
        )

    @staticmethod
    def pyspark_job(name: str, main_python_file_uri: str):
        return DataprocCreatePysparkJobOperator(
            task_id=name,
            # main_python_file_uri=f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py",
            main_python_file_uri=main_python_file_uri,
            python_file_uris=[f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark.zip",
                              f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade_libs.zip"],
            properties={"spark.pyspark.python": "python3"}
        )


if __name__ == "__main__":
    DagTool.ensure_cluster_running()
    DagTool.stop_cluster()

    print("ok")
    # cfg = AppTool.read_config()
    # tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
    # cluster_id = tool.get_cluster_id(cluster_name=tool.hadoop_cluster_name)
    # print(f"Cluster id: {cluster_id}")
    # sa_id = tool.get_sa_id("cryptotrade-hadoop")
    # print(f"cryptotrade-hadoop service account id: {sa_id}")

    # print(f"Cloud id: {CloudTool().get_cloud_id()}")
    # print(f"Folder id: {CloudTool().get_folder_id()}")
