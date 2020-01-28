package code.stable_pair;
import code.shared.*;
import java.io.*;
import java.util.*;
import java.math.*;

/**
*	Finds the regret-equal stable matching using the Regret-Equal Stable Pair
* Algorithm for a single instance. </p>
*
* @author Frances
*/

public class Main_ReStablePairAlgorithm {

	/**
	* <p> Help message. </p>
	*/
	public static void helpAndExit() {
		String message = "\nThis class finds the regret-equal stable matching "
		+ "using the Regret-Equal Stable Pair Algorithm for a single "
		+ "instance.\n\n"
		+ "Using this program:\n"
		+ "$ java Main_ReStablePairAlgorithm [-h] <instance file name>";
		System.out.println(message);
		System.exit(0);
	}
	

	/**
	* <p> Main method. Runs a single instance. </p>
	*/
	public static void main(String args[]) {
		/* <p>Start time in milliseconds.</p> */
		BigInteger startTot = new BigInteger("" + System.currentTimeMillis());

		// input checks
		String originalFileName = "";
		File originalFile = null;
		try {
			originalFileName = args[0];

			if (originalFileName.equals("-h")) {
				helpAndExit();
			}
			originalFile = new File(originalFileName);
		}
		catch (Exception e) {
			System.out.println("Input name error");
			helpAndExit();
		}

		// creating the model
		BigInteger startMod = new BigInteger("" + System.currentTimeMillis()); 
		Model model = Util_FileIO.readFile(originalFile);

		if (model == null) {
			System.out.println("** error: the file " + originalFile.getName() 
				+ " is incompatable");
			helpAndExit();
		}

		Util_FileIO.createCal();
		String easyResults = Util_FileIO.getCal(false) + "\n";
		model.timeAndDate = easyResults;
		BigInteger endMod = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenMod = endMod.subtract(startMod);

		// run the regret equal algorithm recording start and end times
		BigInteger startAlg = new BigInteger("" + System.currentTimeMillis());
		ReStablePairAlgorithm reAlg = new ReStablePairAlgorithm(model);
		BigInteger endAlg = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenAlg = endAlg.subtract(startAlg);

		// output results
		System.out.println(reAlg.getResults(model));
		System.out.println("Duration_ModCreation_ms: " + timeTakenMod);
		System.out.println("Duration_Alg_ms: " + timeTakenAlg);

		BigInteger endTot = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenTot = endTot.subtract(startTot);
		System.out.println("Duration_Total_ms: " + timeTakenTot + "\n");
	}	
}
