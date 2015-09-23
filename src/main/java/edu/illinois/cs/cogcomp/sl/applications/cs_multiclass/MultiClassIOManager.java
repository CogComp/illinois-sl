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
package edu.illinois.cs.cogcomp.sl.applications.cs_multiclass;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class MultiClassIOManager {

	public static float[][] getCostMatrix(Map<String, Integer> labelMapping,String fname) throws Exception{
		int numLabels = labelMapping.size();
		
		float[][] res = new float[numLabels][numLabels];
		for(int i=0;i  < numLabels ;i ++){
			for(int j=0; j < numLabels; j ++){
				if (i==j)
					res[i][j] = 0;
				else
					res[i][j] = 1.0f;
			}
		}
		ArrayList<String> lines = LineIO.read(fname);
		
		for(String line : lines){
			
			if (line.trim().charAt(0) == '#')
				continue;
			String[] tokens = line.split("\\s+");
			if (tokens.length != 3)
				throw new Exception("Format error in the cost matrix file.");
			if (!labelMapping.containsKey(tokens[0]))
				throw new Exception("Format error in the cost matrix file. Label (" + tokens[0] +") does not exist!"); 
			if (!labelMapping.containsKey(tokens[1]))
				throw new Exception("Format error in the cost matrix file. Label (" + tokens[1] +") does not exist!");
						
			int i = labelMapping.get(tokens[0]);
			int j = labelMapping.get(tokens[1]);
			float cost = -1;
			
			try{
				cost = Float.parseFloat(tokens[2]);				
			} catch(NumberFormatException e){
				throw new Exception("Format error in the cost matrix file. The cost should be a number!");
			}
			
			if (i ==j && cost !=0 )
				throw new Exception("The cost should be zero when pred == gold.");
			
			if (cost < 0)
				throw new Exception("The cost cannot be negative.");
			res[i][j] = cost;
		}		
		System.out.println("Done!");
		return res;
	}
	
	
	private static int checkNumOfFeaturesAndBuildClassMapping(String fileName,
			Map<String, Integer> labelMapping) throws Exception {
		int numFeatures = 0;
		
		ArrayList<String> lines = LineIO.read(fileName);

		for (String line : lines) {			
			String[] tokens = line.split("\\s+");
			String lab = tokens[0];

			// put the lab names into labels_mapping
			if (!labelMapping.containsKey(lab)) {
				int lab_size = labelMapping.size();
				labelMapping.put(lab, lab_size);
			}

			for (int i = 1; i < tokens.length; i++) {
				String[] featureTokens = tokens[i].split(":");
				
				if (featureTokens.length != 2){
					throw new Exception("Format error in the input file! in >" + line +"<");
				}
				
				int idx = Integer.parseInt(featureTokens[0]);

				if (idx <= 0) {
					throw new Exception(
							"The feature index must >= 1 !");
				}

				if (idx > numFeatures) {
					numFeatures = idx;
				}
			}
		}

		numFeatures ++; //allocate for zero 
		numFeatures ++; //allocate for the bias term		
		
		System.out.println("Label Mapping: "
				+ labelMapping.toString().replace("=", "==>"));
		System.out.println("number of features:" + numFeatures);

		return numFeatures;
	}

	/**
	 * Read training data
	 * 
	 * @param fname
	 *            The filename contains the training data
	 * @return A LabeledMulticlasssData
	 * @throws Exception
	 */
	public static LabeledMultiClassData readTrainingData(String fname)
			throws Exception {
		Map<String, Integer> labelMapping = new HashMap<String, Integer>();
		int numFeatures = checkNumOfFeaturesAndBuildClassMapping(fname,
				labelMapping);
		int numClasses = labelMapping.size();

		LabeledMultiClassData res = new LabeledMultiClassData(labelMapping,
				numFeatures);
		readMultiClassDataAndAddBiasTerm(fname, labelMapping, numFeatures, numClasses, res);
		return res;
	}

	/**
	 * Read testing data.
	 * 
	 * @param fname
	 *            The filename contains the testing data
	 * @return A LabeledMulticlasssData
	 * @throws Exception
	 */
	public static LabeledMultiClassData readTestingData(String fname,
			Map<String, Integer> labelsMapping, int numFeatures) throws Exception {
		int numClasses = labelsMapping.size();
		LabeledMultiClassData res = new LabeledMultiClassData(labelsMapping,
				numFeatures);
		readMultiClassDataAndAddBiasTerm(fname, labelsMapping, numFeatures, numClasses, res);
		return res;
	}

	private static void readMultiClassDataAndAddBiasTerm(String fname,
			Map<String, Integer> labelMapping, int numFeatures, int numClasses,
			LabeledMultiClassData res) throws FileNotFoundException {
		ArrayList<String> lines = LineIO.read(fname);
		for (String line : lines) {
			String[] tokens = line.split("\\s+");

			int activeLen = 1;

			// ignore the features > n_features
			for (int i = 1; i < tokens.length; i++) {

				String[] featureTokens = tokens[i].split(":");
				int idx = Integer.parseInt(featureTokens[0]); 
				if (idx <= numFeatures) 
					activeLen++;
			}

			int[] idxList = new int[activeLen];
			float[] valueList = new float[activeLen];

			for (int i = 1; i < tokens.length; i++) {
				String[] feaureTokens = tokens[i].split(":");
				int idx = Integer.parseInt(feaureTokens[0]); 
				if (idx <= numFeatures) { 
					idxList[i - 1] = idx;
					valueList[i - 1] = Float.parseFloat(feaureTokens[1]);
				}
			}
			// append the bias term
			idxList[activeLen-1] = numFeatures-1;
			valueList[activeLen-1] = 1;

			IFeatureVector fv = new SparseFeatureVector(idxList, valueList);
			MultiClassInstance mi = new MultiClassInstance(numFeatures, numClasses,
					fv);
			res.instanceList.add(mi);

			String lab = tokens[0];
			if (labelMapping.containsKey(lab)) {
				res.goldStructureList.add(new MultiClassLabel(labelMapping.get(lab)));
			} else {
				// only design for unknown classes in the test data
				res.goldStructureList.add(new MultiClassLabel(-1));
			}
		}
	}
}
