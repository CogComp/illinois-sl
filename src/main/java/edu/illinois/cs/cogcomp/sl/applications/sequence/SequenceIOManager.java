package edu.illinois.cs.cogcomp.sl.applications.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
public class SequenceIOManager {
	static Logger logger = LoggerFactory.getLogger(SequenceIOManager.class);
	/**
	 * File format: odd lines: words even lines: tags
	 * 
	 * @param fname
	 * @return
	 * @throws IOException
	 */
	public static int numFeatures;
	public static int numLabels;
	
	 public static SLProblem readProblem(String fname, Boolean fixFeatureNum) throws IOException, Exception {
		 	List<String> strList = LineIO.read(fname);
		 	List<String> qids = new ArrayList<String>();
			SLProblem sp = new SLProblem();
			Map<String, Pair<List<Integer>, List<IFeatureVector>>> insMap = new HashMap<String, Pair<List<Integer>,List<IFeatureVector>>>();
			Set<Integer> indeices = new HashSet<Integer>(); 
			if(!fixFeatureNum){
				numFeatures = 0;
				numLabels = 0;
			}
			for(String line : strList){
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			    String token;
			    if(!line.contains("qid"))
			    		throw new Exception("invalid input String or empty line: "+ line);
                
			    try {
                    token = st.nextToken();
                } catch (NoSuchElementException e) {
                    throw new Exception("empty line", e);
                }
                int label = Integer.parseInt(token)-1;
                if(label < 0)
                	new Exception("label Id starts from 1");
                if(fixFeatureNum && label > numLabels)
                	throw new Exception("Some labels appear in test data do not appear in training");
                if(label > numLabels)
                	numLabels = label;
                
                // take out qid:
                token = st.nextToken();
                String qid = st.nextToken();
                
                
                if(!insMap.containsKey(qid)){
                	qids.add(qid);
                	insMap.put(qid, new Pair<List<Integer>, List<IFeatureVector>>(new ArrayList<Integer>(), new ArrayList<IFeatureVector>()));
                }
                

                
                FeatureVectorBuffer fvb = new FeatureVectorBuffer();
                
                while(st.countTokens()>0){
                	int index = Integer.parseInt(st.nextToken());
                	float value = Float.parseFloat(st.nextToken());
                	if(index < 0)
                		throw new Exception("invalid index: " + index);
                	if(fixFeatureNum && index+1 > numFeatures)
                		continue;
                	if(index+1 > numFeatures)
                		numFeatures = index+1;
                	indeices.add(index);
                	fvb.addFeature(index+1, value);
                }
                // put label
                insMap.get(qid).getFirst().add(label);
                insMap.get(qid).getSecond().add(fvb.toFeatureVector());
			}
			for(String qid : qids){
				Pair<List<Integer>, List<IFeatureVector>> ins = insMap.get(qid);				
				List<IFeatureVector> fvs = ins.getSecond();
				List<Integer> labels = ins.getFirst();
				int[] labelArray = new int[labels.size()];
				SequenceInstance seq = new SequenceInstance(fvs.toArray(new IFeatureVector[fvs.size()]));
				sp.instanceList.add(seq);
				for(int i=0; i<labels.size(); i++)
					labelArray[i] = labels.get(i);
				sp.goldStructureList.add(new SequenceLabel(labelArray));
			}
			System.out.println(numFeatures);
			System.out.println(indeices.size());
			return sp;
	 }
}
