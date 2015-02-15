JAVA=java
jarPath=dist/illinois-sl-0.2-jar-with-dependencies.jar

nice $JAVA  -ea -Xmx4096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.reranking.MainClass  $*
