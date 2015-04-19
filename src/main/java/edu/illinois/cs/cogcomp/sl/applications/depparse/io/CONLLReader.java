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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.applications.depparse.features.RelationalFeature;

/**
 * A reader for files in CoNLL format.
 * 
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 * 
 * @author Jason Baldridge
 * @version $Id: CONLLReader.java 137 2013-09-10 09:33:47Z wyldfire $
 * @see mstparser.io.DependencyReader
 */
public class CONLLReader {

	public CONLLReader() {

	}

	protected BufferedReader inputReader;
	protected boolean labeled = true;

	protected boolean confScores = false;

	public DependencyInstance getNext() throws IOException {

		ArrayList<String[]> lineList = new ArrayList<String[]>();

		String line = inputReader.readLine();
		while (line != null && !line.equals("") && !line.startsWith("*")) {
			lineList.add(line.split("\t"));
			line = inputReader.readLine();
		}

		int length = lineList.size();

		if (length == 0) {
			inputReader.close();
			return null;
		}

		String[] forms = new String[length + 1]; // +1 for the 0 root
		String[] lemmas = new String[length + 1];
		String[] cpos = new String[length + 1];
		String[] pos = new String[length + 1];
		String[][] feats = new String[length + 1][];
		String[] deprels = new String[length + 1];
		int[] heads = new int[length + 1];
		double[] confscores = confScores ? new double[length + 1] : null;

		forms[0] = "<root>";
		lemmas[0] = "<root-LEMMA>";
		cpos[0] = "<root-CPOS>";
		pos[0] = "<root-POS>";
		deprels[0] = "<no-type>";
		heads[0] = -1;
		// head idx range from 1 to sent.size()
		if (confScores)
			confscores[0] = 1;

		for (int i = 0; i < length; i++) {
			String[] info = lineList.get(i);
			forms[i + 1] = normalize(info[1]);
			lemmas[i + 1] = normalize(info[2]);
			cpos[i + 1] = info[3];
			pos[i + 1] = info[4];
			feats[i + 1] = info[5].split("\\|");
			deprels[i + 1] = labeled ? info[7] : "<no-type>";
			heads[i + 1] = Integer.parseInt(info[6]);
			if (confScores)
				confscores[i + 1] = Double.parseDouble(info[10]);
		}

		feats[0] = new String[feats[1].length];
		for (int i = 0; i < feats[1].length; i++)
			feats[0][i] = "<root-feat>" + i;

		ArrayList<RelationalFeature> rfeats = new ArrayList<RelationalFeature>();

		while (line != null && !line.equals("")) {
			rfeats.add(new RelationalFeature(length, line, inputReader));
			line = inputReader.readLine();
		}

		RelationalFeature[] rfeatsList = new RelationalFeature[rfeats.size()];
		rfeats.toArray(rfeatsList);

		return new DependencyInstance(forms, lemmas, cpos, pos, feats, deprels,
				heads, rfeatsList, confscores);

	}

	protected boolean fileContainsLabels(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = in.readLine();
		in.close();

		if (line.trim().length() > 0)
			return true;
		else
			return false;
	}
	protected String normalize(String s) {
		if (s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
			return "<num>";

		return s;
	}
	public boolean startReading(String file) throws IOException {
		inputReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		return labeled;
	}
}
