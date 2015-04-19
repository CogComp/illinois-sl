package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

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
 * @author Shyam
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
		// System.out.println("sent size " + sent.size());
		DepStruct tree = (DepStruct) y;
		// for(int i=0;i<tree.heads.length;i++)
		// {
		// System.out.println(tree.heads[i]);
		// }
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
			// System.out.println("handling "+head+" "+(i+1));
			IFeatureVector fv = getEdgeFeatures(head, i, sent);
			fb.addFeature(fv);
		}
		return fb.toFeatureVector();
	}

	private void addSurroundingPOS(int head, int i, DepInst sent,
			List<String> flist) {
		String nextmod = (i + 1 < sent.pos.length) ? sent.pos[i + 1] : "NULL";
		String nexthead = (head + 1 < sent.pos.length) ? sent.pos[head + 1]
				: "NULL";
		String prevmod = (i - 1 > 0) ? sent.pos[i - 1] : "NULL";
		String prevhead = (head - 1 > 0) ? sent.pos[head - 1] : "NULL";
		flist.add("headpos_" + sent.pos[head] + "_nexthead_" + nexthead
				+ "_modpos_" + sent.pos[i] + "_nextmod_" + nextmod);
		flist.add("headpos_" + sent.pos[head] + "_prevhead_" + prevhead
				+ "_modpos_" + sent.pos[i] + "_nextmod_" + nextmod);
		flist.add("headpos_" + sent.pos[head] + "_nexthead_" + nexthead
				+ "_modpos_" + sent.pos[i] + "_prevmod_" + prevmod);
		flist.add("headpos_" + sent.pos[head] + "_prevhead_" + prevhead
				+ "_modpos_" + sent.pos[i] + "_prevmod_" + prevmod);

	}

	private void addBigramFeats(int head, int i, DepInst sent,
			List<String> featureMap) {
		featureMap.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierboth_" + sent.lemmas[i] + "_" + sent.pos[i]);
		featureMap.add("headpos_" + sent.pos[head] + "_modifierboth_"
				+ sent.lemmas[i] + "_" + sent.pos[i]);
		featureMap.add("headword_" + sent.lemmas[head] + "_modifierboth_"
				+ sent.lemmas[i] + "_" + sent.pos[i]);
		featureMap.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierpos_" + sent.pos[i]);
		featureMap.add("headboth_" + sent.lemmas[head] + "_" + sent.pos[head]
				+ "_modifierword_" + sent.lemmas[i]);
		featureMap.add("headword_" + sent.lemmas[head] + "_modifierword_"
				+ sent.lemmas[i]);
		featureMap.add("headpos_" + sent.pos[head] + "_modifierpos_"
				+ sent.pos[i]);

	}

	private void addDirectionalFeats(int head, int i, DepInst sent,
			List<String> featureMap) {
		boolean attR = i < head ? false : true;
		featureMap.add("headpos_" + sent.pos[head] + "_" + attR
				+ "_modifierpos_" + sent.pos[i]);
		featureMap.add("headword_" + sent.lemmas[head] + "_" + attR
				+ "_modifierword_" + sent.lemmas[i]);

	}

	private void addUnigramFeats(int head, int i, DepInst sent,
			List<String> featureMap) {
		featureMap.add("head_" + sent.lemmas[head] + "_" + sent.pos[head]);
		featureMap.add("head_" + sent.lemmas[head]);
		featureMap.add("head_" + sent.pos[head]);
		featureMap.add("modifier_" + sent.lemmas[i] + "_" + sent.pos[i]);
		featureMap.add("modifier_" + sent.lemmas[i]);
		featureMap.add("modifier_" + sent.pos[i]);
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
		List<String> featureMap = new ArrayList<>();

		addUnigramFeats(head, i, sent, featureMap);
		addBigramFeats(head, i, sent, featureMap);
		addSurroundingPOS(head, i, sent, featureMap);
		addDirectionalFeats(head, i, sent, featureMap);

		FeatureVectorBuffer fv = new FeatureVectorBuffer();
		for (String f : featureMap) {
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
