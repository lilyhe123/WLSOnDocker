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

### make sure your kubernetes environment is started and ready
See: //depot/dev/wls-k8s/infra/install_docker_k8s.sh

### prepare the physical volume folders
Three volumes are defined in pv.yml. Currently they refer to three local folders under //scratch/k8s_dir/volumes.
Before proceeding, pls make sure the referred folders by volumes are already there and are empty.

### deploy all the k8s resources
```
$ kubectl create -f  k8s/secrets.yml
$ kubectl create -f  k8s/pv.yml
$ kubectl create -f  k8s/pvc.yml
$ kubectl create -f  k8s/wls-admin.yml
$ kubectl create -f  k8s/wls-stateful.yml
```

### after run you'll get following resources deployed to k8s
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
We have three pv defined and two pvc defined. But only one pvc is actually used by weblogic server pods.
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
### go to the weblogic admin console to check server status
The admin console usl is 'http://[hostIP]:30007/console' and the user/pwd are weblogic/weblogic1.
