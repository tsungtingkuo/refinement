package interpro;

import java.io.*;
import java.util.*;
import utility.*;

public class Proteins {

	Vector<Protein> proteins = new Vector<Protein>();
	TreeMap<String, Protein> roots = new  TreeMap<String, Protein>();

	public static void main(String[] args) throws Exception {
		//Proteins.toForests();
		//Proteins.clearForests();
	}

	public static void clearForests() throws Exception {
		File dir = new File("forest");
		for(String s : dir.list()) {
			String prefix = s.substring(0, s.lastIndexOf("."));
			System.out.println(prefix);
			clearForest(prefix);
		}
	}
	
	public static void clearForest(String prefix) throws Exception {
		Vector<String> v = Utility.loadVector("forest/" + prefix + ".txt");
		PrintWriter pw = new PrintWriter("ip/" + prefix + ".txt");
		for(String s : v) {
			pw.println(s.substring(0, getIndentIndex(s)) + s.split("::")[1]);
		}
		pw.close();
	}
	
	public static int getIndentIndex(String s) {
		int index = -1;
		for(int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			if(ch != '-') {
				index = i;
				break;
			}
		}
		return index;
	}
	
	public static void toForests() throws Exception {
		File dir = new File("interpro");
		for(String s : dir.list()) {
			String prefix = s.substring(0, s.lastIndexOf("."));
			System.out.println(prefix);
			Proteins ps = new Proteins();
			ps.toForest(prefix);
		}
	}
	
	public void toForest(String prefix) throws Exception {
		load("interpro/" + prefix + ".txt");
		buildForest();
		saveForest("forest/" + prefix + "_forest.txt");
	}
	
	public void saveForest(Protein p, PrintWriter pw, String prefix) throws Exception {
		pw.println(prefix + p.toFileString());
		for(String id : p.getChildren().keySet()) {
			Protein c = p.getChildren().get(id);
			saveForest(c, pw, prefix + "--");
		}
	}
	
	public void saveForest(String fileName) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		for(String id : this.roots.keySet()) {
			Protein p = this.roots.get(id);
			if(p.getChildren().size() > 0) {
				saveForest(p, pw, "");
			}
		}
		pw.close();
	}

	public void buildForest() {
		buildRoots();
		buildTrees();
	}
	
	public void buildTrees() {
		for(Protein p : proteins) {
			if(!p.getParentId().equalsIgnoreCase("")) {
				Protein parent = this.getById(p.getParentId());
				//System.out.println("Child = " + p.getId());
				//System.out.println("Parent = " + parent.getId());
				if(parent != null) {
					p.setParent(parent);
					parent.getChildren().put(p.getId(), p);
				}
			}
		}
	}
	
	public void buildRoots() {
		for(Protein p : proteins) {
			if(p.getParentId().equalsIgnoreCase("")) {
				roots.put(p.getId(), p);
			}
		}
	}
	
	public Proteins() {
		super();
	}
	
	public Proteins(String fileName) throws Exception {
		super();
		this.load(fileName);
	}
	
	/**
	 * @return the proteins
	 */
	public Vector<Protein> getProteins() {
		return proteins;
	}

	/**
	 * @param proteins the proteins to set
	 */
	public void setProteins(Vector<Protein> proteins) {
		this.proteins = proteins;
	}
	
	public void save(String fileName) throws Exception {
		PrintWriter pw = new PrintWriter(fileName);
		for(Protein p : this.proteins) {
			pw.println(p.toFileString());
		}
		pw.close();
	}
	
	public void load(String fileName) throws Exception {
		Vector<String> v = Utility.loadVector(fileName);
		for(String s : v) {
			Protein p = new Protein();
			p.load(s);
			this.proteins.add(p);
		}
	}
	
	public int size() {
		return this.proteins.size();
	}
	
	public int rootSize() {
		int count = 0;
		for(Protein p : this.proteins) {
			if(p.getParentId().equalsIgnoreCase("")) {
				count++;
			}
		}
		return count;
	}
	
	public int nonRootSize() {
		return this.size() - this.rootSize();
	}
	
	public Protein getById(String id) {
		for(Protein p : this.proteins) {
			if(p.getId().equalsIgnoreCase(id)) {
				return p;
			}
		}
		return null;
	}
}
