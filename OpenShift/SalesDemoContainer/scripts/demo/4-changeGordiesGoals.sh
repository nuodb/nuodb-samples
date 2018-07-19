#!/usr/bin/env bash

/scripts/demo/runsql $PEER_ADDRESS $DB_NAME $DB_USER $DB_PASSWORD "*" ./update_hockey.sql 1
