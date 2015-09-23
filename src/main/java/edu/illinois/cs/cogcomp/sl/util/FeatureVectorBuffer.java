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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

/**
 * Generate a sparse feature vector.
 * This class is handy when the feature vector is constructed by 
 * summing up several sub-components.
 * 
 * Here is a short example:
 *  	FeatureVector fv1 = new FeatureVector(new int[]{1,2,3}, new float[]{1,2,3});
 *  	FeatureVector fv2 = new FeatureVector(new int[]{4,5,6}, new float[]{4,5,6});
 *  	FeatureVector fv3 = new FeatureVector(new int[]{7,8,9}, new float[]{7,8,9});
 *  	FeatureVectorBuffer fvb = new FeatureVectorBuffer(fv1);
 *  	fvb.addFeature(fv2,3); // shift fv2 by 3 and add to fvb;
 *  	fvb.addFeature(fv3); // add fv3 to fvb
 *  	FeatureVector fRes = fvb.toFeatureVector();
 *  	System.out.println(fRes);
 *  
 *  The above code outputs:
 *  		1:1.0 2:2.0 3:3.0 4:4.0 5:5.0 6:6.0 7:7.0 8:8.0 9:9.0
 * 
 * @author Kai-Wei Chang
 * 
 */

public class FeatureVectorBuffer {
	Logger logger = LoggerFactory.getLogger(FeatureVectorBuffer.class);

	
	/**
	 * the indices of active features.
	 * 
	 * <b> Note that the feature should always start from 1 </b> 0 is preserved
	 * for some special operations. Please use FeatureVectorBuffer to construct 
	 * and shift your feature vector.
	 */

	List<Integer> idxList;

	/**
	 * The values of active features.
	 */
	List<Float> valList;
	
	
	/**
	 * Constructor
	 */
	public FeatureVectorBuffer(){
		idxList = new ArrayList<Integer>();
		valList = new ArrayList<Float>();
	}
	

	/**
	 * Construct a feature buffer by indices and values arrays.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public FeatureVectorBuffer(int[] fIdxArray, double[] fValueArray) {
		assert fIdxArray.length == fValueArray.length;
		idxList = Ints.asList(fIdxArray);
		valList = new ArrayList<Float>();
		for (int i = 0; i < fIdxArray.length; i++) 
			valList.add((float)fValueArray[i]);
	}

	/**
	 * Construct a feature buffer by indices and values arrays.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public FeatureVectorBuffer(int[] fIdxArray, float[] fValueArray) {
		assert fIdxArray.length == fValueArray.length;
		idxList = Ints.asList(fIdxArray);
		valList = Floats.asList(fValueArray);
	}


	/**
	 * Construct a feature buffer by indices and values list.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public <T extends Number> FeatureVectorBuffer(List<Integer> fIdxList, List<T> fValueList) {
		idxList = new ArrayList<Integer>();
		valList = new ArrayList<Float>();
		addFeature(fIdxList, fValueList);
	}
	

	/**
	 * Construct a feature buffer by a feature vector.
	 * @param fv
	 */
	public FeatureVectorBuffer(IFeatureVector fv) {
		idxList= Ints.asList(fv.getIndices());
		valList= Floats.asList(fv.getValues());
	}

	/**
	 * currentVector  = currentVector + fv
	 * @param fv
	 */
	public void addFeature(IFeatureVector fv){
		idxList.addAll(Ints.asList(fv.getIndices()));
		valList.addAll(Floats.asList(fv.getValues()));
	}
	
	/**
	 * currentVector  = currentVector + fvb
	 * @param fvb
	 */
	public void addFeature(FeatureVectorBuffer fvb){
		idxList.addAll(fvb.idxList);
		valList.addAll(fvb.valList);
	}


	/**
	 * currentVector  = currentVector + shift(fv,offset),
	 * where shift(fv, offset) shifts fv by offset.
	 * @param fv
	 */	
	public void addFeature(IFeatureVector fv, int offset){
		for (int i = 0; i < fv.getNumActiveFeatures(); i++) {
			idxList.add(fv.getIdx(i)+offset);
		}
		valList.addAll(Floats.asList(fv.getValues()));
	}
	
	/**
	 * currentVector  = currentVector + shift(fv,offset),
	 * where shift(fv, offset) shifts fv by offset.
	 * @param fv
	 */
	public void addFeature(FeatureVectorBuffer fvb, int offset){
		for (int i = 0; i < fvb.idxList.size(); i++) {
			idxList.add(fvb.idxList.get(i)+offset);
		}
		valList.addAll(fvb.valList);
	}
	
	/**
	 * add feature
	 * @param idx
	 * @param value
	 */
	public void addFeature(int idx, float value){
		idxList.add(idx);
		valList.add(value);
	}
	
	/**
	 * add feature
	 * @param idx
	 * @param value
	 */
	public void addFeature(int idx, double value){
		idxList.add(idx);
		valList.add((float)value);
	}
	

	/**
	 * add features by arrays
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public void addFeature(int[] fIdxArray, float[] fValueArray) {
		idxList.addAll(Ints.asList(fIdxArray));
		valList.addAll(Floats.asList(fValueArray));
	}

	/**
	 * add features by arrays
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public void addFeature(int[] fIdxArray, double[] fValueArray) {
		idxList.addAll(Ints.asList(fIdxArray));
		for (int i = 0; i < fIdxArray.length; i++) {
			valList.add((float)fValueArray[i]);
		}
	}
	
	/**
	 * Add features by lists
	 * @param fIdxList
	 * @param fValueList
	 */
	public <T extends Number> void addFeature(List<Integer> fIdxList,
			List<T> fValueList) {
		idxList.addAll(fIdxList);
		for (int i = 0; i < fValueList.size(); i++) 
			valList.add((float)fValueList.get(i).floatValue());
		
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < idxList.size(); i++) {
			int idx = idxList.get(i);
			float val = valList.get(i);
			sb.append(idx + ":" + val + " ");
		}
		return sb.toString();
	}

	/**
	 * shift the feature vector by offset
	 * 
	 * @param gap
	 * @return
	 */
	public void shift(int offset) {
		for (int i = 0; i < idxList.size(); i++) {
			idxList.set(i, idxList.get(i)+offset);
		}
	}

	
	/**
	 * generate a feature vector by the elements in the buffer 
	 * @return fv
	 */
	public  IFeatureVector toFeatureVector() {
		return toFeatureVector(true);
	}
	public  IFeatureVector toFeatureVector(boolean sorted) {
		if(!sorted)
			return new SparseFeatureVector(Ints.toArray(idxList), Floats.toArray(valList), false);
		if(idxList.size() == 0)
			return new SparseFeatureVector(new int[0], new float[0]);
		// sort items
		Integer[] idxs = new Integer[idxList.size()];
		for(int i = 0; i < idxs.length; i++) idxs[i] = i;
		Arrays.sort(idxs, new Comparator<Integer>(){
			public int compare(Integer o1, Integer o2){
				return Integer.compare(idxList.get(o1), idxList.get(o2));
			}
		});
				
		int preIdx = -1;
			

		if(sorted && idxList.get(idxs[0])< 0) {
			logger.error("Feature vector index should start at 1. Please shift your feature vector "
					+ "index by 1 using shift(int offset) function . See readme for details.");
			throw new IllegalArgumentException("index must be >= 1");
		}
		
		int numNonZeroFeature = 0;
		for(int i =0; i < idxs.length; i ++){
			if(preIdx == idxList.get(idxs[i])){
				continue;
			}
			numNonZeroFeature++;
			preIdx = idxList.get(idxs[i]);
		}
		int[] indices = new int[numNonZeroFeature];
		float[] values = new float[numNonZeroFeature];
		
		numNonZeroFeature = 0;
		preIdx = -1;
		for(int i =0; i < idxList.size(); i ++){
			if(preIdx == idxList.get(idxs[i])){
				values[numNonZeroFeature-1] += valList.get(idxs[i]);
				continue;
			}
			indices[numNonZeroFeature] = idxList.get(idxs[i]);
			values[numNonZeroFeature] =  valList.get(idxs[i]);
			preIdx = idxList.get(idxs[i]);
			numNonZeroFeature++;
		}
		return new SparseFeatureVector(indices, values, sorted);
	}
	
	public static class FeatureItem {
	    public int    index;
	    public final float value;

	    public FeatureItem( final int index, final float value ) {
	        this.index = index;
	        this.value = value;
	    }
	    
		@Override 	
		public boolean equals( Object aThat ) {
			
		    //check for self-comparison
		    if ( this == aThat ) return true;

		    if ( !(aThat instanceof FeatureItem) ) return false;
	
		    //cast to native object is now safe
		    FeatureItem that = (FeatureItem)aThat;

		    //now a proper field-by-field evaluation can be made
		    return this.index == that.index && (Math.abs(this.value -that.value) < 1e-30); 	        
		  }	

		 @Override 
		 public int hashCode() {
		    return (17*37 + index) + (23*37 + Float.floatToIntBits(value));
		 }	
	}
}
