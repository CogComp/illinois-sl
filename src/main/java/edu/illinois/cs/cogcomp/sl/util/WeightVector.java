package edu.illinois.cs.cogcomp.sl.util;

/**
 * The weight vector 
 * @author Ming-Wei Chang
 *
 */
public class WeightVector extends DenseVector {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param n The size of the weight vector.
	 */
	public WeightVector(int n) {		
		super(n);
	}
	
	/**
	 * Construct the weight vector by an array
	 * @param in
	 */
	public WeightVector(float[] in) {
		u =  new float[in.length];
		System.arraycopy(in, 0, u, 0, in.length);
		size = in.length;
	}
	
	/**
	 * Duplicate a weight vector
	 * @param in
	 */
	public WeightVector(WeightVector wv) {
		float in[] = wv.getInternalArray();
		u =  new float[in.length];
		System.arraycopy(in, 0, u, 0, in.length);
		size = in.length;
	}
	
	@Override
	public int getLength(){
		return super.getLength();
	}
	
	/**
	 * wv = old
	 * @param old
	 * @param additionalSpace
	 */
	public WeightVector(WeightVector old, int additionalSpace) {		
		u =  new float[old.u.length + additionalSpace];
		System.arraycopy(old.u, 0, u, 0, old.u.length);
		extendable = old.extendable;
		size = old.size;
	}

	/**
	 * Please use the addFeatureVector function instead !
	 * @param fv
	 * @param alpha
	 */
	@Deprecated
	public synchronized void addToW(IFeatureVector fv, float alpha) {		
		super.addSparseFeatureVector(fv, alpha);
	}

	
	public float getGlobalBiasTerm(){
		return u[0];
	}
	
	/**
	 * should avoid using this function
	 * @return
	 */
	public float[] getWeightArray(){
		return super.getInternalArray();
	}
}
