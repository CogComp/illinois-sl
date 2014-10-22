package edu.illinois.cs.cogcomp.sl.applications.sequence;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class SequenceLabel implements IStructure {
	protected final SequenceInstance ins;	
	public final int[] tags;
	protected int hashCode = 0;
	IFeatureVector f = null;
	public SequenceLabel(SequenceInstance ins, int[] tags){
		this.tags = tags;
		this.ins = ins;
		
		//compute hashCode by some magic number
		for (int i = 0; i < tags.length; i++){
			hashCode = hashCode*27 + tags[i]*17;
		}
		hashCode += ins.hashCode()*53;
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
		if(ins.hashCode() != that.ins.hashCode())
			return false;

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
		return hashCode;
	}
}
