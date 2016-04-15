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
package edu.illinois.cs.cogcomp.sl.solverTest;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.zip.DataFormatException;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLParameters.LearningModelType;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner.*;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class SolverTest {

	@Test 
	public void testL2LossModel() throws Exception {
		SLParameters para = new SLParameters();
		para.LEARNING_MODEL = LearningModelType.L2LossSSVM;
		para.L2_LOSS_SSVM_SOLVER_TYPE = SolverType.DCDSolver;
		para.TRAINMINI = true;
		para.TRAINMINI_SIZE = 1;
		para.MAX_NUM_ITER = 1000;
		para.STOP_CONDITION = 0.01f;
		para.C_FOR_STRUCTURE = 0.01f;
		para.INNER_STOP_CONDITION = 0.01f;
		testModel(para, 1.0f, 12.26f );

		para.L2_LOSS_SSVM_SOLVER_TYPE = SolverType.ParallelDCDSolver;
		testModel(para, 1.0f, 12.26f );

		para.L2_LOSS_SSVM_SOLVER_TYPE = SolverType.DEMIParallelDCDSolver;
		para.MAX_NUM_ITER = 10000;
		para.STOP_CONDITION = 0.01f;
		para.DEMIDCD_NUMBER_OF_UPDATES_BEFORE_UPDATE_BUFFER = 1;
		para.DEMIDCD_NUMBER_OF_INF_PARSE_BEFORE_UPDATE_WV = 1;
		para.PROGRESS_REPORT_ITER = 500;
		testModel(para, 1.0f, 12.26f );
	}

	@Test
	public void testSPModel() throws Exception {
		SLParameters para = new SLParameters();
		para.LEARNING_MODEL = LearningModelType.StructuredPerceptron;
		para.MAX_NUM_ITER = 100;
		para.C_FOR_STRUCTURE = 0.01f;
		testModel(para, 1.0f, 0.0f );

		para.MAX_NUM_ITER = 10;
		para.LEARNING_MODEL = LearningModelType.StructuredPerceptronIPM;
		testModel(para, 1.0f, 0.0f );
	}

	@Test
	public void testSplit() throws Exception {
		SLModel model = new SLModel();
		SLParameters para = new SLParameters();
		model.lm = new Lexiconer();
		SLProblem sp = getStructuredData(model.lm);
		sp.splitTrainTest(1);
		sp = getStructuredData(model.lm);
		sp.splitDataToNFolds(3,new Random());
		para.loadConfigFile("config/StructuredPerceptron.config");
        assertEquals( 10, para.MAX_NUM_ITER);
	}
	public void testModel(SLParameters para, float ref_acc, float ref_obj)	throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();

		SLProblem sp = getStructuredData(model.lm);

		// Disallow the creation of new features
		model.lm.setAllowNewFeatures(false);

		// initialize the inference solver
		model.infSolver = new ViterbiInferenceSolver(model.lm);

		POSManager fg = new POSManager(model.lm);
		
		para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature()
			* model.lm.getNumOfLabels() + model.lm.getNumOfLabels()
			+ model.lm.getNumOfLabels() * model.lm.getNumOfLabels();

		// numLabels*numLabels for transition features
		// numWordsInVocab*numLabels for emission features
		// numLabels for prior on labels
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg,
				para);
		model.wv = learner.train(sp);
		WeightVector.printSparsity(model.wv);
		if(learner instanceof L2LossSSVMLearner){
			float primal_obj =  ((L2LossSSVMLearner)learner).getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE);
			System.out.println("Primal objective:" + primal_obj);
        	assertEquals( primal_obj, ref_obj, 0.1f);
		}

		double acc = 0.0;
		double total = 0.0;

		for (int i = 0; i < sp.instanceList.size(); i++) {

			POSTag gold = (POSTag) sp.goldStructureList.get(i);
			POSTag prediction = (POSTag) model.infSolver.getBestStructure(
					model.wv, sp.instanceList.get(i));

			for (int j = 0; j < prediction.tags.length; j++) {
				total += 1.0;
				if (prediction.tags[j] == gold.tags[j]) {
					acc += 1.0;
				}
			}
		}
		acc /= total;
		System.out.println("Acc: " + acc);
        assertEquals(acc, ref_acc, 0.01f);
	}

	public static SLProblem getStructuredData(Lexiconer lm)
		throws IOException, DataFormatException {
		List<String> lines = new ArrayList<String>();
		SLProblem sp = new SLProblem();

		// A simple example
		lines.add("In	fact	,	the	earnings	report	unfolded	as	representatives	of	the	world	's	No.	1	jet	maker	and	the	striking	Machinists	union	came	back	to	the	negotiating	table	for	their	first	meeting	in	two	weeks	.");
		lines.add("IN	NN	,	DT	NNS	NN	VBD	IN	NNS	IN	DT	NN	POS	NN	CD	NN	NN	CC	DT	JJ	NNS	NN	VBD	RB	TO	DT	NN	NN	IN	PRP$	JJ	NN	IN	CD	NNS	.");
		lines.add("Doug	Hammond	,	the	federal	mediator	in	Seattle	,	where	Boeing	is	based	,	said	the	parties	will	continue	to	sit	down	daily	until	a	new	settlement	proposal	emerges	or	the	talks	break	off	again	.");
		lines.add("NNP	NNP	,	DT	JJ	NN	IN	NNP	,	WRB	VBG	VBZ	VBN	,	VBD	DT	NNS	MD	VB	TO	VB	RP	RB	IN	DT	JJ	NN	NN	VBZ	CC	DT	NNS	VBP	RP	RB	.");
		lines.add("The	union	,	though	,	has	called	the	offer	``	insulting	.	''");
		lines.add("DT	NN	,	RB	,	VBZ	VBN	DT	NN	``	JJ	.	''");

		for (int i = 0; i < lines.size() / 2; i++) {
			String[] words = lines.get(i * 2).split("\\s+");
			int[] wordIds = new int[words.length];

			for (int j = 0; j < words.length; j++) {
				// this will be off at test time, so new words wont be added to the lexicon
				if (lm.isAllowNewFeatures()) {
					lm.addFeature("w:" + words[j]);
				}
				if (lm.containFeature("w:" + words[j]))
					wordIds[j] = lm.getFeatureId("w:" + words[j]);
				else
					wordIds[j] = lm.getFeatureId("w:unknownword");

			}
			Sentence x = new Sentence(wordIds);
			String[] tags = lines.get(i * 2 + 1).split("\\s+");
			int[] tagIds = new int[words.length];


			for (int j = 0; j < tags.length; j++) {
				lm.addLabel("tag:" + tags[j]);
				tagIds[j] = lm.getLabelId("tag:" + tags[j]);
			}
			sp.addExample(x, new POSTag(tagIds));
		}
		return sp;
	}
}
