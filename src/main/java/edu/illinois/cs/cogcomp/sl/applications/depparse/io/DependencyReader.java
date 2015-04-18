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

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;

/**
 * A class that defines common behavior and abstract methods for readers for
 * different formats.
 * 
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 * 
 * @author Jason Baldridge
 * @version $Id: DependencyReader.java 137 2013-09-10 09:33:47Z wyldfire $
 */
public abstract class DependencyReader {

	protected BufferedReader inputReader;

	protected boolean labeled = true;

	protected boolean confScores = false;

	public static DependencyReader createDependencyReader(String format,
			boolean discourseMode) throws IOException {

		if (format.equals("MST")) {
			return new MSTReader();
		} else if (format.equals("CONLL")) {
			return new CONLLReader();
		} else {
			System.out.println("!!!!!!!  Not a supported format: " + format);
			System.out.println("********* Assuming CONLL format. **********");
			return new CONLLReader();
		}
	}

	public static DependencyReader createDependencyReader(String format)
			throws IOException {

		return createDependencyReader(format, false);
	}

	public static DependencyReader createDependencyReaderWithConfidenceScores(
			String format) throws IOException {
		DependencyReader reader = createDependencyReader(format);
		reader.confScores = true;
		return reader;
	}

	public boolean startReading(String file) throws IOException {
		labeled = fileContainsLabels(file);
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

	public static void main(String[] args) throws IOException {
		CONLLReader depReader = new CONLLReader();
		
		depReader.startReading("data/depparse/english_train.conll");
		
		SLProblem problem = new SLProblem();
		
		DependencyInstance instance = depReader.getNext();
		
		int num1 = 0;

		while (instance != null) {
			Pair<IInstance, IStructure> pair = getSLPair(instance);
			problem.addExample(pair.getFirst(),pair.getSecond());
			num1++;
			instance = depReader.getNext();
			System.out.println(num1);
		}
		System.out.println(problem.size());
	}
	
	public void getParseTree(DependencyInstance instance){
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
