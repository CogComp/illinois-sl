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
	
	abstract public WeightVector train(final SLProblem sp, WeightVector init) throws Exception;
	
	/*Getter and Setter Function */
	public SLParameters getParameters() {
		return parameters;
	}
	public void setParameters(SLParameters parameters) {
		this.parameters = parameters;
	}
}