

## steps to run
### setup leasing db
#### prepare mysql.yml file and deploy to k8s
```
$ kubectl create -f mysql.yml
```
#### create leasing table to wlsdb
```
$ kubectl exec -it $mysqlPod -- mysql -h localhost -u mysql -pmysql wlsdb < leasing.ddl
```

#### create datasource in weblogic domain
prepare ds1-jdbc.xml
```
# copy ds1-jdbc.xml to admin pod
$ kubectl cp ./ds1-jdbc.xml  $adminPod:/u01/oracle/
$ kubectl exec -it $adminPod bash
  > cd /u01/oracle && python run.py createDS

# create datasource
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
#### update cluster to set datasource for auto-migration
```
$ kubectl exec $adminPod -- curl -v \
--user weblogic:weblogic1 \
-H X-Requested-By:MyClient \
-H Accept:application/json \
-H Content-Type:application/json \
-d '{
  "dataSourceForAutomaticMigration": ["JDBCSystemResources", "ds1"]
}' -X POST http://localhost:8001/management/weblogic/latest/edit/clusters/myCluster
```

### deploy jms related resources
```
$ cd ../jms-dd
$ kubectl cp ./mymodule-jms.xml  $adminPod:/u01/oracle/
$ kubectl cp ./jmsres.json $adminPod:/u01/oracle/
$ kubectl exec -it $adminPod bash
  > cd /u01/oracle && python run.py createJMS
```
### setup jms client pod
