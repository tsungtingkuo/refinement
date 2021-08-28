package acmtree;

import java.io.*;
import java.util.*;

public class DOAJParser {
	
	Vector<String> doaj = new Vector<String>();
	Vector<String> ccs91 = new Vector<String>();
	Vector<String> ccs98 = new Vector<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		DOAJParser.getDOAJKeywords("doaj.txt", "doaj_keywords.txt");
		DOAJParser.uniqueDOAJKeywords("doaj_keywords.txt", "doaj_keywords_unique.txt");
		
		DOAJParser dp = new DOAJParser();
		
		dp.loadKeywords("doaj_keywords_unique.txt", dp.doaj);
		dp.loadKeywords("ccs91_keyword.txt", dp.ccs91);
		dp.saveNewDOAJKeywords("doaj_keywords_91.txt", dp.ccs91);

		dp.loadKeywords("doaj_keywords_91.txt", dp.doaj);
		dp.loadKeywords("ccs98_keyword.txt", dp.ccs98);
		dp.saveNewDOAJKeywords("doaj_keywords_91_98.txt", dp.ccs98);
	}
	
	public void saveNewDOAJKeywords(String fileName, Vector<String> v) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		for(int i=0; i<doaj.size(); i++) {
			boolean unique = true;
			String d = doaj.get(i);
			for(int j=0; j<v.size(); j++) {
				String c = v.get(j);
				if(d.equalsIgnoreCase(c)) {
					unique = false;
					break;
				}
			}
			if(unique == true) {
				pw.println(d);
			}
		}
		pw.close();
	}
	
	public void loadKeywords(String fileName, Vector<String> v) throws Exception {
		v.clear();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;
		while ((s=lnr.readLine()) != null) {
			v.add(s);
		}
		lnr.close();
		fr.close();
	}
	
	public static void getDOAJKeywords(String inputFileName, String outputFileName) throws Exception {
		FileReader fr = new FileReader(inputFileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		PrintWriter pw = new PrintWriter(outputFileName);
		String s = null;
		while ((s=lnr.readLine()) != null) {
			if(s.indexOf("Keywords") > -1) {
				s= s.trim();
				s = s.replaceAll(", ", "\n");
				s = s.replace("/", " ");
				s = s.replace("-", " ");
				s = s.replace(",", "");
				s = s.replaceAll("S W", "software");
				s = s.replaceAll("H W", "hardware");
				s = s.replaceAll("IT", "information technology");
				s = s.replaceAll("Boolean", "boolean");
				s = s.replaceAll("ICT", "");
				s = s.replaceAll("R project", "");
				s = s.replaceAll("RP", "");
				s = s.replaceAll("RT", "");
				s = s.replaceAll("SMEs", "");
				s= s.trim();
				pw.println(s.substring(10, s.length()));
			}
		}
		pw.close();
		lnr.close();
		fr.close();
	}
	
	public static void uniqueDOAJKeywords(String inputFileName, String outputFileName) throws Exception {
		FileReader fr = new FileReader(inputFileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		PrintWriter pw = new PrintWriter(outputFileName);
		String s = null;
		TreeSet<String> ts = new TreeSet<String>();
		while ((s=lnr.readLine()) != null) {
			if(s.length() > 0) {
				ts.add(s);
			}
		}
		for(String ss : ts) {
			pw.println(ss);
		}
		pw.close();
		lnr.close();
		fr.close();
	}	
}
