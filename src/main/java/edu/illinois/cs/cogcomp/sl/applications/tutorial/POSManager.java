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
package edu.illinois.cs.cogcomp.sl.applications.tutorial;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class POSManager extends AbstractFeatureGenerator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * This function returns a feature vector \Phi(x,y) based on an instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is the
	 *         output structure
	 */
	protected Lexiconer lm = null;	
   
	public POSManager(Lexiconer lm) {		
		this.lm = lm;
	}

	
	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		FeatureVectorBuffer fv = new FeatureVectorBuffer();
		Sentence ins = (Sentence) x;
		int[] tags = ((POSTag) y).tags;
		
		// add emission features
		for (int i = 0; i < ins.tokens.length; i++)
			fv.addFeature(ins.tokens[i] +  lm.getNumOfFeature() * tags[i], 1.0f);

		// add prior features 
		fv.addFeature(lm.getNumOfFeature() * lm.getNumOfLabels() + tags[0], 1.0f);			

		// add transition features
		int priorEmissionOffset = lm.getNumOfFeature() * lm.getNumOfLabels() + lm.getNumOfLabels();
		// calculate transition features
		for (int i = 1; i < ins.tokens.length; i++)
			fv.addFeature(priorEmissionOffset + tags[i - 1] * lm.getNumOfLabels() + tags[i], 1.0f);					
		
		return fv.toFeatureVector(); 		
	}

}
