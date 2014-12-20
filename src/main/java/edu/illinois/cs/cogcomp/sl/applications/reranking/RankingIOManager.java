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
