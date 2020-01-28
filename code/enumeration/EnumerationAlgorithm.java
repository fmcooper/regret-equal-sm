package code.enumeration;
import code.shared.*;
import java.util.*;

public class EnumerationAlgorithm {
	/** <p> Stable matchings. </p>*/
	private StableMatchings stableMatchings;
	/** <p> Model for this instance. </p>*/
	private Model model;
	/** <p> List of rotations for this instance. </p>*/
	private Rotations rotations;
	/** <p> Digraph for this instance. </p>*/
	private Digraph digraph;


	/**
	* <p> Constructor. </p>
	* @param model
	*/
	public EnumerationAlgorithm(Model model) {
		this.model = model;
		rotations = model.getRotations();
		digraph = model.getDigraph();
		stableMatchings = new StableMatchings(model);

		// run the algorithm 
		runAlg(model);
	}


	/**
	 * <p> Find and save all stable matchings. </p>
	 */
	public void runAlg(Model model) {
		// run Gale Shapley algorithm to find man and woman optimal stable
		// matchings
		GaleShapleyExtended alg1 = new GaleShapleyExtended(model);
		// we use the reduced preference lists again for another GS run.
		model.preferenceListConsistency();
		int[] manOpt = model.getProposerAssignments().clone();
		model.resetAssignedAndCurrentIndex();
		model.swapProposer();

		GaleShapleyExtended alg2 = new GaleShapleyExtended(model);
		int[] womanOpt = model.getProposerAssignmentsSwitched().clone();
		// switch back to the mens point of view and set the current matching to
		// the man optimal matching
		model.swapProposer();
		model.resetAssignedAndCurrentIndex();
		model.setProposerAssignments(manOpt);
		model.setAssignmentInfo();
		model.preferenceListConsistency();
		model.saveGSlists(); // required for digraph creation

		// minimal differences algorithm and digraph
		new MinimalDifferences(model, womanOpt);
		model.reinstateGSlists();
		new DigraphCreator(model);
		model.refreshAgentLists();

		ArrayList<ArrayList<Integer>> simpleDigraph = digraph.getSimpleDigraph();

		// find number of incoming edges to each rotation in the digraph
		int[] predecessorCount = new int[rotations.getNum()];
		for (int i = 0; i < simpleDigraph.size(); i++) {
			ArrayList<Integer> row = simpleDigraph.get(i);
			for (int j = 0; j < row.size(); j++) {
				predecessorCount[row.get(j)]++;
			}
		}

		// save list of rotations with no incoming edges
		ArrayList<Integer> rotationsNoPredecessor = new ArrayList<Integer>();
		for (int i = 0; i < predecessorCount.length; i++) {
			if (predecessorCount[i] == 0) {
				rotationsNoPredecessor.add(i);
			}
		}

		// no need to relabel rotations as they are labelled in topological
		// order from the minimal differences algorithm save M_0 and start
		// recursion over digraph from each starter node (with no incoming
		// edges)
		int numProposers = model.getNumProposers();
		int[] stableMatching = new int[numProposers];
		for (int i = 0; i < numProposers; i++) {
			stableMatching[i] = model.getProposerAssignments()[i];
		}
		stableMatchings.add(model.createMatching(stableMatching));

		for (int rotation : rotationsNoPredecessor) {
			findStableRecursive(
				stableMatching, 
				rotation, 
				rotationsNoPredecessor, 
				predecessorCount);
		}
	}


	/**
	 * <p> Recursive: saves a stable matching. </p>
	 * @param stableMatching 			the current stable matching
	 * @param currentRotation 			the rotation to eliminate
	 * @param rotationsNoPredecessor 	rotations to eliminate
	 * @param predecessorCount			the number of predecessors for rotation
	 */
	private void findStableRecursive(
		int[] stableMatching, 
		int currentRotation, 
		ArrayList<Integer> rotationsNoPredecessor, 
		int[] predecessorCount) {

		ArrayList<ArrayList<Integer>> simpleDigraph = digraph.getSimpleDigraph();

		if (rotationsNoPredecessor.size() != 0) {
			
			int[] copySM = deepCopy(stableMatching);
			copySM = rotations.get(currentRotation).eliminateRotation(copySM);
			stableMatchings.add(model.createMatching(copySM));

			ArrayList<Integer> copyRWP = deepCopy(rotationsNoPredecessor);

			copyRWP.remove(Integer.valueOf(currentRotation));

			// update the predecessor counts - if any are 0 then add rotation to
			// copyRWP
			int[] copyPC = deepCopy(predecessorCount);

			ArrayList<Integer> sdChildren = simpleDigraph.get(currentRotation);
			for (int i = 0; i < sdChildren.size(); i++) {
				copyPC[sdChildren.get(i)]--;
				if (copyPC[sdChildren.get(i)] == 0) {
					copyRWP.add(sdChildren.get(i));
				}
			}
			
			// recurse call
			for (int rwp : copyRWP) {
				if (rwp > currentRotation) {
					findStableRecursive(copySM, rwp, copyRWP, copyPC);
				}
			}
		}
	}


	/**
	 * <p> Performs a deep copy of an int array. </p>
	 * @param array to copy
	 * @return copied array
	 */
	private int[] deepCopy(int[] array) {
		int[] copy = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			copy[i] = array[i];
		}
		return copy;
	}


	/**
	 * <p> Performs a deep copy of an int ArrayList. </p>
	 * @param array to copy
	 * @return copied array
	 */
	private ArrayList<Integer> deepCopy(ArrayList<Integer> array) {
		ArrayList<Integer> copy = new ArrayList<Integer>();
		for (int i = 0; i < array.size(); i++) {
			copy.add(array.get(i));
		}
		return copy;
	}


	/**
	 * <p> Returns a string list of all stable matchings of the instance. </p>
	 * @return rotations
	 */
	public String getResults() {
		stableMatchings.findStableOptimals();

		String s = model.timeAndDate + "\n";
		s += "// All stable matching results\n";
		s += "numRotations: " + rotations.getNum() + "\n";
		s += "numStableMatchings: " + stableMatchings.getNum() + "\n\n\n";

		s += stableMatchings.getResults();
		s += rotations.getResults();
		s += digraph.getResults();

		return s;
	}
}
