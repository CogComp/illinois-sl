package edu.illinois.cs.cogcomp.sl.core;

import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public abstract class AbstractFeatureGenerator {

	/**
	 * Feature generating function
	 * @return a feature vector based on an instance-structure pair (x,y).
	 */

	public abstract IFeatureVector getFeatureVector(IInstance x, IStructure y);

	
	/***
	 * Computes wv^T\phi(x,y).
	 * Override this function if you have a faster implementation for computing
	 * wv^T\phi(x,y).
	 * @param wv
	 * @param x
	 * @param y
	 * @return
	 */
	public float decisionValue(WeightVector wv, IInstance x, IStructure y) {
		return wv.dotProduct(getFeatureVector(x, y));
	}
}
