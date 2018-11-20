

import java.util.*;


import weka.WekaNaiveBayesTrain;
import acmtree.*;

public class Main {

	public static void main(String[] args) throws Exception {

		// Parameter
		int method = Method.Learning_AllEnriched;
		int dataset = Dataset.ACMCCS98;
		boolean testOnly = true;

		// Timer start
		long startTime = (new Date()).getTime();

		// Training
		if(testOnly == false) {
			System.out.println("Starting training phase...");
			ACMTreeTrainingDataGeneration.run(method);
			WekaNaiveBayesTrain.run();
			System.out.println("Training phase complete.");
		}
		
		// Insertion
		System.out.println("Starting insertion phase...");
		if(dataset == Dataset.ACMCCS98) {
			ACMTreeTesting.run(method);
		}
		else {
			DOAJTesting.run(method);
		}
		System.out.println("Insertion phase complete.");
		
		// Timer stop
		long stopTime = (new Date()).getTime();
		long elapsedTime = stopTime - startTime;
		System.out.println();
		System.out.println("Elapsed Time = " + elapsedTime);
	}
}
