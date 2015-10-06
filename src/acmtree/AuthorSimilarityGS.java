package acmtree;

import java.io.*;
import java.net.*;
import java.util.*;

public class AuthorSimilarityGS {
	
	public static HttpURLConnection connection;
	
	public Vector<String> getKeyAuthorLinks(String fileName) throws Exception {
		Vector<String> result = new Vector<String>();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = lnr.readLine();
		int startIndex = s.indexOf("Key authors:&nbsp;&nbsp;");
		int stopIndex = s.indexOf("Result&nbsp;Page:&nbsp;", startIndex);
		//System.out.println("stopIndex = " + stopIndex);
		
		for(int i=0; i<AuthorSimilarity.AUTHOR_SIZE; i++) {
			int authorStartIndex = s.indexOf("<a href=\"", startIndex) + 10;
			//System.out.println("authorStartIndex = " + authorStartIndex);
			if(authorStartIndex>startIndex && authorStartIndex<stopIndex) {
				int authorStopIndex = s.indexOf("\"><b>", authorStartIndex);
				String authorLink = "http://scholar.google.com.tw/" + s.substring(authorStartIndex, authorStopIndex);
				//System.out.println(author);
				result.add(authorLink);
				startIndex = authorStopIndex;
			}
			else {
				break;
			}
		}
		
		lnr.close();
		fr.close();
		
		return result;
	}
	
	public Vector<String> getKeyAuthors(String fileName) throws Exception {
		Vector<String> result = new Vector<String>();
		FileReader fr = new FileReader(fileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = lnr.readLine();
		int startIndex = s.indexOf("Key authors:&nbsp;&nbsp;");
		int stopIndex = s.indexOf("Result&nbsp;Page:&nbsp;", startIndex);
		//System.out.println("stopIndex = " + stopIndex);
		
		for(int i=0; i<AuthorSimilarity.AUTHOR_SIZE; i++) {
			int authorStartIndex = s.indexOf("<b>", startIndex) + 3;
			//System.out.println("authorStartIndex = " + authorStartIndex);
			if(authorStartIndex>startIndex && authorStartIndex<stopIndex) {
				int authorStopIndex = s.indexOf("</b>", authorStartIndex);
				String author = s.substring(authorStartIndex, authorStopIndex);
				//System.out.println(author);
				result.add(author);
				startIndex = authorStopIndex;
			}
			else {
				break;
			}
		}
		
		lnr.close();
		fr.close();
		
		return result;
	}
	
	public void saveLevelTwoPages(String inputFileName, String outputFilePrefix, int startIndex) throws Exception {
		FileReader fr = new FileReader(inputFileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;

		while ((s=lnr.readLine()) != null) {
			int iIndex = s.indexOf("\t");
			//System.out.println("iIndex = " + iIndex);
			int i = Integer.parseInt(s.substring(0, iIndex));
			//System.out.println("i = " + i);
			int jIndex = s.indexOf("\t", iIndex+1);
			//System.out.println("jIndex = " + jIndex);
			int j = Integer.parseInt(s.substring(iIndex+1, jIndex));
			//System.out.println("j" + j);
			String query = s.substring(jIndex+1, s.length());
			
			if(i >= startIndex) {
				String outputFileName = "html/" + outputFilePrefix + "_" + Integer.toString(i) + "_" + Integer.toString(j) + ".html"; 			
				this.saveGoogleScholarLoggedPages(query, outputFileName);
				System.out.println(s);
			}
		}
			
		lnr.close();
		fr.close();
	}
	
	public void savePages(String inputFileName, String outputFilePrefix) throws Exception {
		FileReader fr = new FileReader(inputFileName);
		LineNumberReader lnr = new LineNumberReader(fr);
		String s = null;
		int count = 1;

		while ((s=lnr.readLine()) != null) {
			String outputFileName = "html/" + outputFilePrefix + "_" + Integer.toString(count) + ".html"; 			
			String query = "http://scholar.google.com/scholar?q=" + s.replace(" ", "+");
			this.saveGoogleScholarLoggedPages(query, outputFileName);
			System.out.println(s);
			count++;
		}
			
		lnr.close();
		fr.close();
	}
	
	public void saveGoogleScholarLoggedPages(String query, String fileName) throws Exception {
		String html = this.getHtml(query);
		PrintWriter pw = new PrintWriter(fileName);
		pw.println(html);
		pw.close();
	}
	
	public String getHtml(String address) throws Exception {

		while(true) {
			try {
				URL url = new URL(address);
				InputStream stream;
				connection = (HttpURLConnection)url.openConnection();		
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)");
					
				connection.connect(); 
						
				stream = connection.getInputStream(); 
				InputStreamReader isr = new InputStreamReader(stream);
				BufferedReader br = new BufferedReader(isr); 
				
				String html = "";
				String currentLine = "";
				
				while ((currentLine = br.readLine()) != null) { 
					html += currentLine; 
				}
	
				connection.disconnect();
				
				return html;
			}
			catch (Exception e){
			}
		}
	}
	
	/**
	 * @return the connection
	 */
	public static HttpURLConnection getConnection() {
		return connection;
	}


}
