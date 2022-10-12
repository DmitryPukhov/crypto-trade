import logging
import logging.config
import os
import yaml


class AppTool:
    @staticmethod
    def read_config():
        """
        Set up logging system from log.conf files
        """
        cfgpaths = ["cfg/application.defaults.conf", "cfg/application.dev.conf", "cfg/application.conf"]
        print(f"Init logging from {cfgpaths}")
        config = dict()
        for cfgpath in cfgpaths:
            if os.path.exists(cfgpath):
                with open(cfgpath) as cur_cfg:
                    config.update(yaml.safe_load(cur_cfg))
            else:
                print(f"{cfgpath} does not exist. It can be ok.")
        # Enviroment variabless
        config.update(os.environ)
        logging.info("Config initialized")
        logging.info(config)
        return config

    @staticmethod
    def init_logger():
        """
        Set up logging system from log.conf files
        """
        cfgpaths = ["cfg/log.defaults.conf", "cfg/log.conf", "cfg/log.dev.conf"]
        print(f"Init logging from {cfgpaths}")
        for cfgpath in cfgpaths:
            if os.path.exists(cfgpath):
                logging.config.fileConfig(cfgpath)
            else:
                print(f"{cfgpath} does not exist. It can be ok.")
        logging.info("Logging initialized")