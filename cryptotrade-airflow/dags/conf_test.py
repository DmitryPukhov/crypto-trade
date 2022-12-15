from airflow import DAG
from airflow.operators.python import PythonOperator
from datetime import datetime
from AppTool import AppTool

def conf_test():
    print("Hello World")
    AppTool.read_config("/home/airflow/dags/cfg/application.defaults.conf")


with DAG(dag_id="conf_test_dag",
         start_date=datetime(2021, 1, 1),
         catchup=False) as dag:
    task1 = PythonOperator(
        task_id="conf_test_task",
        python_callable=conf_test)


task1
