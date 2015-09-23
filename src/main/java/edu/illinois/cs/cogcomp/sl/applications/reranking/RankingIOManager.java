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
package edu.illinois.cs.cogcomp.sl.applications.reranking;

import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class RankingIOManager {
	public static SLProblem readProblem(String fileName)
			throws IOException {
		int index = 0;
		SLProblem sp = new SLProblem();
		ArrayList<String> lines = LineIO.read(fileName);

		while (index < lines.size()) {
			RankingInstance ri = new RankingInstance();

			String[] tokens = lines.get(index).split("\\s+");
			index++;

			int n_tree = Integer.parseInt(tokens[0]);
			ri.example_id = tokens[1];
			int bestItem = -1;
			double bestScore = Float.NEGATIVE_INFINITY;

			for (int i = 0; i < n_tree; i++) {
				String scoreLines = lines.get(index);
				String[] items = scoreLines.split("\\s+");
				float score = Float.parseFloat(items[0]);
				if (score > bestScore){
					bestItem = i;
					bestScore = score;
				}
				ri.scoreList.add(score);
				ri.viewNameList.add((items[1]));
				index++;
				
				String[] featureItems = lines.get(index).split("\\s+");
				index ++;
				
				int numActive = featureItems.length;
				int[] idx_list = new int[numActive];
				double[] valueList = new double[numActive];
				for(int j=0; j < numActive; j++)
				{
					String[] idx_value = featureItems[j].split(":"); 
					idx_list[j] = Integer.parseInt(idx_value[0]);
					valueList[j] = Double.parseDouble(idx_value[1]);
				}
				
				ri.featureList.add(new SparseFeatureVector(idx_list, valueList));
			}
			
			RankingLabel labeledRerankingIns = new RankingLabel(
					bestItem);

			sp.addExample(ri, labeledRerankingIns);
		}
		return sp;
	}

}
