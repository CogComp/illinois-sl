package edu.illinois.cs.cogcomp.sl.applications.latentsvm2;

import edu.illinois.cs.cogcomp.sl.core.IStructure;

public class GoldLatentPair implements IStructure {
	
	private IStructure y;
	private IStructure h;
	public IStructure getY() {
		return y;
	}
	public void setY(IStructure y) {
		this.y = y;
	}
	public IStructure getH() {
		return h;
	}
	public void setH(IStructure h) {
		this.h = h;
	}
	
	
}
