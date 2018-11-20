package interpro;

import java.util.*;
import utility.*;

public class NewKeywordExtractor {

	public static void main(String[] args) throws Exception {
		extractNewKeyword("ip/interpro.25.0_forest.txt", "ip/interpro.26.0_forest.txt", "ip260_keyword.txt");
		extractNewKeyword("ip/interpro.24.0_forest.txt", "ip/interpro.25.0_forest.txt", "ip250_keyword.txt");
		extractNewKeyword("ip/interpro.23.1_forest.txt", "ip/interpro.24.0_forest.txt", "ip240_keyword.txt");
		extractNewKeyword("ip/interpro.22.0_forest.txt", "ip/interpro.23.1_forest.txt", "ip231_keyword.txt");
		extractNewKeyword("ip/interpro.21.0_forest.txt", "ip/interpro.22.0_forest.txt", "ip220_keyword.txt");
		extractNewKeyword("ip/interpro.20.0_forest.txt", "ip/interpro.21.0_forest.txt", "ip210_keyword.txt");
		extractNewKeyword("ip/interpro.19.0_forest.txt", "ip/interpro.20.0_forest.txt", "ip200_keyword.txt");
		extractNewKeyword("ip/interpro.18.0_forest.txt", "ip/interpro.19.0_forest.txt", "ip190_keyword.txt");
		extractNewKeyword("ip/interpro.14.1_forest.txt", "ip/interpro.18.0_forest.txt", "ip180_keyword.txt");
		extractNewKeyword("ip/interpro.13.0_forest.txt", "ip/interpro.14.1_forest.txt", "ip141_keyword.txt");
		extractNewKeyword("ip/interpro.12.1_forest.txt", "ip/interpro.13.0_forest.txt", "ip130_keyword.txt");
		extractNewKeyword("ip/interpro.12.0_forest.txt", "ip/interpro.12.1_forest.txt", "ip121_keyword.txt");
		extractNewKeyword("ip/interpro.11.0_forest.txt", "ip/interpro.12.0_forest.txt", "ip120_keyword.txt");
	}
	
	public static void extractNewKeyword(String oldTreeFileName, String newTreeFileName, String outputFileName) throws Exception {
		Vector<String> ip25 = Utility.loadVector(oldTreeFileName);
		Vector<String> ip26 = Utility.loadVector(newTreeFileName);
		Vector<String> v = new Vector<String>();
		for(String s : ip26) {
			v.add(s.substring(Proteins.getIndentIndex(s), s.length()));
		}
		for(String s : ip25) {
			v.remove(s.substring(Proteins.getIndentIndex(s), s.length()));
		}
		Utility.saveVector(outputFileName, v);
	}
}
