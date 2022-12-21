from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_huobi_import",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Start hadoop cluster
    ensure_cluster_running = PythonOperator(task_id="ensure_cluster_running",
                                            python_callable=DagTool.ensure_cluster_running)

    # Single job
    job = DagTool.pyspark_job(
        name="batch_huobi_import",
        main_python_file_uri=f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py")

    # Stop cluster, save money
    stop_cluster = PythonOperator(task_id="stop_cluster", python_callable=DagTool.stop_cluster,
                                  trigger_rule=TriggerRule.ALL_DONE)

    # Workflow
    ensure_cluster_running >> job >> stop_cluster
