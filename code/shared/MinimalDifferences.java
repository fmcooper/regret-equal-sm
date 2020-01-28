package code.shared;
import java.util.*;


/**
* <p> The minimal differences algorithm to list all rotations of an instance. 
* </p><p> The Stable Marriage Problem - Gusfield and Irving (1989), pg 110.
* O(n^2) time. </p>
* 
* @author Frances
*/
public class MinimalDifferences {
	/** <p> Model of the instance. </p>*/
	Model model;
	/** <p> Proposer (man) optimal stable matching. </p>*/
	int[] matchingM0;
	/** <p> Receiver (woman) optimal stable matching. </p>*/
	int[] matchingMz;


	/**
	* <p> Constructor. </p>
	* @param first model
	* @param woman optimal stable matching
	*/
	public MinimalDifferences(Model model, int[] womanOpt) {
		this.model = model;
		matchingM0 = model.getProposerAssignments();
		matchingMz = womanOpt;
		run();
	}


	/** <p> Minimal-Differences algorithm. </p> */
	public void run() {
		// creating a stack that will hold men related to a particular rotation
		Stack<Person> st = new Stack<Person>();
		int x = 0; // the current man number
		int numProp = model.getNumProposers();
		int[] matchingMi = new int[numProp]; // the current matching
		for (int index = 0; index < numProp; index++) {
			matchingMi[index] = matchingM0[index];
		}

		// main while loop
		while (x < numProp) {

			// if the stack is empty
			if (st.isEmpty()) {
				// find a man which has a different assignment in M0 and Mz
				while((x < numProp) && (matchingMi[x] == matchingMz[x])) {
					x = x + 1;
				}
				// if we have a man who has a different assignment then push
				// them onto the stack
				if (x < numProp) {
					Person man = model.getProposer(x);
					man.mark();
					st.push(man);
				}
			}

			// if the stack is not empty
			if (!st.isEmpty()) {
				Person firstMan = st.peek();

				// defns on pg 87 of book
				// s is the first woman on m's list such that w strictly prefers
				// m to their partner in Mi
				Person s = getS(firstMan, matchingMi);
				
				// get the man that s is currently assigned to
				Person m = getAssignedForWoman(s, matchingMi);

				// get all the men who will be involved in this rotation
				while (m.getMark() == false) {
					st.push(m);
					m.mark();
					s = getS(m, matchingMi);

					if (s != null) {
						m = getAssignedForWoman(s, matchingMi);
					}	
				}
				
				// get the first pair of the rotation
				Person topMan = st.pop();
				topMan.unmark();
				Rotation r = new Rotation();
				r.add(topMan, getAssignedForMan(topMan, matchingMi));

				// get the rest of the rotating pairs
				while (topMan != m) {
					topMan = st.pop();
					topMan.unmark();
					r.add(topMan, getAssignedForMan(topMan, matchingMi));
				}

				// rotate the current matching by the found rotation
				matchingMi = r.rotate(matchingMi);
				model.getRotations().add(r);
				// update the preference lists after this rotation
				r.updatePrefs();
			}
		}
	}

		/** 
	* <p> Retrieves the man assigned to the first woman on m's list such that w
	* strictly prefers m to their partner in Mi. </p> 
	* @param man
	* @param matchingMi
	*
	* @return man
	*/
	public Person getNextM(Person man, int[] matchingMi) {
		Person womanS = getS(man, matchingMi);
		if (womanS == null) {
			System.out.println("womanS: null!");
		}
		for (int i = 0; i < matchingMi.length; i++) {
			if (matchingMi[i] == womanS.getIdIndex()) {
				return model.getProposers()[i];
			}
		}
		return null;
	} 


	/** 
	* <p> Retrieves the first woman on m's list such that w strictly prefers m
	* to their partner in Mi. </p> 
	* @param man
	* @param matchingMi
	*
	* @return woman
	*/
	public Person getS(Person man, int[] matchingMi) {
		boolean passedAssignedWoman = false;
		int index = 0;
		ArrayList<Person> prefList = man.getPreferenceList();
		while(index < prefList.size()) {
			Person currentWoman = prefList.get(index);

			if (currentWoman != null) {
				// check if current woman would prefer to be with this man than
				// with their current partner
				if (passedAssignedWoman) {
					if (currentWoman.getRank(
						getAssignedForWoman(currentWoman,matchingMi)) 
						> currentWoman.getRank(man)) {
						return currentWoman;
					}
				}
				// check if have passed the current assigned woman
				// shallow compare
				if (currentWoman == getAssignedForMan(man, matchingMi)) { 
					passedAssignedWoman = true;
				}
			}
			index++;
		}
		return null;
	}


	/** 
	* <p> Returns the assignment for a given woman in the given matching. </p> 
	* @param w
	* @param matchingMi
	*
	* @return man assigned to the given woman
	*/
	public Person getAssignedForWoman(Person w, int[] matchingMi) {
		for (int i = 0; i < matchingMi.length; i++) {
			if (matchingMi[i] == w.getIdIndex()) {
				return model.getProposers()[i];
			}
		}
		return null;
	}


	/** 
	* <p> Returns the assignment for a given man in the given matching. </p> 
	* @param m
	* @param matchingMi
	*
	* @return woman assigned to the given man
	*/
	public Person getAssignedForMan(Person m, int[] matchingMi) {
		return model.getReceivers()[matchingMi[m.getIdIndex()]];
	}
}
