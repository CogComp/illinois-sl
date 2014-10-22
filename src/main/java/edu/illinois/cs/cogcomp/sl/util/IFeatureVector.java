package edu.illinois.cs.cogcomp.sl.util;

import java.io.IOException;
import java.io.Serializable;

/**
 * Feature Vector Representation
 * 
 * @author Kai-Wei Chang
 */

public interface IFeatureVector extends Serializable {
			
	/**
	 * Element-wise multiply the feature vector by c.
	 * 
	 * @param c
	 */
	public void multiply(float c);

	/**
	 * Get 2-norm of the feature vector
	 * 
	 * @return norm
	 */
	public float getSquareL2Norm();
	
	/**
	 * Get the largest feature index.
	 * @return MaxIdx
	 */
	public int getMaxIdx();

	/**
	 * Return index of i-th smallest feature
	 * @param i
	 * @return index (int)
	 */
	public int getIdx(int i);
	
	/**
	 * Return value of i-th smallest feature
	 * @param i
	 * @return value (float)
	 */
	public float getValue(int i);
	
	/**
	 * Return number of active features
	 * @return int
	 */
	public int getNumActiveFeatures();
	
	public IFeatureVector difference(IFeatureVector fv1);
	
	/**
	 * Serialize the feature vector
	 * @return featureVectorBinary
	 * @throws IOException
	 */
	public byte[] serialize() throws IOException;
	
}
