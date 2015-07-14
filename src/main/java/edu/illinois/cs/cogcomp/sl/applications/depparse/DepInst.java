package edu.illinois.cs.cogcomp.sl.applications.depparse;



import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.applications.depparse.base.DependencyInstance;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

/**
 * represents a sentence which is to be parsed, with some additional features (pos,lemmas)
 * @author Shyam
 *
 */
public class DepInst implements IInstance {
	// all these fields have length # of tokens +1
	private static final int posCode = 59359;
	public int[] lemmas;
	public int[] pos;
	public int[] cpos;
	Map<Integer, String> map = new HashMap<Integer, String>();

	public DepInst(DependencyInstance instance) {
		lemmas = new int[instance.lemmas.length];
		pos = new int[instance.postags.length];
		cpos = new int[instance.cpostags.length];
		for(int i=0; i< instance.lemmas.length; i++)
			lemmas[i] = instance.lemmas[i].hashCode();// & SLParameters.HASHING_MASK;			
		for(int i=0; i< instance.postags.length; i++)
			pos[i] = (instance.postags[i].hashCode()*posCode);// & SLParameters.HASHING_MASK;
		for(int i=0; i< instance.cpostags.length; i++)
			cpos[i] = (1023+instance.cpostags[i].hashCode());// & SLParameters.HASHING_MASK;
	}

	/***
	 * # of tokens in the sentence
	 * @return
	 */
	public int size() {
		return lemmas.length - 1; // this is the true size, after removing the 0
									// root
	}

}
