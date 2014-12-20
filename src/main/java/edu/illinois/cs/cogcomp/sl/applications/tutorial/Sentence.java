package edu.illinois.cs.cogcomp.sl.applications.tutorial;


import edu.illinois.cs.cogcomp.sl.core.IInstance;

/**
 * An implementation of IInstance for part-of-speech task 
 * @author kchang10
 */
public class Sentence implements IInstance {

	public final int[] tokens;
	int hashCode = 0;
	public Sentence(int[] tokens) {
		this.tokens = tokens;
		for(int i=0; i< tokens.length; i++){
			hashCode+= tokens.hashCode()*19;
		}
	}

	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < tokens.length; i++)
			sb.append(tokens[i] + "\t");
		sb.append("\n");

		return sb.toString();
	} 
	@Override
	public int hashCode(){
		return hashCode;
	}
}