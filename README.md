Illinois Structured Learning Package v1.0.0
===========================================

Illinois Structured Learning Package (Illinois-SL) is a general
purpose JAVA library for performing structured learning. It houses
learning algorithms like averaged Structured Perceptron and Structured
SVM with L2-Loss, and provides a minimal interface for your structured
learning needs. The training algorithm employed for training SSVM is
dual coordinate descent(DCD), which has been proven to have very good
convergence properties. Illinois-SL comes with an efficient
implementation of DCD with support for multi-threading. Illinois-SL
provides a simple and neat framework for developing applications using
structured prediction models.

Maven Coordinates
-----------------
To use Illinois-SL in your project add the following to your pom,

```
  <dependencies>
  ...  
    <dependency>
      <groupId>edu.illinois.cs.cogcomp</groupId>
      <artifactId>illinois-sl-core</artifactId>
      <version>1.0.0</version>
    </dependency>
  ...
  </dependencies>

<repositories>
  ...
    <repository>
      <id>CogcompSoftware</id>
      <name>CogcompSoftware</name>
      <url>http://cogcomp.cs.illinois.edu/m2repo/</url>
    </repository>
  ...  
  </repositories>

```
Example Usage
-------------
We provide detailed examples in an accompanying package at [illinois-sl-examples](https://github.com/IllinoisCogComp/illinois-sl-examples).

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
Linux OS without any issues.

We assume that the package is installed on a machine with sufficient
memory. The actual requirement of the memory depends on the task and size of the learning problem.

NOTE: When running your project, if working with a large dataset, you
may need to invoke your project using the -Xmx1G and
-XX:MaxPermSize=1G JVM command line parameters.


Additional Documentation/Citation
---------------------

Additional documentation is available in the JavaDoc located in doc/index.html

Citing
------
Please cite the following papers when using this library

K.-W. Chang, S. Upadhyay, M.-W. Chang, V. Srikumar, D. Roth.
IllinoisSL: A JAVA Library for Structured Prediction.
Arxiv, 2015.

M.-W. Chang, V. Srikumar, D. Goldwasser and Dan Roth. 
Structured output learning with indirect supervision. 
ICML, 2010.

K.-W. Chang, V. Srikumar, D. Roth. 
Multi-core Structural SVM Training.
ECML, 2013.

Contribute to the package
-------------------------

If you encounter any issue when using the package, please file a bug report as a [GitHub issue](https://github.com/IllinoisCogComp/illinois-sl/issues) and provide information about how to reproduce the error. 

You are also welcomed to contribute to the package. To do so, please fork git repo https://github.com/IllinoisCogComp/illinois-sl/ and submit a pull request.

Please find the detailed instructions about how to contribute to a GitHub project at https://guides.github.com/activities/contributing-to-open-source/



Contact Information
------------

Please open a new issue with a minimal working example, in case you
run into problems when using this package, and we will assist you. You
can also email your questions to illinois-ml-nlp-users@cs.uiuc.edu.

(C) 2015 Kai-Wei Chang, Shyam Upadhyay, Ming-Wei Chang, Vivek Srikumar and Dan Roth, 
Cognitive Computation Group, University of Illinois at Urbana-Champaign.

