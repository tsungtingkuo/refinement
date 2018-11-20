package interpro;

import java.util.TreeMap;

public class Protein {

	String id = "";
	String name = "";
	String parentId = "";
	Protein parent = null;
	TreeMap<String, Protein> children = new  TreeMap<String, Protein>();
	
	/**
	 * @return the children
	 */
	public TreeMap<String, Protein> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(TreeMap<String, Protein> children) {
		this.children = children;
	}

	public void load(String fileString) {
		String[] t = fileString.split("::");
		this.id = t[0];
		this.name = t[1];
		if(t.length > 2) {
			this.parentId = t[2];
		}
	}
	
	public Protein() {
		super();	
	}
	
	public Protein(String id) {
		super();
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the parent
	 */
	public Protein getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(Protein parent) {
		this.parent = parent;
	}
	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String toFileString() {
		return this.id + "::" + this.name + "::" + this.parentId;
	}
}
