package edu.illinois.cs.cogcomp.sl.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.DenseVector;

/**
 * The class that controls all of the parameters including the hyper parameters
 * for learning, optimization control parameters, stopping conditions.
 * 
 * @author Cogcomp @ UI
 * 
 */
public class SLParameters implements Serializable {
	/**
	 * 
	 */

	private static final long serialVersionUID = 3630883016928318230L;

	public static enum LearningModelType {
		L2LossSSVM, StructuredPerceptron
	};

	public LearningModelType LEARNING_MODEL = LearningModelType.L2LossSSVM;

	public L2LossSSVMLearner.SolverType L2_LOSS_SSVM_SOLVER_TYPE = L2LossSSVMLearner.SolverType.DEMIParallelDCDSolver;

	/**
	 * Number of threads use to train the model. This parameter is useful only
	 * when applying a multi-core training algorithm.
	 */

	public int NUMBER_OF_THREADS = 16;

	/**
	 * The regularization parameter that controls how much we want to overfit
	 * the structured labeled data. If the value is higher, the training error
	 * of the structured data should be lower.
	 * <p>
	 */
	public float C_FOR_STRUCTURE = 1.0f;

	/**
	 * This option is used to speed up the training for structured SVM.
	 * <p>
	 * 
	 * When it is set to true, the algorithm will first train a model based on a
	 * small subset of the data. The size of the subset data is specified by the
	 * parameter: {@link SLParameters#TRAINMINI_SIZE}.
	 * 
	 */
	public boolean TRAINMINI = false;
	public int TRAINMINI_SIZE = 1000;

	/**
	 * This parameter controls the precision of the solution to the SSVM
	 * optimization problem. When this parameter is set to a small number (e.g.,
	 * 0.01), the algorithm takes more time to stop but achieves a more precise
	 * solution. In general, a large value is enough for obtaining a stable
	 * model.
	 * 
	 */

	public float STOP_CONDITION = 0.1f;

	/**
	 * This is an important variable. If you want to do ILP or dynamic
	 * programming and you want to check if your inference procedure is correct.
	 * JLIS will help you (learned from SVM_struct) check your procedure by
	 * compared the current solutions to the solutions of your working set.
	 * 
	 * You should turn this off only if you want to use approximated inference
	 * procedure (like beamsearch).
	 */
	public boolean CHECK_INFERENCE_OPT = true;

	/**
	 * Maximum number of iterations. This usually means maximum number of times
	 * that the learning process goes over the whole training data.
	 */

	public int MAX_NUM_ITER = 250;

	public int PROGRESS_REPORT_ITER = 10;

	// -----------------------------------------------------------------------------------
	// You should not change the parameters below if you do not know their
	// meanings.
	// -----------------------------------------------------------------------------------

	/**
	 * The stopping condition of updParaating on working set at each iteration.
	 * We use this parameter to control the precision of the quadratic
	 * programming solver in the most inner loop.
	 */

	public float INNER_STOP_CONDITION = 0.1f;

	/**
	 * Maximum number of updates on working set at each iteration (the most
	 * inner loop). We observe that usually we don't need to solve the inner
	 * problem tightly, especially in the beginning of optimization process.
	 * 
	 */
	public int MAX_ITER_INNER = 250;

	public int MAX_ITER_INNER_FINAL = 2500;

	/**
	 * Although there is no need to specify number of total features, sometimes
	 * it is better to provide this information and fix the size of the weight
	 * vector. By using {@link DenseVector#setExtendable(boolean)}, we can
	 * disallow the weight vector to grow. When TOTAL_NUMBER_FEATURE is set to
	 * -1 (default) the learning algorithm will decide the number of features on
	 * the fly. This setting is especially useful when the feature are extracted
	 * on the fly.
	 */
	public int TOTAL_NUMBER_FEATURE = -1;

	/**
	 * The flag that allows to "remove" candidate structures in the working set
	 * (if the alphas associated to them are very close to zero).
	 */
	public boolean CLEAN_CACHE = true;

	/**
	 * If the {@link SLParameters#CLEAN_CACHE} is true, we will remove unused
	 * candidate structure every {@link SLParameters#CLEAN_CACHE_ITER}
	 * iterations.
	 */
	public int CLEAN_CACHE_ITER = 5;

	public boolean DCD_CACHE_Y_HEURISTIC_FLAG = false;

	// Parameters that only used in DEMI-DCD

	public int DEMIDCD_NUMBER_OF_UPDATES_BEFORE_UPDATE_BUFFER = 100;
	public int DEMIDCD_NUMBER_OF_INF_PARSE_BEFORE_UPDATE_WV = 10;

	// Parameters that only used in Structured Perceptron
	public float LEARNING_RATE = 0.01f;

	public boolean DECAY_LEARNING_RATE = false;

	/*
	 * 32 bit mask for doing feature hashing. Turn off bits to cause collisions in feature hashing.
	 * When loading a config file, we use 30bits for feature hashing by default.
	 */
	public static int HASHING_MASK = 0xFFFFFFFF;

	// -----------------------------------------------------------------------------------
	// Read Parameters from a config file
	// -----------------------------------------------------------------------------------

	public void loadConfigFile(String fileName) throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		FileInputStream fileInputStream = new FileInputStream(fileName);
		props.load(fileInputStream);
		fileInputStream.close();
		L2_LOSS_SSVM_SOLVER_TYPE = L2LossSSVMLearner.SolverType.valueOf(props
				.getProperty("L2_LOSS_SSVM_SOLVER_TYPE",
						L2LossSSVMLearner.SolverType.DEMIParallelDCDSolver
								.name()));
		LEARNING_MODEL = LearningModelType.valueOf(props.getProperty(
				"LEARNING_MODEL", LearningModelType.L2LossSSVM.name()));
		NUMBER_OF_THREADS = Integer.parseInt(props.getProperty(
				"NUMBER_OF_THREADS", "16"));
		C_FOR_STRUCTURE = Float.parseFloat(props.getProperty("C_FOR_STRUCTURE",
				"1.0"));
		TRAINMINI = Boolean.parseBoolean(props
				.getProperty("TRAINMINI", "false"));
		TRAINMINI_SIZE = Integer.parseInt(props.getProperty("TRAINMINI_SIZE",
				"1000"));
		STOP_CONDITION = Float.parseFloat(props.getProperty("STOP_CONDITION",
				"0.1"));
		CHECK_INFERENCE_OPT = Boolean.parseBoolean(props.getProperty(
				"CHECK_INFERENCE_OPT", "true"));
		MAX_NUM_ITER = Integer.parseInt(props
				.getProperty("MAX_NUM_ITER", "250"));
		PROGRESS_REPORT_ITER = Integer.parseInt(props.getProperty(
				"PROGRESS_REPORT_ITER", "10"));

		INNER_STOP_CONDITION = Float.parseFloat(props.getProperty(
				"INNER_STOP_CONDITION", "0.1"));
		MAX_ITER_INNER = Integer.parseInt(props.getProperty("MAX_ITER_INNER",
				"250"));
		MAX_ITER_INNER_FINAL = Integer.parseInt(props.getProperty(
				"MAX_ITER_INNER_FINAL", "2500"));
		TOTAL_NUMBER_FEATURE = Integer.parseInt(props.getProperty(
				"TOTAL_NUMBER_FEATURE", "-1"));
		CLEAN_CACHE = Boolean.parseBoolean(props.getProperty("CLEAN_CACHE",
				"true"));
		CLEAN_CACHE_ITER = Integer.parseInt(props.getProperty(
				"CLEAN_CACHE_ITER", "5"));
		DCD_CACHE_Y_HEURISTIC_FLAG = Boolean.parseBoolean(props.getProperty(
				"DCD_CACHE_Y_HEURISTIC_FLAG", "false"));
		DEMIDCD_NUMBER_OF_UPDATES_BEFORE_UPDATE_BUFFER = Integer.parseInt(props
				.getProperty("DEMIDCD_NUMBER_OF_UPDATES_BEFORE_UPDATE_BUFFER",
						"100"));
		DEMIDCD_NUMBER_OF_INF_PARSE_BEFORE_UPDATE_WV = Integer.parseInt(props
				.getProperty("DEMIDCD_NUMBER_OF_INF_PARSE_BEFORE_UPDATE_WV",
						"10"));
		LEARNING_RATE = Float.parseFloat(props.getProperty("LEARNING_RATE",
				"0.01"));
		DECAY_LEARNING_RATE = Boolean.parseBoolean(props.getProperty(
				"DECAY_LEARNING_RATE", "false"));
		
		int b = Integer.parseInt(props.getProperty("NUMBER_OF_FEATURE_BITS","30"));
		HASHING_MASK = (1<<b)-1;
		fileInputStream.close();
	}
}