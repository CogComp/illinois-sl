package edu.illinois.cs.cogcomp.sl.core;

/**
 * interface of a structure (y). In the POS tagging example, this class represents the POS tags.
 * 
 * @author Cogcomp @ UI
 * 
 */
public interface IStructure {
	
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
