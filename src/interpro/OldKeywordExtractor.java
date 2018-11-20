package interpro;

import java.util.*;
import utility.*;

public class OldKeywordExtractor {

	public static void main(String[] args) throws Exception {
		OldKeywordExtractor.extract("gpcr230.txt", "gpcr230_keyword.txt");
		OldKeywordExtractor.extract("gpcr24.txt", "gpcr24_keyword.txt");
	}
	
	public static void extract(String inputFileName, String outputFileName) throws Exception {
		Vector<String> ip25 = Utility.loadVector(inputFileName);
		Vector<String> v = new Vector<String>();
		for(String s : ip25) {
			v.add(s.substring(Proteins.getIndentIndex(s), s.length()));
		}
		Utility.saveVector(outputFileName, v);
	}
}
