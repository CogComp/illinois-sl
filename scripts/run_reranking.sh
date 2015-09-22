JAVA=java
jarPath=dist/illinois-sl-1.0-jar-with-dependencies.jar

nice $JAVA  -ea -Xmx4096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.reranking.MainClass  $*
