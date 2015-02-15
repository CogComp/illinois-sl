package edu.illinois.cs.cogcomp.sl.applications.latentsvm2;

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public abstract class AbstractLatentInferenceSolver extends AbstractInferenceSolver{

	@Override
	public IStructure getBestStructure(WeightVector weight, IInstance ins)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/***
	 * should return GoldLatentPair
	 */
	@Override
	public abstract IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;

	// TODO: abstract
	public float getLossWithLatent(IInstance ins,IStructure gold,IStructure goldLatentPair)
	{
		return 0;
	}
	@Override
	public float getLoss(IInstance ins, IStructure gold, IStructure pred) {
		// TODO Auto-generated method stub
		return 0;
	}

}
