package edu.illinois.cs.cogcomp.sl.bilinear;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.Table;
import edu.illinois.cs.cogcomp.sl.core.*;
import edu.illinois.cs.cogcomp.sl.util.DenseVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Shyam on 11/24/15.
 */
public class BilinearParams extends SLParameters {
    public int MAXITERS = 50;
    public int REPORT_ITERS = 10;

    int rank=10;
    float C=0.1f;
    private Map<String,Integer> labelMap;

    public float lambda1 = 0.1f;
    public float lambda2 = 0.1f;
    public float LR = 0.001f;
    public boolean average = false;
    public String Ufile;
    public String Wfile;
    public String modelPath;

//    public static void main(String[] args) {
//        DenseVector[] V=new DenseVector[40];
//        load_matrix("V.40.matrix",V);
//        System.out.println(V.length+" "+V[0].getLength());
//        for(int i=0;i<V.length;i++) {
//            for (int j = 0; j < V[0].getLength(); j++) {
//                System.out.print(V[i].get(j) + " ");
//            }
//            System.out.println();
//        }
//    }

    public void randomlyInitUW(int N, int D)
    {
        for (int i = 0; i < rank; ++i) {
            U[i] = getRandomUnitVector(N);
            W[i] = getRandomUnitVector(D);
        }
    }



    public static DenseVector getRandomUnitVector(int length)
    {
        DenseVector vec = new DenseVector();
        for (int i = 0; i < length; ++i) {
            vec.setElement(i,Params.rnd.nextFloat() - 0.5f);
        }
        double invSqrt = 1.0 / Math.sqrt(vec.getSquareL2Norm());
        vec.scale(invSqrt);
        return vec;
    }


    public BilinearParams(Map<String,Integer> labelMap, int rank,String Ufile, String Wfile){
        this.rank=rank;
        this.labelMap=labelMap;
        U=new DenseVector[rank];
        W=new DenseVector[rank];
        if(Params.average) {
            totalU = new DenseVector[rank];
            totalW = new DenseVector[rank];
        }
        Utils.load_matrix(Ufile, U);
        printUStats();
        Utils.load_matrix(Wfile, W);
        printWStats();
        if(Params.average) {
            totalU=copyMatrix(U);
            totalW=copyMatrix(W);
        }

    }



    public BilinearParams(Map<String,Integer> labelMap, int rank, int N, int D){
        this.rank=rank;
        this.labelMap=labelMap;
        U=new DenseVector[rank];
        W=new DenseVector[rank];
        randomlyInitUW(N, D);
        printUStats();
        printWStats();
    }


    /***
     *
     * @param train
     * @param dev for validation
     * @param sparseModel the sparse model obtained from MC classication, for interpolating
     * @param lambda1 for interpolation
     * @param modelPath loc where trained U and W will be written
     */
    public void trainBilinear(SLProblem train, SLProblem dev, SLModel sparseModel, float lambda1, String modelPath){
        this.lambda1=lambda1;
        float minloss = Float.POSITIVE_INFINITY;
        for (int iIter = 0; iIter< MAXITERS; ++iIter) {
            doOneIter(train, iIter);
            if(Params.average)
                averageParams((iIter+1)*train.size());

            if(iIter % REPORT_ITERS==0)
            {
                printUStats();
                printWStats();
            }
            float train_loss=validate(train,"train");
            if(train_loss==0.0)
            {
                System.out.println("Training loss=0!");
                break;
            }
            float loss = validate(dev, "dev");
            if(loss<minloss)
            {
                minloss=loss;
                System.out.println("min loss on dev so far, saving ....");
                bestU=copyMatrix(U);
                bestW=copyMatrix(W);
            }
//            interpolateValidate(train, "train", sparseModel);

            interpolateValidate(dev,"dev",sparseModel);
            if(Params.average)
                unaverageParams();

//            if(iIter % RENORM_ITER==0)
//            {
//                renormalize(U);
//                renormalize(W);
//            }
        }
        System.out.println("Reached Max Iters!");
        Utils.save_matrix(modelPath+"U."+"matrix",bestU);
        Utils.save_matrix(modelPath+"W."+"matrix",bestW);
    }

//    private void interpolateValidate(SLProblem sl, String type, SLModel sparseModel) {
//        ClassificationTester tester = new ClassificationTester();
//        float total_loss=0.0f;
//        for(int t=0;t<sl.size();t++) {
//            AlgebraInstance x = (AlgebraInstance) sl.instanceList.get(t);
//            TemplateLabel tgold = (TemplateLabel) sl.goldStructureList.get(t);
//            float bestScore=Float.NEGATIVE_INFINITY;
//            TemplateLabel best=null;
//            for (String sign : labelMap.keySet()) {
//                int i = labelMap.get(sign);
//                TemplateLabel candidate = new TemplateLabel(sign, i);
//                float score = lambda1 * getScore(x,candidate);
//                score+=(1-lambda1)*sparseModel.wv.dotProduct(sparseModel.featureGenerator.getFeatureVector(x,candidate));
//                if(score>bestScore)
//                {
//                    bestScore=score;
//                    best=candidate;
//                }
//            }
//            TemplateLabel tpred = best;
//            if(tpred.output!=tgold.output)
//            {
//                total_loss+=1.0f;
//            }
//            tester.record(tgold.template + "", tpred.template + "");
//        }
//        System.out.println(type + "-->total loss:" + total_loss + "/" + sl.size());
//        Table perfTable = tester.getPerformanceTable();
//        for(int jj=0;jj<perfTable.getColumnCount();jj++)
//            System.out.print(perfTable.getValueAt(perfTable.getRowCount() - 1, jj) + " ");
//        System.out.println();
//
//
//    }
//
//    private void unaverageParams() {
//        U=backupU;
//        W=backupW;
//    }




//    public float validate(SLProblem sl, String datatype) {
//        float total_loss=0f;
//        ClassificationTester tester = new ClassificationTester();
//        List<Integer> wrong_ids = new ArrayList<>();
//        for(int t=0;t<sl.size();t++) {
//            IInstance x = sl.instanceList.get(t);
//            AlgebraInstance prob = (AlgebraInstance) x;
//            IStructure gold = sl.goldStructureList.get(t);
//            IStructure pred = getBestStructure(x);
//            TemplateLabel tpred = (TemplateLabel) pred;
//            TemplateLabel tgold = (TemplateLabel) gold;
//            if(tpred.output!=tgold.output)
//            {
//                total_loss+=1.0f;
//                wrong_ids.add(prob.id);
//            }
//            tester.record(tgold.template+ "", tpred.template+ "");
//        }
//        System.out.println(datatype + "-->total loss:" + total_loss + "/" + sl.size());
//        Table perfTable = tester.getPerformanceTable();
//        for(int jj=0;jj<perfTable.getColumnCount();jj++)
//            System.out.print(perfTable.getValueAt(perfTable.getRowCount() - 1, jj) + " ");
//        System.out.println();
//        // prec@k
//        getPrecAtK(sl);
//        return total_loss;
//    }

//    private void getPrecAtK(SLProblem sl) {
//        ClassificationTester precAtK = new ClassificationTester();
//        int K=10;
//
//        for(int idx=0;idx<sl.size();idx++) {
//            AlgebraInstance mi = (AlgebraInstance) sl.instanceList.get(idx);
//            TemplateLabel lmi = (TemplateLabel) sl.goldStructureList.get(idx);
//            Counter<TemplateLabel> cnt = new Counter<>();
//
//            for (String sign : labelMap.keySet()) {
//                int i = labelMap.get(sign);
//                TemplateLabel candidate = new TemplateLabel(sign, i);
//                float score = getScore(mi,candidate);
//                cnt.incrementCount(candidate,score);
//            }
//
//            List<TemplateLabel> rank_list = cnt.getSortedItemsHighestFirst();
//            assert rank_list.size()==labelMap.size():"all candidates must be scored! "+rank_list.size()+" "+labelMap.size();
//
//            boolean found=false;
//            for(int t=0;t<rank_list.size();t++)
//            {
//                TemplateLabel label = rank_list.get(t);
////                System.out.println(label+" gold:"+lmi);
////                System.out.println(label.output+" "+lmi.output);
//                if(label.output==lmi.output)
//                {
//                    found=true;
//                }
//                if(t==K)
//                {
//                    break;
//                }
//            }
//            if(found)
//            {
////                System.out.println("YAYY!");
//                precAtK.record(lmi.template+"",lmi.template+"");
//            }
//            else
//                precAtK.record(lmi.template+"",rank_list.get(0).template+"");
//        }
//        System.out.println("---------------");
//        Table p_at_k= precAtK.getPerformanceTable();
//        System.out.println("Perf at K="+K);
//        for(int jj=0;jj<p_at_k.getColumnCount();jj++)
//            System.out.print(p_at_k.getValueAt(p_at_k.getRowCount() - 1, jj) + " ");
//        System.out.println();
//    }

    private float ComputeLoss(IInstance x, IStructure pred, IStructure gold) {
        float loss=0.0f;
        TemplateLabel tpred = (TemplateLabel) pred;
        TemplateLabel tgold = (TemplateLabel) gold;
        if(tpred.output!=tgold.output)
        {
            loss+=1.0f;
        }
        float predScore = getScore(x, pred);
        float goldScore = getScore(x, gold);
        loss+= predScore;
        loss-= goldScore;
        if(Float.isNaN(loss) || Float.isInfinite(loss))
        {
            System.out.println("something wrong with loss "+predScore+" "+goldScore);
            System.exit(-1);
        }
        return loss;
    }





    private float getScore(IInstance prob, IStructure temp) {
        AlgebraInstance x = (AlgebraInstance) prob;
        TemplateLabel t = (TemplateLabel) temp;
        t.extractBaseFeature(); // IMPORTANT!!!
        SparseFeatureVector fx = x.fx;
        SparseFeatureVector ft = t.ft;

        DenseVector Ux = projectU(fx);
        DenseVector Wt = projectW(ft);
        float score = Ux.dotProduct(Wt);
        if(Float.isNaN(score) || Float.isInfinite(score))
        {
            System.out.println("Infite or Nan score");
            System.exit(-1);
        }
        return score;
    }

    private IStructure getBestStructure(IInstance x) {
        return getLossAugmentedBestStructure(x,null);
    }

//    public void updateJoint(IInstance x, IStructure gold, IStructure pred, Float loss){
//
//        AlgebraInstance mx = (AlgebraInstance) x;
//        TemplateLabel tpred = (TemplateLabel) pred;
//        TemplateLabel tgold = (TemplateLabel) gold;
//        SparseFeatureVector fx = mx.fx; // phi(x)
//        SparseFeatureVector t_hat = tpred.ft; // phi(t^)
//        SparseFeatureVector t= tgold.ft; // phi(t)
//        SparseFeatureVector dt_hat_t = (SparseFeatureVector) t_hat.difference(t);
//
////        DenseVector Wt_hat = projectW(t_hat); // W x phi(t^)
////        DenseVector Wt = projectW(t); // W x phi(t)
//
//        assert dt_hat_t.getNumActiveFeatures()<=2 : "diff of one hot can have atmost 2 diff";
//        DenseVector Wt_hat_minus_Wt = projectW(dt_hat_t);
//
//        System.out.println("updating U and W");
//        printMatrix(U);
//        printUStats();
//        SparseFeatureVector dUk;
//        DenseVector Ux = projectU(fx);
//        SparseFeatureVector dt = (SparseFeatureVector) t_hat.difference(t);
//        SparseFeatureVector dWk;
//        float norm=0.0f;
//        for (int k = 0; k < rank; k++) {
//            dUk = new SparseFeatureVector(fx.getIndices(),fx.getValues());
//            dUk.multiply(Wt_hat_minus_Wt.get(k));
//            dWk= new SparseFeatureVector(dt.getIndices(),dt.getValues());
//            dWk.multiply(Ux.get(k));
//
//            norm += dUk.getSquareL2Norm();
//            norm += dWk.getSquareL2Norm();
//
//            float alpha = Math.min(C,loss/norm);
//            if(alpha>0) {
//                System.out.println("alphaU=" + alpha);
//
//                if (Float.isInfinite(alpha) || Float.isNaN(alpha)) {
//                    System.out.println("infnite/nan step length " + loss + "/" + norm);
//                    System.exit(-1);
//                }
//                U[k].addSparseFeatureVector(dUk, -1.0f * alpha);
//                W[k].addSparseFeatureVector(dWk, -1.0f * alpha);
//            }
//        }
//        printMatrix(U);
//        printUStats();
//        printMatrix(W);
//        printWStats();
//    }







}
