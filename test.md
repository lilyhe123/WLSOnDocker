WebLogic Sample on Kubernetes with Shared Domain Home
=========================================
This sample extends the Oracle WebLogic developer install image by creating a sample WLS 12.2.1.3 domain and cluster to run in Kubernetes. The WebLogic domain consists of an Admininstrator Server and several Managed Servers running in a WebLogic cluster. All WebLogic servers share the same domain home which has been mapped to an external volume.

This sample offers you an option by which you can choose to create a WebLogic domain with no leasing or to create a WebLogic domain with database leasing. If you want to try service migration with this domain, you need to choose the later.

## Prer-steps Before Run
1. You need to have a Kubernetes cluster up and running with kubectl installed.
2. You have built oracle/weblogic:12.2.1.3-developer image locally based on Dockerfile and scripts here: https://github.com/oracle/docker-images/tree/master/OracleWebLogic/dockerfiles/12.2.1.3/
3. Username/password for the WebLogic domain are stored in k8s/secrets.yml and they are encoded by base64. The default values are weblogic/weblogic1.  
If you want to customize it, first get the encoded data of your username/password via running `echo -n <username> | base64` and `echo -n <password> | base64`. Next upate k8s/secrets.yml with the new encoded data.
4. The option to create a WebLogic domain with or without leasing is the enviroment variable named 'USELEASING' in k8s/wls-admin.yml. The default value of 'USELEASING' is 'FALSE' which is to create a WebLogic domain with no leasing. Change the value to 'TRUE' if you want to create WebLogic domain with database leasing.

## How to Build and Run

### 1. Build the WebLogic Image for This Sample Domain
Pre-steps before build the image:
1. You need to download get-pip.py from https://bootstrap.pypa.io/get-pip.py and save it to folder 'container-scripts'.
2. If you run `docker build` behind a proxy, you need to set up http and https proxy in the Dockerfile.

Then build the image:
```
$ docker build -t wls-k8s-domain .
```
Or you can build the image by running build.sh directly.

### 2. Prepare Volume Directories
Three volumes are defined in k8s/pv.yml which refer to three external directories. You can choose to use host paths or shared NFS directories. Please change the paths accordingly. The external directories need to be initially empty.

**NOTE:** The first two persistent volumes 'pv1' and 'pv2' will be used by WebLogic server pods. All processes in WebLogic server pods are running with UID 1000 and GID 1000 by default, so proper permissions need to be set to these two external directories to make sure that UID 1000 or GID 1000 have permission to read and write the volume directories.
If you chhose to create a WebLogic domain with no leasing, the third persistent volumes 'pv3' is not used. If you choose to create WebLogic domain with database leasing then the third persistent volume 'pv3' is used by MySQL server. MySQL server is running with root privilege so no particular permission need to be set to its external directory.  
 
### 3. Deploy All the Kubernetes Resources
If you chhose to create the WebLogic domain with no leasing, run the script deploy.sh to deploy all resources to your Kubernetes cluster. 

If you choose to create the WebLogic domain with database leasing, deploy all the resources by running the following commands:
```
$ kubectl create -f  k8s/secrets.yml 
$ kubectl create -f  k8s/pv.yml 
$ kubectl create -f  k8s/mysql.yml
# Create leasing table to MySQL database after teh MySQL server pod are running.
$ kubectl exec -it $mysqlPod -- mysql -h localhost -u mysql -pmysql wlsdb < leasing.ddl
$ kubectl create -f  k8s/pvc.yml
$ kubectl create -f  k8s/wls-admin.yml
$ kubectl create -f  k8s/wls-stateful.yml
```

### 4. Check Resources Deployed to Kubernetes
#### 4.1 Check Pods and Controllers
List all pods and controllers:
```
$ kubectl get all
```
The following are all the pods and controllers of the WebLogic domain without leasing:
```
NAME                               READY     STATUS    RESTARTS   AGE
po/admin-server-1238998015-f932w   1/1       Running   0          11m
po/managed-server-0                1/1       Running   0          11m
po/managed-server-1                1/1       Running   0          8m

NAME                CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
svc/admin-server    10.102.160.123   <nodes>       8001:30007/TCP   11m
svc/kubernetes      10.96.0.1        <none>        443/TCP          39d
svc/wls-service     10.96.37.152     <nodes>       8011:30009/TCP   11m
svc/wls-subdomain   None             <none>        8011/TCP         11m

NAME                          DESIRED   CURRENT   AGE
statefulsets/managed-server   2         2         11m

NAME                  DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deploy/admin-server   1         1         1            1           11m

NAME                         DESIRED   CURRENT   READY     AGE
rs/admin-server-1238998015   1         1         1         11m

```
The following are all the pods and controllers of the WebLogic domain with database leasing:
```
NAME                               READY     STATUS    RESTARTS   AGE
po/admin-server-1868326005-ctbqm   1/1       Running   0          2h
po/managed-server-0                1/1       Running   0          2h
po/managed-server-1                1/1       Running   0          2h
po/mysql-server-3736789149-t3q9l   1/1       Running   0          2h

NAME                CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
svc/admin-server    10.102.150.206   <nodes>       8001:30007/TCP   2h
svc/kubernetes      10.96.0.1        <none>        443/TCP          63d
svc/mysql-server    None             <none>        3306/TCP         2h
svc/wls-service     10.109.244.13    <nodes>       8011:30009/TCP   2h
svc/wls-subdomain   None             <none>        8011/TCP         2h

NAME                          DESIRED   CURRENT   AGE
statefulsets/managed-server   2         2         2h

NAME                  DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deploy/admin-server   1         1         1            1           2h
deploy/mysql-server   1         1         1            1           2h

NAME                         DESIRED   CURRENT   READY     AGE
rs/admin-server-1868326005   1         1         1         2h
rs/mysql-server-3736789149   1         1         1         2h
```

#### 4.2 Check PV and PVC
List all PVs:
```
$ kubectl get pv
```
The following are all the PVs of the WebLogic domain without leasing:
```
NAME      CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS      CLAIM                    STORAGECLASS   REASON    AGE
pv1       10Gi       RWX           Recycle         Available                            manual                   17m
pv2       10Gi       RWX           Recycle         Bound       default/wlserver-pvc-1   manual                   17m
pv3       10Gi       RWX           Recycle         Bound       default/wlserver-pvc-2   manual                   17m

```
The following are all the PVs of the WebLogic domain with database leasing:
```
NAME      CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS    CLAIM                    STORAGECLASS   REASON    AGE
pv1       10Gi       RWX           Recycle         Bound     default/wlserver-pvc-1   manual                   2h
pv2       20Gi       RWX           Recycle         Bound     default/wlserver-pvc-2   manual                   2h
pv3       10Gi       RWO           Recycle         Bound     default/mysql-pv-claim   manual                   2h
```
List all PVCs:
```
$ kubectl get pvc
```
The following are all the PVCs of the WebLogic domain without leasing:
```
NAME             STATUS    VOLUME    CAPACITY   ACCESSMODES   STORAGECLASS   AGE
wlserver-pvc-1   Bound     pv2       10Gi       RWX           manual         18m
wlserver-pvc-2   Bound     pv3       10Gi       RWX           manual         18m
```
The following are all the PVCs of the WebLogic domain with database leasing:
```
NAME             STATUS    VOLUME    CAPACITY   ACCESSMODES   STORAGECLASS   AGE
mysql-pv-claim   Bound     pv3       10Gi       RWO           manual         2h
wlserver-pvc-1   Bound     pv1       10Gi       RWX           manual         2h
wlserver-pvc-2   Bound     pv2       20Gi       RWX           manual         2h
```

#### 4.3 Check Secrets
List all secrets:
```
$ kubectl get secrets
```
The following are all the secrets of the WebLogic domain without leasing:
```
NAME                  TYPE                                  DATA      AGE
default-token-m93m1   kubernetes.io/service-account-token   3         39d
wlsecret              Opaque                                2         19m
```
The following are all the secrets of the WebLogic domain with database leasing:
```
NAME                  TYPE                                  DATA      AGE
dbsecret              Opaque                                3         2h
default-token-m93m1   kubernetes.io/service-account-token   3         63d
wlsecret              Opaque                                2         2h
```

### 5. Check Weblogic Server Status via Administrator Console
The admin console URL is 'http://[hostIP]:30007/console'.

### 6. Troubleshooting
You can trace output and logs of any pod for troubleshooting.

Trace a pod output. Replace $podName with the actual pod name of WebLogic Server or MySQL server.
```
$ kubectl logs -f $serverPod
```
You can look at the WebLogic server logs by running:
```
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/managed-server-0/logs/managed-server-0.log
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/managed-server-1/logs/managed-server-1.log
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/AdminServer/logs/AdminServer.log
```

### 7. Restart All Pods
#### 7.1 Shutdown the Managed Servers' Pods Gracefully
```
$ kubectl exec -it managed-server-0 -- /u01/wlsdomain/bin/stopManagedWebLogic.sh managed-server-0 t3://admin-server:8001
$ kubectl exec -it managed-server-1 -- /u01/wlsdomain/bin/stopManagedWebLogic.sh managed-server-1 t3://admin-server:8001
```
#### 7.2 Shutdown the Administrator Server Pod Gracefully
First gracefully shutdown admin server process. Note that you need to replace $adminPod with the real admin server pod name.
```
$ kubectl exec -it $adminPod -- /u01/wlsdomain/bin/stopWebLogic.sh <username> <password> t3://localhost:8001
```
Next manually delete the admin pod.
```
$ kubectl delete pod/$adminPod
```
#### 7.3 Shutdown the MySQL Server Pod Gracefully
```
$ kubectl exec -it $mysqlpod /etc/init.d/mysql stop
```
After the pods are stopped, each pod's corresponding controller is responsible for restarting the pods automatically.
Wait until all pods are running and ready again. Monitor status of pods via `kubectl get pod`.

### 8. Cleanup
If you choose to create the WebLogic domain with no leasing, run the script clean.sh to remove all resources from your Kubernetes cluster.

If you choose to create the WebLogic domain with database leasing, remove all resources by running the following commands:
```
$ kubectl delete -f k8s/wls-stateful.yml
$ kubectl delete -f k8s/wls-admin.yml
$ kubectl delete -f k8s/pvc.yml
$ kubectl delete -f k8s/mysql.yml
$ kubectl delete -f k8s/pv.yml
$ kubectl delete -f k8s/secrets.yml
```
And you need to clean up all data in volume directories via `rm -rf *`.

## COPYRIGHT 
Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
