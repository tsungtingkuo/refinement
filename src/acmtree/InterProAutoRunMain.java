package acmtree;

import java.util.*;
import utility.*;

public class InterProAutoRunMain {
	
	public static void main(String[] args) throws Exception {
		
		// Timer start
		long startTime = (new Date()).getTime();
		
		Directory.createDirectories();
		
		Vector<String> v = new Vector<String>();
		v.add(InterProMain.run(Method.Random, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Level, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Sibling, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Children, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Frequency, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Name, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Page, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Jaccard, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_NGD, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Coauthor, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_Sequence, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Similarity_OneNorm, Feature.None, Enrichment.Disable));
		v.add(InterProMain.run(Method.Learning, Feature.Topology, Enrichment.Disable));
		v.add(InterProMain.run(Method.Learning, Feature.Content, Enrichment.Disable));
		v.add(InterProMain.run(Method.Learning, Feature.Content, Enrichment.Enable));
		v.add(InterProMain.run(Method.Learning, Feature.Social, Enrichment.Disable));
		v.add(InterProMain.run(Method.Learning, Feature.Social, Enrichment.Enable));
		v.add(InterProMain.run(Method.Learning, Feature.All, Enrichment.Disable));
		v.add(InterProMain.run(Method.Learning, Feature.All, Enrichment.Enable));
		Utility.saveVector("results_gpcr.csv", v);
		
		// Timer stop
		long stopTime = (new Date()).getTime();
		long time = stopTime - startTime;
		System.out.println();
		System.out.println("  Execution time = " + ((double)time/(double)1000) + " (sec)");
	}
}
