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

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * An implementation of the Viterbi algorithm
 * @author kchang10
 */
public class ViterbiInferenceSolver extends
		AbstractInferenceSolver {

	private static final long serialVersionUID = 1L;	
	protected Lexiconer lm = null;	

	public ViterbiInferenceSolver(Lexiconer lm) {		
		this.lm = lm;
	}
	
	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance input, IStructure gold)
			throws Exception {
		assert lm.isAllowNewFeatures() == false;
		POSTag goldLabeledSeq = (POSTag) gold;
		// initialization
		Sentence sen = (Sentence) input;
		
		int numOflabels = lm.getNumOfLabels();
		int numOfTokens = sen.tokens.length;
		int numOfEmissionFeatures = lm.getNumOfFeature();
				
		float[][] dpTable = new float[2][numOflabels];
		int[][] path = new int[numOfTokens][numOflabels]; 

		
		int offset = (numOfEmissionFeatures+1) * numOflabels;
		
		// Viterbi algorithm
		for (int j = 0; j < numOflabels; j++) {
			float priorScore = wv.get(numOfEmissionFeatures * numOflabels + j);
			float zeroOrderScore =  wv.get(sen.tokens[0] + j*numOfEmissionFeatures) +
					((gold !=null && j != goldLabeledSeq.tags[0])?1:0);
			dpTable[0][j] = priorScore + zeroOrderScore; 	 
			path[0][j] = -1;
		}
		
		for (int i = 1; i < numOfTokens; i++) {
			for (int j = 0; j < numOflabels; j++) {
				float zeroOrderScore = wv.get(sen.tokens[i] + j*numOfEmissionFeatures)
						+ ((gold!=null && j != goldLabeledSeq.tags[i])?1:0);
				
				float bestScore = Float.NEGATIVE_INFINITY;
				for (int k = 0; k < numOflabels; k++) {
					float candidateScore = dpTable[(i-1)%2][k] +  wv.get(offset + (k * numOflabels + j));
					if (candidateScore > bestScore) {
						bestScore = candidateScore;
						path[i][j] = k;
					}
				}
				dpTable[i%2][j] = zeroOrderScore + bestScore;
			}
		}
		
		// find the best sequence		
		int[] tags = new int[numOfTokens];
		
		int maxTag = 0;
		for (int i = 0; i < numOflabels; i++)
			if (dpTable[(numOfTokens - 1)%2][i] > dpTable[(numOfTokens - 1)%2][maxTag]) 
				maxTag = i;
		
		tags[numOfTokens - 1] = maxTag;
		
		for (int i = numOfTokens - 1; i >= 1; i--) 
			tags[i-1] = path[i][tags[i]];
		
		return new POSTag(tags);
	}
	
	@Override
	public float getLoss(IInstance ins, IStructure goldStructure,  IStructure structure){
		POSTag goldLabeledSeq = (POSTag) goldStructure;
		float loss = 0;
		for (int i = 0; i < goldLabeledSeq.tags.length; i++)
			if (((POSTag) structure).tags[i] != goldLabeledSeq.tags[i])
				loss += 1.0f;
		return loss;
	}

	@Override
	public IStructure getBestStructure(WeightVector wv,
			IInstance input) throws Exception {
		return getLossAugmentedBestStructure(wv, input, null);
	}

	@Override
	public Object clone(){
		return new ViterbiInferenceSolver(lm);
	}
}
