package edu.illinois.cs.cogcomp.sl.applications.reranking;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class RankingInstance implements IInstance{
	public String example_id;
	public List<IFeatureVector> featureList;
	public List<Float> scoreList;// score that compares to the gold data
	public List<String> viewNameList;// score that compares to the gold data

	public RankingInstance(){
		featureList = new ArrayList<IFeatureVector>();
		scoreList = new ArrayList<Float>();
		viewNameList = new ArrayList<String>();
	}
	
	
	public double size() {
		return 1.0;
	}
}
