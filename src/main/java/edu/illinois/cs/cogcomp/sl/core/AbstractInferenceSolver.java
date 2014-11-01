package edu.illinois.cs.cogcomp.sl.core;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * The inference procedure that is used for finding the best structure. This is
 * the place to put the code for calculating argmax. 
 * 
 * You need to implement two inference procedures. One for argmax (extended from this class) and another
 * one for loss-sensitive argmax procedure.
 * 
 * @author Ming-Wei Chang
 * 
 */
public abstract class AbstractInferenceSolver implements Cloneable, Serializable{
	private static final long serialVersionUID = 5233172041125345036L;
	static Logger logger = LoggerFactory.getLogger(AbstractInferenceSolver.class);   

	/**
	 * the inference procedure of solving: <p>
	 * 
	 * \max_{y} w^T \phi(x,y) <p>
	 * 
	 * Note that x is the input example (ins). The return is the best structure
	 * for this example.<p>
	 * 
	 * At test time, this function is usually used to predict the final structure.
	 * 
	 * @param weight
	 *            The weight vector that is used for finding the best structure
	 * @param ins
	 *            The input example
	 * @return The best structure for this input example
	 * @throws Exception
	 */
	public abstract IStructure getBestStructure(WeightVector weight,
			IInstance ins) throws Exception;
	
	/**
	 * the inference procedure of solving:
	 * <p>
	 * 
	 * \max_{y} w^T \phi(x,y) + \delta(y,y*)
	 * <p>
	 * 
	 * where the y* is the gold structure for this example. The function \delta
	 * is the distance function between two structures (y,y*).
	 * <p>
	 * 
	 * This inference procedure is used for finding the structure that violates
	 * the constraint the most.
	 * <p>
	 * 
	 * @param weight
	 *            The weight vector that is used for finding the best structure
	 * @param ins
	 *            The input example
	 * @param goldStructure
	 *            The gold structure for this example
	 * @return A {@link Pair} of {@link IStructure} and {@link Double}: The
	 *         {@link IStructure} contains the structure that violates the
	 *         constraints the most. The {@link Double} represents the distance
	 *         (delta) between the returning structure (y) and the gold
	 *         structure (y*) for this example.
	 * @throws Exception
	 */
	public abstract IStructure getLossAugmentedBestStructure(
			WeightVector weight, IInstance ins, IStructure goldStructure)
			throws Exception;
	
	/**
	 * Get loss of predicted structure
	 * 
	 * @param ins
	 *            The input sample
	 * @param gold
	 *            The gold output structure 
	 * @param pred
	 *            The predicted Structure
	 */
	public abstract float getLoss(IInstance ins, IStructure gold, IStructure pred);
	
	/**
	 * Please implement the clone function if you want to use multiple CPU cores.
	 */
	@Override
	public Object clone(){
		logger.error("Fatal Error: you must overrid the clone method of inference "
	+ "solver, or provide an array of inference solvers (see README for details.)");
		return null;
	}


}
