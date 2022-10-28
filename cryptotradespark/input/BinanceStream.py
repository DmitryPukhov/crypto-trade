import logging
from binance.lib.utils import config_logging
from binance.websocket.spot.websocket_client import SpotWebsocketClient
from AppTool import AppTool


class BinanceStream:

    def __init__(self):
        AppTool.init_logger()
        self.conf = AppTool.read_config()
        # apikey = self.conf["dmitrypukhov.cryptotrade.input.binance.api.key"]
        # secretkey = self.conf["dmitrypukhov.cryptotrade.input.binance.secret.key"]
        url = self.conf["dmitrypukhov.cryptotrade.input.binance.uri"]
        logging.info(f"Init binance websocket client with uri {url}")
        self.client = SpotWebsocketClient(url)

    def run(self):
        logging.info("Run binance client")
        config_logging(logging, logging.DEBUG)

        self.client.start()
        self.client.ticker(symbol="btcusdt", id=1, callback=self.on_message)

    def on_message(self,message):
        logging.info(message)


if __name__ == '__main__':
    CurrencyBinanceStream().run()
