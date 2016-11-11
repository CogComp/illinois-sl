package edu.illinois.cs.cogcomp.sl.learner.pegasos;

import edu.illinois.cs.cogcomp.sl.core.*;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/***
 * Implementation of the Pegasos SVM Solver.
 *
 * Shalev-Shwartz, Shai, et al. "Pegasos: Primal Estimated sub-GrAdient SOlver for SVM."
 *
 * @author Bhargav Mangipudi
 *
 */
public final class Pegasos extends Learner {

    private static Logger log = LoggerFactory.getLogger(Pegasos.class);

    public static Random random = new Random();

    private int epochUpdateCount;

    private double SQRT_C_PARAMETER;

    public Pegasos(AbstractInferenceSolver infSolver, AbstractFeatureGenerator fg, SLParameters parameters) {
        super(infSolver, fg, parameters);
        this.SQRT_C_PARAMETER = Math.sqrt(parameters.C_FOR_STRUCTURE);
    }

    @Override
    public WeightVector train(SLProblem sp) throws Exception {
        WeightVector init = new WeightVector(10000);
        return train(sp, init);
    }

    @Override
    public WeightVector train(SLProblem sp, WeightVector init) throws Exception {
        log.info("Starting Pegasos learner");

        long start = System.currentTimeMillis();

        WeightVector w = init;

        int epoch = 0;
        boolean done = false;

        while (!done) {
            if (epoch % parameters.PROGRESS_REPORT_ITER == 0)
                log.info("Starting epoch {}", epoch);

            this.doOneInteration(w, sp, epoch);

            if (epoch % parameters.PROGRESS_REPORT_ITER == 0)
                log.info("End of epoch {}. {} updates made", epoch, epochUpdateCount);

            epoch++;
            done = !reachedStoppingCriterion(epoch);

            if (parameters.PROGRESS_REPORT_ITER > 0 &&
                    (epoch + 1) % parameters.PROGRESS_REPORT_ITER == 0 &&
                    this.f != null) {
                f.run(w, infSolver);
            }
        }

        long end = System.currentTimeMillis();

        log.info("Learning complete. Took {}s", "" + (end - start) * 1.0 / 1000);

        return new WeightVector(w);
    }

    /**
     * Project the weight-vector to a ball of radius square-root of C parameter.
     * @param w
     */
    protected void projectWeightVector(WeightVector w) {
        double l2Norm = w.getSquareL2Norm();

        if (l2Norm != 0) {
            double projectionScale = SQRT_C_PARAMETER / Math.sqrt(l2Norm);

            if (projectionScale < 1) {
                w.scale(projectionScale);
            }
        }
    }

    /**
     * Checks if stopping criterion has been met. We will stop if either no mistakes were made
     * during this iteration, or the maximum number of passes over the training data has been made.
     * @param epoch
     * @return
     */
    protected boolean reachedStoppingCriterion(int epoch) {
        if (epochUpdateCount == 0) {
            log.info("No errors made. Stopping outer loop because learning is complete!");
            return false;
        }
        return epoch < parameters.MAX_NUM_ITER;
    }

    /**
     * Perform a single iteration of the update step on all training instances.
     *
     * @param w
     * @param problem
     * @param epoch
     * @throws Exception
     */
    protected void doOneInteration(WeightVector w, SLProblem problem, int epoch) throws Exception
    {
        int numExamples = problem.size();

        epochUpdateCount = 0;
        problem.shuffle(random); // shuffle your training data after every iteration

        double weightVectorScale = 1 - (1.0 / (epoch + 1));
        w.scale(weightVectorScale);

        for (int exampleId = 0; exampleId < numExamples; exampleId++) {
            IInstance example = problem.instanceList.get(exampleId);	// the input "x"
            IStructure gold = problem.goldStructureList.get(exampleId);	// the gold output structure "y"

            IStructure prediction;
            boolean shouldUpdate;

            prediction = this.infSolver.getLossAugmentedBestStructure(w, example, gold);	// the predicted structure
            assert prediction != null;

            //NOTE: this is loss augmented, so that you can train loss augmented variants too.
            // if you want the usual behavior, where inference returns best structure,
            // just write your loss augmented inference to ignore the loss.
            // this was done to support general behavior.
            // we will update if the loss is non-zero for this example
            shouldUpdate = this.infSolver.getLoss(example, gold, prediction) > 0;

            if (shouldUpdate) {
                update(example, gold, prediction, w, epoch, numExamples);
                epochUpdateCount++;
            }
        }

        // Do the weight-vector projection step if required.
        if (parameters.PEGASOS_PERFORM_WEIGHT_VECTOR_PROJECTION) {
            projectWeightVector(w);
        }
    }

    /**
     * Performs the update of the weight vector for a single data instance.
     *
     * @param gold
     * @param prediction
     * @param w
     * @param epoch
     */
    protected void update(IInstance ins, IStructure gold, IStructure prediction,
                          WeightVector w, int epoch, int numExamples) {
        IFeatureVector goldFeatures = featureGenerator.getFeatureVector(ins, gold);
        IFeatureVector predictedFeatures = featureGenerator.getFeatureVector(ins, prediction);

        IFeatureVector update = goldFeatures.difference(predictedFeatures);

        double learningRate = getLearningRate(epoch) / numExamples;

        w.addSparseFeatureVector(update, learningRate);
    }

    /**
     * Get the effective learning rate for each training round.
     *
     * @param epoch
     * @return
     */
    protected double getLearningRate(int epoch) {
        return this.parameters.C_FOR_STRUCTURE / (epoch + 1);
    }
}
