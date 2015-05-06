#!/usr/bin/env bash
JAVA=java
jarPath=dist/illinois-sl-0.2.2-jar-with-dependencies.jar
MEMORY="-Xmx8g -XX:MaxPermSize=500m"
OPTIONS="$MEMORY -Xss40m -ea "

nice $JAVA  -ea -Xmx4096M -cp $jarPath $OPTIONS edu.illinois.cs.cogcomp.sl.applications.depparse.MainClass  $*
