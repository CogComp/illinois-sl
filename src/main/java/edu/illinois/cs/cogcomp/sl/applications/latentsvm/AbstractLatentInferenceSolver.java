package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public abstract class AbstractLatentInferenceSolver extends AbstractInferenceSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * computes best explaining latent structure (the latent variable completion
	 * step) returns argmax_h w^T \phi(x_i,y_i,h)
	 * 
	 * @param weight
	 * @param ins
	 * @param goldStructure
	 * @return
	 * @throws Exception
	 */
	public abstract IStructure getBestLatentStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;

	
	/***
	 * returns argmax_(y,h) w^T \phi(x_i,y,h) + Delta(y,y_i). Note that y and h combine to create a single IStructure
	 * 
	 * @param weight
	 * @param ins
	 * @param goldStructure
	 * @return
	 * @throws Exception
	 */
	public abstract IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception;

}
