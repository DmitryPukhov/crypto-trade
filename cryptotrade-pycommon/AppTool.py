import logging
import logging.config
import os
import yaml
#from cfg.app_conf import app_conf


class AppTool:
    @staticmethod
    def read_config(*extrapaths):
        """
        Read configuration from config files
        """
        cfgpaths = ["cfg/application.defaults.conf", "cfg/application.conf", "cfg/application.dev.conf"] + list(extrapaths)
        print("Reading config from {}".format(cfgpaths))
        #config = app_conf
        config = {}
        for cfgpath in cfgpaths:
            if os.path.exists(cfgpath):
                with open(cfgpath) as cur_cfg:
                    config.update(yaml.safe_load(cur_cfg))
            else:
                print("{} does not exist. It can be ok.".format(cfgpath))
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
        print("Init logging from {}".format(cfgpaths))
        logging.basicConfig(level="INFO")
        for cfgpath in cfgpaths:
            if os.path.exists(cfgpath):
                logging.config.fileConfig(cfgpath)
            else:
                print("{} does not exist. It can be ok.".format(cfgpath))
        logging.info("Logging initialized")
