package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class MultiClassInstance implements IInstance {
	public final IFeatureVector base_fv;
	public final int base_n_fea;
	public final int number_of_class;
	
	public MultiClassInstance(int total_n_fea,int total_number_class,IFeatureVector base_fv){
		this.base_fv = base_fv;
		this.base_n_fea = total_n_fea; 
		this.number_of_class = total_number_class;
	}
}
