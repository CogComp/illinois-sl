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
