package code.shared;
import java.io.*;
import java.util.*;
import java.util.ArrayList;

/**
* <p> This class contains functions to read an SMI instance from a file and
* create a Model instance, along with other IO functions. </p>
*
* @author Frances
*/
public abstract class Util_FileIO {
	private static Calendar calendar;
	private static int maxPrefLength;

	// cannot be instantiated - this is a utility class
	private Util_FileIO() {
	}


	/**
	* <p> Input the instance from the file and create a Model. </p>
	* @param file 		the file to input data from
	* @return the created Model instance
	*/
	public static Model readFile(File file) {
		int numProposers = 0;
		int[][] proposersPrefs = new int[1][1];
		int[][] receiversPrefs = new int[1][1];

		FileReader fr = null;
		try {
			try {			
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);
				// input the number of men
				numProposers = scan.nextInt();
				scan.nextLine();
				proposersPrefs = new int[numProposers][];
				receiversPrefs = new int[numProposers][];

				
				// input men preferences
				for (int i = 0; i < numProposers; i++) {
					String prefListString = scan.nextLine();
					String[] prefSplit = prefListString.split("[ :]+");
					int[] prefList = new int[prefSplit.length - 1];

					for (int j = 1; j < prefSplit.length; j++) {		
						int index = j - 1;	
						prefList[index] = (Integer.parseInt(prefSplit[j]) - 1);
					}
					proposersPrefs[i] = prefList;
				}

				// input women preferences
				for (int i = 0; i < numProposers; i++) {
					String prefListString = scan.nextLine();
					String[] prefSplit = prefListString.split("[ :]+");
					int[] prefList = new int[prefSplit.length - 1];

					for (int j = 1; j < prefSplit.length; j++) {		
						int index = j - 1;	
						prefList[index] = (Integer.parseInt(prefSplit[j]) - 1);
					}
					receiversPrefs[i] = prefList;
				}	

				Model model = new Model(
					numProposers, 
					proposersPrefs, 
					receiversPrefs);
				return model;
			}
			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {
			// error message if problem with input file
			// suppressed since error is handled as an exit code
			//System.out.println("Error in reading from file: " + file.toString());
		}
		return null;
	}


	/**
	* <p> Input all stable matchings from a file. </p>
	* @param file 		the file to input results from
	* @return stable matchings
	*/
	public static ArrayList<int[]> inputAllStable(File file) {
		FileReader fr = null;
		ArrayList<int[]> matchings = null;
		String resultsline = "";
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);

				boolean acceptableContents = false;
				boolean finishedScanning = false;
				while (!finishedScanning) {
					if (scan.hasNextLine()) {
						String line = scan.nextLine();
						if (line.contains("timeout")) {
							acceptableContents = true;
						}
						if (line.equals("stable_matching_list:")) {
							acceptableContents = true;
							boolean finishedSMs = false;
							matchings = new ArrayList<int[]>();
							while (!finishedSMs) {
								resultsline = scan.nextLine();
								if (resultsline.equals("")) {
									finishedSMs = true;
									finishedScanning = true;
								}
								else {
									String [] resultslineSplit = resultsline.split(" ");
									int[] result = new int [resultslineSplit.length];

									for (int i = 0; i < resultslineSplit.length; i++) {
										int receiver = Integer.parseInt(resultslineSplit[i]);
										receiver--;
										result[i] = receiver;
									}
									matchings.add(result);
								}
							}
						}
					}
					else {
						break;
					}
				}
				if (!acceptableContents) {
					throw new Exception("no acceptable contents in file");
				}
			}

			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {

			// error message if problem with input file
			System.out.println("Error from file: " + file.toString());
		}

		return matchings;
	}


	/**
	* <p> Input the regret equal stable matching from a file. </p>
	* @param file 		the file to input results from
	* @return regret equal stable matching
	*/
	public static int[] inputRegEqStable(File file) {
		FileReader fr = null;
		int[] regEq = null;
		String resultsline = "";
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);

				boolean finishedScanning = false;
				boolean hasAcceptableContents = false;
				while (!finishedScanning) {
					if (scan.hasNextLine()) {
						String line = scan.nextLine();
						if (line.contains("timeout")) {
							hasAcceptableContents = true;
						}
						String[] resultslineSplit = line.split(" ");
						if (resultslineSplit[0].contains("matching:")) {
							hasAcceptableContents = true;
							regEq = new int [resultslineSplit.length - 1];
							for (int i = 1; i < resultslineSplit.length; i++) {
								int receiver = Integer.parseInt(
									resultslineSplit[i]);
								receiver--;
								regEq[i - 1] = receiver;
							}
						}
					}
					else {
						break;
					}
				}
				if (!hasAcceptableContents) {
					throw new Exception("no acceptable contents in file");
				}
			}

			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {

			// error message if problem with input file
			System.out.println("Error from file: " + file.toString());
		}

		return regEq;
	}


	/**
	* <p> Input the regret equal scores from a file. </p>
	* @param file 		the file to input results from
	* @return regret equal score
	*/
	public static int getRegEqScore(File file, String tag) {
		FileReader fr = null;
		int regEqScore = -1;
		try {
			try {				
				// opens file and create a scanner on that file
				fr = new FileReader (file);
				Scanner scan = new Scanner(fr);

				boolean acceptableContents = false;
				boolean finishedScanning = false;
				while (!finishedScanning) {
					if (scan.hasNextLine()) {
						String line = scan.nextLine();
						if (line.contains("timeout")) {
							acceptableContents = true;
						}
						String[] resultslineSplit = line.split(" ");
						if (resultslineSplit[0].contains(tag)) {
							acceptableContents = true;
							regEqScore = Integer.parseInt(resultslineSplit[1]);
						}
					}
					else {
						break;
					}
				}
				if (!acceptableContents) {
					throw new Exception("no acceptable contents in file");
				}
			}

			// closes file if it was successfully opened
			finally {
				if (fr != null) {fr.close();}
			}
		}
		// catches IO exception
		catch (Exception e) {

			// error message if problem with input file
			System.out.println("Error from file: " + file.toString());
		}

		return regEqScore;
	}


	/**
	*<p>Create a calendar object.</p>
	*/
	public static void createCal() {
		calendar = new GregorianCalendar();
	}


	/**
	*<p>Return a short or long version of the calendar's time.</p>
	*/
	public static String getCal(boolean shortCal) {
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		if (shortCal) {
			return "" + year + "," + month + "," + day + "_" + hour + "," + minute + "," + second;
		}
		else {
			return "Date: " + year + "/" + month + "/" + day + "  Time: " + hour + ":" + minute + ":" + second;
		}	
	}


	/**
	 * <p> Print an int[][]. </p>
	 * @param intArray
	 * @param message
	 */
	public static void print(int[][] intArray, String message) {
		String s = message + "\n";

		for (int[] row : intArray) {
			for (int cell : row) {
				s += cell + " ";
			}
			s += "\n";
		}
		System.out.println(s);
	}


	/**
	 * <p> Print an int[][]. </p>
	 * @param intArray
	 * @param message
	 */
	public static void print(
		ArrayList<ArrayList<Integer>> array, 
		String message) {
		
		String s = message + "\n";

		for (ArrayList<Integer> row : array) {
			for (int cell : row) {
				s += cell + " ";
			}
			s += "\n";
		}
		System.out.println(s);
	}



	/**
	 * <p> Print an int[]. </p>
	 * @param intArray
	 * @param message
	 */
	public static void print(int[] intArray, String message) {
		String s = message + "\n";

		for (int cell : intArray) {
			s += cell + " ";  
		}
		s += "\n";
		System.out.println(s);
	}

		/**
	 * <p> Print an int[]. </p>
	 * @param intArray
	 * @param message
	 */
	public static void print(Integer[] intArray, String message) {
		String s = message + "\n";

		for (Integer cell : intArray) {
			s += cell + " ";  
		}
		s += "\n";
		System.out.println(s);
	}


	public static void print(boolean[] boolArray, String message) {
		String s = message + "\n";

		for (boolean cell : boolArray) {
			s += cell + " ";  
		}
		s += "\n";
		System.out.println(s);
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
