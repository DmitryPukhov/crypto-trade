
jobNames=$@
echo "Jobs to run: $jobNames"

yc dataproc job create-spark \
   --cluster-name cryptotrade-hadoop \
   --name="currency_process" \
   --main-class="dmitrypukhov.cryptotrade.process.CurrencyJob" \
   --main-jar-file-uri="s3a://dmitrypukhov-cryptotrade/app/crypto-trade-spark-assembly-0.1.0-SNAPSHOT.jar" \
   --args="$jobNames"
