
YCSB - Yahoo Cloud Servings Benchmark
https://github.com/brianfrankcooper/YCSB/wiki


1. CREATE DATABASE ON NUODB MACHINE

mkdir  mkdir /tmp/dbs/ycsb
chmod -R 777 /tmp/dbs

/opt/nuodb/bin/nuodbmgr --broker localhost --password bird
start process sm archive /tmp/dbs/ycsb host instance-1 database ycsb options '--ping-timeout 10' initialize true
start process te host instance-1 database ycsb options '--dba-user dba --dba-password dba --ping-timeout 10 --commit remote:1'


/opt/nuodb/bin/nuosql ycsb --user dba --password dba

CREATE TABLE usertable (
        YCSB_KEY VARCHAR(255) PRIMARY KEY,
        FIELD0 TEXT, FIELD1 TEXT,
        FIELD2 TEXT, FIELD3 TEXT,
        FIELD4 TEXT, FIELD5 TEXT,
        FIELD6 TEXT, FIELD7 TEXT,
        FIELD8 TEXT, FIELD9 TEXT
);

To run <insert> workloads, remove the primary key index
and create a not unique index. See create_usertable.sql script.


2. DOWNLOAD LATEST YCSB BENCHMARK

execute the following from the YCSB application machine:

curl -O --location https://github.com/brianfrankcooper/YCSB/releases/download/0.12.0/ycsb-0.12.0.tar.gz
tar xfvz ycsb-0.12.0.tar.gz


3. CUSTOMIZING AND RUNNING YCSB AGAINST NUODB 

ycsb - contains YCSB copied from download w/o changes
ycsb-nuo - directory with all nuodb-specific files

nuo-config/nuo.properties - connection properties
nuo-config/workloads - files to customise benchmark runs 
ycsb_gen.sh - script to create a YCSB database and run a workload


