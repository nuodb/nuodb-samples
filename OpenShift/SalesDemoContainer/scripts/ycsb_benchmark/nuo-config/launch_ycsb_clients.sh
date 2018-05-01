

YCSB_CLIENT_COUNT=$1
WORKLOAD=$2

if [ "$YCSB_CLIENT_COUNT" == "" ] || [ "$WORKLOAD" == "" ]; then
    echo "usage: launch_ycsb_clients.sh numberOfCleints workload"
    echo ""
    echo "    workload a =  50/50 read update"
    echo "             b =  95/5  read update"
    echo "             c =  100   read"
    echo "             d =  95/5  read insert"
    echo "             e =  95/5  scans insert"
    echo "             f =  50/50 read read/modify"
    exit 0
fi


  echo "usage: launch_ycsb_clients.sh NumberOfCleints WorkLoadType"
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

if [ "$AGENT" == "" ]; then
  AGENT="nuoadmin1"
fi

echo ""
for i in $(seq 1 $YCSB_CLIENT_COUNT); do
    echo "Launching ycsb client ${i}"
    ycsb_gen.sh run user 10000 c 0 &
done

echo "Done"
echo ""
