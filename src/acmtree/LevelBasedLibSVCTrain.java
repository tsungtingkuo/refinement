package acmtree;

import liblinear.*;

public class LevelBasedLibSVCTrain {

	public static void main(String[] args) throws Exception {
		// Parameters
		String fileSuffix = "m1_g0_lv_";
		String s = "0";
		String c = "1";
		String b = "1";
		String e = "0.01";
		String w0 = "1";					// LibLinear weight
		String w1 = "1";					// LibLinear (positive) weight
		String w2 = "1";					// LibLinear weight
		String w3 = "1";					// LibLinear weight
		String w4 = "1";					// LibLinear weight
		String w5 = "1";					// LibLinear weight
		String w6 = "1";					// LibLinear weight
		String w7 = "1";					// LibLinear weight
		String w8 = "1";					// LibLinear weight
		String w9 = "1";					// LibLinear weight
		String w10 = "1";					// LibLinear weight
		
		LevelBasedLibSVCTrain.run(fileSuffix, s, c, b, e, w0, w1, w2, w3, w4, w5, w6, w7, w8, w9, w10);		
	}
	
	public static void run(String fileSuffix, String s, String c, String b, String e, String w0, String w1, String w2, String w3, String w4, String w5, String w6, String w7, String w8, String w9, String w10) throws Exception {
		
		System.out.println("LibLinear training...");

		// Run LibLinear
		String trainingDataFileName = "training_data_classification_" + fileSuffix + ".txt";
		String modelFileName = "model_classification_" + fileSuffix + ".txt";		
		String parameters[] = {"-s", s, "-c", c, "-w0", w0, "-w1", w1, "-w2", w2, "-w3", w3, "-w4", w4, "-w5", w5, "-w6", w6, "-w7", w7, "-w8", w8, "-w9", w9, "-w10", w10, "-b", b, "-e", e, trainingDataFileName, modelFileName};
	    Train.main(parameters);

	    System.out.println("LibLinear training complete.");
	}

}
