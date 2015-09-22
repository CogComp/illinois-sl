JAVA=java
jarPath=dist/illinois-sl-1.3-jar-with-dependencies.jar

nice $JAVA  -ea -Xmx2096M -cp $jarPath edu.illinois.cs.cogcomp.sl.applications.cs_multiclass.MainClass  $*
