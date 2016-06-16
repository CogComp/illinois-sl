/*******************************************************************************
 * University of Illinois/NCSA Open Source License
 * Copyright (c) 2010, 
 *
 * Developed by:
 * The Cognitive Computations Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
 * Neither the names of the Cognitive Computations Group, nor the University of Illinois at Urbana-Champaign, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *     
 *******************************************************************************/
package edu.illinois.cs.cogcomp.sl.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
	 * @param wv
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
	public static WeightVector readFromFile(Lexiconer lm, String filepath) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(filepath), Charset.defaultCharset());
		float[] input = new float[lines.size()];
		for(String line:lines)
		{
			String[] parts = line.split("\\s+");
			assert parts.length==2 : "weight file corrupted";
			String fstr = parts[0];
			if(fstr.equals("null"))
			    continue;
			float val = Float.parseFloat(parts[1]);
			input[lm.getFeatureId(fstr)]=val;
		}
		WeightVector wv = new WeightVector(input);
		return wv;
	}
}
