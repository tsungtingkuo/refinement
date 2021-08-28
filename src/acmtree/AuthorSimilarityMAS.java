package acmtree;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import utility.*;

public class AuthorSimilarityMAS {
	
	public void getLevel12Authors(String inputPrefix, String outputFileName, int size) throws Exception {
		PrintWriter pw = new PrintWriter(outputFileName);
		for(int i=0; i<size; i++) {
			Vector<String> uniqueAuthors = new Vector<String>();
			for(int j=0; j<AuthorSimilarity.AUTHOR_SIZE; j++) {
				String iIndex = Integer.toString(i+1);
				String jIndex = Integer.toString(j+1);
				System.out.println(iIndex + ", " + jIndex);
				Vector<String> authors = this.getLevel2KeyAuthors(inputPrefix + iIndex + "_" + jIndex + ".html");
				Utility.uniqueStringVectorAdd(uniqueAuthors, authors);
			}
			for(int k=0; k<uniqueAuthors.size(); k++) {
				pw.print((String)uniqueAuthors.get(k) + "\t");
			}
			pw.println();
		}
		pw.close();
	}
	
	public void getLevel2Authors(String inputPrefix, String outputFileName, int size) throws Exception {
		PrintWriter pw = new PrintWriter(outputFileName);
		for(int i=0; i<size; i++) {
			for(int j=0; j<AuthorSimilarity.AUTHOR_SIZE; j++) {
				String iIndex = Integer.toString(i+1);
				String jIndex = Integer.toString(j+1);
				pw.print(iIndex + "\t" + jIndex + "\t");
				System.out.println(iIndex + ", " + jIndex);
				Vector<String> authors = this.getLevel2KeyAuthors(inputPrefix + iIndex + "_" + jIndex + ".html");
				for(int k=0; k<authors.size(); k++) {
					pw.print(authors.get(k) + "\t");
				}
				pw.println();
			}
		}
		pw.close();
	}
	
	public void getLinks(String inputPrefix, String outputFileName, int size) throws Exception {
		PrintWriter pw = new PrintWriter(outputFileName);
		for(int i=0; i<size; i++) {
			String iIndex = Integer.toString(i+1);
			Vector<String> authorLinks = this.getKeyAuthorLinks(inputPrefix + iIndex + ".html");
			for(int j=0; j<authorLinks.size(); j++) {
				String jIndex = Integer.toString(j+1);
				pw.println(iIndex + "\t" + jIndex + "\t" + (String)authorLinks.get(j));				
			}
		}
		pw.close();
	}
	
	public void getAuthors(String inputPrefix, String outputFileName, int size) throws Exception {
		PrintWriter pw = new PrintWriter(outputFileName);
		for(int i=0; i<size; i++) {
			System.out.println((i+1) + "/" + size);
			Vector<String> authors = this.getKeyAuthors(inputPrefix + Integer.toString(i+1) + ".html");
			for(int j=0; j<authors.size(); j++) {
				pw.print((String)authors.get(j) + "\t");
			}
			pw.println();
		}
		pw.close();
	}

	public Vector<String> getKeyAuthorLinks(String fileName) throws Exception {
		Vector<String> result = new Vector<String>();
		String s = Utility.loadString(fileName);
		int beginIndex = s.indexOf("searchtype=1\">Author");
		if(beginIndex == -1) {
			return result;
		}
		int endIndex = s.indexOf("<div class=\"section-wrapper\">", beginIndex);
		s = s.substring(beginIndex, endIndex);
		beginIndex = s.indexOf("<a href=\"");
		while(beginIndex != -1) {
			beginIndex += 9;
			endIndex = s.indexOf("\" onclick", beginIndex);
			result.add("http://academic.research.microsoft.com" + s.substring(beginIndex, endIndex));			
			beginIndex = s.indexOf("<a href=\"", beginIndex);			
		}
		return result;
	}
	
	public Vector<String> getLevel2KeyAuthors(String fileName) throws Exception {
		Vector<String> result = new Vector<String>();
		File f = new File(fileName);
		if(!f.exists()) {
			return result;
		}
		String s = Utility.loadString(fileName);
		int beginIndex = s.indexOf("Co-author <span class=\"item-count\">");
		if(beginIndex == -1) {
			return result;
		}
		int endIndex = s.indexOf("<div class=\"section-wrapper\">", beginIndex);
		s = s.substring(beginIndex, endIndex);
		beginIndex = s.indexOf(".aspx');\">");
		while(beginIndex != -1) {
			beginIndex += 10;
			endIndex = s.indexOf("</a></li>", beginIndex);
			result.add(s.substring(beginIndex, endIndex));			
			beginIndex = s.indexOf(".aspx');\">", beginIndex);			
		}
		return result;
	}
	
	public Vector<String> getKeyAuthors(String fileName) throws Exception {
		Vector<String> result = new Vector<String>();
		String s = Utility.loadString(fileName);
		int beginIndex = s.indexOf("searchtype=1\">Author");
		if(beginIndex == -1) {
			return result;
		}
		int endIndex = s.indexOf("<div class=\"section-wrapper\">", beginIndex);
		s = s.substring(beginIndex, endIndex);
		beginIndex = s.indexOf(".aspx');\">");
		while(beginIndex != -1) {
			beginIndex += 10;
			endIndex = s.indexOf("</a></li>", beginIndex);
			result.add(s.substring(beginIndex, endIndex));			
			beginIndex = s.indexOf(".aspx');\">", beginIndex);			
		}
		return result;
	}
	
	public void saveLevelTwoPages(String inputFileName, String outputPrefix, int startIndex) throws Exception {
		Vector<String> v = Utility.loadVector(inputFileName);
		for(int k=0; k<v.size(); k++) {
			String s = v.get(k);
			int iIndex = s.indexOf("\t");
			int i = Integer.parseInt(s.substring(0, iIndex));
			int jIndex = s.indexOf("\t", iIndex+1);
			int j = Integer.parseInt(s.substring(iIndex+1, jIndex));
			String query = s.substring(jIndex+1, s.length());
			if(i >= startIndex) {
				String outputFileName = outputPrefix + Integer.toString(i) + "_" + Integer.toString(j) + ".html"; 			
				Utility.saveString(outputFileName, Utility.getHtml(query, "", "", true));
				System.out.println((k+1) + "/" + v.size() + ", " + s);
			}
		}
	}
	
	public void savePages(String inputFileName, String outputPrefix) throws Exception {
		Vector<String> v = Utility.loadVector(inputFileName);
		for(int i=0; i<v.size(); i++) {
			String s = v.get(i);
			String outputFileName = outputPrefix + Integer.toString(i+1) + ".html"; 			
			String query = "http://academic.research.microsoft.com/Search.aspx?query=" + s.replace(" ", "%20").replaceAll(",", "%2c");
			Utility.saveString(outputFileName, Utility.getHtml(query, "", "", true));
			System.out.println((i+1) + "/" + v.size() + ", " + s);
		}			
	}
}
