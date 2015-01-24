package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public abstract class AbstractLatentInferenceSolver {

	/***
	 * computes best explaining latent structure
	 * returns argmax_h w^T \phi(x_i,y_i,h)
	 * @param weight
	 * @param ins
	 * @param goldStructure
	 * @return
	 * @throws Exception
	 */
	public abstract IStructure getBestLatentStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;
	
	public abstract IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;

}
