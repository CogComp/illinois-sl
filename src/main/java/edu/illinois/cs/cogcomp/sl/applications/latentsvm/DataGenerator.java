package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class DataGenerator {

	private final static Random random = new Random();
	public static final int DIM = 4;

	public static final Lexiconer lexicon = new Lexiconer();

	public static List<Pair<double[], boolean[]>> generatePoints(int numPoints,
			int dimensionality) {

		List<Pair<double[], boolean[]>> list = new ArrayList<Pair<double[], boolean[]>>();

		assert dimensionality >= 3;

		for (int i = 0; i < numPoints; i++) {
			double[] point = new double[dimensionality];
			for (int j = 0; j < dimensionality; j++) {
				point[j] = (random.nextDouble() - 0.5) * 2;
			}
			list.add(new Pair<double[], boolean[]>(point, labelPoint(point)));
		}
		return list;
	}

	public static void previewFeatures(int dimensionality) {

		for (int yi = 0; yi < 4; yi++) {
			for (boolean y : new boolean[] { true, false }) {
				for (int i = 0; i < dimensionality; i++) {
					String feature = getUnigramFeature(yi, y, i);
					lexicon.addFeature(feature);
				}

				for (int yj = 0; yj < 4; yj++) {
					if (yi == yj)
						continue;
					for (boolean yy : new boolean[] { true, false }) {
						for (int i = 0; i < dimensionality; i++) {
							String feature = getBigramFeature(yi, y, yj, yy, i);
							lexicon.addFeature(feature);
						}
					}
				}

			}
		}

	}

	public static String getBigramFeature(int yi, boolean y, int yj,
			boolean yy, int i) {
		String feature = "y" + yi + "=" + y + ",y" + yj + "=" + yy + ":x" + i;
		return feature;
	}

	public static String getUnigramFeature(int yi, boolean y, int xi) {
		String feature = "y" + yi + "=" + y + ":x" + xi;
		return feature;
	}

	public static boolean[] labelPoint(double[] p) {

		double x = p[0];
		double y = p[1];
		double z = p[2];
		double w = p[3];

		int sx = (int) Math.signum(x);
		int sy = (int) Math.signum(y);
		int sz = (int) Math.signum(z);
		int sw = (int) Math.signum(w);

		boolean[] b = new boolean[4];

		b[0] = sx == sy;
		b[1] = z >= 0;
		b[2] = sz == sw;
		b[3] = x >= 0;

		return b;

	}

	public static SLProblem createProblem(int n) {
		List<Pair<double[], boolean[]>> points1 = generatePoints(n, DIM);

		SLProblem problem = new SLProblem();
		problem.instanceList= new ArrayList<IInstance>();
		problem.goldStructureList= new ArrayList<IStructure>();

		for (Pair<double[], boolean[]> example : points1) {
			problem.instanceList.add(new Instance(example.getFirst()));
			problem.goldStructureList.add(new Structure(example.getFirst(), example
					.getSecond()));
		}

		return problem;

	}

	public static void main(String[] args) {
		int n = 10000;

		List<Pair<double[], boolean[]>> points = generatePoints(n, DIM);

		Counter<String> counter = new Counter<String>();

		for (Pair<double[], boolean[]> x : points) {
			counter.incrementCount(Arrays.toString(x.getSecond()));
		}

		for (String item : counter.items()) {
			System.out.println(item + "\t" + counter.getCount(item));
		}
	}
}
