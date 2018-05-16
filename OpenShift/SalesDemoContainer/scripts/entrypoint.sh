#!/usr/bin/env bash

#change SSH port to 2222
#echo "Port 2222" >>/etc/ssh/sshd_config

#systemctl restart sshd
if [ ! -z "${YCSB_WORKLOAD}" ]; then
    /scripts/entrypoint-ycsb.sh
else
    while [ true ]; do
        sleep 1200
    done
fi
