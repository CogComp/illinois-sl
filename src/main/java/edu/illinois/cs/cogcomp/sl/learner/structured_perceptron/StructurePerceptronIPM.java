package edu.illinois.cs.cogcomp.sl.learner.structured_perceptron;

/**
 * 
 */
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * @author Vivek Srikumar
 * 
 */
public class StructurePerceptronIPM extends Learner {

	private int num_threads;

	public StructurePerceptronIPM(AbstractInferenceSolver infSolver,
			AbstractFeatureGenerator fg, SLParameters params, int num_threads) {
		super(infSolver, fg, params);
		this.num_threads = num_threads;
	}

	public static int numUpdates = 0;
	private static Logger log = LoggerFactory
			.getLogger(StructurePerceptronIPM.class);

	class StructPerceptronHandler extends Thread {
		private StructuredPerceptron learner;
		private WeightVector wv;
		private SLProblem sp;
		private SLParameters para;

		public StructPerceptronHandler(StructuredPerceptron learner,
				SLProblem sp, WeightVector wv, SLParameters para) {
			this.learner = learner;
			this.wv = wv;
			this.para = para;
			this.sp = sp;
		}

		@Override
		public void run() {
			try {
				wv = learner.train(sp, wv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private WeightVector trainParallelStructuredPerceptron(
			AbstractInferenceSolver[] struct_finder_list, SLProblem sp,
			SLParameters para) throws Exception {
		// initialize thread
		int n_thread = struct_finder_list.length;

		// Approach 1: preprocess and split instances evenly (if data is on the
		// disk)
		// Approach 2: use a queue to store the instances, every instances
		// access data from the queue

		sp.shuffle(new Random(0));
		List<SLProblem> subProbs = sp.splitData(n_thread);
		StructPerceptronHandler[] inf_runner_list = new StructPerceptronHandler[n_thread];

		WeightVector wv = new WeightVector(10000);
		long startTime = System.currentTimeMillis();
		long trainTime = 0;
		float initLearningRate = para.LEARNING_RATE;
		for (int iter = 0; iter < para.MAX_NUM_ITER; iter++) {
			for (int i = 0; i < n_thread; i++) {
				StructuredPerceptron spLearner = new StructuredPerceptron(
						struct_finder_list[i], featureGenerator, para);
				inf_runner_list[i] = new StructPerceptronHandler(spLearner,
						subProbs.get(i), new WeightVector(wv, 0), para);
			}
			// run the threads
			for (int i = 0; i < n_thread; i++) {
				inf_runner_list[i].start();
			}
			// wait until all of them are finished
			for (int i = 0; i < n_thread; i++) {
				inf_runner_list[i].join();
			}

			// collect results
			wv = new WeightVector(10000);
			for (int i = 0; i < n_thread; i++) {
				wv.addDenseVector(inf_runner_list[i].wv);
			}
			wv.scale(1.0 / (double) n_thread);
			trainTime = System.currentTimeMillis() - startTime;

			log.info("Training time was " + trainTime);

			if (iter % para.PROGRESS_REPORT_ITER == 0) {
				para.LEARNING_RATE = initLearningRate / (float) (iter + 1);
				log.info("Changing learning rate to " + para.LEARNING_RATE);

			}

			para.LEARNING_RATE = initLearningRate;
		}
		return wv;
	}

	@Override
	public WeightVector train(SLProblem sp) throws Exception {
		return train(sp, new WeightVector(10000));
	}

	@Override
	public WeightVector train(SLProblem sp, WeightVector init) throws Exception {
		AbstractInferenceSolver[] solvers = new AbstractInferenceSolver[num_threads];
		for (int i = 0; i < num_threads; i++) {
			solvers[i] = (AbstractInferenceSolver) infSolver.clone();
		}
		WeightVector w = trainParallelStructuredPerceptron(solvers, sp,
				parameters);
		return w;
	}
}
