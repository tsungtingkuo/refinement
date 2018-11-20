/**
 * 
 */
package acmtree;

import java.io.*;

/**
 * @author Tim Kuo
 *
 */
public class SearchEngineSimilarityMain6 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		SearchEngineSimilarity ses = new SearchEngineSimilarity();
		BufferedReader daoj = new BufferedReader(new FileReader("gpcr254_keyword.txt"));

		String line,lineName; // also be file Name
		double sim;
		int low = Integer.parseInt(args[0]);
		int high = Integer.parseInt(args[1]);
		int i=low;
		
		for (int j=0;j<low-1;j++){
			lineName = daoj.readLine(); //skip line
		}
		
		int z = i;
		
		while(i <= high){
			
			File file = new File(i + "_gpcr254.txt");
		    
		    if (!file.exists() || !file.isFile()) {
		      System.out.println("File doesn\'t exist");
		      
		    }
		    else if(file.length()!=0){
		    	i++;
		    	continue;
		    }
		    //Here we get the actual size
		
		    PrintWriter pw_label=new PrintWriter(new FileOutputStream (i + "_gpcr254.txt"));
		    
			lineName = daoj.readLine();					
			System.out.println(lineName);			

			BufferedReader acm91 = new BufferedReader(new FileReader("gpcr254_keyword.txt"));

			int count = 1;
			while (acm91.ready()){
				line = acm91.readLine(); 	
						try{
							sim = ses.getSimilarity("%22" + line.replace(' ', '+') + "%22" + "+" + "%22" + lineName.replace(' ', '+') + "%22");
						}
						catch(Exception e){
							sim=0.00;
						}
						System.out.println(count + ", " + sim);
						pw_label.println(sim);
						count++;
			}
			
			i++;
			pw_label.close();	
		}
		
		System.out.println("Data Complete !  From " + z + " to " + high);
	}

}
