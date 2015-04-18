package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class ChuLiuEdmondsDecoder extends AbstractInferenceSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7033487235520156024L;
	private DepFeatureGenerator feat;

	public ChuLiuEdmondsDecoder(Lexiconer lm,
			AbstractFeatureGenerator featureGenerator) {
		feat = (DepFeatureGenerator) featureGenerator;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}

	@Override
	public IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception {
		DepInst sent = (DepInst) ins;
		DepStruct gold = goldStructure != null ? (DepStruct) goldStructure
				: null;
		// TODO edge matrix dims?
		float[][] edgeScore = new float[sent.size()][sent.size()];
		for (int i = 0; i <= sent.size(); i++) {
			for (int j = 1; j <= sent.size(); j++) {
				IFeatureVector fv = feat.getEdgeFeatures(i, j, sent);
				// edge from head i to modifier j
				edgeScore[i][j] = weight.dotProduct(fv);
				if (gold != null) {
					if (gold.heads[j] != i)
						edgeScore[i][j] += 1.0f;
				}

			}
		}
		DepStruct pred = ChuLiuEdmonds(edgeScore);
		return pred;
	}

	/**
	 * takes matrix[i][j] with directed edge i-->j scores and find the maximum
	 * aborescence using Chu-Liu-Edmonds algorithm
	 * 
	 * @param edgeScore
	 * @return
	 */
	private DepStruct ChuLiuEdmonds(float[][] edgeScore) {
		return null;
	}

	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {

		float loss = 0.0f;
		DepStruct predDep = (DepStruct) pred;
		DepStruct goldDep = (DepStruct) gold;
		for (int i = 1; i < predDep.heads.length; i++) {
			if (predDep.heads[i] != goldDep.heads[i]) {
				loss += 1.0f;
			}
		}
		return loss;
	}

}
