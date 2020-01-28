package code.shared;
import java.util.ArrayList;


/**
 * <p> Person object, characterising men and women. </p>
 *
 * @author Frances
 */
public class Person {

	/** <p> Preference list of this person </p> */
	private ArrayList<Person> preferenceList;
	/** <p> Preference list index </p> */
	private int currentPrefIndex;
	/** <p> Person ID </p> */
	private int id;
	/** <p> Person ID index = Person ID - 1 </p> */
	private int idIndex;
	/** <p> Preferences held as a rank list </p> */
	private int[] rankList;
	/** <p> This persons assigned partner </p> */
	private Person assigned;
	/** <p> A tag used in the minimal differences algorithm </p> */
	private boolean mark;

	/** <p> Copies of the original preference and rank list </p> */
	private ArrayList<Person> noChangesPreferenceList;
	private int[] noChangesRankList;
	private ArrayList<Person> gsList;
	private int[] gsRanks;


	/**
	 * <p> Constructor - sets the instance variables. </p>
	 * @param idIndex		
	 * @param preferenceList 
	 * @param rankList
	 */
	public Person(
		int idIndex, 
		ArrayList<Person> preferenceList, 
		int[] rankList) {

		this.preferenceList = preferenceList;
		this.idIndex = idIndex;
		this.id = idIndex + 1;
		this.currentPrefIndex = -1;
		assigned = null;
		this.rankList = rankList;

		if (preferenceList != null) {
			noChangesPreferenceList = new ArrayList<Person>();
			for (int pIndex = 0; pIndex < preferenceList.size(); pIndex++) {
				noChangesPreferenceList.add(preferenceList.get(pIndex));
			}
			noChangesRankList = new int[rankList.length]; 
			this.noChangesRankList = rankList.clone();
		}

		mark = false;
		gsList = null;
		gsRanks = null;
	}


	/**
	* <p> Makes preference lists and rank lists consistent with each other.
	* Nulls all preference elements where their rank is -1. </p>
	*/
	public void prefsConsistent() {
		// consistent preference lists
		for (int i = 0; i < preferenceList.size(); i++) {
			Person p = preferenceList.get(i);
			if (p != null) {
				if (rankList[p.getIdIndex()] == -1) {
					preferenceList.set(i, null);
				}
			}
		}
	}


	/**
	 * <p> Refreshes preference and rank lists from the saved lists. </p>
	 * @param 
	 */
	public void refreshLists(Person[] otherAgentList) {
		// refresh preference list
		ArrayList<Person> newPrefList = new ArrayList<Person>();
		for (int i = 0; i < noChangesPreferenceList.size(); i++) {
			newPrefList.add(
				otherAgentList[noChangesPreferenceList.get(i).getIdIndex()]);
		}
		preferenceList = newPrefList;

		// refresh rank list
		int[] newRankList = new int[noChangesRankList.length];
		for (int i = 0; i < noChangesRankList.length; i++) {
			newRankList[i] = noChangesRankList[i];
		}
		rankList = newRankList;
	}

	
	/**
	 * <p> Returns the next preference list Person according to the preference
	 * list index. </p>
	 *
	 * @return next person
	 */
	public Person getNextReceiver() {
	 	currentPrefIndex ++;
	 	return preferenceList.get(currentPrefIndex);
	}


	/**
	 * <p> Returns whether this person likes the given person more than their
	 * currently assigned person. </p>
	 * @param proposer
	 *
	 * @return prefers
	 */
	public boolean likes(Person proposer) {
		if (getRank(proposer) == 0) {
			return false;
		}
		if (assigned == null) {
			return true;
		}
		return getRank(proposer) < getRank(assigned);
	}


	/**
	 * <p> Returns the rank of the given person. </p>
	 * @param other
	 *
	 * @return rank
	 */
	public int getRank(Person other) {
		if (other == null) {
			return -1;
		}
		return getRankIndex(other) + 1;
	}

	/**
	 * <p> Returns the rank of the given person. </p>
	 * @param other
	 *
	 * @return rank
	 */
	public int getRankOrig(Person other) {
		return getRankOrigIndex(other) + 1;
	}


	/**
	 * <p> Returns the rank index of the given person. </p>
	 * @param other
	 *
	 * @return rank
	 */
	public int getRankIndex(Person other) {
		return rankList[other.idIndex];
	}


	/**
	 * <p> Returns the rank index of the given person. </p>
	 * @param other
	 *
	 * @return rank
	 */
	public int getRankOrigIndex(Person other) {
		return noChangesRankList[other.idIndex];
	}


	/**
	 * <p> Removes worse proposers from just after the assigned person in
	 * preference list. </p>
	 */
	public void removeWorseProposers() {
		int toDelFrom = rankList[assigned.getIdIndex()] + 1;
		int lengthPrefListOrig = preferenceList.size();

		for (int i = toDelFrom; i < lengthPrefListOrig; i++) {
			Person personToDelete = preferenceList.get(i);

			if (personToDelete != null) {
				// remove from rank lists
				rankList[personToDelete.getIdIndex()] = -1;
				personToDelete.getRankList()[this.getIdIndex()] = -1;

				// remove from preference list
				preferenceList.set(i, null);
			}
		}
	}


	/**
	 * <p> Removes worse proposers from just after the given person in
	 * preference list. Also, removes this person from that preference list as
	 * well. </p>
	 * @param person
	 */
	public void removeWorseProposersMD(Person man) {
		int toDelFrom = rankList[man.getIdIndex()] + 1;
		int lengthPrefListOrig = preferenceList.size();

		for (int i = toDelFrom; i < lengthPrefListOrig; i++) {
			Person personToDelete = preferenceList.get(i);

			if (personToDelete != null) {
				// remove from rank lists
				rankList[personToDelete.getIdIndex()] = -1;

				// remove from preference list
				preferenceList.set(i, null);
				int rankPersonToDelete = personToDelete.getRankList()[this.getIdIndex()] + 1;
				personToDelete.setPersonToNull(this);
			}
		}
	}


	/**
	 * <p> Saves the current preference list as a GS list. </p>
	 */
	public void saveGSlist() {
		ArrayList<Person> list = new ArrayList<Person>();
		for (int i = 0; i < preferenceList.size(); i++) {
			list.add(preferenceList.get(i));
		}
		gsList = list;
		gsRanks = rankList.clone();
	}


	/**
	 * <p> Sets the given person to null in the preference list and -1 in the
	 * rank list. </p>
	 */
	public void setPersonToNull(Person toSet) {
		int rankIndex = getRankIndex(toSet);
		if (rankIndex != -1) {
			preferenceList.set(rankIndex, null);
			rankList[toSet.getIdIndex()] = -1;
		}
	}


	/**
	 * <p> Truncates the preference list at a given rank and updates rank list.
	 * </p>
	 * @param rank
	 */
	public void truncate(Integer rank) {
		// System.out.println("this: " + toString());
		for (int i = rank; i < preferenceList.size(); i++) {
			Person other = preferenceList.get(i);
			
			if (other != null) {
				other.setPersonToNull(this);
				preferenceList.set(i, null);
				rankList[other.getIdIndex()] = -1;
			}
			
		}
	}


	/**
	 * <p> Reinstates the saved GS list. </p>
	 */
	public void reinstateGSlists() {
		preferenceList = new ArrayList<Person>();
		for (int pIndex = 0; pIndex < gsList.size(); pIndex++) {
			preferenceList.add(gsList.get(pIndex));
		}

		rankList = gsRanks.clone();
	}


	/**
	 * <p> Reinstates the saved preference list. </p>
	 */
	public void reinstateSavedPrefList() {
		for (int pIndex = 0; pIndex < noChangesPreferenceList.size(); pIndex++) {
			preferenceList.set(pIndex, noChangesPreferenceList.get(pIndex));
			rankList[pIndex] = noChangesRankList[pIndex];
		}
	}


	/**
	 * <p> Creates a new rank list from the current preference list. </p>
	 * @param n 		the size of the new rank list
	 */
	public void createNewRankList(int n) {
		int[] newRankList = new int[n];
		for (int i = 0; i < newRankList.length; i++) {
			newRankList[i] = -1;
		}
		for (int j = 0; j < preferenceList.size(); j++) {
			Person p = preferenceList.get(j);
			newRankList[p.idIndex] = j;
		}
		rankList = newRankList;
	}


	/**
	 * <p> Getters and setters. </p>
	 */
	public ArrayList<Person> getPreferenceList() {return preferenceList;}
	public Person getPreferenceListPersonAt(int id) {
		return preferenceList.get(id);
	}
	public int[] getRankList() {return rankList;}
	public Person getAssigned() {return assigned;}
	public int getIdIndex() {return idIndex;}
	public int getId() {return id;}
	public int getCurrentPrefIndex() {return currentPrefIndex;}
	public boolean getMark() {return mark;}
	public void setPreferenceList(ArrayList<Person> prefList) {
		preferenceList = prefList;
	}
	public void setRankList(int[] rankList) {this.rankList = rankList;}
	public void setNoChangePreferenceList() {
		noChangesPreferenceList = new ArrayList<Person>();
		for (int pIndex = 0; pIndex < preferenceList.size(); pIndex++) {
			noChangesPreferenceList.add(preferenceList.get(pIndex));
		}
	}
	public void setNoChangeRankList() {
		noChangesRankList = new int[rankList.length]; 
		this.noChangesRankList = rankList.clone();
	}
	public void setAssigned(Person assigned) {this.assigned = assigned;}
	public void setRank(Person p, int rank) {this.rankList[p.idIndex] = rank;}
	public void mark() {mark = true;}
	public void unmark() {mark = false;}
	public void setCurrentPrefIndex(int currentPrefIndex) {
		this.currentPrefIndex = currentPrefIndex;
	}
	

	/**
	 * <p> Utilities. </p>
	 */
	public String toString() {
		String s = "" + id + ":";
		for (Person p : preferenceList) {
			if (p == null) {
				s += " n";
			}
			else {
				s += " " + p.getId();
			}
		}
		s = s + "  saved: ";
		for (Person prefElem : noChangesPreferenceList) {
			s += " " + prefElem.getId();
		}
		s = s + "  rank: ";
		for (int i : rankList) {
			s += " " + i;
		}
		s = s + "  ranksaved: ";
		for (int i : noChangesRankList) {
			s += " " + i;
		}
		return s;
	}
}
