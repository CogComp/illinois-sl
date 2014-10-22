package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class MultiClassFeatureGenerator extends AbstractFeatureGenerator {
	/**
	 * This function returns a feature vector \Phi(x,y) based on an instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is the
	 *         output structure
	 */

	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		MultiClassInstance mx = (MultiClassInstance) x;
		LabeledMulticlassStructure my = (LabeledMulticlassStructure)y;
		FeatureVectorBuffer fvb = new FeatureVectorBuffer(mx.base_fv);
		fvb.shift(my.output * mx.base_n_fea);
		return fvb.toFeatureVector();
	}

}
