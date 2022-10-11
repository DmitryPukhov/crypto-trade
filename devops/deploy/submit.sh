yc dataproc job create-spark \
   --cluster-name crypto-trade-hadoop \
   --name="btcusdt" \
   --main-class="dmitrypukhov.cryptotrade.btcusdt.BtcUsdtJob" \
   --main-jar-file-uri="s3a://dmitrypukhov-cryptotrade/app/crypto-trade-spark-assembly-0.1.0-SNAPSHOT.jar"
