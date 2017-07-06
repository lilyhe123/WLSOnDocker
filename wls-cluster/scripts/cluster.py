# Author Lily He

import requests
from requests.auth import HTTPBasicAuth 
import json
import shutil
import sys
import os
from time import time
from time import sleep
from collections import OrderedDict
import base

dsfile='ds1-jdbc.xml'
serverdata='cluster.json'
modulefile='mymodule-jms.xml'
inputfile='jmsres.json'

def createAll():
    base.waitAdmin()
    base.cpJDBCResource(dsfile)
    base.createAll(serverdata)
    base.cpJMSResource(modulefile)
    base.createAll(inputfile)

def waitUntil():
    print("start checking jmsserver1 status")
    tail1='domainRuntime/serverRuntimes/ms1/JMSRuntime/JMSServers/jmsserver1-01?links=none&fields=name,healthState'
    tail2='domainRuntime/serverRuntimes/ms2/JMSRuntime/JMSServers/jmsserver1-01?links=none&fields=name,healthState'
    fail = True
    while(fail):
        sleep(2)
        res = base.get(tail1)
        if(res.ok):
            return
        res = base.get(tail2)
        if(res.ok):
            return

print 'url:', base.prefix 
start=time()
option=sys.argv[1]
if(option == 'create'):
    createAll()
elif(option == 'wait'):
    waitUntil()

end=time()
print option, "spent", (end-start), "seconds"
