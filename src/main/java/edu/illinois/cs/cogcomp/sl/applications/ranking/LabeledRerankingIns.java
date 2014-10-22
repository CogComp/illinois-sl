package edu.illinois.cs.cogcomp.sl.applications.ranking;

import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;


public class LabeledRerankingIns implements IStructure{
	public RerankingInstance input;
	public final int pred_item;
	
	public LabeledRerankingIns(RerankingInstance ri, int best_item){
		this.input= ri;
		this.pred_item = best_item;
	}
	
	@Override
	public boolean equals(Object aThat) {
		// check for self-comparison
		if (this == aThat)
			return true;

		if (!(aThat instanceof LabeledRerankingIns))
			return false;

		// cast to native object is now safe
		LabeledRerankingIns that =  (LabeledRerankingIns) aThat;
	

		if (!this.input.equals(that.input))
			return false;
		else {
			if (this.input != that.input)
				return false;
			if (this.pred_item != that.pred_item)
				return false;
			return true;
		}
	}

	public IFeatureVector getFeatureVector() {
		return input.fea_list.get(pred_item);
	}
	
}
