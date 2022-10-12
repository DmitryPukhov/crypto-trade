import logging

from huobi.constant import CandlestickInterval
from AppTool import AppTool
from huobi.client.market import MarketClient
from pyspark.sql import SparkSession


class CurrencyImport:
    """
    Import btc usdt m1 from stock exchange to raw layer
    """

    def __init__(self):
        AppTool.init_logger()
        self.conf = AppTool.read_config()
        self.max_attempts = 3
        self.raw_dir = self.conf["dmitrypukhov.cryptotrade.data.raw.dir"]

    def get_candles_huobi(self, symbol: str, interval:str):
        """
        Request Huobi exchange for candles
        :return: huobi candle object
        """
        market_client = MarketClient(init_log=True)
        size = 60
        logging.info(f"Reading data from huobi, symbol:{symbol}, interval: {interval}, size:{size}")
        return market_client.get_candlestick(symbol, interval, size)

    def write_raw(self, candles, symbol: str, interval: str):
        """
        Write candles to raw layer
        """
        path = f"{self.raw_dir}/{symbol}_{interval}"
        logging.info(f"Writing candles to: {path}")
        spark = SparkSession.builder.getOrCreate()
        spark.createDataFrame(candles).write.json(path, "overwrite")

    def run(self):
        interval = CandlestickInterval.MIN1
        symbol = "btcusdt"

        huobi_candles = self.get_candles_huobi(symbol, interval)
        self.write_raw(huobi_candles, symbol, interval)


CurrencyImport().run()