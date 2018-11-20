package acmtree;
/**
 * 
 */

import java.util.*;

/**
 * @author Tim Kuo
 *
 */
public class InterProTrainingDataGeneration {
		
	public static void main(String[] args) throws Exception{
		// Parameter
		//String fileSuffix = "full";
		double alpha = 0.8;					// Structural similarity computation
		double beta = 0.15;					// Semantic similarity adjustment
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size, default = -1
		double structuralThreshold = -1.0;	// Structural threshold to reduce data size, default = -1
		double possitiveThreshold = 1.0;	// Positive data threshold, default = 1
		boolean enrichment = false;			// Use advanced features or not
		boolean useAnd = true;				// Use and / or for the two thresholds
		boolean singleFeature = false;		// Use 1 feature only
		int method = 6;						// -1=Random, 26=ConceptName, 21=Intersection, 16=Jaccard, 0=NGD, 14=KeyAuthor, 18=ExtendedKeyAuthor, 29=OneNorm, 30=TwoNorm, 6=Learning
		int features = 0;					// 0=full, 1=topology, 2=content, 3=social
		int upsampling = 1;					// Repeat output for positive data
		int adjustSimilarity = -1;			// -1 = none
		int possitiveLevel = 5;				// Additional positive data level, default = 5, valid = 4(5320), 3(36858), 2(160454)
		
		InterProTrainingDataGeneration.run(Integer.toString(method), alpha, beta, semanticThreshold, structuralThreshold, useAnd, singleFeature, upsampling, adjustSimilarity, possitiveThreshold, possitiveLevel, method, enrichment, features);
	}
	
	public static int run(String fileSuffix, double alpha, double beta, double semanticThreshold, double structuralThreshold, boolean useAnd, boolean singleFeature, int upsampling, int adjustSimilarity, double possitiveThreshold, int possitiveLevel, int method, boolean enrichment, int features) throws Exception{
		

		// Load 91 tree
		ACMTreeNode gpcr230 = new ACMTreeNode("gpcr230_experiment.txt", true);
		
		// Load NGD similarities from File
		ACMTreeSimilarity ns = new ACMTreeSimilarity(true);
		ns.loadAllNGDSimilarities();
	
		// Load web page number for original keywords
		gpcr230.loadWebPageNumber(ns.getGpcr230(), 0);
		System.out.println("GPCR230 loading complete.");
		System.out.println();
		
		// Load author number
		AuthorSimilarity as = new AuthorSimilarity(true);
		as.loadAndComputeAuthorSimilarities(false);
		
		// Load word similarity
		WordSimilarity ws = new WordSimilarity(true);
		ws.loadAndComputeWordSimilarities();
		
		// Generate training data
		//for(double semanticThreshold = -0.1; semanticThreshold <= 1.0; semanticThreshold += 0.1) {
			Vector<ACMTreeDataInstance> trainingData = gpcr230.generateTrainingData(ns.getGpcr230ToGpcr230(), ns.getGpcr230ToGpcr230NGD(), ns.getGpcr230ToGpcr230Jaccard(), as.getGpcr230ToGpcr230Level1(), as.getGpcr230ToGpcr230Level2(), as.getGpcr230ToGpcr230LevelBoth(), ws.getGpcr230ToGpcr230Word(), alpha, semanticThreshold, structuralThreshold, useAnd, beta, adjustSimilarity, possitiveThreshold, possitiveLevel);
			System.out.println("Training data generation complete, size = " + trainingData.size());
			System.out.println();
		//}
		
		// Output training data
		
		if(method == 6 || method == 20 || method == 22 || method == 29 || method == 30) {
			System.out.print("Outputing training data (Weka)...");
			gpcr230.outputDataForWekaClassification("training_data_classification_" + fileSuffix + ".arff", trainingData, singleFeature, upsampling, possitiveThreshold, possitiveLevel, method, enrichment, features);
			System.out.println("done!");
		}
			
		if(method == 20 || method == 22) {
			System.out.print("Outputing training data (LibLinear)...");
			gpcr230.outputDataForLibSVC("training_data_classification_" + fileSuffix + ".txt", trainingData, singleFeature, upsampling, possitiveThreshold, possitiveLevel, method);
			System.out.println("done!");
		}
		
		System.out.println("Training data output complete.");
		System.out.println();
			
		return trainingData.size();
	}
}
