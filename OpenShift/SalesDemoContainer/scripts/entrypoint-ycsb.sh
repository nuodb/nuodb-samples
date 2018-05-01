#!/usr/bin/env bash

#########################################
#
# expected params
#
#    YCSB_WORKLOAD = work load
#    PEER_ADDRESS = admin service load balancer address
#    DB_NAME
#    DB_USER
#    DB_PASSWORD

set -x

YCSB_THREADS=10

#create table
/scripts/ycsb_benchmark/nuo-config/create_usertable.sh 1

#check of table is populated
rowcount=$( nuosql ${DB_NAME}@${PEER_ADDRESS} --schema user1 --user ${DB_USER} --password ${DB_PASSWORD} < /scripts/ycsb_benchmark/nuo-config/getRowCount.sql | tail -n +7 | xargs echo -n )
if [ "$rowcount" == "0" ]; then
    /scripts/ycsb_benchmark/nuo-config/ycsb_gen.sh load user1 10000
fi


for i in $(seq 1 $YCSB_THREADS);
do
    /scripts/ycsb_benchmark/nuo-config/ycsb_gen.sh run user1 10000 ${YCSB_WORKLOAD} 0 &

done

    while [ true ]; do
        sleep 1200
    done
