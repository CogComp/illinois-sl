package edu.illinois.cs.cogcomp.sl.latentsvm;

import edu.illinois.cs.cogcomp.sl.core.*;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LatentLearner extends Learner{

	private final AbstractLatentInferenceSolver solver;
	private final SLParameters params;
	private Learner baseLearner;

	public LatentLearner(Learner baseLearner, AbstractFeatureGenerator fg, SLParameters params, AbstractLatentInferenceSolver solver) {
		super(solver,fg, params);
		this.baseLearner = baseLearner;
		this.solver=solver;
		this.params=params;
	}

	public WeightVector train(SLProblem problem) throws Exception {
		return train(problem, new WeightVector(10000));
	}

	public WeightVector train(SLProblem problem, WeightVector w_init)
			throws Exception {

		WeightVector w = w_init; // new WeightVector(100000);//baseLearner.train(problem); // init w

		for (int outerIter = 0; outerIter < params.MAX_NUM_ITER; outerIter++) {

			SLProblem new_prob = runLatentStructureInference(problem, w,solver); // returns structured problem with (x_i,h_i)
			w = baseLearner.train(new_prob, w); // update weight vector

			if (params.PROGRESS_REPORT_ITER > 0 && (outerIter+1) % params.PROGRESS_REPORT_ITER == 0 && this.f != null)
				f.run(w, solver);
		}

		return w;
	}

	private static SLProblem runLatentStructureInference(SLProblem problem,
														 WeightVector w, AbstractLatentInferenceSolver inference)
			throws Exception {

		SLProblem p = new SLProblem();
		for (int i = 0; i < problem.size(); i++) {
			IInstance x = problem.instanceList.get(i);
			IStructure gold = problem.goldStructureList.get(i);
			IStructure y = inference.getBestLatentStructure(w, x, gold); // best
			// explaining
			// latent
			// structure
			p.instanceList.add(x);
			p.goldStructureList.add(y);

		}

		return p;
	}

}
