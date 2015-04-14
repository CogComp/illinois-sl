package edu.illinois.cs.cogcomp.sl.util;

import java.io.Serializable;

/**
 * The class defines a dense vector and its related operations such as dot-product, add, and norm.
 * 
 * @author Ming-Wei Chang
 * 
 */
public class DenseVector implements Serializable, Cloneable{
	private static final long serialVersionUID = 4496565917496408855L;
	
	// the elements of the dense vector 
	protected float[] u = null;
	
	// the size of the vector
	protected int size=0;
	
	// if expendable is true, the capacity of the vector can be extended. 
	protected boolean extendable = true;
	
	public DenseVector() {
		// default capacity is 8
		this(8);
	}

	/**
	 * @return If this vector is allowed to grow
	 */
	public boolean isExtendable() {
		return extendable;
	}

	/**
	 * Set the flag to indicate if this vector can grow or not
	 * 
	 * @param flag
	 */
	public void setExtendable(boolean flag) {
		extendable = flag;
	}

	/**
	 * Constructs a new vector with the specified capacity
	 * 
	 * @param n
	 */
	public DenseVector(int n) {
		u = new float[n];
	}

	/**
	 * return The size of the vector
	 */
	public int getLength() {
		return size;
	}

	/**
	 * return the dot product between a sparse vector and a dense vector.
	 * Note that the dot product function will ignore the element in the sparse
	 * vector that does not appear in the dense vector instead of throwing an exception.
	 * @param fv
	 *            Sparse feature vector
	 * @return
	 */
	public float dotProduct(IFeatureVector fv) {
		float res = 0.0f;
		if(fv.getMaxIdx() >= size){
			for(int i=0; i< fv.getNumActiveFeatures(); i++){
				if(fv.getIdx(i) < size){
					res += u[fv.getIdx(i)] * fv.getValue(i);
				}
			}
		} else {
			for(int i=0; i< fv.getNumActiveFeatures(); i++){
						res += u[fv.getIdx(i)] * fv.getValue(i);
				}
		}
		return res;
	}

	/**
	 * First, the sparse is shifted by offset. Then, compute the dot product 
	 * between the dense vector and the shifted sparse vector.
	 * Note that the dot product function will ignore the element in the sparse
	 * vector that does not appear in the dense vector instead of throwing an exception.
	 * 
	 * @param fv
	 *            Sparse feature vector
	 * @param offset 
	 * @return
	 */
	
	public float dotProduct(IFeatureVector fv, int offset){
		float res = 0.0f;
		if(fv.getMaxIdx() + offset >= size){
			for(int i=0; i< fv.getNumActiveFeatures(); i++){
				if(fv.getIdx(i)+offset < size){
					res += u[fv.getIdx(i)+offset] * fv.getValue(i);
				}
			}
		} else {
			for(int i=0; i< fv.getNumActiveFeatures(); i++){
				res += u[fv.getIdx(i)+offset] * fv.getValue(i);
			}
		}
		return res;
	}
	
	/**
	 * Return the dot product to a dense feature vector
	 * 
	 * @param df
	 * @return
	 */
	public float dotProduct(DenseVector df) {
		float res = 0.0f;
		
		for (int i=0; i<Math.min(size, df.getLength()); i++) {
			res += u[i] * df.u[i];
		}
		return res;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<u.length; i++)
			sb.append(i + ":" + u[i] + " ");
		return sb.toString();
	}

	/**
	 * increase the size of the dense vector by 2 or contain the new idx (pick
	 * the larger one).
	 * 
	 * @param new_idx
	 */
	private synchronized void expandFor(int idx) {
		if(idx < size) return;
		assert extendable == true;
		int capacity = u.length, oldSize = size;
		size = idx+1;
		if(capacity >= size) return;
		
		while(capacity < size) capacity *=2;
		float[] newU = new float[capacity];
		try{
		System.arraycopy(u, 0, newU, 0, oldSize);
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println(oldSize);
			System.out.println(u.length);
			System.out.println(newU.length);
		}
		u = newU;
	}

	/**
	 * Initialize an element of the weight vector, if the weight vector does not
	 * contain this item, allocate space internally
	 * 
	 * @param index
	 *            The index of the initialized item
	 * @param v
	 *            The value of the initialized item
	 */
	public synchronized void setElement(int index, float v) {
		assert extendable == true;
		expandFor(index);
		u[index] = v;
	}

	/**
	 * w = w + alpha * dv
	 * 
	 * @param dv
	 *            the dense vector
	 * @param alpha
	 *            the scalar
	 */
	public synchronized void addDenseVector(DenseVector dv, float alpha) {
		if (this.isExtendable())
			this.expandFor(dv.getLength());

		int n = dv.getLength();
		for (int i = 0; i < n; i++) {
			u[i] += alpha * dv.u[i];
		}
	}
	
	/**
	 * wi = wi + alphai * dvi
	 * To allow for adagrad like updates
	 * 
	 * @param dv
	 *            the dense vector
	 * @param alpha
	 *            the scalar
	 */
	public synchronized void addDenseVector(DenseVector dv, float[] alpha) {
		if (this.isExtendable())
			this.expandFor(dv.getLength());

		int n = dv.getLength();
		for (int i = 0; i < n; i++) {
			u[i] += alpha[i] * dv.u[i];
		}
	}
	/**
	 * w = w + dv
	 * 
	 * @param dv
	 *            the dense vector
	 */
	public synchronized void addDenseVector(DenseVector dv) {
		if (this.isExtendable())
			this.expandFor(dv.getLength());
		
		int n = dv.getLength();
		for (int i = 0; i < n; i++) {
			u[i] += dv.u[i];
		}
	}

	/**
	 * w = w*scale 
	 * <p>
	 * 
	 * @param scale
	 *            the dense vector
	 */
	public synchronized void scale(float scale) {
		scale((double) scale);
	}
	
	/**
	 * w = w*scale 
	 * <p>
	 * 
	 * @param scale
	 *            the dense vector
	 */
	public synchronized void scale(double scale) {
		for(int i=0; i< size; i++) {
			u[i] *= scale;
		}
	}
	
	/**
	 * Set the  weight vector to v
	 * 
	 * w = v
	 * 
	 * @param v
	 *            the dense vector
	 */
	public synchronized void setDenseVector(DenseVector v) {
		try {
			if (this.isExtendable())
				this.expandFor(v.getLength());
			System.arraycopy(v.getInternalArray(), 0, u, 0, v.size);
		} catch(Exception e) {
			e.printStackTrace();
		}
		size = v.size;
	}
	
	/**
	 * 
	 * w = w + alpha * sfv
	 * 
	 * @param sfv
	 *            A sparse feature vector
	 * @param alpha
	 *            The scalar
	 */
	public void addSparseFeatureVector(IFeatureVector sfv,
			float alpha) {
		addSparseFeatureVector(sfv,(double) alpha);
	}
	
	/**
	 * 
	 * w = w + alpha * sfv
	 * 
	 * @param sfv
	 *            A sparse feature vector
	 * @param alpha
	 *            The scalar
	 */
	public void addSparseFeatureVector(IFeatureVector sfv,
			double alpha) {
		if (this.isExtendable()) {
			this.expandFor(sfv.getMaxIdx());
		}
		
		for(int i=0; i< sfv.getNumActiveFeatures(); i++){
			u[sfv.getIdx(i)]+= alpha* sfv.getValue(i);
		}
	}

	/**
	 * return the square of the 2-norm of this dense vector 
	 * @return
	 */
	public float getSquareL2Norm() {
		float res = 0;
		for (int i = 0; i < size; i++) {
			res += u[i] * u[i];
		}
		return res;
	}

	/** should avoid using this function, currently only for liblinear
	 * 
	 * @return the internal array representation of this vector
	 */
	public float[] getInternalArray() {
		return u;
	}
	
	public float get(int i){
		if(i >= u.length )
			return 0.0f;
		else
			return u[i];
	}
}
