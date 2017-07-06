#!/bin/bash

folder="$DOMAIN_HOME/config"
if [ -d "$folder" ]
then
	echo "init already done."
	exit 0;
fi

. $ORACLE_HOME/wlserver/server/bin/setWLSEnv.sh

# start admin server
echo "starting admin server..."
cp /scripts/input $DOMAIN_HOME
cd $DOMAIN_HOME
nohup java -Xms256m -Xmx512m -Dweblogic.ListenPort=7001  -Dweblogic.management.username=system \
-Dweblogic.management.password=gumby1234 -Dweblogic.Domain=mydomain -Dweblogic.Name=admin \
 weblogic.Server<input 1>&2 >admin.out &

# create domain resources
cd /scripts
python cluster.py create

# compile jms client
mkdir $WL_HOME/server/classes
cd /samples
javac -d $WL_HOME/server/classes -g *.java
