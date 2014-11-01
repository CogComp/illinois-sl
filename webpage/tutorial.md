---
layout: post
title:  "Tutorial SL"
date:   2014-08-23 11:53:57
categories: jekyll update
excerpt:
---

# Getting Started
This is a tutorial for the Illinois-SL package. We will walk you through installation, some simple examples and other relevant details.

In case of problems, you can contact the author at 

# Example - POS Tagging
We will start with a very simple example - part-of-speech tagging. 

{% highlight java %}
public static void trainSequenceModel(String trainingDataPath,
String configFilePath, String modelPath) throws Exception {
SLModel model = new SLModel();
model.lm = new Lexiconer();

SLProblem sp = readStructuredData(trainingDataPath, model.lm);

// Disallow the creation of new features
model.lm.setAllowNewFeatures(false);

// initialize the inference solver
model.infSolver = new ViterbiInferenceSolver(model.lm);

SLParameters para = new SLParameters();
POSManager fg = new POSManager(model.lm);
para.loadConfigFile(configFilePath);
para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature()
* model.lm.getNumOfLabels() + model.lm.getNumOfLabels()
+ model.lm.getNumOfLabels() * model.lm.getNumOfLabels();
// numLabels*numLabels for transition features
// numWordsInVocab*numLabels for emission features
// numLabels for prior on labels
Learner learner = LearnerFactory.getLearner(model.infSolver, fg,
para);
model.wv = learner.train(sp);

// save the model
model.saveModel(modelPath);
}
{% endhighlight %}
Let's break this down one class at a time.
`SLModel` contains all relevant details about your model.
1. The weight vector for your model.
2. The Lexicon for your features - the lexicon manages the mapping from your features to ids (more on it later)
3. 