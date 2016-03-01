package edu.illinois.cs.cogcomp.sl.applications.latentsvm2;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public abstract class LatentFeatureGenerator extends AbstractFeatureGenerator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * return phi(x,y,h)
	 */
	public IFeatureVector getFeatureVector(IInstance x, IStructure y_h_pair){
		return getLatentFeatureVector(x,(GoldLatentPair) y_h_pair);
	}

	abstract IFeatureVector getLatentFeatureVector(IInstance x,
			GoldLatentPair y_h_pair);

}
