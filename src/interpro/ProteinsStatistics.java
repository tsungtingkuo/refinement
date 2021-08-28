package interpro;

import java.io.*;
import java.util.*;
import utility.*;

public class ProteinsStatistics {

	public static void main(String[] args) throws Exception {
		//ProteinsStatistics.checkInterpro();
		//ProteinsStatistics.checkForest();
		//ProteinsStatistics.checkTreeSize("ip250_experiment.txt", "ip250_size.txt");
		ProteinsStatistics.checkTreeSize("ip/interpro.26.0_forest.txt", "ip260_size.txt");

//		ProteinsStatistics.getNewTreeRoots("ip/interpro.26.0_forest.txt", "ip260_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.25.0_forest.txt", "ip250_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.24.0_forest.txt", "ip240_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.23.1_forest.txt", "ip231_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.22.0_forest.txt", "ip220_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.21.0_forest.txt", "ip210_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.20.0_forest.txt", "ip200_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.19.0_forest.txt", "ip190_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.18.0_forest.txt", "ip180_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.14.1_forest.txt", "ip141_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.13.0_forest.txt", "ip130_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.12.1_forest.txt", "ip121_keyword.txt");
//		ProteinsStatistics.getNewTreeRoots("ip/interpro.12.0_forest.txt", "ip120_keyword.txt");
		
		//ProteinsStatistics.extractNewTrees("ip250_experiment.txt", "ip260_experiment.txt", "ip26_keyword.txt", "ip25_experiment_new.txt");
	}

	public static void extractNewTrees(String oldTreeFileName, String newTreeFileName, String newNodeFileName, String outputFileName) throws Exception {
		Vector<String> r = new Vector<String>();
		Vector<String> n = ProteinsStatistics.getNewTreeRoots(newTreeFileName, newNodeFileName);
		Vector<String> t = Utility.loadVector(oldTreeFileName);
		boolean isNew = false;
		for(String st : t) {
			if(!st.contains("--")) {
				isNew = false;
				for(String sn : n) {
					if(st.equalsIgnoreCase(sn)) {
						isNew = true;
						r.add(new String(st));
						break;
					}					
				}
			}
			else {
				if(isNew == true) {
					r.add(new String(st));
				}
			}
		}
		Utility.saveVector(outputFileName, r);
	}

	public static Vector<String> getNewTreeRoots(String treeFileName, String newFileName) throws Exception {
		Vector<String> r = new Vector<String>();
		Vector<String> t = Utility.loadVector(treeFileName);
		Vector<String> n = Utility.loadVector(newFileName);
		String root = "";
		int rootCount = 0;
		for(String st : t) {
			if(!st.contains("--")) {
				root = new String(st);
			}
			for(String sn : n) {
				if(st.equalsIgnoreCase(sn)) {
					r.add(new String(root));
					if(st.equalsIgnoreCase(root)) {
						rootCount++;
					}
					break;
				}
			}
		}
		System.out.println(rootCount + "/" + r.size());
		return r;
	}

	public static void checkTreeSize(String inputFileName, String outputFileName) throws Exception {
		Vector<Integer> r = new Vector<Integer>();
		Vector<String> v = Utility.loadVector(inputFileName);
		int size = 0;
		for(int i=0; i<v.size(); i++) {
			String s = v.get(i);
			if(!s.contains("--")) {
				if(i>0) {
					r.add(new Integer(size));
				}
				size = 0;
			}
			else {
				size++;
			}
		}
		if(size>0) {
			r.add(new Integer(size));
		}
		Utility.saveVector(outputFileName, r);
	}

	public static void checkForest() throws Exception {
		File dir = new File("forest");
		for(String s : dir.list()) {
			Vector<String> v = Utility.loadVector("forest/" + s);
			int root = 0;
			for(String ss : v) {
				if(!ss.contains("--")) {
					root++;
				}
			}
			System.out.println(s + ", " + v.size() + ", " + root);
		}
	}

	public static void checkInterpro() throws Exception {
		File dir = new File("interpro");
		for(String s : dir.list()) {
			Proteins ps = new Proteins("interpro/" + s);
			System.out.println(s + ", " + ps.size() + ", " + ps.rootSize());
		}
	}
}
