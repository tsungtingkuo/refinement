/**
 * 
 */
package acmtree;

import java.io.*;

/**
 * @author Tim Kuo
 *
 */
public class SearchEngineSimilarityMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		SearchEngineSimilarity ses = new SearchEngineSimilarity();
		BufferedReader acm98 = new BufferedReader(new FileReader("ccs91_preprocessed_patch.txt"));
		//BufferedReader acm98 = new BufferedReader(new FileReader("ccs91_preprocessed.txt"));	
		//BufferedReader acm98 = new BufferedReader(new FileReader("ccs98_pn100.txt"));

		String line,lineName; // also be file Name
		double sim;
		int low = Integer.parseInt(args[0]);
		int high = Integer.parseInt(args[1]);
		int part = Integer.parseInt(args[2]);				
		int i=low;
		
		for (int j=0;j<low-1;j++){
			lineName = acm98.readLine(); //skip line
		}
		
		int z = i;
		
		while(i <= high){
			
		    File file = new File(i + "_1101_" + part + ".txt");
			//File file = new File(i + "_100_" + part + ".txt");
		    
		    if (!file.exists() || !file.isFile()) {
		      System.out.println("File doesn\'t exist");
		      
		    }
		    else if(file.length()!=0){
		    	i++;
		    	continue;
		    }
		    //Here we get the actual size
		
			PrintWriter pw_label=new PrintWriter(new FileOutputStream (i + "_1101_" + part + ".txt"));
		    //PrintWriter pw_label=new PrintWriter(new FileOutputStream (i + "_100_" + part + ".txt"));
		    
			lineName = acm98.readLine();					
			System.out.println(lineName);			

			BufferedReader acm91 = new BufferedReader(new FileReader("ccs91_preprocessed" + part + ".txt"));

			while (acm91.ready()){
				line = acm91.readLine(); 	
						try{
							sim = ses.getSimilarity("%22" + line.replace(' ', '+') + "%22" + "+" + "%22" + lineName.replace(' ', '+') + "%22");
						}
						catch(Exception e){
							sim=0.00;
						}
				System.out.println(sim);
				pw_label.println(sim);
			
			}
			
			i++;
			pw_label.close();	
		}
		
		System.out.println("Data Complete !  From " + z + " to " + high);
	}

}
