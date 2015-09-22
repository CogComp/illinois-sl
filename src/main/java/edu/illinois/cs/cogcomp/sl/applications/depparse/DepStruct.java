package edu.illinois.cs.cogcomp.sl.applications.depparse;

import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

/**
 * represents a dependency tree, where head[i] is the head of the ith token in
 * the sentence(tokens are indexed from 1..n). Deprels[i] contains the label for
 * the edge from token i to its head. 
 * 
 * @author Shyam
 *
 */
public class DepStruct implements IStructure {

	public int[] heads; // pos of heads of ith token is heads[i]

	public DepStruct(DependencyInstance instance) {
		heads = instance.heads;
	}

	public DepStruct(int sent_size) {
		heads = new int[sent_size + 1];
		heads[0] = -1;
	}

	public DepStruct(int[] heads) {
		this.heads = heads;
	}
	
	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;		
		if(this.heads.length != ((DepStruct) aThat).heads.length)
			return false;
		if(this.hashCode() != ((DepStruct) aThat).hashCode())
			return false;
		//check if their tags are the same
		for(int i=0; i < this.heads.length; i++)
			if(heads[i]!=((DepStruct) aThat).heads[i])
				return false;
		return true;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 0;
		for (int i = 0; i < heads.length; i++)
			hashCode = hashCode*31 + heads[i];
		return hashCode;
	}

}
