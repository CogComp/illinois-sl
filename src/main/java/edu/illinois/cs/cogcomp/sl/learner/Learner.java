package edu.illinois.cs.cogcomp.sl.learner;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * The interface for a Structured SVM learner
 * @author Ming-Wei Chang
 *
 */
public abstract class Learner {
	protected AbstractInferenceSolver infSolver = null;
	protected SLParameters parameters = null;
	protected AbstractFeatureGenerator featureGenerator;
	
	protected ProgressReportFunction f;

	
	public static interface ProgressReportFunction {
		void run(WeightVector w, AbstractInferenceSolver inference)
				throws Exception;
	}
	
	
	/**
	 * Construct a learner by giving an inference solver and a parameter set.
	 * 
	 * @param infSolver
	 * @param parameters
	 */
	public Learner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg,  SLParameters parameters){
		this.setParameters(parameters);
		this.infSolver = infSolver;
		this.featureGenerator = fg;
	}
	
	/**
	 * Construct a learner by giving an inference solver and use a default 
	 * parameter set
	 * 
	 * @param infSolver
	 */
	public Learner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg){
		this(infSolver,  fg, new SLParameters());
	}
	
	/**
	 * A function which should be run at the end of every epoch. 
	 * Useful to see the progress made by the training.
	 * @param f
	 */
	public void runWhenReportingProgress(ProgressReportFunction f) {
		this.f = f;
	}
	
	/**
	 * The function for the users to call for the structured SVM
	 * 
	 * @param sp
	 *            {@link SLProblem}: the labeled data.
	 * @return {@link WeightVector}: trained weight vector.
	 * @throws Exception
	 */
	abstract public WeightVector train(final SLProblem sp) throws Exception;
	
	
	/*Getter and Setter Function */
	public SLParameters getParameters() {
		return parameters;
	}
	public void setParameters(SLParameters parameters) {
		this.parameters = parameters;
	}
}