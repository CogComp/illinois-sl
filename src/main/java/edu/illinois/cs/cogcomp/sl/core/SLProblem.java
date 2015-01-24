package edu.illinois.cs.cogcomp.sl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

/**
 * This class represents a structured problem. A structured problem is a
 * collection of input-structure (X_i,Y_i) pairs.
 * 
 * @author Cogcomp @ UI
 * 
 */
public class SLProblem implements Iterable<Pair<IInstance, IStructure>>{
	/**
	 * The output list that contains the corresponding gold output structures
	 * (y) for the input examples in the input_list (x)
	 */
	public List<IStructure> goldStructureList;
	/**
	 * The input list contains the input examples (x)
	 */
	public List<IInstance> instanceList;
	/**
	 * The weight list. Our implementation allows using different values of
	 * C for different examples! If this list is null. It means that every
	 * example should be treated equally. If it is not null, it should have the
	 * same number of elements as that of input/output_list.
	 * <p>
	 * 
	 * More precisely, this weight list changes the formation to the following
	 * one:
	 * <p>
	 * 
	 * \min 1/2 w*w + \sum_i C * weight_i * Loss(w,x,y)
	 * <p>
	 * 
	 * Therefore, if you put more weight on an example, it means that this
	 * example is more important and the learning algorithm will try harder to
	 * fit this example.
	 */
	public List<Float> instanceWeightList = null;
	

	public SLProblem() {
		goldStructureList = new ArrayList<IStructure>();
		instanceList = new ArrayList<IInstance>();
	}

	/**
	 * @return the number of instances of this structured problem.
	 */
	public int size() {
		assert goldStructureList.size() == instanceList.size();
		assert instanceWeightList == null
				|| (goldStructureList.size() == instanceWeightList.size());

		return goldStructureList.size();
	}

	/**
	 * A helper function that shuffles the order of the examples in this
	 * problem.
	 * 
	 * @param rnd
	 *            A random number generator ---- if you use the same random
	 *            generator (with the same seed), you will get the same
	 *            ordering.
	 */
	public void shuffle(Random rnd) {
		int numberOfInstance = size();
		for (int i = 0; i < numberOfInstance; i++) {
			int j = i + rnd.nextInt(numberOfInstance - i);

			IInstance tmpIns = instanceList.get(i);
			instanceList.set(i, instanceList.get(j));
			instanceList.set(j, tmpIns);

			IStructure tmpStructure = goldStructureList.get(i);
			goldStructureList.set(i, goldStructureList.get(j));
			goldStructureList.set(j, tmpStructure);

			if (instanceWeightList != null) {
				Float tmp_weight = instanceWeightList.get(i);
				instanceWeightList.set(i, instanceWeightList.get(j));
				instanceWeightList.set(j, tmp_weight);
			}
		}
	}

	public List<SLProblem> splitData(int splits){
		this.shuffle(new Random(0));
		List<SLProblem> subProb = new ArrayList<SLProblem>();
		for(int i=0; i<splits; i++){
			subProb.add(new SLProblem());
		}
		int idx = 0;
		for(Pair<IInstance, IStructure> instanceStructurePair : this){
			subProb.get(idx % splits).addExample(instanceStructurePair.getFirst(),
					instanceStructurePair.getSecond());
			idx++;
		}
		return subProb;
	}
	
	/**
	 * A helper function that helps you to split the training data
	 * 
	 * @param numberOfTrainInstance
	 *            The number of the training examples.
	 * @return A {@link Pair} of the Binary Problem. The first one represents
	 *         the training set (which has n_train examples). The second one
	 *         represents the testing set
	 */
	public Pair<SLProblem, SLProblem> splitTrainTest(int numberOfTrainInstance) {
		SLProblem train = new SLProblem();
		SLProblem test = new SLProblem();
		if (instanceWeightList != null) {
			train.instanceWeightList = new ArrayList<Float>();
			test.instanceWeightList = new ArrayList<Float>();
		}

		for (int i = 0; i < size(); i++) {
			if (i < numberOfTrainInstance) {
				train.instanceList.add(instanceList.get(i));
				train.goldStructureList.add(goldStructureList.get(i));
				if (instanceWeightList != null) {
					train.instanceWeightList.add(instanceWeightList.get(i));
				}
			} else {
				test.instanceList.add(instanceList.get(i));
				test.goldStructureList.add(goldStructureList.get(i));
				if (instanceWeightList != null) {
					test.instanceWeightList.add(instanceWeightList.get(i));
				}
			}
		}
		return new Pair<SLProblem, SLProblem>(train, test);
	}

	/**
	 * A helper function that helps you to perform cross validation. It splits
	 * the data in to n_fold {@link Pair}s, and each pair contains the (Training
	 * and Testing) split.
	 * 
	 * @param numOffolds
	 *            The number of fold you wish to performance cross validations.
	 *            It equals to the length of the returned list.
	 * @param rnd
	 *            A random number generator. If you use the same seed, you will
	 *            generate the same split. It makes the comparisons between
	 *            different algorithms easier.
	 * @return
	 */
	public List<Pair<SLProblem, SLProblem>> splitDataToNFolds(
			int numOffolds, Random rnd) {

		List<Integer> indexList = new ArrayList<Integer>();
		int bp_size = size();
		for (int i = 0; i < bp_size; i++)
			indexList.add(i);

		Collections.shuffle(indexList, rnd);
		List<Pair<SLProblem, SLProblem>> res = new ArrayList<Pair<SLProblem, SLProblem>>();

		for (int f = 0; f < numOffolds; f++) {
			SLProblem cvTrain = new SLProblem();
			SLProblem cvTest = new SLProblem();

			if (instanceWeightList != null) {
				cvTrain.instanceWeightList = new ArrayList<Float>();
				cvTest.instanceWeightList = new ArrayList<Float>();
			}

			for (int i = 0; i < bp_size; i++) {
				int real_idx = indexList.get(i);
				if ((i) % numOffolds == f) {
					// test
					cvTest.instanceList.add(instanceList.get(real_idx));
					cvTest.goldStructureList.add(goldStructureList.get(real_idx));
					if (instanceWeightList != null) {
						cvTest.instanceWeightList.add(instanceWeightList.get(real_idx));
					}
				} else {
					// train
					cvTrain.instanceList.add(instanceList.get(real_idx));
					cvTrain.goldStructureList.add(goldStructureList.get(real_idx));
					if (instanceWeightList != null) {
						cvTrain.instanceWeightList.add(instanceWeightList.get(real_idx));
					}
				}
			}

			res.add(new Pair<SLProblem, SLProblem>(cvTrain,
					cvTest));
		}
		return res;
	}
	
	/**
	 * Add instance and corresponding gold structure to a StrctureProblem instance.
	 * @param instance
	 * @param goldStructure
	 */
	public void addExample(IInstance instance, IStructure goldStructure) {
		this.instanceList.add(instance);
		this.goldStructureList.add(goldStructure);
	}
	
	public static class StructuredProblemIterator implements Iterator<Pair<IInstance, IStructure>>{
		int index = 0;
		List<IInstance> instanceList;
		List<IStructure> goldStructureList;
		public StructuredProblemIterator(List<IInstance> instanceList, List<IStructure> goldStructureList) {
			this.instanceList = instanceList;
			this.goldStructureList = goldStructureList;
			index = 0;
		}
		@Override
		public boolean hasNext() {
			if(index < goldStructureList.size()) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Pair<IInstance, IStructure> next() {
			Pair<IInstance, IStructure> ex = new Pair<IInstance, IStructure>(
					this.instanceList.get(index), this.goldStructureList.get(index));
			index++;
			return ex;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			// TODO Auto-generated method stub
		}
		
	}
	
	@Override
	public Iterator<Pair<IInstance, IStructure>> iterator() {
		// TODO Auto-generated method stub
		return new StructuredProblemIterator(instanceList, goldStructureList);
	}
}
