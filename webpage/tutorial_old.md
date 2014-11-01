<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. Installation</a></li>
<li><a href="#sec-2">2. Getting Started</a></li>
<li><a href="#sec-3">3. Example - POS Tagging</a></li>
</ul>
</div>
</div>

{% raw %}
\(

\newcommand{\opt}[1]{{#1}^{*}} 
\newcommand{\pred}[1]{\hat{#1}} 
\newcommand{\dnorm}[1]{{#1}^{*}} 
\newcommand{\dotprod}[2]{ {#1}^T{#2}} 
\renewcommand{\Pr}{\mathbb{P}} 
\renewcommand{\vec}[1]{\mathbf{#1}} 


\DeclareMathOperator*{\argmin}{\mathbf{arg\,min}}
\DeclareMathOperator*{\argmax}{\mathbf{arg\,max}}
\DeclareMathOperator*{\sup}{sup}
\DeclareMathOperator{\F}{\mathscr{F}} 
\DeclareMathOperator{\H}{\mathscr{H}} 
\DeclareMathOperator{\R}{\mathbb{R}} 
\DeclareMathOperator{\E}{\mathbb{E}} 
\DeclareMathOperator{\Or}{\mathcal{O}}
\DeclareMathOperator{\Tr}{\textbf{Tr}} 
\DeclareMathOperator{\grad}{\nabla} 
\DeclareMathOperator{\LLH}{\mathcal{L}} 
\DeclareMathOperator{\Lag}{\mathcal{L}} 
\DeclareMathOperator{\X}{\mathcal{X}} 
\DeclareMathOperator{\Y}{\mathcal{Y}} 
\DeclareMathOperator{\w}{\mathbf{w}} 
\DeclareMathOperator{\bF}{\mathbf{F}} 
\DeclareMathOperator{\y}{\mathbf{y}} 
\DeclareMathOperator{\x}{\mathbf{x}} 
\DeclareMathOperator{\implies}{\rightarrow}

\)
{% endraw %}

# Installation

# Getting Started

# Example - POS Tagging

    para.TOTAL_NUMBER_FEATURE = model.lm.getNumOfFeature() * model.lm.getNumOfLabels() + model.lm.getNumOfLabels()
    + model.lm.getNumOfLabels() * model.lm.getNumOfLabels();
      // numLabels*numLabels for transition features
      // numWordsInVocab*numLabels for emission features
      // numLabels for prior on labels
      Learner learner = LearnerFactory.getLearner(model.infSolver, fg,
    para);
    model.wv = learner.train(sp);
    
    // save the model
    model.saveModel(modelPath);