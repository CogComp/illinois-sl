package edu.illinois.cs.cogcomp.sl.applications.depparse.features;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class DependencyInstance implements Serializable, IInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1738430545985436659L;

	public SparseFeatureVector fv;

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

	public DependencyInstance(DependencyInstance source) {
		this.fv = source.fv;
		this.actParseTree = source.actParseTree;
	}

	public DependencyInstance(String[] forms, SparseFeatureVector fv) {
		this.forms = forms;
		this.fv = fv;
	}

	public DependencyInstance(String[] forms, String[] postags,
			SparseFeatureVector fv) {
		this(forms, fv);
		this.postags = postags;
	}

	public DependencyInstance(String[] forms, String[] postags, String[] labs,
			SparseFeatureVector fv) {
		this(forms, postags, fv);
		this.deprels = labs;
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

	public void setFeatureVector(SparseFeatureVector fv) {
		this.fv = fv;
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
