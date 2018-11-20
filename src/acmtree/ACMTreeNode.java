package acmtree;
/**
 * 
 */

import java.util.*;
import java.io.*;
import liblinear.*;
import libsvm.*;
import utility.*;
import interpro.*;
import weka.core.*;
import weka.classifiers.meta.*;


/**
 * @author Tim Kuo
 *
 */
public class ACMTreeNode {
		
	// Tree
	ACMTreeNode parentNode = null;
	Vector<ACMTreeNode> childNode = new Vector<ACMTreeNode>();

	// Parameters
	ACMTreeClassificationNumber classificationNumber = null;
	
	String classificationName = "Root";
	String parenthesis = "";
	
	Vector<String> possibleKeywords = new Vector<String>();
	Vector<String> isRelatedTo = new Vector<String>();
	Vector<String> example = new Vector<String>();
	
	boolean isNew = false;
	boolean isUnique = true;
	boolean isGeneral = false;
	boolean isMiscellaneous = false;
	boolean isRetired1991 = false;
	boolean isRetired1998 = false;
	boolean isLevelOneCorrect = false;
	boolean isLevelTwoCorrect = false;
	boolean isLevelThreeCorrect = false;
	boolean isCorrect = false;
	boolean isAnswer = false;
	boolean isInterpro = false;
	
	double similarity = 0;				// NGD
	double webPageNumber = 0;			// log
	double structuralDistance = 0;		// For result
	double structuralSimilarity = 0;	// For result
	double answerRanking = -1;			// Actual ranking of the answer
	double score = 0;					// Final score
	double rank = -1;					// Final rank
	double jaccard = 0;					// Jaccard
	double author1 = 0;					// Default = 0
	double author2 = 0;					// Default = 0
	double author12 = 0;				// Default = 0
	double intersection = 0;			// Default = 0
	double word = 0;					// Default = 0

	int insertedKeywordIndex = -1;		// If this is inserted keyword, this value is not -1
	int type = 0;						// 0 = n/a, 1 = answer child / parent, 2 = predicted child / parent, 3 = answer + predict child / parent
	int correctNumber = 0;				// Log how many correct times
	int levelOneClassNumber = 11;		// 0 - 10

	public ACMTreeNode(boolean isInterpro) {
		super();
		this.isInterpro = isInterpro;
	}

	public ACMTreeNode(String fileName, boolean isInterpro) throws Exception {
		super();
		this.isInterpro = isInterpro;
		if(isInterpro) {
			this.loadAndAdjustInterproTree(fileName);
		}
		else {
			this.loadAndAdjustACMTree(fileName, isInterpro);
		}
	}

	public int[] getLevelNodes(int height) {
		int[] levelNodes = new int[height];
		return this.getLevelNodes(height, 0, levelNodes);
	}

	public int[] getLevelNodes(int height, int currentHeight, int[] levelNodes) {
		for(ACMTreeNode child : this.getChildNode()) {
			if(child.isValid()) {
				levelNodes[currentHeight] ++;
			}
		}
		if(currentHeight < height - 1) {
			for(ACMTreeNode child : this.getChildNode()) {
				levelNodes = child.getLevelNodes(height, currentHeight+1, levelNodes);
			}
		}
		return levelNodes;
	}
	
	public double[] getChildScoreArray() {
		Vector<Double> scoreVector = new Vector<Double>();
		for(ACMTreeNode child : this.getChildNode()) {
			if(child.isValid()) {
				scoreVector.add(child.getScore());
			}
		}	
		return Utility.doubleVectorToDoubleArray(scoreVector);
	}
	
	public int getChildNodeIndex(String childName) {
		int index = 0;
		for(ACMTreeNode child : this.getChildNode()) {
			if(child.isValid()) {
				if(child.getClassificationName().equalsIgnoreCase(childName)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
	
	public double getRecommendationRank(ACMTreeNode parentNode, PrintWriter pw) throws Exception {
		double rank = 0.0;
		String name = parentNode.getClassificationName();
		ACMTreeNode node = parentNode.getParentNode();
		while(node != null) {
			double[] scoreArray = node.getChildScoreArray();
			double[] rankArray = Utility.scoreArrayToRankArrayDescending(scoreArray);
			int index = node.getChildNodeIndex(name);
			rank += (rankArray[index] + 1);
			if(pw != null) {
				pw.print("scoreArray=");
				Utility.printDoubleArrayForFile(scoreArray, scoreArray.length, pw);
				pw.print(",rankArray=");
				Utility.printDoubleArrayForFile(rankArray, rankArray.length, pw);
				pw.print(",index=" + index);
				pw.print(",rankIndex=" + rankArray[index]);
				pw.print(",rank=" + rank);
				pw.print(" | ");
			}
			name = node.getClassificationName();
			node = node.getParentNode();
		}
		return rank;
	}
	
	public double getRandomRecommendationRank(ACMTreeNode parentNode) {
		double rank = 0.0;
		ACMTreeNode node = parentNode.getParentNode();
		while(node != null) {
			rank += (node.getChildNode().size()/2);
			node = node.getParentNode();
		}
		return rank;
	}


	/**
	 * Load Interpro Tree from file and adjust it
	 * @param fileName the file name to be loaded
	 * @return unique keywords
	 */
	public Vector<String> loadAndAdjustInterproTree(String fileName) throws IOException {
		loadInterproTreeFromFile(fileName);
		Vector<String> uniqueKeywords = findUniqueKeywords(new Vector<String>(), this);
		adjustRepeatedName();
		return uniqueKeywords;
	}
	
	/**
	 * Load Interpro Tree from file, only called by root
	 * @param fileName the file name to be loaded
	 */
	public void loadInterproTreeFromFile(String fileName) throws IOException {
		Vector<ACMTreeNode> result = new Vector<ACMTreeNode>();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		while ((s=lnr.readLine()) != null) {
			ACMTreeNode n = this.createNode(s, true);
			result.add(n);
		}
		
		loadTree(result);
		
		lnr.close();
		fr.close();
	}
	
	/**
	 * Create node by its line
	 * @param childLine the line of child
	 * @param currentLevel current loading level
	 */
	public ACMTreeNode createNode(String childLine, boolean isInterpro) {
		ACMTreeNode childNode = new ACMTreeNode(isInterpro);
		childNode.parseLine(childLine);
		return childNode;
	}
	
	/**
	 * Parse a line in Interpro Tree
	 * @param line the line to be parsed
	 * @param isLevelOne if this is level one, set level to 1; otherwise the level must be recalculated
	 * @param previousNumber if this is level four, we need previous number to get this number
	 */
	public void parseLine(String line) {
		this.classificationNumber = new ACMTreeClassificationNumber();
		int intentIndex = Proteins.getIndentIndex(line);
		if(intentIndex > -1) {
			this.classificationNumber.classificationLevel = intentIndex/2;
		}
		this.classificationName = line.substring(intentIndex, line.length());
	}
	
	/**
	 * Generate data instance for a (parent, target) pair
	 * We use "parent" as predicted parent node,
	 * "parent of target" as actual parent node.
	 */
	public ACMTreeDataInstance generateDataInstance(ACMTreeNode parent, double targetWeb) {
		
		ACMTreeNode grandparent = parent.getParentNode();
		Vector<ACMTreeNode> sibling = grandparent.getChildNode();
		Vector<ACMTreeNode> children = parent.getChildNode();
		
		ACMTreeDataInstance di = new ACMTreeDataInstance();
		
		di.targetWebPageNumber = targetWeb;		// Feature 12
		di.parentLevel = parent.getClassificationNumber().getClassificationLevel();	// Feature 1
		
		di.parentWebPageNumber = parent.getWebPageNumber();	// Feature 2
		di.parentAndTargetSimilarity = parent.getSimilarity();	// Feature 13
		di.parentAndTargetAuthorLevel1 = parent.getAuthor1();	// Feature 21
		di.parentAndTargetJaccard = parent.getJaccard();	// Feature 29
		di.parentAndTargetIntersection = parent.getIntersection();	// Feature 37
		di.parentAndTargetAuthorLevel12 = parent.getAuthor12();			// Feature 45
		di.parentAndTargetWord = parent.getWord();	// Feature 53		
		
		if(grandparent != null) {
			di.grandparentWebPageNumber = grandparent.getWebPageNumber();	// Feature 5
			di.grandparentAndTargetSimilarity = grandparent.getSimilarity();	// Feature 14
			di.grandparentAndTargetAuthorLevel1 = grandparent.getAuthor1();	// Feature 22
			di.grandparentAndTargetJaccard = grandparent.getJaccard();	// Feature 30
			di.grandparentAndTargetIntersection = grandparent.getIntersection();	// Feature 38
			di.grandparentAndTargetAuthorLevel12 = grandparent.getAuthor12();		// Feature 46
			di.grandparentAndTargetWord = grandparent.getWord();	// Feature 54
		}
		
		if(sibling.size() > 0) {
			
			di.siblingCount = sibling.size();	// Feature 3
			
			double siblingWebSum = 0;
			double siblingWebMax = 0;
			double siblingWebMin = 0;
			
			double siblingSimSum = 0;
			double siblingSimMax = 0;
			double siblingSimMin = 0;
			
			double siblingAut1Sum = 0;
			double siblingAut1Max = 0;
			double siblingAut1Min = 0;

			double siblingJacSum = 0;
			double siblingJacMax = 0;
			double siblingJacMin = 0;

			double siblingIntSum = 0;
			double siblingIntMax = 0;
			double siblingIntMin = 0;

			double siblingAut12Sum = 0;
			double siblingAut12Max = 0;
			double siblingAut12Min = 0;

			double siblingWordSum = 0;
			double siblingWordMax = 0;
			double siblingWordMin = 0;
			
			for(int i=0; i<sibling.size(); i++) {
				
				ACMTreeNode sib = sibling.get(i);
				
				//if(sib.isValid()) {
					double webPageNumber = sib.getWebPageNumber();
					double similarity = sib.getSimilarity();
					double authorLevel1 = sib.getAuthor1();
					double jaccard = sib.getJaccard();
					double intersection = sib.getIntersection();
					double authorLevel12 = sib.getAuthor12();
					double word = sib.getWord();
					
					siblingWebSum += webPageNumber;
					siblingSimSum += similarity;
					siblingAut1Sum += authorLevel1;
					siblingJacSum += jaccard;
					siblingIntSum += intersection;
					siblingAut12Sum += authorLevel12;
					siblingWordSum += word;
					
					if(siblingWebMax == 0 || siblingWebMax < webPageNumber) {
						siblingWebMax = webPageNumber;
					}
	
					//if(siblingWebMin == 0 || (webPageNumber>0 && siblingWebMin>webPageNumber)) {
					if(siblingWebMin == 0 || siblingWebMin > webPageNumber) {
						siblingWebMin = webPageNumber;
					}
	
					if(siblingSimMax == 0 || siblingSimMax < similarity) {
						siblingSimMax = similarity;
					}
	
					//if(siblingSimMin == 0 || (similarity>0 && siblingSimMin>similarity)) {
					if(siblingSimMin == 0 || siblingSimMin > similarity) {
						siblingSimMin = similarity;
					}
					
					if(siblingAut1Max == 0 || siblingAut1Max < authorLevel1) {
						siblingAut1Max = authorLevel1;
					}
	
					//if(siblingAut1Min == 0 || (authorLevel1>0 && siblingAut1Min>authorLevel1)) {
					if(siblingAut1Min == 0 || siblingAut1Min > authorLevel1) {
						siblingAut1Min = authorLevel1;
					}
					
					if(siblingJacMax == 0 || siblingJacMax < jaccard) {
						siblingJacMax = jaccard;
					}
	
					//if(siblingJacMin == 0 || (jaccard>0 && siblingJacMin>jaccard)) {
					if(siblingJacMin == 0 || siblingJacMin > jaccard) {
						siblingJacMin = jaccard;
					}

					if(siblingIntMax == 0 || siblingIntMax < intersection) {
						siblingIntMax = intersection;
					}
	
					//if(siblingIntMin == 0 || (intersection>0 && siblingIntMin>intersection)) {
					if(siblingIntMin == 0 || siblingIntMin > intersection) {
						siblingIntMin = intersection;
					}
					
					if(siblingAut12Max == 0 || siblingAut12Max < authorLevel12) {
						siblingAut12Max = authorLevel12;
					}
	
					//if(siblingAut12Min == 0 || (authorLevel12>0 && siblingAut12Min>authorLevel12)) {
					if(siblingAut12Min == 0 || siblingAut12Min > authorLevel12) {
						siblingAut12Min = authorLevel12;
					}
					
					if(siblingWordMax == 0 || siblingWordMax < word) {
						siblingWordMax = word;
					}
	
					//if(siblingWordMin == 0 || (word>0 && siblingWordMin>word)) {
					if(siblingWordMin == 0 || siblingWordMin > word) {
						siblingWordMin = word;
					}
				//}
			}
			
			di.siblingWebPageNumberAverage = (double)siblingWebSum / (double)sibling.size();	// Feature 6
			di.siblingWebPageNumberMaximum = siblingWebMax;	// Feature 7
			di.siblingWebPageNumberMinimum = siblingWebMin;	// Feature 8
					
			di.siblingAndTargetSimilarityAverage = (double)siblingSimSum / (double)sibling.size();		// Feature 15
			di.siblingAndTargetSimilarityMaximum = siblingSimMax;	// Feature 16
			di.siblingAndTargetSimilarityMinimum = siblingSimMin;	// Feature 17

			di.siblingAndTargetAuthorLevel1Average = (double)siblingAut1Sum / (double)sibling.size();		// Feature 23
			di.siblingAndTargetAuthorLevel1Maximum = siblingAut1Max;	// Feature 24
			di.siblingAndTargetAuthorLevel1Minimum = siblingAut1Min;	// Feature 25

			di.siblingAndTargetJaccardAverage = (double)siblingJacSum / (double)sibling.size();		// Feature 31
			di.siblingAndTargetJaccardMaximum = siblingJacMax;	// Feature 32
			di.siblingAndTargetJaccardMinimum = siblingJacMin;	// Feature 33

			di.siblingAndTargetIntersectionAverage = (double)siblingIntSum / (double)sibling.size();	// Feature 39
			di.siblingAndTargetIntersectionMaximum = siblingIntMax;		// Feature 40
			di.siblingAndTargetIntersectionMinimum = siblingIntMin;		// Feature 41

			di.siblingAndTargetAuthorLevel12Average = (double)siblingAut12Sum / (double)sibling.size();	// Feature 47
			di.siblingAndTargetAuthorLevel12Maximum = siblingAut12Max;	// Feature 48
			di.siblingAndTargetAuthorLevel12Minimum = siblingAut12Min;	// Feature 49

			di.siblingAndTargetWordAverage = (double)siblingWordSum / (double)sibling.size();	// Feature 55
			di.siblingAndTargetWordMaximum = siblingWordMax;	// Feature 56
			di.siblingAndTargetWordMinimum = siblingWordMin;	// Feature 57
		}
		
		if(children.size() > 0) {
			
			di.childrenCount = children.size();	// Feature 4

			double childrenWebSum = 0;
			double childrenWebMax = 0;
			double childrenWebMin = 0;
			
			double childrenSimSum = 0;
			double childrenSimMax = 0;
			double childrenSimMin = 0;

			double childrenAut1Sum = 0;
			double childrenAut1Max = 0;
			double childrenAut1Min = 0;

			double childrenJacSum = 0;
			double childrenJacMax = 0;
			double childrenJacMin = 0;

			double childrenIntSum = 0;
			double childrenIntMax = 0;
			double childrenIntMin = 0;

			double childrenAut12Sum = 0;
			double childrenAut12Max = 0;
			double childrenAut12Min = 0;

			double childrenWordSum = 0;
			double childrenWordMax = 0;
			double childrenWordMin = 0;
			
			for(int i=0; i<children.size(); i++) {
				ACMTreeNode child = children.get(i);
				
				//if(child.isValid()) {
					double webPageNumber = child.getWebPageNumber();
					double similarity = child.getSimilarity();
					double authorLevel1 = child.getAuthor1();
					double jaccard = child.getJaccard();
					double intersection = child.getIntersection();
					double authorLevel12 = child.getAuthor12();
					double word = child.getWord();
					
					childrenWebSum += webPageNumber;
					childrenSimSum += similarity;
					childrenAut1Sum += authorLevel1;
					childrenJacSum += jaccard;
					childrenIntSum += intersection;
					childrenAut12Sum += authorLevel12;
					childrenWordSum += word;
					
					if(childrenWebMax == 0 || childrenWebMax < webPageNumber) {
						childrenWebMax = webPageNumber;
					}
	
					//if(childrenWebMin == 0 || (webPageNumber>0 && childrenWebMin>webPageNumber)) {
					if(childrenWebMin == 0 || childrenWebMin > webPageNumber) {
						childrenWebMin = webPageNumber;
					}
	
					if(childrenSimMax == 0 || childrenSimMax < similarity) {
						childrenSimMax = similarity;
					}
	
					//if(childrenSimMin == 0 || (similarity>0 && childrenSimMin>similarity)) {
					if(childrenSimMin == 0 || childrenSimMin > similarity) {
						childrenSimMin = similarity;
					}
					
					if(childrenAut1Max == 0 || childrenAut1Max < authorLevel1) {
						childrenAut1Max = authorLevel1;
					}
	
					//if(childrenAut1Min == 0 || (authorLevel1>0 && childrenAut1Min>authorLevel1)) {
					if(childrenAut1Min == 0 || childrenAut1Min > authorLevel1) {
						childrenAut1Min = authorLevel1;
					}
					
					if(childrenJacMax == 0 || childrenJacMax < jaccard) {
						childrenJacMax = jaccard;
					}
	
					//if(childrenJacMin == 0 || (jaccard>0 && childrenJacMin>jaccard)) {
					if(childrenJacMin == 0 || childrenJacMin > jaccard) {
						childrenJacMin = jaccard;
					}

					if(childrenIntMax == 0 || childrenIntMax < intersection) {
						childrenIntMax = intersection;
					}
	
					//if(childrenIntMin == 0 || (intersection>0 && childrenIntMin>intersection)) {
					if(childrenIntMin == 0 || childrenIntMin > intersection) {
						childrenIntMin = intersection;
					}

					if(childrenAut12Max == 0 || childrenAut12Max < authorLevel12) {
						childrenAut12Max = authorLevel12;
					}
	
					//if(childrenAut12Min == 0 || (authorLevel12>0 && childrenAut12Min>authorLevel12)) {
					if(childrenAut12Min == 0 || childrenAut12Min > authorLevel12) {
						childrenAut12Min = authorLevel12;
					}
					
					if(childrenWordMax == 0 || childrenWordMax < word) {
						childrenWordMax = word;
					}
	
					//if(childrenWordMin == 0 || (word>0 && childrenWordMin>word)) {
					if(childrenWordMin == 0 || childrenWordMin > word) {
						childrenWordMin = word;
					}
				//}
			}
			
			di.childrenWebPageNumberAverage = (double)childrenWebSum / (double)children.size();	// Feature 9
			di.childrenWebPageNumberMaximum = childrenWebMax;	// Feature 10
			di.childrenWebPageNumberMinimum = childrenWebMin;	// Feature 11
					
			di.childrenAndTargetSimilarityAverage = (double)childrenSimSum / (double)children.size();		// Feature 18
			di.childrenAndTargetSimilarityMaximum = childrenSimMax;		// Feature 19
			di.childrenAndTargetSimilarityMinimum = childrenSimMin;		// Feature 20

			di.childrenAndTargetAuthorLevel1Average = (double)childrenAut1Sum / (double)children.size();		// Feature 26
			di.childrenAndTargetAuthorLevel1Maximum = childrenAut1Max;		// Feature 27
			di.childrenAndTargetAuthorLevel1Minimum = childrenAut1Min;		// Feature 28

			di.childrenAndTargetJaccardAverage = (double)childrenJacSum / (double)children.size();		// Feature 34
			di.childrenAndTargetJaccardMaximum = childrenJacMax;	// Feature 35
			di.childrenAndTargetJaccardMinimum = childrenJacMin;	// Feature 36

			di.childrenAndTargetIntersectionAverage = (double)childrenIntSum / (double)children.size();	// Feature 42
			di.childrenAndTargetIntersectionMaximum = childrenIntMax;	// Feature 43
			di.childrenAndTargetIntersectionMinimum = childrenIntMin;	// Feature 44

			di.childrenAndTargetAuthorLevel12Average = (double)childrenAut12Sum / (double)children.size();	// Feature 50
			di.childrenAndTargetAuthorLevel12Maximum = childrenAut12Max;	// Feature 51
			di.childrenAndTargetAuthorLevel12Minimum = childrenAut12Min;	// Feature 52

			di.childrenAndTargetWordAverage = (double)childrenWordSum / (double)children.size();	// Feature 58
			di.childrenAndTargetWordMaximum = childrenWordMax;	// Feature 59
			di.childrenAndTargetWordMinimum = childrenWordMin;	// Feature 60
		}		
				
		return di;
	}
	
	
	/**
	 * Output data set for LibSVM / LibLinear classification 
	 */
	public void outputLevelBasedDataForLibSVC(String fileName, Vector<LevelBasedDataInstance> data, boolean singleFeature) throws Exception {
		
		PrintWriter pw = new PrintWriter(fileName);
		
		for(int i=0; i<data.size(); i++) {
			//System.out.println("SVC = " + i);
			LevelBasedDataInstance di = data.get(i);
			pw.println(di.toStringForLibSVC(singleFeature));
		}
		
		pw.close();
	}

	
	/**
	 * Output data set for LibSVM / LibLinear classification 
	 */
	public void outputDataForLibSVC(String fileName, Vector<ACMTreeDataInstance> data, boolean singleFeature, int upsampling, double possitiveThreshold, int possitiveLevel, int method) throws Exception {
		
		PrintWriter pw = new PrintWriter(fileName);
		
		for(int i=0; i<data.size(); i++) {
			//System.out.println("SVC = " + i);
			ACMTreeDataInstance di = data.get(i);
			int repeatTimes = 1;
			if(di.getScore()>=possitiveThreshold || di.commonLevel>=possitiveLevel) {
				repeatTimes = upsampling;
			}
			for(int j=0; j<repeatTimes; j++) {
				pw.println(di.toStringForLibSVC(singleFeature, possitiveThreshold, possitiveLevel, method));
			}
		}
		
		pw.close();
	}
	
	/**
	 * Output data set for Weka classification 
	 */
	public void outputLevelBasedDataForWekaClassification(String fileName, Vector<LevelBasedDataInstance> data, boolean singleFeature) throws Exception {
		
		PrintWriter pw = new PrintWriter(fileName);
		
		// Header
		pw.println("@relation LevelBased");
		pw.println("@attribute class {0,1,2,3,4,5,6,7,8,9,10}");
		pw.println("@attribute targetWebPageNumber numeric");
		for(int i=0; i<levelOneClassNumber; i++) {
			//pw.println("@attribute parentWebPageNumber" + i + " numeric");
			pw.println("@attribute parentAndTargetSimilarity" + i + " numeric");
			//pw.println("@attribute childrenCount" + i + " numeric");
			pw.println("@attribute childrenAndTargetSimilarityAverage" + i + " numeric");
			pw.println("@attribute childrenAndTargetSimilarityMaximum" + i + " numeric");
			pw.println("@attribute childrenAndTargetSimilarityMinimum" + i + " numeric");
			//pw.println("@attribute childrenWebPageNumberAverage" + i + " numeric");
			//pw.println("@attribute childrenWebPageNumberMaximum" + i + " numeric");
			//pw.println("@attribute childrenWebPageNumberMinimum" + i + " numeric");
		}
		pw.println("@data");
		
		// Data
		for(int i=0; i<data.size(); i++) {
			//System.out.println("Weka = " + i);
			LevelBasedDataInstance di = data.get(i);
			pw.println(di.toStringForWekaClassification(singleFeature));
		}
		
		pw.close();
	}
	
	/**
	 * Output data set for Weka classification 
	 */
	public void outputDataForWekaClassification(String fileName, Vector<ACMTreeDataInstance> data, boolean singleFeature, int upsampling, double possitiveThreshold, int possitiveLevel, int method, boolean enrichment, int features) throws Exception {
		
		PrintWriter pw = new PrintWriter(fileName);
		
		// Header
		ACMTreeDataInstance di = new ACMTreeDataInstance();
		pw.println(di.toStringForWekaHeader(singleFeature, method, enrichment, features));
		
		// Data
		for(int i=0; i<data.size(); i++) {
			//System.out.println("Weka = " + i);
			di = data.get(i);
			int repeatTimes = 1;
			if(di.getScore()>=possitiveThreshold || di.commonLevel>=possitiveLevel) {
				repeatTimes = upsampling;
			}
			for(int j=0; j<repeatTimes; j++) {
				pw.println(di.toStringForWekaClassification(singleFeature, possitiveThreshold, possitiveLevel, method, enrichment, features));
			}
		}
		
		pw.close();
	}
	
	/**
	 * Output data set for LibSVM regression 
	 */
	public void outputDataForLibSVR(String fileName, Vector<ACMTreeDataInstance> data, boolean singleFeature, int upsampling, int method) throws Exception {
		
		PrintWriter pw = new PrintWriter(fileName);
		
		for(int i=0; i<data.size(); i++) {
			//System.out.println("SVR = " + i);
			ACMTreeDataInstance di = data.get(i);
			int repeatTimes = 1;
			if(di.getScore() == 1) {
				repeatTimes = upsampling;
			}
			for(int j=0; j<repeatTimes; j++) {
				pw.println(di.toStringForLibSVR(singleFeature, method));
			}
		}
		
		pw.close();
	}

	/**
	 * Find node with maximum similarity;
	 * if multiple nodes are found, return the last one
	 */
	public ACMTreeNode findPredictedParentNode(double[] scoreArray, boolean newAsParent, int actualIndex) {
		
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
			    
	    // Get index with highest probability
	    int highestIndex = Utility.getMaxScoreIndex(scoreArray);
	    ACMTreeNode highestParent = parentVector.get(highestIndex);
	    
	    // Get actual parent ranking and save it temporarily in root;
	    // should be copied to target child later
	    if(actualIndex > -1) {
	    	int answerRanking = Utility.getActualRankingDescending(scoreArray, actualIndex);
	    	//System.out.println("Answer ranking = " + answerRanking);
	    	this.setAnswerRanking(answerRanking);
	    }
	    else {
	    	this.setAnswerRanking(-1);
	    }
	    	
	    return highestParent;	    
	}
	
	/*
	 * Set score from array
	 */
	public double[] getScores(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.score;
	    }
	    return scoreArray;
	}
	
	/*
	 * Set score from array
	 */
	public void setScores(double[] scoreArray, boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	predictedParent.score = scoreArray[i];
	    }
	}

	/*
	 * Set score from array
	 */
	public void setScoresAndRanks(double[] scoreArray, boolean newAsParent) {
		// Get target parents and the position of actual parent
		double[] rankArray = Utility.scoreArrayToRankArrayDescending(scoreArray);
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	predictedParent.score = scoreArray[i];
	    	predictedParent.rank = rankArray[i];
	    }
	}
	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsemble(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method) throws Exception{
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] svc = this.getScoreArrayByLibLinear(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[ngd.length];
		for(int i=0; i<ngd.length; i++) {
			scoreArray[i] = ngd[i] * svc[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}
	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccard(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * ngd[i] * jaccard[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}
	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardWord(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] word = this.getScoreArrayByWord(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * ngd[i] * jaccard[i] * word[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardIntersection(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] intersection = this.getScoreArrayByIntersection(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * ngd[i] * jaccard[i] * intersection[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBNGDJaccardAuthor(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] author = this.getScoreArrayByAuthor1(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * ngd[i] * jaccard[i] * author[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBNGD(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] ngd = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * ngd[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}
	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNB(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBJaccard(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * jaccard[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersection(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] intersection = this.getScoreArrayByIntersection(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * jaccard[i] * intersection[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersectionWord(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] word = this.getScoreArrayByWord(newAsParent);
		double[] intersection = this.getScoreArrayByIntersection(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * jaccard[i] * intersection[i] * word[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleLLNBJaccardIntersectionAuthor(String modelFileNameLL, String modelFileNameNB, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] author = this.getScoreArrayByAuthor12(newAsParent);
		double[] intersection = this.getScoreArrayByIntersection(newAsParent);
		double[] jaccard = this.getScoreArrayByJaccard(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileNameNB, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] ll = this.getScoreArrayByLibLinear(modelFileNameLL, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[nb.length];
		for(int i=0; i<nb.length; i++) {
			scoreArray[i] = nb[i] * ll[i] * jaccard[i] * intersection[i] * author[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByMultiplyScoreEnsembleNGDNB(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{
		double[] baseline = this.getScoreArrayByNGD(newAsParent);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		
		double[] scoreArray = new double[baseline.length];
		for(int i=0; i<baseline.length; i++) {
			scoreArray[i] = baseline[i] * nb[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
	    
	    return scoreArray;
	}

	
	/**
	 * Find max parent by classification, rank ensemble
	 */
	public double[] getScoreArrayByMultiplyRankEnsemble(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method) throws Exception{
		double[] baseline = this.getScoreArrayByNGD(newAsParent);
		double[] rankBaseline = Utility.scoreArrayToRankArrayAscending(baseline);
		double[] svc = this.getScoreArrayByLibLinear(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		double[] rankSvc = Utility.scoreArrayToRankArrayAscending(svc);
		
		double[] scoreArray = new double[rankBaseline.length];
		for(int i=0; i<rankBaseline.length; i++) {
			scoreArray[i] = rankBaseline[i] * rankSvc[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
		
	    return scoreArray;
	}
	
	
	/**
	 * Find max parent by classification, score ensemble
	 */
	public double[] getScoreArrayByLinearScoreEnsemble(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, double gamma, int method) throws Exception{
		double[] baseline = this.getScoreArrayByNGD(newAsParent);
		double[] svc = this.getScoreArrayByLibLinear(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		
		double[] scoreArray = new double[baseline.length];
		for(int i=0; i<baseline.length; i++) {
			scoreArray[i] = gamma * baseline[i] + (1 - gamma) * svc[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
		
		return scoreArray;
	}
	
	/**
	 * Find max parent by classification, rank ensemble
	 */
	public double[] getScoreArrayByLinearRankEnsemble(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, double gamma, int method) throws Exception{
		double[] baseline = this.getScoreArrayByNGD(newAsParent);
		double[] rankBaseline = Utility.scoreArrayToRankArrayAscending(baseline);
		double[] svc = this.getScoreArrayByLibLinear(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method);
		double[] rankSvc = Utility.scoreArrayToRankArrayAscending(svc);
		
		double[] scoreArray = new double[rankBaseline.length];
		for(int i=0; i<rankBaseline.length; i++) {
			scoreArray[i] = gamma * rankBaseline[i] + (1 - gamma) * rankSvc[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
		
	    return scoreArray;
	}

	/**
	 * Find max parent by classification, rank ensemble
	 */
	public double[] getScoreArrayByLinearRankEnsembleNGDNB(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, double gamma, int method, boolean enrichment, int features) throws Exception{
		double[] baseline = this.getScoreArrayByNGD(newAsParent);
		double[] rankBaseline = Utility.scoreArrayToRankArrayAscending(baseline);
		double[] nb = this.getScoreArrayByWekaClassification(modelFileName, index, targetWeb, singleFeature, semanticThreshold, newAsParent, method, enrichment, features);
		double[] rankNb = Utility.scoreArrayToRankArrayAscending(nb);
		
		double[] scoreArray = new double[rankBaseline.length];
		for(int i=0; i<rankBaseline.length; i++) {
			scoreArray[i] = gamma * rankBaseline[i] + (1 - gamma) * rankNb[i];
		}

		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);
		
	    return scoreArray;
	}

	public double[] getScoreArrayByRandom(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = Utility.generateRandomScoreArray(parentVector.size());
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByJaccard(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getJaccard();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByOneNormTopology(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score arrays for scores
	    double[] scoreArray = new double[parentVector.size()];
	    double[][] s = new double[3][parentVector.size()];
    	s[0] = getScoreArrayByParentLevel(newAsParent);
    	s[1] = getScoreArrayBySiblingCount(newAsParent);
    	s[2] = getScoreArrayByChildrenCount(newAsParent);
		
	    for(int i=0; i<parentVector.size(); i++) {
	    	scoreArray[i] += s[0][i];
	    	scoreArray[i] += s[1][i];
	    	scoreArray[i] += s[2][i];
	    	scoreArray[i] /= (double)3;
	    }
    	
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByOneNormContent(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score arrays for scores
	    double[] scoreArray = new double[parentVector.size()];
	    double[][] s = new double[5][parentVector.size()];
    	s[0] = getScoreArrayByParentWebPageNumber(newAsParent);
    	s[1] = getScoreArrayByWord(newAsParent);
    	s[2] = getScoreArrayByIntersection(newAsParent);
    	s[3] = getScoreArrayByJaccard(newAsParent);
    	s[4] = getScoreArrayByNGD(newAsParent);
		
	    for(int i=0; i<parentVector.size(); i++) {
	    	scoreArray[i] += s[0][i];
	    	scoreArray[i] += s[1][i];
	    	scoreArray[i] += s[2][i];
	    	scoreArray[i] += s[3][i];
	    	scoreArray[i] += s[4][i];
	    	scoreArray[i] /= (double)5;
	    }
    	
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByOneNormSocial(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score arrays for scores
	    double[] scoreArray = new double[parentVector.size()];
	    double[][] s = new double[2][parentVector.size()];
    	s[0] = getScoreArrayByAuthor1(newAsParent);
    	s[1] = getScoreArrayByAuthor12(newAsParent);
		
	    for(int i=0; i<parentVector.size(); i++) {
	    	scoreArray[i] += s[0][i];
	    	scoreArray[i] += s[1][i];
	    	scoreArray[i] /= (double)2;
	    }
    	
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByOneNorm(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score arrays for scores
	    double[] scoreArray = new double[parentVector.size()];
	    double[][] s = new double[10][parentVector.size()];
    	s[0] = getScoreArrayByParentLevel(newAsParent);
    	s[1] = getScoreArrayBySiblingCount(newAsParent);
    	s[2] = getScoreArrayByChildrenCount(newAsParent);
    	s[3] = getScoreArrayByParentWebPageNumber(newAsParent);
    	s[4] = getScoreArrayByWord(newAsParent);
    	s[5] = getScoreArrayByIntersection(newAsParent);
    	s[6] = getScoreArrayByJaccard(newAsParent);
    	s[7] = getScoreArrayByNGD(newAsParent);
    	s[8] = getScoreArrayByAuthor1(newAsParent);
    	s[9] = getScoreArrayByAuthor12(newAsParent);
		
	    for(int i=0; i<parentVector.size(); i++) {
	    	scoreArray[i] += s[0][i];
	    	scoreArray[i] += s[1][i];
	    	scoreArray[i] += s[2][i];
	    	scoreArray[i] += s[3][i];
	    	scoreArray[i] += s[4][i];
	    	scoreArray[i] += s[5][i];
	    	scoreArray[i] += s[6][i];
	    	scoreArray[i] += s[7][i];
	    	scoreArray[i] += s[8][i];
	    	scoreArray[i] += s[9][i];
	    	scoreArray[i] /= (double)10;
	    }
    	
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByTwoNorm(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score arrays for scores
	    double[] scoreArray = new double[parentVector.size()];
	    double[][] s = new double[10][parentVector.size()];
    	s[0] = getScoreArrayByParentLevel(newAsParent);
    	s[1] = getScoreArrayBySiblingCount(newAsParent);
    	s[2] = getScoreArrayByChildrenCount(newAsParent);
    	s[3] = getScoreArrayByParentWebPageNumber(newAsParent);
    	s[4] = getScoreArrayByWord(newAsParent);
    	s[5] = getScoreArrayByIntersection(newAsParent);
    	s[6] = getScoreArrayByJaccard(newAsParent);
    	s[7] = getScoreArrayByNGD(newAsParent);
    	s[8] = getScoreArrayByAuthor1(newAsParent);
    	s[9] = getScoreArrayByAuthor12(newAsParent);
		
	    for(int i=0; i<parentVector.size(); i++) {
	    	scoreArray[i] += Math.pow(s[0][i], 2);
	    	scoreArray[i] += Math.pow(s[1][i], 2);
	    	scoreArray[i] += Math.pow(s[2][i], 2);
	    	scoreArray[i] += Math.pow(s[3][i], 2);
	    	scoreArray[i] += Math.pow(s[4][i], 2);
	    	scoreArray[i] += Math.pow(s[5][i], 2);
	    	scoreArray[i] += Math.pow(s[6][i], 2);
	    	scoreArray[i] += Math.pow(s[7][i], 2);
	    	scoreArray[i] += Math.pow(s[8][i], 2);
	    	scoreArray[i] += Math.pow(s[9][i], 2);
	    	scoreArray[i] /= (double)10;
	    	scoreArray[i] = Math.sqrt(scoreArray[i]);
	    }
    	
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByParentWebPageNumber(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getWebPageNumber();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByChildrenCount(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getChildNode().size();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayBySiblingCount(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getParentNode().getChildNode().size();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	public double[] getScoreArrayByParentLevel(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getClassificationNumber().getClassificationLevel();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByWord(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getWord();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	public double[] getScoreArrayByIntersection(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getIntersection();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	public double[] getScoreArrayByAuthor1(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getAuthor1();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}
	
	public double[] getScoreArrayByAuthor2(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getAuthor2();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	public double[] getScoreArrayByAuthor12(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getAuthor12();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	public double[] getScoreArrayByNGD(boolean newAsParent) {
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
	    // Get score array
	    double[] scoreArray = new double[parentVector.size()];
	    for(int i=0; i<parentVector.size(); i++) {
	    	ACMTreeNode predictedParent = parentVector.get(i);
	    	scoreArray[i] = predictedParent.getSimilarity();
	    }
		
	    // Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);		

	    return scoreArray;
	}

	/**
	 * Find max parent by classification.
	 */
	public int getLevelBasedPredictedClassLabelByWekaClassification(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent) throws Exception{

		// Get model
		FilteredClassifier fc = (FilteredClassifier)SerializationHelper.read(modelFileName);

		// Index string
		String indexString = Integer.toString(index + 1);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.arff";

		// Output testing data
		Vector<LevelBasedDataInstance> testingData = this.generateLevelBasedTestingData(targetWeb);
		this.outputLevelBasedDataForWekaClassification(testingDataFileName, testingData, singleFeature);
		
		// Weka classification
		Instances test = new Instances(new BufferedReader(new FileReader(testingDataFileName)));
		test.setClassIndex(0);
		int classLabel = (int)fc.classifyInstance(test.instance(0));
	    	    
	    return classLabel;
	}

	/**
	 * Find max parent by classification.
	 */
	public double[] getScoreArrayByWekaClassification(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method, boolean enrichment, int features) throws Exception{

		// Get model
		FilteredClassifier fc = (FilteredClassifier)SerializationHelper.read(modelFileName);

		// Index string
		String indexString = Integer.toString(index + 1);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.arff";

		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
		// Output testing data
		Vector<ACMTreeDataInstance> testingData = this.generateTestingData(targetWeb, parentVector, semanticThreshold);
		this.outputDataForWekaClassification(testingDataFileName, testingData, singleFeature, 1, 1, 5, method, enrichment, features);
		
		// Weka classification
		Instances test = new Instances(new BufferedReader(new FileReader(testingDataFileName)));
		test.setClassIndex(0);
	    double[] scoreArray = new double[test.numInstances()];
		for(int i=0; i<test.numInstances(); i++) {
			double[] result=(double[])fc.distributionForInstance(test.instance(i));
			scoreArray[i] = result[0];
		}
	    // Get score array
 	    
		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);

	    return scoreArray;
	}


	/**
	 * Find max parent by classification.
	 */
	public double[] getScoreArrayByLibLinear(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method) throws Exception{

		String indexString = Integer.toString(index + 1);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.txt";
		String outputFileName = "output/" + indexString + "_output.txt";

		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
		// Output testing data
		Vector<ACMTreeDataInstance> testingData = this.generateTestingData(targetWeb, parentVector, semanticThreshold);
		this.outputDataForLibSVC(testingDataFileName, testingData, singleFeature, 1, 1, 5, method);
		
		// LibLinear classification
		String parameters[] = {"-b", "1", testingDataFileName, modelFileName, outputFileName};
	    Predict.main(parameters);
	    
	    // Get score array
	    double[] scoreArray = this.getScoreArrayFromOutputFileByLibSVC(outputFileName);
	    
		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);

	    return scoreArray;
	}

	/**
	 * Find max parent by classification.
	 */
	public double[] getScoreArrayByLibSVC(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method) throws Exception{

		String indexString = Integer.toString(index + 1);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.txt";
		String outputFileName = "output/" + indexString + "_output.txt";

		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
		// Output testing data
		Vector<ACMTreeDataInstance> testingData = this.generateTestingData(targetWeb, parentVector, semanticThreshold);
		this.outputDataForLibSVC(testingDataFileName, testingData, singleFeature, 1, 1, 5, method);
		
		// LibLinear classification
		String parameters[] = {"-b", "1", testingDataFileName, modelFileName, outputFileName};
	    SVMPredict.main(parameters);
	    
	    // Get score array
	    double[] scoreArray = this.getScoreArrayFromOutputFileByLibSVC(outputFileName);
	    
		// Normalize the score array
	    scoreArray = Utility.getNormalizedScoreArray(scoreArray);

	    return scoreArray;
	}

	/**
	 * Find max parent by classification.
	 */
	public int getLevelBasedPredictedClassLabelByLibSVC(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent) throws Exception{

		String indexString = Integer.toString(index + 1);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.txt";
		String outputFileName = "output/" + indexString + "_output.txt";

		// Output testing data
		Vector<LevelBasedDataInstance> testingData = this.generateLevelBasedTestingData(targetWeb);
		this.outputLevelBasedDataForLibSVC(testingDataFileName, testingData, singleFeature);
		
		// LibLinear classification
		String parameters[] = {testingDataFileName, modelFileName, outputFileName};
	    Predict.main(parameters);
	    
	    // Get class label
	    int classLabel = this.getLevelBasedClassLabelFromOutputFileByLibSVC(outputFileName);
	    
	    return classLabel;
	}
	
	/**
	 * Find max parent by regression.
	 */
	public double[] getScoreArrayByLibSVR(String modelFileName, int index, double targetWeb, boolean singleFeature, double semanticThreshold, boolean newAsParent, int method) throws Exception{

		String indexString = Integer.toString(index);
		
		// File names
		String testingDataFileName = "testing/" + indexString + "_test.txt";
		String outputFileName = "output/" + indexString + "_output.txt";

		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		
		// Output testing data
		Vector<ACMTreeDataInstance> testingData = this.generateTestingData(targetWeb, parentVector, semanticThreshold);
		this.outputDataForLibSVR(testingDataFileName, testingData, singleFeature, 1, method);
		
		// LibLinear classification
		String parameters[] = {"-b", "1", testingDataFileName, modelFileName, outputFileName};
	    SVMPredict.main(parameters);
	    
	    // Get score array
	    double[] scoreArray = this.getScoreArrayFromOutputFileByLibSVR(outputFileName);
	    
	    return scoreArray;
	}
	
	public int getActualParentIndex(String actualParentName, boolean newAsParent) { 
		// Get target parents and the position of actual parent
		Vector<ACMTreeNode> parentVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), newAsParent);
		int actualIndex = -1;
		for(int i=0; i<parentVector.size(); i++) {
			ACMTreeNode predictedParent = parentVector.get(i);
			//System.out.println(predictedParent.getClassificationName());
			if(predictedParent.getClassificationName().equalsIgnoreCase(actualParentName)) {
				actualIndex = i;
				predictedParent.isAnswer = true;
			}
			else {
				predictedParent.isAnswer = false;
			}
		}
		return actualIndex;
	}
	
	public int getLevelBasedClassLabelFromOutputFileByLibSVC(String fileName) throws Exception {
		
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		
		String s = lnr.readLine();
		//s = lnr.readLine();
		//int stopIndex = s.indexOf(" ");				
		//String classLabelString = s.substring(0, stopIndex); 
		//int classLabel = Integer.parseInt(classLabelString);
		int classLabel = Integer.parseInt(s);
			
		lnr.close();
		fr.close();
		
		return classLabel;
	}

	
	public double[] getScoreArrayFromOutputFileByLibSVC(String fileName) throws Exception {
		
		Vector<Double> result = new Vector<Double>();
		boolean positiveFirst = true;

		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		
		String s = lnr.readLine();
		if(s.equalsIgnoreCase("labels -1 1")) {
			positiveFirst = false;
		}
				
		while ((s=lnr.readLine()) != null) {
			String scoreString = null;
			
			if(!positiveFirst) {
				int startIndex = s.indexOf(" ");				
				startIndex = s.indexOf(" ", startIndex + 1);
				scoreString = s.substring(startIndex + 1, s.length() - 1); 
			}
			else {
				int startIndex = s.indexOf(" ");				
				int stopIndex = s.indexOf(" ", startIndex + 1);
				scoreString = s.substring(startIndex + 1, stopIndex); 				
			}	
			
			result.add(new Double(scoreString));
		}
			
		lnr.close();
		fr.close();
		
		return Utility.doubleVectorToDoubleArray(result);
	}
	
	
	public double[] getScoreArrayFromOutputFileByLibSVR(String fileName) throws Exception {
		
		Vector<Double> result = new Vector<Double>();

		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);

		String s = null;
		while ((s=lnr.readLine()) != null) {
			result.add(new Double(s));
		}
			
		lnr.close();
		fr.close();
		
		return Utility.doubleVectorToDoubleArray(result);
	}
	
	/**
	 * Generate testing data.
	 * This should contain n-1 data instances.
	 * Then, the node with highest score will be the predicted parent.
	 * Should be called only by root.
	 */
	public Vector<LevelBasedDataInstance> generateLevelBasedTestingData(double targetWeb) throws Exception {
		Vector<LevelBasedDataInstance> result = new Vector<LevelBasedDataInstance>();
		LevelBasedDataInstance di = this.generateLevelBasedTestingDataInstance(targetWeb);
		result.add(di);
		return result;
	}

	/**
	 * Generate testing data.
	 * This should contain n-1 data instances.
	 * Then, the node with highest score will be the predicted parent.
	 * Should be called only by root.
	 */
	public Vector<ACMTreeDataInstance> generateTestingData(double targetWeb, Vector<ACMTreeNode> parentVector, double semanticThreshold) throws Exception {
		Vector<ACMTreeDataInstance> result = new Vector<ACMTreeDataInstance>();
		for(int j=0; j<parentVector.size(); j++) {
			ACMTreeNode parent = parentVector.get(j);
			if(parent.getSimilarity() > semanticThreshold) {
				ACMTreeDataInstance di = this.generateTestingDataInstance(parent, targetWeb);
				result.add(di);
			}
		}
		return result;
	}
	
	/**
	 * Generate training data,
	 * which is all combination of (parent, target) pair.
	 * This should contain P(n, 2) data instances.
	 * However, this is too large.
	 * We can down-sample it by setting semantic similarity threshold.
	 * Should be called only by root.
	 */
	public Vector<LevelBasedDataInstance> generateLevelBasedTrainingData(double[][] tree91ToTree91NGD, double alpha, double semanticThreshold, double structuralThreshold, boolean useAnd, double beta, int adjustSimilarity, double possitiveThreshold) throws Exception {
		Vector<LevelBasedDataInstance> result = new Vector<LevelBasedDataInstance>();
		Vector<ACMTreeNode> childVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), false);

		for(int i=0; i<childVector.size(); i++) {		// Child
			this.loadSimilarity(tree91ToTree91NGD[i], 0);			
			this.adjustSimilarities(beta, adjustSimilarity);
			ACMTreeNode target = childVector.get(i);
			if(target.classificationNumber.classificationLevel > 1) {
				LevelBasedDataInstance di = this.generateLevelBasedTrainingDataInstance(target, alpha);
				result.add(di);
			}
		}
		
		return result;
	}

	/**
	 * Generate level-based training data,
	 * which is all combination of (parent, target) pair.
	 * This should contain P(n, 2) data instances.
	 * However, this is too large.
	 * We can down-sample it by setting semantic similarity threshold.
	 * Should be called only by root.
	 */
	public Vector<ACMTreeDataInstance> generateTrainingData(double[][] tree91ToTree91, double[][] tree91ToTree91NGD, double[][] tree91ToTree91Jaccard, double[][] tree91ToTree91Level1, double[][] tree91ToTree91Level2, double[][] tree91ToTree91Level12, double[][] tree91ToTree91Word, double alpha, double semanticThreshold, double structuralThreshold, boolean useAnd, double beta, int adjustSimilarity, double possitiveThreshold, int possitiveLevel) throws Exception {
		Vector<ACMTreeDataInstance> result = new Vector<ACMTreeDataInstance>();
		Vector<ACMTreeNode> nodeVector = this.getPreprocessedNodeVector(new Vector<ACMTreeNode>(), false);
		int possitiveNumber = 0;

		//for(int i=0; i<10; i++) {						// Test
		for(int i=0; i<nodeVector.size(); i++) {		// Child
			
			// Load NGD similarity
			this.loadSimilarity(tree91ToTree91NGD[i], 0);			
			this.adjustSimilarities(beta, adjustSimilarity);
			
			// Load Jaccard similarity
			this.loadJaccard(tree91ToTree91Jaccard[i], 0);
			
			// Load author number
			this.loadAuthor1(tree91ToTree91Level1[i], 0);
			this.loadAuthor2(tree91ToTree91Level2[i], 0);
			this.loadAuthor12(tree91ToTree91Level12[i], 0);
			
			// Load intersection similarity
			this.loadIntersection(tree91ToTree91[i], 0);			
			
			// Load word similarity
			this.loadWord(tree91ToTree91Word[i], 0);
			
			ACMTreeNode target = nodeVector.get(i);

			for(int j=0; j<nodeVector.size(); j++) {	// Parent
				if(i != j) {
					ACMTreeNode parent = nodeVector.get(j);
					ACMTreeDataInstance di = this.generateTrainingDataInstance(parent, target, alpha);
					
					// Apply threshold;
					// only add di with similarity larger than threshold
					boolean add = false;
					
					if(useAnd) {
						if((di.parentAndTargetSimilarity>semanticThreshold) && (di.score>structuralThreshold)) {
							add = true;
						}
					}
					else {
						if((di.parentAndTargetSimilarity>semanticThreshold) || (di.score>structuralThreshold)) {
							add = true;
						}
					}
					
					if(add){
						result.add(di);
						if(di.getScore()>=possitiveThreshold || di.commonLevel>=possitiveLevel) {
							possitiveNumber++;
						}
					}
				}
			}
		}
		
		System.out.println("Possitive number = " + possitiveNumber);
		
		return result;
	}

	/**
	 * Generate training data instance.
	 */
	public LevelBasedDataInstance generateLevelBasedTrainingDataInstance(ACMTreeNode target, double alpha) {
		LevelBasedDataInstance di = this.generateLevelBasedDataInstance(target.getWebPageNumber());
		di.setClassLabel(target.classificationNumber.getLevelOneClassLabel());
		return di;
	}

	/**
	 * Generate testing data instance.
	 */
	public LevelBasedDataInstance generateLevelBasedTestingDataInstance(double targetWeb) {
		LevelBasedDataInstance di = this.generateLevelBasedDataInstance(targetWeb);
		di.setClassLabel(0);	// Assign to 0
		return di;
	}
	
	
	/**
	 * Generate training data instance.
	 */
	public ACMTreeDataInstance generateTrainingDataInstance(ACMTreeNode parent, ACMTreeNode target, double alpha) {
		ACMTreeDataInstance di = this.generateDataInstance(parent, target.getWebPageNumber());
		di.score = this.computeStructuralSimilarity(parent, target.getParentNode(), target.getClassificationName(), alpha);	// 0.0 ~ 1.0
		di.commonLevel = this.computeCommonLevel(parent, target.parentNode, target.getClassificationName());
		return di;
	}

	/**
	 * Generate testing data instance.
	 */
	public ACMTreeDataInstance generateTestingDataInstance(ACMTreeNode parent, double targetWeb) {
		ACMTreeDataInstance di = this.generateDataInstance(parent, targetWeb);
		di.score = -1;	// Give it an always-negative value
		return di;
	}
	
	public LevelBasedDataInstance generateLevelBasedDataInstance(double targetWeb) {
		
		LevelBasedDataInstance di = new LevelBasedDataInstance();
		di.setTargetWebPageNumber(targetWeb);

		Vector<ACMTreeNode> parentVector = this.getChildNode();
		for(int i=0; i<parentVector.size(); i++) {
			ACMTreeNode parent = parentVector.get(i);
			di.parentWebPageNumber[i] = parent.getWebPageNumber();
			di.parentAndTargetSimilarity[i] = parent.getSimilarity();
			//System.out.println(parent.getClassificationName() + ", " + parent.getWebPageNumber() + ", " + parent.getSimilarity());

			Vector<ACMTreeNode> children = parent.getChildNode();
			if(children.size() > 0) {
				
				di.childrenCount[i] = children.size();

				double childrenWebSum = 0;
				double childrenWebMax = 0;
				double childrenWebMin = 0;
				
				double childrenSimSum = 0;
				double childrenSimMax = 0;
				double childrenSimMin = 0;
				
				for(int j=0; j<children.size(); j++) {
					
					ACMTreeNode child = children.get(j);
					if(child.isValid()) {
					
						double webPageNumber = child.getWebPageNumber();
						double similarity = child.getSimilarity();
						
						childrenWebSum += webPageNumber;
						childrenSimSum += similarity;
						
						if(childrenWebMax == 0 || childrenWebMax < webPageNumber) {
							childrenWebMax = webPageNumber;
						}
	
						if(childrenWebMin == 0 || (webPageNumber>0 && childrenWebMin>webPageNumber)) {
						//if(childrenWebMin == 0 || childrenWebMin > webPageNumber) {
							childrenWebMin = webPageNumber;
						}
	
						if(childrenSimMax == 0 || childrenSimMax < similarity) {
							childrenSimMax = similarity;
						}
	
						if(childrenSimMin == 0 || (similarity>0 && childrenSimMin>similarity)) {
						//if(childrenSimMin == 0 || childrenSimMin>similarity) {
							childrenSimMin = similarity;
						}
					}
				}
				
				di.childrenWebPageNumberAverage[i] = (double)childrenWebSum / (double)children.size();	// Feature 9
				di.childrenWebPageNumberMaximum[i] = childrenWebMax;	// Feature 10
				di.childrenWebPageNumberMinimum[i] = childrenWebMin;	// Feature 11
						
				di.childrenAndTargetSimilarityAverage[i] = (double)childrenSimSum / (double)children.size();		// Feature 18
				di.childrenAndTargetSimilarityMaximum[i] = childrenSimMax;		// Feature 19
				di.childrenAndTargetSimilarityMinimum[i] = childrenSimMin;		// Feature 20
			}		
		}
		
		return di;
	}

	

	/**
	 * A node is valid if it is not root, general, miscellaneous and deleted 
	 */
	public boolean isValid() {
		if(this.classificationName != null && !this.classificationName.equalsIgnoreCase("root") && !this.isGeneral && !this.isMiscellaneous && !this.isRetired()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Adjust name by appending parent name as prefix, if this is a repeated node.
	 */
	public void adjustRepeatedName() {

		if(!this.isUnique) {
			String parentName = this.parentNode.getClassificationName();
			if(!parentName.equalsIgnoreCase("Root")) {
				this.classificationName = parentName + " " + this.classificationName;
			}
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			child.adjustRepeatedName();
		}

	}
	
	/**
	 * Get keywords that is marked as deleted in this tree,
	 * and return the new keyword list as a Vector.
	 * 
	 * @return
	 */
	public Vector<String> getDeletedKeywords(Vector<String> deletedKeywords) {

		if(this.isRetired1991 || this.isRetired1998) {
			deletedKeywords.add(this.classificationName);
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			deletedKeywords = child.getDeletedKeywords(deletedKeywords);
		}

		return deletedKeywords;
	}
	
	/**
	 * Get keywords that is marked as non-unique in this tree,
	 * remove root, miscellaneous and general,
	 * and return the repeated keyword list as a Vector.
	 * 
	 * @return
	 */
	public Vector<String> getRepeatedKeywords(Vector<String> repeatedKeywords) {

		if(!this.isUnique) {
			if(this.isValid()) {
				Boolean matched = false;
				for(int j=0; j<repeatedKeywords.size(); j++) {
					String repeated = repeatedKeywords.get(j);
					if(this.classificationName.equalsIgnoreCase(repeated)) {
						matched = true;
					}
				}
				if(!matched) {
					repeatedKeywords.add(this.classificationName);
				}
			}
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			repeatedKeywords = child.getRepeatedKeywords(repeatedKeywords);
		}

		return repeatedKeywords;
	}
	
	/**
	 * Find keywords that is unique,
	 * mark it in this tree,
	 * and return the unique keyword list as a Vector.
	 * 
	 * @return
	 */
	public Vector<String> findUniqueKeywords(Vector<String> uniqueKeywords, ACMTreeNode root) {

		int matchCount = root.searchRepeatedKeywords(this.classificationName);
		//System.out.println(this.classificationName + " = " + matchCount);

		if(matchCount == 1) {
			uniqueKeywords.add(this.classificationName);
			this.isUnique = true;
		}
		else {
			this.isUnique = false;
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			uniqueKeywords = child.findUniqueKeywords(uniqueKeywords, root);
		}

		return uniqueKeywords;
	}
	
	/**
	 * Find target keyword in this tree,
	 * if found, mark the node as non-unique,
	 * and return match count ( = 1 if unique) 
	 * 
	 * @return
	 */
	public int searchRepeatedKeywords(String targetKeyword) {
		
		int matchCount = 0;
		Boolean matched = this.classificationName.equalsIgnoreCase(targetKeyword);
		
		if(matched) {
			matchCount ++;
		}
				
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			matchCount += child.searchRepeatedKeywords(targetKeyword);
		}

		return matchCount;
	}

	/**
	 * Get Leaf keywords that is in this tree but not in old tree,
	 * and return the new subject descriptor keyword list as a Vector.
	 * 
	 * @param newLeafKeywords
	 * @return
	 */
	public Vector<String> getNewSubjectDescriptorKeywords(Vector<String> newLeafKeywords) {
		
		if(this.isNew && this.classificationNumber.getIsSubjectDescriptor()) {
			newLeafKeywords.add(this.classificationName);
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			newLeafKeywords = child.getNewSubjectDescriptorKeywords(newLeafKeywords);
		}

		return newLeafKeywords;
	}
	
	/**
	 * Get Leaf keywords that is in this tree but not in old tree,
	 * and return the new Leaf keyword list as a Vector.
	 * 
	 * @param newLeafKeywords
	 * @return
	 */
	public Vector<String> getNewLeafKeywords(Vector<String> newLeafKeywords) {
		
		if(this.isNew && this.classificationNumber.getIsLeaf()) {
			newLeafKeywords.add(this.classificationName);
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			newLeafKeywords = child.getNewLeafKeywords(newLeafKeywords);
		}

		return newLeafKeywords;
	}
	
	/**
	 * Find keywords that is in this tree but not in old tree,
	 * mark it in this tree,
	 * and return the new keyword list as a Vector.
	 * 
	 * @param oldRoot
	 * @return
	 */
	public Vector<String> findNewKeywords(Vector<String> oldKeywords, Vector<String> newKeywords) {

		Boolean matched = false;
		
		if(this.isValid()) {		
		
			for(int j=0; j<oldKeywords.size(); j++) {
				String old = oldKeywords.get(j);
				if(this.classificationName.equalsIgnoreCase(old)) {
					matched = true;
				}
			}

			// To avoid the deleted as new, we ignore deleted keywords
			if(!matched && !this.isRetired1991 && !this.isRetired1998) {
				newKeywords.add(this.classificationName);
				this.isNew = true;
			}
			else {
				this.isNew = false;
			}
			
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			newKeywords = child.findNewKeywords(oldKeywords, newKeywords);
		}

		return newKeywords;
	}
	
	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public double addScoreByChildMinimumBottomUp(double factor) {
	
		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.addScoreByChildMinimumBottomUp(factor); 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.score += childMin;
		}
		
		return this.score;
	}

	
	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public double addScoreByChildMaximumBottomUp(double factor) {
	
		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.addScoreByChildMaximumBottomUp(factor); 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.score += childMax;
		}
		
		return this.score;
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addScoreByChildAllTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childSum = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					childSum += child.score;
				}
			}
			
			childSum *= (double)factor;
			this.score += childSum;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildAllTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the average similarity of children, weighted by factor
	 */
	public double addScoreByChildAllBottomUp(double factor) {
	
		double adjustment = 0;
		
		if(this.childNode.size() > 0) {
			int i;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					adjustment += child.addScoreByChildAllBottomUp(factor);
				}
			}
			
			adjustment *= (double)factor;
			this.score += adjustment;
		}
		
		return this.score;
	}

	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void addScoreByChildMinMaxTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.score; 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.score += childMin;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildMaximumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void adjustSimilarities(double factor, int adjustSimilarity) {
		switch(adjustSimilarity) {
			case 0: this.addSimilarityByChildMinimumTopDown(0.15); break;
			case 1: this.addSimilarityByChildMinMaxTopDown(1.75); break;
			case 2: this.addSimilarityByParentTopDown(factor); break;
			case 3: this.addSimilarityByChildMaxMinTopDown(0.15); break;
			case 4: this.addSimilarityByChildMinIterativeTopDown(factor); break;
			case 5: this.addSimilarityByChildMaxIterativeTopDown(factor); break;
			default: break;
		}
	}

	/**
	 * Adjust similarity by the minimum similarity of parent, weighted by factor
	 */
	public void addSimilarityByParentTopDown(double factor) {

		if(this.parentNode != null) {
			this.similarity += (factor * this.parentNode.similarity); 
		}
		
		if(this.childNode.size() > 0) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByParentTopDown(factor);
				}
			}
		}
	}
	
	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMinimumTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.similarity += childMin;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMinimumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMaximumTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.similarity += childMax;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMaximumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMaxMinTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.similarity += childMax;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMinimumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMaxIterativeTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.similarity += childMax;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMinIterativeTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMinIterativeTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.similarity += childMin;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMaxIterativeTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void addSimilarityByChildMinMaxTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.similarity; 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.similarity += childMin;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addSimilarityByChildMaximumTopDown(factor);
				}
			}
		}
	}

	
	/**
	 * Adjust similarity by the minimum similarity of children, weighted by factor
	 */
	public void addScoreByChildMinimumTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMin = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.score; 
					if(childMin == 0 || childMin > childSim) {
						childMin = childSim;
					}
				}
			}
			
			childMin *= (double)factor;
			this.score += childMin;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildMinimumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addScoreByChildMaxMinTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.score; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.score += childMax;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildMinimumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void addScoreByChildMaximumTopDown(double factor) {

		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.score; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.score += childMax;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildMaximumTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the average similarity of children, weighted by factor
	 */
	public double addScoreByChildAverageBottomUp(double factor) {
	
		double adjustment = 0;
		
		if(this.childNode.size() > 0) {
			int i;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					adjustment += child.addScoreByChildAverageBottomUp(factor);
				}
			}
			
			adjustment = (double)adjustment / (double)i;
			adjustment *= (double)factor;
			this.score += adjustment;
		}
		
		return this.score;
	}

	/**
	 * Adjust similarity by the average similarity of children, weighted by factor
	 */
	public void addScoreByChildAverageTopDown(double factor) {

		double adjustment = 0;
		
		if(this.childNode.size() > 0) {
			int i;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					adjustment += child.score;
				}
			}
			
			adjustment = (double)adjustment / (double)i;
			adjustment *= (double)factor;
			this.score += adjustment;

			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.addScoreByChildAverageTopDown(factor);
				}
			}
		}
	}

	/**
	 * Load saved similarities to vector
	 */
	public void loadSimilarityFromFile(String fileName) throws IOException {
		
		Vector<String> result = new Vector<String>();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		while ((s=lnr.readLine()) != null) {
			result.add(s);
		}
			
		lnr.close();
		fr.close();
		
		this.loadSimilarity(result);
	}
	
	/**
	 * Load similarity array to nodes;
	 * for both original and new keywords
	 */
	public int loadSimilarity(double[] oldSimilarities, int oldIndex, double[] newSimilarities) {

		if(this.isValid()) {
			if(this.insertedKeywordIndex == -1) {
				this.similarity = oldSimilarities[oldIndex];
				oldIndex++;
			}
			else {
				this.similarity = newSimilarities[this.insertedKeywordIndex];
			}
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			oldIndex = child.loadSimilarity(oldSimilarities, oldIndex, newSimilarities);
		}
		
		return oldIndex;
	}
	
	/**
	 * Load jaccard array to nodes;
	 * only for original keywords
	 */
	public int loadJaccard(double[] jaccardArray, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.jaccard = jaccardArray[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadJaccard(jaccardArray, index);
		}
		
		return index;
	}

	
	/**
	 * Load author array to nodes;
	 * only for original keywords
	 */
	public int loadAuthor1(double[] authorNumbers, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.author1 = authorNumbers[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadAuthor1(authorNumbers, index);
		}
		
		return index;
	}

	/**
	 * Load author array to nodes;
	 * only for original keywords
	 */
	public int loadAuthor2(double[] authorNumbers, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.author2 = authorNumbers[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadAuthor2(authorNumbers, index);
		}
		
		return index;
	}

	/**
	 * Load author array to nodes;
	 * only for original keywords
	 */
	public int loadAuthor12(double[] authorNumbers, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.author12 = authorNumbers[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadAuthor12(authorNumbers, index);
		}
		
		return index;
	}

	/**
	 * Load intersection array to nodes;
	 * only for original keywords
	 */
	public int loadIntersection(double[] intersectionNumber, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.intersection = intersectionNumber[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadIntersection(intersectionNumber, index);
		}
		
		return index;
	}

	/**
	 * Load intersection array to nodes;
	 * only for original keywords
	 */
	public int loadWord(double[] wordArray, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.word = wordArray[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadWord(wordArray, index);
		}
		
		return index;
	}
	
	/**
	 * Load similarity array to nodes;
	 * only for original keywords
	 */
	public int loadWebPageNumber(double[] webPageNumbers, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.webPageNumber = webPageNumbers[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadWebPageNumber(webPageNumbers, index);
		}
		
		return index;
	}
	
	/**
	 * Load similarity array to nodes;
	 * for both original and new keywords
	 */
	public int loadWebPageNumber(double[] oldWebPageNumbers, int oldIndex, double[] newWebPageNumbers) {

		if(this.isValid()) {
			if(this.insertedKeywordIndex == -1) {
				this.similarity = oldWebPageNumbers[oldIndex];
				oldIndex++;
			}
			else {
				this.similarity = newWebPageNumbers[this.insertedKeywordIndex];
			}
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			oldIndex = child.loadWebPageNumber(oldWebPageNumbers, oldIndex, newWebPageNumbers);
		}
		
		return oldIndex;
	}
	
	/**
	 * Load similarity array to nodes;
	 * only for original keywords
	 */
	public int loadSimilarity(double[] similarities, int index) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.similarity = similarities[index];
			index++;
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			index = child.loadSimilarity(similarities, index);
		}
		
		return index;
	}
	
	/**
	 * Load similarity vector to nodes;
	 * only for original keywords
	 */
	public Vector<String> loadSimilarity(Vector<String> similarities) {

		if(this.isValid() && this.insertedKeywordIndex == -1) {		
			this.similarity = Double.parseDouble(similarities.remove(0));
		}
			
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			similarities = child.loadSimilarity(similarities);
		}
		
		return similarities;
	}

	public boolean isRetired() {
		if(this.isRetired1991 || this.isRetired1998) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Get preprocessed name vector (delete root, miscellaneous and general)
	 */
	public Vector<String> getPreprocessedNames(Vector<String> result) {

		if(this.isValid()) {
			result.add(this.classificationName);
		}	
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			result = child.getPreprocessedNames(result);
		}
		
		return result;
	}

	/**
	 * Get node by number
	 */
	public ACMTreeNode getNodeByNumber(String number) {

		if(this.getClassificationNumber()!=null && this.getClassificationNumber().getClassificationNumber().equalsIgnoreCase(number)) {
			return this;
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			ACMTreeNode result = child.getNodeByNumber(number);
			if(result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * Get node by name
	 */
	public ACMTreeNode getNodeByName(String name) {

		if(this.getClassificationName().equalsIgnoreCase(name)) {
			return this;
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			ACMTreeNode result = child.getNodeByName(name);
			if(result != null) {
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * Compute structural similarity
	 */
	public double computeStructuralSimilarity(ACMTreeNode predictedParentNode,ACMTreeNode actualParentNode, String childName, double alpha) {
		Vector<String> predictedNames = predictedParentNode.getStrtucturalSimilarityNames(childName);
		Vector<String> actualNames = actualParentNode.getStrtucturalSimilarityNames(childName);
		double sim = this.computeStructuralSimilarity(predictedNames, actualNames, alpha);
		return sim;
	}

	/**
	 * Compute structural similarity
	 */
	public double computeStructuralSimilarity(Vector<String> predictedParentNames, Vector<String> actualParentNames, double alpha) {

		int dp = predictedParentNames.size();
		int da = actualParentNames.size();
		int dc = this.computeCommonLevel(predictedParentNames, actualParentNames);
		
		// Tim
		//double rp = (double)dc / (double)dp; 
		//double ra = (double)dc / (double)da;
		//double simpa = (alpha * rp) + ( (1 - alpha) * ra);
		
		// Learning Accuracy
		double top = dc - 1;
		double p = dp - dc;
		double a = da - dc;
		double simpa = (double)(top + 1) / (double)(top + 1 + p + a); 
				
		return simpa;
	}
	
	/**
	 * Compute structural distance
	 */
	public double computeStructuralDistance(ACMTreeNode predictedParentNode,ACMTreeNode actualParentNode, String childName) {
		Vector<String> predictedNames = predictedParentNode.getStrtucturalSimilarityNames(childName);
		Vector<String> actualNames = actualParentNode.getStrtucturalSimilarityNames(childName);
		double dis = this.computeStructuralDistance(predictedNames, actualNames);
		return dis;
	}

	/**
	 * Compute structural distance
	 */
	@SuppressWarnings("unchecked")
	public double computeStructuralDistance(Vector<String> predictedParentNames, Vector<String> actualParentNames) {

		// Exclude target child; otherwise, distance will start from 3 instead of 1
		Vector<String> p = (Vector<String>)predictedParentNames.clone();
		p.remove(p.size() - 1);
		Vector<String> a = (Vector<String>)actualParentNames.clone();
		a.remove(a.size() - 1);
		
		int dp = p.size();
		int da = a.size();		
		int dc = this.computeCommonLevel(p, a);

		double dispa = (dp - dc) + (da - dc);
		//System.out.println("dp = " + dp + ", da = " + da + ", dc = " + dc + ", dis = " + dispa);

		return dispa;
	}
	
	/**
	 * Compute common level (dc)
	 */
	public int computeCommonLevel(ACMTreeNode predictedParentNode,ACMTreeNode actualParentNode, String childName) {
		Vector<String> predictedNames = predictedParentNode.getStrtucturalSimilarityNames(childName);
		//Utility.printStringVector(predictedNames);
		Vector<String> actualNames = actualParentNode.getStrtucturalSimilarityNames(childName);
		//Utility.printStringVector(actualNames);
		int dc = this.computeCommonLevel(predictedNames, actualNames);
		return dc;
	}

	/**
	 * Compute common level (dc)
	 */
	public int computeCommonLevel(Vector<String> predictedParentNames, Vector<String> actualParentNames) {

		int dp = predictedParentNames.size();
		int da = actualParentNames.size();
		int dc = 0;
		
		int maxLevel = dp;
		if(maxLevel < da) {
			maxLevel = da; 
		}
		//System.out.println("maxLevel = " + maxLevel);
		
		for(int i=0; i<maxLevel; i++) {
			if(i == dp || i==da) {
				break;
			}
			
			String pName = predictedParentNames.get(i);
			String aName = actualParentNames.get(i);
			if(pName.equalsIgnoreCase(aName)) {
				dc++;
			}
			else {
				break;
			}
		}
		//System.out.println("dc = " + dc);

		return dc;
	}
	
	
	/**
	 * Reset type to 0
	 */
	public void resetType() {
		this.type = 0;
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			child.resetType();
		}
	}
	
	public void computeCorrectNumber(ACMTreeNode predictedParentNode,ACMTreeNode actualParentNode) {
		Vector<String> predictedNames = predictedParentNode.getParentNames(new Vector<String>());
		Vector<String> actualNames = actualParentNode.getParentNames(new Vector<String>());
		int dc = this.computeCommonLevel(predictedNames, actualNames);
		for(int i=0; i<dc; i++) {
			String name = predictedNames.get(i);
			ACMTreeNode node = this.getNodeByName(name);
			node.correctNumber++;
		}
	}

	
	public void computeType(ACMTreeNode predictedParentNode,ACMTreeNode actualParentNode) {
		Vector<String> predictedNames = predictedParentNode.getParentNames(new Vector<String>());
		Vector<String> actualNames = actualParentNode.getParentNames(new Vector<String>());
		int dc = this.computeCommonLevel(predictedNames, actualNames);
		for(int i=0; i<dc; i++) {
			String name = predictedNames.get(i);
			ACMTreeNode node = this.getNodeByName(name);
			node.type = 3;
		}
		for(int i=dc; i<predictedNames.size(); i++) {
			String name = predictedNames.get(i);
			ACMTreeNode node = this.getNodeByName(name);
			node.type = 2;
		}
		for(int i=dc; i<actualNames.size(); i++) {
			String name = actualNames.get(i);
			ACMTreeNode node = this.getNodeByName(name);
			node.type = 1;
		}
	}
	
	/**
	 * Get structural similarity name vector
	 */
	public Vector<String> getStrtucturalSimilarityNames(String childName) {
		Vector<String> result = this.getParentNames(new Vector<String>());
		result.add(childName);
		//result.remove(0);
		//Utility.printStringVector(result);
		return result;
	}
	
	/**
	 * Get parent name vector
	 */
	public Vector<String> getParentNames(Vector<String> result) {

		if(this.parentNode != null) {
			result = this.parentNode.getParentNames(result);
		}
		
		result.add(this.getClassificationName());
		
		return result;
	}
	
	/**
	 * Get preprocessed node vector
	 */
	public Vector<ACMTreeNode> getPreprocessedNodeVector(Vector<ACMTreeNode> result, boolean newAsParent) {

		if(this.isValid()) {
			if(newAsParent) {
				result.add(this);				
			}
			else {
				if(this.insertedKeywordIndex == -1) {
					result.add(this);
				}
			}
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			result = child.getPreprocessedNodeVector(result, newAsParent);
		}
		
		return result;
	}
	
	/**
	 * Get name vector
	 */
	public Vector<String> getNames(Vector<String> result) {

		result.add(this.getClassificationName());
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			result = child.getNames(result);
		}
		
		return result;
	}
	
	public int size() {
		int size = 0;
		if(this.isValid()) {
			size = 1;
		}
		for(ACMTreeNode child : this.childNode) {
			size += child.size();
		}
		return size;
	}
	
	/**
	 * Get subject descriptor names
	 */
	public Vector<String> getPreprocessedSubjectDescriptorNames(Vector<String> result) {

		if(this.classificationNumber != null && this.classificationNumber.getIsSubjectDescriptor()) {
			if(this.isValid()) {
				result.add(this.getClassificationName());
			}
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			result = child.getPreprocessedSubjectDescriptorNames(result);
		}
		
		return result;
	}
	
	/**
	 * Get main class (non subject descriptor) names
	 */
	public Vector<String> getPreprocessedMainClassNames(Vector<String> result) {

		if(this.classificationNumber != null && !this.classificationNumber.getIsSubjectDescriptor()) {
			if(this.isValid()) {
				result.add(this.getClassificationName());
			}
		}
		
		for(int i=0; i<this.childNode.size(); i++) {
			ACMTreeNode child = this.childNode.get(i);
			result = child.getPreprocessedMainClassNames(result);
		}
		
		return result;
	}
	
	/**
	 * To String
	 */
	public String toTestingString() {
		
		String result = new String();

		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;
				
		if(this.insertedKeywordIndex != -1) {
			result = result + " {" + Integer.toString(this.insertedKeywordIndex + 1) + "}";
		}

		return result;
	}
	
	
	/**
	 * To String
	 */
	public String toString() {
		
		String result = new String();
	
		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;
		
		if(this.example.size() > 0) {
			result = result + " ( Example = ";
			for(int i=0; i<this.example.size(); i++) {
				result = result + example.get(i) + " ";
			}
			result = result + ")";
		}
		
		if(this.isRelatedTo.size() > 0) {
			result = result + " < Related = ";
			for(int i=0; i<this.isRelatedTo.size(); i++) {
				result = result + isRelatedTo.get(i) + " ";
			}
			result = result + ">";
		}
	
		if(this.parenthesis.length() > 0) {
			result = result + " (( " + this.parenthesis + " ))";
		}
	
		if(this.isUnique) {
			result = result + " <U>";
		}
		
		if(this.isGeneral) {
			result = result + " <G>";
		}
		
		if(this.isMiscellaneous) {
			result = result + " <M>";
		}
	
		if(this.classificationNumber != null && this.classificationNumber.getIsLeaf()) {
			result = result + " <L>";
		}
		
		if(this.classificationNumber != null && this.classificationNumber.getIsSubjectDescriptor()) {
			result = result + " <S>";
		}
	
		if(this.isRetired1991) {
			result = result + " [1991 Deleted]";
		}
	
		if(this.isRetired1998) {
			result = result + " [1998 Deleted]";
		}
	
		if(this.isNew) {
			result = result + " [1998 New]";
		}
		
		if(this.insertedKeywordIndex != -1) {
			result = result + " {" + Integer.toString(this.insertedKeywordIndex + 1) + "}";
			
			if(this.isLevelOneCorrect) {
				result = result + " LV1";
				
				if(this.isLevelTwoCorrect) {
					result = result + " LV2";
					
					if(this.isLevelThreeCorrect) {
						result = result + " LV3";
					}
				}
			}
			if(this.isCorrect) {
				result = result + " Correct";
			}
	
			result = result + " dis=" + String.format("%.4f", this.structuralDistance);
			result = result + " sim=" + String.format("%.4f", this.structuralSimilarity);
			result = result + " @" + String.format("%.4f", this.answerRanking + 1);			
		}
	
		return result;
	}

	/**
	 * To String
	 */
	public String toLogString() {
		
		String result = new String();

		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;

		if(this.insertedKeywordIndex != -1) {
			result = result + " #" + Integer.toString(this.insertedKeywordIndex + 1);			
			
			result = result + " [";
			if(this.isLevelOneCorrect) {
				result = result + "1";
				
				if(this.isLevelTwoCorrect) {
					result = result + "2";
					
					if(this.isLevelThreeCorrect) {
						result = result + "3";
					}
				}
			}
			if(this.isCorrect) {
				result = result + "C";
			}
			result = result + "]";

			result = result + " dis=" + String.format("%.4f", this.structuralDistance);
			result = result + " sim=" + String.format("%.4f", this.structuralSimilarity);
			result = result + " @" + String.format("%d", (int)(this.answerRanking + 1));			
		}
		else if(this.isValid()){
			result = result + " (score=" + String.format("%.4f", this.score);
			result = result + ", rank=" + String.format("%d", (int)this.rank + 1) + ")";
			
			if(this.isAnswer) {
				result = result + " *Answer";
			}
		}

		return result;
	}
	
	/**
	 * To String
	 */
	public String toAnalysisStringForExcel() {
		
		String result = new String();		
		result = result + "\"" + this.classificationName + "\"";

		result = result + "," + String.format("%.4f", this.score);
		
		if(this.insertedKeywordIndex != -1) {
			result = result + ",\"";
			if(this.isLevelOneCorrect) {
				result = result + "1";
				
				if(this.isLevelTwoCorrect) {
					result = result + "2";
					
					if(this.isLevelThreeCorrect) {
						result = result + "3";
					}
				}
			}
			if(this.isCorrect) {
				result = result + "C";
			}
			result = result + "\"";
			
			result = result + "," + String.format("%.4f", this.structuralDistance);
			result = result + "," + String.format("%.4f", this.structuralSimilarity);
			result = result + "," + String.format("%.4f", this.answerRanking + 1);
		}

		return result;
	}
	
	/**
	 * To new string for Excel
	 */
	public String toNewStringForExcel() {
		
		String result = "\"";

		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;
		
		if(this.example.size() > 0) {
			result = result + " ( Example = ";
			for(int i=0; i<this.example.size(); i++) {
				result = result + example.get(i) + " ";
			}
			result = result + ")";
		}
		
		if(this.isRelatedTo.size() > 0) {
			result = result + " < Related = ";
			for(int i=0; i<this.isRelatedTo.size(); i++) {
				result = result + isRelatedTo.get(i) + " ";
			}
			result = result + ">";
		}

		boolean isChanged = false;
		
		if(this.isRetired1991) {
			if(this.classificationNumber.getIsLeaf()) {
				result = result + "\", [1991 Deleted]";
			}
			else {
				result = result + "\", [1991 Deleted Note!]";				
			}
			
			isChanged = true;
		}

		if(this.isRetired1998) {
			if(this.classificationNumber.getIsLeaf()) {
				result = result + "\", [1998 Deleted]";
			}
			else {
				result = result + "\", [1998 Deleted Note!]";				
			}
			
			isChanged = true;
		}

		if(this.isNew) {
			if(this.classificationNumber.getIsLeaf()) {
				result = result + "\", [1998 New]";				
			}
			else {
				result = result + "\", [1998 New Note!]";
			}

			isChanged = true;
			
		}
		
		if(!isChanged) {
			result = result + "\"";
		}

		return result;
	}
	
	/**
	 * Print all string to System.out
	 */
	public void printNewForExcel() {
		System.out.println(this.toNewStringForExcel());
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.printNewForExcel();
			}
		}
	}
	
	/**
	 * Print all string to System.out
	 */
	public void logAnalysisForExcel(String fileName, Vector<ACMTreeNode> targetChildNodeVector) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		for(int i=0; i<targetChildNodeVector.size(); i++) {
			ACMTreeNode child = targetChildNodeVector.get(i);
			pw.println(child.toAnalysisStringForExcel());
		}
		pw.close();
	}
	
	public double computeRankBasedAUC(Vector<ACMTreeNode> targetChildNodeVector, int max) throws Exception {
		double[] data = new double[targetChildNodeVector.size()];
		for(int i=0; i<targetChildNodeVector.size(); i++) {
			ACMTreeNode child = targetChildNodeVector.get(i);
			data[i] = child.getAnswerRanking()+1;
		}
		return Utility.computeRankBasedAUC(data, max);
	}

	
	/**
	 * Print all string to System.out
	 */
	public void logAll(String fileName, boolean newOnly) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		this.logAll(pw, newOnly);
		pw.close();
	}
	
	/**
	 * Print all string to System.out
	 */
	public void logAll(PrintWriter pw, boolean newOnly) {
		
		if(newOnly) {
			if(this.insertedKeywordIndex > -1) {
				pw.println(this.toLogString());
			}
		}
		else {
			pw.println(this.toLogString());
		}
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.logAll(pw, newOnly);
			}
		}
	}
	
	/**
	 * Print all string to System.out
	 */
	public void logLevelOne(String fileName, boolean newOnly) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		this.logLevelOne(pw, newOnly);
		pw.close();
	}
	
	/**
	 * Print all string to System.out
	 */
	public void logLevelOne(PrintWriter pw, boolean newOnly) {
		
		if(newOnly) {
			if(this.insertedKeywordIndex > -1) {
				pw.println(this.toLogString());
			}
		}
		else {
			if(this.classificationNumber != null) {
				if(this.classificationNumber.classificationLevel==1 || this.isAnswer || this.insertedKeywordIndex > -1) {
					pw.println(this.toLogString());
				}					
			}
		}
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.logLevelOne(pw, newOnly);
			}
		}
	}
	
	/**
	 * Print all string to System.out
	 */
	public void printAll() {
		System.out.println(this.toString());
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.printAll();
			}
		}
	}
	
	/**
	 * To Similarity String
	 */
	public String toSimilarityString() {
		
		String result = new String();

		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;
		result = result + " (";
		result = result + this.similarity;
		result = result + ")";

		return result;
	}

	/**
	 * To Web Page Number String
	 */
	public String toWebPageNumberString() {
		
		String result = new String();

		if(this.classificationNumber != null) {
			for(int i=0; i<this.classificationNumber.getClassificationLevel(); i++) {
				result = result + "  ";
			}
			result = result + this.classificationNumber.getClassificationNumber();
			result = result + " ";
		}
		
		result = result + this.classificationName;
		result = result + " ((";
		result = result + this.webPageNumber;
		result = result + "))";

		return result;
	}

	/**
	 * Get nodes with maximum similarity
	 */
	public Vector<ACMTreeNode> getMaximumSimilarityNodes(Vector<ACMTreeNode> maxVector, double maxSimilarity, boolean newAsParent) {
		
		if(maxSimilarity == this.similarity) {
			if(newAsParent) {
				maxVector.add(this);
			}
			else {
				if(this.insertedKeywordIndex == -1) {
					maxVector.add(this);
				}
			}
		}
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				maxVector = child.getMaximumSimilarityNodes(maxVector, maxSimilarity, newAsParent);
			}
		}
		
		return maxVector;
	}

	/**
	 * Find maximum similarity
	 */
	public double findMaximumSimilarity(double maxSimilarity, boolean newAsParent) {
		
		if(maxSimilarity < this.similarity) {
			if(newAsParent) {
				maxSimilarity = this.similarity;
			}
			else {
				if(this.insertedKeywordIndex == -1) {
					maxSimilarity = this.similarity;
				}
			}
		}
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				maxSimilarity = child.findMaximumSimilarity(maxSimilarity, newAsParent);
			}
		}
		
		return maxSimilarity;
	}
	
	/**
	 * Print similarity string to System.out
	 */
	public void printWebPageNumbers() {
		System.out.println(this.toWebPageNumberString());
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.printWebPageNumbers();
			}
		}
	}
	
	/**
	 * Print similarity string within targetLevel to System.out
	 */
	public void printWebPageNumbers(int level) {
		
		System.out.println(this.toWebPageNumberString());
		
		if(level > 0) {
			if(this.classificationNumber == null || this.classificationNumber.getClassificationLevel() < level) {
				if(childNode != null) {
					for(int i=0; i<this.childNode.size(); i++) {
						ACMTreeNode child = this.childNode.get(i);
						child.printWebPageNumbers(level);
					}
				}
			}
		}
	}
	
	/**
	 * Print similarity string to System.out
	 */
	public int printTesting(int index) {
		
		if(this.isValid()) {
			String result = this.toTestingString() + " @ " + Integer.toString(index + 1);
			System.out.println(result);
			index++;
		}		
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				index = child.printTesting(index);
			}
		}
		
		return index;
	}
	
	/**
	 * Print similarity string to System.out
	 */
	public void printSimilarities() {
		System.out.println(this.toSimilarityString());
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				child.printSimilarities();
			}
		}
	}

	/**
	 * Print similarity string within targetLevel to System.out
	 */
	public int getLevelCount(int level, int count) {
		
		if(this.isValid()) {
			if(this.classificationNumber == null || this.classificationNumber.getClassificationLevel() == level) {
				count++;
			}
		}
		
		if(childNode != null) {
			for(int i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				count = child.getLevelCount(level, count);
			}
		}
		
		return count;
	}

	/**
	 * Print similarity string within targetLevel to System.out
	 */
	public void printSimilarities(int level) {
		
		System.out.println(this.toSimilarityString());
		
		if(level > 0) {
			if(this.classificationNumber == null || this.classificationNumber.getClassificationLevel() < level) {
				if(childNode != null) {
					for(int i=0; i<this.childNode.size(); i++) {
						ACMTreeNode child = this.childNode.get(i);
						child.printSimilarities(level);
					}
				}
			}
		}
	}
	
	/**
	 * Parse a line in ACM Tree
	 * @param line the line to be parsed
	 * @param isLevelOne if this is level one, set level to 1; otherwise the level must be recalculated
	 * @param previousNumber if this is level four, we need previous number to get this number
	 */
	public void parseLine(String line, ACMTreeClassificationNumber previousNumber) {

		int i = 0;
		
		for(i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if(c==' ') {
				break;
			}
		}

		String possibleNumber = line.substring(0, i);
		String possibleName = line.substring(0, i);
		
		if (i < line.length()) {
			possibleName = line.substring(i + 1, line.length());
		}
		
		this.classificationNumber = new ACMTreeClassificationNumber();
		String returnName = this.classificationNumber.setClassificationNumber(possibleNumber, previousNumber);

		// Get the rest information
		if (returnName == "") {
			this.classificationName = possibleName;
		}
		else {
			this.classificationName = line;
		}
		this.classificationName = this.classificationName.trim();
		
		// Check "is no longer used" for 1998
		int index1998 = this.classificationName.lastIndexOf(" [**]");
		if(index1998 >= 0) {
			this.isRetired1998 = true;
			this.classificationName = this.classificationName.substring(0, index1998);
		}

		// Check "is no longer used" for 1991 (1998 version)
		int index1991 = this.classificationName.lastIndexOf(" [*]");
		if(index1991 >= 0) {
			this.isRetired1991 = true;
			this.classificationName = this.classificationName.substring(0, index1991);
		}

		// Check "is no longer used" for 1991 (1991 version)
		int index1991Old = this.classificationName.lastIndexOf("*");
		if(index1991Old >= 0) {
			this.isRetired1991 = true;
			this.classificationName = this.classificationName.substring(0, index1991Old);
		}

		// Check examples
		int indexExample = this.classificationName.lastIndexOf(" (e.g., ");
		if(indexExample >= 0) {
			String exampleString = this.classificationName.substring(indexExample + 8, this.classificationName.length()-1);
			this.classificationName = this.classificationName.substring(0, indexExample);
			int nextIndex = exampleString.indexOf(", ");
			while (nextIndex != -1) {
				this.example.add(exampleString.substring(0, nextIndex));
				exampleString = exampleString.substring(nextIndex + 2, exampleString.length());
				nextIndex = exampleString.indexOf(", ");
			}
			this.example.add(exampleString);
		}

		// Check isRelatedTo, which only occurs in "non-Leaf" levels
		if(this.classificationNumber.isLeaf == false) {
			int indexRelated = this.classificationName.lastIndexOf(" (");
			if(indexRelated >= 0) {
				String relatedString = this.classificationName.substring(indexRelated + 2, this.classificationName.length()-1);
				this.classificationName = this.classificationName.substring(0, indexRelated);
				int nextIndex = relatedString.indexOf(", ");
				while (nextIndex != -1) {
					this.isRelatedTo.add(relatedString.substring(0, nextIndex));
					relatedString = relatedString.substring(nextIndex + 2, relatedString.length());
					nextIndex = relatedString.indexOf(", ");
				}
				this.isRelatedTo.add(relatedString);
			}
		}
		
		// Check parenthesis. This step might need to be changed in future.
		int indexStart = this.classificationName.lastIndexOf(" (");
		if(indexStart >= 0) {
			int indexStop = this.classificationName.lastIndexOf(")");
			this.parenthesis = this.classificationName.substring(indexStart + 2, indexStop); 
			String beforeString = this.classificationName.substring(0, indexStart);
			String afterString = this.classificationName.substring(indexStop + 1, this.classificationName.length());
			this.classificationName = beforeString + afterString;
		}

		// Check if this is miscellaneous
		if(this.classificationName.equalsIgnoreCase("miscellaneous")) {
			this.isMiscellaneous = true;
		}
		
		// Check if this is general
		if(this.classificationName.equalsIgnoreCase("general")) {
			this.isGeneral = true;
		}
		
		// Check possible keywords, which should be considered in future

		// Leafly, post-process the classification name
		this.classificationName = this.classificationName.trim();
		this.classificationName = this.classificationName.replace("/", " ");
		this.classificationName = this.classificationName.replace("-", " ");
		this.classificationName = this.classificationName.replace(",", "");
	}
	
	/**
	 * Create node by its line
	 * @param childLine the line of child
	 * @param currentLevel current loading level
	 */
	public ACMTreeNode createNode(String childLine, ACMTreeClassificationNumber previousNumber, boolean isInterpro) {
		ACMTreeNode childNode = new ACMTreeNode(isInterpro);
		childNode.parseLine(childLine, previousNumber);
		return childNode;
	}

	/**
	 * Add child node
	 * @param childNode the child node
	 */
	public void addChildNode(ACMTreeNode childNode) {
		childNode.setParentNode(this);
		this.getChildNode().add(childNode);		
	}

	/**
	 * Create (but not insert) child node, besides adding a node,
	 * also compute the classification number of it
	 */
	public ACMTreeNode createNewChildNodeAsLeaf(String childName, int index, double webPageNumber, boolean isInterpro) {
		ACMTreeNode childNode = new ACMTreeNode(isInterpro);
		childNode.setClassificationName(childName);
		childNode.setClassificationNumber(new ACMTreeClassificationNumber());
		childNode.getClassificationNumber().setIsLeaf(true);
		childNode.setInsertedKeywordIndex(index);
		childNode.setWebPageNumber(webPageNumber);
		return childNode;
	}

	/**
	 * Insert child node, besides adding a node,
	 * also compute the classification number of it
	 */
	public ACMTreeNode insertNewChildNodeAsLeaf(String childName, int index, double webPageNumber, boolean isInterpro) {
		ACMTreeNode childNode = new ACMTreeNode(isInterpro);
		childNode.setClassificationName(childName);
		childNode.setClassificationNumber(new ACMTreeClassificationNumber());
		childNode.getClassificationNumber().setIsLeaf(true);
		childNode.setInsertedKeywordIndex(index);
		childNode.setWebPageNumber(webPageNumber);
		this.insertChildNode(childNode);
		return childNode;
	}

	/**
	 * Insert child node, besides adding a node,
	 * also compute the classification number of it
	 * @param childNode the child node
	 */
	public void insertChildNode(ACMTreeNode childNode) {
		
		String childNodeNumber = "";
		
		if(this.classificationNumber == null) {		// Root
			char c = (char)(this.childNode.size() + 65);
			childNodeNumber += c;
			childNode.classificationNumber.setClassificationLevel(1);
		}
		else {										// Non-root
			childNodeNumber = this.classificationNumber.getClassificationNumber();
			childNodeNumber += ".";
			childNodeNumber += Integer.toString(this.childNode.size());
			
			if(this.classificationNumber.getIsLeaf()) {
				this.classificationNumber.setIsLeaf(false);
				childNode.classificationNumber.setIsLeaf(true);
			}

			childNode.classificationNumber.setClassificationLevel(this.classificationNumber.getClassificationLevel() + 1);		
		}
		
		childNode.getClassificationNumber().setClassificationNumber(childNodeNumber);
		this.addChildNode(childNode);	
	}

	/**
	 * Insert child node under this node and above grand child node
	 * @param childNode the child node
	 * @param grandChildNode the grand child node
	 */
	public void insertChildNodeBetween(ACMTreeNode childNode, ACMTreeNode grandChildNode) {
		
		childNode.getClassificationNumber().setClassificationNumber(grandChildNode.getClassificationNumber().getClassificationNumber());
		childNode.getClassificationNumber().setClassificationLevel(grandChildNode.getClassificationNumber().getClassificationLevel());
		childNode.getClassificationNumber().setIsLeaf(false);
		
		int index = this.childNode.indexOf(grandChildNode);
		this.childNode.setElementAt(childNode, index);
		childNode.insertChildNode(grandChildNode);
		
		grandChildNode.updateClassificationNumber();
	}
	
	/**
	 * Update number of all child nodes recursively
	 */
	public void updateClassificationNumber() {
		
		for(int i=0; i<this.childNode.size(); i++) {
			String number = this.getClassificationNumber().getClassificationNumber();
			int level = this.getClassificationNumber().getClassificationLevel();
			ACMTreeNode child = this.childNode.get(i);
			ACMTreeClassificationNumber childNumber = child.getClassificationNumber();
			childNumber.setClassificationNumber(number + "." + i);
			childNumber.setClassificationLevel(level + 1);
			
			child.updateClassificationNumber();
		}
	}
	
	
	/**
	 * Load Tree recursively
	 * @param nodeVector the vector which contains lines to load
	 * @param currentLevel current loading level
	 */
	public void loadTree(Vector<ACMTreeNode> nodeVector) {
		
		// Make sure we still need to work
		/*
		if(nodeVector.size() == 0) {
			return;
		}
		*/
		int currentLevel = 0;
		
		if(this.getClassificationNumber() != null) {	// Non-root
			currentLevel = this.getClassificationNumber().getClassificationLevel();
		}
		//System.out.println(this + " (" + nodeVector.size() + ")");
		
		
		// Find all next level nodes first
		Vector<Integer> nextLevelIndexes = new Vector<Integer>();
		
		for(int i=0; i<nodeVector.size(); i++) {
			ACMTreeNode nextNode = nodeVector.get(i);		
			int nextLevel = nextNode.getClassificationNumber().getClassificationLevel();
			if(nextLevel == currentLevel + 1) {
				nextLevelIndexes.add(new Integer(i));
				this.addChildNode(nextNode);
			}
		}
		
		if (nextLevelIndexes.size() > 0) {		// Have child
			int j=0;
			for(j=0; j<nextLevelIndexes.size()-1; j++) {
				int startIndex = ((Integer)nextLevelIndexes.get(j)).intValue();
				int stopIndex = ((Integer)nextLevelIndexes.get(j+1)).intValue();
				ACMTreeNode nextNode = nodeVector.get(startIndex);
				Vector<ACMTreeNode> nextNodes = new Vector<ACMTreeNode>();
				for (int k=startIndex+1; k<stopIndex; k++) {
					nextNodes.add(nodeVector.get(k));
				}			
				nextNode.loadTree(nextNodes);
			}
				
			int startIndex = ((Integer)nextLevelIndexes.get(j)).intValue();
			ACMTreeNode nextNode = nodeVector.get(startIndex);
			Vector<ACMTreeNode> nextNodes = new Vector<ACMTreeNode>();
			for (int k=startIndex+1; k<nodeVector.size(); k++) {
				nextNodes.add(nodeVector.get(k));
			}
			nextNode.loadTree(nextNodes);
		}
		else {									// Do not have child, this is Leaf !
			this.classificationNumber.setIsLeaf(true);
		}
	}

	/**
	 * Load ACM Tree from file and adjust it
	 * @param fileName the file name to be loaded
	 * @return unique keywords
	 */
	public Vector<String> loadAndAdjustACMTree(String fileName, boolean isInterpro) throws IOException {
		loadACMTreeFromFile(fileName, isInterpro);
		Vector<String> uniqueKeywords = findUniqueKeywords(new Vector<String>(), this);
		adjustRepeatedName();
		return uniqueKeywords;
	}
	
	/**
	 * Load ACM Tree from file, only called by root
	 * @param fileName the file name to be loaded
	 */
	public void loadACMTreeFromFile(String fileName, boolean isInterpro) throws IOException {
		Vector<ACMTreeNode> result = new Vector<ACMTreeNode>();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;
		ACMTreeClassificationNumber previousNumber = this.classificationNumber;		

		while ((s=lnr.readLine()) != null) {
			ACMTreeNode n = this.createNode(s, previousNumber, isInterpro);
			previousNumber = n.getClassificationNumber();
			result.add(n);
		}
		
		loadTree(result);
		
		lnr.close();
		fr.close();
	}
	
	/**
	 * @return the classificationNumber
	 */
	public ACMTreeClassificationNumber getClassificationNumber() {
		return classificationNumber;
	}

	/**
	 * @param classificationNumber the classificationNumber to set
	 */
	public void setClassificationNumber(
			ACMTreeClassificationNumber classificationNumber) {
		this.classificationNumber = classificationNumber;
	}

	/**
	 * @return the classificationName
	 */
	public String getClassificationName() {
		return classificationName;
	}

	/**
	 * @param classificationName the classificationName to set
	 */
	public void setClassificationName(String classificationName) {
		this.classificationName = classificationName;
	}

	/**
	 * @return the isRelatedTo
	 */
	public Vector<String> getIsRelatedTo() {
		return isRelatedTo;
	}

	/**
	 * @return the possibleKeywords
	 */
	public Vector<String> getPossibleKeywords() {
		return possibleKeywords;
	}

	/**
	 * @param possibleKeywords the possibleKeywords to set
	 */
	public void setPossibleKeywords(Vector<String> possibleKeywords) {
		this.possibleKeywords = possibleKeywords;
	}

	/**
	 * @param isRelatedTo the isRelatedTo to set
	 */
	public void setIsRelatedTo(Vector<String> isRelatedTo) {
		this.isRelatedTo = isRelatedTo;
	}

	/**
	 * @return the isMiscellaneous
	 */
	public Boolean getIsMiscellaneous() {
		return isMiscellaneous;
	}

	/**
	 * @param isMiscellaneous the isMiscellaneous to set
	 */
	public void setIsMiscellaneous(Boolean isMiscellaneous) {
		this.isMiscellaneous = isMiscellaneous;
	}

	/**
	 * @return the isRetired1991
	 */
	public Boolean getIsRetired1991() {
		return isRetired1991;
	}

	/**
	 * @param isRetired1991 the isRetired1991 to set
	 */
	public void setIsRetired1991(Boolean isRetired1991) {
		this.isRetired1991 = isRetired1991;
	}

	/**
	 * @return the isRetired1998
	 */
	public Boolean getIsRetired1998() {
		return isRetired1998;
	}

	/**
	 * @param isRetired1998 the isRetired1998 to set
	 */
	public void setIsRetired1998(Boolean isRetired1998) {
		this.isRetired1998 = isRetired1998;
	}

	/**
	 * @return the parentNode
	 */
	public ACMTreeNode getParentNode() {
		return parentNode;
	}

	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(ACMTreeNode parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * @return the childNode
	 */
	public Vector<ACMTreeNode> getChildNode() {
		return childNode;
	}

	/**
	 * @param childNode the childNode to set
	 */
	public void setChildNode(Vector<ACMTreeNode> childNode) {
		this.childNode = childNode;
	}

	/**
	 * @return the example
	 */
	public Vector<String> getExample() {
		return example;
	}

	/**
	 * @param example the example to set
	 */
	public void setExample(Vector<String> example) {
		this.example = example;
	}

	public void printNodeVector(Vector<ACMTreeNode> nodeVector) {
		for(int i=0; i<nodeVector.size(); i++) {
			ACMTreeNode node = nodeVector.get(i);
			System.out.println(node.getClassificationName());
		}
		System.out.println();
		System.out.println("Total = " + nodeVector.size());
	}

	/**
	 * @return the insertedKeywordIndex
	 */
	public int getInsertedKeywordIndex() {
		return insertedKeywordIndex;
	}

	/**
	 * @param insertedKeywordIndex the insertedKeywordIndex to set
	 */
	public void setInsertedKeywordIndex(int insertedKeywordIndex) {
		this.insertedKeywordIndex = insertedKeywordIndex;
	}

	/**
	 * @return the webPageNumber
	 */
	public double getWebPageNumber() {
		return webPageNumber;
	}

	/**
	 * @param webPageNumber the webPageNumber to set
	 */
	public void setWebPageNumber(double webPageNumber) {
		this.webPageNumber = webPageNumber;
	}

	/**
	 * @return the isNew
	 */
	public Boolean getIsNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}

	/**
	 * @return the isUnique
	 */
	public Boolean getIsUnique() {
		return isUnique;
	}

	/**
	 * @param isUnique the isUnique to set
	 */
	public void setIsUnique(Boolean isUnique) {
		this.isUnique = isUnique;
	}

	/**
	 * @return the isGeneral
	 */
	public Boolean getIsGeneral() {
		return isGeneral;
	}

	/**
	 * @param isGeneral the isGeneral to set
	 */
	public void setIsGeneral(Boolean isGeneral) {
		this.isGeneral = isGeneral;
	}

	/**
	 * @return the parenthesis
	 */
	public String getParenthesis() {
		return parenthesis;
	}

	/**
	 * @param parenthesis the parenthesis to set
	 */
	public void setParenthesis(String parenthesis) {
		this.parenthesis = parenthesis;
	}

	/**
	 * @return the similarity
	 */
	public double getSimilarity() {
		return similarity;
	}

	/**
	 * @param similarity the similarity to set
	 */
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	/**
	 * @return the isLevelOneCorrect
	 */
	public boolean isLevelOneCorrect() {
		return isLevelOneCorrect;
	}

	/**
	 * @param isLevelOneCorrect the isLevelOneCorrect to set
	 */
	public void setLevelOneCorrect(boolean isLevelOneCorrect) {
		this.isLevelOneCorrect = isLevelOneCorrect;
	}

	/**
	 * @return the isLevelTwoCorrect
	 */
	public boolean isLevelTwoCorrect() {
		return isLevelTwoCorrect;
	}

	/**
	 * @param isLevelTwoCorrect the isLevelTwoCorrect to set
	 */
	public void setLevelTwoCorrect(boolean isLevelTwoCorrect) {
		this.isLevelTwoCorrect = isLevelTwoCorrect;
	}

	/**
	 * @return the isLevelThreeCorrect
	 */
	public boolean isLevelThreeCorrect() {
		return isLevelThreeCorrect;
	}

	/**
	 * @param isLevelThreeCorrect the isLevelThreeCorrect to set
	 */
	public void setLevelThreeCorrect(boolean isLevelThreeCorrect) {
		this.isLevelThreeCorrect = isLevelThreeCorrect;
	}

	/**
	 * @return the isCorrect
	 */
	public boolean isCorrect() {
		return isCorrect;
	}

	/**
	 * @param isCorrect the isCorrect to set
	 */
	public void setCorrect(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	/**
	 * @return the answerRanking
	 */
	public double getAnswerRanking() {
		return answerRanking;
	}

	/**
	 * @param answerRanking the answerRanking to set
	 */
	public void setAnswerRanking(double answerRanking) {
		this.answerRanking = answerRanking;
	}

	/**
	 * @return the structuralDistance
	 */
	public double getStructuralDistance() {
		return structuralDistance;
	}

	/**
	 * @param structuralDistance the structuralDistance to set
	 */
	public void setStructuralDistance(double structuralDistance) {
		this.structuralDistance = structuralDistance;
	}

	/**
	 * @return the structuralSimilarity
	 */
	public double getStructuralSimilarity() {
		return structuralSimilarity;
	}

	/**
	 * @param structuralSimilarity the structuralSimilarity to set
	 */
	public void setStructuralSimilarity(double structuralSimilarity) {
		this.structuralSimilarity = structuralSimilarity;
	}

	/**
	 * Adjust similarity by the average similarity of children, weighted by factor
	 */
	public double adjustScoreByChildAverageBottomUp(double factor) {
	
		double adjustment = 0;
		
		if(this.childNode.size() > 0) {
			int i;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					adjustment += child.adjustScoreByChildAverageBottomUp(factor);
				}
			}
			
			adjustment = (double)adjustment / (double)i;
			adjustment *= (double)factor;
			this.score *= (double)(1 - factor);
			this.score += adjustment;
		}
		
		return this.score;
	}

	/**
	 * Adjust similarity by the average similarity of children, weighted by factor
	 */
	public void adjustScoreByChildAverageTopDown(double factor) {
	
		double adjustment = 0;
		
		if(this.childNode.size() > 0) {
			int i;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					adjustment += child.score;
				}
			}
			
			adjustment = (double)adjustment / (double)i;
			adjustment *= (double)factor;
			this.score *= (double)(1 - factor);
			this.score += adjustment;
	
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.adjustScoreByChildAverageTopDown(factor);
				}
			}
		}
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public double adjustScoreByChildMaximumBottomUp(double factor) {
	
		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.adjustScoreByChildMaximumBottomUp(factor); 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.score *= (double)(1 - factor);
			this.score += childMax;
		}
		
		return this.score;
	}

	/**
	 * Adjust similarity by the maximum similarity of children, weighted by factor
	 */
	public void adjustScoreByChildMaximumTopDown(double factor) {
	
		if(this.childNode.size() > 0) {
			int i;
			double childMax = 0;
			
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					double childSim = child.score; 
					if(childMax < childSim) {
						childMax = childSim;
					}
				}
			}
			
			childMax *= (double)factor;
			this.score *= (double)(1 - factor);
			this.score += childMax;
	
			for(i=0; i<this.childNode.size(); i++) {
				ACMTreeNode child = this.childNode.get(i);
				if(child.isValid()) {
					child.adjustScoreByChildMaximumTopDown(factor);
				}
			}
		}
	}
	
	/*
	 * Output XML file for Prefuse
	 */
	public void outputXMLForPrefuse(String fileName, String childName, int suggestRank) throws Exception{
		PrintWriter pw = new PrintWriter(fileName);
		this.outputXMLForPrefuse(pw, childName, suggestRank);
		pw.close();
	}

	/*
	 * Output XML file for Prefuse
	 */
	public void outputXMLForPrefuse(PrintWriter pw, String childName, int suggestRank) throws Exception{
		
		if(this.classificationNumber == null) {		// Root
			pw.println("<tree>");
			pw.println(" <declarations>");
			pw.println("   <attributeDecl name=\"name\" type=\"String\"/>");
			pw.println("   <attributeDecl name=\"score\" type=\"Double\"/>");
			pw.println("   <attributeDecl name=\"type\" type=\"Integer\"/>");
			pw.println("   <attributeDecl name=\"correct\" type=\"Integer\"/>");
			pw.println(" </declarations>");
		}
		
		if(this.insertedKeywordIndex == -1) {		// Print 91-tree only
			if(this.childNode.size() > 0) {
				pw.println(" <branch>");
				
				if(this.classificationNumber==null && childName!=null) {		// Root
					pw.println("  <attribute name=\"name\" value=\"" + childName + "\"/>");
				}
				else {
					pw.println("  <attribute name=\"name\" value=\"" + this.getClassificationName() + "\"/>");
				}
				
				if(suggestRank==-1 || this.rank < suggestRank) {
					pw.println("  <attribute name=\"score\" value=\"" + String.format("%.4f", this.score) + "\"/>");
				}
				else {
					pw.println("  <attribute name=\"score\" value=\"0.0000\"/>");					
				}

				pw.println("  <attribute name=\"type\" value=\"" + this.type + "\"/>");
				pw.println("  <attribute name=\"correct\" value=\"" + this.correctNumber + "\"/>");
				for(int i=0; i<this.childNode.size(); i++) {
					ACMTreeNode child = this.childNode.get(i);
					if(child.isValid()) {
						child.outputXMLForPrefuse(pw, childName, suggestRank);
					}
				}
				pw.println(" </branch>");
			}
			else {
				pw.println(" <leaf>");						
				pw.println("  <attribute name=\"name\" value=\"" + this.getClassificationName() + "\"/>");
				
				if(suggestRank==-1 || this.rank < suggestRank) {
					pw.println("  <attribute name=\"score\" value=\"" + String.format("%.4f", this.score) + "\"/>");
				}
				else {
					pw.println("  <attribute name=\"score\" value=\"0.0000\"/>");					
				}
				
				pw.println("  <attribute name=\"type\" value=\"" + this.type + "\"/>");
				pw.println("  <attribute name=\"correct\" value=\"" + this.correctNumber + "\"/>");
				pw.println(" </leaf>");
			}
		}
		
		if(this.classificationNumber == null) {		// Root
			pw.println("</tree>");
		}
	}

	/**
	 * @return the author
	 */
	public double getAuthor1() {
		return author1;
	}

	/**
	 * @return the author
	 */
	public double getAuthor2() {
		return author2;
	}
	
	/**
	 * @return the author
	 */
	public double getAuthor12() {
		return author12;
	}

	/**
	 * @return the jaccard
	 */
	public double getJaccard() {
		return jaccard;
	}

	/**
	 * @param jaccard the jaccard to set
	 */
	public void setJaccard(double jaccard) {
		this.jaccard = jaccard;
	}

	/**
	 * @return the isAnswer
	 */
	public boolean isAnswer() {
		return isAnswer;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the rank
	 */
	public double getRank() {
		return rank;
	}

	/**
	 * @return the intersection
	 */
	public double getIntersection() {
		return intersection;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the correctNumber
	 */
	public int getCorrectNumber() {
		return correctNumber;
	}

	/**
	 * @return the levelOneClassNumber
	 */
	public int getLevelOneClassNumber() {
		return levelOneClassNumber;
	}

	/**
	 * @return the word
	 */
	public double getWord() {
		return word;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param correctNumber the correctNumber to set
	 */
	public void setCorrectNumber(int correctNumber) {
		this.correctNumber = correctNumber;
	}
	
	public double getAverageBranchingFactor() {
		return (double)this.getTotalBranchingFactor() / (double)this.getNonLeafNumber();
	}
	
	public int getTotalBranchingFactor() {
		int bf = this.getChildNode().size();
		for(ACMTreeNode child : this.childNode) {
			bf += child.getTotalBranchingFactor();
		}
		return bf;
	}
	
	public int getNonLeafNumber() {
		int nl = this.getChildNode().size(); 
		if(nl > 0) {
			nl = 1;
		}
		for(ACMTreeNode child : this.childNode) {
			nl += child.getNonLeafNumber();
		}
		return nl;
	}
}
