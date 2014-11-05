package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;



public class MulticlassLabel implements IStructure{
	public int output = -1;
	
	public MulticlassLabel(int y){
		output = y;
		assert output > -1;
	}


	@Override
	public String toString() {		
		return "" + output;
	}

	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof MulticlassLabel))
			return false;

		// cast to native object is now safe
		MulticlassLabel that = (MulticlassLabel) aThat;
		if (this.output != that.output)
			return false;
		return true;
	}


	@Override
	public int hashCode() {
		return output;
	}

	

}
