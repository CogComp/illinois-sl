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
	
	/*
	 *  Return index array
	 *  @return int[]
	 */
	public int[]  getIndices();
	
	/*
	 *  Return value array
	 *  @return float[]
	 */
	public float[]  getValues();
	
	
}
