package weka;

import java.io.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.*;

public class WekaNaiveBayesTrain {

	public static void main(String[] args) throws Exception {
		double cost = 1;					// Weka cost, 1 = none
		run("m6_g0_lv_", cost);
	}
	
	// For simplification
	public static void run() throws Exception {
		run("", 1);
	}

	public static void run(String fileSuffix, double cost) throws Exception {
		String trainingDataFileName = "training_data_classification_" + fileSuffix + ".arff";
		String modelFileName = "model_classification_" + fileSuffix + ".model";		

		System.out.print("Loading data... ");
		Instances train = new Instances(new BufferedReader(new FileReader(trainingDataFileName)));
		train.setClassIndex(0);
		System.out.println("done.");

		System.out.print("Configuring parameters... ");
		
		// Naive Bayes
		String[] options = weka.core.Utils.splitOptions("");
		NaiveBayes classifier = new NaiveBayes();
			
		classifier.setOptions(options);
		FilteredClassifier fc = new FilteredClassifier();
		
		if(cost == 1) {
			// Non-cost; used for level-based
			
			fc.setClassifier(classifier);
		}
		else {			
			// Cost Sensitive Classifier

			Classifier csc = new CostSensitiveClassifier();
			CostMatrix cm = new CostMatrix(2);
			cm.initialize();
			cm.setElement(0,1,1);
			cm.setElement(1,0,cost);
			((CostSensitiveClassifier)csc).setCostMatrix(cm);
			((CostSensitiveClassifier)csc).setClassifier(classifier);
			fc.setClassifier(csc);
		}

		System.out.println("done.");

		System.out.print("Training classifier... ");
		fc.buildClassifier(train);
		System.out.println("done.");
		
		// Save the model
		System.out.print("Outputing model... ");
		SerializationHelper.write(modelFileName, fc);
		System.out.println("done.");
	}

}
