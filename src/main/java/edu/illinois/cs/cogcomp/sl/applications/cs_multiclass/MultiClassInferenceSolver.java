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
	 * loss matrix: first dimension is gold, the second dimension is prediction
	 * loss_matrix[i][i] = 0
	 * loss_matrix[i][j] represents the cost of predict j while the gold lab is i
	 */	
	public float[][] distance_matrix = null;
	
	public MultiClassInferenceSolver(){
		
	}
	
	public MultiClassInferenceSolver(float[][] loss_matrix){
		this.distance_matrix = loss_matrix;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		
		MultiClassInstance mi = (MultiClassInstance) ins;
		MulticlassLabel lmi = (MulticlassLabel) goldStructure;
		
		int best_output = -1;
		float best_score = Float.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.number_of_class ; i ++){
			float score = weight.dotProduct(mi.base_fv,mi.base_n_fea*i);
			
			if (lmi!=null && i != lmi.output){
				if(distance_matrix == null)
					score += 1.0;
				else{
					score += distance_matrix[lmi.output][i];
				}
			}								
			
			if (score > best_score){
				best_output = i;
				best_score = score;
			}
		}
					
		assert best_output >= 0 ;
		return new MulticlassLabel(best_output);		
	}

	@Override
	public IStructure getBestStructure(WeightVector weight,
			IInstance ins) throws Exception {
		return getLossAugmentedBestStructure(weight, ins, null);
	}
	public float getLoss(IInstance ins, IStructure gold,  IStructure pred){
		float distance = 0f;
		MulticlassLabel lmi = (MulticlassLabel) gold;
		MulticlassLabel pmi = (MulticlassLabel) pred;
		if (pmi.output != lmi.output){
		    if(distance_matrix == null)
		    	distance = 1.0f;
		    else
		    	distance = distance_matrix[lmi.output][pmi.output];		    
		}
		return distance;
	}

}
