#mvn -q dependency:copy-dependencies
#mvn -q compile

CP="./config/:./target/classes/:./target/dependency/*"

OPTIONS="-Xss40m -ea -cp $CP"
PACKAGE_PREFIX="edu.illinois.cs.cogcomp"

MAIN="$PACKAGE_PREFIX.sl.applications.depparse.io.DependencyReader"

time nice java $OPTIONS $MAIN $CONFIG_STR $*
