package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;

import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 *  A delegator class. The class serves as an interface to train a 
 *  Structured SVM model. It provides three algorithms to train the model
 *  
 * 
 * @author Kai-Wei Chang
 * 
 */
// the delegator
public class L2LossSSVMLearner extends Learner {
	interface IL2LossSSVMSolver{ 
		public WeightVector train(SLProblem sp, SLParameters parameters) throws Exception;
		public void runWhenReportingProgress(ProgressReportFunction f);
	}
	
	public static enum SolverType {DCDSolver, ParallelDCDSolver, DEMIParallelDCDSolver};
	SolverType solverType = null;
	IL2LossSSVMSolver solver = null;
	
	@Override
	public void runWhenReportingProgress(ProgressReportFunction f) {
		solver.runWhenReportingProgress(f); 
	}
	
	public L2LossSSVMLearner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters){
		this(SolverType.DCDSolver, infSolver, fg, parameters);
	}
	public L2LossSSVMLearner(SolverType type, AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters){
		super(infSolver, fg, parameters);
		solverType = type;
		switch(solverType){
			case DCDSolver:
				solver = new L2LossSSVMDCDSolver(infSolver,fg);
				break;
			case ParallelDCDSolver:
				solver = new L2LossSSVMParalleDCDSolver(infSolver, fg, parameters.NUMBER_OF_THREADS);
				break;
			case DEMIParallelDCDSolver:
				solver = new L2LossSSVMDEMIDCDSolver(infSolver, fg, parameters.NUMBER_OF_THREADS);
				break;
		}
	}
	
	public L2LossSSVMLearner(SolverType type, AbstractInferenceSolver[] infSolvers, AbstractFeatureGenerator fg, SLParameters parameters){
		super(infSolvers[0], fg, parameters);
		solverType = type;
		switch(solverType){
			case DCDSolver:
				solver = new L2LossSSVMDCDSolver(infSolvers[0], fg);
				break;
			case ParallelDCDSolver:
				solver = new L2LossSSVMParalleDCDSolver(infSolvers, fg, parameters.NUMBER_OF_THREADS);
				break;
			case DEMIParallelDCDSolver:
				solver = new L2LossSSVMDEMIDCDSolver(infSolvers, fg, parameters.NUMBER_OF_THREADS);
				break;
		}
	}
	
	/**
	 * Get primal objective function value with respect to the weight vector wv
	 * @param sp
	 * @param wv
	 * @param infSolver
	 * @param C
	 * @return
	 * @throws Exception
	 */
	public float getPrimalObjective(
			SLProblem sp, WeightVector wv,
			AbstractInferenceSolver infSolver, float C) throws Exception {
		float obj = 0;

		obj += wv.getSquareL2Norm() * 0.5;
		List<IInstance> input_list = sp.instanceList;
		List<IStructure> output_list = sp.goldStructureList;
		for (int i = 0; i < input_list.size(); i++) {
			IInstance ins = input_list.get(i);
			IStructure gold_struct = output_list.get(i);
			float sC= C;
			IStructure h = infSolver
					.getLossAugmentedBestStructure(wv, ins, gold_struct);
			float loss = infSolver.getLoss(ins, gold_struct, h)
					+ this.featureGenerator.decisionValue(wv, ins, h)
					- this.featureGenerator.decisionValue(wv, ins, gold_struct);
			obj += sC * loss * loss;
		}
		return obj;
	}

	@Override
	public WeightVector train(SLProblem sp) throws Exception {
		return solver.train(sp, parameters);
	}
}
