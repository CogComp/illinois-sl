package edu.illinois.cs.cogcomp.sl.applications.tutorial;


import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

/**
 * An implementation of IInstance for sequential tagging task 
 * @author kchang10
 */
public class Sentence implements IInstance {

	public final int[] tokens;	// we remember the ids of the words in the vocab. The ids are maintained by a lexiconer
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