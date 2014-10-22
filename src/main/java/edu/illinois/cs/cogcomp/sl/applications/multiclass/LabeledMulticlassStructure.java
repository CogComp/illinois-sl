package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;



public class LabeledMulticlassStructure implements IStructure{
	public final MultiClassInstance input;
	public int output = -1;
	
	public LabeledMulticlassStructure(MultiClassInstance x, int y){
		input = x;
		output = y;
		assert output > -1;
	}

	public IFeatureVector getFeatureVector() {
		FeatureVectorBuffer fvb = new FeatureVectorBuffer(input.base_fv);
		fvb.shift(output*input.base_n_fea);
		return fvb.toFeatureVector();
	}

	@Override
	public String toString() {		
		return "" + output + " " + input.base_fv.toString();
	}

	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof LabeledMulticlassStructure))
			return false;

		// cast to native object is now safe
		LabeledMulticlassStructure that = (LabeledMulticlassStructure) aThat;
	

		if (!this.input.equals(that.input))
			return false;
		else {
			if (this.output != that.output)
				return false;
			return true;
		}
	}


	@Override
	public int hashCode() {
		return output + 13 * input.hashCode();
	}

	

}
