package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Random;

import edu.illinois.cs.cogcomp.sl.applications.latentsvm.Instance2.Dataset;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class DataGenerator2 {
	private final static Random random = new Random(31);
	public static final int DIM = 4;

	public static final Lexiconer lexicon = new Lexiconer();

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

	public static SLProblem generateData(int n1, int n2, int n3,
			double noisePercent, int numExtraDim) {

		SLProblem p = new SLProblem();

		int[] n = new int[] { n1, n2, n3 };
		Dataset[] d = new Dataset[] { Dataset.ONE, Dataset.TWO, Dataset.BOTH };

		for (int dId = 0; dId < 3; dId++) {
			for (int i = 0; i < n[dId]; i++) {
				double[] point = new double[DIM + numExtraDim];
				for (int j = 0; j < DIM + numExtraDim; j++) {
					point[j] = (random.nextDouble() - 0.5) * 2;
				}

				Instance2 x = new Instance2(point, d[dId]);

				boolean[] bs = labelPoint(point, d[dId], noisePercent);
				Structure2 label = new Structure2(x, bs);

				p.instanceList.add(x);
				p.goldStructureList.add(label);

			}
		}

		return p;
	}

	public static SLProblem randomizeNoise(SLProblem p) {

		SLProblem p1 = new SLProblem();
		for (int i = 0; i < p.size(); i++) {
			Structure2 s = (Structure2) p.goldStructureList.get(i);

			Instance2 x = (Instance2) p.instanceList.get(i);

			Dataset dataset = x.dataset;
			boolean[] b = s.yOriginal;
			if (dataset == Dataset.ONE) {
				b[2] = random.nextBoolean();
				b[3] = random.nextBoolean();
			} else if (dataset == Dataset.TWO) {
				b[0] = random.nextBoolean();
				b[1] = random.nextBoolean();
			}

			Structure2 s1 = new Structure2(x, b);
			p1.instanceList.add(x);
			p1.goldStructureList.add(s1);

		}
		return p1;
	}

	private static boolean[] labelPoint(double[] p, Dataset dataset,
			double noiseFraction) {
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

		for (int i = 0; i < b.length; i++) {
			if (random.nextDouble() < noiseFraction)
				b[i] = !b[i];
		}

		if (dataset == Dataset.ONE) {
			b[2] = random.nextBoolean();
			b[3] = random.nextBoolean();
		} else if (dataset == Dataset.TWO) {
			b[0] = random.nextBoolean();
			b[1] = random.nextBoolean();
		}

		return b;
	}

}
