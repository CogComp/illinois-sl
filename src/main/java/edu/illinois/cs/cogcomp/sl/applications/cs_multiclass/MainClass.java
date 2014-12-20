package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

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
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;

public class MainClass {

	public static class AllTest{
		@CommandDescription(description="trainMultiClassModel trainingDataPath costMatrix ConfigFilePath modelPath")
		public static void trainMultiClassModel(String trainingDataPath, String costMatrixPath, String configFilePath, String modelPath)
				throws Exception {
			MultiClassModel model = new MultiClassModel();
			
			LabeledMultiClassData sp = MultiClassIOManager.readTrainingData(trainingDataPath);
			model.labelMapping = sp.labelMapping;
			model.numFeatures = sp.numFeatures;
			model.cost_matrix = MultiClassIOManager.getCostMatrix(sp.labelMapping,costMatrixPath);

			// initialize the inference solver
			model.infSolver = new MultiClassInferenceSolver(model.cost_matrix);

			SLParameters para = new SLParameters();
			para.loadConfigFile(configFilePath);
			model.featureGenerator = new MultiClassFeatureGenerator();

			Learner learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, para);
			model.wv = learner.train(sp);
			model.config =  new HashMap<String, String>();
			
			// save the model
			model.saveModel(modelPath);
		}
		@CommandDescription(description="testMultiClassModel modelPath testDataPath")
		public static void testMultiClassModel(String modelPath, String testDataPath) throws Exception{
			testMultiClassModel(modelPath, testDataPath, null);
		}

		@CommandDescription(description="testMultiClassModel modelPath testDataPath prediction")
		public static void testMultiClassModel(String modelPath, String testDataPath, String predictionFileName)
				throws Exception {
			MultiClassModel model = (MultiClassModel)SLModel.loadModel(modelPath);
			SLProblem sp = MultiClassIOManager.readTestingData(testDataPath, model.labelMapping, model.numFeatures);
			
			BufferedWriter writer = null;
			if(predictionFileName!=null){
				writer = new BufferedWriter(new FileWriter(predictionFileName));
			}

			double pred_loss = 0.0;
			for (int i = 0; i < sp.size(); i++) {
				MultiClassInstance ri = (MultiClassInstance) sp.instanceList.get(i);
				MultiClassLabel pred = (MultiClassLabel) model.infSolver.getBestStructure(model.wv, ri);
				pred_loss += model.cost_matrix[((MultiClassLabel)sp.goldStructureList.get(i)).output][pred.output];
				if(writer!=null)
					writer.write(pred.output+ "\n");
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
