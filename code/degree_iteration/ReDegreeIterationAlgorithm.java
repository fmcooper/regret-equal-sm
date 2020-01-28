package code.degree_iteration;
import code.shared.*;
import java.util.*;

public class ReDegreeIterationAlgorithm {
	/** <p> Regret-equal stable matching. </p> */
	private int[] regretEqualMatching;

	/**
	* <p> Constructor. </p>
	* @param mens model
	* @param womens model
	*/
	public ReDegreeIterationAlgorithm(Model model) {
		
		// run the algorithm 
		regretEqualMatching = runAlg(model);
	}

	/**
	* <p> Regret-equal Degree Iteration Algorithm. </p>
	* @param the model instance
	* @return matching 		a regret equal matching
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

		int a0 = model.calcDegree(manOptimal, OptsProfile.proposerView);
		int b0 = model.calcDegree(manOptimal, OptsProfile.receiverView);

		// cannot do better if the man optimal has men doing worse than women
		if (a0 >= b0) {
			return manOptimal;
		}

		// mOpt holds the best matching found so far
		int[] mOpt = manOptimal.clone();

		// column operation for first column
		mOpt = columnOperation(
			model, 
			mOpt.clone(), 
			new boolean[model.getRotations().getNum()], 
			mOpt.clone());

		// if we have an optimal regret-equal matching then return it
		int amOpt = model.calcDegree(mOpt, OptsProfile.proposerView);
		int bmOpt = model.calcDegree(mOpt, OptsProfile.receiverView);
		if (amOpt == bmOpt) {
			return mOpt;
		}

		// for each man
		for (Person man : model.getProposers()) {
			int[] m = manOptimal.clone();
			boolean[] q = new boolean[model.getRotations().getNum()];

			int womanInM = m[man.getIdIndex()];
			int womanInMz = womanOptimal[man.getIdIndex()];

			int am = model.calcDegree(m, OptsProfile.proposerView);
			int bm = model.calcDegree(m, OptsProfile.receiverView);

			// continue to rotate this man down until they reach their partner
			// in Mz
			while(womanInM != womanInMz && am < bm) {
				am = model.calcDegree(m, OptsProfile.proposerView);

				// find the rotation we need to eliminate to move this man down
				int rIndex = model.getRotations().findRotationThatContainsPair(
					model,
					man, 
					model.getReceiver(m[man.getIdIndex()]));

				// rotate this rotation and all predecessors
				m = model.getRotations().rotate(model, m, rIndex, q);

				int dum = model.calcDegree(m, OptsProfile.proposerView);
				int rankManInM = man.getRank(
					model.getReceiver(m[man.getIdIndex()]));

				// if this man is one of the worst ranked in M and we have 
				// increased the worst rank in M then perform the column
				// operation
				if (dum > am && rankManInM == dum) {
					// column operation 
					mOpt = columnOperation(
						model, 
						m.clone(), 
						q.clone(), 
						mOpt.clone());

					// if we have an optimal regret-equal matching then return
					// it
					amOpt = model.calcDegree(mOpt, OptsProfile.proposerView);
					bmOpt = model.calcDegree(mOpt, OptsProfile.receiverView);
					if (amOpt == bmOpt) {
						return mOpt;
					}
				}
				womanInM = m[man.getIdIndex()];
				am = model.calcDegree(m, OptsProfile.proposerView);
				bm = model.calcDegree(m, OptsProfile.receiverView);	
			}
		}
		return mOpt;
	}


	/**
	* <p> Column operation. Handles how to iterate down a column updating mOpt
	* if find a better stable matching according to the regret-equality score. 
	* </p>
	* @param model 		the current model
	* @param m 	the matching M to start at for this column
	* @param q 			the set of rotations corresponding to M
	* @param mOpt 		the most opptimal matching found so far
	* @return mOpt 		the most opptimal matching found so far
	*/
	private int[] columnOperation(
		Model model, int[] m, boolean[] q, int[] mOpt) {

		int a = model.calcDegree(m, OptsProfile.proposerView);

		// while we have not returned mOpt
		while(true) {
			if (model.calcDegreeDiff(m) < model.calcDegreeDiff(mOpt)) {
				mOpt = m.clone();
			}

			int am = model.calcDegree(m, OptsProfile.proposerView);
			int bm = model.calcDegree(m, OptsProfile.receiverView);
			
			if (am >= bm) {
				return mOpt;
			}

			// the number of man-woman pairs in our current matching with women
			// at rank b
			int numPairsInMWithWomenRankB = getNumPairsMWithRankB(
				model, m, bm);

			// rotations not yet eliminated that contain women of rank b and men
			// of rank no larger than a after elimination
			ArrayList<Integer> validRotations = getRotsWithRankBNotIncA(
				model.getRotations(), q, am, bm);

			// the number of man-woman pairs in valid rotations with women at 
			// rank b
			int numPairsRankBInValidRotations = getNumPairsRWithRankB(
				model.getRotations(),
				validRotations, 
				bm);

			if (numPairsRankBInValidRotations != numPairsInMWithWomenRankB) { 
				return mOpt;
			}

			else {
				// eliminate these rotations (total O(n^2) time - see method
				// 'rotate' for details)
				for (int rotIndex : validRotations) {
					m = model.getRotations().rotate(model, m, rotIndex, q);
				}
			}
		}
	}


	/**
	* <p> Finds the number of pairs in the given rotations which have women at
	* rank b. </p>
	* @param rotations 			all rotations of I
	* @param validRotations 	indices of valid rotations
	* @param b 					rank
	* 
	* @return number of pairs in the given rotations which have women at rank b
	*/
	public int getNumPairsRWithRankB(
		Rotations rotations, 
		ArrayList<Integer> validRotations,  
		int b) {

		int num = 0;
		for (int i : validRotations) {
			num += rotations.get(i).numPairsRecieverRank(b);
		}
		return num;
	}


	/**
	* <p> Finds the number of pairs in the given matching which have women at
	* rank b. </p>
	* @param model  			model of I
	* @param m 					given matching m
	* @param b 					rank
	* 
	* @return number of pairs in the given matching with women at rank b
	*/
	public int getNumPairsMWithRankB(Model model, int[] m, int b) {
		int num = 0;
		for (int man = 0; man < m.length; man++) {
			int woman = m[man];
			int rank = model.getReceiver(woman).getRank(model.getProposer(man));

			if (rank == b) {
				num++;
			}
		}
		return num;
	}


	/**
	* <p> Returns all rotations not yet eliminated which contain a woman with
	* rank b that do not increase men to above a once eliminated. </p>
	* @param model 			model of the instance
	* @param b 				the rank to test for
	* 
	* @return rotsRankB 	rotation indices
	*/
	public ArrayList<Integer> getRotsWithRankBNotIncA(
		Rotations rotations, 
		boolean[] q, 
		int a,
		int b) {

		ArrayList<Integer> returningRots = new ArrayList<Integer>();
		for (int i = 0; i < rotations.getNum(); i++) {
			Rotation r = rotations.get(i);
			int worstRankWInR = r.getRankWorstinRotation(
				OptsProfile.receiverView);
			int worstRankMAfterR = r.getRankWorstAfterRotation(
				OptsProfile.proposerView);

			if (!q[i] && (worstRankWInR == b) && (!(worstRankMAfterR > a))) {
				returningRots.add(i);
			}
		}
		return returningRots;
	}


	/**
	* <p> Prints results of this algorithm. </p>
	* @param model 		model of the instance
	* @return s 		results of the regret equal degree iteration algorithm
	*/
	public String getResults(Model model) {
		String s = model.timeAndDate + "\n";
		s += "// Regret Equal Degree Iteration Algorithm stats\n\n";
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
