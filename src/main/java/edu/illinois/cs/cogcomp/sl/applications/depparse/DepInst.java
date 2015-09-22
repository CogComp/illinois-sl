package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;

/**
 * Represents a sentence which is to be parsed, with some additional features (pos,lemmas)
 * @author Shyam
 *
 */
public class DepInst implements IInstance {
	// all these fields have length # of tokens +1
	public int[] lemmas;
	public int[] lemmasPrefix;
	public int[] pos;
	public int[] cpos;
	
	Map<Integer, String> map = new HashMap<Integer, String>();

	public DepInst(DependencyInstance instance) {
	
		lemmas = new int[instance.lemmas.length];
		lemmasPrefix = new int[instance.lemmas.length];		
		pos = new int[instance.postags.length];
		cpos = new int[instance.cpostags.length];
		for(int i=0; i< instance.lemmas.length; i++)
			lemmas[i] =  encodeString(instance.lemmas[i]);
		
		for(int i=0; i< instance.lemmas.length; i++)
			if(instance.lemmas[i].length()>5)
				lemmasPrefix[i] =  encodeString(instance.lemmas[i].substring(0, 5));
			else
				lemmasPrefix[i] =  encodeString(instance.lemmas[i]);
		
		for(int i=0; i< instance.postags.length; i++)
			pos[i] = encodeString(instance.postags[i]);
	
		for(int i=0; i< instance.cpostags.length; i++)			
			cpos[i] = instance.postags[i].toCharArray()[0]*1023 & SLParameters.HASHING_MASK;
		
	}

	/***
	 * # of tokens in the sentence
	 * @return
	 */
	public int size() {
		return lemmas.length - 1; // this is the true size, after removing the 0
									// root
	}
	public int encodeString(String str){
		int hashcode = 0;
		for(byte c: str.getBytes()){
			hashcode+= c;
			hashcode = hashcode * 191;			
		}
		return hashcode & SLParameters.HASHING_MASK;
	}

}
