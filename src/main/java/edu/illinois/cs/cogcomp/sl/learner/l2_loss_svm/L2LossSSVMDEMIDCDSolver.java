package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * A muti-core learning algorithm for structured SVM model.
 * 
 * <p>
 * Please see the following paper for details:
 * K.-W. Chang, V. Srikumar, D. Roth. Multi-core Structural SVM Training. ECML 2013.
 * 
 * @author Kai-Wei Chang
 * 
 */

public class L2LossSSVMDEMIDCDSolver extends L2LossSSVMDCDSolver {

	static Logger logger = LoggerFactory.getLogger(L2LossSSVMDEMIDCDSolver.class);
	Random rnd = new Random(0);
	boolean stop = false;
	int[] numNewStructFromInfThread;
	int learningIter  = 0;
	
	
	private WeightVector wvBuffer;
	
	AbstractInferenceSolver[] infSolvers;
	int numThreads = 0;
	
	// Constructors
	/**
	 * Constructor with an inference solver. The inference solver must implements a clone method
	 * when using the multi-thread algorithm. Otherwise, please specify an array of inference 
	 * solvers and use the following constructor:
	 * public L2LossSSVMDEMIDCDSolver(AbstractInferenceSolver[] infSolvers, int numThreads); 
	 * 
	 * @param infSolver
	 * @param numThreads
	 */
	public L2LossSSVMDEMIDCDSolver(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, int numThreads) {
		super(infSolver, fg);
		infSolvers = new AbstractInferenceSolver[numThreads-1];
		
		for(int i=0; i<numThreads-1; i++){
			infSolvers[i] = (AbstractInferenceSolver) infSolver.clone();
		}
		this.numThreads = numThreads;
	}
	
	/**
	 * Constructor with an array of inference solvers.
	 * @param infSolvers
	 * @param numThreads
	 */
	public L2LossSSVMDEMIDCDSolver(AbstractInferenceSolver[] infSolvers, AbstractFeatureGenerator fg,  int numThreads) {
		super(infSolvers[0],fg);
		this.infSolvers = infSolvers;
		this.numThreads = numThreads;
	}
	
	// Learning Thread
	class LearningThread extends Thread {
		WeightVector wv;
		List<StructuredInstanceWithAlphas> examples = new ArrayList<StructuredInstanceWithAlphas>();
		SLParameters parameters;
		int numInfThreads;
		
		public LearningThread(WeightVector wv, SLParameters para, List<StructuredInstanceWithAlphas> examples, int numInfThreads){
			this.wv = wv;
			this.parameters = para;
			this.examples = examples;
			this.numInfThreads = numInfThreads;
		}
		
		@Override
		public void run() {	
			int numUpdate = 0;
			learningIter = 0;
			stop = false;
			while(true){
				if(learningIter == parameters.MAX_NUM_ITER)
					break;
				if(numNewStructures() < Integer.MAX_VALUE){
					learningIter++;
				}
		//	for(; learningIter < parameters.MAX_NUM_ITER; learningIter++) {
				Collections.shuffle(examples);
				StructuredInstanceWithAlphas.L2SolverInfo si = new StructuredInstanceWithAlphas.L2SolverInfo(); // Inner stopping condition.
									
				for(StructuredInstanceWithAlphas ex : examples){
					ex.solveSubProblemAndUpdateW(si, wv);
					if(numUpdate % parameters.DEMIDCD_NUMBER_OF_UPDATES_BEFORE_UPDATE_BUFFER ==0){
						wvBuffer.setDenseVector(wv);
					}
					numUpdate++;
				}
				if (si.PGMaxNew - si.PGMinNew <= parameters.STOP_CONDITION) {
					if(numNewStructures() ==0){
						stop = true;
						logger.info("Done: Stopping condition: " + parameters.STOP_CONDITION + " projected gradient range: " + (si.PGMaxNew - si.PGMinNew));
						logger.info("total number of iteration " + learningIter);
						return;
					}
				}


				if (parameters.CLEAN_CACHE && learningIter % parameters.CLEAN_CACHE_ITER == 0) {
					logger.trace("Cleaning cache....");
					for(StructuredInstanceWithAlphas ex : examples) {
						ex.cleanCache(wv);
					}
				}
				
				if(logger.isTraceEnabled()){
					printTotalNumberofAlphas(examples.toArray(new StructuredInstanceWithAlphas[examples.size()]));
				}
				
				if((learningIter+1) % parameters.PROGRESS_REPORT_ITER == 0) {
					logger.info("Iteration: " + learningIter
							+ ": Add " + numNewStructures()
							+ " candidate structures into the working set.");
					logger.info("negative dual obj = " + -getDualObjective(
							examples.toArray(new StructuredInstanceWithAlphas[examples.size()]), wv));
					if(f!=null)
						try {
							f.run(wv, (AbstractInferenceSolver)infSolvers[0].clone());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				
			}
			
			stop = true;
			logger.info("Done: reach maximal number of iterations: "  + parameters.MAX_NUM_ITER);
			logger.info("total number of iteration " + learningIter);
			logger.info("negative dual obj = " + -getDualObjective(
					examples.toArray(new StructuredInstanceWithAlphas[examples.size()]), wv));	
		}
		

		private int numNewStructures() {
			int totalAddStruct = 0;
			for(int i = 0; i < numInfThreads; i++){
				if(numNewStructFromInfThread[i]==-1 || totalAddStruct == Integer.MAX_VALUE)
					totalAddStruct= Integer.MAX_VALUE;
				else
					totalAddStruct += numNewStructFromInfThread[i];
			}
			return totalAddStruct;
		}
	}
	// Each inference thread keeps finding new non-zero alpha
	class InferenceThread extends Thread {
		private AbstractInferenceSolver infSolver;
		private StructuredInstanceWithAlphas[] alphaInsList;
		private WeightVector wv;
		private int threadId;
		private int oldLearningIter = 0;
		private SLParameters parameters;

		public InferenceThread(
				AbstractInferenceSolver infSolver,
				StructuredInstanceWithAlphas[] subset, WeightVector wv, int threadId, SLParameters parameters) {
			this.infSolver = infSolver;
			this.alphaInsList = subset;
			this.threadId = threadId;
			this.wv = new WeightVector(wv);
			this.parameters = parameters;
			logger.trace("Thread:" + threadId + " handles "
						+ subset.length + " instances!");
		}

		@Override
		public void run() {
			int numInstanceParsed = 0;
			int numNewStruct = 0;
			while(!stop){
				if(oldLearningIter!= learningIter){
					numNewStruct = 0;
					oldLearningIter = learningIter;
				}
				
				for (StructuredInstanceWithAlphas ex : alphaInsList) {
					// positive h has already been fixed
					try {
						numNewStruct += ex.updateRepresentationCollection(wv, infSolver, parameters);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					if(numInstanceParsed % parameters.DEMIDCD_NUMBER_OF_INF_PARSE_BEFORE_UPDATE_WV ==0){
						wv.setDenseVector(wvBuffer);
						if(stop){
							logger.trace("Inference thread stops");
							return;
						}
					}
					numInstanceParsed++;
				}
				logger.trace("Thread: (b,s) udpate");
				numNewStructFromInfThread[threadId] = numNewStruct;
			}
			logger.trace("Inference thread stops");
		}
	}

	
	private WeightVector trainSSvm (
			final AbstractInferenceSolver[] infSolvers,
			WeightVector initWv, SLProblem sp, SLParameters parameters) throws Exception {
		
		WeightVector wv = new WeightVector(initWv);
		wvBuffer = new WeightVector(wv);
	
		int numThreads = parameters.NUMBER_OF_THREADS;
		stop = false;
		List<SLProblem>  subProbs= sp.splitData(numThreads-1);
		List<StructuredInstanceWithAlphas> allIns = new ArrayList<StructuredInstanceWithAlphas>();
		InferenceThread[] infRunnerList = new InferenceThread[numThreads-1];
		for(int i=0 ;i <numThreads-1; i++){
			StructuredInstanceWithAlphas[] alphaInsList = initArrayOfInstances(subProbs.get(i), parameters.C_FOR_STRUCTURE,  subProbs.get(i).size());
			infRunnerList[i] = new InferenceThread(infSolvers[i], alphaInsList, wv, i, parameters);
			allIns.addAll(Arrays.asList(alphaInsList));
		}

		LearningThread learner = new LearningThread(wv, parameters, allIns, numThreads-1);

		numNewStructFromInfThread = new int[numThreads-1];
		// run the threads
		for (int i = 0; i < numThreads-1; i++) {
			infRunnerList[i].start();
			numNewStructFromInfThread[i] = -1;
		}
		learner.start();

		// wait until all of them are finished
		for (int i = 0; i < numThreads-1; i++) {
			infRunnerList[i].join();
		}
		learner.join();
		
		try{
			double obj = getDualObjective(allIns.toArray(new StructuredInstanceWithAlphas[allIns.size()]), wv);
			logger.info("Finish! The negative dual value is " + -obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return learner.wv;
	}
	
	/**
	 * The parallel version of
	 * {@link L2LossSSVMDCDSolver#trainStructuredSVM(AbstractLossAugmentedInferenceSolver, SLProblem, SLParameters)}
	 * <p>
	 * 
	 * The only difference now is that we need to create more than one inference
	 * solver. The number of the inference solvers will determine how many
	 * threads we will use.
	 * 
	 * @param struct_finder_list
	 *            The list of the inference solvers. It determines how many
	 *            threads the learner will use.
	 * @param sp
	 * @param para
	 * @return
	 * @throws Exception
	 */

	
	@Override
	public WeightVector train(final SLProblem sp, SLParameters parameters) throws Exception{ 
		WeightVector wv = new WeightVector(10000);
		
		if (parameters.TRAINMINI && 5 * parameters.TRAINMINI_SIZE < sp.size()) {
			int t_size = parameters.TRAINMINI_SIZE;
			logger.info("Train a mini sp to speed up! size = " + t_size);
			SLProblem minisp = new SLProblem();
			minisp.instanceList = new ArrayList<IInstance>();
			minisp.goldStructureList = new ArrayList<IStructure>();
			ArrayList<Integer> index_list = new ArrayList<Integer>();
			for (int i = 0; i < sp.size(); i++)
				index_list.add(i);
			Collections.shuffle(index_list, new Random(0));

			for (int i = 0; i < t_size; i++) {
				int idx = index_list.get(i);
				minisp.instanceList.add(sp.instanceList.get(idx));
				minisp.goldStructureList.add(sp.goldStructureList.get(idx));
			}

			wv = trainSSvm(infSolvers, wv,
					minisp, parameters);
		}
		//parameters.CHECK_INFERENCE_OPT = false;

		return trainSSvm(infSolvers, wv, sp,
				parameters);
	}
}
