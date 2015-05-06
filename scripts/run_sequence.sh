JAVA=java
jarPath=dist/illinois-sl-0.2.2-jar-with-dependencies.jar

nice $JAVA  -ea -Xmx2096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.sequence.MainClass  $*

