package acmtree;


/**
 * @author Tim Kuo
 *
 */
public class AuthorSimilarityMain {
	

	public static void main(String[] args) throws Exception {
		
		// Step 0: create crawler 
		AuthorSimilarityMAS as = new AuthorSimilarityMAS();
		
		// Step 1: save pages (level 1)
		as.savePages("ccs91_keyword.txt", "html/html91/ccs91_");
		as.savePages("ccs98_keyword.txt", "html/html98/ccs98_");
		as.savePages("doaj100_keyword.txt", "html/htmlDoaj/doaj100_");
//		as.savePages("gpcr230_keyword.txt", "html/htmlGpcr230/gpcr230_");
//		as.savePages("gpcr254_keyword.txt", "html/htmlGpcr254/gpcr254_");

		// Step 2: get authors (level 1)
		as.getAuthors("html/html91/ccs91_", "html/author91.txt", AuthorSimilarity.TREE91_SIZE);
		as.getAuthors("html/html98/ccs98_", "html/author98.txt", AuthorSimilarity.NEW98_SIZE);
		as.getAuthors("html/htmlDoaj/doaj100_", "html/authorDoaj.txt", AuthorSimilarity.DOAJ100_SIZE);
//		as.getAuthors("html/htmlGpcr230/gpcr230_", "html/authorGpcr230.txt", AuthorSimilarity.GPCR230_SIZE);
//		as.getAuthors("html/htmlGpcr254/gpcr254_", "html/authorGpcr254.txt", AuthorSimilarity.GPCR254_SIZE);

		// Step 3: get links (level 1)
		as.getLinks("html/html91/ccs91_", "html/link91_list.txt", AuthorSimilarity.TREE91_SIZE);
		as.getLinks("html/html98/ccs98_", "html/link98_list.txt", AuthorSimilarity.NEW98_SIZE);
		as.getLinks("html/htmlDoaj/doaj100_", "html/doaj_list.txt.txt", AuthorSimilarity.DOAJ100_SIZE);
//		as.getLinks("html/htmlGpcr230/gpcr230_", "html/linkGpcr230_list.txt", AuthorSimilarity.GPCR230_SIZE);
//		as.getLinks("html/htmlGpcr254/gpcr254_", "html/linkGpcr254_list.txt", AuthorSimilarity.GPCR254_SIZE);

		// Step 4: save pages (level 2)
		as.saveLevelTwoPages("html/link91_list.txt", "html/link91/ccs91_", 1066);
		as.saveLevelTwoPages("html/link98_list.txt", "html/link98/ccs98_", 133);
		as.saveLevelTwoPages("html/doaj_list.txt.txt", "html/linkDoaj/doaj100_", 0);
//		as.saveLevelTwoPages("html/linkGpcr230_list.txt", "html/linkGpcr230/gpcr230_", 0);
//		as.saveLevelTwoPages("html/linkGpcr254_list.txt", "html/linkGpcr254/gpcr254_", 0);
	
		// Step 5: get authors (level 2)
		as.getLevel2Authors("html/link91/ccs91_", "html/link91.txt", AuthorSimilarity.TREE91_SIZE);
		as.getLevel2Authors("html/link98/ccs98_", "html/link98.txt", AuthorSimilarity.NEW98_SIZE);
		as.getLevel2Authors("html/linkDoaj/doaj100_", "html/linkDoaj.txt", AuthorSimilarity.DOAJ100_SIZE);
//		as.getLevel2Authors("html/linkGpcr230/gpcr230_", "html/linkGpcr230.txt", AuthorSimilarity.GPCR230_SIZE);
//		as.getLevel2Authors("html/linkGpcr254/gpcr254_", "html/linkGpcr254.txt", AuthorSimilarity.GPCR254_SIZE);

		// Step 6: get authors (level 1 and 2)
		as.getLevel12Authors("html/link91/ccs91_", "html/author91_level2.txt", AuthorSimilarity.TREE91_SIZE);
		as.getLevel12Authors("html/link98/ccs98_", "html/author98_level2.txt", AuthorSimilarity.NEW98_SIZE);
		as.getLevel12Authors("html/linkDoaj/doaj100_", "html/authorDoaj_level2.txt", AuthorSimilarity.DOAJ100_SIZE);
//		as.getLevel12Authors("html/linkGpcr230/gpcr230_", "html/authorGpcr230_level2.txt", AuthorSimilarity.GPCR230_SIZE);
//		as.getLevel12Authors("html/linkGpcr254/gpcr254_", "html/authorGpcr254_level2.txt", AuthorSimilarity.GPCR254_SIZE);
				
		// Step 7: load similarities (for checking correctness)
		AuthorSimilarity a = new AuthorSimilarity(true);
		a.loadAndComputeAuthorSimilarities(true);
		a.loadAndComputeAuthorSimilarities(false);
	}
	
}
