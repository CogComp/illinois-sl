package edu.illinois.cs.cogcomp.sl.applications.latentsvm;

import java.util.Arrays;

import edu.illinois.cs.cogcomp.sl.core.IInstance;


public class Instance implements IInstance {

	public final double[] x;

	public Instance(double[] x) {
		this.x = x;
	}

	public double size() {
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Instance other = (Instance) obj;
		if (!Arrays.equals(x, other.x))
			return false;
		return true;
	}

}
