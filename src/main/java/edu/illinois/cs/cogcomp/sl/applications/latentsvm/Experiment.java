package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.core.stats.OneVariableStats;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.core.utilities.Table;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;

public class Experiment {
	private static final int NUM_TRIALS = 1;

	public static void ssvmExpt() throws Exception {

		DataGenerator2.previewFeatures(4);

		List<Integer> l = ArrayUtilities.asIntList(new int[] { 100, 0 });

		List<List<Integer>> list = Arrays.asList(l, l, l);

		double[] noises = new double[] { 0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3,
				0.35, 0.4 };
		int[] numExtraDimensions = new int[] { 0, 5, 10, 20, 100 };

		Table output = new Table();
		output.addColumn("Size(D1)");
		output.addColumn("Size(D2)");
		output.addColumn("Size(D3)");
		output.addColumn("P(Noise)");
		output.addColumn("Dimensionality");
		output.addColumn("Mean Acc");
		output.addColumn("Std Acc");
		output.addColumn("Error profile");

		for (List<Integer> input : Permutations.crossProduct(list)) {

			int n1 = input.get(0);
			int n2 = input.get(1);
			int n3 = input.get(2);
			if (n1 + n2 + n3 == 0)
				continue;

			OneVariableStats f1 = new OneVariableStats();
			OneVariableStats acc = new OneVariableStats();

			for (double noise : noises) {
				for (int numExtraDim : numExtraDimensions) {

					int dimensionality = 4 + numExtraDim;
					System.out
							.println("Input: " + input + "\t noise = " + noise
									+ "\t numDimensions = " + (4 + numExtraDim));
					int[] errorProfile = new int[4];
					for (int trial = 0; trial < NUM_TRIALS; trial++) {

						SLProblem train = DataGenerator2.generateData(
								n1, n2, n3, noise, numExtraDim);

						SLParameters params = new SLParameters();
						params.C_FOR_STRUCTURE=10;
						params.MAX_NUM_ITER= 1000;

//						L2LossParallelJLISLearner learner = new L2LossParallelJLISLearner();
//
//						Pair<WeightVector, Double> w = learner
//								.parallelTrainStructuredSVM(getInference2(),
//										train, params);
						SLProblem test = DataGenerator2.generateData(0,
								0, 100, 0d, numExtraDim);

						Pair<ClassificationTester, int[]> perf = evaluate2(
								test, new AInference(4), w.getFirst());
						f1.add(perf.getFirst().getAverageF1());
						acc.add(perf.getFirst().getAverageAccuracy());

						for (int i = 0; i < perf.getSecond().length; i++) {
							errorProfile[i] += perf.getSecond()[i];
						}

					}

					String mA = StringUtils.getFormattedString(acc.mean(), 4);
					String sA = StringUtils.getFormattedString(acc.std(), 4);

					output.addRow(new String[] { n1 + "", n2 + "", n3 + "",
							StringUtils.getFormattedString(noise, 3),
							dimensionality + "", mA, sA,
							Arrays.toString(errorProfile) });
				}
			}
		}

		System.out.println(output.toOrgTable());
	}
}
