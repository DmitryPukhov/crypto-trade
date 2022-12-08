
jobNames=${@:-"raw2ohlcv,ohlcv2macd,ohlcv2psql,raw2csv,macd2psql,ohlcv2click,macd2click,ohlcv2mongo,macd2mongo"}
echo "Jobs to run: $jobNames"

yc dataproc job create-spark \
   --cluster-name cryptotrade-hadoop \
   --name="currency_process" \
   --main-class="dmitrypukhov.cryptotrade.process.CurrencyJob" \
   --main-jar-file-uri="s3a://dmitrypukhov-cryptotrade/app/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar" \
   --args="$jobNames"
