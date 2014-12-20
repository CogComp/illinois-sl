package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner.ProgressReportFunction;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner.IL2LossSSVMSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * A dual coordinate descent solver for Structured SVM.
 * Please see the following papers for details:
 * Ming-Wei Chang and Wen-tau Yih, Dual Coordinate Descent Algorithms for 
 * Efficient Large Margin Structured Prediction, TACL, 2013.
 * 
 * Ming-Wei Chang, Vivek Srikumar, Dan Goldwasser and Dan Roth. Structured 
 * output learning with indirect supervision. ICML, 2010.
 * 
 * @author Ming-Wei Chang
 * 
 */
public class L2LossSSVMDCDSolver implements IL2LossSSVMSolver{

	static Logger logger = LoggerFactory.getLogger(L2LossSSVMDCDSolver.class);
	static Random random = new Random(0);
	
	AbstractInferenceSolver infSolver; 
	
	AbstractFeatureGenerator featureGenerator;
	
	
	protected static final SLProblem emptyStructuredProblem;

	static {
		emptyStructuredProblem = new SLProblem();
		emptyStructuredProblem.instanceList = new ArrayList<IInstance>();
		emptyStructuredProblem.goldStructureList = new ArrayList<IStructure>();
	}
	
	protected ProgressReportFunction f;
	
	public void runWhenReportingProgress(ProgressReportFunction f) {
		this.f = f;
	}
	
	public L2LossSSVMDCDSolver(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg){
		this.featureGenerator = fg;
		this.infSolver = infSolver;
	}
	
	/**
	 * The function for the users to call for the structured SVM
	 * 
	 * @param infSolver
	 *            The inference solver (dynamic programming, ILP,...). Given an
	 *            input (IInstance) and a Weight vector (WeightVector), return
	 *            the best structure (AbstractStructures)
	 * @param sp
	 *            Structured Labeled Dataset
	 * @param parameters
	 *            parameters for JLIS
	 * @return
	 * @throws Exception
	 */
	@Override
	public WeightVector train(final SLProblem sp, SLParameters params) throws Exception {
		WeightVector wv = null;
		
		// +1 because wv.u[0] stores the bias term
		if(params.TOTAL_NUMBER_FEATURE >0){
			wv = new WeightVector(params.TOTAL_NUMBER_FEATURE + 1);
			wv.setExtendable(false);
		} else {
			wv = new WeightVector(8192);
			wv.setExtendable(true);
		}
		return DCDForL2LossSSVM(wv, infSolver, sp, params);
	}
	

	protected WeightVector DCDForL2LossSSVM(WeightVector oldWv,
			final AbstractInferenceSolver infSolver, SLProblem sp,
			SLParameters parameters) throws Exception {
		int size = sp.size();
		float dualObj = 0;

		WeightVector wv = new WeightVector(oldWv);

		StructuredInstanceWithAlphas[] alphaInsList = initArrayOfInstances(sp,
				parameters.C_FOR_STRUCTURE, size);


		boolean finished = false;
		boolean resolved = false;

		// train the inner loop

		resolved = false;
		finished = false;

		for(int iter=0; iter < parameters.MAX_NUM_ITER; iter++){
			int NumOfNewStructures = updateWorkingSet(
					alphaInsList, wv, infSolver, parameters);

			// no more update is necessary, exit the internal loop
			if (NumOfNewStructures == 0) {
				if (finished == false)
					resolved = true;
				else{
					logger.info("Met the stopping condition; Exit Inner loop");
					logger.info("negative dual obj = " + dualObj);
					break;
				}
			}

			// update weight vector and alphas based on the working set
			Pair<Float, Boolean> res = updateWvWithWorkingSet(
					alphaInsList, wv,
					parameters);
			
			if(iter % parameters.PROGRESS_REPORT_ITER == 0) {
				logger.info("Iteration: " + iter
						+ ": Add " + NumOfNewStructures
						+ " candidate structures into the working set.");
				logger.info("negative dual obj = " + res.getFirst());
				if(f!=null)
					f.run(wv, infSolver);
			}
			
			if (resolved) {
				finished = true;
				logger.info("(Resolved) Met the stopping condition; Exit Inner loop");
				logger.info("negative dual obj = " + res.getFirst());
				break;
			} else {
				finished = res.getSecond();
				dualObj = res.getFirst();
			}
			
			if(logger.isTraceEnabled()){
				printTotalNumberofAlphas(alphaInsList);
			}
			
			// remove unused candidate structures from working set
			if (parameters.CLEAN_CACHE && (iter+1) % parameters.CLEAN_CACHE_ITER == 0) {
				for (int i = 0; i < size; i++) {
					alphaInsList[i].cleanCache(wv);
				}
				if (logger.isInfoEnabled()) {
					logger.info("Cleaning cache....");	
					printTotalNumberofAlphas(alphaInsList);
				}
			}
			
		}
		return wv;
	}
	
	// this code runs line 3 to 8 from algorithm 3 (DCD-SSVM) in the paper
	protected static Pair<Float, Boolean> updateWvWithWorkingSet(
			StructuredInstanceWithAlphas[] alphaInsList, WeightVector wv,
			SLParameters parameters) {
		// initialize w: w = sum alpha_i x_i
		int numIns = alphaInsList.length;

		logger.trace("STOPPING criteria:" + parameters.INNER_STOP_CONDITION);
		// coordinate descent

		List<Integer> indices = new ArrayList<Integer>();

		for (int i = 0; i < numIns; i++)
			indices.add(i);

		int t = 0;
		
		boolean finished = false;

		for (t = 0; t < parameters.MAX_ITER_INNER; t++) {

			StructuredInstanceWithAlphas.L2SolverInfo si = new StructuredInstanceWithAlphas.L2SolverInfo();

			// shuffle the indices
			Collections.shuffle(indices, random);

			// coordinate descent
			for (int idx : indices) {
				alphaInsList[idx].solveSubProblemAndUpdateW(si, wv);
			}

			if (si.PGMaxNew - si.PGMinNew <= parameters.INNER_STOP_CONDITION) {
				finished = true;
				break;
			}
		}

		float obj = getDualObjective(alphaInsList, wv);
		obj = -obj;
		return new Pair<Float, Boolean>(obj, finished);
	}
	
	/**
	 * returns number of newly added items in the working set
	 * @param alphaInsList
	 * @param wv
	 * @param infSolver
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	private int updateWorkingSet(
			StructuredInstanceWithAlphas[] alphaInsList, WeightVector wv,
			AbstractInferenceSolver infSolver, SLParameters parameters)
					throws Exception {
		int numNewStructures = 0;

		for (int i = 0; i < alphaInsList.length; i++) {
			float score = alphaInsList[i].updateRepresentationCollection(
					wv, infSolver, parameters);
			alphaInsList[i].solveSubProblemAndUpdateW(null, wv);
			if (score > parameters.STOP_CONDITION)	// 
				numNewStructures += 1;	
		}
		return numNewStructures;
	}
	
	// returns the objective as written in equation (4) in the paper
	protected static float getDualObjective(
			StructuredInstanceWithAlphas[] alphaInsList, WeightVector wv) {
		float obj = 0;

		obj += wv.getSquareL2Norm() * 0.5;

		for (int i = 0; i < alphaInsList.length; i++) {
			StructuredInstanceWithAlphas instanceWithAlphas = alphaInsList[i];
			float w_sum = instanceWithAlphas.getLossWeightAlphaSum();
			float sum = instanceWithAlphas.alphaSum;
			float C = instanceWithAlphas.getC();
			obj -= w_sum;
			obj += (1.0 / (4.0 * C)) * sum * sum;
		}
		return obj;
	}
	
	
	protected static void printTotalNumberofAlphas(
			StructuredInstanceWithAlphas[] alphaInsList) {
		int n_total_alphas = 0;
		int n_ex = alphaInsList.length;
		for (int i = 0; i < n_ex; i++) {
			StructuredInstanceWithAlphas alphaIns = alphaInsList[i];
			//n_total_alphas += alphaIns.alphaFeatureVectorList.size();
			n_total_alphas += alphaIns.candidateAlphas.size();
		}

		logger.trace("Number of ex: " + alphaInsList.length);
		logger.trace("Number of alphas: " + n_total_alphas);
	}


	protected StructuredInstanceWithAlphas[] initArrayOfInstances(
			SLProblem sp, final float CStructure,
			int size) {
		// create the dual variables for each example
		StructuredInstanceWithAlphas[] alphInsList = new StructuredInstanceWithAlphas[size];

		// initialization: structure
		if (sp.instanceWeightList == null) {
			for (int i = 0; i < sp.size(); i++) {
				alphInsList[i] = new StructuredInstanceWithAlphas(
						sp.instanceList.get(i), sp.goldStructureList.get(i),
						CStructure,this);
			}
		} else {
			for (int i = 0; i < sp.size(); i++) {
				alphInsList[i] = new StructuredInstanceWithAlphas(
						sp.instanceList.get(i), sp.goldStructureList.get(i),
						CStructure * sp.instanceWeightList.get(i),this);
			}
		}
		return alphInsList;
	}

	
	@Deprecated
	public static WeightVector getWeightVectorBySumAlpahFv(
			StructuredInstanceWithAlphas[] alphaInsList, boolean isExtendable,
			int numIns) {
		int numFeatures = -1;

		for (int i = 0; i < numIns; i++) {
			int currentMaxIdx = alphaInsList[i].getMaxIdx();
			if (currentMaxIdx > numFeatures)
				numFeatures = currentMaxIdx;
		}

		logger.info("number of features: " + numFeatures);

		WeightVector currentWv = new WeightVector(numFeatures + 1);
		currentWv.setExtendable(isExtendable);
		// float[] cur_w = new float[max_n + 1];
		for (int i = 0; i < numIns; i++) {
			alphaInsList[i].fillWeightVector(currentWv);
		}
		return currentWv;
	}

}
