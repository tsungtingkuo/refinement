package acmtree;

import weka.*;

public class DOAJTraining {

	public static void run(String fileSuffix, double alpha, double beta, double semanticThreshold, double structuralThreshold, boolean useAnd, boolean singleFeature, int upsampling, int adjustSimilarity, double possitiveThreshold, int possitiveLevel, int method, boolean enrichment, int features, double cost) throws Exception{
		if(method == Method.Learning) {
			DOAJTrainingDataGeneration.run(fileSuffix, alpha, beta, semanticThreshold, structuralThreshold, useAnd, singleFeature, upsampling, adjustSimilarity, possitiveThreshold, possitiveLevel, method, enrichment, features);
			WekaNaiveBayesTrain.run(fileSuffix, cost);
		}
	}
}
