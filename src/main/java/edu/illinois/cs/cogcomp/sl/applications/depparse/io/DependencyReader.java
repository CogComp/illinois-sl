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

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.ViterbiInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public abstract class DependencyReader {

	protected BufferedReader inputReader;

	protected boolean labeled = true;

	protected boolean confScores = false;

	public boolean startReading(String file) throws IOException {
		inputReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		return labeled;
	}

	public boolean isLabeled() {
		return labeled;
	}

	public abstract DependencyInstance getNext() throws IOException;

	protected abstract boolean fileContainsLabels(String filename)
			throws IOException;

	protected String normalize(String s) {
		if (s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
			return "<num>";

		return s;
	}
	
	public SparseFeatureVector createFeatureVector(DependencyInstance instance) {

	    final int instanceLength = instance.length();

	    String[] labs = instance.deprels;
	    int[] heads = instance.heads;

	    SparseFeatureVector fv = null;
	    HashMap<String, Float> featMap = new HashMap<String,Float>();
	    for (int i = 0; i < instanceLength; i++) {
	      if (heads[i] == -1) {
	        continue;
	      }
	      int small = i < heads[i] ? i : heads[i];
	      int large = i > heads[i] ? i : heads[i];
	      boolean attR = i < heads[i] ? false : true;
	      addCoreFeatures(instance, small, large, attR, featMap);
	    }
	    return fv;
	  }

	private void addCoreFeatures(DependencyInstance instance, int small,
			int large, boolean attR, HashMap<String, Float> featMap) {
		
		String[] forms = instance.forms;
	    String[] pos = instance.postags;
	    String[] posA = instance.cpostags;

	    String att = attR ? "RA" : "LA";

	    int dist = Math.abs(large - small);
	    String distBool = "0";
	    if (dist > 10) {
	      distBool = "10";
	    } else if (dist > 5) {
	      distBool = "5";
	    } else {
	      distBool = Integer.toString(dist - 1);
	    }

	    String attDist = "&" + att + "&" + distBool;		
	}

	public static void main(String[] args) throws IOException {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();

		CONLLReader depReader = new CONLLReader();
		depReader.startReading("data/depparse/english_train.conll");
		SLProblem problem = new SLProblem();
		DependencyInstance instance = depReader.getNext();
		int num1 = 0;
		while (instance != null) {
			Pair<IInstance, IStructure> pair = getSLPair(instance);
			getParseTree(instance);
			problem.addExample(pair.getFirst(),pair.getSecond());
			num1++;
			instance = depReader.getNext();
			System.out.println(num1);
		}
		System.out.println(problem.size());
		
		model.featureGenerator = new DepFeatureGenerator(model.lm);
		
		// there shld be a better way
		for(Pair<IInstance, IStructure> p:problem)
		{
			model.featureGenerator.getFeatureVector(p.getFirst(), p.getSecond());
		}
		
		model.lm.setAllowNewFeatures(false);
		System.out.println(model.lm.getNumOfFeature());
		model.infSolver = new TreeDecoder(model.lm);
	}
	
	public static void getParseTree(DependencyInstance instance){
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

	private static Pair<IInstance,IStructure> getSLPair(DependencyInstance instance) {
		DepStruct d = new DepStruct(instance);
		DepInst ins = new DepInst(instance);
		return new Pair<IInstance,IStructure> (ins,d);
	}
	
}
