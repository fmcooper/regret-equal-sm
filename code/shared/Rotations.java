package code.shared;
import java.util.ArrayList;

/**
 * <p> This class holds the set of rotations for the current instance. </p>
 *
 * @author Frances
 */
public class Rotations {   
	/** <p> List of rotations for this instance. </p>*/
	private ArrayList<Rotation> rotations;
	/** <p> Model for this instance. </p>*/
	private Model model;


	/** 
	* <p> Constructor. </p> 
	* @param model 		model of this instance
	*/
	public Rotations(Model model) {
		this.model = model;
		rotations = new ArrayList<Rotation>();
	}


	/** 
	* <p> Add a rotation to the list. </p> 
	* @param rotation 		rotation to add
	*/
	public void add(Rotation r) {
		rotations.add(r);
	}


	/**
	 * <p> Refreshes rotation object pointers. </p>
	 * @param proposers
	 * @param receivers
	 */
	public void refresh(Person[] props, Person[] recs) {
		for (Rotation r : rotations) {
			r.refresh(props, recs);
		}
	}


	/**
	* <p> Returns the index of the rotation that creates a specified man-woman
	* pair. </p>
	* O(n^2) time since there are n^2 possible pairs. This could be sped up by
	* having a 2D array of men and women pointing to the rotation number if it
	* exists (constant time lookup). 
	* @param model 			model of the instance
	* @param man 			man of the man-woman pair
	* @param woman 			woman of the man-woman pair
	* @return i 			rotation index
	*/
	public int findRotationThatCreatesPair(
		Model model, 
		Person man, 
		Person woman) {

		for (int i = 0; i < rotations.size(); i++) {
			if (rotations.get(i).creates(man, woman)) {
				return i;
			}
		}
		return -1;
	}


	/**
	* <p> Returns the index of the rotation that contains a specified man-woman
	* pair. </p>
	* O(n^2) time since there are n^2 possible pairs. This could be sped up by
	* having a 2D array of men and women pointing to the rotation number if it
	* exists (constant time lookup). However this is unnecessary to stay within
	* the overall time complexity and may not speed things up in practice where
	* preference lists are short (due to the creation of the array). 
	* @param model 			model of the instance
	* @param man 			man of the man-woman pair
	* @param woman 			woman of the man-woman pair
	* @return i 			rotation index
	*/
	public int findRotationThatContainsPair(
		Model model, 
		Person man, 
		Person woman) {

		for (int i = 0; i < rotations.size(); i++) {
			if (rotations.get(i).contains(man, woman)) {
				return i;
			}
		}
		return -1;
	}


	/**
	* <p> Recursive method to eliminate the current rotation and its
	* predecessors in order defined by the rotation poset over the given
	* matching and returns the new matching. 
	* </p>
	* <p> Note that the number of vertices and edges in the digraph are bounded
	* by n^2 and so this operation takes a maximum of O(|V| + |E|) = O(n^2)
	* time. </p>
	* <p> If this method is called repeatedly for DIFFERENT vertices
	* (rotations) then the total time taken is still O(n^2) since each edge is
	* traversed a maximum of one time (eliminated vertices are not explored).
	* </p>
	* @param model 			model of the instance
	* @param matching 		matching to eliminate the rotation on
	* @param rotationIndex 	index of rotation in question
	* @param q 				rotations already eliminated
	* @return matching 		resultant matching after rotations are eliminated
	*/
	public int[] rotate(
		Model model, 
		int[] matching, 
		int rotationIndex, 
		boolean[] q) {

		Rotation r = rotations.get(rotationIndex);
		
		// ensure all parents of the current rotation are eliminated
		ArrayList<Integer> thisRotPreds = model.getDigraph(
			).getSimpleReverseDigraph().get(rotationIndex);

		for (int i : thisRotPreds) {
			if (!q[i]) {
				rotate(model, matching, i, q);
			}
		}
		
		if (!q[rotationIndex]) {
			q[rotationIndex] = true;
			matching = r.rotate(matching);
		}
		

		return matching;	
	}


	/**
	* <p> Getters. </p>
	*/
	public Rotation get(int index) {return rotations.get(index);}
	public int getNum() {return rotations.size();}


	/**
	* <p> Returns string of all rotation results. </p>
	*/
	public String getResults() {
		String s = "\n\n// Rotations\n";
		for (int i = 0; i < rotations.size(); i++) {
			Rotation r = rotations.get(i);
			s += r.getResults(i, model.getNumProposers()) + "\n";
		}
		return s;
	}
}
