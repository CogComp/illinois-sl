package edu.illinois.cs.cogcomp.sl.applications.ranking;

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class RankerInferenceSolver extends AbstractInferenceSolver{

	private static final long serialVersionUID = 1L;

	@Override

	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		RankingInstance ri = (RankingInstance) ins;
		RankingLabel lri = (RankingLabel) goldStructure;
		int max_index = -1;
		double max_score = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < ri.fea_list.size(); i ++){
			float loss = -ri.score_list.get(i)+ ri.score_list.get(lri.pred_item);
			if (loss< 0){
				System.out.println("-----------");
				System.out.println(ri.score_list);
				System.out.println("best: " + lri.pred_item);
			}
			assert loss >= 0;
			float score = weight.dotProduct(ri.fea_list.get(i)) + loss;
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
		int max_index = -1;
		double max_score = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < ri.fea_list.size(); i ++){
			double score = weight.dotProduct(ri.fea_list.get(i));
			if (score > max_score){
				max_score = score;
				max_index = i;
			}
		}
		assert max_index != -1;
		
		return new RankingLabel(max_index);
	}
	@Override
	public float getLoss(IInstance ins, IStructure goldStructure,  IStructure structure){
		RankingInstance ri = (RankingInstance) ins;
		RankingLabel lri = (RankingLabel) goldStructure;
		RankingLabel pri = (RankingLabel) structure;
		return -ri.score_list.get(pri.pred_item)+ ri.score_list.get(lri.pred_item);
	}
}