package edu.illinois.cs.cogcomp.sl.applications.ranking;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class RerankingInstance implements IInstance{
	public String example_id;
	public List<IFeatureVector> fea_list;
	public List<Float> score_list;// score that compares to the gold data
	public List<String> view_name_list;// score that compares to the gold data

	public RerankingInstance(){
		fea_list = new ArrayList<IFeatureVector>();
		score_list = new ArrayList<Float>();
		view_name_list = new ArrayList<String>();
	}
	
	
	public double size() {
		return 1.0;
	}
}
