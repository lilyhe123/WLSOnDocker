
## Overview
This demo creates wls domain running in k8s with a admin server, several ms and one mysql db as leasing.

* Persistent volumes are used for mysql data folder, domain home and file store folder.
* Domain is provisioned on the fly via REST api after admin server starts. And managed servers share the same domain home via volume, so no need to create domain image.
* The domain has file stores, jms servers and jms destinations deployed to cluster.
* It has jms clients to send/receive msgs.

## Steps to run
Pls follow steps below to run this demo. This may only work on hostlinux. I didn't try this on Mac.

### Get wls docker image

#### pull the regularly built resiliency image
```
$ docker pull wlsldi-v2.docker.oraclecorp.com/weblogic-12.2.1.3-resiliency-sudossh:latest
```
#### tag it with developer:latest to be able to build summercamp
```
$ docker tag wlsldi-v2.docker.oraclecorp.com/weblogic-12.2.1.3-resiliency-sudossh:latest weblogic-12.2.1.3-developer:latest
```

#### Make sure your kubernetes environment is started and ready
See: //depot/dev/wls-k8s/infra/install_docker_k8s.sh

#### Prepare volume folders and update k8s/volumes.yml accordingly
* create three empty folders with names 'v1', 'v2', 'v3' under /scratch/<uid>/vdata
* update values of volume path in k8s/volumes.yml accordingly: find 'scratch/lihhe/vdata/v*' and change to 'scratch/<uid>/vdata/v*'
   
#### Run autorun.sh which includes following steps
* deploy all the resources to k8s, including persistent volumes, mysql server, admin server, managed servers.
* wait until all pods/services are ready in K8s and all WLS resources are ready in wls domain.
* run jms client to send msgs to jms queue and verify the sending succeed.

#### After run you'll get following resources deployed to k8s
```
$ kubectl get all
NAME                                   READY     STATUS    RESTARTS   AGE
po/jms-admin-server-1908067799-1l29x   1/1       Running   0          5m
po/jms-mserver-0                       1/1       Running   0          5m
po/jms-mserver-1                       1/1       Running   0          2m
po/mysql-server-1005119867-6lrj1       1/1       Running   0          5m

NAME                   CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
svc/jms-admin-server   10.102.214.119   <nodes>       8001:30007/TCP   5m
svc/jms-service        10.102.113.105   <nodes>       8011:30009/TCP   5m
svc/jms-subdomain      None             <none>        8011/TCP         5m
svc/kubernetes         10.96.0.1        <none>        443/TCP          7d
svc/mysql-server       None             <none>        3306/TCP         5m

NAME                       DESIRED   CURRENT   AGE
statefulsets/jms-mserver   2         2         5m

NAME                      DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
deploy/jms-admin-server   1         1         1            1           5m
deploy/mysql-server       1         1         1            1           5m

NAME                             DESIRED   CURRENT   READY     AGE
rs/jms-admin-server-1908067799   1         1         1         5m
rs/mysql-server-1005119867       1         1         1         5m
```
#### check persistent volumes
```
$ kubectl get pv
NAME      CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS    CLAIM                         STORAGECLASS   REASON    AGE
pv1       10Gi       RWO           Recycle         Bound     default/mysql-pv-claim        manual                   25s
pv2       10Gi       RWX           Recycle         Bound     default/wlserver-pv-claim-2   manual                   25s
pv3       10Gi       RWX           Recycle         Bound     default/wlserver-pv-claim-1   manual                   25s
```

