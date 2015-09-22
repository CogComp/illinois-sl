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
