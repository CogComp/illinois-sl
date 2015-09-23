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
package edu.illinois.cs.cogcomp.sl.applications.reranking;

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RankingInferenceSolver extends AbstractInferenceSolver{

	private static final long serialVersionUID = 1L;

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		RankingInstance ri = (RankingInstance) ins;
		RankingLabel lri = (RankingLabel) goldStructure;
		int max_index = -1;
		double max_score = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < ri.featureList.size(); i ++){
			float loss = -ri.scoreList.get(i)+ ri.scoreList.get(lri.pred_item);
			if (loss< 0){
				System.out.println("-----------");
				System.out.println(ri.scoreList);
				System.out.println("best: " + lri.pred_item);
			}
			assert loss >= 0;
			float score = weight.dotProduct(ri.featureList.get(i)) + loss;
			if (score > max_score){
				max_score = score;
				max_index = i;
			}
		}
		assert max_index != -1;
		
		RankingLabel pred = new RankingLabel(max_index);
		//System.out.println("pred: " + max_index + " gold: " + lri.pred_item);
		return pred;
	}

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		RankingInstance ri = (RankingInstance) ins;
		int maxIndex = -1;
		double maxScore = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < ri.featureList.size(); i ++){
			double score = weight.dotProduct(ri.featureList.get(i));
			if (score > maxScore){
				maxScore = score;
				maxIndex = i;
			}
		}
		assert maxIndex != -1;
		
		return new RankingLabel(maxIndex);
	}
	@Override
	public float getLoss(IInstance ins, IStructure goldStructure,  IStructure structure){
		RankingInstance ri = (RankingInstance) ins;
		RankingLabel lri = (RankingLabel) goldStructure;
		RankingLabel pri = (RankingLabel) structure;
		return -ri.scoreList.get(pri.pred_item)+ ri.scoreList.get(lri.pred_item);
	}
	
	@Override
	public Object clone(){
		return new RankingInferenceSolver();
	}
}
