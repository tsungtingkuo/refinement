package libsvm;

public class LibSVRTrain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Parameters
		
		// LibSVM parameter s
		// -s svm_type : set type of SVM (default 0)
		// 2 -- one-class SVM
		// 3 -- epsilon-SVR
		// 4 -- nu-SVR
		String s = "3";
		
		// LibSVM parameter t
		String t = "0";
		
		// LibSVM parameter c
		String c = "10";
		
		// LibSVM parameter b (probability estimates)
		String b = "1";

		// LibSVM parameter h (shrinking)
		String h = "1";

		
		// File names
		String trainingDataFileName = "training_data_regression.txt";
		String modelFileName = "model_regression.txt";
		
		// Run LibLinear
		String parameters[] = {"-s", s, "-c", c, "-t", t, "-b", b, "-h", h, trainingDataFileName, modelFileName};
		SVMTrain.main(parameters);
	}

}
