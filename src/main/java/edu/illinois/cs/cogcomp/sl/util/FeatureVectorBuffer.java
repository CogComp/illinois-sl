package edu.illinois.cs.cogcomp.sl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1738932616256244560L;
	
	Logger logger = LoggerFactory.getLogger(FeatureVectorBuffer.class);

	
	/**
	 *  storage of feature elements
	 *  Note that we sum up the feature elements with the same index
	 *  only when transfer it to FeatureVector.
	 */
	List<FeatureItem> featureItemList;
	
	/**
	 * the indices of active features.
	 * 
	 * <b> Note that the feature should always start from 1 </b> 0 is preserved
	 * for some special operations. Please use FeatureVectorBuffer to construct 
	 * and shift your feature vector.
	 */

	int[] indices;

	/**
	 * The values of active features.
	 */
	float[] values;
	
	/**
	 * Constructor
	 */
	public FeatureVectorBuffer(){
		featureItemList = new ArrayList<FeatureVectorBuffer.FeatureItem>();
	}
	

	/**
	 * Construct a feature buffer by indices and values arrays.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public FeatureVectorBuffer(int[] fIdxArray, double[] fValueArray) {
		assert fIdxArray.length == fValueArray.length;
		featureItemList = new ArrayList<FeatureItem>();
		addFeature(fIdxArray, fValueArray);
	}

	/**
	 * Construct a feature buffer by indices and values arrays.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public FeatureVectorBuffer(int[] fIdxArray, float[] fValueArray) {
		assert fIdxArray.length == fValueArray.length;
		featureItemList = new ArrayList<FeatureItem>();
		addFeature(fIdxArray, fValueArray);
	}


	/**
	 * Construct a feature buffer by indices and values list.
	 * Note that we do not validate the feature indices here.
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public <T extends Number> FeatureVectorBuffer(List<Integer> fIdxList, List<T> fValueList) {
		assert fIdxList.size() == fValueList.size();
		featureItemList = new ArrayList<FeatureItem>();
		addFeature(fIdxList, fValueList);
	}
	

	/**
	 * Construct a feature buffer by a feature vector.
	 * @param fv
	 */
	public FeatureVectorBuffer(IFeatureVector fv) {
		featureItemList = new ArrayList<FeatureItem>();
		for (int i = 0; i < fv.getNumActiveFeatures(); i++) {
			featureItemList.add(new FeatureItem(fv.getIdx(i), fv.getValue(i)));
		}
	}

	/**
	 * currentVector  = currentVector + fv
	 * @param fv
	 */
	public void addFeature(IFeatureVector fv){
		for (int i = 0; i < fv.getNumActiveFeatures(); i++) {
			featureItemList.add(new FeatureItem(fv.getIdx(i), fv.getValue(i)));
		}
	}
	
	/**
	 * currentVector  = currentVector + fvb
	 * @param fvb
	 */
	public void addFeature(FeatureVectorBuffer fvb){
		for (int i = 0; i < fvb.featureItemList.size(); i++) {
			FeatureItem item = fvb.featureItemList.get(i);
			featureItemList.add(new FeatureItem(item.index, item.value));
		}
	}


	/**
	 * currentVector  = currentVector + shift(fv,offset),
	 * where shift(fv, offset) shifts fv by offset.
	 * @param fv
	 */	
	public void addFeature(IFeatureVector fv, int offset){
		for (int i = 0; i < fv.getNumActiveFeatures(); i++) {
			featureItemList.add(new FeatureItem(fv.getIdx(i)+offset, fv.getValue(i)));
		}
	}
	
	/**
	 * currentVector  = currentVector + shift(fv,offset),
	 * where shift(fv, offset) shifts fv by offset.
	 * @param fv
	 */
	public void addFeature(FeatureVectorBuffer fvb, int offset){
		for (int i = 0; i < fvb.featureItemList.size(); i++) {
			FeatureItem item = fvb.featureItemList.get(i);
			featureItemList.add(new FeatureItem(item.index+offset, item.value));
		}
	}
	
	/**
	 * add feature
	 * @param idx
	 * @param value
	 */
	public void addFeature(int idx, float value){
		featureItemList.add(new FeatureItem(idx, value));
	}
	
	/**
	 * add feature
	 * @param idx
	 * @param value
	 */
	public void addFeature(int idx, double value){
		featureItemList.add(new FeatureItem(idx, (float)value));
	}
	

	/**
	 * add features by arrays
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public void addFeature(int[] fIdxArray, float[] fValueArray) {
		for (int i = 0; i < fIdxArray.length; i++) {
			featureItemList.add(new FeatureItem(fIdxArray[i], fValueArray[i]));
		}
	}

	/**
	 * add features by arrays
	 * @param fIdxArray
	 * @param fValueArray
	 */
	public void addFeature(int[] fIdxArray, double[] fValueArray) {
		for (int i = 0; i < fIdxArray.length; i++) {
			featureItemList.add(new FeatureItem(fIdxArray[i], (float)fValueArray[i]));
		}
	}
	
	/**
	 * Add features by lists
	 * @param fIdxList
	 * @param fValueList
	 */
	public <T extends Number> void addFeature(List<Integer> fIdxList,
			List<T> fValueList) {
		for (int i = 0; i < fIdxList.size(); i++) {
			featureItemList.add(new FeatureItem(fIdxList.get(i), fValueList.get(i).floatValue()));
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < featureItemList.size(); i++) {
			int idx = featureItemList.get(i).index;
			float val = featureItemList.get(i).value;
			
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
		for (int i = 0; i < featureItemList.size(); i++) {
			featureItemList.get(i).index = featureItemList.get(i).index + offset; 
		}
	}

	
	/**
	 * generate a feature vector by the elements in the buffer 
	 * @return fv
	 */
	public  IFeatureVector toFeatureVector() {
		Collections.sort(featureItemList, new Comparator<FeatureItem>() {
			// @Override
			@Override
			public int compare(FeatureItem o1, FeatureItem o2) {
				if (o1.index < o2.index)
					return -1;
				else if (o1.index > o2.index)
					return 1;
				else
					return 0;
			}
		});
		
		int preIdx = -1;
		if(featureItemList.size() ==0)
			return new SparseFeatureVector(new int[0], new float[0]);

		if(featureItemList.get(0).index < 0) {
			logger.error("Feature vector index should start at 1. Please shift your feature vector "
					+ "index by 1 using shift(int offset) function . See readme for details.");
			throw new IllegalArgumentException("index must be >= 1");
		}
		
		int numNonZeroFeature = 0;
		for(int i =0; i < featureItemList.size(); i ++){
			if(preIdx == featureItemList.get(i).index){
				continue;
			}
			numNonZeroFeature++;
			preIdx = featureItemList.get(i).index;
		}
		int[] indices = new int[numNonZeroFeature];
		float[] values = new float[numNonZeroFeature];
		
		numNonZeroFeature = 0;
		preIdx = -1;
		for(int i =0; i < featureItemList.size(); i ++){
			if(preIdx == featureItemList.get(i).index){
				values[numNonZeroFeature-1] += featureItemList.get(i).value;
				continue;
			}
			indices[numNonZeroFeature] = featureItemList.get(i).index;
			values[numNonZeroFeature] = featureItemList.get(i).value;
			preIdx = featureItemList.get(i).index;
			numNonZeroFeature++;
		}
		return new SparseFeatureVector(indices, values);
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
