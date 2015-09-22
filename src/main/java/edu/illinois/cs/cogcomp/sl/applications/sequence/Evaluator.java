package edu.illinois.cs.cogcomp.sl.applications.sequence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner.ProgressReportFunction;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
public class Evaluator  implements ProgressReportFunction{
	List<WeightVector> wvList;
	List<Double> runningTime;
	int numIterPerProgress = 0;
	double startTime = 0;
	public Evaluator(int numIterPerProgress){
		wvList = new ArrayList<WeightVector>();
		startTime = System.currentTimeMillis();
		runningTime = new ArrayList<Double>();
		this.numIterPerProgress = numIterPerProgress;
	}
	@Override
	public void run(WeightVector w, AbstractInferenceSolver inference)
			throws Exception {
		float [] array = new float[w.getInternalArray().length];
		for(int i=0 ;i< array.length;i++)
			array[i] = w.getInternalArray()[i];
			
		WeightVector wv = new WeightVector(array);
		wvList.add(wv);
		runningTime.add(System.currentTimeMillis() - startTime);
	}
	public void postEvaluation(SLProblem sp, AbstractInferenceSolver infSolver) throws Exception{
		for(int i=0; i< wvList.size();i++){
			double testAcc = evaluate(sp, wvList.get(i), infSolver,null);
			System.out.println("Iter " + numIterPerProgress*(i+1) + ": time " + runningTime.get(i)/1000 + " Acc: " + testAcc);
		}
	}
	public static double evaluate(SLProblem sp, WeightVector wv, AbstractInferenceSolver infSolver, String outputFileName) throws Exception{
		int total = 0;
		double acc = 0;
		BufferedWriter writer = null;
		if(outputFileName!=null){
			writer = new BufferedWriter(new FileWriter(outputFileName));
		}
		for (int i = 0; i < sp.instanceList.size(); i++) {

			SequenceLabel gold = (SequenceLabel) sp.goldStructureList
					.get(i);
			SequenceLabel prediction = (SequenceLabel) infSolver
					.getBestStructure(wv, sp.instanceList.get(i));
			if(outputFileName!=null){
				for(int j=0; j< prediction.tags.length; j++){
					writer.write(String.valueOf(prediction.tags[j]+1)+"\n");
				}
			}
            	
			for (int j = 0; j < prediction.tags.length; j++) {
				total += 1.0;
				if (prediction.tags[j] == gold.tags[j]){
					acc += 1.0;
				}
			}
		}
		if(writer!=null){
			writer.close();
		}
		return acc/total;

	}
	public List<WeightVector> getWvList() {
		// TODO Auto-generated method stub
		return wvList;
	}
	
}
