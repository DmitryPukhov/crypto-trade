DROP  VIEW IF EXISTS cryptotrade.btcusdt_price;
DROP TABLE IF EXISTS cryptotrade.temp_kafka_btcusdt_price;
DROP TABLE IF EXISTS cryptotrade.kafka_btcusdt_price;

CREATE TABLE IF NOT EXISTS cryptotrade.kafka_btcusdt_price
(
    datetime String,
    symbol String,
    price Float32,
    volume Float32
) ENGINE = Kafka()
SETTINGS
    kafka_broker_list = 'rc1b-nvvhd8j0r8pkoff2.mdb.yandexcloud.net:9091',
    kafka_topic_list = 'btcusdt.price',
    kafka_group_name = 'cryptotrade_kafka_clickhouse',
    kafka_format = 'JSONEachRow';
    
CREATE TABLE cryptotrade.temp_kafka_btcusdt_price
(
    datetime DateTime64,
    symbol String,
    price Float32,
    volume Float32
) ENGINE = MergeTree()
ORDER BY datetime;   

CREATE MATERIALIZED VIEW cryptotrade.btcusdt_price TO cryptotrade.temp_kafka_btcusdt_price
    AS 
    SELECT 
    	toDateTime64(datetime,3) as datetime,
    	symbol,
    	price,
    	volume
    FROM cryptotrade.kafka_btcusdt_price;
    
   

SELECT * FROM cryptotrade.btcusdt_price LIMIT 10;



