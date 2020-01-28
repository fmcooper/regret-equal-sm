package code.correctness;
import code.shared.*;
import java.io.*;
import java.util.*;
import java.math.*;


/**
 *	<p>Tests the stability of an SM instance result (depending on tag may also 
 * test that all stable matchings are found using an IP). </p>
 *
 * @author Frances
 */

public class Main_Tester {
	/**<p>The model of the instance.</p>*/
	private static Model model;
	private static ArrayList<int[]> stableMatchings;


	/**
	* <p> Help message. </p>
	*/
	public static void helpAndExit() {
		String message = "\nThis class tests that the regret-equal degree "
		+ "iteration algorithm, regret-equal stable pair algorithm and the "
		+ "enumeration algorithm all produce the same regret-equal score. "
		+ "It also tests that the regret-equal degree iteration algorithm "
		+ "produces a viable stable matching and that all stable matchings "
		+ "found by the enumeration algorithm are stable. "
		+ "If a true tag is given then will also check that all stable "
		+ "matchings are found by the enumeration algorithm using an IP."
		+ "\n\nUsing this program:"
		+ "$ java Main_Tester [-h] <instance file name> "
		+ "<degree iteration result filename> <stable pair result filename>"
		+ "<enumeration result filename> <true/false (IP tag)>\n";
		System.out.println(message);
		System.exit(0);
	}


	/**
	* <p> Main method. </p>
	*/
	public static void main(String args[]) {
		// input
		String instanceFilename = "";
		String rediResultFilename = "";
		String respResultFilename = "";
		String enumResultFilename = "";
		File instanceFile = null;
		File rediResultFile = null;
		File respResultFile = null;
		File enumResultFile = null;
		boolean bruteForce = false;
		try {
			instanceFilename = args[0];
			rediResultFilename = args[1];
			respResultFilename = args[2];
			enumResultFilename = args[3];
			String bfString = args[4];

			if (instanceFilename.equals("-h") || 
				rediResultFilename.equals("-h") || 
				respResultFilename.equals("-h") || 
				enumResultFilename.equals("-h") || 
				(!bfString.equals("true") && !bfString.equals("false"))) {
				helpAndExit();
			}
			else if (bfString.equals("true")) {
				bruteForce = true;
			}

			instanceFile = new File(instanceFilename);
			rediResultFile = new File(rediResultFilename);
			respResultFile = new File(respResultFilename);
			enumResultFile = new File(enumResultFilename);
		}
		catch (Exception e) {
			System.out.println("Input error");
			helpAndExit();
		}

		// create the model
		model = Util_FileIO.readFile(instanceFile);
		if (model == null) {
			System.exit(3);
		}

		// set the time and date instance variable in the model
		Util_FileIO.createCal();
		String easyResults = Util_FileIO.getCal(false) + "\n";
		model.timeAndDate = easyResults;
		System.out.println("Correctness_tests: " + easyResults);

		// re degree iteration algorithm results
		int rediRegEqScore = outputStandardAlgResults(
			rediResultFile,
			"degree_iteration",
			"REDI");
		
		// re stable pair algorithm results
		int respRegEqScore = outputStandardAlgResults(
			respResultFile,
			"stable_pair",
			"RESP");
		
		// enumeration algorithm results
		int enumRegEqScore = outputENUMresults(enumResultFile);
		
		// brute force
		outputBFresults(bruteForce);
		

		String generalResults = "\n# General results";
		// check regret equal scores match
		int[] reScores = new int[]{rediRegEqScore, respRegEqScore, 
			enumRegEqScore};
		int countTimeout = 0;
		int maxRE = -1;
		for (int score : reScores) {
			if (score == -1) {
				countTimeout = countTimeout + 1;
			}
			else if (score > maxRE) {
				maxRE = score;
			}
		}
		// if at least 2 timed out then nothing to compare so output as a
		// timeout
		if (countTimeout >= 2) {
			generalResults += "\nREscoreMatch: timeout";
		}
		// if either the scores are equal at max score or timed out - output
		// true
		else {
			boolean reScoreMatch = ((rediRegEqScore == -1 || 
			rediRegEqScore == maxRE) && 
			(respRegEqScore == -1 || respRegEqScore == maxRE) && 
			(enumRegEqScore == -1 || enumRegEqScore == maxRE));

			generalResults += "\nREscoreMatch: " + reScoreMatch;
		}
		System.out.println(generalResults + "\n");
	}


	/**
	 * <p>Prints regret-equal algorithm results and returns the regret-equal
	 * score.</p>
	 * @param rediResultFile
	 * @return regret-equal score
	 */
	public static int outputStandardAlgResults(
		File algResultFile, 
		String name,
		String acronym){
		String results = "\n# Regret-equal " + name + " algorithm results";
		int[] regretEqualMatching = null;
		int regEqScore = -1;
		try{
			regretEqualMatching = Util_FileIO.inputRegEqStable(algResultFile);
			regEqScore = Util_FileIO.getRegEqScore(
				algResultFile, 
				"regretEqualScore");
		}
		catch (Exception e) {
			System.out.println("**error**");
			System.exit(1);
		}

		if (regretEqualMatching != null && regEqScore != -1) {
			// checking the regret-equal stable matching
			boolean regEqAcc = false;
			boolean regEqCap = false;
			boolean regEqStable = false;
			model.setProposerAssignments(regretEqualMatching);
			model.setAssignmentInfo();
			if (checkAcceptability(model)) {
				regEqAcc = true;
			}
			if (checkCapacity(model)) {
				regEqCap = true;
			}
			if (checkStable(model)) {
				regEqStable = true;
			}
			
			boolean algPass = regEqAcc && regEqCap && regEqStable;

			results += "\n" + acronym + "_Alg_passedAcceptability: " + regEqAcc
			+ "\n" + acronym + "_Alg_passedCapacity: " + regEqCap
			+ "\n" + acronym + "_Alg_passedStability: " + regEqStable
			+ "\n" + acronym + "_Alg_correctness_pass: " + algPass
			+ "\n" + acronym + "_Alg_REscore: " + regEqScore;
		}
		else {
			results += "\n" + acronym + "_Alg_timeout";
		}
		System.out.println(results);
		return regEqScore;
	}


	/**
	 * <p>Prints enumeration algorithm results and returns the regret-equal 
	 * score.</p>
	 * @param enumResultFile
	 * @return regret-equal score
	 */
	public static int outputENUMresults(File enumResultFile) {
		String enumResults = "\n# Enumeration algorithm results";
		int enumRegEqScore = -1;
		try{
			stableMatchings = Util_FileIO.inputAllStable(enumResultFile);
			enumRegEqScore = Util_FileIO.getRegEqScore(
				enumResultFile, 
				"regretEqualOptValue");
		}
		catch (Exception e) {
			System.out.println("**error**");
			System.exit(1);
		}

		if (stableMatchings != null && enumRegEqScore != -1) {
			// for each potential stable matching perform correctness tests
			int numPassedAcceptability = 0;
			int numPassedCapacity = 0;
			int numPassedStability = 0;

			// checking all stable matchings found
			for (int smInd = 0; smInd < stableMatchings.size(); smInd++) {
				int[] sm = stableMatchings.get(smInd);
				model.setProposerAssignments(sm);
				model.setAssignmentInfo();

				if (checkAcceptability(model)) {
					numPassedAcceptability++;
				}
				if (checkCapacity(model)) {
					numPassedCapacity++;
				}
				if (checkStable(model)) {
					numPassedStability++;
				}
			}

			// check correctness and output
			enumResults += "\nENUM_Alg_numStableMatchings: " 
			+ stableMatchings.size()
			+ "\nENUM_Alg_numPassedAcceptability: " + numPassedAcceptability
			+ "\nENUM_Alg_numPassedCapacity: " + numPassedCapacity
			+ "\nENUM_Alg_numPassedStability: " + numPassedStability;

			boolean enumPass = 
			((stableMatchings.size() == numPassedAcceptability) &&
				(stableMatchings.size() == numPassedCapacity) &&
				(stableMatchings.size() == numPassedStability));
				
			enumResults += "\nENUM_Alg_correctness_pass: " + enumPass;
			enumResults += "\nENUM_Alg_REscore: " + enumRegEqScore;
		}
		else {
			enumResults += "\nENUM_Alg_timeout";
		}
		System.out.println(enumResults);
		return enumRegEqScore;
	}


	/**
	 * <p>Brute force.</p>
	 * @param model
	 * @return if all agents have acceptable partners
	 */
	public static void outputBFresults(boolean bruteForce) {
		// not implemented for this project
	}


	/**
	 * <p>Checks that the man and women pairs find each other acceptable.</p>
	 * @param model
	 * @return if all agents have acceptable partners
	 */
	public static boolean checkAcceptability(Model model) {
		for (int i = 0; i < model.getProposerAssignments().length; i++) {
			if (model.getProposerAssignments()[i] != -1) {
				int propInd = i;
				int recInd = model.getProposerAssignments()[i];
				if (model.getReceivers()[recInd].getRankList()[propInd] == -1) {
					return false;
				}
			}	
		}
		return true;
	}


	/**
	 * <p>Checks that the assignment adheres to upper and lower quotas.</p>
	 * @return if the assignment adheres to upper and lower quotas
	 */
	public static boolean checkCapacity(Model model) {
		boolean[] recAssigned = new boolean[model.getNumProposers()];
		for (int i = 0; i < model.getProposerAssignments().length; i++) {
			if (model.getProposerAssignments()[i] != -1) {
				if (recAssigned[model.getProposerAssignments()[i]] == true) {
					return false;
				}
				recAssigned[model.getProposerAssignments()[i]] = true;
			}
		}
		return true;
	}


	/**
	 * <p>Checks that the assignment is stable.</p>
	 * @return if the assignment is stable
	 */
	public static boolean checkStable(Model model) {
		// assume stable until proven otherwise
		boolean isStable = true;

		// for each man
		for (int propInd = 0; propInd < model.getProposerAssignments().length; propInd++) {
			// find the woman assignment
			int receiver = model.getProposerAssignments()[propInd];
			int[] propRankList = model.getProposers()[propInd].getRankList();
			int rank = -1;
			if (receiver != -1) {
				rank = propRankList[receiver];
			}

			// for all man woman project pairs in the mans preference list 
			// decide whether it is a blocking pair
			ArrayList<Person> propPrefList = model.getProposers(
				)[propInd].getPreferenceList();
			for (int prefInd = 0; prefInd < rank; prefInd++) {
				int recInd = propPrefList.get(prefInd).getIdIndex();
				if (model.getReceivers()[recInd].likes(
					model.getProposers()[propInd])) {
					isStable = false;
					// System.err.println("** blocking propInd: " + propInd + " recInd:" + recInd);
				}
			}
		}
		return isStable;
	}
}
