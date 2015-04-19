package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

/**
 * generates features based on edges in the dep. graph
 * 
 * @author upadhya3
 *
 */
public class DepFeatureGenerator extends AbstractFeatureGenerator implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8246812640996043593L;
	private Lexiconer lm;

	public DepFeatureGenerator(Lexiconer lm) {
		this.lm = lm;
	}

	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		DepInst sent = (DepInst) x;
		DepStruct tree = (DepStruct) y;
		return extractFeatures(sent, tree);
	}

	/**
	 * extracts feats for a sent x and deptree y, where f(x,y) = sum_{(i,j) edge
	 * in y} f(i,j)
	 * 
	 * @param sent
	 * @param tree
	 * @return
	 */
	private IFeatureVector extractFeatures(DepInst sent, DepStruct tree) {
		FeatureVectorBuffer fb = new FeatureVectorBuffer();
		for (int i = 1; i <= sent.size(); i++) {
			int head = tree.heads[i];
			IFeatureVector fv = getEdgeFeatures(head, i, sent);
			fb.addFeature(fv);
		}
		return fb.toFeatureVector();
	}

	private void addSurroundingPOS(int head, int i, DepInst sent,
			List<String> fList) {
		String nextmod = (i + 1 < sent.pos.length) ? sent.pos[i + 1] : "NULL";
		String nexthead = (head + 1 < sent.pos.length) ? sent.pos[head + 1]
				: "NULL";
		String prevmod = (i - 1 > 0) ? sent.pos[i - 1] : "NULL";
		String prevhead = (head - 1 > 0) ? sent.pos[head - 1] : "NULL";
		fList.add("headpos_" + sent.pos[head] + "_nexthead_" + nexthead
				+ "_modpos_" + sent.pos[i] + "_nextmod_" + nextmod);
		fList.add("headpos_" + sent.pos[head] + "_prevhead_" + prevhead
				+ "_modpos_" + sent.pos[i] + "_nextmod_" + nextmod);
		fList.add("headpos_" + sent.pos[head] + "_nexthead_" + nexthead
				+ "_modpos_" + sent.pos[i] + "_prevmod_" + prevmod);
		fList.add("headpos_" + sent.pos[head] + "_prevhead_" + prevhead
				+ "_modpos_" + sent.pos[i] + "_prevmod_" + prevmod);

	}

	private void addBigramFeats(int head, int i, DepInst sent,
			List<String> fList) {
		fList.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierboth_" + sent.lemmas[i] + "_" + sent.pos[i]);
		fList.add("headpos_" + sent.pos[head] + "_modifierboth_"
				+ sent.lemmas[i] + "_" + sent.pos[i]);
		fList.add("headword_" + sent.lemmas[head] + "_modifierboth_"
				+ sent.lemmas[i] + "_" + sent.pos[i]);
		fList.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierpos_" + sent.pos[i]);
		fList.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierword_" + sent.lemmas[i]);
		fList.add("headword_" + sent.lemmas[head] + "_modifierword_"
				+ sent.lemmas[i]);
		fList.add("headpos_" + sent.pos[head] + "_modifierpos_"
				+ sent.pos[i]);

	}

	private void addInBetweenPOS(int head, int i, DepInst sent,
			List<String> fList) {
		int low = head > i ? i : head;
		int high = head < i ? i : head;
		for (int k = low + 1; k < high; k++) {
			fList.add("headpos_" + sent.pos[head] + "_bwpos_" + k + "_"
					+ sent.pos[k] + "_modifierpos_" + sent.pos[i]);
		}
	}

	private void addDistanceFeats(int head, int i, DepInst sent,
			List<String> fList) {
		int dist = Math.abs(head-i);
		fList.add("headpos_" + sent.pos[head] + "_dist_" + dist
				+ "_modifierpos_" + sent.pos[i]);
		fList.add("headword_" + sent.lemmas[head] + "_dist_" + dist
				+ "_modifierword_" + sent.lemmas[i]);
	}

	private void addDirectionalFeats(int head, int i, DepInst sent,
			List<String> fList) {
		boolean attR = i < head ? false : true;
		fList.add("headpos_" + sent.pos[head] + "_" + attR
				+ "_modifierpos_" + sent.pos[i]);
		fList.add("headword_" + sent.lemmas[head] + "_" + attR
				+ "_modifierword_" + sent.lemmas[i]);
		fList.add("headword_" + sent.lemmas[head] + "_" + attR
				+ "_modifierpos_" + sent.pos[i]);
		fList.add("headpos_" + sent.pos[head] + "_" + attR
				+ "_modifierword_" + sent.lemmas[i]);

	}

	private void addUnigramFeats(int head, int i, DepInst sent,
			List<String> fList) {
		fList.add("head_" + sent.lemmas[head] + "_" + sent.pos[head]);
		fList.add("head_" + sent.lemmas[head]);
		fList.add("head_" + sent.pos[head]);
		fList.add("modifier_" + sent.lemmas[i] + "_" + sent.pos[i]);
		fList.add("modifier_" + sent.lemmas[i]);
		fList.add("modifier_" + sent.pos[i]);
	}

	/**
	 * returns f(i,j) head idx from 0..n i idx from 1..n
	 * 
	 * @param head
	 * @param i
	 * @param sent
	 * @return
	 */
	public IFeatureVector getEdgeFeatures(int head, int i, DepInst sent) {
		List<String> fList = new ArrayList<>();

		addUnigramFeats(head, i, sent, fList);
		addBigramFeats(head, i, sent, fList);
		addSurroundingPOS(head, i, sent, fList);
		addDirectionalFeats(head, i, sent, fList);
		addInBetweenPOS(head, i, sent, fList);
		addDistanceFeats(head, i, sent, fList);
		
		FeatureVectorBuffer fv = new FeatureVectorBuffer();
		for (String f : fList) {
			if (lm.isAllowNewFeatures())
				lm.addFeature(f);
			if (lm.containFeature(f))
				fv.addFeature(lm.getFeatureId(f), 1.0f);
			else
				fv.addFeature(lm.getFeatureId("W:unknownword"), 1.0f);
		}
		return fv.toFeatureVector();
	}

}
