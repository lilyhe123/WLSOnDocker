WebLogic Sample on Kubernetes with Shared Domain Home
=========================================
This sample extends the Oracle WebLogic developer install image by creating a sample WLS 12.2.1.3 domain and cluster to run in Kubernetes. The WebLogic domain consists of an Admin server and one or more Managed servers running in a WebLogic cluster. And all the WebLogic servers are shared the same domain home which is stored in an external volume.

## Prerequisites
1. You need to have a Kubernetes cluster up and running with kubectl installed.
2. You have built oracle/weblogic:12.2.1.3-developer image locally based on Dockerfile and scripts here: https://github.com/oracle/docker-images/tree/master/OracleWebLogic/dockerfiles/12.2.1.3/

## How to Build and Run

1. Build the WebLogic image wls-installer for this sample domain
```
$ docker build -t wls-installer .
```

2. Prepare volume directories

Three volumes are defined in k8s/pv.yml which refer to three external directories. You can choose to use host paths or shared NFS directories. Please change the paths accordingly. The external directories need to be initially empty.
   
3. Deploy all the k8s resources
```
$ kubectl create -f  k8s/secrets.yml 
$ kubectl create -f  k8s/pv.yml 
$ kubectl create -f  k8s/pvc.yml
$ kubectl create -f  k8s/wls-admin.yml
$ kubectl create -f  k8s/wls-stateful.yml
```
Or you can run k8s/deploy.sh to deploy all the resources in one command.

4. Check resources deployed to k8s
### Check pods and controllers etc
```
$ kubectl get all
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

### Check pv and pvc
We have three pv defined and two pvc defined. One pv is reserved for later use
```
$ kubectl get pv
NAME      CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS      CLAIM                    STORAGECLASS   REASON    AGE
pv1       10Gi       RWX           Recycle         Available                            manual                   17m
pv2       10Gi       RWX           Recycle         Bound       default/wlserver-pvc-1   manual                   17m
pv3       10Gi       RWX           Recycle         Bound       default/wlserver-pvc-2   manual                   17m

$ kubectl get pvc
NAME             STATUS    VOLUME    CAPACITY   ACCESSMODES   STORAGECLASS   AGE
wlserver-pvc-1   Bound     pv2       10Gi       RWX           manual         18m
wlserver-pvc-2   Bound     pv3       10Gi       RWX           manual         18m
```

### Check secrets
```
$ kubectl get secrets
NAME                  TYPE                                  DATA      AGE
default-token-m93m1   kubernetes.io/service-account-token   3         39d
wlsecret              Opaque                                2         19m
```

5. Go to admin console to check server status
The admin console URL is 'http://[hostIP]:30007/console' and the user/pwd are weblogic/weblogic1.

6. Troubleshooting
Trace WebLogic server output. Note you need to replace $serverPod with the actual pod name of a WebLogic server.
```
$ kubectl logs -f $serverPod
```
Trace WebLogic server logs. Since the domain home is shared by all WebLogic server, you can trace all servers' logs in any one server pod.
```
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/managed-server-0/logs/managed-server-0.log
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/managed-server-1/logs/managed-server-1.log
$ kubectl exec managed-server-0 -- tail -f /u01/wlsdomain/servers/AdminServer/logs/AdminServer.log
```

7. Restart all pods
#### Shutdown the managed servers' pods gracefully
```
$ kubectl exec -it managed-server-0 -- /u01/wlsdomain/bin/stopManagedWebLogic.sh managed-server-0 t3://admin-server:8001
$ kubectl exec -it managed-server-1 -- /u01/wlsdomain/bin/stopManagedWebLogic.sh managed-server-1 t3://admin-server:8001
```
#### Shutdown the admin server pod gracefully
First we need to gracefully shutdown admin server process. Note you need to replace $adminPod with the real admin server pod name.
```
$ kubectl exec -it $adminPod -- /u01/wlsdomain/bin/stopWebLogic.sh weblogic weblogic1 t3://localhost:8001
```
Then kill the main process in admin server container/pod which run `tail -f /u01/wlsdomain/admin.out`.
```
$ kubectl exec -it $adminPod bash
  >  ps -ef | grep tail | kill -9 $(awk '{print $2}')
  > exit
```
After the pods are stopped, each pod's corresponding controller is responsible for restarting the pods automatically.
Wait until all pods are running and ready again. Monitor status of pods via `kubectl get pod`

8. Cleanup
```
$ kubectl delete -f k8s/wls-stateful.yml
$ kubectl delete -f k8s/wls-admin.yml
$ kubectl delete -f k8s/pvc.yml
$ kubectl delete -f k8s/pv.yml
$ kubectl delete -f k8s/secrets.yml
```
Or you can run clean.sh to do the cleanup in one command.
And then delete all files under volume directories via `rm -rf *` to clean up all persistent data.

##COPYRIGHT Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
