package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Arrays;

import edu.illinois.cs.cogcomp.sl.core.IInstance;

public class Instance2 implements IInstance {
	public final Dataset dataset;
	public final double[] x;

	public static enum Dataset {
		ONE, TWO, BOTH
	}

	public Instance2(double[] x, Dataset dataset) {
		this.x = x;
		this.dataset = dataset;

	}

	public double size() {
		return 1;
	}

	@Override
	public String toString() {
		return dataset + ": " + Arrays.toString(x);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
		result = prime * result + Arrays.hashCode(x);
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
		Instance2 other = (Instance2) obj;
		if (dataset != other.dataset)
			return false;
		if (!Arrays.equals(x, other.x))
			return false;
		return true;
	}

}
