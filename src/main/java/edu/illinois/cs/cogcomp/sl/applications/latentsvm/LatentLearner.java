package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class LatentLearner {

	private Learner baseLearner;

	public LatentLearner(Learner baseLearner) {
		this.baseLearner = baseLearner;
	}

	public WeightVector learn(int numInnerIters, int numOuterIters,
			SLProblem problem, AbstractLatentInferenceSolver inference)
			throws Exception {

		WeightVector w = baseLearner.train(problem);

		for (int outerIter = 0; outerIter < numOuterIters; outerIter++) {

			SLProblem new_prob = runLatentStructureInference(problem, w,
					inference);

			w = baseLearner.train(new_prob);

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
			IStructure y = inference.getBestLatentStructure(w, x, gold);

			p.instanceList.add(x);
			p.goldStructureList.add(y);

		}

		return p;
	}
}
