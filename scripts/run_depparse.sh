#!/usr/bin/env bash

#mvn -q dependency:copy-dependencies
#mvn -q compile

CP="./config/:./target/classes/:./target/dependency/*"

MEMORY="-Xmx20g -XX:MaxPermSize=500m"

OPTIONS="$MEMORY -Xss40m -ea -cp $CP"
PACKAGE_PREFIX="edu.illinois.cs.cogcomp"

MAIN="$PACKAGE_PREFIX.sl.applications.depparse.MainClass"

time nice java $OPTIONS $MAIN $CONFIG_STR $*
