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
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class MainClass {

	public static class AllTest {
		
		@CommandDescription(description = "trainPOSModel trainingDataPath ConfigFilePath modelPath")
		public static void trainPOSModel(String trainingDataPath,
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
			WeightVector.printSparsity(model.wv);
			if(learner instanceof L2LossSSVMLearner)
				System.out.println("Primal objective:" + ((L2LossSSVMLearner)learner).getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE));

			// save the model
			model.saveModel(modelPath);
		}
		
		
		@CommandDescription(description = "testPOSModel modelPath testDataPath")
		public static void testPOSModel(String modelPath,
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

		assert lines.size() % 2 == 0;

		// w:unknownword indicates out-of-vocabulary words in test phase
		if (lm.isAllowNewFeatures())
			lm.addFeature("w:unknownword"); 

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
					wordIds[j] = lm.getFeatureId("w:unknownword");

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
