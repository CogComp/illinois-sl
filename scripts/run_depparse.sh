#!/usr/bin/env bash
# JAVA=java
# jarPath=dist/illinois-sl-1.3-jar-with-dependencies.jar
MEMORY="-Xmx8g -XX:MaxPermSize=500m"
# OPTIONS="$MEMORY -Xss40m -ea "

# nice $JAVA  -ea -Xmx4096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.depparse.MainClass $* 


CP="./config/:./target/classes/:./target/dependency/*"

OPTIONS="$MEMORY -Xss40m -ea -cp $CP"
PACKAGE_PREFIX="edu.illinois.cs.cogcomp"
MAIN="$PACKAGE_PREFIX.sl.applications.depparse.MainClass"

time nice java $OPTIONS $MAIN $CONFIG_STR $*
