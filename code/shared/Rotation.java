package code.shared;
import java.util.*;

/**
 * <p> Rotation object, characterises a rotation of a matching. </p>
 *
 * @author Frances
 */
public class Rotation {

	/** <p> Preference list of this person </p> */
	private ArrayList<Person[]> rotation;


	/**
	 * <p> Constructor - sets the instance variables. </p>
	 */
	public Rotation() {
		rotation = new ArrayList<Person[]>();
	}


	/**
	 * <p> Adds a man, woman pair to the rotation. </p>
	 * @param man 
	 * @param woman
	 */
	public void add(Person man, Person woman) {
		Person[] couple = {man, woman};
		rotation.add(0, couple);
	}


	/**
	 * <p> Rotate the given matching about this rotation and return the
	 * resultant matching. </p>
	 * @param matching
	 *
	 * @return resultant matching
	 */
	public int[] rotate(int[] matching) {
		int firstWoman = rotation.get(0)[1].getIdIndex();
		for (int i = 0; i < rotation.size() - 1; i++) {
			int man = rotation.get(i)[0].getIdIndex();
			int nextwoman = rotation.get(i+1)[1].getIdIndex();
			matching[man] = nextwoman;
		}

		int lastMan = rotation.get(rotation.size() - 1)[0].getIdIndex();
		matching[lastMan] = firstWoman;

		return matching;
	}


	/**
	 * <p> Returns whether this rotation is exposed in the given matching. </p>
	 * @param matching
	 *
	 * @return if this rotation is exposed
	 */
	public boolean isExposed(int[] matching) {
		for (Person[] pair : rotation) {
			int manIdIndex = pair[0].getIdIndex();
			int womanIdIndex = pair[1].getIdIndex();
			if (matching[manIdIndex] != womanIdIndex) {
				return false;
			}
		}
		return true;
	} 


	/**
	 * <p> Returns number of pairs containing receiver of rank b. </p>
	 * @param b 		womens rank
	 *
	 * @return number of pairs with receiver of rank b
	 */
	public int numPairsRecieverRank(int b) {
		int num = 0;
		for (Person[] pair : rotation) {
			if (pair[1].getRank(pair[0]) == b) {
				num++;
			}
		}
		return num;
	}


	/**
	 * <p> Returns whether this rotation contains the given man woman pair. </p>
	 * @param man
	 * @param woman
	 *
	 * @return if this rotation contains the pair
	 */
	public boolean contains(Person man, Person woman) {
		for (Person[] pair : rotation) {
			if (pair[0] == man && pair[1] == woman) {
				return true;
			}
		}
		return false;
	}


	/**
	 * <p> Returns whether this rotation creates the given man woman pair. </p>
	 * @param man
	 * @param woman
	 *
	 * @return if this rotation creates the pair
	 */
	public boolean creates(Person man, Person woman) {
		for (int i = 0; i < rotation.size() - 1; i++) {
			if (rotation.get(i)[0] == man && rotation.get(i + 1)[1] == woman) {
				return true;
			}
		}
		if (rotation.get(
			rotation.size() - 1)[0] == man && rotation.get(0)[1] == woman) {
			return true;
		}

		return false;
	}


	/**
	 * <p> Returns the rank of the worst person in this rotation according to
	 * the specified view. </p>
	 * @param view
	 *
	 * @return rank of worst person in rotation
	 */
	public int getRankWorstinRotation(OptsProfile view) {
		int worstRankSoFar = -1;
		
		for (int i = 0; i < rotation.size(); i++) {
			Person man = rotation.get(i)[0];
			Person woman = rotation.get(i)[1];
			int rank = -1;

			if (view == OptsProfile.proposerView) {
				rank = man.getRank(woman);
			}
			else if (view == OptsProfile.receiverView) {
				rank = woman.getRank(man);
			}
			else {
				int manRank = man.getRank(woman);
				int womanRank = woman.getRank(man);
				if (manRank < womanRank) {
					rank = womanRank;
				}
				else {
					rank = manRank;
				}
			}
			
			if (worstRankSoFar == -1 || rank > worstRankSoFar) {
				worstRankSoFar = rank;
			}
		}
		return worstRankSoFar;
	}


	/**
	 * <p> Returns the rank of the worst person after rotating this rotation 
	 * according to the specified view. </p>
	 * @param view
	 *
	 * @return rank of worst person in rotation
	 */
	public int getRankWorstAfterRotation(OptsProfile view) {
		// create a new 'rotation' with these partners rotated
		Rotation fakeRotation = new Rotation();
		for (int i = 0; i < rotation.size() - 1; i++) {
			fakeRotation.add(rotation.get(i)[0], rotation.get(i + 1)[1]);
		}
		fakeRotation.add(
			rotation.get(rotation.size() - 1)[0], rotation.get(0)[1]);

		// use existing method to find number of people at given rank
		return fakeRotation.getRankWorstinRotation(view);
	}

	
	/**
	 * <p> Update the preferences of each pair in the rotation. </p>
	 */
	public void updatePrefs() {
		for (int i = 0; i < rotation.size() - 1; i++) {
			Person man = rotation.get(i)[0];
			Person woman = rotation.get(i + 1)[1];

			woman.removeWorseProposersMD(man);
		}
		// first woman last man
		rotation.get(0)[1].removeWorseProposersMD(
			rotation.get(rotation.size() - 1)[0]);
	}


	/**
	 * <p> Get combined profile change of a rotation. </p>
	 * @param stableMatching
	 *
	 * @return the profile of the rotation
	 */
	private int[] calcProfileChange(int profileSize, OptsProfile indicator) {
		int[] profile = new int[profileSize];

		for (int i = 0; i < rotation.size(); i++) {
			Person thisMan = rotation.get(i)[0];
			Person thisWoman = rotation.get(i)[1];
			Person nextWoman = null;
			Person nextMan = null;
			if (i == rotation.size() - 1) {
				nextMan = rotation.get(0)[0];
				nextWoman = rotation.get(0)[1];
			}
			else {
				nextMan = rotation.get(i + 1)[0];
				nextWoman = rotation.get(i + 1)[1];
			}
			
			// proposer view
			if (indicator == OptsProfile.proposerView) {
				// rank of current partner minus rank of next partner
				int rankCurrent = thisMan.getRank(thisWoman);
				int rankNew = thisMan.getRank(nextWoman);
				profile[rankCurrent - 1]--;
				profile[rankNew - 1]++;
			}
			// receiver view
			else if (indicator == OptsProfile.receiverView) {
				int rankCurrent = nextWoman.getRank(nextMan);
				int rankNew = nextWoman.getRank(thisMan);
				profile[rankCurrent - 1]--;
				profile[rankNew - 1]++;
			}
			// combined view
			else if (indicator == OptsProfile.combinedView) {
				// prop view
				int rankCurrent = thisMan.getRank(thisWoman);
				int rankNew = thisMan.getRank(nextWoman);
				profile[rankCurrent - 1]--;
				profile[rankNew - 1]++;

				// rec view
				rankCurrent = nextWoman.getRank(nextMan);
				rankNew = nextWoman.getRank(thisMan);
				profile[rankCurrent - 1]--;
				profile[rankNew - 1]++;
			}
		}
		return profile;
	}


	/**
	 * <p> Given an input matching, returns the new matching found by
	 * eliminating this rotation in the input matching. </p>
	 * @param matching
	 * 
	 * @return new matching
	 */
	public int[] eliminateRotation(int[] matching) {
		for (int i = 0; i < rotation.size(); i++) {
			int thisManIndex = rotation.get(i)[0].getIdIndex();
			int nextWomanIndex = -1;
			if (i == rotation.size() - 1) {
				nextWomanIndex = rotation.get(0)[1].getIdIndex();
			}
			else {
				nextWomanIndex = rotation.get(i + 1)[1].getIdIndex();
			}
			matching[thisManIndex] = nextWomanIndex;
		}

		return matching;
	}


	/**
	 * <p> Refreshes rotation object pointers. </p>
	 * @param proposers
	 * @param receivers
	 */
	public void refresh(Person[] props, Person[] recs) {
		for (Person[] pair : rotation) {
			pair[0] = props[pair[0].getIdIndex()];
			pair[1] = recs[pair[1].getIdIndex()];
		}
	}


	/**
	 * <p> Getters. </p>
	 */
	public ArrayList<Person[]> getRotation() {return rotation;}


	/**
	 * <p> Get combined profile change of a rotation. </p>
	 * @param stableMatching
	 *
	 * @return the profile of the rotation
	 */
	private String rotationProfileChange(
		int profileSize, 
		OptsProfile indicator) {

		int[] profile = calcProfileChange(profileSize, indicator);
		String stringProfile = "";
		for (int i = 0; i < profile.length; i++) {
			stringProfile += profile[i] + " ";
		}
		return stringProfile;
	}


	/**
	* <p> Returns string information for this rotation. </p>
	*/
	public String getResults(int i, int profileSize) {
		String s = "";
		s += "rotation_" + i + ": ";
		for (int j = 0; j < rotation.size(); j++) {
			Person[] couple = rotation.get(j);
			s += "(" + couple[0].getId() + ", " + couple[1].getId() + ")";
		}
		s+= "\n";
		s += "rotProfileMen_" + i + ": " + rotationProfileChange(
			profileSize, OptsProfile.proposerView) + "\n";
		s += "rotProfileWomen_" + i + ": " + rotationProfileChange(
			profileSize, OptsProfile.receiverView) + "\n";
		s += "rotProfileCombined_" + i + ": " + rotationProfileChange(
			profileSize, OptsProfile.combinedView) + "\n";
		return s;
	}
}
