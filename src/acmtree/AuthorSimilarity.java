/**
 * 
 */
package acmtree;

import java.io.*;

import utility.*;

/**
 * @author Tim Kuo
 *
 */
public class AuthorSimilarity {
	
	// Constant
	public static final int AUTHOR_SIZE = 5;				// Level 1
	public static final int LEVEL2_AUTHOR_SIZE = 25;		// Level 2

	public static final int TREE91_SIZE = 1101;
	public static final int NEW98_SIZE = 340;
	public static final int DOAJ100_SIZE = 100;
	public static final int GPCR254_SIZE = 24;
	public static final int GPCR230_SIZE = 230;

	boolean isInterpro = false;
	
	// Author Matrix
	String[][] tree91Level1 = new String[TREE91_SIZE][AUTHOR_SIZE];
	String[][] tree91Level2 = new String[TREE91_SIZE][LEVEL2_AUTHOR_SIZE];
	String[][] tree91LevelBoth = new String[TREE91_SIZE][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];

	String[][] new98Level1 = new String[NEW98_SIZE][AUTHOR_SIZE];
	String[][] new98Level2 = new String[NEW98_SIZE][LEVEL2_AUTHOR_SIZE];
	String[][] new98LevelBoth = new String[NEW98_SIZE][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];
	
	String[][] doaj100Level1 = new String[DOAJ100_SIZE][AUTHOR_SIZE];
	String[][] doaj100Level2 = new String[DOAJ100_SIZE][LEVEL2_AUTHOR_SIZE];
	String[][] doaj100LevelBoth = new String[DOAJ100_SIZE][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];
	
	String[][] gpcr254Level1 = new String[GPCR254_SIZE][AUTHOR_SIZE];
	String[][] gpcr254Level2 = new String[GPCR254_SIZE][LEVEL2_AUTHOR_SIZE];
	String[][] gpcr254LevelBoth = new String[GPCR254_SIZE][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];

	String[][] gpcr230Level1 = new String[GPCR230_SIZE][AUTHOR_SIZE];
	String[][] gpcr230Level2 = new String[GPCR230_SIZE][LEVEL2_AUTHOR_SIZE];
	String[][] gpcr230LevelBoth = new String[GPCR230_SIZE][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];

	// Similarity Matrix
	double[][] new98ToTree91Level1 = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98Level1 = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91Level1 = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91Level1 = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] doaj100ToDoaj100Level1 = new double[DOAJ100_SIZE][DOAJ100_SIZE];
	double[][] gpcr254ToGpcr254Level1 = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230Level1 = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230Level1 = new double[GPCR230_SIZE][GPCR230_SIZE];

	double[][] new98ToTree91Level2 = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98Level2 = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91Level2 = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91Level2 = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] doaj100ToDoaj100Level2 = new double[DOAJ100_SIZE][DOAJ100_SIZE];
	double[][] gpcr254ToGpcr254Level2 = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230Level2 = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230Level2 = new double[GPCR230_SIZE][GPCR230_SIZE];
	
	double[][] new98ToTree91LevelBoth = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98LevelBoth = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91LevelBoth = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91LevelBoth = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] doaj100ToDoaj100LevelBoth = new double[DOAJ100_SIZE][DOAJ100_SIZE];
	double[][] gpcr254ToGpcr254LevelBoth = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230LevelBoth = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230LevelBoth = new double[GPCR230_SIZE][GPCR230_SIZE];
	
	public AuthorSimilarity(boolean isInterpro) {
		this.isInterpro = isInterpro;
	}
	
	public void loadAndComputeAuthorSimilarities(boolean isTesting) throws Exception {
		
		// Load author names
		//System.out.print("Loading author files...");
		
		this.tree91Level1 = this.loadAuthorsFromFile("html/author91.txt", TREE91_SIZE, AUTHOR_SIZE);
		this.tree91Level2 = this.loadAuthorsFromFile("html/author91_level2.txt", TREE91_SIZE, LEVEL2_AUTHOR_SIZE);
		this.tree91LevelBoth = this.combineBothLevelAuthors(tree91Level1, tree91Level2);

		this.new98Level1 = this.loadAuthorsFromFile("html/author98.txt", NEW98_SIZE, AUTHOR_SIZE);
		this.new98Level2 = this.loadAuthorsFromFile("html/author98_level2.txt", NEW98_SIZE, LEVEL2_AUTHOR_SIZE);
		this.new98LevelBoth = this.combineBothLevelAuthors(new98Level1, new98Level2);
		
		this.doaj100Level1 = this.loadAuthorsFromFile("html/authorDoaj.txt", DOAJ100_SIZE, AUTHOR_SIZE);
		this.doaj100Level2 = this.loadAuthorsFromFile("html/authorDoaj_level2.txt", DOAJ100_SIZE, LEVEL2_AUTHOR_SIZE);
		this.doaj100LevelBoth = this.combineBothLevelAuthors(doaj100Level1, doaj100Level2);

		this.gpcr254Level1 = this.loadAuthorsFromFile("html/authorGpcr254.txt", GPCR254_SIZE, AUTHOR_SIZE);
		this.gpcr254Level2 = this.loadAuthorsFromFile("html/authorGpcr254_level2.txt", GPCR254_SIZE, LEVEL2_AUTHOR_SIZE);
		this.gpcr254LevelBoth = this.combineBothLevelAuthors(gpcr254Level1, gpcr254Level2);

		this.gpcr230Level1 = this.loadAuthorsFromFile("html/authorGpcr230.txt", GPCR230_SIZE, AUTHOR_SIZE);
		this.gpcr230Level2 = this.loadAuthorsFromFile("html/authorGpcr230_level2.txt", GPCR230_SIZE, LEVEL2_AUTHOR_SIZE);
		this.gpcr230LevelBoth = this.combineBothLevelAuthors(gpcr230Level1, gpcr230Level2);

		//System.out.println("done!");
		
		// Compute similarity
		
		
		//System.out.print("Computing level 1 author similarity...");
		//this.new98ToNew98Level1 = computeAuthorSimilarity(this.new98Level1, this.new98Level1);
		if(isTesting) {
			if(isInterpro) {
				this.gpcr254ToGpcr230Level1 = computeAuthorSimilarity(this.gpcr254Level1, this.gpcr230Level1);
				this.gpcr254ToGpcr254Level1 = computeAuthorSimilarity(this.gpcr254Level1, this.gpcr254Level1);
			}
			else {
				this.new98ToTree91Level1 = computeAuthorSimilarity(this.new98Level1, this.tree91Level1);
				this.new98ToNew98Level1 = computeAuthorSimilarity(this.new98Level1, this.new98Level1);
				this.doaj100ToTree91Level1 = computeAuthorSimilarity(this.doaj100Level1, this.tree91Level1);
				this.doaj100ToDoaj100Level1 = computeAuthorSimilarity(this.doaj100Level1, this.doaj100Level1);
			}
		}
		else {
			if(isInterpro) {
				this.gpcr230ToGpcr230Level1 = computeAuthorSimilarity(this.gpcr230Level1, this.gpcr230Level1);
			}
			else {
				this.tree91ToTree91Level1 = computeAuthorSimilarity(this.tree91Level1, this.tree91Level1);
			}
		}
		//System.out.println("done!");

		//System.out.print("Computing level 2 author similarity...");
		//this.new98ToNew98Level2 = computeAuthorSimilarity(this.new98Level2, this.new98Level2);
		if(isTesting) {
			if(isInterpro) {
				this.gpcr254ToGpcr230Level2 = computeAuthorSimilarity(this.gpcr254Level2, this.gpcr230Level2);
				this.gpcr254ToGpcr254Level2 = computeAuthorSimilarity(this.gpcr254Level2, this.gpcr254Level2);
			}
			else {
				this.new98ToTree91Level2 = computeAuthorSimilarity(this.new98Level2, this.tree91Level2);		
				this.new98ToNew98Level2 = computeAuthorSimilarity(this.new98Level2, this.new98Level2);
				this.doaj100ToTree91Level2 = computeAuthorSimilarity(this.doaj100Level2, this.tree91Level2);		
				this.doaj100ToDoaj100Level2 = computeAuthorSimilarity(this.doaj100Level2, this.doaj100Level2);
			}
		}
		else {
			if(isInterpro) {
				this.gpcr230ToGpcr230Level2 = computeAuthorSimilarity(this.gpcr230Level2, this.gpcr230Level2);
			}
			else {
				this.tree91ToTree91Level2 = computeAuthorSimilarity(this.tree91Level2, this.tree91Level2);
			}
		}
		//System.out.println("done!");

		//System.out.print("Computing level 1+2 author similarity...");
		//this.new98ToNew98LevelBoth = computeAuthorSimilarity(this.new98LevelBoth, this.new98LevelBoth);
		if(isTesting) {
			if(isInterpro) {
				this.gpcr254ToGpcr230LevelBoth = computeAuthorSimilarity(this.gpcr254LevelBoth, this.gpcr230LevelBoth);
				this.gpcr254ToGpcr254LevelBoth = computeAuthorSimilarity(this.gpcr254LevelBoth, this.gpcr254LevelBoth);
			}
			else {
				this.new98ToTree91LevelBoth = computeAuthorSimilarity(this.new98LevelBoth, this.tree91LevelBoth);
				this.new98ToNew98LevelBoth = computeAuthorSimilarity(this.new98LevelBoth, this.new98LevelBoth);
				this.doaj100ToTree91LevelBoth = computeAuthorSimilarity(this.doaj100LevelBoth, this.tree91LevelBoth);
				this.doaj100ToDoaj100LevelBoth = computeAuthorSimilarity(this.doaj100LevelBoth, this.doaj100LevelBoth);
			}
		}
		else {
			if(isInterpro) {
				this.gpcr230ToGpcr230LevelBoth = computeAuthorSimilarity(this.gpcr230LevelBoth, this.gpcr230LevelBoth);
			}
			else {
				this.tree91ToTree91LevelBoth = computeAuthorSimilarity(this.tree91LevelBoth, this.tree91LevelBoth);
			}
		}
		//System.out.println("done!");
	}
	
	public String[][] combineBothLevelAuthors(String[][] x, String[][]y) {
		String[][] both = new String[x.length][AUTHOR_SIZE + LEVEL2_AUTHOR_SIZE];
		for(int i=0; i<x.length; i++) {
			both[i] = Utility.uniqueStringArrayAdd(Utility.uniqueStringArray(x[i]), y[i]);
			//both[i] = Utility.uniqueStringArrayAdd(x[i], y[i]);
			//both[i] = Utility.stringArrayAdd(x[i], y[i]);
			//Utility.printStringArray(both[i], both[i].length);
		}
		return both;
	}
	
	public double[][] computeAuthorSimilarity(String[][] x, String[][] y) {
		double[][] result = new double[x.length][y.length];
		for(int i=0; i<x.length; i++) {
			for(int j=0; j<y.length; j++) {
				result[i][j] = Utility.computeSameWords(x[i], y[j]);
				//System.out.print(result[i][j] + "\t");
			}
			//System.out.println();
		}	
		return result;
	}
	
	public String[][] loadAuthorsFromFile(String fileName, int size, int number) throws IOException {
		
		String[][] result = new String[size][number];
		
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		int i=0;
		while ((s=lnr.readLine()) != null) {			
			result[i] = s.split("\t");
			//for(int j=0; j<result[i].length; j++) {
			//	System.out.print(result[i][j] + ", ");
			//}
			//System.out.println();
			i++;
		}
			
		lnr.close();
		fr.close();
		
		return result;
	}


	/**
	 * @return the TREE91_SIZE
	 */
	public int getTree91Size() {
		return TREE91_SIZE;
	}

	/**
	 * @return the NEW98_SIZE
	 */
	public int getNew98Size() {
		return NEW98_SIZE;
	}

	/**
	 * @return the AUTHOR_SIZE
	 */
	public int getAuthorSize() {
		return AUTHOR_SIZE;
	}

	/**
	 * @return the tree91
	 */
	public String[][] getTree91Level1() {
		return tree91Level1;
	}

	/**
	 * @return the new98
	 */
	public String[][] getNew98Level1() {
		return new98Level1;
	}

	/**
	 * @return the new98ToTree91
	 */
	public double[][] getNew98ToTree91Level1() {
		return new98ToTree91Level1;
	}

	/**
	 * @return the new98ToNew98
	 */
	public double[][] getNew98ToNew98Level1() {
		return new98ToNew98Level1;
	}

	/**
	 * @return the tree91ToTree91
	 */
	public double[][] getTree91ToTree91Level1() {
		return tree91ToTree91Level1;
	}

	/**
	 * @return the LEVEL2_AUTHOR_SIZE
	 */
	public int getLevel2AuthorSize() {
		return LEVEL2_AUTHOR_SIZE;
	}

	/**
	 * @return the tree91Level2
	 */
	public String[][] getTree91Level2() {
		return tree91Level2;
	}

	/**
	 * @return the new98Level2
	 */
	public String[][] getNew98Level2() {
		return new98Level2;
	}

	/**
	 * @return the tree91LevelBoth
	 */
	public String[][] getTree91LevelBoth() {
		return tree91LevelBoth;
	}

	/**
	 * @return the new98LevelBoth
	 */
	public String[][] getNew98LevelBoth() {
		return new98LevelBoth;
	}

	/**
	 * @return the new98ToTree91Level2
	 */
	public double[][] getNew98ToTree91Level2() {
		return new98ToTree91Level2;
	}

	/**
	 * @return the new98ToNew98Level2
	 */
	public double[][] getNew98ToNew98Level2() {
		return new98ToNew98Level2;
	}

	/**
	 * @return the tree91ToTree91Level2
	 */
	public double[][] getTree91ToTree91Level2() {
		return tree91ToTree91Level2;
	}

	/**
	 * @return the new98ToTree91LevelBoth
	 */
	public double[][] getNew98ToTree91LevelBoth() {
		return new98ToTree91LevelBoth;
	}

	/**
	 * @return the new98ToNew98LevelBoth
	 */
	public double[][] getNew98ToNew98LevelBoth() {
		return new98ToNew98LevelBoth;
	}

	/**
	 * @return the tree91ToTree91LevelBoth
	 */
	public double[][] getTree91ToTree91LevelBoth() {
		return tree91ToTree91LevelBoth;
	}

	/**
	 * @return the DOAJ100_SIZE
	 */
	public int getDoaj100Size() {
		return DOAJ100_SIZE;
	}

	/**
	 * @return the doaj100Level1
	 */
	public String[][] getDoaj100Level1() {
		return doaj100Level1;
	}

	/**
	 * @return the doaj100Level2
	 */
	public String[][] getDoaj100Level2() {
		return doaj100Level2;
	}

	/**
	 * @return the doaj100LevelBoth
	 */
	public String[][] getDoaj100LevelBoth() {
		return doaj100LevelBoth;
	}

	/**
	 * @return the doaj100ToTree91Level1
	 */
	public double[][] getDoaj100ToTree91Level1() {
		return doaj100ToTree91Level1;
	}

	/**
	 * @return the doaj100ToDoaj100Level1
	 */
	public double[][] getDoaj100ToDoaj100Level1() {
		return doaj100ToDoaj100Level1;
	}

	/**
	 * @return the doaj100ToTree91Level2
	 */
	public double[][] getDoaj100ToTree91Level2() {
		return doaj100ToTree91Level2;
	}

	/**
	 * @return the doaj100ToDoaj100Level2
	 */
	public double[][] getDoaj100ToDoaj100Level2() {
		return doaj100ToDoaj100Level2;
	}

	/**
	 * @return the doaj100ToTree91LevelBoth
	 */
	public double[][] getDoaj100ToTree91LevelBoth() {
		return doaj100ToTree91LevelBoth;
	}

	/**
	 * @return the doaj100ToDoaj100LevelBoth
	 */
	public double[][] getDoaj100ToDoaj100LevelBoth() {
		return doaj100ToDoaj100LevelBoth;
	}

	/**
	 * @return the aUTHOR_SIZE
	 */
	public static int getAUTHOR_SIZE() {
		return AUTHOR_SIZE;
	}

	/**
	 * @return the lEVEL2_AUTHOR_SIZE
	 */
	public static int getLEVEL2_AUTHOR_SIZE() {
		return LEVEL2_AUTHOR_SIZE;
	}

	/**
	 * @return the tREE91_SIZE
	 */
	public static int getTREE91_SIZE() {
		return TREE91_SIZE;
	}

	/**
	 * @return the nEW98_SIZE
	 */
	public static int getNEW98_SIZE() {
		return NEW98_SIZE;
	}

	/**
	 * @return the dOAJ100_SIZE
	 */
	public static int getDOAJ100_SIZE() {
		return DOAJ100_SIZE;
	}

	/**
	 * @return the gPCR254_SIZE
	 */
	public static int getGPCR254_SIZE() {
		return GPCR254_SIZE;
	}

	/**
	 * @return the gPCR230_SIZE
	 */
	public static int getGPCR230_SIZE() {
		return GPCR230_SIZE;
	}

	/**
	 * @return the gpcr254Level1
	 */
	public String[][] getGpcr254Level1() {
		return gpcr254Level1;
	}

	/**
	 * @return the gpcr254Level2
	 */
	public String[][] getGpcr254Level2() {
		return gpcr254Level2;
	}

	/**
	 * @return the gpcr254LevelBoth
	 */
	public String[][] getGpcr254LevelBoth() {
		return gpcr254LevelBoth;
	}

	/**
	 * @return the gpcr230Level1
	 */
	public String[][] getGpcr230Level1() {
		return gpcr230Level1;
	}

	/**
	 * @return the gpcr230Level2
	 */
	public String[][] getGpcr230Level2() {
		return gpcr230Level2;
	}

	/**
	 * @return the gpcr230LevelBoth
	 */
	public String[][] getGpcr230LevelBoth() {
		return gpcr230LevelBoth;
	}

	/**
	 * @return the gpcr254ToGpcr254Level1
	 */
	public double[][] getGpcr254ToGpcr254Level1() {
		return gpcr254ToGpcr254Level1;
	}

	/**
	 * @return the gpcr254ToGpcr230Level1
	 */
	public double[][] getGpcr254ToGpcr230Level1() {
		return gpcr254ToGpcr230Level1;
	}

	/**
	 * @return the gpcr230ToGpcr230Level1
	 */
	public double[][] getGpcr230ToGpcr230Level1() {
		return gpcr230ToGpcr230Level1;
	}

	/**
	 * @return the gpcr254ToGpcr254Level2
	 */
	public double[][] getGpcr254ToGpcr254Level2() {
		return gpcr254ToGpcr254Level2;
	}

	/**
	 * @return the gpcr254ToGpcr230Level2
	 */
	public double[][] getGpcr254ToGpcr230Level2() {
		return gpcr254ToGpcr230Level2;
	}

	/**
	 * @return the gpcr230ToGpcr230Level2
	 */
	public double[][] getGpcr230ToGpcr230Level2() {
		return gpcr230ToGpcr230Level2;
	}

	/**
	 * @return the gpcr254ToGpcr254LevelBoth
	 */
	public double[][] getGpcr254ToGpcr254LevelBoth() {
		return gpcr254ToGpcr254LevelBoth;
	}

	/**
	 * @return the gpcr254ToGpcr230LevelBoth
	 */
	public double[][] getGpcr254ToGpcr230LevelBoth() {
		return gpcr254ToGpcr230LevelBoth;
	}

	/**
	 * @return the gpcr230ToGpcr230LevelBoth
	 */
	public double[][] getGpcr230ToGpcr230LevelBoth() {
		return gpcr230ToGpcr230LevelBoth;
	}
}
