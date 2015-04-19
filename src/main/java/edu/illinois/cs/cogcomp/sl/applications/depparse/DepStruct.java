package edu.illinois.cs.cogcomp.sl.applications.depparse;

import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class DepStruct implements IStructure{

	public int[] heads; // pos of heads of ith token is heads[i]
	public String[] deprels;

	public DepStruct(DependencyInstance instance) {
		heads = instance.heads;
		deprels = instance.deprels;
	}
	public DepStruct(int sent_size)
	{
		heads=new int[sent_size+1];
		heads[0]=-1;
		deprels=new String[sent_size+1];
	}
	public DepStruct(int[] heads, String[] deprels){
		this.heads=heads;
		this.deprels=deprels;
	}
}
