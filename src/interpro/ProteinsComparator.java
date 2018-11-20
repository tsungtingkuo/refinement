package interpro;

import java.util.*;
import utility.*;

public class ProteinsComparator {

	public static void main(String[] args) throws Exception {
		Vector<String> u = Utility.loadVector("ParentChildTreeFile.26.0.txt");
		Vector<String> v = Utility.loadVector("forest/interpro.26.0_forest.txt");
		Vector<String> r = new Vector<String>();
		for(String s : u) {
			r.add(s.split("::")[0]);
		}
		for(String s : v) {
			r.remove(s.split("::")[0]);
		}
		for(String s: r) {
			System.out.println(s);
		}
	}
}
