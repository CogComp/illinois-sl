package edu.illinois.cs.cogcomp.sl.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
	
	public static void printSparsity(WeightVector wv) {
		int nzeroes=0;
		System.out.println("SIZE: "+wv.getLength());
		for(float f:wv.getInternalArray())
		{
			if(f!=0.0)
				nzeroes++;
		}
		System.out.println("NZ values: "+nzeroes);
	}
	
	public void printToFile(Lexiconer lm, String filepath) throws FileNotFoundException {
		float[] ff= this.getInternalArray();
		PrintWriter w = new PrintWriter(filepath);
		for(int i=0;i<ff.length;i++)
		{
			w.println(lm.getFeatureString(i)+" "+ff[i]);
		}
		w.close();
	}
}
