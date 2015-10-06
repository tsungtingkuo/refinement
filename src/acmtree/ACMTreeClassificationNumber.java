package acmtree;
/**
 * 
 */

/**
 * @author Tim Kuo
 *
 */
public class ACMTreeClassificationNumber {

	int classificationLevel = 0;
	String classificationNumber = "";
	Boolean isLeaf = false;					// In Leaf level, not necessary subject descriptor
	Boolean isSubjectDescriptor = false;	// No code, must in Leaf

	/**
	 * @return the isLeaf
	 */
	public Boolean getIsLeaf() {
		return isLeaf;
	}

	/**
	 * Parse number to get level
	 * @param previousNumber if this is level four, we need previous number to get this number
	 */
	public String parseNumber(ACMTreeClassificationNumber previousNumber) {
		
		String number = this.classificationNumber;
		this.classificationLevel = 1;
		for(int i=0; i<number.length(); i++) {
			char c = number.charAt(i);
			if(c=='.') {
				this.classificationLevel++;
			}
		}

		if(this.classificationLevel == 1 && number.length() != 1) {
			int previousLevel = previousNumber.getClassificationLevel();
			Boolean previousIsLeaf = previousNumber.getIsLeaf();
			this.isLeaf = true;
			this.isSubjectDescriptor = true;
					
			if(previousIsLeaf) {		// continue the number
				String previous = previousNumber.getThisLevelNumber();
				String current = null;
				int temp = Integer.parseInt(previous);
				current = Integer.toString(temp + 1);				
				this.classificationNumber = previousNumber.getUpperLevelNumber() + "." + current;			
				this.classificationLevel = previousLevel;
			}
			else {						// create a new number
				this.classificationNumber = previousNumber.getClassificationNumber() + ".0";
				this.classificationLevel = previousLevel + 1;
			}
		}
		else {
			number = "";
		}

		/* Debug
		System.out.print("Parsing Number = " + this.classificationNumber + ", level = " + this.classificationLevel);
		if(previousNumber == null) {
			System.out.println();
		}
		else {
			System.out.println(", previous = " + previousNumber.getClassificationNumber());
		}
		*/
		
		return number;
	}
	
	/**
	 * Get number of upper level
	 */
	public String getUpperLevelNumber() {
		if(this.classificationLevel == 1) {
			return null;
		}
		else {
			int length = this.classificationNumber.length();
			int i;
			for(i=length-1; i>=0; i--) {
				char c = this.classificationNumber.charAt(i);
				if(c=='.') {
					break;
				}
			}
			String result = this.classificationNumber.substring(0, i);
			return result;			
		}
	}

	/**
	 * Get number of this level
	 */
	public String getThisLevelNumber() {
		int length = this.classificationNumber.length();
		int i;
		for(i=length-1; i>=0; i--) {
			char c = this.classificationNumber.charAt(i);
			if(c=='.') {
				break;
			}
		}
		String result = this.classificationNumber.substring(i + 1, length);
		return result;
	}

	
	/**
	 * Compare the classification level of another classification number
	 * @param cn the classification number to be compared
	 */
	public boolean isSameLevel(ACMTreeClassificationNumber cn) {
		if(cn.getClassificationLevel() == this.classificationLevel) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * @return the classificationLevel
	 */
	public int getClassificationLevel() {
		return classificationLevel;
	}

	/**
	 * @return the classificationNumber
	 */
	public String getClassificationNumber() {
		return classificationNumber;
	}

	/**
	 * @param classificationNumber the classificationNumber to set
	 * @param isLevelOne if this is level one, set level to 1; otherwise the level must be recalculated
	 * @param previousNumber if this is level four, we need previous number to get this number
	 */
	public String setClassificationNumber(String classificationNumber, ACMTreeClassificationNumber previousNumber) {
		this.classificationNumber = classificationNumber;
		return parseNumber(previousNumber);
	}

	public void setClassificationNumber(String classificationNumber) {
		this.classificationNumber = classificationNumber;
	}

	/**
	 * @param classificationLevel the classificationLevel to set
	 */
	public void setClassificationLevel(int classificationLevel) {
		this.classificationLevel = classificationLevel;
	}

	/**
	 * @param isLeaf the isLeaf to set
	 */
	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	/**
	 * @return the isSubjectDescriptor
	 */
	public Boolean getIsSubjectDescriptor() {
		return isSubjectDescriptor;
	}

	/**
	 * @param isSubjectDescriptor the isSubjectDescriptor to set
	 */
	public void setIsSubjectDescriptor(Boolean isSubjectDescriptor) {
		this.isSubjectDescriptor = isSubjectDescriptor;
	}

	public int getLevelOneClassLabel() {
		switch(this.classificationNumber.charAt(0)) {
			case 'A': return 0;
			case 'B': return 1;
			case 'C': return 2;
			case 'D': return 3;
			case 'E': return 4;
			case 'F': return 5;
			case 'G': return 6;
			case 'H': return 7;
			case 'I': return 8;
			case 'J': return 9;
			case 'K': return 10;
		}
		return 0;
	}
}
