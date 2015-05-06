log=testAllSolversLog
rm scripts/$log
for x in config/*;
do
echo $x | tee -a scripts/$log

#tutorial
echo 'run tutorial...' | tee -a scripts/$log
bash scripts/run_tutorial.sh trainPOSModel data/tutorial/big.train  $x model | tee -a scripts/$log
bash scripts/run_tutorial.sh testPOSModel model data/tutorial/big.test | tee -a scripts/$log

#sequence
echo 'run sequence...' | tee -a scripts/$log
bash scripts/run_sequence.sh  trainSequenceModel data/sequence/wsj.sub.train $x model | tee -a scripts/$log
bash scripts/run_sequence.sh  testSequenceModel model data/sequence/wsj.sub.test  | tee -a scripts/$log

#multiclass
echo 'run multiclass...' | tee -a scripts/$log
bash scripts/run_multiclass.sh  trainMultiClassModel data/multiclass/heart_scale.train  data/multiclass/heart_scale.cost $x model | tee -a scripts/$log
bash scripts/run_multiclass.sh  testMultiClassModel model data/multiclass/heart_scale.test | tee -a scripts/$log

#reranking
echo 'run raranking...' | tee -a scripts/$log
bash scripts/run_reranking.sh  trainRankingModel data/reranking/rerank.train $x model | tee -a scripts/$log
bash scripts/run_reranking.sh  testRankingModel model data/reranking/rerank.test | tee -a scripts/$log

#depparse
echo 'run depparse...' | tee -a scripts/$log
bash scripts/run_depparse.sh train data/depparse/english_train.conll $x eng.model | tee -a scripts/$log
bash scripts/run_depparse.sh test eng.model data/depparse/english_test.conll | tee -a scripts/$log

echo '' | tee -a scripts/$log
done

