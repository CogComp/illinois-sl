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
package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.util.Map;

import edu.cmu.cs.ark.cle.Arborescence;
import edu.cmu.cs.ark.cle.ChuLiuEdmonds;
import edu.cmu.cs.ark.cle.graph.DenseWeightedGraph;
import edu.cmu.cs.ark.cle.util.Weighted;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * Finds the best dep tree using McDonald's directed MST approach
 * @author upadhya3
 *
 */
public class ChuLiuEdmondsDecoder extends AbstractInferenceSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7033487235520156024L;

	private DepFeatureGenerator feat; // for getting edge features

	public ChuLiuEdmondsDecoder(AbstractFeatureGenerator featureGenerator) {
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
		// edgeScore[i][j] score of edge from head i to modifier j
		// i (head) varies from 0..n, while j (token idx) varies over 1..n
		double[][] edgeScore = new double[sent.size() + 1][sent.size() + 1];
		initEdgeScores(edgeScore);

		for (int head = 0; head <= sent.size(); head++) {
			for (int j = 1; j <= sent.size(); j++) {
				FeatureVectorBuffer edgefv = feat.getEdgeFeatures(head, j, sent);
				// edge from head i to modifier j
				edgeScore[head][j] = weight.dotProduct(edgefv.toFeatureVector(false));
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
		for (int i = 0; i < edgeScore.length; i++)
			for (int j = 0; j < edgeScore[0].length; j++) {
				edgeScore[i][j] = Float.NEGATIVE_INFINITY;
			}
	}

	/**
	 * takes matrix[i][j] with directed edge i-->j scores and find the maximum
	 * aborescence using Chu-Liu-Edmonds algorithm. Thanks to code from
	 * https://github.com/sammthomson/ChuLiuEdmonds
	 * 
	 * @param edgeScore
	 * @return
	 */
	private DepStruct ChuLiuEdmonds(double[][] edgeScore) {
		DenseWeightedGraph<Integer> dg = DenseWeightedGraph.from(edgeScore);
		Weighted<Arborescence<Integer>> weightedSpanningTree = ChuLiuEdmonds
				.getMaxArborescence(dg, 0);
		
		Map<Integer, Integer> node2parent = weightedSpanningTree.val.parents;

		int[] head = new int[edgeScore.length];
		for (Integer node : node2parent.keySet()) {
			head[node] = node2parent.get(node);
		}
		return new DepStruct(head);
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

	// For using DEMIDCD
	@Override
	public Object clone() {
		return new ChuLiuEdmondsDecoder(feat);
	}
}
