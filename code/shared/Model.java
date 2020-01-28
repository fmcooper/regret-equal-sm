package code.shared;
import java.util.ArrayList;
import java.util.*;
import java.math.*;

/**
 * <p> This class represents the model of an SM instance. </p>
 *
 * @author Frances
 */
public class Model {   
	/** <p> Information string - output to results file </p> */
	private String infoString;

	/** <p> The number of proposers in an instance </p> */
	private int numProposers;
	/** <p> An array of Person proposers </p> */
	private Person[] proposers;
	/** <p> An array of Person receivers </p> */
	private Person[] receivers;

	/** <p> proposer assignment results </p> */
	private int[] proposerAssignments;
	/** <p> List of rotations for this instance. </p>*/
	private Rotations rotations;
	/** <p> Digraph for this instance. </p>*/
	private Digraph digraph;

	/** <p> Stores when an instance run took place. </p> */
	public String timeAndDate;


	/**
	 * <p> Constructor for the Model class - sets the instance variables. </p>
	 * @param numProposers		
	 * @param proposersPrefs 
	 * @param receiversPrefs
	 */
	public Model(
		int numProposers, 
		int[][] proposersPrefs, 
		int[][] receiversPrefs) {
		
		// instantiate the instance variables
		this.numProposers = numProposers;
		proposers = new Person[numProposers];
		receivers = new Person[numProposers];
		int[][] proposersRankLists = new int[numProposers][numProposers];
		int[][] receiversRankLists = new int[numProposers][numProposers];
		for (int i = 0; i < numProposers; i++) {
			for (int j = 0; j < numProposers; j++) {
				proposersRankLists[i][j] = -1;
				receiversRankLists[i][j] = -1;
			}
		}

		proposerAssignments = new int[numProposers];

		// creating proposers
		for (int i = 0; i < numProposers; i++) {
			proposers[i] = new Person(i, null, null);
		}

		// creating receivers
		for (int i = 0; i < numProposers; i++) {
			int[] prefListInt = receiversPrefs[i];
			ArrayList<Person> prefList = new ArrayList<Person>();
			for (int j = 0; j < prefListInt.length; j++) {
				int propInd = prefListInt[j];
				receiversRankLists[i][propInd] = j;
				prefList.add(proposers[propInd]);
			}
			receivers[i] = new Person(i, prefList, receiversRankLists[i]);
		}

		// adding preference list to proposers
		for (int i = 0; i < numProposers; i++) {
			int[] prefListInt = proposersPrefs[i];
			ArrayList<Person> prefList = new ArrayList<Person>();
			for (int j = 0; j < prefListInt.length; j++) {
				int recInd = prefListInt[j];
				proposersRankLists[i][recInd] = j;
				prefList.add(receivers[recInd]);
			}
			proposers[i].setPreferenceList(prefList);
			proposers[i].setRankList(proposersRankLists[i]);
			proposers[i].setNoChangePreferenceList();
			proposers[i].setNoChangeRankList();

		}

		rotations = new Rotations(this);
		digraph = new Digraph(this);
		
		infoString = "";
		timeAndDate = "";
	}


	/**************************************************************************/
	// Matching properties methods
	/**************************************************************************/

	/**
	* <p> Creates a new Matching with the given. </p>
	* @param array of matching indices
	*/
	public Matching createMatching(int[] matching) {
		int[] profileMen = calcProfile(matching, OptsProfile.proposerView);
		int[] profileWomen = calcProfile(matching, OptsProfile.receiverView);
		int[] profile = calcProfile(matching, OptsProfile.combinedView);
		int costMen = calcCost(matching, OptsProfile.proposerView);
		int costWomen = calcCost(matching, OptsProfile.receiverView);
		int cost = calcCost(matching, OptsProfile.combinedView);

		return new Matching(
			matching, 
			profileMen, 
			profileWomen, 
			profile, 
			costMen, 
			costWomen, 
			cost);
	}


	/**
	 * <p> Returns the profile of the given matching. </p>
	 * @param matching
	 * @param indicator (whether looking from proposer, reciever or both view)
	 * 
	 * @return the profile of the matching
	 */
	public int[] calcProfile(int[] matching, OptsProfile indicator) {
		int[] profile = new int[numProposers];
		for (int i = 0; i < numProposers; i++) {
			if (matching[i] != -1) {
				// proposer view
				if (indicator == OptsProfile.proposerView) {
					int rank = proposers[i].getRank(receivers[matching[i]]);
					profile[rank - 1]++;
				}
				// receiver view
				else if (indicator == OptsProfile.receiverView) {
					int rank = receivers[matching[i]].getRank(proposers[i]);
					profile[rank - 1]++;
				}
				// combined view
				else if (indicator == OptsProfile.combinedView) {
					int rank = proposers[i].getRank(receivers[matching[i]]);
					profile[rank - 1]++;
					rank = receivers[matching[i]].getRank(proposers[i]);
					profile[rank - 1]++;
				}
			}
		}
		return profile;
	}

	/**
	 * <p> Returns the degree of the given matching. </p>
	 * @param matching
	 * @param indicator (whether looking from proposer, reciever or both view)
	 * 
	 * @return the profile of the matching
	 */
	public int calcDegree(int[] matching, OptsProfile indicator) {
		int[] profile = calcProfile(matching, indicator);
		int numzeros = 0;
		boolean fin = false;
		int index = profile.length - 1;
		while (!fin && index >= 0) {
			if (profile[index] == 0) {
				numzeros++;
			}
			else {
				fin = true;
			}
			index--;
		}
		return profile.length - numzeros;
	}


	/**
	 * <p> Returns the regret equal score of the given matching. </p>
	 * @param matching
	 * 
	 * @return the regret equal score
	 */
	public int calcDegreeDiff(int[] matching) {
		int ma = calcDegree(matching, OptsProfile.proposerView);
		int mb = calcDegree(matching, OptsProfile.receiverView);
		int md = -1;
		if (ma < mb) {
			md = mb - ma;
		}
		else {
			md = ma - mb;
		}
		return md;
	}


	/**
	 * <p> Returns the cost of the given matching. </p>
	 * @param matching
	 * @param indicator (whether looking from propower, reciever or both view)
	 *
	 * @return the cost of the matching
	 */
	public int calcCost(int[] matching, OptsProfile indicator) {
		int cost = 0;
		for (int i = 0; i < numProposers; i++) {
			if (matching[i] != -1) {
				// proposer view
				if (indicator == OptsProfile.proposerView) {
					int rank = proposers[i].getRank(receivers[matching[i]]);
					cost += rank;
				}
				// receiver view
				else if (indicator == OptsProfile.receiverView) {
					int rank = receivers[matching[i]].getRank(proposers[i]);
					cost += rank;
				}
				// combined view
				else if (indicator == OptsProfile.combinedView) {
					int rank = proposers[i].getRank(receivers[matching[i]]);
					cost += rank;
					rank = receivers[matching[i]].getRank(proposers[i]);
					cost += rank;
				}
			}
		}
		return cost;
	}



	/**************************************************************************/
	// Instance adaptation methods
	/**************************************************************************/

	/**
	 * <p> Truncates the preference lists according to the given ranks. </p>
	 * @param pair 		the pair of ranks that show where to truncate preference
	 * lists
	 */
	public void truncate(Integer[] pair) {
		// truncate proposers and receivers
		for (Person p : proposers) {
			p.truncate(pair[0]);
		}
		for (Person r : receivers) {
			r.truncate(pair[1]);
		}
	}


	/**
	 * <p> Undoes a preference list truncation by reinstating original saved
	 * lists. </p>
	 */
	public void undoTruncate() {
		for (Person p : proposers) {
			p.reinstateSavedPrefList();
		}
		for (Person r : receivers) {
			r.reinstateSavedPrefList();
		}
	}


	/**
	 * <p> Resets agents assignments and their current index variable. </p>
	 */
	public void resetAssignedAndCurrentIndex() {
		proposerAssignments = new int[numProposers];
		for (Person p : proposers) {
			p.setAssigned(null);
			p.setCurrentPrefIndex(-1);
		}
		for (Person r : receivers) {
			r.setAssigned(null);
			r.setCurrentPrefIndex(-1);
		}
	}


	/**
	 * <p> Sets fresh agents and refreshes rotation object pointers. </p>
	 * @param proposers
	 * @param receivers
	 */
	public void setAndRefreshAgents(Person[] props, Person[] recs) {
		proposers = props;
		receivers = recs;

		rotations.refresh(props, recs);
	}


	/**
	 * <p> Swaps proposers and receivers around. </p>
	 */
	public void swapProposer() {
		Person[] tempProp = receivers;
		receivers = proposers;
		proposers = tempProp;
	}


	/**
	 * <p> Set the assignment information. </p>
	 */
	public void setAssignmentInfo() {
		for (int i = 0; i < proposerAssignments.length; i++) {
			int recInd = proposerAssignments[i];
			if (recInd != -1) {
				proposers[i].setAssigned(receivers[recInd]);
				receivers[recInd].setAssigned(proposers[i]);
			}
		}
	}


	/**
	 * <p> Refreshes the preference and rank lists from saved lists. </p>
	 */
	public void refreshAgentLists() {
		for (int i = 0; i < numProposers; i++) {
			proposers[i].refreshLists(receivers);
			receivers[i].refreshLists(proposers);
		}
	}


	/**
	 * <p> Makes preference lists and rank lists consistent with each other.
	 * </p>
	 */
	public void preferenceListConsistency() {
		for (int i = 0; i < numProposers; i++) {
			proposers[i].prefsConsistent();
			receivers[i].prefsConsistent();
		}
	}


	/**
	 * <p> Saves the current preference lists as GS lists. </p>
	 */
	public void saveGSlists() {
		for (int i = 0; i < numProposers; i++) {
			proposers[i].saveGSlist();
			receivers[i].saveGSlist();
		}
	}


	/**
	 * <p> Reinstates the save GS lists as preference lists. </p>
	 */
	public void reinstateGSlists() {
		for (int i = 0; i < numProposers; i++) {
			proposers[i].reinstateGSlists();
			receivers[i].reinstateGSlists();
		}
	}


	/**************************************************************************/
	// Other methods
	/**************************************************************************/

	/**
	 * <p> Getters and setters. </p>
	 */

	public int getNumProposers() {return numProposers;}
	public Person[] getProposers() {return proposers;}
	public Person getProposer(int i) {return proposers[i];}
	public Person getReceiver(int i) {return receivers[i];}
	public Person[] getReceivers() {return receivers;}
	public Rotations getRotations() {return rotations;}
	public int[] getProposerAssignments() {return proposerAssignments;}
	public Digraph getDigraph() {return digraph;}

	public int[] getProposerAssignmentsSwitched() {
		// create switched array and initialise to -1 for each entry
		int[] switched = new int[numProposers]; 
		for (int i = 0; i < numProposers; i++) {
			switched[i] = -1;
		}

		// add results to switched array
		for (int i = 0; i < numProposers; i++) {
			if (proposerAssignments[i] != -1) {
				switched[proposerAssignments[i]] = i;
			}
		}
		return switched;
	}

	public int[] getProposerAssignmentsSwitched(int[] array) {
		// create switched array and initialise to -1 for each entry
		int[] switched = new int[array.length]; 
		for (int i = 0; i < numProposers; i++) {
			switched[i] = -1;
		}

		// add results to switched array
		for (int i = 0; i < numProposers; i++) {
			if (array[i] != -1) {
				switched[array[i]] = i;
			}
		}
		return switched;
	}

	public void setProposerAssignments(int[] proposerAssignments) {
		this.proposerAssignments = proposerAssignments;
	}


	/**
	 * <p> Print the model. </p>
	 */
	public void print() {
		System.out.println("proposers");
		for (Person p : proposers) {
			System.out.println(p);
		}
		System.out.println("receivers");
		for (Person p : receivers) {
			System.out.println(p);
		}
	}


	/**
	 * <p> Prints IDs in a Person array. </p>
	 */
	public void print(Person[] pArray, String message) {
		System.out.println("\n" + message);
		for (int i = 0; i < pArray.length; i++) {
			System.out.print(pArray[i].getId() + " ");
		}
		System.out.println("\n");
	}
}
