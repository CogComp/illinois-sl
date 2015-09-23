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

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;


public class MultiClassInferenceSolver extends AbstractInferenceSolver{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * lossMatrix: first dimension is gold, the second dimension is prediction
	 * lossMatrix[i][i] = 0
	 * lossMatrix[i][j] represents the cost of predict j while the gold lab is i
	 */	
	public float[][] lossMatrix = null;
	
	public MultiClassInferenceSolver(float[][] lossMatrix){
		this.lossMatrix = lossMatrix;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		
		MultiClassInstance mi = (MultiClassInstance) ins;
		MultiClassLabel lmi = (MultiClassLabel) goldStructure;
		
		int bestOutput = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.numberOfClasses ; i ++){
			float score = weight.dotProduct(mi.baseFv,mi.baseNfeature*i);
			
			if (lmi!=null && i != lmi.output){
				if(lossMatrix == null)
					score += 1.0;
				else
					score += lossMatrix[lmi.output][i];
			}								
			
			if (score > bestScore){
				bestOutput = i;
				bestScore = score;
			}
		}
					
		assert bestOutput >= 0 ;
		return new MultiClassLabel(bestOutput);		
	}

	@Override
	public IStructure getBestStructure(WeightVector weight,
			IInstance ins) throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}
	
	@Override
	public float getLoss(IInstance ins, IStructure gold,  IStructure pred){
		float loss = 0f;
		MultiClassLabel lmi = (MultiClassLabel) gold;
		MultiClassLabel pmi = (MultiClassLabel) pred;
		if (pmi.output != lmi.output){
		    if(lossMatrix == null)
		    	loss = 1.0f;
		    else
		    	loss = lossMatrix[lmi.output][pmi.output];		    
		}
		return loss;
	}
	
	@Override
	public Object clone(){
		return new MultiClassInferenceSolver(lossMatrix);
	}
}
