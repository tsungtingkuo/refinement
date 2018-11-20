package weka;

import java.io.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;

public class WekaRandomForestTrain {

	public static void main(String[] args) throws Exception {
		double cost = 1;					// Weka cost, 1 = none
		String I = "10";					// RandomForest number of trees
		String K = "0";						// RandomForest number of features
		run("m12_g0_lv_", cost, I, K);
	}
	
	public static void run(String fileSuffix, double cost, String I, String K) throws Exception {
		String trainingDataFileName = "training_data_classification_" + fileSuffix + ".arff";
		String modelFileName = "model_classification_" + fileSuffix + ".model";		

		System.out.print("Loading data... ");
		Instances train = new Instances(new BufferedReader(new FileReader(trainingDataFileName)));
		train.setClassIndex(0);
		System.out.println("done.");

		System.out.print("Configuring parameters... ");

		// Random Forest
		String[] options = weka.core.Utils.splitOptions("-I " + I + " -K " + K + " -S 1");
		RandomForest classifier = new RandomForest();

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
