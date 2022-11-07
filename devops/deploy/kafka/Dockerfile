#from ubuntu:latest
from confluentinc/cp-kafka:7.0.0

#RUN apt update && \
#apt install default-jdk --yes && \
#apt install kafkacat
#RUN wget https://downloads.apache.org/kafka/3.1.0/kafka_2.12-3.1.0.tgz && tar -xvf kafka_2.12-3.1.0.tgz --strip 1 --directory /opt/kafka/
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

#ENTRYPOINT cd /opt/kafka/bin/ && \
#    ./connect-standalone.sh \
#     /etc/kafka-connect-worker/worker.properties \
#     /etc/kafka-connect-worker/file-connector.properties

