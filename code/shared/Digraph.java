package code.shared;
import java.util.ArrayList;
import java.util.*;
import java.math.*;

/**
 * <p> This class holds the set of all stable matchings. </p>
 *
 * @author Frances
 */
public class Digraph {   
	/** <p> 2D array of type 1 labels for women in mens lists (used to create
	* digraph) </p> */
	private int[][] mensListType1Labels;
	/** <p> 2D array of type 2 labels for women in mens lists (used to create
	* digraph) </p> */
	private int[][] mensListType2Labels;
	/** <p> Rotation Digraph in full, simplified and simplified reverse forms.
	* </p>*/
	private int[][][] digraph;
	private ArrayList<ArrayList<Integer>> simpleDigraph;
	private ArrayList<ArrayList<Integer>> simpleReverseDigraph;
	/** <p> Model for this instance. </p>*/
	private Model model;


	/** 
	* <p> Constructor. </p> 
	* @param model 		model of this instance
	*/
	public Digraph(Model model) {
		this.model = model;
		int numMen = model.getNumProposers();
		mensListType1Labels = new int [numMen][numMen];
		mensListType2Labels = new int [numMen][numMen];
	}


	/**
	 * <p> Instantiates the rotation poset digraph. </p>
	 * @param numRotations
	 */
	public void instantiateDigraph(int numRotations) {
		digraph = new int[numRotations][numRotations][2];
	}


	/**
	 * <p> Adds an edge to the rotation poset digraph. </p>
	 * @param from 		from this rotation
	 * @param to 		to this rotation
	 * @param type 		type of rotation
	 */
	public void addDigraphEdge(int from, int to, int type) {
		digraph[from - 1][to - 1][type - 1] = 1;
	}


	/**
	 * <p> Creates the simple digraph representation (where each ij element is
	 * an edge) from the larger digraph. </p>
	 */
	public void createSimpleDigraph() {
		simpleDigraph = new ArrayList<ArrayList<Integer>>();
		simpleReverseDigraph = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < digraph.length; i++) {
			simpleReverseDigraph.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < digraph.length; i++) {
			simpleDigraph.add(new ArrayList<Integer>());
			for (int j = 0; j < digraph[i].length; j++) {
				int type1 = digraph[i][j][0];
				int type2 = digraph[i][j][1];
				if (type1 != 0 || type2 != 0) {
					simpleDigraph.get(i).add(j);
					simpleReverseDigraph.get(j).add(i);
				}
			}
		}
	}


	/**
	 * <p> Returns digraph and reversed digraph as a String. </p>
	 * @return string representation of the digraph
	 */
	public String getResults() {
		String s = "\n// Digraph \n";
		for (int i = 0; i < digraph.length; i++) {
			int[][] row = digraph[i];
			for (int j = 0; j < row.length; j++) {
				s += "(" + digraph[i][j][0] + "," + digraph[i][j][1] + ") ";

			}
			s += "\n";
		}

		s += "\n// Simple digraph \n";
		for (ArrayList<Integer> row : simpleDigraph) {
			for (int elem : row) {
				s += elem + " ";
			}
			s += "\n";
		}

		s += "\n// Simple reverse digraph \n";
		for (ArrayList<Integer> row : simpleReverseDigraph) {
			for (int elem : row) {
				s += elem + " ";
			}
			s += "\n";
		}
		return s;
	}


	/** 
	* <p> Getters and Setters. </p> 
	*/
	public int[][] getMensListType1Labels() {return mensListType1Labels;}
	public int[][] getMensListType2Labels() {return mensListType2Labels;}
	public int[][][] getDigraph() {return digraph;}
	public ArrayList<ArrayList<Integer>> getSimpleDigraph() {
		return simpleDigraph;
	}
	public ArrayList<ArrayList<Integer>> getSimpleReverseDigraph() {
		return simpleReverseDigraph;
	}
	public void setMensListType1Label(int manIndex, int womanIndex, int label) {
		mensListType1Labels[manIndex][womanIndex] = label;
	}
	public void setMensListType2Label(int manIndex, int womanIndex, int label) {
		mensListType2Labels[manIndex][womanIndex] = label;
	}	
}
