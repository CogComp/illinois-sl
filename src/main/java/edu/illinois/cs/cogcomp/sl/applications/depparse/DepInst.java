/*******************************************************************************
 * University of Illinois/NCSA Open Source License
 * Copyright (c) 2010, 
 *
 * Developed by:
 * The Cognitive Computations Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
 * Neither the names of the Cognitive Computations Group, nor the University of Illinois at Urbana-Champaign, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *     
 *******************************************************************************/
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
