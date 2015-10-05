/*******************************************************************************
 * University of Illinois/NCSA Open Source License
 * Copyright (c) 2010, 
 *
 * Developed by:
 * The Cognitive Computations Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
 * Neither the names of the Cognitive Computations Group, nor the University of Illinois at Urbana-Champaign, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *     
 *******************************************************************************/
package edu.illinois.cs.cogcomp.sl.learner;

import java.security.InvalidParameterException;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.learner.structured_perceptron.StructuredPerceptronIPM;
import edu.illinois.cs.cogcomp.sl.learner.structured_perceptron.StructuredPerceptron;

/**
 * This class generate a learner from {@link SLParameters}.
 * 
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
	public static Learner getLearner(AbstractInferenceSolver infSolver,
			AbstractFeatureGenerator fg, SLParameters parameters) {

		if (parameters.LEARNING_MODEL == SLParameters.LearningModelType.L2LossSSVM)
			return new L2LossSSVMLearner(parameters.L2_LOSS_SSVM_SOLVER_TYPE,
					infSolver, fg, parameters);
		else if (parameters.LEARNING_MODEL == SLParameters.LearningModelType.StructuredPerceptron)
			return new StructuredPerceptron(infSolver, fg, parameters);
		else if (parameters.LEARNING_MODEL == SLParameters.LearningModelType.StructuredPerceptronIPM) {
			System.out.println("Using IPM Perceptron ....");
			return new StructuredPerceptronIPM(infSolver, fg, parameters,
					parameters.NUMBER_OF_THREADS);
		} else
			throw new InvalidParameterException(
					"parameters.LEARNING_MODEL does not support"
							+ parameters.LEARNING_MODEL.name());

	}
}