package code.shared;
import java.util.*;


/**
* <p> Creates rotation digraph from rotations. Max O(n^2). </p>
* <p> The Stable Marriage Problem - Gusfield and Irving (1989), pg 112. </p>
* 
* @author Frances
*/
public class DigraphCreator {
	/** <p> Model of the instance. </p>*/
	Model model;
	/** <p> Rotations of the instance. </p>*/
	Rotations rotations;
	/** <p> Digraph of the instance. </p>*/
	Digraph digraph;


	/**
	* <p> Constructor. </p>
	*/
	public DigraphCreator(Model model) {
		this.model = model;
		rotations = model.getRotations();
		digraph = model.getDigraph();
		run();
	}


	/** <p> Create the rotation digraph. </p> */
	public void run() {

		// create the type 1 and type 2 labels
		for (int i = 0; i < rotations.getNum(); i++) {
			Rotation r = rotations.get(i);
			for (int j = 0; j < r.getRotation().size(); j++) {
				Person[] couple = r.getRotation().get(j);
				Person man = couple[0];
				Person woman = couple[1];
				// Type 1: label w in m's preference list with the rotation 
				// number if (m,w) is in a rotation
				digraph.setMensListType1Label(
					man.getIdIndex(), 
					woman.getIdIndex(), 
					i + 1);

				// Type 2: label w in m's preference list with the rotation 
				// number if w moves from below m to above m in her list
				int womanIndex = woman.getIdIndex();
				int thisManRank = woman.getRank(man);
				Person nextMan = null;
				if (j == 0) {
					nextMan = r.getRotation().get(
						r.getRotation().size() - 1)[0];
				}
				else {
					nextMan = r.getRotation().get(j - 1)[0];
				}
				int nextManRank = woman.getRank(nextMan);

				// get each man between these ranks (often none) and label pairs
				// as type 2
				for (int rank = nextManRank + 1; rank < thisManRank; rank++) {
					if (model.getReceiver(womanIndex).getPreferenceListPersonAt(
						rank - 1) != null) {
						int intermmediateMan = model.getReceiver(
							womanIndex).getPreferenceListPersonAt(
							rank - 1).getIdIndex();
						digraph.setMensListType2Label(
							intermmediateMan, 
							womanIndex, 
							i + 1);
					}
				}
			}
		}

		digraph.instantiateDigraph(rotations.getNum());
		model.refreshAgentLists();

		// scan the type 1 and type 2 labels to create the digraph as described
		// in book
		for (int i = 0; i < model.getNumProposers(); i++) {
			int mostRecentType1rotation = -1;
			ArrayList<Person> currentPropPrefs = model.getProposer(
				i).getPreferenceList();
			
			for (int j = 0; j < currentPropPrefs.size(); j++) {
				int currentReciever = currentPropPrefs.get(j).getIdIndex();
				int type1 = digraph.getMensListType1Labels()[i][currentReciever];
				int type2 = digraph.getMensListType2Labels()[i][currentReciever];
				
				if (type1 != 0) {
					if (mostRecentType1rotation != -1) {
						digraph.addDigraphEdge(
							mostRecentType1rotation, 
							type1, 
							1);
					}
					mostRecentType1rotation = type1;
				}

				if (type2 != 0) {
					if (mostRecentType1rotation != -1) {
						digraph.addDigraphEdge(
							type2, 
							mostRecentType1rotation, 
							2);
					}
				}
			}
		}
		digraph.createSimpleDigraph();
	}
}
