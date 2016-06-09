package hcm.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

public class TextUtility {
	private BufferedReader br = null;
	private Vector<String> lines = null;
	
	public void read(String path) {
		try {
			lines = new Vector<String>();
			String sCurrentLine = null;
			br = new BufferedReader(new FileReader(path));
			while ((sCurrentLine = br.readLine()) != null) {
				lines.addElement(sCurrentLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public Vector<String> getCollection(String sr, String type) {
		Vector<String> collection = new Vector<String>();
		boolean foundSr = false;
		boolean foundType = false;
		boolean foundCase = false;
		
		Enumeration<String> elements = lines.elements();
		
		
		while(elements.hasMoreElements()) {
			String current = elements.nextElement();
			if(foundSr) {
				if(current.startsWith(":") && current.contains(type)) {
					foundType = true;
					continue;
				}
				if(current.startsWith("[") || current.startsWith(":")) {
				{
					if(foundType)
						break;
				}
				} else if(foundType) {
					collection.addElement(current);
				}
				//If next [ is found and no type yet...
				if(current.startsWith("["))
				{
					if(!foundType){
						collection.addElement("skip:");
					break;
					}
				} 
			} else 
				//if(current.contains(sr)) foundSr = true;
				if(current.contentEquals("["+sr+"]")) foundSr = true;
		}
		return collection;
	}
	
	public Vector<String> getCaseCollection(String sr, String caseItem, String caseType) {
		Vector<String> collection = new Vector<String>();
		boolean foundSr = false;
		boolean foundCase = false;
		boolean foundSec = false;
		
		Enumeration<String> elements = lines.elements();
		
		while(elements.hasMoreElements()) {
			String current = elements.nextElement();
			
			if(foundSr) {
				if(current.contentEquals(caseType)){
					foundSec = true;
				}
				if(current.startsWith("case:") && current.contains(caseItem) && foundSec) {
					foundCase = true;
					continue;
				}
				if(current.startsWith("[") || current.startsWith(":") || current.startsWith("case:")) {
				{
					if(foundCase)
						break;
				}
				} else if(foundCase) {
					collection.addElement(current);
				}
			} else 
				//if(current.contains(sr)) foundSr = true;
				if(current.contentEquals("["+sr+"]")) foundSr = true;
		}
		return collection;
	}
	
}
