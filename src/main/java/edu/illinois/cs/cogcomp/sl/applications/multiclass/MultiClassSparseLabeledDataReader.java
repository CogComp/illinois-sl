package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.SparseFeatureVector;

public class MultiClassSparseLabeledDataReader {

	public static float[][] getCostMatrix(Map<String, Integer> labels_maping,String fname) throws Exception{
		int n_lab = labels_maping.size();
		
		System.out.println("Read Cost Matrix...");
		float[][] res = new float[n_lab][n_lab];
		for(int i=0;i  < n_lab ;i ++){
			for(int j=0; j < n_lab; j ++){
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
				throw new Exception("Format error in the cost matrix file. Each line should have only three tokens!");
			if (!labels_maping.containsKey(tokens[0]))
				throw new Exception("Format error in the cost matrix file. In training data, label (" + tokens[0] +") does not exist!"); 
			if (!labels_maping.containsKey(tokens[1]))
				throw new Exception("Format error in the cost matrix file. In training data, label (" + tokens[1] +") does not exist!");
						
			int i = labels_maping.get(tokens[0]);
			int j = labels_maping.get(tokens[1]);
			float cost = -1;
			
			try{
				cost = Float.parseFloat(tokens[2]);				
			}catch(NumberFormatException e){
				throw new Exception("Format error in the cost matrix file. The cost should be a number!");
			}
			
			if (i ==j && cost !=0 )
				throw new Exception("The cost needs to be zero when pred == gold!");
			
			if (cost < 0)
				throw new Exception("The cost (distance) between two classes cannot be negative!");
			res[i][j] = cost;
		}		
		System.out.println("Done!");
		return res;
	}
	
	
	private static int checkNumOfFeaturesAndBuildClassMapping(String fname,
			Map<String, Integer> labels_maping) throws Exception {
		int n_features = 0;
		
		ArrayList<String> lines = LineIO.read(fname);

		// phase 1: check if the output labels is from 0,1,to something, check
		// the maximum number of features
		for (String line : lines) {			
			String[] tokens = line.split("\\s+");
			String lab = tokens[0];

			// put the lab names into labels_mapping
			if (!labels_maping.containsKey(lab)) {
				int lab_size = labels_maping.size();
				labels_maping.put(lab, lab_size);
			}

			for (int i = 1; i < tokens.length; i++) {
				String[] fea_tokens = tokens[i].split(":");
				
				if (fea_tokens.length != 2){
					throw new Exception("Format error in the input file! in >" + line +"<");
				}
				
				int idx = Integer.parseInt(fea_tokens[0]);

				if (idx <= 0) {
					throw new Exception(
							"The feature index needs to be >= 1 !");
				}

				if (idx > n_features) {
					n_features = idx;
				}
			}
		}

		n_features ++; //allocate for zero 
		n_features ++; //allocate for the bias term		
		
		System.out.println("Label Mapping (external=>internal): "
				+ labels_maping.toString().replace("=", "==>"));
		System.out.println("n_train_base_fea:" + n_features);

		return n_features;
	}

	/**
	 * Read training data for JLIS-Multiclass
	 * 
	 * @param fname
	 *            The filename contains the training data
	 * @return A LabeledMulticlasssData
	 * @throws Exception
	 */
	public static LabeledMulticlassData readTrainingData(String fname)
			throws Exception {
		Map<String, Integer> labels_maping = new HashMap<String, Integer>();
		int n_feature = checkNumOfFeaturesAndBuildClassMapping(fname,
				labels_maping);
		int n_class = labels_maping.size();

		System.out.println("n_feature (should be number of features in the training data +2 ): " + n_feature);
		System.out.println("n_class: " + n_class);
		LabeledMulticlassData res = new LabeledMulticlassData(labels_maping,
				n_feature);
		readMultiClassDataAndAddBiasTerm(fname, labels_maping, n_feature, n_class, res);
		return res;
	}

	/**
	 * Read testing data for JLIS-Multiclass. Unlike {@link
	 * this#readTrainingData(String)}, the labels_mapping and the number feature
	 * are from training.
	 * 
	 * @param fname
	 *            The filename contains the testing data
	 * @return A LabeledMulticlasssData
	 * @throws Exception
	 */
	public static LabeledMulticlassData readTestingData(String fname,
			Map<String, Integer> labels_maping, int n_feature) throws Exception {
		int n_class = labels_maping.size();
		LabeledMulticlassData res = new LabeledMulticlassData(labels_maping,
				n_feature);
		readMultiClassDataAndAddBiasTerm(fname, labels_maping, n_feature, n_class, res);
		return res;
	}

	private static void readMultiClassDataAndAddBiasTerm(String fname,
			Map<String, Integer> labels_maping, int n_feature, int n_class,
			LabeledMulticlassData res) throws FileNotFoundException {
		ArrayList<String> lines = LineIO.read(fname);
		for (String line : lines) {
			String[] tokens = line.split("\\s+");

			int active_len = 1;

			// ignore the features > n_features
			for (int i = 1; i < tokens.length; i++) {

				String[] fea_tokens = tokens[i].split(":");
				int idx = Integer.parseInt(fea_tokens[0]); // allocate for
															// bias term
				if (idx <= n_feature) { // only consider the features that has
										// index
										// less than n_fea!!
					active_len++;
				}
			}

			// System.out.println("active_len:" + active_len);
			int[] idx_list = new int[active_len];
			float[] value_list = new float[active_len];

			for (int i = 1; i < tokens.length; i++) {
				String[] fea_tokens = tokens[i].split(":");
				int idx = Integer.parseInt(fea_tokens[0]); // allocate for
															// bias term
				if (idx <= n_feature) { // only consider the features that has
										// index
					// less than n_fea!!
					idx_list[i - 1] = idx;
					value_list[i - 1] = Float.parseFloat(fea_tokens[1]);
				}
			}
			// append bias term
			idx_list[active_len-1] = n_feature-1;
			value_list[active_len-1] = 1;

			IFeatureVector fv = new SparseFeatureVector(idx_list, value_list);
			MultiClassInstance mi = new MultiClassInstance(n_feature, n_class,
					fv);
			res.sp.instanceList.add(mi);

			String lab = tokens[0];
			if (labels_maping.containsKey(lab)) {
				res.sp.goldStructureList.add(new LabeledMulticlassStructure(mi,
						labels_maping.get(lab)));
			} else {
				// only design for unknown classes in the test data
				res.sp.goldStructureList.add(new LabeledMulticlassStructure(mi, -1));
			}
		}
	}

	// public static BinaryProblem getBinaryData(String name, int n_fea,
	// int n_class) throws Exception {
	// // artificially replace the labels to create indirect supervision
	// LabeledMulticlassData raw_binary_data = MultiClassSparseLabeledDataReader
	// .getDataWithBiasFeature(name, n_fea, n_class);
	// List<Integer> binary_labels = new ArrayList<Integer>();
	//
	// for (int i = 0; i < raw_binary_data.output_list.size(); i++) {
	// LabeledMultiClassStructure output = (LabeledMultiClassStructure)
	// raw_binary_data.output_list
	// .get(i);
	// if (output.output == 0)
	// binary_labels.add(-1);
	// else if (output.output == 1)
	// binary_labels.add(+1);
	// else {
	// System.out.println(">>>" + output.output);
	// throw new Exception("wrong format in binary data files");
	// }
	// }
	// // the one we will used in JLIS
	// BinaryProblem bp = new BinaryProblem();
	// bp.input_list = raw_binary_data.input_list;
	// bp.output_list = binary_labels;
	//
	// return bp;
	// }

}
