package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.SLProblem;


public class LabeledMultiClassData extends SLProblem{
	/**
	 * The label mapping
	 */
	public final Map<String, Integer> labelMapping;

	/**
	 * Number of total features
	 */
	public final int numFeatures;

	
	public LabeledMultiClassData(Map<String, Integer> m, Integer numfeatures) {		
		labelMapping = m;
		numFeatures = numfeatures;
	}
}
