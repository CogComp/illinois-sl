version=0.2.2

mvn compile assembly:single
mvn package
mvn source:jar
mvn site
mvn dependency:copy-dependencies
mkdir -p dist
mkdir -p lib
cp target/dependency/*.jar lib
cp target/*.jar dist
yes| cp -rf target/site/apidocs doc
mvn clean

zip -r illinois-SL.$version.zip src target doc scripts/*.sh  data dist config test pom.xml README.standalone
