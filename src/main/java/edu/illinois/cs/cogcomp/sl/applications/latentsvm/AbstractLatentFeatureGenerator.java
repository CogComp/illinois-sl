package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public abstract class AbstractLatentFeatureGenerator {

	public abstract IFeatureVector getFeatureVector(IInstance x, IStructure y);

}
