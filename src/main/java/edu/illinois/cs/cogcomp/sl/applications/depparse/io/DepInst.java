package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class DepInst implements IInstance{

	private SparseFeatureVector feat;
	private String[] lemmas;

	public DepInst(DependencyInstance instance) {
		feat = instance.fv;
		lemmas=instance.lemmas;
	}

}
