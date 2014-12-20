package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import edu.illinois.cs.cogcomp.sl.core.IStructure;



public class MultiClassLabel implements IStructure{
	public int output = -1;
	
	public MultiClassLabel(int y){
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

		if (!(aThat instanceof MultiClassLabel))
			return false;

		// cast to native object is now safe
		MultiClassLabel that = (MultiClassLabel) aThat;
		if (this.output == that.output){
			return true;
		}
		return false;
	}


	@Override
	public int hashCode() {
		return output;
	}

	

}
