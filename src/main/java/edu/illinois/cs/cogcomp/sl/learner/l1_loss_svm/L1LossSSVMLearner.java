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
package edu.illinois.cs.cogcomp.sl.learner.l1_loss_svm;

import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
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
public class L1LossSSVMLearner extends Learner {
	interface IL1LossSSVMSolver{ 
		public WeightVector train(SLProblem sp, SLParameters parameters) throws Exception;
		public WeightVector train(SLProblem sp, SLParameters parameters, WeightVector init) throws Exception;
		public void runWhenReportingProgress(ProgressReportFunction f);
	}
	
	public static enum SolverType {SGDSolver};
	SolverType solverType = null;
	IL1LossSSVMSolver solver = null;
	
	@Override
	public void runWhenReportingProgress(ProgressReportFunction f) {
		solver.runWhenReportingProgress(f); 
	}
	
	public L1LossSSVMLearner(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters){
		this(SolverType.SGDSolver, infSolver, fg, parameters);
	}
	public L1LossSSVMLearner(SolverType type, AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters){
		super(infSolver, fg, parameters);
		solverType = type;
		switch(solverType){
			case SGDSolver:
				solver = new L1LossSSVMSGDSolver(infSolver,fg);
				break;
		}
	}
	
	public L1LossSSVMLearner(SolverType type, AbstractInferenceSolver[] infSolvers, AbstractFeatureGenerator fg, SLParameters parameters){
		super(infSolvers[0], fg, parameters);
		solverType = type;
		switch(solverType){
			case SGDSolver:
				solver = new L1LossSSVMSGDSolver(infSolver,fg);
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
			obj += sC * loss ;
		}
		return obj;
	}

	@Override
	public WeightVector train(SLProblem sp) throws Exception {
		return solver.train(sp, parameters);
	}

	@Override
	public WeightVector train(SLProblem sp, WeightVector init) throws Exception {
		return solver.train(sp, parameters,init);
	}
}
