package edu.illinois.cs.cogcomp.sl.applications.tutorial;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class POSTag implements IStructure {
	public final int[] tags;
	public POSTag(int[] tags){
		this.tags = tags;
	}
	
	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;		
		if(this.tags.length != ((POSTag) aThat).tags.length)
			return false;
		//check if their tags are the same
		for(int i=0; i < this.tags.length; i++)
			if (tags[i] != ((POSTag) aThat).tags[i])
				return false;
		return true;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 0;
		for (int i = 0; i < tags.length; i++)
			hashCode = hashCode*27 + tags[i]*17;
		return hashCode;
	}

}
