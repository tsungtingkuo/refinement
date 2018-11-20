/**
 * 
 */
package acmtree;

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.*;
import utility.*;


/**
 * @author Tim Kuo
 *
 */
public class SearchEngineSimilarity {

	public static HttpURLConnection connection;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SearchEngineSimilarity ses = new SearchEngineSimilarity();
		
		// Multiple
//		ses.getSimilarities("gpcr230_keyword.txt", "gpcr230.txt");
		
		// Single
		String s1 = "GPCR, rhodopsin-like superfamily";
		String s2 = "Orexin receptor, type 1";
		double sim = ses.getSimilarity("%22" + s1.replace(' ', '+') + "%22" + "+" + "%22" + s2.replace(' ', '+') + "%22");
		System.out.println(sim);
	}

	public void getSimilarities(String inputFileName, String outputFileName) throws Exception {
		Vector<String> v = Utility.loadVector(inputFileName);
		PrintWriter pw = new PrintWriter(outputFileName);
		for(int i=0; i<v.size(); i++) {
			String s = v.get(i);
			String query = "%22" + s + "%22";
			query = query.replace(" ", "%20");
			double sim = this.getSimilarity(query);
			pw.println(sim);
			System.out.println((i+1) + "/" + v.size() + ", " + s + ", " + sim);
		}
		pw.close();
	}
	
	public double getSimilarity(String query) throws Exception {
		
		double loggedPages = this.getYahooLoggedPages(query);
		//double loggedPages = this.getGoogleLoggedPagesFromAPI(query); 
		//double loggedPages = this.getGoogleLoggedPages(query);
		//double loggedPages = this.getACMDigitalLibraryLoggedPages(query);
		//double loggedPages = this.getACMGuideLoggedPages(query);
		
		// We might refine this later
		
		return loggedPages;
	}
	
	public double getYahooLoggedPages(String query) throws Exception {
		//System.out.println("http://search.yahoo.com/search?p=" + query);
		String html = this.getHtml("http://search.yahoo.com/search?p=" + query);
		int startIndex = html.indexOf("<strong id=\"resultCount\">") + 25;
		int stopIndex = html.indexOf("</strong>", startIndex);
		String temp = html.substring(startIndex, stopIndex);
		String result = "";
		for(int i=0; i<temp.length(); i++) {
			if(temp.charAt(i) != ',') {
				result += temp.charAt(i);
			}
		}
		double value = Double.valueOf(result);
		//System.out.println(value);
		if(value == 0.0) {
			return value;
		}
		else {
			return Math.log10(value);
		}
	}
	
//	public double getYahooLoggedPages(String query) throws Exception {
//		System.out.println("http://search.yahoo.com/search?p=" + query);
//		String html = this.getHtml("http://search.yahoo.com/search?p=" + query);
//		
//		int testIndex = html.indexOf(" for <strong>");
//
//		if(testIndex != -1) {
//			int stopIndex = testIndex;
//			int startIndex = html.lastIndexOf(" of ", stopIndex) + 4;
//			String temp = html.substring(startIndex, stopIndex);
//			String result = "";
//			for(int i=0; i<temp.length(); i++) {
//				if(temp.charAt(i) != ',') {
//					result += temp.charAt(i);
//				}
//			}
//			double value = Double.valueOf(result);
//			//System.out.println(value);
//			return Math.log10(value);
//		}
//		else {
//			return 0;
//		}
//	}
	
	public double getGoogleLoggedPagesFromAPI(String query) throws Exception {
		
		URL url = new URL("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=" + query);
		
		URLConnection connection = url.openConnection();
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while((line = reader.readLine()) != null) {
			builder.append(line);
		}

		//System.out.println(builder);

		if(!builder.toString().contains("\"results\":[]")) {
			JSONObject json = new JSONObject(builder.toString());
			JSONObject responseData = json.getJSONObject("responseData");
			JSONObject cursor = responseData.getJSONObject("cursor");
			double value = cursor.getDouble("estimatedResultCount");			
			//System.out.println(value);		
			return Math.log10(value);
		}
		else {
			return 0;
		}
		
	}
	
	public double getGoogleLoggedPages(String query) throws Exception {
		String html = this.getHtml("http://www.google.com.tw/search?hl=en&q=" + query);
		
		int testIndex = html.indexOf("</b> of about <b>");

		if(testIndex != -1) {
			int startIndex = testIndex + 17;
			int stopIndex = html.indexOf("</b>", startIndex);
			String temp = html.substring(startIndex, stopIndex);
			String result = "";
			for(int i=0; i<temp.length(); i++) {
				if(temp.charAt(i) != ',') {
					result += temp.charAt(i);
				}
			}
			double value = Double.valueOf(result);
			//System.out.println(value);
			return Math.log10(value);
		}
		else {
			return 0;
		}
	}

	public double getACMDigitalLibraryLoggedPages(String query) throws Exception {
		String html = this.getHtml("http://portal.acm.org/results.cfm?coll=Portal&dl=Portal&CFID=13281553&CFTOKEN=34564051&parser=Internet&whichDL=acm&query=" + query);
		
		int failIndex = html.indexOf("was not found");
		int testIndex = html.indexOf("Found <b>");

		if(failIndex == -1 && testIndex != -1) {
			int startIndex = testIndex + 9;
			int stopIndex = html.indexOf("</b>", startIndex);
			String temp = html.substring(startIndex, stopIndex);
			String result = "";
			for(int i=0; i<temp.length(); i++) {
				if(temp.charAt(i) != ',') {
					result += temp.charAt(i);
				}
			}
			double value = Double.valueOf(result);
			//System.out.println(value);
			return Math.log10(value);
		}
		else {
			return 0;
		}
	}
	
	// This will return the result of ACM Digital Library.
	// Need debugging.
	public double getACMGuideLoggedPages(String query) throws Exception {
		String html = this.getHtml("http://portal.acm.org/results.cfm?coll=Portal&dl=Portal&CFID=13290869&CFTOKEN=43330691&parser=Internet&whichDL=guide&query=" + query);
		
		int failIndex = html.indexOf("was not found");
		int testIndex = html.indexOf("Found <b>");

		if(failIndex == -1 && testIndex != -1) {
			int startIndex = testIndex + 9;
			int stopIndex = html.indexOf("</b>", startIndex);
			String temp = html.substring(startIndex, stopIndex);
			String result = "";
			for(int i=0; i<temp.length(); i++) {
				if(temp.charAt(i) != ',') {
					result += temp.charAt(i);
				}
			}
			double value = Double.valueOf(result);
			//System.out.println(value);
			return Math.log10(value);
		}
		else {
			return 0;
		}
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
}
