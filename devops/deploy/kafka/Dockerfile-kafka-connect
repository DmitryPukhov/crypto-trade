#from ubuntu:latest
from confluentinc/cp-kafka:7.0.0

USER root
ENV CLASSPATH=/opt/cryptotrade/kafka/*


# Set up kafka connect plugin
RUN mkdir -p  /opt/cryptotrade/kafka
COPY tmp /opt/cryptotrade/kafka
RUN ls /opt/cryptotrade/kafka


# Set up security and certs
RUN mkdir -p /opt/cryptotrade/kafka/.ssh
COPY .ssh/YandexInternalRootCA.crt /opt/cryptotrade/kafka/.ssh/YandexCA.crt
RUN keytool -importcert \
             -alias YandexCA -file /opt/cryptotrade/kafka/.ssh/YandexCA.crt \
             -keystore ssl -storepass 123456 \
             --noprompt
RUN mkdir --parents /etc/kafka-connect-worker
COPY .ssh/client.truststore.jks /opt/cryptotrade/kafka/.ssh/client.truststore.jks


# Run kafka connect
ENTRYPOINT /bin/connect-standalone \
     /opt/cryptotrade/kafka/worker.properties \
     /opt/cryptotrade/kafka/binance.connector.properties