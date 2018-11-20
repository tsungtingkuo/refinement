package acmtree;

public class Method {
	
	// Random
	public static final int Random = -1;
	
	// Similarity-based (single)
	public static final int Similarity_Level = 31;
	public static final int Similarity_Sibling = 32;
	public static final int Similarity_Children = 33;
	
	public static final int Similarity_Frequency = 34;
	public static final int Similarity_Name = 26;
	public static final int Similarity_Page = 21;
	public static final int Similarity_Jaccard = 16;
	public static final int Similarity_NGD = 0;	
	
	public static final int Similarity_Coauthor = 14;
	public static final int Similarity_Sequence = 18;
	
	// Similarity-based (group)
	public static final int Similarity_Topology = 35;
	public static final int Similarity_Content = 36;
	public static final int Similarity_Social = 37;
	
	// Similarity-based (all)
	public static final int Similarity_OneNorm = 29;
	public static final int Similarity_TwoNorm = 30;
	
	// Learning-based
	public static final int Learning = 6;

	// Learning-based (group)
	public static final int Learning_Topology = 110;
	public static final int Learning_Content = 120;
	public static final int Learning_Social = 130;

	// Learning-based (group, enriched)
	public static final int Learning_ContentEnriched = 121;
	public static final int Learning_SocialEnriched = 131;	

	// Learning-based (all)
	public static final int Learning_All = 100;
	
	// Learning-based (all, enriched)
	public static final int Learning_AllEnriched = 101;

}
