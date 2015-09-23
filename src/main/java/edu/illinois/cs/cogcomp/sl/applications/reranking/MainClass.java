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