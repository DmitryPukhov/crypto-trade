from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_ohlcv_psql",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Start the cluster
    ensure_cluster_running = PythonOperator(task_id="ensure_cluster_running",
                                            python_callable=DagTool.ensure_cluster_running)
    # Single job
    job = DagTool.spark_currency_job(args="ohlcv2psql", properties={"spark.submit.deployMode": "client"})

    # Stop cluster
    stop_cluster = PythonOperator(task_id="stop_cluster", python_callable=DagTool.stop_cluster,
                                  trigger_rule=TriggerRule.ALL_DONE)
    # Dag workflow
    ensure_cluster_running >> job >> stop_cluster
