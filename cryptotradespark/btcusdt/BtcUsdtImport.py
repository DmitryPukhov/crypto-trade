import logging

from AppTool import AppTool


class BtcUsdtImport:
    """
    Import btc usdt m1 from stock exchange to raw layer
    """

    def __init__(self):
        AppTool.init_logger()
        self.conf = AppTool.read_config()

    def run(self):
        logging.info("Run the job")


#BtcUsdtImport().run()
