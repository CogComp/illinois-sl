package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.sl.applications.latentsvm.Instance2.Dataset;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class AInference extends AbstractLatentInferenceSolver{

	private int nBits;

	public AInference(int nBits) {
		this.nBits = nBits;
	}

	@Override
	public IStructure getBestLatentStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {

		Structure2 gold = (Structure2) goldStructure;

		Instance2 in = (Instance2) ins;

		if (in.dataset == Dataset.BOTH)
			return gold;

		Structure2 best = null;
		double max = Double.NEGATIVE_INFINITY;

		for (int[] is : Permutations.getAllBinaryCombinations(2)) {
			boolean[] b = new boolean[4];

			if (in.dataset == Dataset.ONE) {

				b[0] = gold.y[0];
				b[1] = gold.y[1];
				b[2] = is[0] > 0;
				b[3] = is[1] > 0;
			} else if (in.dataset == Dataset.TWO) {

				b[2] = gold.y[0];
				b[3] = gold.y[1];
				b[0] = is[0] > 0;
				b[1] = is[1] > 0;
			}

			Structure2 s = new Structure2(in, b);

			double score = weight.dotProduct(s.getFeatureVector());
			if (score > max) {
				max = score;
				best = s;
			}

		}

		return best;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {

		Structure2 best = null;
		double max = Double.NEGATIVE_INFINITY;
		Structure2 g = (Structure2) goldStructure;
		Instance2 x = (Instance2) ins;

		double l = 0;

		for (int[] is : Permutations.getAllBinaryCombinations(nBits)) {
			Structure2 s = new Structure2(x, getAssignment(is));

			double loss = 0;
			if (g != null) {

				for (int i = 0; i < 4; i++) {

					boolean count = (x.dataset == Dataset.BOTH)
							|| (x.dataset == Dataset.ONE && i < 2)
							|| (x.dataset == Dataset.TWO && i >= 2);

					if (count) {
						if (g.yOriginal[i] != s.yOriginal[i])
							loss++;
					}
				}
			}

			double score = weight.dotProduct(s.getFeatureVector()) + loss;

			if (score > max) {
				max = score;
				best = s;
				l = loss;
			}
		}
		return best;
	}

	private boolean[] getAssignment(int[] is) {
		boolean[] b = new boolean[is.length];
		for (int i = 0; i < is.length; i++)
			b[i] = is[i] == 1;
		return b;
	}

}
