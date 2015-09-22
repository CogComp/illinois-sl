package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected final static float UPDATE_CONDITION = 1e-8f;

	protected IInstance ins = null;
	protected float sC = 0.0f;
	float alphaSum;

	public static class L2SolverInfo {
		public float PGMaxNew = Float.NEGATIVE_INFINITY;
		public float PGMinNew = Float.POSITIVE_INFINITY;
	}

	public static class AlphaStruct{
		float alpha;
		float loss;
		IFeatureVector alphaFeactureVector;
		IStructure struct;
	}

	protected L2LossSSVMDCDSolver basedSolver = null;
	protected IStructure goldStructure;
	protected IFeatureVector goldFeatureVector;
	public List<AlphaStruct> candidateAlphas;
	// the following two arrays are designed for avoiding ConcurrentModificationException
	// when using DEMI-DCD.
	
	public List<AlphaStruct> newCandidateAlphas;
	protected Set<IStructure> candidateSet = Collections.newSetFromMap(new HashMap<IStructure, Boolean>());

	protected StructuredInstanceWithAlphas(IInstance ins,
			IStructure goldStruct, float C, L2LossSSVMDCDSolver solver) {
		this.goldStructure = goldStruct;
		this.ins = ins;
		this.goldFeatureVector = solver.featureGenerator.getFeatureVector(ins, goldStruct);	// 
		candidateAlphas = new ArrayList<AlphaStruct>();
		newCandidateAlphas = Collections.synchronizedList(new ArrayList<AlphaStruct>());
		sC = C;
		basedSolver = solver;
	}

	/*
	 * Update an alpha element and w.
	 */
	protected void solveSubProblemAndUpdateW(L2SolverInfo si, WeightVector wv) {
		// solve sub-problem over alphas associated with an instance.
		int i = 0;
		float stop;
		candidateAlphas.addAll(newCandidateAlphas);
		newCandidateAlphas.clear();
		while(true){
			i++;
			float inner_PGmax_new = Float.NEGATIVE_INFINITY;
			float inner_PGmin_new = Float.POSITIVE_INFINITY;

			// this loop performs the update for each alpha separately
			for(AlphaStruct as: candidateAlphas){
				float alpha = as.alpha;
				float loss = as.loss;

				IFeatureVector fv = as.alphaFeactureVector;	// get the difference vector 
				float dot_product = wv.dotProduct(fv);
				float xij_norm2 = fv.getSquareL2Norm();	

				float NG = (loss - dot_product) - alphaSum / (2.0f * sC);

				float PG = -NG;	// projected gradient
				if (alpha == 0)
					PG = Math.min(-NG, 0);

				inner_PGmax_new = Math.max(inner_PGmax_new, PG);
				inner_PGmin_new = Math.min(inner_PGmin_new, PG);

				if (Math.abs(PG) > UPDATE_CONDITION) {

					float step = NG / (xij_norm2 + 1.0f / (2.0f * sC));
					float new_alpha = Math.max(alpha + step, 0);
					alphaSum += (new_alpha - alpha);
					wv.addSparseFeatureVector(fv, (new_alpha - alpha));
					as.alpha = new_alpha;
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

	protected  void cleanCache(WeightVector wv) {
		Iterator<AlphaStruct> iterator = candidateAlphas.iterator();
		while(iterator.hasNext()){
			AlphaStruct as = iterator.next();
			if(as.alpha <=1e-8){
				iterator.remove();
				candidateSet.remove(as.struct);
				
			}
		}
	}

	/**
	 * changes the working set, returns 1 if the instance is added, 0 otherwise
	 * @param wv
	 * synchronized@param infSolver
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	protected int updateRepresentationCollection(WeightVector wv,
			AbstractInferenceSolver infSolver, SLParameters parameters) throws Exception {

		float C = sC;

		IStructure h = infSolver
				.getLossAugmentedBestStructure(wv, ins, goldStructure);
		// already in candidateSet
		if(candidateSet.contains(h))
			return 0;
		
		float loss = infSolver.getLoss(ins, goldStructure, h);


		IFeatureVector best_features = basedSolver.featureGenerator.getFeatureVector(ins, h);
		IFeatureVector diff = goldFeatureVector.difference(best_features);

		float xi = alphaSum / (2.0f * C);
		float dotProduct = wv.dotProduct(diff);
		float score = (loss - dotProduct) - xi;	// line 12 in DCD-SSVM (algorithm 3 in paper)

		if(parameters.CHECK_INFERENCE_OPT) {
			float max_score_in_cache = Float.NEGATIVE_INFINITY;
			for(AlphaStruct as:new ArrayList<AlphaStruct>(candidateAlphas)){
				if(as !=null){
					float s = as.loss - wv.dotProduct(as.alphaFeactureVector) - xi;
					if (max_score_in_cache < s)
						max_score_in_cache = s;
				}
			}

			if (score < max_score_in_cache - 1e-4) {
				if(logger.isErrorEnabled()){
					printErrorLogForIncorrectInference(wv, loss, h, xi, dotProduct, score,
							max_score_in_cache);
				}
				throw new Exception(
						"The inference procedure obtains a sub-optimal solution!"
								+ "If you want to use an approximate inference solver, set SLParameter.check_inference_opt = false.");
			}
		}

		if (score < parameters.STOP_CONDITION) // not enough contribution
			return 0;
		
		AlphaStruct as = new AlphaStruct();
		as.alpha = 0.0f;
		as.loss = loss;
		as.alphaFeactureVector = diff;
		as.struct = h;
		newCandidateAlphas.add(as);
		candidateSet.add(h);
		return 1;
	}

	private void printErrorLogForIncorrectInference(WeightVector wv, float loss, IStructure h,
			float xi, float dotProduct, float score,
			float max_score_in_cache) {
		logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		logger.error("The inference procedure finds a sub-optimal solution.");
		logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		logger.error("If you want to use an approximate inference solver, set SLParameter.check_inference_opt = false.");
		
		logger.error("score: " + score);
		logger.error("max score of the cached structure: " + max_score_in_cache);
	}

	/**
	 * returns the sum of alphas, weighed by the corresponding losses (the last term of the dual objective in equation (4))
	 * @return
	 */
	protected float getLossWeightAlphaSum() {
		float sum_alpha = 0f;
		for(AlphaStruct as: candidateAlphas){
			sum_alpha += as.loss*as.alpha;
		}
		return sum_alpha;

	}

	public float getC() {
		return sC;
	}


	@Deprecated
	protected void fillWeightVector(WeightVector w) {
		for(AlphaStruct as: candidateAlphas){			
			w.addSparseFeatureVector(as.alphaFeactureVector, as.alpha);
		}
	}
	@Deprecated
	protected int getMaxIdx() {
		int max_idx = -1;
		for(AlphaStruct as: candidateAlphas){
			int curidx = as.alphaFeactureVector.getMaxIdx();
			if (curidx > max_idx)
				max_idx = curidx;
		}
		return max_idx;
	}

}