package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import edu.illinois.cs.cogcomp.sl.applications.depparse.features.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class DepStruct implements IStructure{

	private int[] heads;
	private String[] deprels;

	public DepStruct(DependencyInstance instance) {
		heads = instance.heads;
		deprels = instance.deprels;
	}
}
