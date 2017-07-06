#!/bin/bash

export JAVA_OPTIONS='-Dweblogic.management.username=system -Dweblogic.management.password=gumby1234'
$DOMAIN_HOME/bin/startManagedWebLogic.sh $1 http://admin:7001