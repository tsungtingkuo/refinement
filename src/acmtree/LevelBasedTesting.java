package acmtree;

import java.util.*;


import utility.*;

public class LevelBasedTesting {

	public static void main(String[] args) throws Exception {
		// Parameter
		String fileSuffix = "m6_g0_lv_";
		boolean testingNew98 = true;		// False if testing 98PN100
		boolean singleFeature = false;		// Use 1 feature only
		boolean newAsParent = false;		// Allow new keyword as parent
		boolean validAnswerOnly = true;		// Test valid answer only
		int method = 6;						// 0 = baseline, 1 = LibLinear, 2 = score ensemble, 3 = rank ensemble 
		int keywordNumber = -1;				// -1 = all
		int adjustScore = -1;				// -1 = none
		int adjustSimilarity = -1;			// -1 = none
		int view = -1;						// View mode, -1 = none, 0 = single, 1 = all, 2 = user
		int suggestRank = -1;				// -1 = all
		double alpha = 0.8;					// Structural similarity computation
		double beta = 1;					// Semantic similarity adjustment
		double gamma = 0.0;					// Ratio of baseline (gamma) and classifier (1 - gamma)
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size
		
		LevelBasedTesting.run(fileSuffix, singleFeature, alpha, beta, gamma, semanticThreshold, newAsParent, method, validAnswerOnly, testingNew98, keywordNumber, adjustScore, adjustSimilarity, view, suggestRank, 0);
	}

	public static void run(String fileSuffix, boolean singleFeature, double alpha, double beta, double gamma, double semanticThreshold, boolean newAsParent, int method, boolean validAnswerOnly, boolean testingNew98, int keywordNumber, int adjustScore, int adjustSimilarity, int view, int suggestRank, int trainingDataSize) throws Exception {
			
		// Experiment
		//double confidenceLevel = 0.95;
		int repeatNumber = 1;

		double[] levelOneAccuracyArray = new double[repeatNumber];
		
		//System.out.println("Start insertion, repeat = " + repeatNumber);
		System.out.println();
		
		for(int s=0; s<repeatNumber; s++) {

			// Load 91 tree and 98 tree
			ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt", false);
			ACMTreeNode root98 = new ACMTreeNode("ccs98_experiment.txt", false);
			
			Vector<String> newKeywords = new Vector<String>();
			Vector<String> newNumbers = new Vector<String>();
			if(testingNew98) {
				newKeywords = Utility.loadVector("ccs98_keyword.txt");
			}
			else {
				newKeywords = Utility.loadVector("ccs98_pn100.txt");
				newNumbers = Utility.loadVector("ccs98_pn100_answer_number.txt");				
			}
	
			// Load NGD similarities from File
			ACMTreeSimilarity ns = new ACMTreeSimilarity(false);
			ns.loadAllNGDSimilarities();
			int size = 0;
			if(testingNew98) {
				size = ns.getNew98Size();
			}
			else {
				size = ns.getPn98Size();
			}
	
			// Load web page number for original keywords
			root91.loadWebPageNumber(ns.getTree91(), 0);
						
			// Similarity array, distance array and randomized vector
			boolean[] valid = new boolean[size];
			//Vector<Integer> randomVector = Utility.getShuffledVector(size);
			
			int totalNumber = 0;
			int levelOneCorrectNumber = 0;
			
			int iStart=0;
			int iStop = size;
			if(keywordNumber > -1) {
				iStart = keywordNumber;
				iStop = keywordNumber + 1;
			}
			for(int i=iStart; i<iStop; i++) {
				
				// Use perfect or randomized index
				int index = i;
				//int index = ((Integer)randomVector.get(i)).intValue();
				System.out.print(".");
				if(((i+1)%(size/5))==0) {
					System.out.println();
				}
				
				// Get child name
				String childName = (String)newKeywords.get(index);
				if(keywordNumber > -1) {
					System.out.println("Target keyword = " + childName);
				}
				
				// We might add reordering call here
				
				// Get the node from 98 tree for answer
				ACMTreeNode actualParentNode = new ACMTreeNode(false);
				if(testingNew98) {
					actualParentNode = root98.getNodeByName(childName).getParentNode();
					ACMTreeNode actualParentNode91 = root91.getNodeByName(actualParentNode.getClassificationName());
					if(actualParentNode91 != null) {
						actualParentNode = actualParentNode91;
					}
				}
				else {
					String childNumber = (String)newNumbers.get(index);
					actualParentNode = root91.getNodeByNumber(childNumber);
				}
									
				String actualParentName = actualParentNode.getClassificationName();
				//System.out.println("actualParent = " + actualParentName);
				int actualIndex = root91.getActualParentIndex(actualParentName, newAsParent);
				//System.out.println("actualIndex = " + actualIndex);
				int actualClassLabel = actualParentNode.getClassificationNumber().getLevelOneClassLabel();
				
				// Run if the answer can be found
				valid[index] = false;
				
				if(!validAnswerOnly || actualIndex!=-1) {
				
					//String lv1 = (String)actualParentNode.getParentNameVector(new Vector()).get(1);
					//System.out.println(lv1);
					
					valid[index] = true;
					totalNumber++;
					
					// Load similarities for original and new keywords
					if(testingNew98) {
						root91.loadSimilarity(ns.getNew98ToTree91NGD()[index], 0, ns.getNew98ToNew98NGD()[index]);
						root91.adjustSimilarities(beta, adjustSimilarity);
					}
					else {
						root91.loadSimilarity(ns.getPn98ToTree91NGD()[index], 0);
					}
					
					// Get predicted level 1 class label
					int predictedClassLabel = 0;
					if(method == 1) {
						predictedClassLabel = root91.getLevelBasedPredictedClassLabelByLibSVC("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent);
					}
					else if(method == 6 || method == 7 || method == 8 || method == 12) {		// Weka classification
						predictedClassLabel = root91.getLevelBasedPredictedClassLabelByWekaClassification("model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent);
					}
					//System.out.println("Predicted = " + predictedClassLabel + ", Actual = " + actualClassLabel);
					
					if(predictedClassLabel == actualClassLabel) {
						levelOneCorrectNumber++;
					}	
				}
			}
						
			// Display in original order, for easier comparison
			/*
			for(int i=0; i<ns.getCurrentNew98Size(); i++) {
				System.out.println(sim[i]);
			}
			System.out.println();
			*/
			
			levelOneAccuracyArray[s] = (double)levelOneCorrectNumber / (double)totalNumber;

			System.out.println();
			System.out.println("Done, valid keyword number = " + totalNumber);
		}

		// Compute statistical measurements
		System.out.println();
		Utility.computeAndPrintMean("Level 1 Accuracy", levelOneAccuracyArray);
	}
}
