package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

@SuppressWarnings("serial")
public class Inference extends AbstractInferenceSolver {

	private final int nBits;

	public Inference(int nBits) {
		this.nBits = nBits;

	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {

		Structure best = null;
		double max = Double.NEGATIVE_INFINITY;
		Structure g = (Structure) goldStructure;
		double[] x = ((Instance) ins).x;

		double l = 0;

		for (int[] is : Permutations.getAllBinaryCombinations(nBits)) {

			double loss = 0;

			boolean[] b = new boolean[is.length];
			for (int i = 0; i < is.length; i++) {
				b[i] = is[i] == 1;

				if (g != null)
					if (b[i] != g.y[i])
						loss++;
			}

			Structure s = new Structure(x, b);

			double score = weight.dotProduct(s.getFeatureVector()) + loss;

			if (score > max) {
				max = score;
				best = s;
				l = loss;
			}
		}
		return best;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		return 0;
	}

}
