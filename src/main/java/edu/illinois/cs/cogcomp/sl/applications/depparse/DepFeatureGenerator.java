package edu.illinois.cs.cogcomp.sl.applications.depparse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
/**
 * Generates features based on edges in the dep. graph
 * features for a dep tree is sum of the features for all its edges
 * We use similar features as in MSTParser:
 * http://www.seas.upenn.edu/~strctlrn/MSTParser/MSTParser.html
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
	@SuppressWarnings("unused")
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
			FeatureVectorBuffer edgefv = getEdgeFeatures(head, i, sent);
			fb.addFeature(edgefv);
		}
		return fb.toFeatureVector();
	}

	private void addSurroundingPOS(int head, int i, int dist, DepInst sent,
			List<Integer> fList) {
		int left = Math.min(head, i); 
		int right = Math.max(head, i);
		
		int nextleft = nextCode*iCode*((left + 1 < sent.pos.length) ? sent.pos[left + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int nextright = nextCode*headCode*((right + 1 < sent.pos.length) ? sent.pos[right + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevleft = preCode*iCode*((left - 1 > 0) ? sent.pos[left - 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevright = preCode*headCode*((right - 1 > 0) ? sent.pos[right - 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevleftnextright = prevleft+nextright;
		int nextleftprevright = nextleft+prevright;
		int nextleftA = nextCode*iCode*((left + 1 < sent.pos.length) ? sent.cpos[left + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int nextrightA = nextCode*headCode*((right + 1 < sent.pos.length) ? sent.cpos[right + 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevleftA = preCode*iCode*((left - 1 > 0) ? sent.cpos[left - 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevrightA = preCode*headCode*((right - 1 > 0) ? sent.cpos[right - 1] : NULLCode)& SLParameters.HASHING_MASK;
		int prevleftnextrightA = prevleftA+nextrightA;
		int nextleftprevrightA = nextleftA+prevrightA;
		int leftbase = headCode* (sent.pos[left]); 
		int rightbase = iCode* (sent.pos[right]);
		int leftrightbase = leftbase+rightbase;
		int leftbaseA = headCode* (sent.cpos[left]); 
		int rightbaseA = iCode* (sent.cpos[right]);
		int leftrightbaseA = leftbase+rightbase;		
		
		addFeature(fList,  leftrightbase + prevleftnextright + dist);
		addFeature(fList,  leftrightbase + nextright + dist);
		addFeature(fList,  leftrightbase + prevleft + dist);
		addFeature(fList,  rightbase + prevleftnextright + dist);
		addFeature(fList,  leftbase + prevleftnextright + dist);	
		addFeature(fList,  leftrightbase + prevleftnextright);
		addFeature(fList,  leftrightbase + nextright);
		addFeature(fList,  leftrightbase + prevleft);
		addFeature(fList,  rightbase + prevleftnextright);
		addFeature(fList,  leftbase + prevleftnextright);		
		addFeature(fList,  leftrightbaseA + prevleftnextrightA + dist);
		addFeature(fList,  leftrightbaseA + nextrightA + dist);
		addFeature(fList,  leftrightbaseA + prevleftA + dist);
		addFeature(fList,  rightbaseA + prevleftnextrightA + dist);
		addFeature(fList,  leftbaseA + prevleftnextrightA + dist);
		addFeature(fList,  leftrightbaseA + prevleftnextrightA);
		addFeature(fList,  leftrightbaseA + nextrightA);
		addFeature(fList,  leftrightbaseA + prevleftA);
		addFeature(fList,  rightbaseA + prevleftnextrightA);
		addFeature(fList,  leftbaseA + prevleftnextrightA);
		
		addFeature(fList,  leftrightbase + nextleftprevright + dist);
		addFeature(fList,  leftrightbase + prevright + dist);
		addFeature(fList,  leftrightbase + nextleft + dist);
		addFeature(fList,  rightbase + nextleftprevright + dist);
		addFeature(fList,  leftbase + nextleftprevright + dist);	
		addFeature(fList,  leftrightbase + nextleftprevright);
		addFeature(fList,  leftrightbase + prevright);
		addFeature(fList,  leftrightbase + nextleft);
		addFeature(fList,  rightbase + nextleftprevright);
		addFeature(fList,  leftbase + nextleftprevright);		
		addFeature(fList,  leftrightbaseA + nextleftprevrightA + dist);
		addFeature(fList,  leftrightbaseA + prevrightA + dist);
		addFeature(fList,  leftrightbaseA + nextleftA + dist);
		addFeature(fList,  rightbaseA + nextleftprevrightA + dist);
		addFeature(fList,  leftbaseA + nextleftprevrightA + dist);
		addFeature(fList,  leftrightbaseA + nextleftprevrightA);
		addFeature(fList,  leftrightbaseA + prevrightA);
		addFeature(fList,  leftrightbaseA + nextleftA);
		addFeature(fList,  rightbaseA + nextleftprevrightA);
		addFeature(fList,  leftbaseA + nextleftprevrightA);
		
		addFeature(fList,  leftrightbase + prevleft + prevright + dist);
		addFeature(fList,  leftrightbase + prevleft + prevright);
		addFeature(fList,  leftrightbase + nextleft + nextright +dist);
		addFeature(fList,  leftrightbase + nextleft + nextright);
		addFeature(fList,  leftrightbaseA + prevleftA + prevrightA + dist);
		addFeature(fList,  leftrightbaseA + prevleftA + prevrightA);
		addFeature(fList,  leftrightbaseA + nextleftA + nextrightA +dist);
		addFeature(fList,  leftrightbaseA + nextleftA + nextrightA);

	}

	private void addInBetweenPOS(int head, int i, int dist, DepInst sent,
			List<Integer> fList) {
		int low = head > i ? i : head;
		int high = head < i ? i : head;
		for (int k = low + 1; k < high; k++) {
			addFeature(fList,  headCode* sent.pos[head] + iCode*sent.pos[i] + betweenCode*sent.pos[k] + dist);
			addFeature(fList,  headCode* sent.pos[head] + iCode*sent.pos[i] + betweenCode*sent.pos[k] );
			addFeature(fList,  headCode* sent.cpos[head] + iCode*sent.cpos[i] + betweenCode*sent.cpos[k] + dist);
			addFeature(fList,  headCode* sent.cpos[head] + iCode*sent.cpos[i] + betweenCode*sent.cpos[k] );
			addFeature(fList,  betweenCode*sent.pos[k] );
		}
	}

	private void addLexFeats(int head, int i, int dist, DepInst sent,
			List<Integer> fList) {
		int hw = headCode*sent.lemmas[head], hp = headCode*sent.pos[head], iw = iCode*sent.lemmas[i], ip = iCode*sent.pos[head];
		int all = hw+hp+iw+ip, hpiwip=hp+iw+ip, hwhpip=hw+hp+ip, hwip=hw+ip, hpiw=hp+iw, hwiw=hw+iw, hpip=hp+ip, hwhp=hw+hp, iwip=iw+ip;
		
		addFeature(fList, all+dist);
		addFeature(fList, hpiwip+dist);
		addFeature(fList, hwhpip+dist);
		addFeature(fList, hwip+ip+dist);
		addFeature(fList, hpiw+dist);
		addFeature(fList, hwiw+dist);
		addFeature(fList, hpip+dist);
		addFeature(fList, hwhp+dist);
		addFeature(fList, iwip+dist);
		addFeature(fList, iw+dist);
		addFeature(fList, ip+dist);
		addFeature(fList, hw+dist);
		addFeature(fList, hp+dist);
		addFeature(fList, all);
		addFeature(fList, hpiwip);
		addFeature(fList, hwhpip);
		addFeature(fList, hwip+ip);
		addFeature(fList, hpiw);
		addFeature(fList, hwiw);
		addFeature(fList, hpip);
		addFeature(fList, hwhp);
		addFeature(fList, iwip);
		addFeature(fList, iw);
		addFeature(fList, ip);
		addFeature(fList, hw);
		addFeature(fList, hp);
	}
	private void addLexPrefixFeats(int head, int i, int dist, DepInst sent, List<Integer> fList){
		int hw = headCode*sent.lemmasPrefix[head]*257, hp = headCode*sent.pos[head], iw = iCode*sent.lemmasPrefix[i]*257, ip = iCode*sent.pos[head];
		int all = hw+hp+iw+ip, hpiwip=hp+iw+ip, hwhpip=hw+hp+ip, hwip=hw+ip, hpiw=hp+iw, hwiw=hw+iw, hwhp=hw+hp, iwip=iw+ip;
		
		addFeature(fList, all+dist);
		addFeature(fList, hpiwip+dist);
		addFeature(fList, hwhpip+dist);
		addFeature(fList, hwip+ip+dist);
		addFeature(fList, hpiw+dist);
		addFeature(fList, hwiw+dist);
		addFeature(fList, hwhp+dist);
		addFeature(fList, iwip+dist);
		addFeature(fList, iw+dist);
		addFeature(fList, hw+dist);
		addFeature(fList, all);
		addFeature(fList, hpiwip);
		addFeature(fList, hwhpip);
		addFeature(fList, hwip+ip);
		addFeature(fList, hpiw);
		addFeature(fList, hwiw);
		addFeature(fList, hwhp);
		addFeature(fList, iw);
		addFeature(fList, hw);
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
	public FeatureVectorBuffer getEdgeFeatures(int head, int i, DepInst sent) {
		List<Integer> fList = new ArrayList<>();
		List<Float> valList = new ArrayList<>();
		int dist = Math.abs(head-i);
		dist = (dist > 10)? 10 : (dist>5)? 5: dist;
		dist *= (head > i)? templeteCode*191021:templeteCode*350377;
		addSurroundingPOS(head, i, dist, sent, fList);
		addInBetweenPOS(head, i, dist, sent, fList);
		addLexFeats(head, i, dist, sent, fList);
		addLexPrefixFeats(head, i, dist, sent, fList);
				
		for(int t=0; t< fList.size();t++)
			valList.add(1.0f);
		FeatureVectorBuffer fvb = new FeatureVectorBuffer(fList, valList);

		return fvb;
	}
	public static final void addFeature(List<Integer> idxList, int feature ){
		idxList.add(feature & SLParameters.HASHING_MASK);
		
	}

}
