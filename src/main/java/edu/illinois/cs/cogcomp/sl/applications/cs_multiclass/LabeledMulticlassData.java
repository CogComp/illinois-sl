package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.SLProblem;


public class LabeledMulticlassData{
	/**
	 * In the training data, the first token of a line represents a class
	 * "string". It does not need to be a number. Later internally this mapping
	 * will map the class string into the corresponding class index.
	 * 
	 * At training time, this information comes from the training data.
	 * 
	 * At test time, this information comes from the one built in the training
	 * data
	 */
	public final Map<String, Integer> label_mapping;

	/**
	 * Number of features used in \phi(x) (later we will convert it to \phi(x,y)
	 * by shifting \phi(x) using n_base_feature_in_train*y. This number will be
	 * fixed after reading the training data. The feature index exceeds the
	 * n_train_base_feature in the testing data will automatically be discarded.
	 * 
	 * At training time, this information comes from the training data.
	 * 
	 * At test time, this information comes from the one built in the training
	 * data
	 */
	public final int n_base_feature_in_train;

	public final SLProblem sp;
	
	public LabeledMulticlassData(Map<String, Integer> m, Integer n_fea) {		
		label_mapping = m;
		n_base_feature_in_train = n_fea;
		sp = new SLProblem();
	}	
	
	public LabeledMulticlassData(Map<String, Integer> m, Integer n_fea, SLProblem in_sp)
	{
		label_mapping = m;
		n_base_feature_in_train = n_fea;
		sp = in_sp;
	}
}
