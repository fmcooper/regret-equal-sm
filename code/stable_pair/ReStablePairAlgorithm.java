package code.stable_pair;
import code.shared.*;
import java.util.*;
import java.math.*;

public class ReStablePairAlgorithm {
	/** <p> Regret-equal stable matching. </p> */
	private int[] regretEqualMatching;

	/**
	* <p> Constructor. </p>
	* @param mens model
	* @param womens model
	*/
	public ReStablePairAlgorithm(Model model) {
		// run the algorithm 
		regretEqualMatching = runAlg(model);
	}

	/**
	* <p> Regret-equal Algorithm. </p>
	* @param the current model (either looking from men or women's side)
	* @param the associate man or woman optimal matching
	* @return matching 		regret equal matching for either men or women
	*/
	private int[] runAlg(Model model){
		// man-optimal matching
		GaleShapleyExtended alg1 = new GaleShapleyExtended(model);
		model.preferenceListConsistency();
		int[] manOptimal = model.getProposerAssignments().clone();
		model.resetAssignedAndCurrentIndex();
		model.swapProposer();

		// woman-optimal matching
		GaleShapleyExtended alg2 = new GaleShapleyExtended(model);
		int[] womanOptimal = model.getProposerAssignmentsSwitched().clone();
		// switch back to the mens point of view and set the current matching to
		// the man optimal matching
		model.swapProposer();
		model.resetAssignedAndCurrentIndex();
		model.setProposerAssignments(manOptimal.clone());
		model.setAssignmentInfo();
		model.preferenceListConsistency();
		model.saveGSlists(); // required for digraph creation

		// minimal differences algorithm and digraph
		new MinimalDifferences(model, womanOptimal.clone());
		model.reinstateGSlists();
		new DigraphCreator(model);
		model.refreshAgentLists();

		// create the tao and phi arrays
		int[][] taos = createTaoArray(model);
		int[][] phis = createPhiArray(model);

		// create the transitive closure of the rotation digraph
		int[][] rotationTC = createTransitiveClosure(model);

		// create list of possible degree pairs
		int manMin = model.calcDegree(manOptimal, OptsProfile.proposerView);
		int manMax = model.calcDegree(womanOptimal, OptsProfile.proposerView);
		int womanMin = model.calcDegree(womanOptimal, OptsProfile.receiverView);
		int womanMax = model.calcDegree(manOptimal, OptsProfile.receiverView);
		LinkedList<Integer[]> possibleDegreePairs = getPossiblePairs(
			manMin,
			manMax,
			womanMin,
			womanMax);

		// for each pair of ranks
		int bestDifference = model.getNumProposers();
		Rotations rotations = model.getRotations();

		for (Integer[] rankBounds : possibleDegreePairs) {
			int a = rankBounds[0];
			int b = rankBounds[1];

			// truncate at these ranks
			model.resetAssignedAndCurrentIndex();
			model.truncate(rankBounds);

			// find the new man optimal matching in our truncated instance, if
			// any rotation 
			GaleShapleyExtended gse = new GaleShapleyExtended(model);
			int[] manOptTruncated = model.getProposerAssignments();
			
			// find the number of unassigned agents in the matching
			boolean containsUnassigned = false;
			for (int i : manOptTruncated) {
				if (i == -1) {
					containsUnassigned = true;
				}
			}

			// if there is an unassigned agent then there is no stable matching
			// of size n, so finish this iteration
			if (!containsUnassigned) {
				model.resetAssignedAndCurrentIndex();

				// find women optimal of truncated instance
				model.swapProposer();
				gse = new GaleShapleyExtended(model);
				int[] womanOptTruncated = model.getProposerAssignmentsSwitched();
				model.swapProposer();

				// reinstate preference lists
				model.refreshAgentLists();

				// list the eligible rotations in our truncated instance
				boolean[] eligibleRotations = new boolean[rotations.getNum()];
				for (int ri = 0; ri < rotations.getNum(); ri++) {
					boolean eligible = checkRotationEligibility(
						model, 
						rotations.get(ri), 
						manOptTruncated, 
						womanOptTruncated);

					eligibleRotations[ri] = eligible;
				}

				// calculate the set of stable pairs
				// only save a stable pair if rank(m1,w1)=a or rank(w1,m1)=b
				ArrayList<Person[]> stablePairsA = new ArrayList<Person[]>();
				ArrayList<Person[]> stablePairsB = new ArrayList<Person[]>();

				// get the truncated woman optimal matching pairs
				for (int i = 0; i < womanOptTruncated.length; i++) {
					Person[] stablePair = new Person[2];
					stablePair[0] = model.getProposer(i);
					stablePair[1] = model.getReceiver(womanOptTruncated[i]);

					if (stablePair[0].getRankOrig(stablePair[1]) == a) {
						stablePairsA.add(stablePair);
					}
					if (stablePair[1].getRankOrig(stablePair[0]) == b) {
						stablePairsB.add(stablePair);
					}
				}

				// add them to the pairs in all the rotations that qualify
				for (int eIndex = 0; eIndex < eligibleRotations.length; eIndex++) {
					if (eligibleRotations[eIndex]) {

						for (Person[] rotPair : rotations.get(eIndex).getRotation()) {
							if (rotPair[0].getRankOrig(rotPair[1]) == a) {
								stablePairsA.add(rotPair);
							}
							if (rotPair[1].getRankOrig(rotPair[0]) == b) {
								stablePairsB.add(rotPair);
							}
						}
					}
				}

				// for each pair of stable pairs (m1,w1) and (m2,w2) where
				// rank(m1,w1)=a and rank(w2,m2)=b check whether there is a
				// stable matching containing (m1,w1) and (m2,w2)
				for (Person[] firstPair : stablePairsA) {

					for (Person[] secondPair : stablePairsB) {
						
						// check whether there is a stable matching containing this pair
						int firstPairTao = taos[firstPair[0].getIdIndex()][firstPair[1].getIdIndex()];
						int secondPairPhi = phis[secondPair[0].getIdIndex()][secondPair[1].getIdIndex()];

						boolean fpt_spp = false;
						if (firstPairTao == -1 || secondPairPhi == -1 
							|| rotationTC[secondPairPhi][firstPairTao] != 1) {
							fpt_spp = true;
						}

						if (fpt_spp) {
							if (a <= b && b - a < bestDifference) {
								bestDifference = b - a;
							}
							else if (b < a && a - b < bestDifference) {
								bestDifference = a - b;
							}

							// eliminate rotation containing first pair and all
							// predecessors &
							// eliminate any rotations containing pair with
							// woman rank worse than b and all predecessors
							LinkedList<Integer> rotsToEliminate = new LinkedList<Integer>();

							int r1 = model.getRotations(
								).findRotationThatCreatesPair(
								model,
								firstPair[0],
								firstPair[1]);
							if (r1 >= 0) {
								rotsToEliminate.add(r1);
							}
							
							for (int ri = 0; ri < rotations.getNum(); ri++) {
								Rotation r = rotations.get(ri);
								if (r.getRankWorstinRotation(OptsProfile.receiverView) 
									> b) {
									rotsToEliminate.add(ri);
								}
							}

							regretEqualMatching = manOptimal.clone();
							boolean[] eliminatedRots = new boolean[rotations.getNum()];
							for (Integer ri : rotsToEliminate) {
								regretEqualMatching = rotations.rotate(
									model,
									regretEqualMatching,
									ri,
									eliminatedRots);
							}

							return regretEqualMatching;
						}
					}
				}
			}
			model.undoTruncate();
		}	
		return null;
	}


	/**	
	* <p> Checks whether a rotations is eligible in the current truncated
	* instance. </p>
	* @param model 			model of the truncated instance
	* @param r 				rotation to check
	* @param manOpt 		the man optimal matching for the truncated instance
	* @param womanOpt 		the woman optimal matching for the truncated
	* instance
	* @return 	whether this rotation is eligible
	*/
	private boolean checkRotationEligibility(
		Model model, 
		Rotation r, 
		int[] manOptT, 
		int[] womanOptT) {

		// for each pair in the rotation
		// in fact we need only check a single pair in the rotation (see
		// associated paper). However this does not affect the time complexity
		// since there are n^2 pairs in total.
		for (int pairInd = 0; pairInd < 1; pairInd++) {
			// rank of the man and woman in the rotation pair
			Person rotMan = r.getRotation().get(pairInd)[0];
			Person rotWoman = r.getRotation().get(pairInd)[1];
			int rankRotManInRot = rotMan.getRank(rotWoman);
			int rankRotWomanInRot = rotWoman.getRank(rotMan);

			// the rank of the man in the man optimal matching
			Person womanAssignedToRotManInManOpt = model.getReceiver(
				manOptT[rotMan.getIdIndex()]);
			int rankRotManInManOpt = rotMan.getRank(
				womanAssignedToRotManInManOpt);

			// the rank of the man in the woman optimal matching
			Person womanAssignedToRotManInWomanOpt = model.getReceiver(
				womanOptT[rotMan.getIdIndex()]);
			int rankRotManInWomanOpt = rotMan.getRank(
				womanAssignedToRotManInWomanOpt);
	
			// if the man of the pair is doing better than in the man optimal
			// matching then the rotation is ineligible
			if (rankRotManInRot < rankRotManInManOpt) {
				return false;
			}
			// if the man of the pair is doing worse or equal to the woman
			// optimal matching then the rotation is ineligible
			if (rankRotManInRot >= rankRotManInWomanOpt) {
				return false;
			}
		}
		return true;
	}


	/**
	* <p> Returns the tao array for the current instance. </p>
	* <p> taos[m][w] = x if x is the unique rotation that moves m to w. </p>
	* @param model 		model of the instance
	* @return taos array 
	*/
	private int[][] createTaoArray(Model model) {
		int[][] taos = new int[model.getNumProposers()][model.getNumProposers()];
		for (int i = 0; i < taos.length; i++) {
			for (int j = 0; j < taos[i].length; j++) {
				taos[i][j] = -1;
			}
		}

		Rotations rotations = model.getRotations();
		for (int ri = 0; ri < rotations.getNum(); ri++) {
			Rotation rotation = rotations.get(ri);

			// this man moves to the next woman
			for (int rElem = 0; rElem < rotation.getRotation().size() - 1; rElem++) {
				Person man = rotation.getRotation().get(rElem)[0];
				Person woman = rotation.getRotation().get(rElem + 1)[1];
				taos[man.getIdIndex()][woman.getIdIndex()] = ri;
			}

			// final man moves to the first woman
			Person finalMan = rotation.getRotation().get(
				rotation.getRotation().size() - 1)[0];
			Person firstWoman = rotation.getRotation().get(0)[1];
			taos[finalMan.getIdIndex()][firstWoman.getIdIndex()] = ri;
		}

		return taos;
	}


	/**
	* <p> Returns the phi array for the current instance. </p>
	* <p> phis[m][w] = x if x is the unique rotation that moves m from w. </p>
	* @param model 		model of the instance
	* @return phis array 
	*/
	private int[][] createPhiArray(Model model) {
		int[][] phis = new int[model.getNumProposers()][model.getNumProposers()];
		for (int i = 0; i < phis.length; i++) {
			for (int j = 0; j < phis[i].length; j++) {
				phis[i][j] = -1;
			}
		}

		Rotations rotations = model.getRotations();
		for (int ri = 0; ri < rotations.getNum(); ri++) {
			Rotation rotation = rotations.get(ri);

			// this man is moving from this woman
			for (int rElem = 0; rElem < rotation.getRotation().size(); rElem++) {
				Person[] rotPair = rotation.getRotation().get(rElem);
				phis[rotPair[0].getIdIndex()][rotPair[1].getIdIndex()] = ri;
			}
		}

		return phis;
	}


	/**
	* <p> Returns the transitive closure for roations of the current instance.
	* </p>
	* <p> rotationTC[x][y] = 1 if x preceeds y in the rotation digraph, 0
	* otherwise. </p>
	* @param model 		model of the instance
	* @return rotationTC array 
	*/
	private int[][] createTransitiveClosure(Model model) {
		Rotations rotations = model.getRotations();
		int[][] rotationTC = new int[rotations.getNum()][rotations.getNum()];
		ArrayList<ArrayList<Integer>> simpleDigraph = model.getDigraph(
			).getSimpleDigraph();

		// create an array to allow marking of visited nodes (1 = visited,
		// 0 = not visited)
		int[] visitedNodes = new int[rotations.getNum()];

		// for each rotation perform a breadth first search to find all
		// reachable rotations
		for (int ri = 0; ri < rotations.getNum(); ri++) {

			// bfs
			Queue<Integer> queue = new LinkedList<Integer>();
			queue.add(ri);
			rotationTC[ri][ri] = 1;
			visitedNodes[ri] = 1;
			while (queue.size() > 0) {
				int currentRotation = queue.remove();

				// add all children to queue if unvisited
				for (int childIndex : simpleDigraph.get(currentRotation)) {
					if (visitedNodes[childIndex] == 0) {
						queue.add(childIndex);
						visitedNodes[childIndex] = 1;
						rotationTC[ri][childIndex] = 1;
					}
				}
			}

			// reset visited nodes
			for (int i = 0; i < visitedNodes.length; i++) {
				visitedNodes[i] = 0;
			}
		}

		return rotationTC;
	}


	/**
	* <p> Finds all possible degree pairs for stable matchings. </p>
	* @param manMin 		min man degree in any stable matching 
	* @param manMax 		max man degree in any stable matching 
	* @param womanMin 		min woman degree in any stable matching 
	* @param womanMax 		max woman degree in any stable matching 
	* @return rotationTC array 
	*/
	private LinkedList<Integer[]> getPossiblePairs(
		int manMin,
		int manMax,
		int womanMin,
		int womanMax) {

		// find the maximum possible difference
		int difference1 = womanMax - manMin;
		int difference2 = manMax - womanMin;
		int maxDifference  = -1;
		if (difference1 < difference2) {
			maxDifference = difference2 + 1;
		}
		else {
			maxDifference = difference1 + 1;
		}

		// create an arraylist to hold the lists with difference differences
		// of pairs
		ArrayList<LinkedList<Integer[]>> pairs = new ArrayList<LinkedList<Integer[]>>(maxDifference);
		for (int i = 0; i < maxDifference; i++) {
			pairs.add(new LinkedList<Integer[]>());
		}

		// create and add the pairs to the correct difference lists
		for (int manDegree = manMin; manDegree <= manMax; manDegree++) {
			for (int womanDegree = womanMin; womanDegree <= womanMax; womanDegree++) {
				
				int diff1 = manDegree - womanDegree;
				int diff2 = womanDegree - manDegree;
				int posDiff = -1;
				if (diff1 > 0) {
					posDiff = diff1;
				}
				else {
					posDiff = diff2;
				}

				pairs.get(posDiff).add(new Integer[] {manDegree, womanDegree});
			}
		}

		LinkedList<Integer[]> finalList = new LinkedList<Integer[]>();

		for (int i = 0; i < pairs.size(); i++) {
			finalList.addAll(pairs.get(i));
		}

		return finalList;
	}


	/**
	* <p> Prints results of this algorithm. </p>
	* @param model 		model of the instance
	* @return s 		results of the regret equal stable pair algorithm
	*/
	public String getResults(Model model) {
		String s = model.timeAndDate + "\n";
		s += "// Regret Equal Stable Pair Algorithm stats\n\n";
		Matching reMatching = model.createMatching(regretEqualMatching);
		s += reMatching.getReducedString() + "\n";
		return s;
	}


	/**
	* <p> Get a matching string for a given indexed matching. </p>
	* @param the matching to be returned
	* @return matching 		a string of the matching
	*/
	public String matchingAsString(int[] matching) {
		String s = "";
		for (int i = 0; i < matching.length; i++) {
			int receiver = matching[i] + 1;
			s += receiver + " ";
		}
		return s;
	}
}
