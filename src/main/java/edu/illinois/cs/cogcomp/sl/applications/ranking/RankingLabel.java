package edu.illinois.cs.cogcomp.sl.applications.ranking;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;


public class RankingLabel implements IStructure{
	public final int pred_item;
	
	public RankingLabel(int best_item){
		this.pred_item = best_item;
	}
	
	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof RankingLabel))
			return false;

		// cast to native object is now safe
		RankingLabel that =  (RankingLabel) aThat;
	

		if (this.pred_item != that.pred_item)
			return false;
		return true;
	}
}
