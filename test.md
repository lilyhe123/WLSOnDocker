## prerequisites
You need to have a Kubernetes cluster up and running with kubectl installed

## steps to run

### get wls 12.2.1.3 docker image

#### pull the regularly built resiliency image
```
$ docker pull wlsldi-v2.docker.oraclecorp.com/weblogic-12.2.1.3-resiliency-sudossh:latest
```
#### tag it with developer:latest
```
$ docker tag wlsldi-v2.docker.oraclecorp.com/weblogic-12.2.1.3-resiliency-sudossh:latest weblogic-12.2.1.3-developer:latest
```

### build local wls image
```
$ docker build -t wls-installer .
```

### prepare volume directories
Three volumes are defined in k8s/pv.yml which refer to three external directories. You can use host paths or shared NFS directories. Pls change the paths accordingly.
   
### deploy all the k8s resources
```
$ kubectl create -f  k8s/secrets.yml 
$ kubectl create -f  k8s/pv.yml 
$ kubectl create -f  k8s/pvc.yml
$ kubectl create -f  k8s/wls-admin.yml
$ kubectl create -f  k8s/wls-stateful.yml
```

### check resources deployed to k8s after all the deployments finish
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

### check pv and pvc
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

### check secrets
```
$ kubectl get secrets
NAME                  TYPE                                  DATA      AGE
default-token-m93m1   kubernetes.io/service-account-token   3         39d
wlsecret              Opaque                                2         19m
```

### go to admin console to check server status
The admin console usl is 'http://[hostIP]:30007/console' and the user/pwd are weblogic/weblogic1.
 
### restart all pods

