Illinois Structured Learning Package v1.0.0
===========================================

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
latent structured models or indirect supervision, please check JLIS at [cogcomp
website](http://cogcomp.cs.illinois.edu/page/software_view/JLIS).

License
--------
The Illinois Structured Learning Package is available under a Research
and Academic use license. For more details, view the license file `LICENSE`.


System requirements
----------------------------

The Illinois Structured Learning Package was developed on and for
GNU/Linux, specifically CENTOS (2.6.18-238.12.1.el5) and Scientific
Linux (2.6.32-279.5.2.el6.x86_64). There are no guarantees for running
it under any other operating system, but we hope it should run on a
Linux OS without any hiccups. We assume that the package is installed
on a machine with sufficient memory. The actual requirement of the
memory depends on the task.

//Download contents
//----------------------------
// We recommend to download the stable version at
// http://cogcomp.cs.illinois.edu/page/software_view/illinois-sl

// The download contains the following files:
// - dist/illinois-sl-1.0.0-jar-with-dependencies.jar :  the jar file with dependencies.
// - dist/illinois-sl-1.0.0-sources.jar : the jar file
// - dist/illinois-sl-1.0.0.jar : the archive of source files
// - README.standalone : this file
// - config/ : a set of sample configuration files
// - scripts/ : scripts for testing the package
// - data/ : sample data sets
// - pom.xml : Maven pom file
// - doc/webpage/tutorial.html : A offline copy of the webpage http://cogcomp.cs.illinois.edu/software/illinois-sl. 
// - doc/apidocs : java API

// The details are shown later.

// Using the jar
// ----------------------------


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
questions.

(C) 2015 Kai-Wei Chang, Shyam Upadhyay, Ming-Wei Chang, Vivek Srikumar and Dan Roth, 
Cognitive Computation Group, University of Illinois at Urbana-Champaign.

