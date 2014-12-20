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
