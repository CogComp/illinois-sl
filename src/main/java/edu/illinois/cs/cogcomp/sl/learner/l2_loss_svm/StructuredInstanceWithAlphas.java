package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/*
 * This class maintains the working set for one structure
 */
public class StructuredInstanceWithAlphas{

	static Logger logger = LoggerFactory.getLogger(StructuredInstanceWithAlphas.class);
	protected static int MAX_DCD_INNNER_ITER = 10;
	protected static float DCD_INNNER_STOP = 0.1f;
	public static int cacheYHeuristicCount = 0;

	protected final static float UPDATE_CONDITION = 1e-12f;


	protected IInstance ins = null;
	protected float sC = 0.0f;

	protected Lock lock;

	public static class L2SolverInfo {
		public float PGMaxNew = Float.NEGATIVE_INFINITY;
		public float PGMinNew = Float.POSITIVE_INFINITY;
	}


	protected IStructure goldStructure;
	protected IFeatureVector goldFeatureVector;
	private List<IStructure> candidateStructureList;	// list of structures found using loss augmented inference currently in the working set
	protected List<Pair<float[], IFeatureVector>> alphaFeatureVectorList;	
	// each pair contains first float: alpha, second float: loss
	// the Feature vector is the difference of the gold feature and the features of the candidate structure.
	
	protected L2LossSSVMDCDSolver basedSolver = null;
	protected Set<IStructure> structuredSet = Collections.newSetFromMap(new ConcurrentHashMap<IStructure, Boolean>());

	protected StructuredInstanceWithAlphas(IInstance ins,
			IStructure goldStruct, float C, L2LossSSVMDCDSolver solver) {
		this.goldStructure = goldStruct;
		this.ins = ins;
		this.goldFeatureVector = solver.featureGenerator.getFeatureVector(ins, goldStruct);	// 
		
		
		candidateStructureList = new ArrayList<IStructure>();
		alphaFeatureVectorList = new ArrayList<Pair<float[], IFeatureVector>>();
		sC = C;
		lock = new ReentrantLock();
		basedSolver = solver;
		
	}

	
	protected float getAlphaSum() {
		float sum_alpha = 0f;
		for (Pair<float[], IFeatureVector> p : alphaFeatureVectorList) {
			sum_alpha += p.getFirst()[0];
		}
		return sum_alpha;
	}
	static public long accumulateTime = 0;
	
	/*
	 * performs the update as mentioned in algorithm 1 in the paper.
	 */
	protected void solveSubProblemAndUpdateW(L2SolverInfo si, WeightVector wv) {
		float sum_alpha = getAlphaSum();
		// solve subproblem over alphas corresponding to an instance and update w.
		int i = 0;
		float stop;
		while(true){
			i++;
			float inner_PGmax_new = Float.NEGATIVE_INFINITY;
			float inner_PGmin_new = Float.POSITIVE_INFINITY;
			
			// this loop performs the update for each alpha separately
			for (Pair<float[], IFeatureVector> p : alphaFeatureVectorList){

				float alpha = p.getFirst()[0];
				float loss = p.getFirst()[1];

				IFeatureVector fv = p.getSecond();	// get the difference vector 
				float dot_product = wv.dotProduct(fv);
				float xij_norm2 = fv.getSquareL2Norm();	

				float NG = (loss - dot_product) - sum_alpha / (2.0f * sC);

				float PG = -NG;
				if (alpha == 0)
					PG = Math.min(-NG, 0);

				inner_PGmax_new = Math.max(inner_PGmax_new, PG);
				inner_PGmin_new = Math.min(inner_PGmin_new, PG);

				if (Math.abs(PG) > UPDATE_CONDITION) {

					float step = NG / (xij_norm2 + 1.0f / (2.0f * sC));	// line 3 in algorithm
					float new_alpha = Math.max(alpha + step, 0);
					sum_alpha += (new_alpha - alpha);
					long timeStart = System.currentTimeMillis();
					wv.addSparseFeatureVector(fv, (new_alpha - alpha));
					accumulateTime += System.currentTimeMillis()-timeStart;	
					
					float[] alpha_loss = p.getFirst();	// update alpha in the working set too
					alpha_loss[0] = new_alpha;
					p.setFirst(alpha_loss);
				}
			}

			stop = inner_PGmax_new - inner_PGmin_new;

			// satisfied inner stopping condition
			if (stop < DCD_INNNER_STOP || i >= MAX_DCD_INNNER_ITER){
				if(si != null){
					si.PGMaxNew = Math.max(si.PGMaxNew, inner_PGmax_new);
					si.PGMinNew = Math.min(si.PGMinNew, inner_PGmin_new);
				}
				break;
			}

		}
	}

	protected void cleanCache(WeightVector wv) {
		List<IStructure> removeSet = new ArrayList<IStructure>();
		for (int i = 0; i < candidateStructureList.size(); i++) {
			IStructure saved_h = candidateStructureList.get(i);

			// not in the workingSet
			if (alphaFeatureVectorList.get(i).getFirst()[0] <= 1e-6) 
			{
				removeSet.add(saved_h);
				// remove it if I haven't remove it
				/*if (!removedStructure.contains(saved_h)) { 
					removedStructure.add(saved_h);
					removeSet.add(saved_h);
				}*/
			}
		}
		candidateStructureList.removeAll(removeSet);
		alphaFeatureVectorList.removeAll(removeSet);
		/*
		for (IStructure remove_h : removeSet) {
			
			for (int i = 0; i < candidateStructureList.size(); i++) {
				if (remove_h == candidateStructureList.get(i)) {
					candidateStructureList.remove(i);
					alphaFeatureVectorList.remove(i);
					break;
				}
			}
		}*/

	}
	
	/**
	 * changes the working set, returns 1 if the instance is added, 0 otherwise
	 * @param wv
	 * @param infSolver
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	protected int updateRepresentationCollection(WeightVector wv,
			AbstractInferenceSolver infSolver, SLParameters parameters) throws Exception {

		float C = sC;
			
		IStructure h = infSolver
				.getLossAugmentedBestStructure(wv, ins, goldStructure);
		float loss = infSolver.getLoss(ins, goldStructure, h);
		

		IFeatureVector best_features = basedSolver.featureGenerator.getFeatureVector(ins, h);
		IFeatureVector diff = goldFeatureVector.difference(best_features);

		float xi = getAlphaSum() / (2.0f * C);
		float dotProduct = wv.dotProduct(diff);
		float score = (loss - dotProduct) - xi;	// line 6 in DCD-SSVM (algorithm 3 in paper)
		// implement a looser condition
		// System.out.println("cache size:" + alphafv_map.size());
		// if (!alphafv_map.containsKey(h)) {
		

		if(parameters.CHECK_INFERENCE_OPT) {
			// float check if the code is right
			float max_score_in_cache = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < candidateStructureList.size(); i++) {
				Pair<float[], IFeatureVector> tmp_alpha_loss = alphaFeatureVectorList.get(i);
			// IStructure f= structure_list.get(i);

			float s = tmp_alpha_loss.getFirst()[1]
					- wv.dotProduct(tmp_alpha_loss.getSecond()) - xi;
			if (max_score_in_cache < s)
				max_score_in_cache = s;
			}

			if (score < max_score_in_cache - 1e-4) {
				if(logger.isErrorEnabled()){
					printErrorLogForIncorrectInference(wv, loss, h, xi, dotProduct, score,
						max_score_in_cache);
				}
				throw new Exception(
					"The inference procedure is not correct! The max solution is worse than some of the solution in the cache! "
							+ "If you want to use atmp_alpha_lossn approximated inference. Check JLISParameter.check_inference_opt ");
			}
		}
		
		if (score < parameters.STOP_CONDITION) // not enough contribution
			return 0;
		if(!structuredSet.contains(h)){
			float[] alpha_loss = new float[2];
			alpha_loss[0] = 0.0f;
			alpha_loss[1] = loss;
			candidateStructureList.add(h);
			alphaFeatureVectorList.add(new Pair<float[], IFeatureVector>(alpha_loss, diff));
			structuredSet.add(h);
		}
		return 1;


	}

	private void printErrorLogForIncorrectInference(WeightVector wv, float loss, IStructure h,
			float xi, float dotProduct, float score,
			float max_score_in_cache) {
		logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		logger.error("The inference procedure is not correct!");
		logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		logger.error("Pred: " + h);
		logger.error("docproduct on pred: "
				+ basedSolver.featureGenerator.decisionValue(wv, ins, h)); 
		logger.error("dotproduct on diff: " + dotProduct);
		logger.error("loss: " + loss);
		logger.error("xi: " + xi);
		logger.error("Warning: the inference (argmax) code is not right......");
		logger.error("score: " + score);
		logger.error("max score in cache: " + max_score_in_cache);

		for (int i = 0; i < candidateStructureList.size(); i++) {
			IStructure f = candidateStructureList.get(i);
			Pair<float[], IFeatureVector> pair = alphaFeatureVectorList.get(i);
			logger.error(">>>" + f + " alpha: " + pair.getFirst()[0]
					+ " loss: " + pair.getFirst()[1]);
			logger.error(">>> (dot) "
					+ wv.dotProduct(pair.getSecond()));
		}
		logger.error("[GOLD]" + goldStructure);
		logger.error("gold dot product: "
				+ basedSolver.featureGenerator.decisionValue(wv, ins, goldStructure));
	}

	/**
	 * returns the sum of alphas, weighed by the corresponding losses (the last term of the dual objective in equation (4))
	 * @return
	 */
	protected float getLossWeightAlphaSum() {

		float sum_alpha = 0f;
		for (Pair<float[], IFeatureVector> p : alphaFeatureVectorList) {
			float[] alpha_loss = p.getFirst();
			sum_alpha += alpha_loss[0] * alpha_loss[1];
		}
		return sum_alpha;

	}

	public float getC() {
		return sC;
	}

	public Lock getLock() {
		return lock;
	}

	@Deprecated
	protected void fillWeightVector(WeightVector w) {
		for (Pair<float[], IFeatureVector> p : alphaFeatureVectorList) {
			float alpha = p.getFirst()[0];
			IFeatureVector fv = p.getSecond();			
			w.addSparseFeatureVector(fv, alpha);
		}
	}
	@Deprecated
	protected int getMaxIdx() {
		int max_idx = -1;
		for (Pair<float[], IFeatureVector> p : alphaFeatureVectorList) {
			IFeatureVector fv = p.getSecond();
			int curidx = fv.getMaxIdx();
			if (curidx > max_idx)
				max_idx = curidx;
		}
		return max_idx;
	}

}