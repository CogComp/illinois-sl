package edu.illinois.cs.cogcomp.sl.applications.ranking;

import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class RankDataReader {
	public static SLProblem readFeatureFile(String fea_file_name)
			throws IOException {
		int index = 0;
		SLProblem sp = new SLProblem();
		ArrayList<String> fea_lines = LineIO.read(fea_file_name);

		while (index < fea_lines.size()) {
			RankingInstance ri = new RankingInstance();

			String[] tokens = fea_lines.get(index).split("\\s+");
			index++;

			int n_tree = Integer.parseInt(tokens[0]);
			ri.example_id = tokens[1];
			int best_item = -1;
			double best_score = Float.NEGATIVE_INFINITY;

			for (int i = 0; i < n_tree; i++) {
				String score_view_line = fea_lines.get(index);
				String[] items = score_view_line.split("\\s+");
				float score = Float.parseFloat(items[0]);
				if (score > best_score){
					best_item = i;
					best_score = score;
				}
				ri.score_list.add(score);
				ri.view_name_list.add((items[1]));
				index++;
				
				String[] fea_items = fea_lines.get(index).split("\\s+");
				index ++;
				
				int n_active = fea_items.length;
				int[] idx_list = new int[n_active];
				double[] value_list = new double[n_active];
				for(int j=0; j < n_active; j++)
				{
					String[] idx_value = fea_items[j].split(":"); 
					idx_list[j] = Integer.parseInt(idx_value[0]);
					value_list[j] = Double.parseDouble(idx_value[1]);
				}
				
				ri.fea_list.add(new SparseFeatureVector(idx_list, value_list));

			}
			
			RankingLabel labeledRerankingIns = new RankingLabel(
					best_item);

			sp.addExample(ri, labeledRerankingIns);
		}

		return sp;
	}

}
