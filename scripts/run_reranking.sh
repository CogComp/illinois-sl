JAVA=java
LOCALCLASSPATH=
for item in `ls target/dependency`; do
    LOCALCLASSPATH=target/dependency/$item:$LOCALCLASSPATH
done

#echo $LOCALCLASSPATH

nice $JAVA  -ea -Xmx4096M -cp target/classes:$LOCALCLASSPATH edu.illinois.cs.cogcomp.sl.applications.reranking.MainClass  $*
