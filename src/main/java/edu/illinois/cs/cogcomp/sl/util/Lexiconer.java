package edu.illinois.cs.cogcomp.sl.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages the lexicon of labels and features.
 * 
 * @author Ming-Wei Chang
 * 
 */
public class Lexiconer implements Serializable {

	private static final long serialVersionUID = 1L;

	// be careful, start from zero
	Map<String, Integer> feaStr2IdMap = null;
	Map<Integer, String> feaId2StrMap = null;

	Map<String, Integer> LabelStr2IdMap = null;
	Map<Integer, String> Id2LabelStrMap = null;

	// It is truly important to have this. look at the comment in the
	// constructor
	public static final String biasStr = "*-global-bias-*";

	private boolean allowNewFeatures = true;

	/**
	 * The constructor: Note that the feature indexes need to be greater than
	 * zero. We preview the 0 feature in the constructor to prevent the user
	 * tries to use feature 0
	 */
	public Lexiconer() {
		feaStr2IdMap = new ConcurrentHashMap<String, Integer>();
		feaId2StrMap = new ConcurrentHashMap<Integer, String>();
		LabelStr2IdMap = new ConcurrentHashMap<String, Integer>();
		Id2LabelStrMap = new ConcurrentHashMap<Integer, String>();

		// VERY IMPORTANT
		// In the current version of the weight vector, we use zero as the
		// global bias
		// term
		// therefore, it is very important to preview the first feature
		// so no one can take over the zero index!
		this.addFeature(biasStr); // zero should always be bias

		// What happened in Structured case?
		// The structure case might have their own lex
		// However, they also need to find a way to keepLexiconer the zero index
		// for the
		// bias term
		// In JLIS-sequence, we use this Lexmanager inside the structural lex
		// manger
		// Therefore, the zero index will still be not used by regular features

	}

	/**
	 * The function that views all of the label names. Should always call this
	 * function before using {@link Lexiconer#hasLabel(String)},
	 * {@link Lexiconer#getLabelId(String)} and
	 * {@link Lexiconer#getLabelId(String)}
	 * 
	 * @param labs
	 */
	public void addLabels(String[] labs) {
		// LabelStr2IdMap = new ConcurrentHashMap<String, Integer>();
		// Id2LabelStrMap = new ConcurrentHashMap<Integer, String>();

		for (String str : labs) {
			if (!LabelStr2IdMap.containsKey(str)) {
				int v = LabelStr2IdMap.size();
				LabelStr2IdMap.put(str, v);
				Id2LabelStrMap.put(v, str);
			}
		}
	}

	public void addLabel(String label) {
		// LabelStr2IdMap = new ConcurrentHashMap<String, Integer>();
		// Id2LabelStrMap = new ConcurrentHashMap<Integer, String>();
		if (!LabelStr2IdMap.containsKey(label)) {
			int v = LabelStr2IdMap.size();
			LabelStr2IdMap.put(label, v);
			Id2LabelStrMap.put(v, label);
		}
	}

	/**
	 * Get the index for this label string
	 * 
	 * @param str
	 *            A label string
	 * @return
	 */
	public int getLabelId(String str) {
		if (!containsLabel((str)))
			System.out.println("!!!!! " + str);

		return LabelStr2IdMap.get(str);
	}

	/**
	 * Get the name of the label
	 * 
	 * @param id
	 * @return
	 */
	public String getLabelString(int id) {
		return Id2LabelStrMap.get(id);
	}

	/**
	 * @return Total number of labels
	 */
	public int getNumOfLabels() {
		return LabelStr2IdMap.size();
	}

	public String getFeatureString(int id) {
		return feaId2StrMap.get(id);
	}

	public int getNumOfFeature() {
		return feaStr2IdMap.size();
	}

	public int getFeatureId(String s) {
		assert feaStr2IdMap.containsKey(s);
		return feaStr2IdMap.get(s);
	}

	public boolean containsLabel(String s) {
		return LabelStr2IdMap.containsKey(s);
	}

	public boolean containFeature(String s) {
		return feaStr2IdMap.containsKey(s);
	}

	/**
	 * Let the LexManager remember this feature
	 * 
	 * @param s
	 */
	public synchronized void addFeature(String s) {
		assert allowNewFeatures == true;

		if (!feaStr2IdMap.containsKey(s)) {
			int v = feaStr2IdMap.size();
			feaStr2IdMap.put(s, v);
			feaId2StrMap.put(v, s);
		}
	}

	public Set<String> allCurrentFeatures() {
		return feaStr2IdMap.keySet();
	}

	/**
	 * Note that only {@link Lexiconer#addFeature(String)} and this function can
	 * let {@link Lexiconer} remember the features and their corresponding
	 * indexes.
	 * 
	 * @param <T>
	 * 
	 * @param exFeatureMap
	 *            The String representations of the feature vector
	 * @return The final FeatureVector.
	 */
	public synchronized <T> IFeatureVector convertToFeatureVector(
			Map<String, T> exFeatureMap) {
		int realSize = 0;

		for (String s : exFeatureMap.keySet()) {
			if (!feaStr2IdMap.containsKey(s) && !allowNewFeatures)
				continue;
			realSize++;
		}

		int[] idx = new int[realSize];
		float[] values = new float[realSize];
		int i = 0;
		for (String s : exFeatureMap.keySet()) {
			if (!feaStr2IdMap.containsKey(s)) {

				if (!allowNewFeatures) {
					continue; // do not create any features..
				}

				int new_id = feaStr2IdMap.size();
				feaStr2IdMap.put(s, new_id);
				feaId2StrMap.put(new_id, s);

			}
			int id = feaStr2IdMap.get(s);

			idx[i] = id;
			T value = exFeatureMap.get(s);
			if (value instanceof Double)
				values[i] = ((Double) value).floatValue();
			else
				values[i] = ((Float) value).floatValue();
			i += 1;
		}
		return new FeatureVectorBuffer(idx, values).toFeatureVector();
	}

	/**
	 * Control if you want the LexManger to create more features.
	 * 
	 * @param allowNewFeatures
	 */
	public void setAllowNewFeatures(boolean allowNewFeatures) {
		this.allowNewFeatures = allowNewFeatures;
	}

	/**
	 * @return return if the lex allows new feature
	 */
	public boolean isAllowNewFeatures() {
		return allowNewFeatures;
	}
}
