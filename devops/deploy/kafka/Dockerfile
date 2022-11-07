#from ubuntu:latest
from confluentinc/cp-kafka:7.0.0

USER root
# Set up security and certs
RUN mkdir -p /usr/local/share/ca-certificates/Yandex/
COPY .ssh/YandexInternalRootCA.crt /usr/local/share/ca-certificates/Yandex/YandexCA.crt
RUN keytool -importcert \
             -alias YandexCA -file /usr/local/share/ca-certificates/Yandex/YandexCA.crt \
             -keystore ssl -storepass 123456 \
             --noprompt
RUN mkdir --parents /etc/kafka-connect-worker
COPY .ssh/client.truststore.jks /etc/kafka-connect-worker/client.truststore.jks

# Set up kafka connect plugin
RUN mkdir -p  /opt/cryptotrade/kafka
COPY tmp /opt/cryptotrade/kafka
RUN ls /opt/cryptotrade/kafka


ENTRYPOINT /bin/connect-standalone \
     /opt/cryptotrade/kafka/worker.properties \
     /opt/cryptotrade/kafka/binance.connector.properties