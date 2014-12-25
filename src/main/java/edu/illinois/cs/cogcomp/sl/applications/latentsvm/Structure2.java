package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;


public class Structure2 implements IStructure {

	public final boolean[] yOriginal;
	private final Instance2 x;
	public final boolean[] h;
	public final boolean[] y;

	public Structure2(Instance2 x, boolean[] bs) {

		this.x = x;
		this.yOriginal = bs;

		boolean[] b1 = new boolean[2];
		boolean[] b2 = new boolean[2];

		System.arraycopy(bs, 0, b1, 0, 2);
		System.arraycopy(bs, 2, b2, 0, 2);

		switch (x.dataset) {
		case ONE:
			h = b2;
			y = b1;
			break;
		case TWO:
			h = b1;
			y = b2;
			break;
		default:
			h = null;
			y = null;
		}
	}

	public IFeatureVector getFeatureVector() {

		Map<String, Double> features = new HashMap<String, Double>();
		features.put("BIAS", 1d);

		for (int yi = 0; yi < yOriginal.length; yi++) {
			for (int xi = 0; xi < x.x.length; xi++) {
				String feature = DataGenerator2.getUnigramFeature(yi,
						yOriginal[yi], xi);
				setFeature(features, xi, feature);
			}
		}

		for (int yi = 0; yi < yOriginal.length; yi++) {
			for (int yj = 0; yj < yOriginal.length; yj++) {
				for (int xi = 0; xi < x.x.length; xi++) {
					String feature = DataGenerator2.getBigramFeature(yi,
							yOriginal[yi], yj, yOriginal[yj], xi);

					setFeature(features, xi, feature);
				}
			}
		}

		return DataGenerator2.lexicon.convertToFeatureVector(features);

	}

	private void setFeature(Map<String, Double> features, int xi, String feature) {
		double value = x.x[xi];
		if (features.containsKey(feature))
			value += features.get(feature);
		features.put(feature, value);
	}

	@Override
	public String toString() {
		return x + ": " + Arrays.toString(yOriginal);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(h);
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + Arrays.hashCode(y);
		result = prime * result + Arrays.hashCode(yOriginal);
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
		Structure2 other = (Structure2) obj;
		if (!Arrays.equals(h, other.h))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (!Arrays.equals(y, other.y))
			return false;
		if (!Arrays.equals(yOriginal, other.yOriginal))
			return false;
		return true;
	}

}
