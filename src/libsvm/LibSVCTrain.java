package libsvm;

public class LibSVCTrain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Parameters
		
		// LibSVM parameter s, svm_type : set type of SVM (default 0)
		// 0 -- C-SVC
		// 1 -- nu-SVC
		String s = "0";
		
		// LibSVM parameter t, kernel_type : set type of kernel function (default 2)
		//0 -- linear: u'*v
		//1 -- polynomial: (gamma*u'*v + coef0)^degree
		//2 -- radial basis function: exp(-gamma*|u-v|^2)
		//3 -- sigmoid: tanh(gamma*u'*v + coef0)
		//4 -- precomputed kernel (kernel values in training_set_file)
		String t = "2";
			
		// LibSVM parameter c, cost of C-SVC (default 1)
		String c = "1";
		
		// LibSVM parameter n, nu : set the parameter nu of nu-SVC (default 0.5)
		String n = "0.5";
			
		// LibSVM parameter b (probability estimates)
		String b = "1";

		// LibSVM parameter h (shrinking)
		String h = "1";

		// LibSVM parameter d, degree : set degree in kernel function (default 3)
		String d = "3";
		
		// LibSVM parameter g, gamma : set gamma in kernel function (default 1/k)
		//String g = "1";
		
		// LibSVM parameter r, coef0 : set coef0 in kernel function (default 0)
		String r = "0";

		// LibSVM parameter m, cachesize : set cache memory size in MB (default 100)
		String m = "4000";
		
		// LibSVM parameter e, epsilon : set tolerance of termination criterion (default 0.001)
		String e = "0.001";

		// LibSVM parameter wi, weight: set the parameter C of class i to weight*C, for C-SVC (default 1)
		String w = "1";
		
		String fileSuffix = "m13_g0_";		
		LibSVCTrain.run(fileSuffix, s, c, b, h, n, d, r, m, e, w, t);
	}

	// Run LibSVC
	public static void run(String fileSuffix, String s, String c, String b, String h, String n, String d, String r, String m, String e, String w, String t) throws Exception {		
		System.out.println("LibSVC training...");
		String trainingDataFileName = "training_data_classification_" + fileSuffix + ".txt";
		String modelFileName = "model_classification_" + fileSuffix + ".txt";		
		String parameters[] = {"-s", s, "-c", c, "-t", t, "-b", b, "-h", h, "-n", n, "-d", d, "-r", r, "-m", m, "-e", e, "-w1", w, trainingDataFileName, modelFileName};
		SVMTrain.main(parameters);
	    System.out.println("LibSVC training complete.");
	}
}
