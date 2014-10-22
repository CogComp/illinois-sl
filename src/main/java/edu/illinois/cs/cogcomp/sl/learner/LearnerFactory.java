package edu.illinois.cs.cogcomp.sl.learner;

import java.security.InvalidParameterException;

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.learner.structured_percpetron.StructuredPerceptron;

/**
 * This class generate a learner from {@link SLParameters}. 
 * @author Kai-Wei Chang
 *
 */
public class LearnerFactory {
	protected AbstractInferenceSolver infSolver = null;
	protected SLParameters parameters = null;
	
	/**
	 * generate a learner using an inference solver and parameters.
	 * 
	 * @param infSolver
	 * @param parameters
	 */
	public static Learner getLearner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters){
		
		if(parameters.LEARNING_MODEL == SLParameters.LearningModelType.L2LossSSVM)
			return new L2LossSSVMLearner(parameters.L2_LOSS_SSVM_SOLVER_TYPE, infSolver, fg, parameters);
		else if(parameters.LEARNING_MODEL == SLParameters.LearningModelType.StructuredPerceptron)
			return new StructuredPerceptron(infSolver, fg, parameters);
		else
			throw new InvalidParameterException("parameters.LEARNING_MODEL does not support" + parameters.LEARNING_MODEL.name());
	
	}
}