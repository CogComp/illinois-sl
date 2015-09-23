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
package edu.illinois.cs.cogcomp.sl.applications.sequence;

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
		
		@CommandDescription(description="trainSequenceModel trainingDataPath ConfigFilePath modelPath")
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
