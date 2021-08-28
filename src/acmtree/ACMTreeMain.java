package acmtree;

public class ACMTreeMain {

	public static String run(int method, int features, boolean enrichment) throws Exception {
				
		boolean useAnd = true;				// Use and / or for the two thresholds
		boolean testingNew98 = true;		// False if testing 98PN100
		boolean singleFeature = false;		// Use 1 feature only
		boolean newAsParent = false;		// Allow new keyword as parent 
		boolean validAnswerOnly = true;		// Test valid answer only
		
		int adjustScore = -1;				// -1 = none
		int adjustSimilarity = -1;			// -1 = none, 0 = min 0.15, 1 = minmax 1.75
		int keywordNumber = -1;				// -1 = all
		int upsampling = 1;					// Repeat output for positive data
		int view = -1;						// View mode, -1 = none, 0 = single-analysis, 1 = all-correct, 2 = all-suggest
		int suggestRank = -1;				// -1 = all
		int possitiveLevel = 5;				// Additional positive data level, default = 5, valid = 4(5320), 3(36858), 2(160454)
	
		double alpha = 0.8;					// Structural similarity computation
		double beta = 0.3;					// Semantic similarity adjustment
		double gamma = 0.0;					// Ratio of baseline (gamma) and classifier (1 - gamma)
		double semanticThreshold = -1.0;	// Semantic threshold to reduce data size, default = -1
		double structuralThreshold = -1.0;	// Structural threshold to reduce data size, default = -1
		double possitiveThreshold = 1;		// Positive data threshold, default = 1
		double cost = 1;					// Weka cost, 1 = none

		// Training
		System.out.println("Starting training phase...");
		ACMTreeTraining.run(Integer.toString(method), alpha, beta, semanticThreshold, structuralThreshold, useAnd, singleFeature, upsampling, adjustSimilarity, possitiveThreshold, possitiveLevel, method, enrichment, features, cost);
		System.out.println("Training phase complete.");
		
		// Insertion
		System.out.println("Starting insertion phase...");
		String result = ACMTreeTesting.run(Integer.toString(method), singleFeature, alpha, beta, gamma, semanticThreshold, newAsParent, method, validAnswerOnly, testingNew98, keywordNumber, adjustScore, adjustSimilarity, view, suggestRank, 1211100, enrichment, features);
		if(view > -1) {
			ACMTreeView.run(Integer.toString(method), keywordNumber, view);
		}
		System.out.println("Insertion phase complete.");
		
		return result;
	}
}
