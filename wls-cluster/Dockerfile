# author Lily He
FROM jdk8:latest

# set env
# --------------------------------------------------------------------
ENV ORACLE_HOME=/u01/oracle \
    USER_MEM_ARGS="-Djava.security.egd=file:/dev/./urandom" \
    EXE_HOME=/u01/bin \
    PATH=/usr/java/default/bin:/u01/bin:/u01/mydomain:$PATH \
    HTTPS_PROXY=cn-proxy.jp.oracle.com:80 \
    HTTP_PROXY=cn-proxy.jp.oracle.com:80 \
    http_proxy=cn-proxy.jp.oracle.com:80 \
    https_proxy=cn-proxy.jp.oracle.com:80 \
    no_proxy='127.0.0.1, localhost, 172.17.0.*' \
    JAVA_PKG=server-jre-8u*-linux-x64.tar.gz \
    FMW_JAR=fmw_12.2.1.*wls*.jar \
    JAVA_HOME=/usr/java/default \
    DOMAIN_HOME=/u01/mydomain
 
    
# Setup filesystem and oracle user
# Adjust file permissions, go to /u01 as user 'oracle' to proceed with WLS installation
# ------------------------------------------------------------  
RUN mkdir -p /u01 && \
    chmod a+xr /u01 && \
    useradd -b /u01 -d /u01/oracle -m -s /bin/bash oracle && \
    chown oracle:oracle -R /u01

RUN mkdir -p /scripts
COPY scripts/* /scripts/

RUN mkdir -p /samples
COPY jmsclient/samples/* /samples/

# Copy packages
# -------------
COPY $FMW_JAR install.file oraInst.loc /u01/

# Install weblogic
# ------------------------------------------------------------
USER oracle

RUN $JAVA_HOME/bin/java -jar /u01/$FMW_JAR -silent -responseFile /u01/install.file -invPtrLoc /u01/oraInst.loc -jreLoc $JAVA_HOME -ignoreSysPrereqs -force -novalidation ORACLE_HOME=$ORACLE_HOME INSTALL_TYPE="WebLogic Server" && \
    rm /u01/$FMW_JAR /u01/oraInst.loc /u01/install.file

USER root
# install pip and python requests module
# ------------------------------------------------------------
COPY get-pip.py setup.sh $EXE_HOME/

RUN chmod +x $EXE_HOME/setup.sh && \
    chmod +x $EXE_HOME/get-pip.py && \
    python $EXE_HOME/get-pip.py && \
    pip install requests && \
    rm $EXE_HOME/get-pip.py && \
    mkdir $DOMAIN_HOME

COPY startMS.sh $DOMAIN_HOME/

RUN chmod +x $DOMAIN_HOME/startMS.sh

# VOLUME ["/u01/oracle", "/u01/mydomain", "/u01/stores"]
WORKDIR ${ORACLE_HOME}

# Define default command to start script.
CMD ["setup.sh"]


