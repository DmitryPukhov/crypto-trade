app_dir="s3a://dmitrypukhov-cryptotrade/app/cryptotrade-pyspark"
yc dataproc job create-pyspark \
   --cluster-name cryptotrade-hadoop \
   --name="currency_import" \
   --main-python-file-uri="$app_dir/cryptotrade-pyspark/input/CurrencyImport.py" \
   --python-file-uris="$app_dir/cryptotrade-pyspark.zip,$app_dir/cryptotrade_libs.zip"
   #--python-file-uris="$app_dir/requirements.txt,$app_dir/AppTool.py,$app_dir/app_conf.py,$app_dir/cfg/application.defaults.conf,$app_dir/cfg/log.defaults.conf,$app_dir/lib/pytrade_libs.zip"
   #--file-uris="$app_dir/cfg/application.defaults.conf,$app_dir/log.defaults.conf"
