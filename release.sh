version=1.0
sed -i "s/jarPath=.*/jarPath=dist\/illinois-sl-$version-jar-with-dependencies.jar/g" scripts/run_depparse.sh;
sed -i "s/jarPath=.*/jarPath=dist\/illinois-sl-$version-jar-with-dependencies.jar/g" scripts/run_multiclass.sh;
sed -i "s/jarPath=.*/jarPath=dist\/illinois-sl-$version-jar-with-dependencies.jar/g" scripts/run_reranking.sh;
sed -i "s/jarPath=.*/jarPath=dist\/illinois-sl-$version-jar-with-dependencies.jar/g" scripts/run_sequence.sh;
sed -i "s/jarPath=.*/jarPath=dist\/illinois-sl-$version-jar-with-dependencies.jar/g" scripts/run_tutorial.sh;
bash scripts/getExternalDeps.sh
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
data="data/depparse/english_train.conll data/depparse/english_test.conll data/multiclass/heart_scale.cost data/multiclass/heart_scale.train data/multiclass/heart_scale.test \
	data/reranking/rerank.train data/reranking/rerank.test data/reranking/rerank-README.standalone data/sequence/wsj.sub.train data/sequence/wsj.sub.test data/tutorial/big.train data/tutorial/big.test" 
zip -r illinois-SL.$version.zip src target doc scripts/*.sh  $data dist/illinois-sl-$version*.jar config/*.config test pom.xml README.md
