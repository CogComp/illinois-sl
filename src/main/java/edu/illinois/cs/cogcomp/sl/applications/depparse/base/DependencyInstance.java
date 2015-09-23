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
package edu.illinois.cs.cogcomp.sl.applications.depparse.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * Borrowed from McDonald's MST Parser at 
 * http://www.seas.upenn.edu/~strctlrn/MSTParser/MSTParser.html
 *
 */
public class DependencyInstance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1738430545985436659L;

	public String actParseTree;

	// The various data types. Here's an example from Portuguese:
	//
	// 3 eles ele pron pron-pers M|3P|NOM 4 SUBJ _ _
	// ID FORM LEMMA COURSE-POS FINE-POS FEATURES HEAD DEPREL PHEAD PDEPREL
	//
	// We ignore PHEAD and PDEPREL for now.

	// FORM: the forms - usually words, like "thought"
	public String[] forms;

	// LEMMA: the lemmas, or stems, e.g. "think"
	public String[] lemmas;

	// COURSE-POS: the course part-of-speech tags, e.g."V"
	public String[] cpostags;

	// FINE-POS: the fine-grained part-of-speech tags, e.g."VBD"
	public String[] postags;

	// FEATURES: some features associated with the elements separated by "|",
	// e.g. "PAST|3P"
	public String[][] feats;

	// HEAD: the IDs of the heads for each element
	public int[] heads;

	// DEPREL: the dependency relations, e.g. "SUBJ"
	public String[] deprels;

	// RELATIONAL FEATURE: relational features that hold between items
	public RelationalFeature[] relFeats;

	// Confidence scores per edge
	public double[] confidenceScores;

	public DependencyInstance() {
	}

	public DependencyInstance(String[] forms, String[] postags, String[] labs,
			int[] heads) {
		this.forms = forms;
		this.postags = postags;
		this.deprels = labs;
		this.heads = heads;
	}

	public DependencyInstance(String[] forms, String[] postags, String[] labs,
			int[] heads, double[] confidenceScores) {
		this(forms, postags, labs, heads);
		this.confidenceScores = confidenceScores;
	}

	public DependencyInstance(String[] forms, String[] lemmas,
			String[] cpostags, String[] postags, String[][] feats,
			String[] labs, int[] heads) {
		this(forms, postags, labs, heads);
		this.lemmas = lemmas;
		this.cpostags = cpostags;
		this.feats = feats;
	}

	public DependencyInstance(String[] forms, String[] lemmas,
			String[] cpostags, String[] postags, String[][] feats,
			String[] labs, int[] heads, RelationalFeature[] relFeats,
			double[] confidenceScores) {
		this(forms, lemmas, cpostags, postags, feats, labs, heads);
		this.relFeats = relFeats;
		this.confidenceScores = confidenceScores;
	}

	public void getParseTree() {
		String[] labs = this.deprels;
		int[] heads = this.heads;

		StringBuffer spans = new StringBuffer(heads.length * 5);
		for (int i = 1; i < heads.length; i++) {
			spans.append(heads[i]).append("|").append(i).append(":")
					.append(labs[i]).append(" ");
		}
		this.actParseTree = spans.substring(0, spans.length() - 1);
//		System.out.println(this.actParseTree);
	}
	public int length() {
		return forms.length;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Arrays.toString(forms)).append("\n");
		return sb.toString();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(forms);
		out.writeObject(lemmas);
		out.writeObject(cpostags);
		out.writeObject(postags);
		out.writeObject(heads);
		out.writeObject(deprels);
		out.writeObject(actParseTree);
		out.writeObject(feats);
		out.writeObject(relFeats);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		forms = (String[]) in.readObject();
		lemmas = (String[]) in.readObject();
		cpostags = (String[]) in.readObject();
		postags = (String[]) in.readObject();
		heads = (int[]) in.readObject();
		deprels = (String[]) in.readObject();
		actParseTree = (String) in.readObject();
		feats = (String[][]) in.readObject();
		relFeats = (RelationalFeature[]) in.readObject();
	}

}
