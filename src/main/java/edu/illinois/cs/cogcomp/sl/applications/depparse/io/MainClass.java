///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
//
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
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

public abstract class MainClass {

	public static void main(String[] args) throws Exception {

		// System.out.println(problem.size());
		if(args.length!=1)
		{
			System.out.println("usage: <config file>");
		}
		SLModel model = train(args[0]);
		model.saveModel("trained.model");
		System.out.println("Testing on Training Data");
		test("trained.model","data/depparse/english_train.conll");
		System.out.println("Testing on Test Data");
		test("trained.model","data/depparse/english_test.conll");
	}

	static SLProblem getStructuredData(String filepath) throws IOException {
		CONLLReader depReader = new CONLLReader();
		depReader.startReading(filepath);
		SLProblem problem = new SLProblem();
		DependencyInstance instance = depReader.getNext();
		while (instance != null) {
			Pair<IInstance, IStructure> pair = getSLPair(instance);
//			getParseTree(instance);
			problem.addExample(pair.getFirst(), pair.getSecond());
			instance = depReader.getNext();
		}
		return problem;
	}

	public static SLModel train(String configFilePath) throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();
		if (model.lm.isAllowNewFeatures())
			model.lm.addFeature("W:unknownword");
		model.featureGenerator = new DepFeatureGenerator(model.lm);
		SLProblem problem = getStructuredData("data/depparse/english_train.conll");
		pre_extract(model, problem);
		// extraction done
		System.out.println(model.lm.getNumOfFeature());
		model.infSolver = new ChuLiuEdmondsDecoder(model.lm,
				model.featureGenerator);
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature();
		Learner learner = LearnerFactory.getLearner(model.infSolver,
				model.featureGenerator, para);
		model.wv = learner.train(problem);
		model.lm.setAllowNewFeatures(false);
		return model;
	}

	public static void test(String modelPath, String testDataPath) throws Exception{
		SLModel model = SLModel.loadModel(modelPath);
		SLProblem sp = getStructuredData(testDataPath);
		double acc = 0.0;
		double total = 0.0;

		for (int i = 0; i < sp.instanceList.size(); i++) {
			DepInst sent =(DepInst) sp.instanceList.get(i);
			DepStruct gold = (DepStruct) sp.goldStructureList.get(i);
			DepStruct prediction = (DepStruct) model.infSolver.getBestStructure(model.wv, sent);
			IntPair tmp = evaluate(sent, gold, prediction);
			acc+=tmp.getFirst();
			total+=tmp.getSecond();
		}
		System.out.println("acc "+acc);
		System.out.println("total "+total);
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
		// there shld be a better way, feature extraction
		for (Pair<IInstance, IStructure> p : problem) {
			model.featureGenerator
					.getFeatureVector(p.getFirst(), p.getSecond());
		}
	}

	public static void getParseTree(DependencyInstance instance) {
		String[] labs = instance.deprels;
		int[] heads = instance.heads;

		StringBuffer spans = new StringBuffer(heads.length * 5);
		for (int i = 1; i < heads.length; i++) {
			spans.append(heads[i]).append("|").append(i).append(":")
					.append(labs[i]).append(" ");
		}
		instance.actParseTree = spans.substring(0, spans.length() - 1);
		System.out.println(instance.actParseTree);
	}

	private static Pair<IInstance, IStructure> getSLPair(
			DependencyInstance instance) {
		DepStruct d = new DepStruct(instance);
		DepInst ins = new DepInst(instance);
		return new Pair<IInstance, IStructure>(ins, d);
	}

}
