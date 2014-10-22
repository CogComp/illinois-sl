package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class MulticlassModel extends SLModel{
	private static final long serialVersionUID = -2919690450966535216L;
	
	public MulticlassModel() {		
	}	
	
	public int numOfBaseFeatures; // number of features appeared in the training time
	public Map<String, Integer> labelMapping;

	/**
	 * cost matrix: first dimension is gold lab, the second dimension is prediction lab
	 * cost_matrix[i][i] = 0
	 * cost_matrix[i][j] represents the cost of predicting j while the gold lab is i
	 */	
	public float[][] cost_matrix = null;
		
	public String[] getReverseMapping(){
		String[] reverse = new String[labelMapping.size()];
		for(int i=0; i < reverse.length; i ++){
			reverse[i] = "";
		}
		for(String key:labelMapping.keySet()){
			reverse[labelMapping.get(key)]= key;
		}
		
		for(int i=0; i < reverse.length; i ++){
			assert !reverse[i].equals("");
		}
		return reverse;
	}
}
