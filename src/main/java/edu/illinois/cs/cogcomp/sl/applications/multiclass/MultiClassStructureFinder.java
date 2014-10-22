package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;


public class MultiClassStructureFinder extends AbstractInferenceSolver{

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
	
	public MultiClassStructureFinder(){
		
	}
	
	public MultiClassStructureFinder(float[][] loss_matrix){
		this.distance_matrix = loss_matrix;
	}

	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception {
		
		MultiClassInstance mi = (MultiClassInstance) ins;
		LabeledMulticlassStructure lmi = (LabeledMulticlassStructure) goldStructure;
		
		int best_output = -1;
		float best_score = Float.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.number_of_class ; i ++){
			LabeledMulticlassStructure cand = new LabeledMulticlassStructure(mi, i);
			float score = weight.dotProduct(cand.getFeatureVector());
			
			if (i != lmi.output){
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
		return new LabeledMulticlassStructure(mi, best_output);		
	}

	@Override
	public IStructure getBestStructure(WeightVector weight,
			IInstance ins) throws Exception {
		MultiClassInstance mi = (MultiClassInstance) ins;
		
		int best_output = -1;
		float best_score = Float.NEGATIVE_INFINITY;
		
		for(int i=0; i < mi.number_of_class ; i ++){
			LabeledMulticlassStructure cand = new LabeledMulticlassStructure(mi, i);
			float score = weight.dotProduct(cand.getFeatureVector());
			if (score > best_score){
				best_output = i;
				best_score = score;
			}
		}
				
		assert best_output >= 0 ;
		return new LabeledMulticlassStructure(mi, best_output);
	}
	public float getLoss(IInstance ins, IStructure gold,  IStructure pred){
		float distance = 0f;
		LabeledMulticlassStructure lmi = (LabeledMulticlassStructure) gold;
		LabeledMulticlassStructure pmi = (LabeledMulticlassStructure) pred;
		if (pmi.output != lmi.output){
		    if(distance_matrix == null)
		    	distance = 1.0f;
		    else
		    	distance = distance_matrix[lmi.output][pmi.output];		    
		}
		return distance;
	}

}
