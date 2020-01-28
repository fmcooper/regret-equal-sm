package code.shared;
import java.util.ArrayList;

/**
 * <p> This class represents the matching in an SMI instance. </p>
 * <p> Only used to store matchings found via the Enumeration Algorithm since 
 * all properties are output to file in this case. </p>
 *
 * @author Frances
 */
public class Matching {  
	/** <p> Matching represented by receiver indices. </p>*/ 
	private int[] matching;
	/** <p> Proposer profile. </p>*/ 
	private int[] profileMen;
	/** <p> Receiver profile. </p>*/ 
	private int[] profileWomen;
	/** <p> Combined profile. </p>*/ 
	private int[] profile;
	/** <p> Egalitarian cost for proposers. </p>*/ 
	private int costMen;
	/** <p> Egalitarian cost for receivers. </p>*/ 
	private int costWomen;
	/** <p> Combined egalitarian cost. </p>*/ 
	private int cost;
	/** <p> Combined degree. </p>*/ 
	private int degree;
	/** <p> Degree from men's side. </p>*/ 
	private int degreeMen;
	/** <p> Degree from women's side. </p>*/ 
	private int degreeWomen;
	/** <p> Difference in degree between men and women. </p>*/ 
	private int degreeDiff;


	/**
	 * <p> Matching constructor. </p>
	 *
	 * @param matching
	 * @param profileMen
	 * @param profileWomen
	 * @param profile
	 * @param costMen
	 * @param costWomen
	 * @param cost
	 */
	public Matching(
		int[] matching, 
		int[] profileMen, 
		int[] profileWomen, 
		int[] profile, 
		int costMen, 
		int costWomen, 
		int cost) {

		this.matching = matching;
		this.profileMen = profileMen;
		this.profileWomen = profileWomen;
		this.profile = profile;
		this.costMen = costMen;
		this.costWomen = costWomen;
		this.cost = cost;
		degreeMen = calcDegree(profileMen);
		degreeWomen = calcDegree(profileWomen);
		if (degreeMen > degreeWomen) {
			degree = degreeMen;
			degreeDiff = degreeMen - degreeWomen;
		}
		else {
			degree = degreeWomen;
			degreeDiff = degreeWomen - degreeMen;
		}
	}


	/**
	 * <p> Calculates degree when given a profile. </p>
	 * @param profile
	 * @return degree
	 */
	private int calcDegree(int[] profile) {
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
	 * <p> Getters and setters. </p>
	 */
	public int[] getMatching() {return matching;}
	public int[] getProfileMen() {return profileMen;}
	public int[] getProfileWomen() {return profileWomen;}
	public int[] getProfile() {return profile;}
	public int getCostMen() {return costMen;}
	public int getCostWomen() {return costWomen;}
	public int getCost() {return cost;}
	public int getDegree() {return degree;}
	public int getDegreeMen() {return degreeMen;}
	public int getDegreeWomen() {return degreeWomen;}
	public int getDegreeDiff() {return degreeDiff;}
	public int getSexEqualCost() {
		if (costMen > costWomen) {
			return costMen - costWomen;
		}
		return costWomen - costMen;
	}
	public int getBalancedScore() {
		if (costMen > costWomen) {
			return costMen;
		}
		return costWomen;
	}

	public void setCostMen(int costMen) {this.costMen = costMen;}
	public void setCostWomen(int costWomen) {this.costWomen = costWomen;}
	public void setCost(int cost) {this.costMen = cost;}


	/**
	 * <p> Output. </p>
	 */
	public String getString(int i) {
		String s = "";
		s += "matching_" + i + ": " + stringOfArray(matching, true) + "\n";
		s += "profileMen_" + i + ": " + stringOfArray(profileMen, false) + "\n";
		s += "profileWomen_" + i + ": " + stringOfArray(profileWomen, false) 
		+ "\n";
		s += "profileCombined_" + i + ": " + stringOfArray(profile, false) 
		+ "\n";
		s += "costMen_" + i + ": " + costMen + "\n";
		s += "costWomen_" + i + ": " + costWomen + "\n";
		s += "egalitarianCost_" + i + ": " + cost + "\n";
		s += "sexEqualityCost_" + i + ": " + getSexEqualCost() + "\n";
		s += "menDegree_" + i + ": " + degreeMen + "\n";
		s += "womenDegree_" + i + ": " + degreeWomen + "\n";
		s += "degree_" + i + ": " + degree + "\n";
		s += "regretEqualScore_" + i + ": " + degreeDiff + "\n";
		s += "balancedScore_" + i + ": " + getBalancedScore() + "\n";
		int sumRegret = degreeMen + degreeWomen;
		s += "sumRegret_" + i + ": " + sumRegret + "\n";
		return s;
	}


	/**
	 * <p> Output. </p>
	 */
	public String getReducedString() {
		String s = "";
		s += "matching: " + stringOfArray(matching, true) + "\n";
		s += "profileMen: " + stringOfArray(profileMen, false) + "\n";
		s += "profileWomen: " + stringOfArray(profileWomen, false) 
		+ "\n";
		s += "profileCombined: " + stringOfArray(profile, false) 
		+ "\n";
		s += "costMen: " + costMen + "\n";
		s += "costWomen: " + costWomen + "\n";
		s += "egalitarianCost: " + cost + "\n";
		s += "sexEqualityCost: " + getSexEqualCost() + "\n";
		s += "menDegree: " + degreeMen + "\n";
		s += "womenDegree: " + degreeWomen + "\n";
		s += "degree: " + degree + "\n";
		s += "regretEqualScore: " + degreeDiff + "\n";
		s += "balancedScore: " + getBalancedScore() + "\n";
		int sumRegret = degreeMen + degreeWomen;
		s += "sumRegret: " + sumRegret + "\n";
		return s;
	}


	/**
	 * <p> Return the string of an Array. </p>
	 * @param intArray
	 * @param message
	 * @param adding1
	 */
	public String stringOfArray(int[] intArray, boolean adding1) {
		String s = "";

		for (int cell : intArray) {
			int val = cell;
			if (adding1) {
				val++;
			}
			s += val + " ";  
		}
		return s;
	}
}
