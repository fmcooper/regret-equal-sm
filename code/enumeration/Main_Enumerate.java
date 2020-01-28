package code.enumeration;
import code.shared.*;
import java.io.*;
import java.util.*;
import java.math.*;

/**
*	Finds all stable matchings for a given complete instance of SMI. </p>
*
* @author Frances
*/

public class Main_Enumerate {

	/**
	* <p> Help message. </p>
	*/
	public static void helpAndExit() {
		String message = "\nThis class finds all stable matchings for a given "
		+ "instance.\n\n"
		+ "Using this program:\n"
		+ "$ java Main_GetRegretEqual [-h] <inst file name>";
		System.out.println(message);
		System.exit(0);
	}


	/**
	* <p> Main method. Runs a single instance. </p>
	*/
	public static void main(String args[]) {
		// Start time in milliseconds
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
		EnumerationAlgorithm ea = new EnumerationAlgorithm(model);
		BigInteger endAlg = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTakenAlg = endAlg.subtract(startAlg);

		// output results
		System.out.println(ea.getResults());
		System.out.println("Duration_ModCreation_ms: " + timeTakenMod);
		System.out.println("Duration_Alg_ms: " + timeTakenAlg);

		BigInteger endTot = new BigInteger("" + System.currentTimeMillis());
		BigInteger timeTot = endTot.subtract(startTot);
		System.out.println("Duration_Total_milliseconds: " + timeTot + "\n");
	}	
}
