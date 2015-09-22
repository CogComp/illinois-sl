JAVA=java
jarPath=dist/illinois-sl-1.3-jar-with-dependencies.jar

nice $JAVA  -ea -Xmx4096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.reranking.MainClass  $*
