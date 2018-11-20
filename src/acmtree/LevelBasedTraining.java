package acmtree;
/**
 * 
 */

import java.util.*;



/**
 * @author Tim Kuo
 *
 */
public class LevelBasedTraining {
		
	public static void main(String[] args) throws Exception{
		// Parameter
		String fileSuffix = "m1_g0_lv_";
		double alpha = 0.8;					// Structural similarity computation
		double beta = 0.15;					// Semantic similarity adjustment
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size, default = -1
		double structuralThreshold = -1.0;	// Structural threshold to reduce data size, default = -1
		double possitiveThreshold = 1.0;	// Positive data threshold, default = 1
		boolean useAnd = true;				// Use and / or for the two thresholds
		boolean singleFeature = false;		// Use 1 feature only
		int upsampling = 1;					// Repeat output for positive data
		int adjustSimilarity = -1;			// -1 = none
		
		LevelBasedTraining.run(fileSuffix, alpha, beta, semanticThreshold, structuralThreshold, useAnd, singleFeature, upsampling, adjustSimilarity, possitiveThreshold);
	}
	
	public static int run(String fileSuffix, double alpha, double beta, double semanticThreshold, double structuralThreshold, boolean useAnd, boolean singleFeature, int upsampling, int adjustSimilarity, double possitiveThreshold) throws Exception{
		

		// Load 91 tree and 98 tree
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt", false);
		
		// Load NGD similarities from File
		ACMTreeSimilarity ns = new ACMTreeSimilarity(false);
		ns.loadAllNGDSimilarities();
	
		// Load web page number for original keywords
		root91.loadWebPageNumber(ns.getTree91(), 0);
		System.out.println("91-Tree loading complete.");
		System.out.println();
		
		// Generate training data
		//for(double semanticThreshold = -0.1; semanticThreshold <= 1.0; semanticThreshold += 0.1) {
			Vector<LevelBasedDataInstance> trainingData = root91.generateLevelBasedTrainingData(ns.getTree91ToTree91NGD(), alpha, semanticThreshold, structuralThreshold, useAnd, beta, adjustSimilarity, possitiveThreshold);
			System.out.println("Training data generation complete, size = " + trainingData.size());
			System.out.println();
		//}
		
		// Output training data

		root91.outputLevelBasedDataForWekaClassification("training_data_classification_" + fileSuffix + ".arff", trainingData, singleFeature);
		root91.outputLevelBasedDataForLibSVC("training_data_classification_" + fileSuffix + ".txt", trainingData, singleFeature);
		System.out.println("Training data output complete.");
		System.out.println();
		
		return trainingData.size();
	}
}
