app_dir="s3a://dmitrypukhov-cryptotrade/app"
job="raw2ohlcv"
yc dataproc job create-spark \
   --cluster-name cryptotrade-hadoop \
   --name=job \
   --main-jar-file-uri "$app_dir/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar" \
   --main-class dmitrypukhov.cryptotrade.process.CurrencyJob \
   --args $job \
   --properties spark.submit.deployMode=cluster \
   --properties spark.submit.master=yarn
   #--python-file-uris="$app_dir/requirements.txt,$app_dir/AppTool.py,$app_dir/app_conf.py,$app_dir/cfg/application.defaults.conf,$app_dir/cfg/log.defaults.conf,$app_dir/lib/pytrade_libs.zip"
   #--file-uris="$app_dir/cfg/application.defaults.conf,$app_dir/log.defaults.conf"
