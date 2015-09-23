/*******************************************************************************
 * University of Illinois/NCSA Open Source License
 * Copyright (c) 2010, 
 *
 * Developed by:
 * The Cognitive Computations Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
 * Neither the names of the Cognitive Computations Group, nor the University of Illinois at Urbana-Champaign, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *     
 *******************************************************************************/
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
