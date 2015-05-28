package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
/**
 * generates features based on edges in the dep. graph
 * features for a dep tree is sum of the features for all its edges
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
	private static final int headCode = 103703;
	private static final int iCode = 3163;
	private static final int betweenCode = 479909;
	private static final int templeteCode = 224737;
	private static final int NULLCode = 746777;
	private static final int nextCode = 414977; 
	private static final int preCode = 2741;
	

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
			List<Integer> fList) {
		int nextmod = nextCode*iCode*((i + 1 < sent.pos.length) ? sent.pos[i + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int nexthead = nextCode*headCode*((head + 1 < sent.pos.length) ? sent.pos[head + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevmod = preCode*iCode*((i - 1 > 0) ? sent.pos[i - 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevhead = preCode*headCode*((head - 1 > 0) ? sent.pos[head - 1] : NULLCode)& SLParameters.HASHING_MASK;
		addFeature(fList,  headCode* (sent.pos[head])+iCode* (sent.pos[i]) + nexthead + nextmod);
		addFeature(fList,  headCode* (sent.pos[head])+iCode* (sent.pos[i]) + prevhead + nextmod);
		addFeature(fList,  headCode* (sent.pos[head])+iCode* (sent.pos[i]) + nexthead + prevmod);
		addFeature(fList,  headCode* (sent.pos[head])+iCode* (sent.pos[i]) + prevhead + prevmod);
	}

	private void addBigramFeats(int head, int i, DepInst sent, List<Integer> fList) {
		addFeature(fList,  headCode* (sent.pos[head]+ sent.lemmas[head]) + iCode*(sent.pos[i]+ sent.lemmas[i]));
		addFeature(fList,  headCode* (sent.pos[head]) + iCode*(sent.pos[i]+ sent.lemmas[i]));
		addFeature(fList,  headCode* (sent.lemmas[head]) + iCode*(sent.pos[i]+ sent.lemmas[i]));
		addFeature(fList,  headCode* (sent.pos[head]+ sent.lemmas[head]) + iCode*(sent.pos[i]));
		addFeature(fList,  headCode* (sent.pos[head]+ sent.lemmas[head]) + iCode*(sent.lemmas[i]));
		addFeature(fList,  headCode* (sent.pos[head]) + iCode*(sent.pos[i]));
		addFeature(fList,  headCode* (sent.lemmas[head]) + iCode*(sent.lemmas[i]));

	}

	private void addInBetweenPOS(int head, int i, DepInst sent,
			List<Integer> fList) {
		int dist = Math.abs(head-i);
		dist = (dist > 10)? 10 : (dist>5)? 5: dist;
		dist *= (head > i)? templeteCode:-templeteCode;
		int low = head > i ? i : head;
		int high = head < i ? i : head;
		for (int k = low + 1; k < high; k++) {
			addFeature(fList,  headCode* sent.pos[head] + iCode*sent.pos[i] + betweenCode*sent.pos[k] + dist);
			addFeature(fList,  headCode* sent.pos[head] + iCode*sent.pos[i] + betweenCode*sent.pos[k] );
			addFeature(fList,  betweenCode*sent.pos[k] );
		}
	}

	private void addDistanceFeats(int head, int i, DepInst sent,
			List<Integer> fList) {
		int dist = Math.abs(head-i);
		dist = (dist > 10)? 10 : (dist>5)? 5: dist;
		dist *= (head > i)? templeteCode:-templeteCode;
		int hw = headCode*sent.lemmas[head], hp = headCode*sent.pos[head], iw = iCode*sent.lemmas[i], ip = iCode*sent.pos[head];		
		addFeature(fList, hw+dist);
		addFeature(fList, hp+dist);
		addFeature(fList, hw+hp+dist);
		addFeature(fList, hw+hp+ip+dist);
		addFeature(fList, hw+hp+iw+ip+dist);
		addFeature(fList, hw+iw+dist);
		addFeature(fList, hw+hp+dist);
		addFeature(fList, hp+iw+dist);
		addFeature(fList, hp+iw+ip+dist);
		addFeature(fList, hp+ip+dist);
		addFeature(fList, iw+ip+dist);
		addFeature(fList, iw+dist);
		addFeature(fList, ip+dist);
	}

	private void addDirectionalFeats(int head, int i, DepInst sent,
			List<Integer> fList) {
		int attR = i < head ? 1299706 : 350377;
		addFeature(fList, headCode* sent.pos[head] + attR + iCode*sent.pos[i]);
		addFeature(fList, headCode* sent.lemmas[head] + attR + iCode*sent.lemmas[i]);
		addFeature(fList, headCode* sent.pos[head] + attR + iCode*sent.lemmas[i]);
		addFeature(fList, headCode* sent.lemmas[head] + attR + iCode*sent.pos[i]);
	}

	private void addUnigramFeats(int head, int i, DepInst sent,
			List<Integer> fList) {
		int hw = headCode*sent.lemmas[head], hp = headCode*sent.pos[head], iw = iCode*sent.lemmas[i], ip = iCode*sent.pos[head];		
		addFeature(fList, hw);
		addFeature(fList, hp);
		addFeature(fList, hw+hp);
		addFeature(fList, hw+hp+ip);
		addFeature(fList, hw+hp+iw+ip);
		addFeature(fList, hw+iw);
		addFeature(fList, hw+hp);
		addFeature(fList, hp+iw);
		addFeature(fList, hp+iw+ip);
		addFeature(fList, hp+ip);
		addFeature(fList, iw+ip);
		addFeature(fList, iw);
		addFeature(fList, ip);
	}
	

	/**
	 * the main feat extraction primitive
	 * returns f(i,j) head idx from 0..n i idx from 1..n
	 * 
	 * @param head
	 * @param i
	 * @param sent
	 * @return
	 */
	public IFeatureVector getEdgeFeatures(int head, int i, DepInst sent) {
		List<Integer> fList = new ArrayList<>();
		List<Float> valList = new ArrayList<>();

		addUnigramFeats(head, i, sent, fList);
//		addBigramFeats(head, i, sent, fList);
		addSurroundingPOS(head, i, sent, fList);
		addDirectionalFeats(head, i, sent, fList);
		addInBetweenPOS(head, i, sent, fList);
		addDistanceFeats(head, i, sent, fList);
		
		for(int t=0; t< fList.size();t++)
			valList.add(1.0f);
		FeatureVectorBuffer fv = new FeatureVectorBuffer(fList, valList);

		return fv.toFeatureVector();
	}
	private void addFeature(List<Integer> idxList, int feature ){
		idxList.add(feature & SLParameters.HASHING_MASK);
		
	}

}
