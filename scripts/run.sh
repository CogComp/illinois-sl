#!/usr/bin/env bash

MEMORY="-Xmx8g -XX:MaxPermSize=500m"

CP="./config/:./target/classes/:./target/dependency/*"

OPTIONS="$MEMORY -Xss40m -ea -cp $CP"
PACKAGE_PREFIX="edu.illinois.cs.cogcomp"
MAIN="$PACKAGE_PREFIX.sl.applications.depparse.MainClass"
MAIN="$PACKAGE_PREFIX.sl.applications.cs_multiclass.MainClass"
time nice java $OPTIONS $MAIN $CONFIG_STR $*
