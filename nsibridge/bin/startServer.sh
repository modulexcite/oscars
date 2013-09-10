#!/bin/sh

pidfile=$1
jarfile=$2
shortname=nsibridge

#set context 
if [ -z "$context" ]; then
    context="DEVELOPMENT"
fi

if [ -z "$pidfile" ]; then
    DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
    if [ ! -d "$DEFAULT_PID_DIR" ]; then
        mkdir "$DEFAULT_PID_DIR"
    fi
    pidfile=$DEFAULT_PID_DIR/${shortname}.pid
fi

if [ -z "$jarfile" ]; then
    vers=`cat $OSCARS_DIST/VERSION`
    jarfile=$OSCARS_DIST/${shortname}/target/${shortname}-$vers.one-jar.jar
    echo "Starting ${shortname} with version:$vers context:$context"
fi

java -Djava.net.preferIPv4Stack=true -Xmx512m -Dlog4j.configuration=file:/etc/oscars/NsiBridgeService/conf/log4j.properties -Dnsibridge.manifest=file:/etc/oscars/NsiBridgeService/conf/manifest.yaml -Dnsibridge.beans=file:/etc/oscars/NsiBridgeService/conf/beans.xml -jar $jarfile&
echo $! > $pidfile

