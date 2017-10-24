## steps to run

### deploy jms related resources
mymodule-jms.xml contains a file store, a jms server, a jms module with one dq and one dt
```
$ cd ../jms-dd
$ kubectl cp ./mymodule-jms.xml  $adminPod:/u01/oracle/
$ kubectl cp ./jmsres.json $adminPod:/u01/oracle/
$ kubectl exec -it $adminPod bash
  > cd /u01/oracle && python run.py createJMS
```

### deploy servlet app and mdb app
```
# copy the two app files to admin pod
$ kubectl cp apps/servlet/signIn.war $adminPod:/u01/wlsdomain/signIn.war
$ kubectl cp apps/mdb/signInMDB.jar $adminPod:/u01/wlsdomain/signInMDB.jar
# deploy the two app via REST api
$ kubectl exec $adminPod -- curl -v \
--user $wluser:$wlpwd \
-H X-Requested-By:MyClient \
-H Content-Type:application/json \
-d "{
  name:       'webapp',
  sourcePath: '/u01/wlsdomain/signIn.war',
  targets:    [ { identity: [ 'clusters', 'myCluster' ] } ]
}" \
-X POST http://localhost:8001/management/weblogic/latest/edit/appDeployments

$ kubectl exec $adminPod -- curl -v \
--user $wluser:$wlpwd \
-H X-Requested-By:MyClient \
-H Content-Type:application/json \
-d "{
  name:       'mdb',
  sourcePath: '/u01/wlsdomain/signInMDB.jar',
  targets:    [ { identity: [ 'clusters', 'myCluster' ] } ]
}" \
-X POST http://localhost:8001/management/weblogic/latest/edit/appDeployments
```

### setup mysql server
#### prepare mysql.yml file and deploy to k8s
```
$ kubectl create -f mysql.yml
```
#### create sample table to wlsdb
```
$ kubectl exec -it $mysqlPod -- mysql -h localhost -u mysql -pmysql wlsdb < sampleTable.ddl
```

### create datasource in weblogic domain
#### prepare ds1-jdbc.xml file
```
$ kubectl cp ./ds1-jdbc.xml  $adminPod:/u01/oracle/
$ kubectl exec -it $adminPod bash
  > cd /u01/oracle && python run.py createDS

#### create the datasource
$ kubectl exec $adminPod -- curl -v \
--user $wluser:$wlpwd \
-H X-Requested-By:MyClient \
-H Accept:application/json \
-H Content-Type:application/json \
-d '{
  "name": "ds1",
  "descriptorFileName": "jdbc/ds1-jdbc.xml",
  "targets":[{
              "identity":["clusters", "myCluster"]
            }]
```

### access webapp via http://$hostIP:30009/signIn/
