package edu.illinois.cs.cogcomp.sl.applications.multiclass;

import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;

public class MultiClassTrainer {

	public static Pair<int[],int[]> getPredictionResults(MulticlassModel model,
			LabeledMulticlassData test) throws Exception {
		int[] gold = new int[test.sp.size()];
		int[] pred = new int[test.sp.size()];
		for (int i = 0; i < test.sp.size(); i++) {
			LabeledMulticlassStructure prediction = (LabeledMulticlassStructure) model.infSolver
					.getBestStructure(model.wv, test.sp.instanceList.get(i));
			gold[i] = ((LabeledMulticlassStructure) test.sp.goldStructureList
					.get(i)).output;
			pred[i] = prediction.output;			
		}
		return new Pair<int[], int[]>(gold, pred);
		
	}
	
	public static float getTestingAcc(int[] gold, int[] pred) throws Exception {
		float acc = 0.0f;
		for (int i = 0; i < gold.length; i++) {
			if (gold[i]==pred[i])
				acc += 1.0;
		}
		return acc / gold.length;
	}

	public static float getTestingCost(int[] gold, int[] pred, float[][] cost_matirx)
			throws Exception {
		float cost = 0.0f;
		for (int i = 0; i < gold.length; i++) {			
			cost += cost_matirx[gold[i]][pred[i]];
		}
		return cost / gold.length;
	}

	public static float crossValidation(float C, int n_thread, int n_fold,
			LabeledMulticlassData train) throws Exception {
		float cv_res = 0;
		List<Pair<SLProblem, SLProblem>> data_pair_list = train.sp
				.splitDataToNFolds(n_fold, new Random(0));
		for (int i = 0; i < n_fold; i++) {
			SLProblem cv_sp_train = data_pair_list.get(i).getFirst();
			SLProblem cv_sp_test = data_pair_list.get(i).getSecond();

			LabeledMulticlassData cv_train = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_train);
			LabeledMulticlassData cv_test = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_test);

			MulticlassModel cv_model = trainMultiClassModel(C, n_thread,
					cv_train);
			Pair<int[], int[]> gold_pred = getPredictionResults(cv_model, cv_test);
			float test_acc = getTestingAcc(gold_pred.getFirst(), gold_pred.getSecond());
			cv_res += test_acc;
			System.out.println("Fold " + i + ":" + test_acc);
		}
		return cv_res / n_fold;
	}

	public static MulticlassModel trainMultiClassModel(float C, int n_thread,
			LabeledMulticlassData train) throws Exception {
		MulticlassModel model = new MulticlassModel();
		model.labelMapping = train.label_mapping; // for the bias term
		model.numOfBaseFeatures = train.n_base_feature_in_train;

		model.para = new SLParameters();

		// para.total_number_features = train.label_mapping.size() *
		// train.n_base_feature_in_train;
		model.para.C_FOR_STRUCTURE = C;
		model.para.TRAINMINI = true;
		model.featureGenerator = new MultiClassFeatureGenerator();


		System.out.println("Initializing Solvers...");
		System.out.flush();
		AbstractInferenceSolver[] s_finder_list = new AbstractInferenceSolver[n_thread];
		for (int i = 0; i < s_finder_list.length; i++) {
			s_finder_list[i] = new MultiClassStructureFinder();
		}
		System.out.println("Done!");
		System.out.flush();

		model.infSolver = s_finder_list[0];
		Learner learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, model.para);


		// train model
		model.wv = learner.train(train.sp);
		return model;
	}

	public static MulticlassModel trainCostSensitiveMultiClassModel(float C,
			int n_thread, LabeledMulticlassData train, float[][] cost_matrix)
			throws Exception {
		MulticlassModel model = new MulticlassModel();
		model.labelMapping = train.label_mapping; // for the bias term
		model.numOfBaseFeatures = train.n_base_feature_in_train;

		model.para = new SLParameters();
		//model.para.verbose_level = JLISParameters.VLEVEL_HIGH;
		model.cost_matrix = cost_matrix;

		// para.total_number_features = train.label_mapping.size() *
		// train.n_base_feature_in_train;
		model.para.C_FOR_STRUCTURE = C;
		model.para.TRAINMINI = true;
		model.featureGenerator = new MultiClassFeatureGenerator();

		System.out.println("Initializing Solvers...");
		System.out.flush();
		AbstractInferenceSolver[] s_finder_list = new AbstractInferenceSolver[n_thread];
		for (int i = 0; i < s_finder_list.length; i++) {
			s_finder_list[i] = new MultiClassStructureFinder(cost_matrix);
		}
		System.out.println("Done!");
		System.out.flush();

		model.infSolver = s_finder_list[0];
		Learner learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, model.para);

		// train model
		model.wv = learner.train(train.sp); 
		return model;
	}

	public static Pair<Float,Float> crossValidation(float C, int n_thread, int n_fold,
			LabeledMulticlassData train, float[][] cost_matrix) throws Exception {
		float cv_acc = 0;
		float cv_cost = 0;
		
		List<Pair<SLProblem, SLProblem>> data_pair_list = train.sp
				.splitDataToNFolds(n_fold, new Random(0));
		for (int i = 0; i < n_fold; i++) {
			SLProblem cv_sp_train = data_pair_list.get(i).getFirst();
			SLProblem cv_sp_test = data_pair_list.get(i).getSecond();

			LabeledMulticlassData cv_train = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_train);
			LabeledMulticlassData cv_test = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_test);

			MulticlassModel cv_model = trainCostSensitiveMultiClassModel(C, n_thread,
					cv_train,cost_matrix);
			Pair<int[], int[]> gold_pred = getPredictionResults(cv_model, cv_test);
			float test_acc = getTestingAcc(gold_pred.getFirst(), gold_pred.getSecond());
			cv_acc += test_acc;
			System.out.println("Fold " + i + " acc :" + test_acc);
						
			float test_cost = getTestingCost(gold_pred.getFirst(), gold_pred.getSecond(), cost_matrix);
			cv_cost += test_cost;
			System.out.println("Fold " + i + " cost :" + test_cost);
		}
		return new Pair<Float, Float>(cv_acc/n_fold, cv_cost/n_fold);
	}

}
