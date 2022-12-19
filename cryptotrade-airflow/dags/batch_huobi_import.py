from datetime import datetime
from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import DataprocDeleteClusterOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_huobi_import",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Create hadoop cluster
    create_cluster = DagTool.create_cluster_operator(services=["SPARK", "YARN"])

    # Single job
    job = DagTool.pyspark_job(
        name="batch_huobi_import",
        main_python_file_uri=f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py")

    # Delete cluster at the end
    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE)

    create_cluster >> job >> delete_cluster
