package acmtree;

public class LevelBasedDataInstance {

	/*
	 * This class is used as training / testing data instance.
	 * This instance corresponds to a (parent, target) pair,
	 * and should be generated by ACMTreeNode. 
	 * The order of the features can't be changed arbitrary. 
	 */
	
	int levelOneClassNumber = 11;						// 0 - 10
	int classLabel = 0;									// 0 - 10
	
	// Total 100 features
	double targetWebPageNumber = 0;
	double[] parentWebPageNumber = new double[levelOneClassNumber];
	double[] parentAndTargetSimilarity = new double[levelOneClassNumber];
	double[] childrenCount = new double[levelOneClassNumber];
	double[] childrenAndTargetSimilarityAverage = new double[levelOneClassNumber];
	double[] childrenAndTargetSimilarityMaximum = new double[levelOneClassNumber];
	double[] childrenAndTargetSimilarityMinimum = new double[levelOneClassNumber];
	double[] childrenWebPageNumberAverage = new double[levelOneClassNumber];
	double[] childrenWebPageNumberMaximum = new double[levelOneClassNumber];
	double[] childrenWebPageNumberMinimum = new double[levelOneClassNumber];
	
	public static void main(String[] args) throws Exception{
		LevelBasedDataInstance di = new LevelBasedDataInstance();
		System.out.println(di.toStringForLibSVC(false));
	}

	public String toStringForLibSVC(boolean singleFeature) {
		String result = Integer.toString(this.classLabel);
		result += String.format(" 1:%.4f", targetWebPageNumber);
		int featureCount = 2;
		for(int i=0; i<levelOneClassNumber; i++) {
			//result += String.format(" %d:%.4f", featureCount++, parentWebPageNumber[i]);
			result += String.format(" %d:%.4f", featureCount++, parentAndTargetSimilarity[i]);
			//result += String.format(" %d:%.4f", featureCount++, childrenCount[i]);
			result += String.format(" %d:%.4f", featureCount++, childrenAndTargetSimilarityAverage[i]);
			result += String.format(" %d:%.4f", featureCount++, childrenAndTargetSimilarityMaximum[i]);
			result += String.format(" %d:%.4f", featureCount++, childrenAndTargetSimilarityMinimum[i]);
			//result += String.format(" %d:%.4f", featureCount++, childrenWebPageNumberAverage[i]);
			//result += String.format(" %d:%.4f", featureCount++, childrenWebPageNumberMaximum[i]);
			//result += String.format(" %d:%.4f", featureCount++, childrenWebPageNumberMinimum[i]);
		}

		return result;
	}
	
	public String toStringForWekaClassification(boolean singleFeature) {
		String result = Integer.toString(this.classLabel);
		result += String.format(",%.4f", targetWebPageNumber);
		for(int i=0; i<levelOneClassNumber; i++) {
			//result += String.format(",%.4f", parentWebPageNumber[i]);
			result += String.format(",%.4f", parentAndTargetSimilarity[i]);
			//result += String.format(",%.4f", childrenCount[i]);
			result += String.format(",%.4f", childrenAndTargetSimilarityAverage[i]);
			result += String.format(",%.4f", childrenAndTargetSimilarityMaximum[i]);
			result += String.format(",%.4f", childrenAndTargetSimilarityMinimum[i]);
			//result += String.format(",%.4f", childrenWebPageNumberAverage[i]);
			//result += String.format(",%.4f", childrenWebPageNumberMaximum[i]);
			//result += String.format(",%.4f", childrenWebPageNumberMinimum[i]);
		}
		return result;
	}

	/**
	 * @return the classLabel
	 */
	public int getClassLabel() {
		return classLabel;
	}

	/**
	 * @param classLabel the classLabel to set
	 */
	public void setClassLabel(int classLabel) {
		this.classLabel = classLabel;
	}

	/**
	 * @return the targetWebPageNumber
	 */
	public double getTargetWebPageNumber() {
		return targetWebPageNumber;
	}

	/**
	 * @param targetWebPageNumber the targetWebPageNumber to set
	 */
	public void setTargetWebPageNumber(double targetWebPageNumber) {
		this.targetWebPageNumber = targetWebPageNumber;
	}
}
