package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public abstract class AbstractLatentInferenceSolver {

	public abstract IStructure getBestLatentStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;
	public abstract IStructure getLossAugmentedBestStructure(WeightVector weight,
			IInstance ins, IStructure goldStructure) throws Exception;

}
