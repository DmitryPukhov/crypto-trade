import logging
import sys
from datetime import datetime
from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocCreatePysparkJobOperator,
    DataprocDeleteClusterOperator,
)
from airflow.utils.trigger_rule import TriggerRule
from AppTool import AppTool
from CloudTool import CloudTool

# Read config
cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
cfg = AppTool.read_config(*cfg_path)
logging.info(f"Loaded config: {cfg}")

# Read service account id
cloud_tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
sa_hadoop_id = cloud_tool.get_sa_id("cryptotrade-hadoop")
logging.info(f"Got hadoop service account id: {sa_hadoop_id}")

# Get parameters from config
bucket = cfg["dmitrypukhov.cryptotrade.bucket"]
app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]
zone_id = cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"]
ssh_pub_key = cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
cluster_name = cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"]

print(f"sys.version: {sys.version}")

with DAG(dag_id="batch_huobi_import",
         start_date=datetime(2021, 1, 1),
         # schedule_interval="@hourly",
         catchup=False) as dag:
    create_cluster = DataprocCreateClusterOperator(
        task_id='create_hadoop_cluster',
        cluster_name=cluster_name,
        zone=zone_id,
        s3_bucket=bucket,
        computenode_count=2,
        datanode_count=0,
        #computenode_max_hosts_count=5,
        services=["SPARK", "YARN"],
        # computenode_max_hosts_count=5,
        service_account_id=sa_hadoop_id,
        ssh_public_keys=ssh_pub_key#,
        #params={"configSpec.versionId": "2.0", "spark.yarn.maxAppAttempts": 1}
    )
    # Import
    currency_import_spark_job = DataprocCreatePysparkJobOperator(
        task_id="batch_huobi_import",
        main_python_file_uri=f"{app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py",
        python_file_uris=[f"{app_dir}/cryptotrade-pyspark/cryptotrade-pyspark.zip",
                          f"{app_dir}/cryptotrade-pyspark/cryptotrade_libs.zip"],
        properties={"spark.submit.master": "yarn", "spark.submit.deployMode": "cluster"}
    )

    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE
    )

# Workflow: create cluster - job - delete cluster
create_cluster >> currency_import_spark_job #>> delete_cluster
