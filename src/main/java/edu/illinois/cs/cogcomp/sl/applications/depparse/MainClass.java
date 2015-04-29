package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandIgnore;
import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;
import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.applications.depparse.io.CONLLReader;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.ViterbiInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
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

	static SLProblem getStructuredData(String filepath) throws IOException {
		CONLLReader depReader = new CONLLReader();
		depReader.startReading(filepath);
		SLProblem problem = new SLProblem();
		DependencyInstance instance = depReader.getNext();
		while (instance != null) {
			Pair<IInstance, IStructure> pair = getSLPair(instance);
			// getParseTree(instance);
			problem.addExample(pair.getFirst(), pair.getSecond());
			instance = depReader.getNext();
		}
		return problem;
	}

	@CommandDescription(description = "String trainFile, String configFilePath,	String modelFile")
	public static SLModel train(String trainFile, String configFilePath,
			String modelFile) throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();
		if (model.lm.isAllowNewFeatures())
			model.lm.addFeature("W:unknownword");
		model.featureGenerator = new DepFeatureGenerator(model.lm);
		SLProblem problem = getStructuredData(trainFile);
		pre_extract(model, problem);
		// extraction done
		printMemoryUsage();
		System.out.println(model.lm.getNumOfFeature());
		model.infSolver = new ChuLiuEdmondsDecoder(model.featureGenerator);
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		System.out.println(para.HASHING_MASK);
		para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature();
		Learner learner = LearnerFactory.getLearner(model.infSolver,
				model.featureGenerator, para);
		model.wv = learner.train(problem);
		model.lm.setAllowNewFeatures(false);
		model.saveModel(modelFile);
		return model;
	}

	@CommandDescription(description = "String modelFile, String testFile")
	public static void test(String modelPath, String testDataPath)
			throws Exception {
		SLModel model = SLModel.loadModel(modelPath);
		SLProblem sp = getStructuredData(testDataPath);
		double acc = 0.0;
		double total = 0.0;

		for (int i = 0; i < sp.instanceList.size(); i++) {
			DepInst sent = (DepInst) sp.instanceList.get(i);
			DepStruct gold = (DepStruct) sp.goldStructureList.get(i);
			DepStruct prediction = (DepStruct) model.infSolver
					.getBestStructure(model.wv, sent);
			IntPair tmp = evaluate(sent, gold, prediction);
			acc += tmp.getFirst();
			total += tmp.getSecond();
		}
		System.out.println("acc " + acc);
		System.out.println("total " + total);
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

	private static void pre_extract(SLModel model, SLProblem problem) {
		System.out.println("Pre-extracting features ...");
		// there shld be a better way, feature extraction
		for (Pair<IInstance, IStructure> p : problem) {
			model.featureGenerator
					.getFeatureVector(p.getFirst(), p.getSecond());
		}
	}

	private static Pair<IInstance, IStructure> getSLPair(
			DependencyInstance instance) {
		DepStruct d = new DepStruct(instance);
		DepInst ins = new DepInst(instance);
		return new Pair<IInstance, IStructure>(ins, d);
	}

	public static void printMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		NumberFormat nformat = NumberFormat.getInstance();
		StringBuilder sb = new StringBuilder();
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
