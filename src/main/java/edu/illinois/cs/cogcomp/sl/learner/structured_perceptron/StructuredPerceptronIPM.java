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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Vivek Srikumar
 * 
 */
public class StructuredPerceptronIPM extends Learner {

	private int num_threads;
	static CyclicBarrier barrier; 

	public StructuredPerceptronIPM(AbstractInferenceSolver infSolver,
			AbstractFeatureGenerator fg, SLParameters params, int num_threads) {
		super(infSolver, fg, params);
		this.num_threads = num_threads;
	}

	public static int numUpdates = 0;
	private static Logger log = LoggerFactory
			.getLogger(StructuredPerceptronIPM.class);

	class StructPerceptronHandler extends Thread {
		private StructuredPerceptron learner;
		private WeightVector wv;
		private SLProblem sp;
		private SLParameters para;		
		public boolean stop = false;

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
				for(int iter =0; iter < para.MAX_NUM_ITER; iter++){
					wv = learner.train(sp, wv);					
					barrier.await();					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void setWv(WeightVector wv){
			this.wv.setDenseVector(wv);
		}
	}
	

	private WeightVector trainParallelStructuredPerceptron(
			AbstractInferenceSolver[] struct_finder_list, SLProblem sp,
			SLParameters para) throws Exception {
		// initialize thread
		final int n_thread = Math.min(struct_finder_list.length,sp.size());						
		log.info("Using # of threads "+n_thread);

		sp.shuffle(new Random(0));
		List<SLProblem> subProbs = sp.splitData(n_thread);
		final StructPerceptronHandler[] inf_runner_list = new StructPerceptronHandler[n_thread];


		long startTime = System.currentTimeMillis();
		long trainTime = 0;
		float initLearningRate = para.LEARNING_RATE;
		for (int i = 0; i < n_thread; i++) {
			StructuredPerceptron spLearner = new StructuredPerceptron(
					struct_finder_list[i], featureGenerator, para);
			inf_runner_list[i] = new StructPerceptronHandler(spLearner,
					subProbs.get(i), new WeightVector(10000), para);
		}		

		final WeightVector wv = new WeightVector(10000);				
		barrier = new CyclicBarrier(n_thread, new Runnable(){
			public void run(){
				// collect results
				wv.empty();

				for (int i = 0; i < n_thread; i++) {
					wv.addDenseVector(inf_runner_list[i].wv);
				}
				wv.scale(1.0 / (double) n_thread);
				for (int i = 0; i < n_thread; i++) {
					inf_runner_list[i].setWv(wv);
				}
			}
		});
			

		for (int i = 0; i < n_thread; i++) {
			inf_runner_list[i].start();
		}


		for (int i = 0; i < n_thread; i++) {
			inf_runner_list[i].join();
		}

		wv.empty();		
		for (int i = 0; i < n_thread; i++) {
			wv.addDenseVector(inf_runner_list[i].wv);
		}
		wv.scale(1.0 / (double) n_thread);


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
