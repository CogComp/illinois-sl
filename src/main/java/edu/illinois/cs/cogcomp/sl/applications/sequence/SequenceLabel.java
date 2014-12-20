package edu.illinois.cs.cogcomp.sl.applications.sequence;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

/**
 * Tag sequence 
 * @author kchang10
 */

public class SequenceLabel implements IStructure {	
	public final int[] tags;
	IFeatureVector f = null;
	
	public SequenceLabel(int[] tags){
		this.tags = tags;
	}
	
	@Override
	public boolean equals(Object aThat) {
		
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof SequenceLabel))
			return false;		

		// cast to native object is now safe
		SequenceLabel that = (SequenceLabel) aThat;

		//check if their tags are the same
		for(int i=0; i < this.tags.length; i ++){
			if (tags[i] != that.tags[i]){
				return false;
			}
		}

		return true;
	}
	@Override
	public int hashCode(){
		int hashCode = 0;
		for (int i = 0; i < tags.length; i++){
			hashCode = hashCode*27 + tags[i]*17;
		}
		return hashCode;
	}
}
