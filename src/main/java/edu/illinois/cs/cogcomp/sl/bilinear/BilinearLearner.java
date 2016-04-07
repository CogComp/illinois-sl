package edu.illinois.cs.cogcomp.sl.bilinear;

import edu.illinois.cs.cogcomp.sl.core.*;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.DenseVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * Created by upadhya3 on 4/7/16.
 */
public class BilinearLearner {

    private final BilinearParams params;
    private DenseVector[] bestW;
    private DenseVector[] bestU;
    private DenseVector[] backupU;
    private DenseVector[] backupW;
    public DenseVector[] U, W;
    public DenseVector[] totalU, totalW;

    public BilinearLearner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg,  BilinearParams params)
    {
//        super(infSolver,fg,params);
        this.params= params;
        U=new DenseVector[params.rank];
        W=new DenseVector[params.rank];
        if(params.average) {
            totalU = new DenseVector[params.rank];
            totalW = new DenseVector[params.rank];
        }
        Utils.load_matrix(params.Ufile, U);
        Utils.printUStats(U,params);
        Utils.load_matrix(params.Wfile, W);
        Utils.printWStats(W,params);
        if(params.average) {
            totalU=Utils.copyMatrix(U);
            totalW=Utils.copyMatrix(W);
        }

    }
    public WeightVector train(SLProblem sp) throws Exception {
        return train(sp,new WeightVector(10000));
    }

    public WeightVector train(SLProblem train, WeightVector init) throws Exception {
        float minloss = Float.POSITIVE_INFINITY;
        for (int iIter = 0; iIter< params.MAXITERS; ++iIter) {
            doOneIter(train, iIter);
            if(params.average)
                averageParams((iIter+1)*train.size());

            if(iIter % params.REPORT_ITERS==0)
            {
                Utils.printUStats(U, params);
                Utils.printWStats(W, params);
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
                bestU=Utils.copyMatrix(U);
                bestW=Utils.copyMatrix(W);
            }
//            interpolateValidate(train, "train", sparseModel);

            interpolateValidate(dev,"dev",sparseModel);
            if(params.average)
                unaverageParams();

//            if(iIter % RENORM_ITER==0)
//            {
//                renormalize(U);
//                renormalize(W);
//            }
        }
        System.out.println("Reached Max Iters!");
        Utils.save_matrix(params.modelPath+"U."+"matrix",bestU);
        Utils.save_matrix(params.modelPath+"W."+"matrix",bestW);
    }

    private void averageParams(int T) {
        backupU=U;
        DenseVector[] avgU = new DenseVector[params.rank];
        for(int i=0;i<params.rank;i++)
        {
            avgU[i]=new DenseVector();
            for(int z=0;z<U[i].getLength();z++) {
                float val = (U[i].get(z) * (T + 1) - totalU[i].get(z)) / T;
                avgU[i].setElement(z,val);
            }
        }
        U=avgU;

        ////
        backupW=W;
        DenseVector[] avgW = new DenseVector[params.rank];
        for(int i=0;i<params.rank;i++)
        {
            avgW[i]=new DenseVector();
            for(int z=0;z<W[i].getLength();z++) {
                float val = (W[i].get(z) * (T + 1) - totalW[i].get(z)) / T;
                avgW[i].setElement(z,val);
            }
        }
        W=avgW;
    }


    public void doOneIter(SLProblem train, int currIter){
        float loss;
        for(int t=0;t<train.size();t++)
        {
            IInstance x = train.instanceList.get(t);
            IStructure gold = train.goldStructureList.get(t);
            IStructure pred = getLossAugmentedBestStructure(x, gold);
            loss=ComputeLoss(x,pred,gold);
            if(loss!=0)
            {
                updateAlternate(x, gold, pred, loss, currIter*train.size()+t+1);
                // updateJoint(x, gold, pred, loss);
            }

//            if(norm_after_every_update)
//            {
//                renormalize(U);
//                renormalize(W);
//            }
        }

    }

    public void updateAlternate(IInstance x, IStructure gold, IStructure pred, Float loss, int updCnt){

        AlgebraInstance mx = (AlgebraInstance) x;
        TemplateLabel tpred = (TemplateLabel) pred;
        TemplateLabel tgold = (TemplateLabel) gold;
        SparseFeatureVector fx = mx.fx; // phi(x)
        SparseFeatureVector t_hat = tpred.ft; // phi(t^)
        SparseFeatureVector t= tgold.ft; // phi(t)
        SparseFeatureVector dt_hat_t = (SparseFeatureVector) t_hat.difference(t);

        assert dt_hat_t.getNumActiveFeatures()<=2 : "diff of one hot can have atmost 2 diff";
        DenseVector Wt_hat_minus_Wt = projectW(dt_hat_t); // W phi(t^) - W phi(t)

        // update U
        updateU(fx,Wt_hat_minus_Wt,loss,updCnt);

        // update W
        updateW(fx,dt_hat_t,loss,updCnt);

    }

    private void updateU(SparseFeatureVector fx, DenseVector Wt_hat_minus_Wt, Float loss, int updCnt) {
//        System.out.println("updating U");
//        printMatrix(U);
//        printUStats();
        SparseFeatureVector dUk;
        for (int k = 0; k < params.rank; k++) {
            dUk = new SparseFeatureVector(fx.getIndices(),fx.getValues());
            dUk.multiply(Wt_hat_minus_Wt.get(k));
//            float norm = dUk.getSquareL2Norm();
//            float alpha = Math.min(C,loss/norm);
//            if(alpha>0) {
////                System.out.println("alphaU=" + alpha + " normU="+norm+ " lossU="+loss);
//                if (Float.isInfinite(alpha) || Float.isNaN(alpha)) {
//                    System.out.println("infnite/nan step length " + loss + "/" + norm);
//                    System.exit(-1);
//                }
//                U[k].addSparseFeatureVector(dUk, -1.0f * alpha);
//                U[k].addDenseVector(U[k], -1.0f * alpha * lambda1);
//                if(Params.average) {
//                    totalU[k].addSparseFeatureVector(dUk, -1.0f * updCnt * alpha);
//                    totalU[k].addDenseVector(U[k], -1.0f * updCnt * alpha);
//                }
//            }
            U[k].addSparseFeatureVector(dUk, -1.0f * params.LR);
//            U[k].addDenseVector(U[k], -1.0f * alpha * lambda1);
            if(params.average) {
                totalU[k].addSparseFeatureVector(dUk, -1.0f * updCnt * params.LR);
//                totalU[k].addDenseVector(U[k], -1.0f * updCnt * LR);
            }
        }
//        printMatrix(U);
//        printUStats();
    }

    private void updateW(SparseFeatureVector fx, SparseFeatureVector dt_hat_t, Float loss, int updCnt) {
//        System.out.println("updating W");
//        printMatrix(W);
//        printWStats();
        DenseVector Ux = projectU(fx);
        SparseFeatureVector dWk;
        for (int k = 0; k < params.rank; ++k) {
            dWk= new SparseFeatureVector(dt_hat_t.getIndices(),dt_hat_t.getValues());
            dWk.multiply(Ux.get(k));
//            float norm =dWk.getSquareL2Norm();
//            float alpha = Math.min(C,loss/norm);
//            if(alpha>0) {
//                System.out.println("alphaW=" + alpha + " normW="+norm+ " lossW="+loss);
//                if (Float.isInfinite(alpha) || Float.isNaN(alpha)) {
//                    System.out.println("infnite/nan step length " + loss + "/" + norm);
//                    System.exit(-1);
//                }
//                W[k].addSparseFeatureVector(dWk, -1.0f * alpha);
//                W[k].addDenseVector(W[k], -1.0f * alpha * lambda2);
//                if(Params.average) {
//                    totalW[k].addSparseFeatureVector(dWk, -1.0f * updCnt * alpha);
//                    totalW[k].addDenseVector(W[k], -1.0f * updCnt * alpha);
//                }
//            }
            W[k].addSparseFeatureVector(dWk, -1.0f * params.LR);
            if(params.average) {
                totalW[k].addSparseFeatureVector(dWk, -1.0f * updCnt * params.LR);
//                totalW[k].addDenseVector(W[k], -1.0f * updCnt * alpha);
            }
        }
//        printMatrix(W);
//        printWStats();
    }

    /**
     * takes the feat vec of X and projects it to dense
     * @param fv
     */
    public DenseVector projectU(SparseFeatureVector fv)
    {
        DenseVector result = new DenseVector();
        for (int r = 0; r < params.rank; ++r)
        {
            // todo: make sure you do not need the other case handling
            float res = 0.0f;
            for(int i=0; i< fv.getNumActiveFeatures(); i++){
                res += U[r].get(fv.getIdx(i)) * fv.getValue(i);
            }
            result.setElement(r,res);
        }
        return result;
    }

    /**
     * * takes the feat vec of T and projects it to dense
     * @param fv
     */
    public DenseVector projectW(SparseFeatureVector fv)
    {
        DenseVector result = new DenseVector();
        for (int r = 0; r < params.rank; ++r)
        {
            // todo: make sure you do not need the other case handling
            float res = 0.0f;
            for(int i=0; i< fv.getNumActiveFeatures(); i++){
                res += W[r].get(fv.getIdx(i)) * fv.getValue(i);
            }
            result.setElement(r,res);
        }
        return result;
    }
}
