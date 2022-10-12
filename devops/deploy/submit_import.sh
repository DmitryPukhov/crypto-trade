app_dir="s3a://dmitrypukhov-cryptotrade/app/cryptotradespark"
yc dataproc job create-pyspark \
   --cluster-name cryptotrade-hadoop \
   --name="currency_import" \
   --main-python-file-uri="$app_dir/input/CurrencyImport.py" \
   --python-file-uris="$app_dir/AppTool.py,$app_dir/cfg/application.defaults.conf,$app_dir/cfg/log.defaults.conf"
   #--file-uris="$app_dir/cfg/application.defaults.conf,$app_dir/log.defaults.conf"
