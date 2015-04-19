package edu.illinois.cs.cogcomp.sl.applications.depparse;

import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

/**
 * represents a sentence which is to be parsed, with some additional features (pos,lemmas)
 * @author Shyam
 *
 */
public class DepInst implements IInstance {
	// all these fields have length # of tokens +1
	public String[] lemmas;
	public String[] pos;
	public String[] cpos;

	public DepInst(DependencyInstance instance) {
		lemmas = instance.lemmas;
		pos = instance.postags;
		cpos = instance.cpostags;
	}

	/***
	 * # of tokens in the sentence
	 * @return
	 */
	public int size() {
		return lemmas.length - 1; // this is the true size, after removing the 0
									// root
	}

}
