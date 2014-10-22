log=testAllSolversLog
rm $log
cd ..
for x in config/*;
do
echo $x | tee -a scripts/$log
bash run.sh trainSequenceModel data/big.train $x | tee -a scripts/$log
bash run.sh testSequenceModel big.train.model data/big.test | tee -a scripts/$log;
echo '' | tee -a scripts/$log
done

cd scripts
