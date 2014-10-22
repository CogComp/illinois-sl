package edu.illinois.cs.cogcomp.sl.applications.sequence;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.applications.tutorial.POSTag;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * This class is an implementation of AbstractInferenceSolver for sequential
 * tagging tasks with Hamming loss. The inference problem is solved by Viterbi
 * algorithm.
 * 
 * @author kchang10
 *
 */
public class SequenceInferenceSolver extends
		AbstractInferenceSolver {

	private static final long serialVersionUID = 1L;	

	@Override
	public Object clone(){
		return new SequenceInferenceSolver();
	}
	
	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance input, IStructure gold)
			throws Exception {
		SequenceLabel goldLabeledSeq = (SequenceLabel) gold;
		
		// initialization
		SequenceInstance sen = (SequenceInstance) input;
		
		int numOflabels = SequenceIOManager.numLabels;
		int numOfTokens = sen.baseFeatures.length;
		int numOfEmissionFeatures = SequenceIOManager.numFeatures;
		
		double[][] emissionScores = new double[numOfTokens][numOflabels];
		double[] priorScores = new double[numOflabels];
		double[][] transitionScores = new double[numOflabels][numOflabels];
		double[][] dpTable = new double[numOfTokens][numOflabels];
		int[][] path = new int[numOfTokens][numOflabels];

		// fill in the score tables 
		
		// emission score table
		for (int i = 0; i < numOfTokens; i++) {
			IFeatureVector fv = sen.baseFeatures[i]; 
			for (int j = 0; j < numOflabels; j++) {				
				emissionScores[i][j] = wv.dotProduct(fv, j*numOfEmissionFeatures);				
			}
		}
		
		
		// We only have one transition feature to identify if tag_{i-1}->tag_{i} appears
		IFeatureVector transFv = new SparseFeatureVector(new int[]{1}, new float[]{1.0f});

		// fill in prior score table
		int offset = numOfEmissionFeatures * numOflabels;
		for (int j = 0; j < numOflabels; j++) {
			priorScores[j] = wv.dotProduct(transFv, offset);
			offset += 1;
		}

		// fill in transition score table
		if(numOfTokens>1){
			for (int k = 0; k < numOflabels; k++) {
				for (int j = 0; j < numOflabels; j++) {
					int tr_s = offset + (j * numOflabels + k);						
					transitionScores[k][j] = wv.dotProduct(transFv, tr_s);
				}
			}
		}
		
		// for loss augmented inference, we add Hamming loss into the score table
		if(gold != null) {
			for (int i = 0; i < numOfTokens; i++) {
				for (int j = 0; j < numOflabels; j++) {
					if (j != goldLabeledSeq.tags[i])
						emissionScores[i][j] += 1.0; // Hamming loss
				}
			}
		}		
		
		// Viterbi algorithm
		for (int j = 0; j < numOflabels; j++) {
			double priorScore = priorScores[j];
			double zeroOrderScore = emissionScores[0][j];
			dpTable[0][j] = priorScore + zeroOrderScore;
			path[0][j] = -1;
		}
		for (int i = 1; i < numOfTokens; i++) {
			for (int j = 0; j < numOflabels; j++) {
				double zeroOrderScore = emissionScores[i][j];
				double bestScore = Double.NEGATIVE_INFINITY;
				for (int k = 0; k < numOflabels; k++) {
					double candidateScore = dpTable[i - 1][k]
							+  transitionScores[j][k];
					if (candidateScore > bestScore) {
						bestScore = candidateScore;
						path[i][j] = k;
					}
				}
				dpTable[i][j] = zeroOrderScore + bestScore;
			}
		}
		
		// find the best sequence		
		int[] tags = new int[numOfTokens];
		double maxScore = Double.NEGATIVE_INFINITY;
		int maxTag = -1;

		for (int i = 0; i < numOflabels; i++) {
			if (dpTable[numOfTokens - 1][i] > maxScore) {
				maxScore = dpTable[numOfTokens - 1][i];
				maxTag = i;
			}
		}
		tags[numOfTokens - 1] = maxTag;

		int curTag = maxTag;
		for (int i = numOfTokens - 1; i >= 1; i--) {
			curTag = path[i][curTag]; // trace back one step;
			tags[i - 1] = curTag;
		}
		return new SequenceLabel(sen, tags);
	}

	@Override
	public IStructure getBestStructure(WeightVector wv,
			IInstance input) throws Exception {
		return getLossAugmentedBestStructure(wv, input, null);
	}
	@Override
	public float getLoss(IInstance ins, IStructure goldStructure,  IStructure structure){
		POSTag goldLabeledSeq = (POSTag) goldStructure;
		float loss = 0;
		for (int i = 0; i < goldLabeledSeq.tags.length; i++)
			if (((POSTag) structure).tags[i] != goldLabeledSeq.tags[i])
				loss += 1.0f;
		return loss;
	}

}
