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
public class WordSimilarity {
	
	// Constant
	public static final int TREE91_SIZE = 1101;
	public static final int NEW98_SIZE = 340;
	public static final int DOAJ100_SIZE = 100;
	public static final int GPCR254_SIZE = 24;
	public static final int GPCR230_SIZE = 230;

	boolean isInterpro = false;

	// Word Array
	String[] tree91Word = new String[TREE91_SIZE];
	String[] new98Word = new String[NEW98_SIZE];
	String[] doaj100Word = new String[DOAJ100_SIZE];
	String[] gpcr254Word = new String[GPCR254_SIZE];
	String[] gpcr230Word = new String[GPCR230_SIZE];

	// Similarity Matrix
	double[][] new98ToTree91Word = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98Word = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91Word = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91Word = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] gpcr254ToGpcr254Word = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230Word = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230Word = new double[GPCR230_SIZE][GPCR230_SIZE];
	
	public WordSimilarity(boolean isInterpro) {
		this.isInterpro = isInterpro;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		WordSimilarity ws = new WordSimilarity(true);
		ws.loadAndComputeWordSimilarities();
	}

	public void loadAndComputeWordSimilarities() throws Exception {
		if(isInterpro) {
			// Load author names
			this.gpcr254Word = this.loadWordsFromFile("gpcr254_keyword.txt", GPCR254_SIZE);
			this.gpcr230Word = this.loadWordsFromFile("gpcr230_keyword.txt", GPCR230_SIZE);
			
			// Compute similarity
			this.gpcr254ToGpcr254Word = computeWordSimilarity(this.gpcr254Word, this.gpcr254Word);
			this.gpcr254ToGpcr230Word = computeWordSimilarity(this.gpcr254Word, this.gpcr230Word);
			this.gpcr230ToGpcr230Word = computeWordSimilarity(this.gpcr230Word, this.gpcr230Word);
		}
		else {
			// Load author names
			this.tree91Word = this.loadWordsFromFile("ccs91_keyword.txt", TREE91_SIZE);
			this.new98Word = this.loadWordsFromFile("ccs98_keyword.txt", NEW98_SIZE);
			this.doaj100Word = this.loadWordsFromFile("doaj_100.txt", DOAJ100_SIZE);
			
			// Compute similarity
			this.new98ToNew98Word = computeWordSimilarity(this.new98Word, this.new98Word);
			this.new98ToTree91Word = computeWordSimilarity(this.new98Word, this.tree91Word);
			this.tree91ToTree91Word = computeWordSimilarity(this.tree91Word, this.tree91Word);
			this.doaj100ToTree91Word = computeWordSimilarity(this.doaj100Word, this.tree91Word);
		}
	}
	
	
	public double[][] computeWordSimilarity(String[] x, String[] y) {
		double[][] result = new double[x.length][y.length];
		for(int i=0; i<x.length; i++) {
			for(int j=0; j<y.length; j++) {
				String[] xWords = Utility.removeStopWords(x[i].split(" "));
				String[] yWords = Utility.removeStopWords(y[j].split(" "));
				result[i][j] = Utility.computeSameWords(xWords, yWords);
				/*
				if(result[i][j] > 0) {
					System.out.println(x[i] + ", " + y[j] + " = " + result[i][j]);
				}
				*/
			}
		}	
		return result;
	}
	
	public String[] loadWordsFromFile(String fileName, int size) throws IOException {
		String[] result = new String[size];
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;
		int i=0;
		while ((s=lnr.readLine()) != null) {			
			result[i] = s;
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
	 * @return the tree91Word
	 */
	public String[] getTree91Word() {
		return tree91Word;
	}

	/**
	 * @return the new98Word
	 */
	public String[] getNew98Word() {
		return new98Word;
	}

	/**
	 * @return the new98ToTree91Word
	 */
	public double[][] getNew98ToTree91Word() {
		return new98ToTree91Word;
	}

	/**
	 * @return the new98ToNew98Word
	 */
	public double[][] getNew98ToNew98Word() {
		return new98ToNew98Word;
	}

	/**
	 * @return the tree91ToTree91Word
	 */
	public double[][] getTree91ToTree91Word() {
		return tree91ToTree91Word;
	}

	/**
	 * @return the DOAJ100_SIZE
	 */
	public int getDoaj100Size() {
		return DOAJ100_SIZE;
	}

	/**
	 * @return the doaj100Word
	 */
	public String[] getDoaj100Word() {
		return doaj100Word;
	}

	/**
	 * @return the doaj100ToTree91Word
	 */
	public double[][] getDoaj100ToTree91Word() {
		return doaj100ToTree91Word;
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
	 * @return the isInterpro
	 */
	public boolean isInterpro() {
		return isInterpro;
	}

	/**
	 * @return the gpcr254Word
	 */
	public String[] getGpcr254Word() {
		return gpcr254Word;
	}

	/**
	 * @return the gpcr230Word
	 */
	public String[] getGpcr230Word() {
		return gpcr230Word;
	}

	/**
	 * @return the gpcr254ToGpcr254Word
	 */
	public double[][] getGpcr254ToGpcr254Word() {
		return gpcr254ToGpcr254Word;
	}

	/**
	 * @return the gpcr254ToGpcr230Word
	 */
	public double[][] getGpcr254ToGpcr230Word() {
		return gpcr254ToGpcr230Word;
	}

	/**
	 * @return the gpcr230ToGpcr230Word
	 */
	public double[][] getGpcr230ToGpcr230Word() {
		return gpcr230ToGpcr230Word;
	}

}
