package edu.illinois.cs.cogcomp.sl.core;

/**
 * An interface of an input example x. 
 * 
 * @author Ming-Wei Chang
 * 
 */

public interface IInstance {

	/**
	 * Override the toString function. It usually prints the structure
	 * information so that it is easier to debug.
	 * 
	 */
	@Override
	public abstract String toString();

	/**
	 * Tell if two structures are equal or not
	 */	
	@Override
	public abstract boolean equals(Object aThat);

	/**
	 * Hash code for the structure
	 */
	@Override
	public abstract int hashCode();
	
}
