package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;


public class Structure implements IStructure {

	public final boolean[] y;
	private final double[] x;

	public Structure(double[] x, boolean[] bs) {

		assert x.length == DataGenerator.DIM;
		this.x = x;
		this.y = bs;

	}

	public IFeatureVector getFeatureVector() {

		Map<String, Double> features = new HashMap<String, Double>();
		features.put("BIAS", 1d);

		for (int yi = 0; yi < y.length; yi++) {
			for (int xi = 0; xi < x.length; xi++) {
				String feature = DataGenerator.getUnigramFeature(yi, y[yi], xi);
				setFeature(features, xi, feature);
			}
		}

		for (int yi = 0; yi < y.length; yi++) {
			for (int yj = 0; yj < y.length; yj++) {
				for (int xi = 0; xi < x.length; xi++) {
					String feature = DataGenerator.getBigramFeature(yi, y[yi],
							yj, y[yj], xi);

					setFeature(features, xi, feature);
				}
			}
		}

		return DataGenerator.lexicon.convertToFeatureVector(features);

	}

	private void setFeature(Map<String, Double> features, int xi, String feature) {
		double value = x[xi];
		if (features.containsKey(feature))
			value += features.get(feature);
		features.put(feature, value);
	}

	@Override
	public String toString() {
		return Arrays.toString(y);
	}

	@Override
	public int hashCode() {
		final int prime = 127;
		int result = 1;
		result = prime * result + Arrays.hashCode(x);
		result = prime * result + Arrays.hashCode(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Structure other = (Structure) obj;
		if (!Arrays.equals(x, other.x))
			return false;
		if (!Arrays.equals(y, other.y))
			return false;
		return true;
	}

}
