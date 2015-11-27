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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.sl.core.SLParameters;

/**
 * Sparse Feature Vector Representation
 * 
 * @author Ming-Wei Chang
 */

public class SparseFeatureVector implements IFeatureVector {
	/**
	 * 
	 */

	public static Logger logger = LoggerFactory.getLogger(SparseFeatureVector.class);

	private static final long serialVersionUID = 1738932616256244560L;

	/**
	 * the indices of active features.
	 * 
	 * <b> Note that the feature should always start from 1 </b> 0 is preserved
	 * for some special operations. Please use FeatureVectorBuffer to construct 
	 * and shift your feature vector.
	 */

	protected int[] indices;

	/**
	 * The values of active features.
	 */
	protected float[] values;


	/*
	 * Store the square l2-norm  of the feature vector;
	 */
	float squareL2Norm;

	/*
	 * Store the max index;
	 */
	int maxIndex=-1;

	int minIndex=Integer.MAX_VALUE;

	boolean sorted = false;


	public SparseFeatureVector(int[] fIdxArray, double[] fValArray){
		this(fIdxArray, fValArray, true);
	}


	/**
	 * Constructor: construct a sparse feature vector by providing an array of indices and 
	 * an array of feature values. The third parameter specifies if the indices are sorted.
	 * In general, using unsorted sparse vector takes less training time. However, some algorithms
	 * converge only when using sorted sparse vector.
	 * If your feature vector is not sorted, or the feature vector is constructed on the fly, 
	 * please use @FeatureVectorBuffer to construct @SparseFeatureVector. 
	 * 
	 * Please note that, we use float (single precision) to store feature values internally.
	 * We allow using double array to construct the feature vector for users' convenience.   
	 *  
	 * @param fIdxArray
	 * @param fValArray
	 * @param sorted
	 */

	public SparseFeatureVector(int[] fIdxArray, double[] fValArray, boolean sorted) {
		this.sorted = sorted;
		if(sorted){
			for(int i=1; i< fIdxArray.length; i++){
				if(fIdxArray[i-1]>=fIdxArray[i]){
					throw new IllegalArgumentException("Please use FeatureVectorBuffer to generate a sorted feature vector. See readme for details. Caused by fIdx:"+i);
				}
			}
		}

		if( fIdxArray.length != fValArray.length){		
			throw new IllegalArgumentException("Length of index array is different from length of value array.");
		}
		if(fIdxArray.length == 0){
			indices = new int[0];
			values = new float[0];
			squareL2Norm = 0;
		}
		else{
			indices = new int[fIdxArray.length];
			values = new float[fValArray.length];
			for (int i = 0; i < fIdxArray.length; i++) {
				indices[i] = fIdxArray[i] & SLParameters.HASHING_MASK;
				values[i] = (float) fValArray[i];
				maxIndex = Math.max(maxIndex, indices[i]);
				minIndex = Math.min(minIndex, indices[i]);
			}
			squareL2Norm = 0;

			for (int i = 0; i < indices.length; i++)
				squareL2Norm += values[i] * values[i];

			if(minIndex < 0){
				throw new IllegalArgumentException("Feature vector index should start at 1. Please use FeatureVectorBuffer"
						+ "to shift your feature vector index by 1. See readme for details.");
			}
		}
	}

	public SparseFeatureVector(int[] fIdxArray, float[] fValArray){
		this(fIdxArray, fValArray, true);
	}
	public SparseFeatureVector(int[] fIdxArray, float[] fValArray, boolean sorted) {
		this.sorted = sorted;
		if(sorted){
			for(int i=1; i< fIdxArray.length; i++){
				if(fIdxArray[i-1]>=fIdxArray[i]){
					throw new IllegalArgumentException("Please use FeatureVectorBuffer to generate a sorted feature vector. See readme for details");
				}			
			}
		}

		if( fIdxArray.length != fValArray.length){		
			throw new IllegalArgumentException("Length of index array is different from length of value array.");
		}
		if(fIdxArray.length == 0){
			indices = new int[0];
			values = new float[0];
			squareL2Norm = 0;
		}
		else{
			indices = new int[fIdxArray.length];
			values = new float[fValArray.length];
			for (int i = 0; i < fIdxArray.length; i++) {
				indices[i] = fIdxArray[i] & SLParameters.HASHING_MASK;
				values[i] = fValArray[i];
				maxIndex = Math.max(maxIndex, indices[i]);
				minIndex = Math.min(minIndex, indices[i]);
			}
			squareL2Norm = 0;

			for (int i = 0; i < indices.length; i++)
				squareL2Norm += values[i] * values[i];

			if(minIndex < 0){
				throw new IllegalArgumentException("Feature vector index should start at 1. Please use FeatureVectorBuffer"
						+ "to shift your feature vector index by 1. See readme for details.");
			}
		}	}



	/**
	 * Element-wise multiply the feature vector by c.
	 * 
	 * @param c
	 */
	public void multiply(float c) {
		for (int i = 0; i < indices.length; i++) {
			values[i] *= c;
		}
		squareL2Norm *= c;
	}

	/**
	 * Get 2-norm of the feature vector
	 * 
	 * @return norm
	 */
	public float getSquareL2Norm() {
		return squareL2Norm;
	}

	/**
	 * Get the largest feature index.
	 * @return MaxIdx
	 */
	public int getMaxIdx() {
		if(indices.length < 1)
			return 0;
		return maxIndex; 
		//indices[indices.length-1];
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indices.length; i++) {
			sb.append(indices[i] + ":" + values[i] + " ");
		}
		return sb.toString();
	}

	/**
	 * return a new vector of (fv1-fv2)
	 * 
	 * @param fv2
	 * @return diff
	 */
	@Override
	public IFeatureVector difference(IFeatureVector fv2) {




		List<Integer> rIdx = new ArrayList<Integer>();
		List<Float> rVal= new ArrayList<Float>();
		int i1 = 0;
		int i2 = 0;

		if(!sorted){
			for(i1=0;i1 < this.indices.length;i1++){
				rIdx.add(this.indices[i1]);
				rVal.add(this.values[i1]);
			}
			for(i1=0;i1 < fv2.getNumActiveFeatures();i1++){
				rIdx.add(fv2.getIdx(i1));
				rVal.add(-fv2.getValue(i1));
			}
		}
		else{


			while (i1 < this.indices.length && i2 < fv2.getNumActiveFeatures()) {
				int idxFv1 = this.indices[i1];
				int idxFv2 = fv2.getIdx(i2);
				float valFv1 = this.values[i1];
				float valFv2 = fv2.getValue(i2);
				if (idxFv1 < idxFv2) {
					rIdx.add(idxFv1);
					rVal.add(valFv1);
					i1++;
				} else if (idxFv1 > idxFv2) {
					rIdx.add(idxFv2);
					rVal.add(-valFv2);
					i2++;
				} else {
					rIdx.add(idxFv1);
					rVal.add(valFv1-valFv2);
					i1++;
					i2++;
				}
			}

			while (i1 < this.indices.length) {
				rIdx.add(this.indices[i1]);
				rVal.add(this.values[i1]);
				i1++;
			}

			while (i2 < fv2.getNumActiveFeatures()) {
				rIdx.add(fv2.getIdx(i2));
				rVal.add(-fv2.getValue(i2));
				i2++;
			}
		}
		int[] resIdx = new int[rIdx.size()];
		float[] resValue = new float[rVal.size()];

		for (int i = 0; i < rIdx.size(); i++) {
			resIdx[i] = rIdx.get(i);
			resValue[i] = rVal.get(i);
		}

		return new SparseFeatureVector(resIdx, resValue, sorted);
	}

	/**
	 * Serialize the feature vector
	 * @return featureVectorBinary
	 * @throws IOException
	 */
	public byte[] serialize()
			throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream oos = new DataOutputStream(baos);
		oos.writeBoolean(sorted);
		oos.writeInt(indices.length);

		for (int i = 0; i < indices.length; i++) {
			oos.writeInt(indices[i]);
			oos.writeFloat(values[i]);
		}

		oos.flush();
		return baos.toByteArray();
	}

	/**
	 * @param array
	 * @return fv
	 * @throws IOException
	 */
	public static SparseFeatureVector deserialize(byte[] array)
			throws IOException {

		DataInputStream in = new DataInputStream(
				new ByteArrayInputStream(array));
		boolean sorted = in.readBoolean();
		int numFeatures = in.readInt();

		int[] featureIds = new int[numFeatures];
		float[] featureValues = new float[numFeatures];

		int count = 0;
		try {
			for (int i = 0; i < numFeatures; i++) {
				featureIds[i] = in.readInt();
				featureValues[i] = in.readFloat();
				count++;
			}
		} catch (EOFException e) {
			System.out.println("Error reading features. Expecting "
					+ numFeatures + ", found " + count);
			throw e;

		}
		return new SparseFeatureVector(featureIds, featureValues, sorted);
	}

	@Override
	public int getIdx(int i) {
		return indices[i];
	}

	@Override
	public float getValue(int i) {
		// TODO Auto-generated method stub
		return values[i];
	}

	@Override
	public int getNumActiveFeatures() {
		// TODO Auto-generated method stub
		return indices.length;
	}
	
	public float[]  getValues(){
		return Arrays.copyOf(values, values.length);
	}
	
	public int[] getIndices(){
		return Arrays.copyOf(indices, indices.length);
	}
}
