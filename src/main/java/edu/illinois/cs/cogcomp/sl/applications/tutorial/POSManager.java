package edu.illinois.cs.cogcomp.sl.applications.tutorial;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class POSManager extends AbstractFeatureGenerator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * This function returns a feature vector \Phi(x,y) based on an instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is the
	 *         output structure
	 */
	protected Lexiconer lm = null;	
   
	public POSManager(Lexiconer lm) {		
		this.lm = lm;
	}

	
	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		FeatureVectorBuffer fv = new FeatureVectorBuffer();
		Sentence ins = (Sentence) x;
		int[] tags = ((POSTag) y).tags;
		
		// add emission features
		for (int i = 0; i < ins.tokens.length; i++)
			fv.addFeature(ins.tokens[i] +  lm.getNumOfFeature() * tags[i], 1.0f);

		// add prior features 
		fv.addFeature(lm.getNumOfFeature() * lm.getNumOfLabels() + tags[0], 1.0f);			

		// add transition features
		int priorEmissionOffset = lm.getNumOfFeature() * lm.getNumOfLabels() + lm.getNumOfLabels();
		// calculate transition features
		for (int i = 1; i < ins.tokens.length; i++)
			fv.addFeature(priorEmissionOffset + tags[i - 1] * lm.getNumOfLabels() + tags[i], 1.0f);					
		
		return fv.toFeatureVector(); 		
	}

}
