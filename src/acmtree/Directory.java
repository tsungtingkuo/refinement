package acmtree;

import java.io.*;

public class Directory {

	public static void main(String[] args) throws Exception {
		Directory.createDirectories();
	}
	
	public static void createDirectories() {
		File dirAnalysis = new File("analysis");
		dirAnalysis.mkdir();
		
		File dirFinal = new File("final");
		dirFinal.mkdir();

		File dirLv1 = new File("lv1");
		dirLv1.mkdir();

		File dirResult = new File("result");
		dirResult.mkdir();

		File dirTesting = new File("testing");
		dirTesting.mkdir();
	}
}
