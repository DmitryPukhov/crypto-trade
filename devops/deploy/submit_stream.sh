echo "Submitting binance spark stream"
yc dataproc job create-spark \
   --cluster-name cryptotrade-hadoop \
   --name="currency_stream" \
   --main-class="dmitrypukhov.cryptotrade.stream.BinanceSparkStream" \
   --main-jar-file-uri="s3a://dmitrypukhov-cryptotrade/app/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar"
