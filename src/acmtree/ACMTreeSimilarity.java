/**
 * 
 */
package acmtree;

import java.io.*;

/**
 * @author Tim Kuo
 *
 */
public class ACMTreeSimilarity {
	
	// Constant
	double logN = 12;				// Estimated total page size
	public static final int TREE91_SIZE = 1101;
	public static final int NEW98_SIZE = 340;
	public static final int PN98_SIZE = 100;
	public static final int DOAJ100_SIZE = 100;
	public static final int GPCR254_SIZE = 24;
	public static final int GPCR230_SIZE = 230;
	
	boolean isInterpro = false;

	// Similarity Matrix
	double[] tree91 = new double[TREE91_SIZE];
	double[] new98 = new double[NEW98_SIZE];
	double[] pn98 = new double[PN98_SIZE];
	double[] doaj100 = new double[DOAJ100_SIZE];
	double[] gpcr254 = new double[GPCR254_SIZE];
	double[] gpcr230 = new double[GPCR230_SIZE];
	
	double[][] new98ToTree91 = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98 = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91 = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] pn98ToTree91 = new double[PN98_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91 = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] gpcr254ToGpcr254 = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230 = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230 = new double[GPCR230_SIZE][GPCR230_SIZE];
	
	// NGD Similarity Matrix
	double[][] new98ToTree91NGD = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98NGD = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91NGD = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] pn98ToTree91NGD = new double[PN98_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91NGD = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] gpcr254ToGpcr254NGD = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230NGD = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230NGD = new double[GPCR230_SIZE][GPCR230_SIZE];

	// Jaccard Similarity Matrix
	double[][] new98ToTree91Jaccard = new double[NEW98_SIZE][TREE91_SIZE];
	double[][] new98ToNew98Jaccard = new double[NEW98_SIZE][NEW98_SIZE];
	double[][] tree91ToTree91Jaccard = new double[TREE91_SIZE][TREE91_SIZE];
	double[][] pn98ToTree91Jaccard = new double[PN98_SIZE][TREE91_SIZE];
	double[][] doaj100ToTree91Jaccard = new double[DOAJ100_SIZE][TREE91_SIZE];
	double[][] gpcr254ToGpcr254Jaccard = new double[GPCR254_SIZE][GPCR254_SIZE];
	double[][] gpcr254ToGpcr230Jaccard = new double[GPCR254_SIZE][GPCR230_SIZE];
	double[][] gpcr230ToGpcr230Jaccard = new double[GPCR230_SIZE][GPCR230_SIZE];

	public ACMTreeSimilarity(boolean isInterpro) {
		this.isInterpro = isInterpro;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		ACMTreeSimilarity ns = new ACMTreeSimilarity(true);
		ns.loadAllNGDSimilarities();
		
		// Patch 100 * 1101
		//ns.combinePatchFiles("pn100", "_100_6", "100", "patched", 100, 2);
		/*
		for(int i=0; i<100; i++) {
			//ns.patchSimilarityFiles("pn100", Integer.toString(i+1), "yahoo/98pn100_91tree", "100", "patched", 6);
			ns.patchSimilarityFiles("pn100", Integer.toString(i+1), "yahoo/98pn100_91tree", "100", "patched", 237);
		}
		*/		
		
		// Patch 340 * 1101
		//ns.combinePatchFiles("yahoo", "_340_0", "340", "patched", 340, 2);
		/*
		for(int i=0; i<340; i++) {
			//ns.patchSimilarityFiles("yahoo", Integer.toString(i+1), "yahoo/98new_91tree", "340", "patched", 6);
			ns.patchSimilarityFiles("yahoo", Integer.toString(i+1), "yahoo/98new_91tree", "340", "patched", 237);
		}
		*/
		
		// Patch 1101 * 1101
		//ns.patchSimilarityFiles("y", "7", "told", "tpatch", "tnew", 6);
		/*
		for(int i=0; i<1101; i++) {
			//ns.patchSimilarityFiles("y", Integer.toString(i+1), "yahoo/91tree_91tree", "1101", "patched", 6);
			ns.patchSimilarityFiles("y", Integer.toString(i+1), "yahoo/91tree_91tree", "1101", "patched", 237);
		}
		*/
		

		// Combine files for doaj 100 * 1101
		/*
		for(int i=0; i<100; i++) {
			ns.combineSimilarityFiles("doaj", Integer.toString(i+1), "yahoo/part", "yahoo/doaj100_91tree");
		}
		*/
		
		// Combine files for pn 100 * 1101
		/*
		for(int i=0; i<100; i++) {
			ns.combineSimilarityFiles("pn100", Integer.toString(i+1), "yahoo/part", "yahoo/98pn100_91tree");
		}
		*/
		
		// Combine files for acm98 340 * 1101
		/*
		for(int i=0; i<340; i++) {
			ns.combineSimilarityFiles("yahoo", Integer.toString(i+1), "yahoo/part", "yahoo/98new_91tree");
		}
		*/

		// Combine files for acm91 1101 * 1101
		/*
		for(int i=0; i<1101; i++) {
			ns.combineSimilarityFiles("y", Integer.toString(i+1), "part", "yahoo/91tree_91tree");
		}
		*/
		
		// Test NGD Similarity
		/*
		double ngdSim = ns.computeNGDSimilarity(7.7795964912578, 5.945468585131819, 5.184691430, 12);
		System.out.println(ngdSim);
		*/
		
		// Compute NGD Similarity
		/*
		Vector logfxVector = ns.loadSimilarityFromFile("ccs91_proprocessed_Yahoo.txt");
		double logN = 12;

		Vector logfxyVector = ns.loadSimilarityFromFile("Yahoo_3D Stereo Scene Analysis.txt");
		//Vector logfxyVector = ns.loadSimilarityFromFile("Yahoo_Distributed Objects.txt");
		//Vector logfxyVector = ns.loadSimilarityFromFile("Yahoo_Quadratic Programming Methods.txt");
		
		double logfy = 2.6273658565927325;	// 3D Stereo Scene Analysis
		//double logfy = 5.945468585131819;	// Distributed Objects
		//double logfy = 3.802089257881733;	// Quadratic Programming Methods
		
		for(int i=0; i<logfxVector.size(); i++) {
			double logfx = Double.parseDouble((String)logfxVector.get(i));
			double logfxy = Double.parseDouble((String)logfxyVector.get(i));
			double ngdSim = ns.computeNGDSimilarity(logfx, logfy, logfxy, logN);
			System.out.println(ngdSim);
		}
		*/
	}

	public void loadAllNGDSimilarities() throws Exception {
		
		if(isInterpro) {
			this.gpcr254 = this.loadSimilarityFromFile("yahoo/gpcr254/gpcr254.txt", GPCR254_SIZE);
			this.gpcr230 = this.loadSimilarityFromFile("yahoo/gpcr230/gpcr230.txt", GPCR230_SIZE);
			
			// 254 * 230
			for(int i=0; i<GPCR254_SIZE; i++) {
				this.gpcr254ToGpcr230[i] = this.loadSimilarityFromFile("yahoo/gpcr254_gpcr230/" + (i+1) + "_gpcr254.txt", GPCR230_SIZE);
				for(int j=0; j<GPCR230_SIZE; j++) {
					double logfx = this.gpcr254[i];
					double logfy = this.gpcr230[j];
					double logfxy = this.gpcr254ToGpcr230[i][j];
					this.gpcr254ToGpcr230NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.gpcr254ToGpcr230Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
			
			// 254 * 254
			for(int i=0; i<GPCR254_SIZE; i++) {
				this.gpcr254ToGpcr254[i] = this.loadSimilarityFromFile("yahoo/gpcr254_gpcr254/" + (i+1) + "_gpcr254.txt", GPCR254_SIZE);
				for(int j=0; j<GPCR254_SIZE; j++) {
					double logfx = this.gpcr254[i];
					double logfy = this.gpcr254[j];
					double logfxy = this.gpcr254ToGpcr254[i][j];
					this.gpcr254ToGpcr254NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.gpcr254ToGpcr254Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
			
			// 230 * 230
			for(int i=0; i<GPCR230_SIZE; i++) {
				this.gpcr230ToGpcr230[i] = this.loadSimilarityFromFile("yahoo/gpcr230_gpcr230/" + (i+1) + "_gpcr230.txt", GPCR230_SIZE);
				for(int j=0; j<GPCR230_SIZE; j++) {
					double logfx = this.gpcr230[i];
					double logfy = this.gpcr230[j];
					double logfxy = this.gpcr230ToGpcr230[i][j];
					this.gpcr230ToGpcr230NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.gpcr230ToGpcr230Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
		}
		else {
			this.tree91 = this.loadSimilarityFromFile("yahoo/91tree/91tree.txt", TREE91_SIZE);
			this.new98 = this.loadSimilarityFromFile("yahoo/98new/98new.txt", NEW98_SIZE);
			this.pn98 = this.loadSimilarityFromFile("yahoo/98pn100/98pn100.txt", PN98_SIZE);
			this.doaj100 = this.loadSimilarityFromFile("yahoo/doaj100/doaj100.txt", DOAJ100_SIZE);
			
	
			// 98-New * 91-Tree
			for(int i=0; i<NEW98_SIZE; i++) {
				this.new98ToTree91[i] = this.loadSimilarityFromFile("yahoo/98new_91tree/yahoo_" + (i+1) + ".txt", TREE91_SIZE);
				for(int j=0; j<TREE91_SIZE; j++) {
					double logfx = this.new98[i];
					double logfy = this.tree91[j];
					double logfxy = this.new98ToTree91[i][j];
					this.new98ToTree91NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.new98ToTree91Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
	
			// 98-New * 98-New
			for(int i=0; i<NEW98_SIZE; i++) {
				this.new98ToNew98[i] = this.loadSimilarityFromFile("yahoo/98new_98new/" + (i+1) + "_340.txt", NEW98_SIZE);			
				for(int j=0; j<NEW98_SIZE; j++) {
					double logfx = this.new98[i];
					double logfy = this.new98[j];
					double logfxy = this.new98ToNew98[i][j];
					this.new98ToNew98NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.new98ToNew98Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}			
			}	
			
			// 91-Tree * 91-Tree
			for(int i=0; i<TREE91_SIZE; i++) {
				this.tree91ToTree91[i] = this.loadSimilarityFromFile("yahoo/91tree_91tree/y_" + (i+1) + ".txt", TREE91_SIZE);
				for(int j=0; j<TREE91_SIZE; j++) {
					double logfx = this.tree91[i];
					double logfy = this.tree91[j];
					double logfxy = this.tree91ToTree91[i][j];
					this.tree91ToTree91NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.tree91ToTree91Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
			
			// 98-pn * 91-tree
			for(int i=0; i<PN98_SIZE; i++) {
				this.pn98ToTree91[i] = this.loadSimilarityFromFile("yahoo/98pn100_91tree/pn100_" + (i+1) + ".txt", TREE91_SIZE);
				for(int j=0; j<TREE91_SIZE; j++) {
					double logfx = this.pn98[i];
					double logfy = this.tree91[j];
					double logfxy = this.pn98ToTree91[i][j];
					this.pn98ToTree91NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.pn98ToTree91Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
			
			// doaj * 91-tree
			for(int i=0; i<DOAJ100_SIZE; i++) {
				this.doaj100ToTree91[i] = this.loadSimilarityFromFile("yahoo/doaj100_91tree/doaj_" + (i+1) + ".txt", TREE91_SIZE);
				for(int j=0; j<TREE91_SIZE; j++) {
					double logfx = this.doaj100[i];
					double logfy = this.tree91[j];
					double logfxy = this.doaj100ToTree91[i][j];
					this.doaj100ToTree91NGD[i][j] = this.computeNGDSimilarity(logfx, logfy, logfxy, this.logN);
					this.doaj100ToTree91Jaccard[i][j] = this.computeJaccardSimilarity(logfx, logfy, logfxy);
				}
			}
		}
	}

	public double computeJaccardSimilarity(double logfx, double logfy, double logfxy) {
		double jaccardSim = 0;
		
		//double fx = Math.pow(10, logfx);
		//double fy = Math.pow(10, logfy);
		//double fxy = Math.pow(10, logfxy);		
		//double jaccardNumerator = fxy;
		//double jaccardDenominator = fx + fy - fxy;
		
		double jaccardNumerator = logfxy;
		double jaccardDenominator = logfx + logfy - logfxy;
		
		jaccardSim = (double)jaccardNumerator / (double)jaccardDenominator;
			
		if(jaccardSim<0 || jaccardDenominator==0) {
			jaccardSim = 0;
		}
		
		return jaccardSim;
	}
	
	public double computeNGDSimilarity(double logfx, double logfy, double logfxy, double logN) {
		double ngdSim = 0;
		
		if((logfx>0) && (logfx>0) && (logfxy>0)) {
		
			double max = Math.max(logfx, logfy);
			double min = Math.min(logfx, logfy);
			double ngdNumerator = max - logfxy;
			double ngdDenominator = logN - min;
			double ngd = (double)ngdNumerator / (double)ngdDenominator;
			
			ngdSim = 1 - ngd;
			
			if(ngdSim < 0) {
				ngdSim = 0;
			}
		}
		
		return ngdSim;
	}
	
	public double[] loadSimilarityFromFile(String fileName, int size) throws IOException {
		
		double[] result = new double[size];
		
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		int i=0;
		while ((s=lnr.readLine()) != null) {
			result[i] = Double.parseDouble(s);
			i++;
		}
			
		lnr.close();
		fr.close();
		
		return result;
	}
	
	public void combineSimilarityFiles(String prefix, String fileNumber, String oldDir, String newDir) throws IOException {
		
		int j=0;
		PrintWriter pw = new PrintWriter(newDir + "/" + prefix + "_" + fileNumber + ".txt");
		
		for(int i=0; i<5; i++) {
			FileReader fr = new FileReader(oldDir + "/" + fileNumber + "_100_" + (i+1) + ".txt");
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
	
			while ((s=lnr.readLine()) != null) {
				//System.out.println(s);
				pw.println(s);
				j++;
			}
				
			lnr.close();
			fr.close();
		}
	
		pw.close();
		
		//System.out.println();
		System.out.println("Total = " + j);
	}

	public void combinePatchFiles(String prefix, String suffix, String oldDir, String newDir, int keywordSize, int patchSize) throws IOException {
		
		int j=0;
		PrintWriter[] pws = new PrintWriter[patchSize];
		for(int k=0; k<patchSize; k++) {
			pws[k] = new PrintWriter(newDir + "/" + prefix + "_" + Integer.toString(k + 1) + ".txt");
		}

		for(int i=0; i<keywordSize; i++) {
			FileReader fr = new FileReader(oldDir + "/" + Integer.toString(i + 1) + suffix + ".txt");
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			for(int k=0; k<patchSize; k++) {
				s=lnr.readLine();
				pws[k].println(s);
				j++;
			}
				
			lnr.close();
			fr.close();
		}
	
		for(int k=0; k<patchSize; k++) {
			pws[k].close();
		}
		//System.out.println();
		System.out.println("Total = " + j);
	}
	
	/*
	 * Patch "X * 1101" files
	 */
	public void patchSimilarityFiles(String prefix, String fileNumber, String oldDir, String patchDir, String newDir, int target) throws IOException {
		
		int number = Integer.parseInt(fileNumber) - 1;
		int j=0;
		PrintWriter pw = new PrintWriter(newDir + "/" + prefix + "_" + fileNumber + ".txt");
		
		FileReader frOld = new FileReader(oldDir + "/" + prefix + "_" + fileNumber + ".txt");
		LineNumberReader lnrOld = new LineNumberReader(frOld);
		String sOld = null;

		FileReader frPatch = new FileReader(patchDir + "/" + prefix + "_" + Integer.toString(target + 1) + ".txt");
		LineNumberReader lnrPatch = new LineNumberReader(frPatch);
		String sPatch = null;
		
		for(int i=0; i<=number; i++) { 
			sPatch = lnrPatch.readLine();
		}

		while ((sOld=lnrOld.readLine()) != null) {
			if(j == target) {
				pw.println(sPatch);
			}
			else {
				pw.println(sOld);
			}			
			j++;
		}
				
		lnrOld.close();
		frOld.close();
		lnrPatch.close();
		frPatch.close();
	
		pw.close();
		
		//System.out.println();
		System.out.println("Total = " + j);
	}

	/**
	 * @return the logN
	 */
	public double getLogN() {
		return logN;
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
	 * @return the PN98_SIZE
	 */
	public int getPn98Size() {
		return PN98_SIZE;
	}

	/**
	 * @return the PN98_SIZE
	 */
	public int getDoaj100Size() {
		return DOAJ100_SIZE;
	}

	/**
	 * @return the tree91
	 */
	public double[] getTree91() {
		return tree91;
	}

	/**
	 * @return the new98
	 */
	public double[] getNew98() {
		return new98;
	}

	/**
	 * @return the pn98
	 */
	public double[] getPn98() {
		return pn98;
	}

	/**
	 * @return the new98ToTree91
	 */
	public double[][] getNew98ToTree91() {
		return new98ToTree91;
	}

	/**
	 * @return the new98ToTree91NGD
	 */
	public double[][] getNew98ToTree91NGD() {
		return new98ToTree91NGD;
	}

	/**
	 * @return the new98Tonew98
	 */
	public double[][] getNew98ToNew98() {
		return new98ToNew98;
	}

	/**
	 * @return the new98ToNew98NGD
	 */
	public double[][] getNew98ToNew98NGD() {
		return new98ToNew98NGD;
	}

	/**
	 * @return the tree91ToTree91
	 */
	public double[][] getTree91ToTree91() {
		return tree91ToTree91;
	}

	/**
	 * @return the tree91ToTree91NGD
	 */
	public double[][] getTree91ToTree91NGD() {
		return tree91ToTree91NGD;
	}

	/**
	 * @return the pn98ToTree91
	 */
	public double[][] getPn98ToTree91() {
		return pn98ToTree91;
	}

	/**
	 * @param pn98ToTree91 the pn98ToTree91 to set
	 */
	public void setPn98ToTree91(double[][] pn98ToTree91) {
		this.pn98ToTree91 = pn98ToTree91;
	}

	/**
	 * @return the pn98ToTree91NGD
	 */
	public double[][] getPn98ToTree91NGD() {
		return pn98ToTree91NGD;
	}

	/**
	 * @param pn98ToTree91NGD the pn98ToTree91NGD to set
	 */
	public void setPn98ToTree91NGD(double[][] pn98ToTree91NGD) {
		this.pn98ToTree91NGD = pn98ToTree91NGD;
	}

	/**
	 * @return the new98ToTree91Jaccard
	 */
	public double[][] getNew98ToTree91Jaccard() {
		return new98ToTree91Jaccard;
	}

	/**
	 * @return the new98ToNew98Jaccard
	 */
	public double[][] getNew98ToNew98Jaccard() {
		return new98ToNew98Jaccard;
	}

	/**
	 * @return the tree91ToTree91Jaccard
	 */
	public double[][] getTree91ToTree91Jaccard() {
		return tree91ToTree91Jaccard;
	}

	/**
	 * @return the pn98ToTree91Jaccard
	 */
	public double[][] getPn98ToTree91Jaccard() {
		return pn98ToTree91Jaccard;
	}

	/**
	 * @return the doaj100
	 */
	public double[] getDoaj100() {
		return doaj100;
	}

	/**
	 * @return the doaj100ToTree91
	 */
	public double[][] getDoaj100ToTree91() {
		return doaj100ToTree91;
	}

	/**
	 * @return the doaj100ToTree91NGD
	 */
	public double[][] getDoaj100ToTree91NGD() {
		return doaj100ToTree91NGD;
	}

	/**
	 * @return the doaj100ToTree91Jaccard
	 */
	public double[][] getDoaj100ToTree91Jaccard() {
		return doaj100ToTree91Jaccard;
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
	 * @return the pN98_SIZE
	 */
	public static int getPN98_SIZE() {
		return PN98_SIZE;
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
	 * @return the gpcr254
	 */
	public double[] getGpcr254() {
		return gpcr254;
	}

	/**
	 * @return the gpcr230
	 */
	public double[] getGpcr230() {
		return gpcr230;
	}

	/**
	 * @return the gpcr254ToGpcr254
	 */
	public double[][] getGpcr254ToGpcr254() {
		return gpcr254ToGpcr254;
	}

	/**
	 * @return the gpcr254ToGpcr230
	 */
	public double[][] getGpcr254ToGpcr230() {
		return gpcr254ToGpcr230;
	}

	/**
	 * @return the gpcr230ToGpcr230
	 */
	public double[][] getGpcr230ToGpcr230() {
		return gpcr230ToGpcr230;
	}

	/**
	 * @return the gpcr254ToGpcr254NGD
	 */
	public double[][] getGpcr254ToGpcr254NGD() {
		return gpcr254ToGpcr254NGD;
	}

	/**
	 * @return the gpcr254ToGpcr230NGD
	 */
	public double[][] getGpcr254ToGpcr230NGD() {
		return gpcr254ToGpcr230NGD;
	}

	/**
	 * @return the gpcr230ToGpcr230NGD
	 */
	public double[][] getGpcr230ToGpcr230NGD() {
		return gpcr230ToGpcr230NGD;
	}

	/**
	 * @return the gpcr254ToGpcr254Jaccard
	 */
	public double[][] getGpcr254ToGpcr254Jaccard() {
		return gpcr254ToGpcr254Jaccard;
	}

	/**
	 * @return the gpcr254ToGpcr230Jaccard
	 */
	public double[][] getGpcr254ToGpcr230Jaccard() {
		return gpcr254ToGpcr230Jaccard;
	}

	/**
	 * @return the gpcr230ToGpcr230Jaccard
	 */
	public double[][] getGpcr230ToGpcr230Jaccard() {
		return gpcr230ToGpcr230Jaccard;
	}
}
