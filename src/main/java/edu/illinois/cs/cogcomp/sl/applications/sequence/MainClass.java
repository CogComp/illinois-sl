package edu.illinois.cs.cogcomp.sl.applications.sequence;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;


public class MainClass {
	
	public static class AllTest{
		@CommandDescription(description="testSequenceModel trainingDataPath ConfigFilePath modelPath")
		public static void trainSequenceModel(String trainingDataPath, String configFilePath, String modelPath)
				throws Exception {
			SLModel model = new SLModel();
			SLProblem sp = SequenceIOManager.readProblem(trainingDataPath, false);
			
			// initialize the inference solver
			model.infSolver = new SequenceInferenceSolver();
			
			SLParameters para = new SLParameters();
			para.loadConfigFile(configFilePath);
			SequenceFeatureGenerator fg = new SequenceFeatureGenerator();
			para.TOTAL_NUMBER_FEATURE = SequenceIOManager.numFeatures * SequenceIOManager.numLabels + SequenceIOManager.numLabels +
					SequenceIOManager.numLabels *SequenceIOManager.numLabels;
			
			Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
			model.wv = learner.train(sp);
			model.config =  new HashMap<String, String>();
			model.config.put("numFeatures", String.valueOf(SequenceIOManager.numFeatures));
			model.config.put("numLabels", String.valueOf(SequenceIOManager.numLabels));
			//System.out.println("Finish. Primal Objective Function Value is " + 
			//L2LossSSVMLearner.getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE));
			
			// save the model
			model.saveModel(modelPath);
		}
		@CommandDescription(description="testSequenceModel modelPath testDataPath")
		public static void testSequenceModel(String modelPath, String testDataPath) throws Exception{
			testSequenceModel(modelPath, testDataPath, null);
		}
		
		@CommandDescription(description="testSequenceModel modelPath testDataPath prediction")
		public static void testSequenceModel(String modelPath, String testDataPath, String predictionFileName)
				throws Exception {
			SLModel model = SLModel.loadModel(modelPath);
			SequenceIOManager.numFeatures = Integer.valueOf(model.config.get("numFeatures"));
			SequenceIOManager.numLabels = Integer.valueOf(model.config.get("numLabels"));
			SLProblem sp = SequenceIOManager.readProblem(testDataPath, true);
			
			System.out.println("Acc = " + Evaluator.evaluate(sp, model.wv, model.infSolver,predictionFileName));
		}
		
		@CommandDescription(description="holdoutExp trainDataPath testDataPath  configFilePath modelPath")
		public static void holdoutExp(String trainingDataPath, String testDataPath, String configFilePath, String modelPath)
				throws Exception {
			SLModel model = new SLModel();
			SLProblem sp = SequenceIOManager.readProblem(trainingDataPath, false);
			SLProblem testSp = SequenceIOManager.readProblem(testDataPath, true);
			// initialize the inference solver
			model.infSolver = new SequenceInferenceSolver();
			
			SLParameters para = new SLParameters();
			para.loadConfigFile(configFilePath);
			para.TOTAL_NUMBER_FEATURE = SequenceIOManager.numFeatures * SequenceIOManager.numLabels + SequenceIOManager.numLabels +
					SequenceIOManager.numLabels *SequenceIOManager.numLabels;
			
			Evaluator eval = new Evaluator(para.PROGRESS_REPORT_ITER);
			SequenceFeatureGenerator fg = new SequenceFeatureGenerator();
			Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
			learner.runWhenReportingProgress(eval);
			model.wv = learner.train(sp);
			
			model.config =  new HashMap<String, String>();
			model.config.put("numFeatures", String.valueOf(SequenceIOManager.numFeatures));
			model.config.put("numLabels", String.valueOf(SequenceIOManager.numLabels));
			System.out.println(SequenceIOManager.numFeatures);
			System.out.println(SequenceIOManager.numLabels);
			System.out.println(Evaluator.evaluate(testSp, model.wv, model.infSolver,null));
			eval.postEvaluation(testSp, new SequenceInferenceSolver());
			model.saveModel(modelPath);
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
