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
package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class MultiClassModel extends SLModel{
	private static final long serialVersionUID = -2919690450966535216L;
	public final Map<String, Integer> label_mapping;
	public int numFeatures;
	
	public MultiClassModel() {		
		label_mapping = new HashMap<String, Integer>();
	}	
	
	public int numOfBaseFeatures; // number of features appeared in the training time
	public Map<String, Integer> labelMapping;

	/**
	 * cost matrix: first dimension is gold lab, the second dimension is prediction lab
	 * cost_matrix[i][i] = 0
	 * cost_matrix[i][j] represents the cost of predicting j while the gold lab is i
	 */	
	public float[][] cost_matrix = null;
		
	public String[] getReverseMapping(){
		String[] reverse = new String[labelMapping.size()];
		for(int i=0; i < reverse.length; i ++){
			reverse[i] = "";
		}
		for(String key:labelMapping.keySet()){
			reverse[labelMapping.get(key)]= key;
		}
		
		for(int i=0; i < reverse.length; i ++){
			assert !reverse[i].equals("");
		}
		return reverse;
	}
}
