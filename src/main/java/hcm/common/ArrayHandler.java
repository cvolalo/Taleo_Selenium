package hcm.common;

import java.util.HashMap;
import java.util.Map;

public class ArrayHandler {
	public static Map<String, String> getArrayArgument(String statement){
		System.out.println("Full statement: "+statement);
		Map<String, String> arrayMap = new HashMap<String, String>();
		String delimiter = "", increment = "1";
		
		String[] aArgs = statement.split("-");
		for(String aArg: aArgs){
			System.out.println("Args: "+aArg);
			String[] args = aArg.trim().split(" ");
			for(String arg: args){
				System.out.println("args: "+arg);
				if(arg.contentEquals("delimiter")) delimiter = aArg.replace("delimiter", "").replaceAll("\"", "").trim();
				else if(arg.contentEquals("increment")) increment = aArg.replace("increment", "").trim();
			}
		}
		System.out.println("Filtered delimiter: "+delimiter);
		arrayMap.put("delimiter", delimiter);
		arrayMap.put("increment", increment);
		return arrayMap;
	}
} 