package interpro;

import java.util.*;
import utility.*;

public class XmlRelationExtractor {

	int idStart = 14;
	int idStop = 23;
	int nameStart = 8;
	int nameStop = 7;
	int parentIdStart = 22;
	int parentIdStop = 3;
	int first = 0;
	String prefix = "interpro.26.0";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		XmlRelationExtractor.getRelations();
	}

	public static void getRelations() throws Exception {
		new XmlRelationExtractor(14, 23, 8, 7, 22, 3, 0, "interpro.27.0");
//		new XmlRelationExtractor(14, 23, 8, 7, 22, 3, 0, "interpro.26.0");
//		new XmlRelationExtractor(14, 23, 8, 7, 22, 3, 0, "interpro.25.0");
//		new XmlRelationExtractor(14, 23, 8, 7, 22, 3, 0, "interpro.24.0");
//		new XmlRelationExtractor(14, 23, 6, 7, 18, 3, 0, "interpro.23.1");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.22.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.21.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.20.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.19.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.18.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.14.1");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 0, "interpro.13.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 0, "interpro.12.1");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 2, "interpro.12.0");
//		new XmlRelationExtractor(14, 23, 10, 7, 24, 4, 0, "interpro.11.0");
	}

	public XmlRelationExtractor(String prefix) throws Exception {
		super();
		this.prefix = prefix;
		this.getRelation();
	}
	
	public XmlRelationExtractor(int idStart, int idStop, int nameStart,
			int nameStop, int parentIdStart, int parentIdStop, int first, String prefix) throws Exception {
		super();
		this.idStart = idStart;
		this.idStop = idStop;
		this.nameStart = nameStart;
		this.nameStop = nameStop;
		this.parentIdStart = parentIdStart;
		this.parentIdStop = parentIdStop;
		this.prefix = prefix;
		this.first = first;
		this.getRelation();
	}

	public String processParentId(String s) {
		return s.substring(parentIdStart, s.length()-parentIdStop);
	}

	public String processName(String s) {
		return s.substring(nameStart, s.length()-nameStop);
	}

	public String processId(String s, boolean isFirst) {
		if(isFirst) {
			return s.substring(idStart + first, idStop + first);
		}
		else {
			return s.substring(idStart, idStop);
		}
	}
	
	public void getRelation() throws Exception {
		Vector<String> v = Utility.loadVector("xml/" + prefix + ".xml");
		Proteins ps = new Proteins();
		boolean saving = false;
		boolean isFirst = true;
		Protein p = null;
		for(String s : v) {
			if(s.contains("<interpro id")) {
				p = new Protein(processId(s, isFirst));
				if(isFirst) {
					isFirst = false;
				}
				ps.getProteins().add(p);
			}
			else if(s.contains("<name>")) {
				p.setName(processName(s));
			}
			else if(s.contains("<parent_list>")) {
				saving = true;
			}
			else if(saving) {
				if(s.contains("</parent_list>")) {
					saving = false;
				}
				else {
					p.setParentId(processParentId(s));
				}
			}
		}
		ps.save("interpro/" + prefix + ".txt");
	}
}
