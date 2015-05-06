version=0.2

mvn compile assembly:single
mvn package
mvn source:jar
mvn cite
mkdir -p dist
cp target/*.jar dist
yes| cp -rf target/site/apidocs doc
mvn clean

zip -r illinois-SL.$version.zip src target doc scripts/*.sh  data dist config test pom.xml README.standalone
