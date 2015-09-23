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
package edu.illinois.cs.cogcomp.sl.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * The class that allows the future implementations of a learned model.
 * This class contains your weight vector, feature lexicon and other config details.
 * @author Cogcomp @ UI
 * 
 */
public class SLModel implements Serializable {

	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(SLModel.class);

	public WeightVector wv;
	public SLParameters para;
	public int numFeatuerBit;
	public Lexiconer lm;
	public AbstractInferenceSolver infSolver;
	public AbstractFeatureGenerator featureGenerator;
	public Map<String, String> config;

	/**
	 * The function that is used to save the model into disk. This function just
	 * serialize the whole object into disk.
	 * 
	 * You can modify the save/load behavior by overriding this function.
	 * 
	 * @param fileName
	 *            The filename of the saved model.
	 * @throws IOException
	 */
	public void saveModel(String fileName) throws IOException {
		logger.info("Save Model to " + fileName + ".....");
		numFeatuerBit = SLParameters.HASHING_MASK;
		ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(fileName)));
		oos.writeObject(this);
		oos.close();
		logger.info("Done!");
	}

	/**
	 * The function is used to load the model. You can modify the save/load
	 * behavior by overriding this function.
	 * 
	 * @param fileName
	 *            The filename of the saved model.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static SLModel loadModel(String fileName) throws IOException,
			ClassNotFoundException {
		logger.info("Load trained Models.....");

		SLModel res = null;
		ObjectInputStream ios = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(fileName)));

		res = (SLModel) ios.readObject();
		ios.close();
		SLParameters.HASHING_MASK = res.numFeatuerBit;
		logger.info("Load Model complete!");
		return res;
	}
}
