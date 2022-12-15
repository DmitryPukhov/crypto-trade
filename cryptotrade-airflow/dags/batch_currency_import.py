import logging
from datetime import datetime

from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocCreatePysparkJobOperator,
    DataprocDeleteClusterOperator,
)
from airflow.utils.trigger_rule import TriggerRule

from AppTool import AppTool

# Read config
cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
cfg = AppTool.read_config(*cfg_path)

#ToDo: remove hard code
sa_hadoop_id = "aje0m5sq60bhdt702rrc"


bucket = cfg["dmitrypukhov.cryptotrade.bucket"]
app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]
zone_id=cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"]
# todo: change to pub key file path
ssh_pub_key=cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
cluster_name=cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"]
logging.info("Loaded config: $cfg")

with DAG(dag_id="batch_currency_import",
         start_date=datetime(2021, 1, 1),
         # schedule_interval="@hourly",
         catchup=False) as dag:

    create_cluster = DataprocCreateClusterOperator(
        task_id='create__hadoop_cluster',
        cluster_name=cluster_name,
        zone=zone_id,
        s3_bucket=bucket,
        computenode_count=2,
        computenode_max_hosts_count=5,
        service_account_id=sa_hadoop_id,
        ssh_public_keys=ssh_pub_key
    )
    # Import
    currency_import_spark_job = DataprocCreatePysparkJobOperator(
        task_id="batch_currency_import",
        main_python_file_uri=f"{app_dir}/cryptotrade-pyspark/input/CurrencyImport.py",
        python_file_uris=[f"{app_dir}/cryptotrade-pyspark.zip", f"{app_dir}/cryptotrade_libs.zip"]
    )

    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE
    )

create_cluster >> currency_import_spark_job >> delete_cluster