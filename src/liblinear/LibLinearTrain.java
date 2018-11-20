package liblinear;

public class LibLinearTrain {

	public static void main(String[] args) throws Exception {
		// Parameters
		String fileSuffix = "full";
		String s = "0";
		String c = "1";
		String B = "1";
		String e = "0.01";
		String w = "0.45";
		
		LibLinearTrain.run(fileSuffix, s, c, B, e, w);		
	}
	
	public static void run(String fileSuffix, String s, String c, String B, String e, String w) throws Exception {
		
		System.out.println("LibLinear training...");

		// Run LibLinear
		String trainingDataFileName = "training_data_classification_" + fileSuffix + ".txt";
		String modelFileName = "model_classification_" + fileSuffix + ".txt";		
		String parameters[] = {"-s", s, "-c", c, "-w1", w, "-B", B, "-e", e, trainingDataFileName, modelFileName};
	    Train.main(parameters);

	    System.out.println("LibLinear training complete.");
	}
}
