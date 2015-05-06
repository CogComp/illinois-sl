Illinois Structured Learning Package v0.2.2
===========================================

(C) 2015 Kai-Wei Chang, Upadhyay Shyam, Ming-Wei Chang, Vivek Srikumar and Dan Roth, 
Cognitive Computation Group, University of Illinois at Urbana-Champaign.

Table of Contents
=================
1. README  
   - Purpose 
   - License 
2. System requirements 
3. Download contents 
4. Using the jar 
5. Using the Source Files 
6. Additional Documentation/ Citation 
7. Contact Information 


Purpose
-------

Illinois Structured Learning Package (Illinois-SL) is a general purpose JAVA 
library for performing structured learning. It houses learning algorithms like
averaged Structured Perceptron and Structured SVM with L2-Loss, and provides 
a minimal interface for your structured learning needs. The training algorithm 
employed for training SSVM is dual coordinate descent(DCD), which has been 
proven to have very good convergence properties. Illinois-SL comes with an 
efficient implementation of DCD with support for multi-threading. Illinois-SL
provides a simple and neat framework for developing applications using 
structured prediction models. For advanced applications which makes use of
latent structured models or indirect supervision, please check JLIS at cogcomp
website.

License
--------
The Illinois Structured Learning Package is available under a Research
and Academic use license. For more details, visit the Curator website 
and click the download link.


System requirements
----------------------------

The Illinois Structured Learning Package was developed on and for GNU/Linux,
specifically CENTOS (2.6.18-238.12.1.el5) and Scientific Linux 
(2.6.32-279.5.2.el6.x86_64). There are no guarantees for running it under any
other operating system. The instructions below assume a Linux OS. We assume
that the package is installed on a machine with sufficient memory. The actual
requirement of the memory depends on the task. 

Download contents
----------------------------

The download contains the following files:
- dist/illinois-sl-0.2.2-jar-with-dependencies.jar :  the jar file with dependencies.
- dist/illinois-sl-0.2.2-sources.jar : the jar file
- dist/illinois-sl-0.2.2.jar : the archive of source files
- README.standalone : this file
- config/ : a set of sample configuration files
- scripts/ : scripts for testing the package
- data/ : sample data sets
- pom.xml : Maven pom file
- doc/webpage/tutorial.html : A offline copy of the webpage http://cogcomp.cs.illinois.edu/software/illinois-sl. 
- doc/apidocs : java API

The details are shown later.

Using the jar
----------------------------


Illinois-SL supports three types of applications -- cost-sensitive multi-class
classification, reranking, and sequential tagging. Besides, these three 
applications, Illinois-SL also contains a simple part-of-speech tagger 
implementation, which takes less than 400 lines of code under 
'edu.illinois.cs.cogcomp.sl.applications.tutorial'. These applications are 
served as examples for using the Illinois-SL library. We provide the following 
scripts for these applications.

- scripts/run_tutorial.sh
- scripts/run_sequence.sh
- scripts/run_multiclass.sh
- scripts/run_reranking.sh

The sample data sets can be found at 'data' directory. 
We describe the usage of each script below. 

scripts/run_tutorial.sh
____

Use the following comment to train a part of speech tagger model 'posModel' on 
'data/tutorial/big.train' with DEMIParallelDCD solver:

```
> ./scripts/run_tutorial.sh trainPOSModel data/tutorial/big.train  config/DEMIParallelDCD.config posModel
```

One can use other structured learning solvers by simply specifying corresponding config file in 'config/'

Currently, we provide the following options:
- DCD.config : a dual coordinate descent approach (single thread) for Structured SVM.
- DEMIParallelDCD.config: DEMIDCD for Structured SVM. (see the ECML paper below)
- ParallelDCD.config: a master-slave dual coordinate descent method for Structured SVM.
- StrcturedPerceptron.config: a Structured Perceptron implementation.

The following script evaluates the performance of 'posModel' on 
'data/tutorial/big.test':

```
>./scripts/run_tutorial.sh testPOSModel posModel data/tutorial/big.test
```

scripts/run_sequence.sh
____


Use the following comment to train a sequential model 'seqModel' on 
'data/tutorial/wsj.sub.train' with DEMIParallelDCD solver:

```
> ./scripts/run_sequence.sh trainSequenceModel data/sequence/wsj.sub.train config/DEMIParallelDCD.config seqModel
```
Again, one can use another structured learning approach to train the model 
by specifying a different config file. Each line follows the following format 

```
[Tag] qid:[example_id] [feature1_index]:[feature1_value] [feature2_index]:[feature2_value] ...
```

`[Tag]` is a positive integer representing the label. All lines with the same `[example_id]` belong to the same structured example. 

The following comment tests 'seqModel" on 'data/sequence/wsj.sub.test' data set.
```
> ./scripts/run_sequence.sh testSequenceModel seqModel data/sequence/wsj.sub.test
```

4.1.2 __ scripts/run_multiclass.sh __

Use the following comment to train a multiclass model 'multiModel' on 
'data/multiclass/heart_scale.train' data with a cost matrix specified in 
'data/multiclass/heart_scale.cost' using DEMIParallelDCD solver:

```
> ./scripts/run_multiclass.sh trainMultiClassModel data/multiclass/heart_scale.train data/multiclass/heart_scale.cost config/DEMIParallelDCD.config multiModel
```
Each line of the input data represents one instance, and it follows the following format:

```
[Tag] [feature1_index]:[feature1_value] [feature2_index]:[feature2_value] ...
```
where `[Tag]` is an integer representing label.

The cost matrix file specifies the loss of  wrong predictions.
Each line follows the following format:

```
[gold_label] [predicted_label] [cost]
```
The cost needs to be positive, and it is 0 when `[gold_label]=[predicted_label]`.

To test the performance of 'multiModel' on 'data/multiclass/heart_scale.test', use

```
> ./scripts/run_multiclass.sh testMultiClassModel multiModel data/multiclass/heart_scale.test
```

scripts/run_reranking.sh
____


Use the following comment to train a re-ranker on 'data/reranking/rank.train' with  Strctured Perceptron

```
> ./scripts/run_reranking.sh trainRankingModel data/reranking/rerank.train config/StructuredPerceptron.config  rankModel
```
Use the following comment to test the re-ranker model on 'data/reranking/rerank.test'
```
> ./scripts/run_reranking.sh testRankingModel rankModel data/reranking/rerank.test
```
These scripts should be runnable on installation; However, it is possible, 
depending on your system configuration, that you will first need to modify
the permissions on the scripts to allow you to execute them:

```
> chmod 744 scripts/*sh
```
Using the Source Files
-----------------------

COMMAND LINE USAGE
The command lines given at the top of this README.


IMPORTING IN YOUR PROJECT
Please refer to 'webpage/tutorial.html' for detailed usage of Illinois-SL
library. 

NOTE:
When running your project, if working with a large dataset,
you may need to invoke your project using the -Xmx1G and -XX:MaxPermSize=1G
JVM command line parameters. 


Additional Documentation/Citation
---------------------

Additional documentation is available in the JavaDoc located in doc/index.html


Citing
------
Please cite the following papers when using this library

M.-W. Chang, V. Srikumar, D. Goldwasser and Dan Roth. 
Structured output learning with indirect supervision. 
ICML, 2010.

K.-W. Chang, V. Srikumar, D. Roth. 
Multi-core Structural SVM Training.
ECML, 2013.

Contact Information
------------

Please send a message to illinois-ml-nlp-users@cs.uiuc.edu for any
questions about installing or using the Curator.
