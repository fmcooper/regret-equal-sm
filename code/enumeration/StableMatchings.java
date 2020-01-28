package code.enumeration;
import code.shared.*;
import java.util.ArrayList;
import java.util.*;
import java.math.*;

/**
 * <p> This class holds the set of all stable matchings. </p>
 *
 * @author Frances
 */
public class StableMatchings {   
	/** <p> List of all stable matchings of the instance. </p>*/
	private ArrayList<Matching> stableMatchings;
	/** <p> Model for this instance. </p>*/
	private Model model;

	/** <p> Indexes of Optimal Stable Matchings. </p>*/
	private int opt_rankMaximal;
	private int opt_generous;
	private int opt_sexEqual;
	private int opt_egalitarian;
	private int opt_generalisedMedian;
	private int opt_balanced;
	private int opt_minRegret;
	private int opt_regretEqual;
	private int opt_minSumRegret;


	/** 
	* <p> Constructor. </p> 
	* @param model 		model of this instance
	*/
	public StableMatchings(Model model) {
		this.model = model;

		stableMatchings = new ArrayList<Matching>();
		opt_rankMaximal = -1;
		opt_generous = -1;
		opt_sexEqual = -1;
		opt_egalitarian = -1;
		opt_generalisedMedian = -1;
		opt_balanced = -1;
		opt_minRegret = -1;
		opt_regretEqual = -1;
		opt_minSumRegret = -1;
	}


	/** 
	* <p> Adds the given matching to the list of stable matchings. </p> 
	* @param matching 		matching to add
	*/
	public void add(Matching matching) {
		stableMatchings.add(matching);
	}


	/** 
	* <p> Assigns indices for optimal stable matchings. </p> 
	*/
	public void findStableOptimals() {
		int bestRmSoFar = 0;
		int bestGenSoFar = 0;
		int bestSexEqSoFar = 0;
		int bestEgalSoFar = 0;
		int bestRegEqSoFar = 0;
		int bestMinRegSoFar = 0;
		int bestBalancedSoFar = 0;
		int bestMinSumRegSoFar = 0;
		for (int i = 1; i < stableMatchings.size(); i++) {
			if (compareRM(
				stableMatchings.get(i), stableMatchings.get(bestRmSoFar))) {
				bestRmSoFar = i;
			}
			if (compareGen(
				stableMatchings.get(i), stableMatchings.get(bestGenSoFar))) {
				bestGenSoFar = i;
			}
			if (compareSexEq(
				stableMatchings.get(i), stableMatchings.get(bestSexEqSoFar))) {
				bestSexEqSoFar = i;
			}
			if (compareEgal(
				stableMatchings.get(i), stableMatchings.get(bestEgalSoFar))) {
				bestEgalSoFar = i;
			}
			if (compareRegEq(
				stableMatchings.get(i), stableMatchings.get(bestRegEqSoFar))) {
				bestRegEqSoFar = i;
			}
			if (compareMinReg(
				stableMatchings.get(i), stableMatchings.get(bestMinRegSoFar))) {
				bestMinRegSoFar = i;
			}
			if (compareBalanced(
				stableMatchings.get(i), stableMatchings.get(
					bestBalancedSoFar))) {
				bestBalancedSoFar = i;
			}
			if (compareMinSumRegret(
				stableMatchings.get(i), stableMatchings.get(
					bestMinSumRegSoFar))) {
				bestMinSumRegSoFar = i;
			}
		}
		opt_rankMaximal = bestRmSoFar;
		opt_generous = bestGenSoFar;
		opt_sexEqual = bestSexEqSoFar;
		opt_egalitarian = bestEgalSoFar;
		opt_generalisedMedian = getMedian(stableMatchings);
		opt_balanced = bestBalancedSoFar;
		opt_minRegret = bestMinRegSoFar;
		opt_regretEqual = bestRegEqSoFar;
		opt_minSumRegret = bestMinSumRegSoFar;
	}


	/**
	* <p> Compare two matchings rank-maximally and returns true if 1st dominates
	* 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	* @return true if 1st dominates 2nd
	*/
	public boolean compareRM(Matching first, Matching second) {
		int[] firstProfile = first.getProfile();
		int[] secondProfile = second.getProfile();
		for (int i = 0; i < firstProfile.length; i++) {
			if (firstProfile[i] > secondProfile[i]) {
				return true;
			}
			else if (firstProfile[i] < secondProfile[i]) {
				return false;
			}
		}
		return false;
	}


	/**
	* <p> Compare two matchings generous-ly and returns true if 1st dominates
	* 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	* @return true if 1st dominates 2nd
	*/
	public boolean compareGen(Matching first, Matching second) {
		int[] firstProfile = first.getProfile();
		int[] secondProfile = second.getProfile();
		for (int i = firstProfile.length - 1; i >= 0; i--) {
			if (firstProfile[i] < secondProfile[i]) {
				return true;
			}
			else if (firstProfile[i] > secondProfile[i]) {
				return false;
			}
		}
		return false;
	}


	/**
	* <p> Compare two matchings in a 'sex equal' way and returns true if 1st
	* dominates 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	* @return true if 1st dominates 2nd
	*/
	public boolean compareSexEq(Matching first, Matching second) {
		if (first.getSexEqualCost() < second.getSexEqualCost()) {
			return true;
		}
		return false;
	}


	/**
	* <p> Compare two matchings by their egalitarian weight function and returns
	* true if 1st dominates 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	*
	* @return true if 1st dominates 2nd
	*/
	public boolean compareEgal(Matching first, Matching second) {
		if (first.getCost() < second.getCost()) {
			return true;
		}
		return false;
	}


	/**
	* <p> Compare two matchings regret-equally and returns true if 1st dominates
	* 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	*
	* @return true if 1st dominates 2nd
	*/
	public boolean compareRegEq(Matching first, Matching second) {
		if (first.getDegreeDiff() < second.getDegreeDiff()) {
			return true;
		}
		return false;
	}


	/**
	* <p> Compare two matchings by balanced score and returns true if 1st 
	* dominates 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	*
	* @return true if 1st dominates 2nd
	*/
	public boolean compareBalanced(Matching first, Matching second) {
		if (Math.max(first.getCostMen(), first.getCostWomen()) 
			< Math.max(second.getCostMen(), second.getCostWomen())) {
			return true;
		}
		return false;
	}


	/**
	* <p> Compare two matchings in a minimum regret way and returns true if 1st
	* dominates 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	*
	* @return true if 1st dominates 2nd
	*/
	public boolean compareMinReg(Matching first, Matching second) {
		if (first.getDegree() < second.getDegree()) {
			return true;
		}
		return false;
	}


	/**
	* <p> Compare two matchings by the min sum regret and returns true if 1st
	* dominates 2nd. </p>
	* <p> Both matching arrays must be of the same length. </p>
	* @param first matching
	* @param second matching
	*
	* @return true if 1st dominates 2nd
	*/
	public boolean compareMinSumRegret(Matching first, Matching second) {
		if (first.getDegreeMen() + first.getDegreeWomen() 
			< second.getDegreeMen() + second.getDegreeWomen()) {
			return true;
		}
		return false;
	}


	/**
	* <p> Returns the stable matching index of the median. </p>
	* @param all stable matchings
	* @return index of the median
	*/
	public int getMedian(ArrayList<Matching> stableMatchings) {
		int numMen = model.getNumProposers();

		// counts of woman w_j on man m_i's lists
		int[][] counts = new int[numMen][numMen];
		for (Matching sm : stableMatchings) {
			for (int i = 0; i < sm.getMatching().length; i++) {
				counts[i][sm.getMatching()[i]]++;	
			}
		}

		int[][] genMedTable = new int[numMen][stableMatchings.size()];
		int currentIndexInGenMedTable = 0;
		// iterate over the mens preferences adding the women from counts in
		// order of preference
		for (int man = 0; man < model.getNumProposers(); man++) {
			Person thisMan = model.getProposer(man);
			currentIndexInGenMedTable = 0;
			for (int womanIndex = 0; womanIndex < numMen; womanIndex++) {
				int womanForMan = thisMan.getPreferenceListPersonAt(
					womanIndex).getIdIndex();
				int countForThisWoman = counts[man][womanForMan];

				// add to the Generalized median table
				for (int i = 0; i < countForThisWoman; i++) {
					genMedTable[man][currentIndexInGenMedTable] = womanForMan;
					currentIndexInGenMedTable++;
				}
			}
		}

		// if there are an odd number of stable matchings then take the middle
		// one of the genMedTable
		int[] generalisedMed = new int[model.getNumProposers()];
		if (stableMatchings.size() % 2 == 1) {
			int middleIndex = (stableMatchings.size() / 2);
			for (int j = 0; j < generalisedMed.length; j++) {
				generalisedMed[j] = genMedTable[j][middleIndex];
			}
		}
		else {
			// taking the 'left' generalised median
			int middleIndex = (stableMatchings.size() / 2) - 1;	
			for (int j = 0; j < generalisedMed.length; j++) {
				generalisedMed[j] = genMedTable[j][middleIndex];
			}
		}
		return getStableMatchingIndex(generalisedMed);
	}


	/**
	* <p> Returns the stable matching index of the given matching. </p>
	* @param matching
	* @return index of the given matching
	*/
	public int getStableMatchingIndex(int[] matching) {
		for (int i = 0; i < stableMatchings.size(); i++) {
			Matching stableMatching = stableMatchings.get(i);
			boolean found = true;
			for (int j = 0; j < stableMatching.getMatching().length; j++) {
				if (stableMatching.getMatching()[j] != matching[j]) {
					found = false; 	// could optimise this - use a while loop
				}
			}
			if (found == true) {
				return i;
			}
		}
		return -1;
	}


	/**
	* <p> Getters. </p>
	*/
	public int getRankMaximal() {return opt_rankMaximal;}
	public int getGenerous() {return opt_generous;}
	public int getSexEqual() {return opt_sexEqual;}
	public int getEgalitarian() {return opt_egalitarian;}
	public int getGeneralisedMedian() {return opt_generalisedMedian;}
	public int getBalanced() {return opt_balanced;}
	public int getMinRegret() {return opt_minRegret;}
	public int getRegretEqual() {return opt_regretEqual;}
	public int getMinSumRegret() {return opt_minSumRegret;}
	public int getNum() {return stableMatchings.size();}


	/**
	* <p> Returns string of all stable matching results. </p>
	*/
	public String getResults() {
		String s = "// Stable matchings\n\n";
		s += getOptimalResults();
		s += getMatchingResults();
		return s;
	}


	/**
	* <p> Returns string of optimal results. </p>
	*/
	public String getOptimalResults() {
		String s = "optimal_stable_matchings: \n";
		s += "rank-maximal_index: " + opt_rankMaximal + "\n";
		s += "generous_index: " + opt_generous + "\n";
		s += "sex_equal_index: " + opt_sexEqual + "\n";
		s += "egalitarian_index: " + opt_egalitarian + "\n";
		s += "generalisedMedian_index: " + opt_generalisedMedian + "\n";
		s += "balanced_index: " + opt_balanced + "\n";
		s += "minimumRegret_index: " + opt_minRegret + "\n";
		s += "minimumSumRegret_index: " + opt_minSumRegret + "\n";
		s += "regretEqual_index: " + opt_regretEqual + "\n";

		Matching regEqMatching = stableMatchings.get(opt_regretEqual);
		s += "regretEqualOptValue: " + regEqMatching.getDegreeDiff() + "\n\n";
		return s;
	}


	/**
	* <p> Returns all matchings as string. </p>
	*/
	public String getMatchingResults() {
		String s = "stable_matching_list:\n";
		for (Matching sm : stableMatchings) {
			s += sm.stringOfArray(sm.getMatching(), true) + "\n";
		}

		s += "\n\n";
		for (int i = 0; i < stableMatchings.size(); i++) {
			Matching matching = stableMatchings.get(i);
			s += matching.getString(i) + "\n --- \n";
		}
		return s;
	}
}
