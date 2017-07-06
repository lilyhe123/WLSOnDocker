
Dockerfile and compose file to run WLS Cluster Domain
================================

## How to build and run

You need to first download weblogic 12.2.1 distribution and put it in this folder with name like fmw_12.2.1.wls*.jar.

### build jdk8 image 
as base image to run WLS server

### prepare data-only container 
This data volume container creates three local volumes: 
 * oracle home directories
 * domain directories
 * directories of file store data

#### build and run
  1. build image with installed weblogic
   
    $ docker build -t wls .
   
  2. run to create a weblogic domain with cluster and create three named data volume
    
    $ docker run -v orace-home:/u01/oracle -v domain-home:/u01/mydomain -v filestore-data:/u01/stores --name wlsdata wls

### run four composed services in one application
This aplication contains four services and the three WLS servers use data volumes from container wlsdata.
  1. mysql db for cluster leasing
  2. admin server
  3. managed server ms1
  4. managed server ms2

* start the app

    $ docker stack deploy -c docker-compose.yml wlssample

* stop the app

    $ docker stack rm wlssample

### check status on WLS console
visit 'http://localhost:7001' in browser in your host machine to check status of servers and resources

### run jms client to send/receive msg from jms queue
enter one of the WLS servers and run jms client program

  $ docker exec -it <containerId> /bin/bash

  $ . /u01/oracle/wlserver/server/bin/setWLSEnv.sh
  
  $ export CLASSPATH=$WL_HOME/server/classes:$CLASSPATH
  
  $ java samples.QueueSend loop
  
  $ java samples.QueueReceive

### cleanup
* remove all stopped containers (make sure that’s what you want before you do)

  $ docker rm $(docker ps -a -q)

* remove all dangling local data volumes
  
  $ docker volume rm $(docker volume ls -qf dangling=true)
