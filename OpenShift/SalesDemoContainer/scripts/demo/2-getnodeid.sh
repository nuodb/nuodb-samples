#!/usr/bin/env bash

if [ -z "$1" ]; then
    echo "Please add load balance region expression"
    echo "example: us-east-2a,us-east-2b,*"
    exit 1
fi

/scripts/demo/runsql $PEER_ADDRESS $DB_NAME $DB_USER $DB_PASSWORD "$1" ./getnodeid.sql 0