#!/bin/bash
# Load environment
source $(dirname $0)/clickhouse-env.sh
echo "Clickhouse user: $CLICKHOUSE_USER"

echo "Getting clickhouse host name"
ch_host_name=$(yc managed-clickhouse host list --cluster-name cryptotrade-clickhouse | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
echo "Got clickhouse host: $ch_host_name"
clickhouse-client --host $ch_host_name \
                  --secure \
                  --user $CLICKHOUSE_USER \
                  --database cryptotrade \
                  --port 9440 \
                  --password $CLICKHOUSE_PASSWORD \
                  --multiquery \
                  < kafka_btcusdt_price.sql