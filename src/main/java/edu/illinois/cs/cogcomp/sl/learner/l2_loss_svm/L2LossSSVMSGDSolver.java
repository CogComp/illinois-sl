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
package edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm;


import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner.IL2LossSSVMSolver;
import edu.illinois.cs.cogcomp.sl.learner.Learner.ProgressReportFunction;

/*
 * @author Kai-Wei Chang
 * 
 */
public class L2LossSSVMSGDSolver implements IL2LossSSVMSolver{

	private static Logger log = LoggerFactory.getLogger(L2LossSSVMSGDSolver.class);

	public static Random random = new Random();

	protected AbstractInferenceSolver inference;

	protected AbstractFeatureGenerator featureGenerator;

	protected int epochUpdateCount;

	/**
	 * @param infSolver 
	 * @param featureGenerator
	 */
	public L2LossSSVMSGDSolver(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg){
		this.featureGenerator = fg;
		this.inference = infSolver;
	}

	/**
	 * To train with the default choice(zero vector) of initial weight vector. 
	 * Often this suffices.
	 * @param problem	The structured problem on which the perceptron should be trained
	 * @return w	The weight vector learnt from the training
	 * @throws Exception
	 */
	@Override
	public WeightVector train(SLProblem problem, SLParameters params) throws Exception {

		WeightVector init = new WeightVector(10000);

		return train(problem, params, init);
	}
	/**
	 * To train with a custom (possibly non-zero) initial weight vector
	 * @param problem	The structured problem on which the perceptron should be trained
	 * @param init	The initial weightvector to be used
	 * @return w	The weight vector learnt from the training data	
	 * @throws Exception
	 */
	@Override
	public WeightVector train(SLProblem problem, SLParameters params, WeightVector init)
	throws Exception {

	log.info("Starting Structured Perceptron learner");

	long start = System.currentTimeMillis();

	WeightVector w = init;

	int epoch = 0;
	boolean done = false;

	int count = 1;

	while (!done) {

		if (epoch % params.PROGRESS_REPORT_ITER == 0) {
			log.info("Starting epoch {}", epoch);
			if(f!=null)
				f.run(w, this.inference);
		}

		count = doOneIteration(w,  problem, epoch, count, params);

		if (epoch % params.PROGRESS_REPORT_ITER == 0){
			log.info("End of epoch {}. {} updates made", epoch,
					epochUpdateCount);
		}

		epoch++;
		done = !reachedStoppingCriterion(w, epoch, params);
		if (params.PROGRESS_REPORT_ITER > 0 && (epoch+1) % params.PROGRESS_REPORT_ITER == 0 && this.f != null)
			f.run(w, inference);

	}

	long end = System.currentTimeMillis();

	log.info("Learning complete. Took {}s", "" + (end - start) * 1.0
			/ 1000);

	return w;

	}

	/**
	 * Checks if stopping criterion has been met. We will stop if either no mistakes were made 
	 * during this iteration, or the maximum number of passes over the training data has been made.
	 * @param w
	 * @param epoch
	 * @return
	 */
	protected boolean reachedStoppingCriterion(WeightVector w, int epoch, SLParameters params) {

		if (epochUpdateCount == 0) {
			log.info("No errors made. Stopping outer loop because learning is complete!");
			return false;
		}
		return epoch < params.MAX_NUM_ITER;
	}
	/**
	 * Performs one pass over the entire training data.
	 * @param w		The weight vector to use during this iteration
	 * @param problem
	 * @param epoch
	 * @param count
	 * @param params
	 * @return
	 * @throws Exception
	 */
	protected int doOneIteration(WeightVector w,
			SLProblem problem, int epoch, int count, SLParameters params) throws Exception {
		int numExamples = problem.size();

		epochUpdateCount = 0;
		problem.shuffle(random); // shuffle your training data after every iteration

		for (int exampleId = 0; exampleId < numExamples; exampleId++) {
			IInstance example = problem.instanceList.get(exampleId);	// the input "x"
			IStructure gold = problem.goldStructureList.get(exampleId);	// the gold output structure "y"

			IStructure prediction = null;
			double loss = 0;
			prediction = this.inference.getLossAugmentedBestStructure(w, example, gold);	// the predicted structure
			loss = this.inference.getLoss(example, gold, prediction);	// we will update if the loss is non-zero for this example

			assert prediction != null;

			IFeatureVector goldFeatures = featureGenerator.getFeatureVector(example, gold); 
			IFeatureVector predictedFeatures = featureGenerator.getFeatureVector(example, prediction);
			IFeatureVector update = goldFeatures.difference(predictedFeatures);
			double loss_term = loss - w.dotProduct(update);

			double learningRate = getLearningRate(epoch, count, params);
			w.scale(1.0f-learningRate);
			w.addSparseFeatureVector(update, 2*learningRate*params.C_FOR_STRUCTURE*loss_term);
			//System.out.println(loss_term);
			epochUpdateCount++;

			count++;
		}
		return count;
	}

	/**
	 * 
	 * @param epoch
	 * @param count
	 * @return
	 */
	protected double getLearningRate(int epoch, int count, SLParameters params) {
		if (params.DECAY_LEARNING_RATE)
			return params.LEARNING_RATE / count;
		else
			return params.LEARNING_RATE;
	}
	protected ProgressReportFunction f;

	public void runWhenReportingProgress(ProgressReportFunction f) {
		this.f = f;
	}
}
