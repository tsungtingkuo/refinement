/**
 * 
 */
package acmtree;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Vector;

/**
 * @author Tim Kuo
 *
 */
public class ACMTreeProperNoun {

	Vector<String> nameVector = new Vector<String>();
	Vector<String> parenthesisVector = new Vector<String>();
	Vector<String> numberVector = new Vector<String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ACMTreeProperNoun pn = new ACMTreeProperNoun();
		pn.loadProperNounFromFile("ccs98_propernoun.txt");

		for (int i=0; i<pn.getNameVector().size(); i++) {
			String name = pn.getNameVector().get(i);
//			String number = pn.getNumberVector().get(i);
//			String parenthesis = pn.getParenthesisVector().get(i);
			
			//System.out.println("Name = " + name + " (" + parenthesis + "), Number = " + number);			
			System.out.println(name);
		}
	}

	public void loadProperNounFromFile(String fileName) throws IOException {
		
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		while ((s=lnr.readLine()) != null) {
			if(s.trim().length() > 0) {
				
				// Check for number
				int indexStart = s.lastIndexOf(" (");
				int indexStop = s.lastIndexOf(")");
				String number = s.substring(indexStart + 2, indexStop);
				this.numberVector.add(number);
					
				String beforeString = s.substring(0, indexStart);
				String afterString = s.substring(indexStop + 1, s.length());
				String name = beforeString + afterString;

				// Check for parenthesis
				indexStart = name.lastIndexOf(" (");
				if(indexStart >= 0) {
					indexStop = name.lastIndexOf(")");
					String parenthesis = name.substring(indexStart + 2, indexStop);
					this.parenthesisVector.add(parenthesis);
					
					beforeString = name.substring(0, indexStart);
					afterString = name.substring(indexStop + 1, name.length());
					name = beforeString + afterString;
				}
				else {
					this.parenthesisVector.add("");
				}
						
				// Preprocessing
				name = name.trim();
				name = name.replace("/", " ");
				name = name.replace("-", " ");
				name = name.replace(",", "");

				this.nameVector.add(name);
			}
		}
			
		lnr.close();
		fr.close();
	}

	/**
	 * @return the nameVector
	 */
	public Vector<String> getNameVector() {
		return nameVector;
	}

	/**
	 * @return the numberVector
	 */
	public Vector<String> getNumberVector() {
		return numberVector;
	}

	/**
	 * @return the parenthesisVector
	 */
	public Vector<String> getParenthesisVector() {
		return parenthesisVector;
	}
}
