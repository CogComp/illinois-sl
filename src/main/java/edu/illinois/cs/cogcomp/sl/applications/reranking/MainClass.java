package edu.illinois.cs.cogcomp.sl.applications.reranking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;

public class MainClass {

	public static class AllTest{
		@CommandDescription(description="trainRankingModel trainingDataPath ConfigFilePath modelPath")
		public static void trainRankingModel(String trainingDataPath, String configFilePath, String modelPath)
				throws Exception {
			SLModel model = new SLModel();
			SLProblem sp = RankingIOManager.readProblem(trainingDataPath);

			// initialize the inference solver
			model.infSolver = new RankingInferenceSolver();

			SLParameters para = new SLParameters();
			para.loadConfigFile(configFilePath);
			RankingFeatureGenerator fg = new RankingFeatureGenerator();

			Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
			model.wv = learner.train(sp);
			model.config =  new HashMap<String, String>();			

			// save the model
			model.saveModel(modelPath);
		}
		@CommandDescription(description="testRankingModel modelPath testDataPath")
		public static void testRankingModel(String modelPath, String testDataPath) throws Exception{
			testRankingModel(modelPath, testDataPath, null);
		}

		@CommandDescription(description="testRankingModel modelPath testDataPath prediction")
		public static void testRankingModel(String modelPath, String testDataPath, String predictionFileName)
				throws Exception {
			SLModel model = SLModel.loadModel(modelPath);
			SLProblem sp = RankingIOManager.readProblem(testDataPath);
			RankingInferenceSolver infSolver = new RankingInferenceSolver();

			BufferedWriter writer = null;
			if(predictionFileName!=null){
				writer = new BufferedWriter(new FileWriter(predictionFileName));
			}

			double pred_loss = 0.0;
			for (int i = 0; i < sp.size(); i++) {
				RankingInstance ri = (RankingInstance) sp.instanceList.get(i);
				RankingLabel pred = (RankingLabel) infSolver.getBestStructure(model.wv, ri);
				pred_loss += ri.scoreList.get(pred.pred_item);
				if(writer!=null)
					writer.write(pred.pred_item+ "\n");
			}
			System.out.println("Loss = " + pred_loss/sp.size());

			if(writer!=null)
				writer.close();
			return;
		}
	}
	public static void main(String[] args) throws Exception{
		InteractiveShell<AllTest> tester = new InteractiveShell<AllTest>(
				AllTest.class);

		if (args.length == 0)
			tester.showDocumentation();
		else
		{
			long start_time = System.currentTimeMillis();
			tester.runCommand(args);

			System.out.println("This experiment took "
					+ (System.currentTimeMillis() - start_time) / 1000.0
					+ " secs");
		}
	}
}