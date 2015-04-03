package edu.illinois.cs.cogcomp.sl.learner.structured_perceptron;


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

/*
 * @author Vivek Srikumar
 * 
 */
public class StructuredPerceptron extends Learner{

	private static Logger log = LoggerFactory.getLogger(StructuredPerceptron.class);

	protected final SLParameters params;

	public static Random random = new Random();

	protected AbstractInferenceSolver inference;

	protected int epochUpdateCount;

	/**
	 * @param infSolver 
	 * @param featureGenerator
	 * @param params
	 */
	public StructuredPerceptron(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg,
			SLParameters params) {
		super(infSolver, fg, params);
		this.inference = infSolver;
		this.params = (SLParameters)params;

	}
	
	/**
	 * To train with the default choice(zero vector) of initial weight vector. 
	 * Often this suffices.
	 * @param problem	The structured problem on which the perceptron should be trained
	 * @return w	The weight vector learnt from the training
	 * @throws Exception
	 */
	@Override
	public WeightVector train(SLProblem problem) throws Exception {

		WeightVector init = new WeightVector(10000);

		return train(problem, init);
	}
	/**
	 * To train with a custom (possibly non-zero) initial weight vector
	 * @param problem	The structured problem on which the perceptron should be trained
	 * @param init	The initial weightvector to be used
	 * @return w	The weight vector learnt from the training data	
	 * @throws Exception
	 */
	public WeightVector train(SLProblem problem, WeightVector init)
			throws Exception {

		log.info("Starting Structured Perceptron learner");

		long start = System.currentTimeMillis();
		
		WeightVector avg = new WeightVector(10000);
		WeightVector w = init;

		int epoch = 0;
		boolean done = false;

		int count = 1;

		while (!done) {

			if (epoch % params.PROGRESS_REPORT_ITER == 0)
				log.info("Starting epoch {}", epoch);

			count = doOneIteration(w, avg, problem, epoch, count);

			if (epoch % params.PROGRESS_REPORT_ITER == 0)
				log.info("End of epoch {}. {} updates made", epoch,
						epochUpdateCount);

			epoch++;
			done = !reachedStoppingCriterion(w, epoch);
			if (params.PROGRESS_REPORT_ITER > 0 && (epoch+1) % params.PROGRESS_REPORT_ITER == 0 && this.f != null)
					f.run(w, inference);

		}

		long end = System.currentTimeMillis();

		log.info("Learning complete. Took {}s", "" + (end - start) * 1.0
					/ 1000);

		WeightVector a = new WeightVector(w);
		a.addDenseVector(avg, -1.0f / (count));

		return a;

	}

	/**
	 * Checks if stopping criterion has been met. We will stop if either no mistakes were made 
	 * during this iteration, or the maximum number of passes over the training data has been made.
	 * @param w
	 * @param epoch
	 * @return
	 */
	protected boolean reachedStoppingCriterion(WeightVector w, int epoch) {

		if (epochUpdateCount == 0) {
			log.info("No errors made. Stopping outer loop because learning is complete!");
			return false;
		}
		return epoch < params.MAX_NUM_ITER;
	}
	/**
	 * Performs one pass over the entire training data.
	 * @param w		The weight vector to use during this iteration
	 * @param avg	The average of all the weight vectors generated so far.
	 * @param problem
	 * @param epoch
	 * @param count
	 * @return
	 * @throws Exception
	 */
	protected int doOneIteration(WeightVector w, WeightVector avg,
			SLProblem problem, int epoch, int count) throws Exception {
		int numExamples = problem.size();

		epochUpdateCount = 0;
		problem.shuffle(random); // shuffle your training data after every iteration

		for (int exampleId = 0; exampleId < numExamples; exampleId++) {
			IInstance example = problem.instanceList.get(exampleId);	// the input "x"
			IStructure gold = problem.goldStructureList.get(exampleId);	// the gold output structure "y"

			IStructure prediction = null;
			boolean shouldUpdate = false;

			prediction = this.inference.getLossAugmentedBestStructure(w, example, gold);	// the predicted structure
			//NOTE: this is loss augmented, so that you can train loss augmented variants too.
			// if you want the usual behavior, where inference returns best structure, just write your loss augmented inference to ignore the loss.
			// this was done to support general behavior.

			shouldUpdate = this.inference.getLoss(example, gold, prediction) > 0;	// we will update if the loss is non-zero for this example

			if (shouldUpdate) {
				assert prediction != null;
				update(example, gold, prediction, w, avg, epoch, count);
				epochUpdateCount++;
			}
			count++;
		}
		return count;
	}
	/**
	 * Performs the update of the weight vector
	 * @param gold
	 * @param prediction
	 * @param w
	 * @param avg
	 * @param epoch
	 * @param count
	 */
	protected void update(IInstance ins, IStructure gold, IStructure prediction,
			WeightVector w, WeightVector avg, int epoch, int count) {

		IFeatureVector goldFeatures = featureGenerator.getFeatureVector(ins, gold); 
		IFeatureVector predictedFeatures = featureGenerator.getFeatureVector(ins, prediction);

		IFeatureVector update = goldFeatures.difference(predictedFeatures);

		double learningRate = getLearningRate(epoch, count);

		w.addSparseFeatureVector(update, learningRate);

		avg.addSparseFeatureVector(update, count * learningRate);

	}
	/**
	 * 
	 * @param epoch
	 * @param count
	 * @return
	 */
	protected double getLearningRate(int epoch, int count) {
		if (params.DECAY_LEARNING_RATE)
			return params.LEARNING_RATE / count;
		else
			return params.LEARNING_RATE;
	}
}
