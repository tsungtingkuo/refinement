
import acmtree.*;
import utility.*;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		ACMTreeNode gpcr230 = new ACMTreeNode("gpcr230_experiment.txt", true);
		System.out.println(gpcr230.size());
		System.out.println(gpcr230.getAverageBranchingFactor());
		int[] level230 = gpcr230.getLevelNodes(5);
		Utility.printIntegerArray(level230, level230.length);

		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt", false);
		System.out.println(root91.size());
		System.out.println(root91.getAverageBranchingFactor());
		int[] level91 = root91.getLevelNodes(4);
		Utility.printIntegerArray(level91, level91.length);
		
//		ACMTreeNode gpcr230 = new ACMTreeNode("gpcr230_experiment.txt", true);
//		ACMTreeNode node = gpcr230.getNodeByName("Interleukin 8B receptor");
//		System.out.println(node.getClassificationName());
		
//		ACMTreeNode gpcr230 = new ACMTreeNode("gpcr230_experiment.txt", true);
//		System.out.println(gpcr230.size());
		
//		String s = Utility.getHtml("http://academic.research.microsoft.com/Search.aspx?query=data%20mining", "", "", true);
//		Utility.saveString("test.htm", s);
			
//		ACMTreeNode n = new ACMTreeNode("gpcr230_experiment.txt", true);
//		System.out.println(n.getChildNode().size());
		
//		int i = 153;
//		int j = i/10%10;
//		System.out.println(j);
		
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		System.out.println(root91.getLevelCount(4, 0));
		*/

		// Rename files
		/*
		File dir = new File("html91");
		File[] files = dir.listFiles();
		for(int i=0; i<files.length; i++) {
			File f = files[i];
			String oldName = f.getAbsolutePath();
			String newName = oldName.replaceFirst("ccs98", "ccs91");
			System.out.println(newName);
			f.renameTo(new File(newName));
		}
		*/
		
		// Compute level 1 count in 91-tree
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		Vector nodeVector = root91.getPreprocessedNodeVector(new Vector(), false);
		int count[] = new int[11];
		for(int i=0; i<nodeVector.size(); i++) {
			ACMTreeNode node = (ACMTreeNode)nodeVector.get(i);
			count[node.getClassificationNumber().getLevelOneClassLabel()]++;
		}
		for(int i=0; i<11; i++) {
			System.out.println(count[i]);
		}
		*/
		
		// Check the difference between 1101 and 1184
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		Vector keyword1101 = root91.getPreprocessedNameVector(new Vector());
		Vector keyword1184 = root91.loadKeywordsFromFile("acm_1184.txt");
		int count = 0;
		for(int i=0; i<keyword1184.size(); i++) {
			String s1184 = (String)keyword1184.get(i);
			s1184 = s1184.trim();
			s1184 = s1184.replace("/", " ");
			s1184 = s1184.replace("-", " ");
			s1184 = s1184.replace(",", "");
			
			boolean matched = false;
			for(int j=0; j<keyword1101.size(); j++) {
				String s1101 = (String)keyword1101.get(j);
				if(s1184.equalsIgnoreCase(s1101)) {
					matched = true;
					break;
				}
			}
			if(matched == false) {
				System.out.println(s1184);
				count++;
			}
		}
		System.out.println(count);
		*/
		
		// Output XML for Prefuse visualization
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		root91.outputXMLForPrefuse("data/acmtree.xml");
		*/
		
		// Check answer for pn100
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		ACMTreeNode root98 = new ACMTreeNode("ccs98_experiment.txt");
		Vector newKeywords = root91.loadKeywordsFromFile("ccs98_pn100_answer_number.txt");
		for(int i=0; i<newKeywords.size(); i++) {
			String number = (String)newKeywords.get(i);
			ACMTreeNode node = root98.getNodeByNunber(number);
			System.out.println(node.getClassificationName());
			
		}
		*/
		
		// Vector test
		/*
		Vector v = new Vector();
		v.add("1");
		v.add("2");
		v.add("3");
		System.out.println(v.size());
		Vector v2 = (Vector)v.clone();
		Test.removeObjectInVector(v2);
		System.out.println(v.size());
		System.out.println(v2.size());
		*/
		
		
		// Combine result files
		/*
		PrintWriter pw = new PrintWriter("result.csv");
		File dir = new File("result");
		File[] files = dir.listFiles();
		for(int i=0; i<files.length; i++) {
			FileReader fr = new FileReader(files[i]);
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			while ((s=lnr.readLine()) != null) {
				pw.print(files[i].getName() + ", ");
				pw.println(s);
			}		
			lnr.close();
			fr.close();
		}
		pw.close();				
		*/
		
		// Test NGD similarities
		/*
		ACMTreeNode root91 = new ACMTreeNode("ccs91_experiment.txt");
		NGDSimilarity ns = new NGDSimilarity();
		ns.loadAllNGDSimilarities();
		
		PrintWriter pw = new PrintWriter("tt.txt");
		double[][] tt = ns.getTree91ToTree91();
		for(int i=0; i<ns.tree91Size; i++) {
			for(int j=0; j<ns.tree91Size; j++) {
				pw.print(tt[i][j] + "\t");
			}
			pw.println();
		}
		
		pw.close();
		*/
	}
}
