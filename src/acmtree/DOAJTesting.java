package acmtree;

import java.io.*;
import java.util.*;


import utility.*;

public class DOAJTesting {

	public static void main(String[] args) throws Exception {
		// Parameter
		String fileSuffix = "full";
		boolean enrichment = false;			// Use advanced features or not
		boolean singleFeature = false;		// Use 1 feature only
		boolean newAsParent = false;		// Allow new keyword as parent
		boolean validAnswerOnly = true;		// Test valid answer only
		int testingMode = 2;				// 0=98, 1=pn, 2=doaj
		int method = 6;						// -1=Random, 26=ConceptName, 21=Intersection, 16=Jaccard, 0=NGD, 14=KeyAuthor, 18=ExtendedKeyAuthor, 29=OneNorm, 30=TwoNorm, 6=Learning
		int features = 0;					// 0=full, 1=topology, 2=content, 3=social
		int keywordNumber = -1;				// -1 = all
		int adjustScore = -1;				// -1 = none
		int adjustSimilarity = -1;			// -1 = none
		int view = -1;						// View mode, -1 = none, 0 = single, 1 = all, 2 = user
		int suggestRank = -1;				// -1 = all
		double alpha = 0.8;					// Structural similarity computation
		double beta = 1;					// Semantic similarity adjustment
		double gamma = 0.5;					// Ratio of baseline (gamma) and classifier (1 - gamma)
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size
		
		DOAJTesting.run(fileSuffix, singleFeature, alpha, beta, gamma, semanticThreshold, newAsParent, method, validAnswerOnly, testingMode, keywordNumber, adjustScore, adjustSimilarity, view, suggestRank, 0, enrichment, features);
	}

	// For simplification
	public static void run(int method) throws Exception {
		boolean singleFeature = false;		// Use 1 feature only
		boolean newAsParent = false;		// Allow new keyword as parent 
		boolean validAnswerOnly = true;		// Test valid answer only
		int testingMode = 2;				// 0=98, 1=pn, 2=doaj
		int adjustScore = -1;				// -1 = none
		int adjustSimilarity = -1;			// -1 = none, 0 = min 0.15, 1 = minmax 1.75
		int keywordNumber = -1;				// -1 = all
		int view = -1;						// View mode, -1 = none, 0 = single-analysis, 1 = all-correct, 2 = all-suggest
		int suggestRank = -1;				// -1 = all
		double alpha = 0.8;					// Structural similarity computation
		double beta = 0.3;					// Semantic similarity adjustment
		double gamma = 0.0;					// Ratio of baseline (gamma) and classifier (1 - gamma)
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size, default = -1
		String fileSuffix = "";
		int features = 0;					// 0=full, 1=topology, 2=content, 3=social
		boolean enrichment = true;			// Use enriched features or not

		run(fileSuffix, singleFeature, alpha, beta, gamma, semanticThreshold, newAsParent, method, validAnswerOnly, testingMode, keywordNumber, adjustScore, adjustSimilarity, view, suggestRank, 1211100, enrichment, features);
	}

	public static String run(String fileSuffix, boolean singleFeature, double alpha, double beta, double gamma, double semanticThreshold, boolean newAsParent, int method, boolean validAnswerOnly, int testingMode, int keywordNumber, int adjustScore, int adjustSimilarity, int view, int suggestRank, int trainingDataSize, boolean enrichmentUsed, int featuresUsed) throws Exception {
		
		// For simplification
		int features = featuresUsed;
		boolean enrichment = enrichmentUsed;
		if(method >= 100) {
			features = method / 10 % 10;
			int enrich = method % 10;
			if(enrich == 0) {
				enrichment = false;
			}
			else {
				enrichment = true;
			}
		}
			
		// Experiment
		double confidenceLevel = 0.95;
		int repeatNumber = 1;

		double[] levelOneAccuracyArray = new double[repeatNumber];
		double[] levelTwoAccuracyArray = new double[repeatNumber];
		double[] levelThreeAccuracyArray = new double[repeatNumber];
		double[] accuracyArray = new double[repeatNumber];
		double[] accuracyLeafArray = new double[repeatNumber];
		double[] accuracyNonLeafArray = new double[repeatNumber];
		double[] meanDistanceArray = new double[repeatNumber];
		double[] meanSimilarityArray = new double[repeatNumber];
		double[] meanRankingArray = new double[repeatNumber];
		double[] rankAUCArray = new double[repeatNumber];
		double[] recommendationRankingArray = new double[repeatNumber];
		
		//System.out.println("Start insertion, repeat = " + repeatNumber);
		System.out.println();
		
		for(int s=0; s<repeatNumber; s++) {

			// Load 91 tree
			ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt", false);
			ACMTreeNode root98 = new ACMTreeNode("ccs98_experiment.txt", false);

			int oldSize = root91.size();
			
			Vector<String> newKeywords = new Vector<String>();
			Vector<String> newNumbers = new Vector<String>();
			if(testingMode == 0) {
				newKeywords = Utility.loadVector("ccs98_keyword.txt");
			}
			else if(testingMode == 1) {
				newKeywords = Utility.loadVector("ccs98_pn100.txt");
				newNumbers = Utility.loadVector("ccs98_pn100_answer_number.txt");				
			}
			else if(testingMode == 2) {
				newKeywords = Utility.loadVector("doaj_100.txt");
				newNumbers = Utility.loadVector("doaj100_answer_number.txt");				
			}
	
			// Load NGD similarities from File
			ACMTreeSimilarity ns = new ACMTreeSimilarity(false);
			
			ns.loadAllNGDSimilarities();
			
			int size = 0;
			if(testingMode == 0) {
				size = ns.getNew98Size();
			}
			else if(testingMode == 1) {
				size = ns.getPn98Size();
			}			
			else if(testingMode == 2) {
				size = ns.getDoaj100Size();
			}			
	
			// Load web page number for original keywords
			root91.loadWebPageNumber(ns.getTree91(), 0);
			
			// Load author number
			AuthorSimilarity as = new AuthorSimilarity(false);
			as.loadAndComputeAuthorSimilarities(true);
			
			// Load word similarity
			WordSimilarity ws = new WordSimilarity(false);
			ws.loadAndComputeWordSimilarities();
			
			// Similarity array, distance array and randomized vector
			boolean[] valid = new boolean[size];
			double[] sim = new double[size];
			double[] dis = new double[size];
			double[] rank = new double[size];
			double[] recommend = new double[size];
			//Vector<Integer> randomVector = Utility.getShuffledVector(size);
			
			int totalNumber = 0;
			int correctNumber = 0;

			int totalLeafNumber = 0;
			int correctLeafNumber = 0;

			int totalNonLeafNumber = 0;
			int correctNonLeafNumber = 0;

			int levelOneCorrectNumber = 0;
			int levelTwoCorrectNumber = 0;
			int levelThreeCorrectNumber = 0;
			
			Vector<ACMTreeNode> predictedParentNodeVector = new Vector<ACMTreeNode>();
			Vector<ACMTreeNode> targetChildNodeVector = new Vector<ACMTreeNode>();
			
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
				
				// Get child name
				String childName = (String)newKeywords.get(index);
				if(keywordNumber > -1) {
					System.out.println("Target keyword = " + childName);
				}
				
				// We might add reordering call here
				
				// Get the node from 98 tree for answer
				ACMTreeNode actualParentNode = new ACMTreeNode(false);
				if(testingMode == 0) {
					actualParentNode = root98.getNodeByName(childName).getParentNode();
					ACMTreeNode actualParentNode91 = root91.getNodeByName(actualParentNode.getClassificationName());
					if(actualParentNode91 != null) {
						actualParentNode = actualParentNode91;
					}
				}
				else if(testingMode == 1 || testingMode == 2) {
					String childNumber = (String)newNumbers.get(index);
					actualParentNode = root91.getNodeByNumber(childNumber);
				}

				int actualIndex = -1;
				if(actualParentNode != null) {
					String actualParentName = actualParentNode.getClassificationName();
					//System.out.println("actualParent = " + actualParentName);
					actualIndex = root91.getActualParentIndex(actualParentName, newAsParent);
					//System.out.println("actualIndex = " + actualIndex);
				}
				
				// Run if the answer can be found
				valid[index] = false;
				
				if(!validAnswerOnly || actualIndex!=-1) {
				
					//System.out.println(childName);
					//System.out.println(childName + "," + actualParentNode.classificationName);
					
					//String lv1 = (String)actualParentNode.getParentNameVector(new Vector()).get(1);
					//System.out.println(lv1);
					
					valid[index] = true;
					totalNumber++;
					
					if(actualParentNode.getClassificationNumber().getIsLeaf()) {
						totalLeafNumber++;
					}
					else {
						totalNonLeafNumber++;
					}
					
					// Load similarities for original and new keywords
					if(testingMode == 0) {
						root91.loadSimilarity(ns.getNew98ToTree91NGD()[index], 0, ns.getNew98ToNew98NGD()[index]);
						root91.adjustSimilarities(beta, adjustSimilarity);
						root91.loadAuthor1(as.getNew98ToTree91Level1()[index], 0);
						root91.loadAuthor2(as.getNew98ToTree91Level2()[index], 0);
						root91.loadAuthor12(as.getNew98ToTree91LevelBoth()[index], 0);
						root91.loadJaccard(ns.getNew98ToTree91Jaccard()[index], 0);
						root91.loadIntersection(ns.getNew98ToTree91()[index], 0);
						root91.loadWord(ws.getNew98ToTree91Word()[index], 0);
					}
					else if(testingMode == 1) {
						root91.loadSimilarity(ns.getPn98ToTree91NGD()[index], 0);
					}
					else if(testingMode == 2) {
						root91.loadSimilarity(ns.getDoaj100ToTree91NGD()[index], 0);
						root91.adjustSimilarities(beta, adjustSimilarity);
						root91.loadAuthor1(as.getDoaj100ToTree91Level1()[index], 0);
						root91.loadAuthor2(as.getDoaj100ToTree91Level2()[index], 0);
						root91.loadAuthor12(as.getDoaj100ToTree91LevelBoth()[index], 0);
						root91.loadJaccard(ns.getDoaj100ToTree91Jaccard()[index], 0);
						root91.loadIntersection(ns.getDoaj100ToTree91()[index], 0);
						root91.loadWord(ws.getDoaj100ToTree91Word()[index], 0);
					}
					
					// Find max similarity node; select first if there are multiple
					double[] scoreArray = new double[0];
					if(method == -1) {			// Random
						scoreArray = root91.getScoreArrayByRandom(newAsParent);
					}
					else if(method == 0) {		// NGD
						scoreArray = root91.getScoreArrayByNGD(newAsParent);
					}
					else if(method == 1) {		// LibLinear
						scoreArray = root91.getScoreArrayByLibLinear("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method);
					}
					else if(method == 2) {		// Score linear ensemble
						scoreArray = root91.getScoreArrayByLinearScoreEnsemble("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, gamma, method);
					}
					else if(method == 3) {		// Rank linear ensemble
						scoreArray = root91.getScoreArrayByLinearRankEnsemble("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, gamma, method);
					}
					else if(method == 4) {		// Score multiply ensemble
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsemble("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method);
					}
					else if(method == 5) {		// Rank multiply ensemble
						scoreArray = root91.getScoreArrayByMultiplyRankEnsemble("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method);
					}
					else if(method == 6 || method == 7 || method == 8 || method == 12 || method == 100 || method == 101 || method == 110 || method == 120 || method == 121 || method == 130 || method == 131) {		// Weka classification
						scoreArray = root91.getScoreArrayByWekaClassification("model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 9) {		// Score multiply ensemble, LL + NB
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNB("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 10) {		// Score multiply ensemble, NGD + NB
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleNGDNB("model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 11) {		// Rank linear ensemble, NGD + NB
						scoreArray = root91.getScoreArrayByLinearRankEnsembleNGDNB("model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, gamma, method, enrichment, features);
					}
					else if(method == 13) {		// LibSVC
						scoreArray = root91.getScoreArrayByLibSVC("model_classification_" + fileSuffix + ".txt", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method);
					}
					else if(method == 14) {		// Author 1
						scoreArray = root91.getScoreArrayByAuthor1(newAsParent);
					}
					else if(method == 15) {		// Score multiply ensemble, LL + NB + NGD
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBNGD("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 16) {		// Jaccard
						scoreArray = root91.getScoreArrayByJaccard(newAsParent);
					}
					else if(method == 17) {		// Author 2
						scoreArray = root91.getScoreArrayByAuthor2(newAsParent);
					}
					else if(method == 18) {		// Author 12
						scoreArray = root91.getScoreArrayByAuthor12(newAsParent);
					}
					else if(method == 19) {		// Score multiply ensemble, LL + NB + Jaccard
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBJaccard("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 20) {		// Score multiply ensemble, LL + NB + NGD + Jaccard
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccard("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 21) {		// Intersection
						scoreArray = root91.getScoreArrayByIntersection(newAsParent);
					}
					else if(method == 22) {		// Score multiply ensemble, LL + NB + Jaccard + Intersection
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersection("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 23) {		// Score multiply ensemble, LL + NB + NGD + Jaccard + Intersection
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardIntersection("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 24) {		// Score multiply ensemble, LL + NB + Jaccard + Intersection + Author
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersectionAuthor("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 25) {		// Score multiply ensemble, LL + NB + NGD + Jaccard + Author
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardAuthor("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 26) {		// Word
						scoreArray = root91.getScoreArrayByWord(newAsParent);
					}
					else if(method == 27) {		// Score multiply ensemble, LL + NB + Jaccard + Intersection + Word
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersectionWord("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 28) {		// Score multiply ensemble, LL + NB + NGD + Jaccard + Word
						scoreArray = root91.getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardWord("model_classification_" + fileSuffix + ".txt", "model_classification_" + fileSuffix + ".model", index, ns.getNew98()[index], singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
					}
					else if(method == 29) {		// OneNorm, combine six baseline methods
						scoreArray = root91.getScoreArrayByOneNorm(newAsParent);
					}
					else if(method == 30) {		// TwoNorm, combine six baseline methods
						scoreArray = root91.getScoreArrayByTwoNorm(newAsParent);
					}
					else if(method == 31) {		// Parent level
						scoreArray = root91.getScoreArrayByParentLevel(newAsParent);
					}
					else if(method == 32) {		// Sibling count
						scoreArray = root91.getScoreArrayBySiblingCount(newAsParent);
					}
					else if(method == 33) {		// Children count
						scoreArray = root91.getScoreArrayByChildrenCount(newAsParent);
					}
					else if(method == 34) {		// Parent web page number
						scoreArray = root91.getScoreArrayByParentWebPageNumber(newAsParent);
					}
					else if(method == 35) {		// OneNorm, topology
						scoreArray = root91.getScoreArrayByOneNormTopology(newAsParent);
					}
					else if(method == 36) {		// OneNorm, content
						scoreArray = root91.getScoreArrayByOneNormContent(newAsParent);
					}
					else if(method == 37) {		// OneNorm, social
						scoreArray = root91.getScoreArrayByOneNormSocial(newAsParent);
					}
					
					//Utility.printDoubleArray(scoreArray, scoreArray.length);					
					
				    // Set score and rank by the score array
				    root91.setScoresAndRanks(scoreArray, newAsParent);
					
					// Adjust scores here: try different methods
					switch(adjustScore) {
						case 0: root91.adjustScoreByChildAverageBottomUp(beta); break;
						case 1: root91.adjustScoreByChildAverageTopDown(beta); break;
						case 2: root91.adjustScoreByChildMaximumBottomUp(beta); break;
						case 3: root91.adjustScoreByChildMaximumTopDown(beta); break;
						case 4: root91.addScoreByChildAverageBottomUp(beta); break;
						case 5: root91.addScoreByChildAverageTopDown(beta); break;
						case 6: root91.addScoreByChildMaximumBottomUp(beta); break;
						case 7: root91.addScoreByChildMaximumTopDown(beta); break;
						case 8: root91.addScoreByChildAllTopDown(beta); break;
						case 9: root91.addScoreByChildAllBottomUp(beta); break;
						case 10: root91.addScoreByChildMinimumBottomUp(beta); break;
						case 11: root91.addScoreByChildMinimumTopDown(beta); break;
						case 12: root91.addScoreByChildMinMaxTopDown(beta); break;
						case 13: root91.addScoreByChildMaxMinTopDown(beta); break;
						default: break;
					}
					
					root91.setScore(0);
					scoreArray = root91.getScores(newAsParent);
					//Utility.printDoubleArray(scoreArray, 5);
					root91.setScoresAndRanks(scoreArray, newAsParent);
					
					ACMTreeNode predictedParentNode = root91.findPredictedParentNode(scoreArray, newAsParent, actualIndex);
					//System.out.println("predictedParent=" + predictedParentNode.getClassificationName());
					//System.out.println();
									
					// Compute structural similarity, structural distance
					sim[index] = root91.computeStructuralSimilarity(predictedParentNode, actualParentNode, childName, alpha);
					dis[index] = root91.computeStructuralDistance(predictedParentNode, actualParentNode, childName);
	
					// Compute recommendation ranking
					if(method == -1) {
						recommend[index] = root91.getRandomRecommendationRank(actualParentNode);
					}
					else {
						recommend[index] = root91.getRecommendationRank(actualParentNode, null);
					}

					// Create new node as leaf
					ACMTreeNode child = new ACMTreeNode(false);
					if(testingMode == 0) {
						child = predictedParentNode.createNewChildNodeAsLeaf(childName, index, ns.getNew98()[index], false);
					}
					else if(testingMode == 1) {
						child = predictedParentNode.createNewChildNodeAsLeaf(childName, index, ns.getPn98()[index], false);
					}
					else if(testingMode == 2) {
						child = predictedParentNode.createNewChildNodeAsLeaf(childName, index, ns.getDoaj100()[index], false);
					}

					rank[index] = root91.getAnswerRanking();
					child.setAnswerRanking(rank[index]);
					child.setStructuralDistance(dis[index]);
					child.setStructuralSimilarity(sim[index]);
					child.setScore(predictedParentNode.getScore());
					
					if(newAsParent) {
						predictedParentNode.insertChildNode(child);
					}
					predictedParentNodeVector.add(predictedParentNode);
					targetChildNodeVector.add(child);

					// Compute level-based accuracy
					int commonLevel = root91.computeCommonLevel(predictedParentNode, actualParentNode, childName);
					
					if(commonLevel > 1) {					// First level
						levelOneCorrectNumber++;
						child.setLevelOneCorrect(true);
						
						if(commonLevel > 2) {				// Second level
							levelTwoCorrectNumber++;
							child.setLevelTwoCorrect(true);
							
							if(commonLevel > 3) {			// Third level
								levelThreeCorrectNumber++;
								child.setLevelThreeCorrect(true);
							}
						}
					}
	
					// Compute accuracy
					//System.out.println(sim[index]);
					if(sim[index] == 1.0) {
						correctNumber++;					
						child.setCorrect(true);
						//System.out.println(child.getClassificationName());

						if(actualParentNode.getClassificationNumber().getIsLeaf()) {
							correctLeafNumber++;
						}
						else {
							correctNonLeafNumber++;
						}
					}
						
					// Display names and similarity
					/*
					String predictedParentName = predictedParentNode.getClassificationName();
					System.out.println(childName + ", " + predictedParentName + ", " + actualParentName + ", " + sim[i] + ", " + dis[i]);					
					root91.printSimilarities();
					*/

					// Output tree for view
					if(view == 0 || view == 2) {
						root91.resetType();
						root91.computeType(predictedParentNode, actualParentNode);
						root91.outputXMLForPrefuse("view/view_" + fileSuffix + "k" + (index + 1) + ".xml", childName, suggestRank);
					}
					else if (view == 1) {
						root91.computeCorrectNumber(predictedParentNode, actualParentNode);
					}
					System.out.print("o");
				}
				else {
					System.out.print(".");					
				}
				if(((i+1)%(size/5))==0) {
					System.out.println();
				}

			}
			
			if(!newAsParent) {
				for(int i=0; i<predictedParentNodeVector.size(); i++) {
					ACMTreeNode parent = (ACMTreeNode)predictedParentNodeVector.get(i);
					ACMTreeNode child = (ACMTreeNode)targetChildNodeVector.get(i);
					parent.insertChildNode(child);
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
			levelTwoAccuracyArray[s] = (double)levelTwoCorrectNumber / (double)totalNumber;
			levelThreeAccuracyArray[s] = (double)levelThreeCorrectNumber / (double)totalNumber;
			accuracyArray[s] = (double)correctNumber / (double)totalNumber;
			accuracyLeafArray[s] = (double)correctLeafNumber / (double)totalLeafNumber;
			accuracyNonLeafArray[s] = (double)correctNonLeafNumber / (double)totalNonLeafNumber;
			
			meanDistanceArray[s] = Utility.computeAndPrintStatistics("dis", Utility.getValidData(dis, valid), confidenceLevel, totalNumber);
			meanSimilarityArray[s] = Utility.computeAndPrintStatistics("sim", Utility.getValidData(sim, valid), confidenceLevel, totalNumber);
			meanRankingArray[s] = Utility.computeAndPrintStatistics("rank", Utility.getValidData(rank, valid), confidenceLevel, totalNumber);
			recommendationRankingArray[s] = Utility.computeAndPrintStatistics("recommend", Utility.getValidData(recommend, valid), confidenceLevel, totalNumber);

			rankAUCArray[s] = root91.computeRankBasedAUC(targetChildNodeVector, oldSize);

			System.out.println();
			System.out.println("Done, valid keyword number = " + totalNumber + ", leaf = " + totalLeafNumber + ", nonleaf = " + totalNonLeafNumber);
			//System.out.println(correctNumber + ", " + totalNumber);
			//System.out.println(s + ", " + accuracyArray[s] + ", " + meanDistanceArray[s] + ", " + meanSimilarityArray[s]);
			
			//root91.printSimilarities();
			//root91.printAll();
			//root91.printTesting(0);
			
			root91.logAll("final/final_" + fileSuffix + ".txt", false);
			root91.logLevelOne("lv1/lv1_" + fileSuffix + ".txt", false);
			root91.logAnalysisForExcel("analysis/analysis_" + fileSuffix + ".csv", targetChildNodeVector);			

			if(view == 1) {
				root91.setCorrectNumber(54);
				root91.outputXMLForPrefuse("view/view_" + fileSuffix + ".xml", null, -1);
			}
		}

		// Compute statistical measurements
		System.out.println();
		double lv1 = Utility.computeAndPrintStatistics("Level 1 Accuracy", levelOneAccuracyArray, confidenceLevel, repeatNumber);
		double lv2 = Utility.computeAndPrintStatistics("Level 2 Accuracy", levelTwoAccuracyArray, confidenceLevel, repeatNumber);
		double lv3 = Utility.computeAndPrintStatistics("Level 3 Accuracy", levelThreeAccuracyArray, confidenceLevel, repeatNumber);
		double total = Utility.computeAndPrintStatistics("Total Accuracy", accuracyArray, confidenceLevel, repeatNumber);
		double leaf = Utility.computeAndPrintStatistics("Leaf Accuracy", accuracyLeafArray, confidenceLevel, repeatNumber);
		double nonleaf = Utility.computeAndPrintStatistics("Non Leaf Accuracy", accuracyNonLeafArray, confidenceLevel, repeatNumber);
		double dis = Utility.computeAndPrintStatistics("Distance", meanDistanceArray, confidenceLevel, repeatNumber);
		double sim = Utility.computeAndPrintStatistics("Similarity", meanSimilarityArray, confidenceLevel, repeatNumber);
		double rank = Utility.computeAndPrintStatistics("Ranking", meanRankingArray, confidenceLevel, repeatNumber);
		double rankAUC = Utility.computeAndPrintStatistics("RankAUC", rankAUCArray, confidenceLevel, repeatNumber);
		double recommend = Utility.computeAndPrintStatistics("Recommendation Ranking", recommendationRankingArray, confidenceLevel, repeatNumber);

		PrintWriter pw = new PrintWriter("result/result_" + fileSuffix + ".txt");
		pw.print(trainingDataSize);
		pw.print(", " + String.format("%.4f", lv1));
		pw.print(", " + String.format("%.4f", lv2));
		pw.print(", " + String.format("%.4f", lv3));
		pw.print(", " + String.format("%.4f", total));
		pw.print(", " + String.format("%.4f", leaf));
		pw.print(", " + String.format("%.4f", nonleaf));
		pw.print(", " + String.format("%.4f", dis));
		pw.print(", " + String.format("%.4f", sim));
		pw.print(", " + String.format("%.4f", rank));
		pw.print(", " + String.format("%.4f", rankAUC));
		pw.print(", " + String.format("%.4f", recommend));
		pw.println();
		pw.close();

		String resultString = "";
		resultString += method; 
		resultString += "," + features;
		resultString += "," + enrichment;
		resultString += "," + total;
		resultString += "," + sim;
		resultString += "," + rank;
		resultString += "," + rankAUC;
		resultString += "," + recommend;
		
		return resultString;

		// Backup //////////////////////////////////////////////////////
		
		// Load similarities
		/*
		ACMTreeNode root91 = new ACMTreeNode();
		root91.loadACMTreeFromFile("ccs91_experiment.txt");
		
		//root91.loadSimilarityFromFile("YahooNGDSim_3D Stereo Scene Analysis.txt");
		//root91.loadSimilarityFromFile("YahooNGDSim_Distributed Objects.txt");
		root91.loadSimilarityFromFile("YahooNGDSim_Quadratic Programming Methods.txt");
		
		root91.adjustSimilarityByChildMaximum(0.5);
		root91.printSimilarities(1);
		*/
		
		// Show preprocessed keywords in 91 (adjusted)
		/*
		ACMTreeNode root91 = new ACMTreeNode();
		root91.loadAndAdjustACMTree("ccs91_experiment.txt");
		Vector names = root91.getPreprocessedNameVector(new Vector());
		//Vector names = root91.getPreprocessedSubjectDescriptorNames(new Vector());
		//Vector names = root91.getPreprocessedMainClassNames(new Vector());
		//Vector names = root91.getRepeatedKeywords(new Vector());
		Utility.printStringVector(names);
		*/
				
		// Find new (leaf / subject descriptor) keywords in 98
		/*
		ACMTreeNode root98 = new ACMTreeNode();
		root98.loadAndAdjustACMTree("ccs98_experiment.txt");
		
		ACMTreeNode root91 = new ACMTreeNode();
		root91.loadAndAdjustACMTree("ccs91_experiment.txt");
		
		Vector name91 = root91.getPreprocessedNameVector(new Vector());
		Vector newKeywords = root98.findNewKeywords(name91, new Vector());
		//Vector newLeafKeywords = root98.getNewLeafKeywords(new Vector());		
		//Vector newSubjectDescriptorKeywords = root98.getNewSubjectDescriptorKeywords(new Vector());
		Utility.printStringVector(newKeywords);
		//root98.printNewForExcel();
		*/
		
		// Find deleted keywords in 98
		/*
		ACMTreeNode root98 = new ACMTreeNode();
		root98.loadAndAdjustACMTree("ccs98_experiment.txt");
		Vector deletedKeywords = root98.getDeletedKeywords(new Vector()); 
		Utility.printStringVector(deletedKeywords);
		*/
		
		// Find replaced (new and deleted) keywords in 98
		/*
		ACMTreeNode root98 = new ACMTreeNode();
		root98.loadAndAdjustACMTree("ccs98_experiment.txt");
		
		ACMTreeNode root91 = new ACMTreeNode();
		root91.loadAndAdjustACMTree("ccs91_experiment.txt");
		Vector name91 = root91.getPreprocessedNameVector(new Vector());
		
		Vector newKeywords = root98.findNewKeywords(name91, new Vector()); 
		Vector deletedKeywords = root98.getDeletedKeywords(new Vector()); 

		for(int i=0; i<newKeywords.size(); i++) {
			String newKeyword = (String)newKeywords.get(i);			
			for(int j=0; j<deletedKeywords.size(); j++) {
				String deletedKeyword = (String)deletedKeywords.get(j);
				if(newKeyword.equalsIgnoreCase(deletedKeyword)) {
					System.out.println(newKeyword);
					break;
				}
			}
		}
		*/
		
		// Insertion Test
		/*
		ACMTreeNode childNode = new ACMTreeNode();
		childNode.setClassificationName("Tim Test");
		childNode.setClassificationNumber(new ACMTreeClassificationNumber());
		childNode.getClassificationNumber().setIsLeaf(true);		
		ACMTreeNode firstChild = (ACMTreeNode)root98.childNode.get(0);
		ACMTreeNode secondChild = (ACMTreeNode)firstChild.childNode.get(0);
		firstChild.insertChildNodeBetween(childNode, secondChild);
		*/
	}
}
