#!/usr/bin/env bash


YCSB_SCHEMA_COUNT=$1

if [ "$YCSB_SCHEMA_COUNT" == "" ]; then
  echo "usage: create_usertable.sh numberOfSchemas"
  exit 0
fi

if [ "$DB_NAME" == "" ]; then
  DB_NAME="test"
fi

if [ "$DB_USER" == "" ]; then
  DB_USER="dba"
fi

if [ "$DB_PASSWORD" == "" ]; then
  DB_PASSWORD="goalie"
fi

if [ "$PEER_ADDRESS" == "" ]; then
  AGENT="nuoadmin1"
fi

echo ""
for i in $(seq 1 $YCSB_SCHEMA_COUNT); do
    echo "Creating ycsb table in schema user${i}"
    nuosql ${DB_NAME}@${PEER_ADDRESS} --schema user${i} --user ${DB_USER} --password ${DB_PASSWORD} < /scripts/ycsb_benchmark/nuo-config/create_usertable.sql > /dev/null
done

echo "Done"
echo ""
