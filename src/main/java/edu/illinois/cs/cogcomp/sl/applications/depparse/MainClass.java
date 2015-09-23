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
package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.io.IOException;
import java.text.NumberFormat;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.applications.depparse.io.CONLLReader;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.Learner.ProgressReportFunction;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class MainClass {

	@CommandIgnore
	public static void main(String args[]) throws Exception {

		InteractiveShell<MainClass> tester = new InteractiveShell<MainClass>(
				MainClass.class);
		if (args.length == 0)
			tester.showDocumentation();
		else {
			tester.runCommand(args);
		}
	}

	static SLProblem getStructuredData(String filepath, int sizeLimit) throws IOException {
		CONLLReader depReader = new CONLLReader();
		depReader.startReading(filepath);
		SLProblem problem = new SLProblem();
		DependencyInstance instance = depReader.getNext();
		int size=0;
		while (instance != null) {
			Pair<IInstance, IStructure> pair = getSLPair(instance);
			// getParseTree(instance);
			problem.addExample(pair.getFirst(), pair.getSecond());
			size++;
			instance = depReader.getNext();
			if(size==sizeLimit)
				break;
		}
		depReader.close();
		System.out.println("# of dependency instances: " + problem.size());
		return problem;
	}

	@CommandDescription(description = "String trainFile, String configFilePath,	String modelFile")
	public static SLModel train(String trainFile, String configFilePath,
			String modelFile) throws Exception {
	
		SLModel model = new SLModel();
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		model.lm = new Lexiconer();
		if (model.lm.isAllowNewFeatures())
			model.lm.addFeature("W:unknownword");
		model.featureGenerator = new DepFeatureGenerator(model.lm);
		SLProblem problem = getStructuredData(trainFile, -1);
		model.infSolver = new ChuLiuEdmondsDecoder(model.featureGenerator);
		Learner learner = LearnerFactory.getLearner(model.infSolver,
				model.featureGenerator, para);
		learner.runWhenReportingProgress(new ProgressReportFunction() {

			@Override
			public void run(WeightVector w, AbstractInferenceSolver inference)
					throws Exception {
				printMemoryUsage();
			}
		});
		model.wv = learner.train(problem);
		printMemoryUsage();
		model.lm.setAllowNewFeatures(false);
		model.saveModel(modelFile);
		return model;
	}

	@CommandDescription(description = "String modelFile, String testFile")
	public static void test(String modelPath, String testDataPath)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		SLProblem sp = getStructuredData(testDataPath,-1);
		double acc = 0.0;
		double total = 0.0;

		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < sp.instanceList.size(); i++) {
			DepInst sent = (DepInst) sp.instanceList.get(i);
			DepStruct gold = (DepStruct) sp.goldStructureList.get(i);
			DepStruct prediction = (DepStruct) model.infSolver
					.getBestStructure(model.wv, sent);
			IntPair tmp = evaluate(sent, gold, prediction);
			acc += tmp.getFirst();
			total += tmp.getSecond();
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Time taken for "+sp.size()+" sentences: "+estimatedTime);
		System.out.println("acc " + acc);
		System.out.println("total " + total);
		System.out.println("%age correct " + (acc * 1.0 / total));
		System.out.println("Done with testing!");
	}

	static IntPair evaluate(DepInst sent, DepStruct gold, DepStruct pred) {
		int instanceLength = sent.size();
		int[] predHeads = pred.heads;
		int[] goldHeads = gold.heads;
		int corr = 0; // we only count attachment score, not edge label
		int total = 0;

		for (int i = 1; i <= instanceLength; i++) {
			if (predHeads[i] == goldHeads[i]) {
				corr++;
			}
			total++;
		}
		return new IntPair(corr, total);
	}

	private static Pair<IInstance, IStructure> getSLPair(
			DependencyInstance instance) {
		DepStruct d = new DepStruct(instance);
		DepInst ins = new DepInst(instance);
		return new Pair<IInstance, IStructure>(ins, d);
	}

	@CommandIgnore
	public static void printMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		NumberFormat nformat = NumberFormat.getInstance();
		long maxMemory = runtime.maxMemory() / (1024 * 1024);
		long allocatedMemory = runtime.totalMemory() / (1024 * 1024);
		long freeMemory = runtime.freeMemory() / (1024 * 1024);
		long usedMemory = allocatedMemory - freeMemory;

		System.out.println("max memory: " + nformat.format(maxMemory) + " MB");
		System.out.println("used-up memory: " + nformat.format(usedMemory)
				+ " MB");
		System.out.println("total free memory: "
				+ nformat.format(freeMemory + (maxMemory - allocatedMemory))
				+ " MB");
	}
}
