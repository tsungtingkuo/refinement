package acmtree;
/**
 * 
 */

import java.util.*;



/**
 * @author Tim Kuo
 *
 */
public class DOAJTrainingDataGeneration {
		
	public static void main(String[] args) throws Exception{
		// Parameter
		String fileSuffix = "full";
		double alpha = 0.8;					// Structural similarity computation
		double beta = 0.15;					// Semantic similarity adjustment
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size, default = -1
		double structuralThreshold = -1.0;	// Structural threshold to reduce data size, default = -1
		double possitiveThreshold = 1.0;	// Positive data threshold, default = 1
		boolean enrichment = false;			// Use advanced features or not
		boolean useAnd = true;				// Use and / or for the two thresholds
		boolean singleFeature = false;		// Use 1 feature only
		int method = 22;					// -1 = random, 0 = baseline, 6 = proposed-1, 20 = proposed-2, 22 = proposed-3 
		int features = 0;					// 0=full, 1=topology, 2=content, 3=social
		int upsampling = 1;					// Repeat output for positive data
		int adjustSimilarity = -1;			// -1 = none
		int possitiveLevel = 5;				// Additional positive data level, default = 5, valid = 4(5320), 3(36858), 2(160454)
		
		DOAJTrainingDataGeneration.run(fileSuffix, alpha, beta, semanticThreshold, structuralThreshold, useAnd, singleFeature, upsampling, adjustSimilarity, possitiveThreshold, possitiveLevel, method, enrichment, features);
	}
	
	public static int run(String fileSuffix, double alpha, double beta, double semanticThreshold, double structuralThreshold, boolean useAnd, boolean singleFeature, int upsampling, int adjustSimilarity, double possitiveThreshold, int possitiveLevel, int method, boolean enrichment, int features) throws Exception{
		

		// Load 91 tree
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt", false);
		
		// Load NGD similarities from File
		ACMTreeSimilarity ns = new ACMTreeSimilarity(false);
		ns.loadAllNGDSimilarities();
	
		// Load web page number for original keywords
		root91.loadWebPageNumber(ns.getTree91(), 0);
		System.out.println("91-Tree loading complete.");
		System.out.println();
		
		// Load author number
		AuthorSimilarity as = new AuthorSimilarity(false);
		as.loadAndComputeAuthorSimilarities(false);
		
		// Load word similarity
		WordSimilarity ws = new WordSimilarity(false);
		ws.loadAndComputeWordSimilarities();
		
		// Generate training data
		//for(double semanticThreshold = -0.1; semanticThreshold <= 1.0; semanticThreshold += 0.1) {
			Vector<ACMTreeDataInstance> trainingData = root91.generateTrainingData(ns.getTree91ToTree91(), ns.getTree91ToTree91NGD(), ns.getTree91ToTree91Jaccard(), as.getTree91ToTree91Level1(), as.getTree91ToTree91Level2(), as.getTree91ToTree91LevelBoth(), ws.getTree91ToTree91Word(), alpha, semanticThreshold, structuralThreshold, useAnd, beta, adjustSimilarity, possitiveThreshold, possitiveLevel);
			System.out.println("Training data generation complete, size = " + trainingData.size());
			System.out.println();
		//}
		
		// Output training data
		
		if(method == 6 || method == 20 || method == 22) {
			System.out.print("Outputing training data (Weka)...");
			root91.outputDataForWekaClassification("training_data_classification_" + fileSuffix + ".arff", trainingData, singleFeature, upsampling, possitiveThreshold, possitiveLevel, method, enrichment, features);
			System.out.println("done!");
		}
			
		if(method == 20 || method == 22) {
			System.out.print("Outputing training data (LibLinear)...");
			root91.outputDataForLibSVC("training_data_classification_" + fileSuffix + ".txt", trainingData, singleFeature, upsampling, possitiveThreshold, possitiveLevel, method);
			System.out.println("done!");
		}
		
		System.out.println("Training data output complete.");
		System.out.println();
			
		return trainingData.size();
	}
}
