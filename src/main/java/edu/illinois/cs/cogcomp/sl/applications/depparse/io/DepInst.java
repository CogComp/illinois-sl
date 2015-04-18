package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class DepInst implements IInstance {

	public SparseFeatureVector feat;
	public String[] lemmas;
	public String[] pos;
	public String[] cpos;

	public DepInst(DependencyInstance instance) {
		feat = instance.fv;
		lemmas = instance.lemmas;
		pos = instance.postags;
		cpos = instance.cpostags;
	}

	public int size() {
		return lemmas.length - 1; // this is the true size, after removing the 0
									// root
	}

}
