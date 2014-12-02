package edu.illinois.cs.cogcomp.sl.applications.ranking;

import java.io.IOException;

import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;


public class RankTrainer {
	
	public static void main(String[] args) throws Exception {
		SLProblem prob = RankDataReader.readFeatureFile("data/reranking/rerank.train");
		trainRerankerModel(0.1f, 1, prob);
	}
	public static SLModel trainRerankerModel(float C, int n_thread,
			SLProblem train) throws Exception {
		SLModel model = new SLModel();
		

		model.para = new SLParameters();

		// para.total_number_features = train.label_mapping.size() *
		// train.n_base_feature_in_train;
		model.para.C_FOR_STRUCTURE = C;
		model.para.TRAINMINI = true;

		// play with the following two parameters if you want to solve SSVM more
		// tightly
		
		model.para.STOP_CONDITION = 0.1f;
		model.para.INNER_STOP_CONDITION = 0.01f;

		System.out.println("Initializing Solvers...");
		System.out.flush();
		
		model.infSolver = new RankerInferenceSolver();
		
		System.out.println("Done!");
		System.out.flush();
		
		Learner learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, model.para);

		// train model
		model.wv = learner.train(train); 
		
		return model;
	}
	
	public static double getTOP1Score(SLProblem sp) throws Exception {
		double pred_loss = 0.0;
		for (int i = 0; i < sp.size(); i++) {
			RankingInstance ri = (RankingInstance) sp.instanceList.get(i);			
			pred_loss += ri.score_list.get(0);
		}
		return pred_loss/sp.size();
	}

	public static double getPredictedScore(SLProblem sp,
			RankerInferenceSolver s_finder, WeightVector wv) throws Exception {
		double pred_loss = 0.0;
		for (int i = 0; i < sp.size(); i++) {
			RankingInstance ri = (RankingInstance) sp.instanceList.get(i);
			RankingLabel pred = (RankingLabel) s_finder
					.getBestStructure(wv, ri);
			pred_loss += ri.score_list.get(pred.pred_item);
		}
		return pred_loss/sp.size();
	}
}
