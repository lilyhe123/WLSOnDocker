FROM oraclelinux:latest

ENV INSTALL_DIR=/install \
    HTTPS_PROXY=cn-proxy.jp.oracle.com:80 \
	HTTP_PROXY=cn-proxy.jp.oracle.com:80 \
	http_proxy=cn-proxy.jp.oracle.com:80 \
	https_proxy=cn-proxy.jp.oracle.com:80 \
	JAVA_PKG=server-jre-8u*-linux-x64.tar.gz \
    JAVA_HOME=/usr/java/default \

# install java
# ------------------------------------------------------------  
ADD  $JAVA_PKG /usr/java/

RUN export JAVA_DIR=$(ls -1 -d /usr/java/*) && \
    ln -s $JAVA_DIR /usr/java/latest && \
    ln -s $JAVA_DIR /usr/java/default && \
    alternatives --install /usr/bin/java java $JAVA_DIR/bin/java 20000 && \
    alternatives --install /usr/bin/javac javac $JAVA_DIR/bin/javac 20000 && \
    alternatives --install /usr/bin/jar jar $JAVA_DIR/bin/jar 20000


WORKDIR $INSTALL_DIR



