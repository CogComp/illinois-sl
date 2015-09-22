package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class MultiClassInstance implements IInstance {
	public final IFeatureVector baseFv;
	public final int baseNfeature;
	public final int numberOfClasses;
	
	public MultiClassInstance(int total_n_fea,int total_number_class,IFeatureVector base_fv){
		this.baseFv = base_fv;
		this.baseNfeature = total_n_fea; 
		this.numberOfClasses = total_number_class;
	}
}
