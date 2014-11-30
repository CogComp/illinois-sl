package edu.illinois.cs.cogcomp.sl.applications.tutorial;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMDCDSolver;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class MainClass {

	public static class AllTest {
		@CommandDescription(description = "testSequenceModel trainingDataPath ConfigFilePath modelPath")
		public static void trainSequenceModel(String trainingDataPath,
				String configFilePath, String modelPath) throws Exception {
			SLModel model = new SLModel();
			model.lm = new Lexiconer();

			SLProblem sp = readStructuredData(trainingDataPath, model.lm);

			// Disallow the creation of new features
			model.lm.setAllowNewFeatures(false);

			// initialize the inference solver
			model.infSolver = new ViterbiInferenceSolver(model.lm);

			POSManager fg = new POSManager(model.lm);
			SLParameters para = new SLParameters();
			para.loadConfigFile(configFilePath);
			para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature()
					* model.lm.getNumOfLabels() + model.lm.getNumOfLabels()
					+ model.lm.getNumOfLabels() * model.lm.getNumOfLabels();
			// numLabels*numLabels for transition features
			// numWordsInVocab*numLabels for emission features
			// numLabels for prior on labels
			Learner learner = LearnerFactory.getLearner(model.infSolver, fg,
					para);
			model.wv = learner.train(sp);
			
			if(learner instanceof L2LossSSVMLearner)
				System.out.println("Primal objective:" + ((L2LossSSVMLearner)learner).getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE));
			// save the model
//			for(float f:model.wv.getInternalArray())
//				System.out.println(f);
			model.saveModel(modelPath);
		}

		@CommandDescription(description = "testSequenceModel modelPath testDataPath")
		public static void testSequenceModel(String modelPath,
				String testDataPath) throws Exception {
			SLModel model = SLModel.loadModel(modelPath);
			SLProblem sp = readStructuredData(testDataPath, model.lm);

			double acc = 0.0;
			double total = 0.0;

			for (int i = 0; i < sp.instanceList.size(); i++) {

				POSTag gold = (POSTag) sp.goldStructureList.get(i);
				POSTag prediction = (POSTag) model.infSolver.getBestStructure(
						model.wv, sp.instanceList.get(i));

				for (int j = 0; j < prediction.tags.length; j++) {
					total += 1.0;
					if (prediction.tags[j] == gold.tags[j]) {
						acc += 1.0;
					}
				}
			}
			System.out.println("Acc = " + acc / total);
		}
	}

	public static void main(String[] args) throws Exception {
		InteractiveShell<AllTest> tester = new InteractiveShell<AllTest>(
				AllTest.class);

		if (args.length == 0)
			tester.showDocumentation();
		else {
			long start_time = System.currentTimeMillis();
			tester.runCommand(args);

			System.out.println("This experiment took "
					+ (System.currentTimeMillis() - start_time) / 1000.0
					+ " secs");
		}
	}

	public static SLProblem readStructuredData(String fname, Lexiconer lm)
			throws IOException, DataFormatException {
		List<String> lines = LineIO.read(fname);
		SLProblem sp = new SLProblem();

		assert lines.size() % 2 == 0; // must be even; contains labels

		if (lm.isAllowNewFeatures())
			lm.addFeature("W:unknownword"); // pre-add the unknown word to the
											// vocab

		for (int i = 0; i < lines.size() / 2; i++) {
			String[] words = lines.get(i * 2).split("\\s+");
			int[] wordIds = new int[words.length];

			for (int j = 0; j < words.length; j++) {
				// this will be off at test time, so new words wont be added to the lexicon
				if (lm.isAllowNewFeatures()) {
					lm.addFeature("w:" + words[j]);
				}
				if (lm.containFeature("w:" + words[j]))
					wordIds[j] = lm.getFeatureId("w:" + words[j]);
				else
					wordIds[j] = lm.getFeatureId("W:unknownword");
				// new word seen at test time, so we map it to unknown
			}
			Sentence x = new Sentence(wordIds);
			String[] tags = lines.get(i * 2 + 1).split("\\s+");
			int[] tagIds = new int[words.length];

			if (words.length != tags.length) {
				throw new DataFormatException(
						"The number of tokens and number tags in " + i
								+ "-th sample does not match");
			}
			for (int j = 0; j < tags.length; j++) {
				lm.addLabel("tag:" + tags[j]);
				tagIds[j] = lm.getLabelId("tag:" + tags[j]);
			}
			sp.addExample(x, new POSTag(tagIds));
		}
		return sp;
	}
}
