package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import java.util.Map;

import edu.cmu.cs.ark.cle.Arborescence;
import edu.cmu.cs.ark.cle.ChuLiuEdmonds;
import edu.cmu.cs.ark.cle.graph.DenseWeightedGraph;
import edu.cmu.cs.ark.cle.util.Weighted;
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
//		System.out.println(sent.size());
		DepStruct gold = goldStructure != null ? (DepStruct) goldStructure
				: null;
		// TODO edge matrix dims?
		double[][] edgeScore = new double[sent.size()+1][sent.size()+1];
		initEdgeScores(edgeScore);
		for (int head = 0; head <= sent.size(); head++) {
			for (int j = 1; j <= sent.size(); j++) {
//				System.out.println(head+" "+j);
				IFeatureVector fv = feat.getEdgeFeatures(head, j, sent);
				// edge from head i to modifier j
				edgeScore[head][j] = weight.dotProduct(fv);
				if (gold != null) {
					if (gold.heads[j] != head) // incur loss
						edgeScore[head][j] += 1.0f;
				}

			}
		}
		DepStruct pred = ChuLiuEdmonds(edgeScore);
		return pred;
	}

	private void initEdgeScores(double[][] edgeScore) {
		// TODO Auto-generated method stub
		for(int i=0;i<edgeScore.length;i++)
			for(int j=0;j<edgeScore[0].length;j++)
			{
				edgeScore[i][j]=Float.NEGATIVE_INFINITY;
			}
	}

	/**
	 * takes matrix[i][j] with directed edge i-->j scores and find the maximum
	 * aborescence using Chu-Liu-Edmonds algorithm
	 * 
	 * @param edgeScore
	 * @return
	 */
	private DepStruct ChuLiuEdmonds(double[][] edgeScore) {
		DenseWeightedGraph<Integer> dg = DenseWeightedGraph.from(edgeScore);
		Weighted<Arborescence<Integer>> weightedSpanningTree = ChuLiuEdmonds.getMaxArborescence(dg, 0);
		Map<Integer, Integer> node2parent = weightedSpanningTree.val.parents;
		
		int[] head=new int[edgeScore.length];
		String[] deprels=new String[edgeScore[0].length];
		for(Integer node:node2parent.keySet())
		{
			head[node]=node2parent.get(node);
		}
		return new DepStruct(head,deprels);
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
