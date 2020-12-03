#
# compile the GettingStarted app
#

echo "[INFO] Building JAR using Maven"

chmod a+x mvnw
./mvnw package

echo ""
JARNAME=`ls target/getting-started*.jar`
echo "[INFO] To run (and generate help): java -jar $JARNAME"
echo "[INFO] Or use run.sh script - you may need to edit run.sh to change parameters such as host or schema name"

