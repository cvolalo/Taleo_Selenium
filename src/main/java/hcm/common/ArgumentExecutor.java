package hcm.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * This class consolidates all statement declared in the fifth step
 * that needs to be executed.
 * 
 * @author jerrick.m.falogme
 */
public class ArgumentExecutor {
	protected static WebDriver driver = SeleniumDriver.driver;
	
	public static String getArgumentStatement(String statement, String argument) throws Exception{
		String[] sArgs = statement.split(",");
		int i = 0;
		while(i<sArgs.length){
			if(sArgs[i].contains(argument)){
				return sArgs[i];
			}
			i+=1;
		}
		return "";
	}
	public static String executeArithmetic(String statement, int intArgument) throws Exception{
		String result = "";
		int i = 0;
		
		statement = statement.replace("execute:", "");
		String[] parts = statement.split(" ");
		while(i < parts.length){
			if(parts[i].contains("Int")){
				parts[i] = ""+intArgument;
			}
			result += parts[i];
			i += 1;
		}
		
		System.out.print("Now executing: "+result);
		JavascriptExecutor js = (JavascriptExecutor)driver;
		result = ""+js.executeScript("return eval(\""+result+"\");");
		System.out.println("..DONE.");
		
		return result;
	}
	public static Map<String, String> executeTrigger(String statement, ExcelReader excelReader, String label, int rowNum, String data) throws Exception{
		int defaultLabelRow = SeleniumDriver.defaultLabelRow;
		int colNum = 0;
		int i = 0;
		System.out.print("Original statement: "+statement);
		statement = statement.substring(statement.indexOf("trigger:"));
		statement = statement.substring(8, statement.indexOf(","));
		System.out.print("Acquired statement: "+statement);
		String[] sArgs = statement.split(" -");
		//Temporary Map<String, String> savedEntry = new HashMap<String, String>();
		Map<String, String> trueArray = new HashMap<String, String>();
		Map<String, String> falseArray = new HashMap<String, String>();
		
		while(i<sArgs.length){
			String option = sArgs[i];
			if(option.startsWith("t")){
					option = option.replace("t", "");
					String[] choice = option.split(":");
					trueArray.put("t", choice[0].trim());
					falseArray.put("t", choice[1].trim());
				}
			if(option.startsWith("e")){
					option = option.replace("e", "");
					String[] choice = option.split(":");
					trueArray.put("e", choice[0].trim());
					falseArray.put("e", choice[1].trim());
				}
			i += 1;
		}
		
	/*	if(data.toLowerCase().trim().contains("yes") || data.toLowerCase().trim().contains("true")){
				System.out.println("Non-skippable task...");
				return trueArray;
			}else if(excelReader.getCellData(rowNum, colNum).toLowerCase().contains("no")
					|| data.isEmpty() || data.contains("blank")	|| data.toLowerCase().contains("false")){
				System.out.println("Skippable task found...");
				return falseArray;
			}*/
		
		//06/01
		if(!data.isEmpty()){
			if(data.toLowerCase().trim().contains("yes") || data.toLowerCase().trim().contains("true")){
				System.out.println("Non-skippable task...");
				return trueArray;
			} else if(excelReader.getCellData(rowNum, colNum).toLowerCase().contains("no")
					|| data.contains("blank")	|| data.toLowerCase().contains("false")){
				System.out.println("Skippable task found...");
				return falseArray;
			} else{
				return trueArray;
			}
			
		} else	{
			return falseArray;
		}
	} 
		
		/*while(excelReader.getCellData(defaultLabelRow, colNum).length()>0){
			
			if(excelReader.getCellData(defaultLabelRow, colNum).trim().contentEquals(label)){
				if(excelReader.getCellData(rowNum, colNum).toLowerCase().contains("yes")
						|| excelReader.getCellData(rowNum, colNum).toLowerCase().contains("true")){
					}else if(excelReader.getCellData(rowNum, colNum).toLowerCase().contains("no")
							|| excelReader.getCellData(rowNum, colNum).isEmpty()
							|| excelReader.getCellData(rowNum, colNum).contains("blank")
							|| excelReader.getCellData(rowNum, colNum).toLowerCase().contains("false")){
						System.out.println("Skippable task found...");
						return falseArray;
					}
			}
			
			colNum += 1;
		}*/
	//	return falseArray;
//	}
	public static String executeThrower(String statement) throws Exception{
		statement = getArgumentStatement(statement, "throws");
		if(statement.indexOf(":") != -1){
			String[] throwSet = statement.split(":");
			for(String throwItem: throwSet){
				if(throwItem.contains("throws")){
					statement = throwItem;
					break;
				}
			}
		}
		
		statement = statement.substring(statement.indexOf("throws ")+7);
		statement = statement	.replace("Invalid", "")
								.replace("Duplicate", "")
								.replace("NotFound", "");
		return statement;
	}
	public static Map<String, String> executeSetExcelRow(String statement, ExcelReader excelReader, int pivotIndex) throws Exception{
		System.out.println("Resetting Excel Row...");
		int rowNum = 0;
		int rowGroup = 0;
		Map<String, String> cellProp = new HashMap<String, String>();
		statement = statement.replaceAll("setExcelRow:", "");
		String group = statement.trim();
		
		System.out.print("locating target row");
		while(true){
			System.out.print(".");
			if(excelReader.getCellData(rowNum, SeleniumDriver.defaulColNum).contentEquals(group)){
					System.out.println("DONE");
					rowGroup = rowNum;
					rowNum = rowNum + 2 + pivotIndex - 1;
					cellProp.put("rowGroup", ""+rowGroup);
					cellProp.put("rowNum", ""+rowNum);
					return cellProp;
					//return rowNum + 2 + pivotIndex - 1;
				}
			
			rowNum += 1;
		}
	}
	public static int executeSetExcelColumn(String statement, ExcelReader excelReader, int rowGroup) throws Exception{
		System.out.println("Resetting Excel Column...");
		//int rowNum = SeleniumDriver.defaultLabelRow;
		int rowNum = rowGroup + 1;
		statement = statement.replaceAll("setExcelCol:", "");
		String label = statement.trim();

		int colNum = 0;
		System.out.print("Locating label");
		while(excelReader.getCellData(rowNum, colNum).length()>0){
			System.out.print(".");
			if(excelReader.getCellData(rowNum, colNum).trim().contentEquals(label)){
					System.out.println("DONE");
					return colNum;
				}
			if(excelReader.getCellData(rowNum, colNum+1).isEmpty()){
				if(excelReader.getCellData(rowNum, colNum+2).length()>0){
					colNum += 1;
				}
			}
			colNum += 1;
		}
		return colNum+1;
	}
	public static boolean executeMissing(String locType, String locator) throws Exception{
		System.out.println("Checking if element is missing...");
		try{
				TaskUtilities.customWaitForElementVisibility(locType, locator, 12);
			} catch(TimeoutException te){
				System.out.println("Element is missing");
				return false;
			}
		
		System.out.println("Element is not missing");
		return true;
	}
	public static Map<String, String> executeWait(String statement) throws Exception{
		Map<String, String> waitArray = new HashMap<String, String>();
		String[] step = statement.split(" \\| ");
		boolean waitImmunity = false;
		statement = step[4];
		String time = "", caseId="", trueCase="", falseCase="";
		int waitTime = 0;
		
		System.out.println("Original statement: "+statement);
		if(statement.indexOf(",") != -1){
				statement = ""+getArgumentStatement(statement, "wait");
			}
				statement = ""+statement.substring(4);
		
		if(!statement.isEmpty() && statement.indexOf("-") != -1){
			String[] parts = statement.split("-");
			time = parts[0].trim();
			if(parts[1].startsWith("c")){
				if(parts[1].contains(":")){
					String[] caseParts = parts[1].replace("c ", "").trim().split(":");
					if(caseParts.length>0 && !caseParts[0].isEmpty()) trueCase = caseParts[0].replace("case ", "").trim();
					if(caseParts.length>1 && !caseParts[1].isEmpty()) falseCase = caseParts[1].replace("case ", "").trim();
				}else{
					trueCase = ""+parts[1].replace("c ","").replace("case ", "");
					if(!trueCase.isEmpty()) {
						if(trueCase.contentEquals("c")) trueCase = "";
						trueCase = trueCase.trim();
					}
				}
			}
		
		}else if(!statement.isEmpty()){
			time = statement.trim();
		}
		
		if(statement.isEmpty()){
				waitTime = 3500;
			}else{
				waitTime = Integer.parseInt(time) * 1000;
			}
		
		if(step[4].contains("-c")){
			try{
					TaskUtilities.customWaitForElementVisibility(step[2], step[3], waitTime/1000, new CustomRunnable() {
						
						public void customRun() throws Exception {
							// TODO Auto-generated method stub
							TaskUtilities.jsCheckMessageContainer();
							TaskUtilities.jsCheckInputErrors();
						}
					});
					if(!trueCase.contains("throws")) caseId = trueCase;
					if(trueCase.contains("throws")) throw new TimeoutException("Exception \""+trueCase+"\" ");
				}catch(Exception e){
					caseId = falseCase;
					waitImmunity = true;
					if(falseCase.contains("throws")) throw new TimeoutException("Exception \""+caseId+"\" "+e.getMessage());
				}
			waitTime = 0;
		}
		waitArray.put("caseId", caseId);
		waitArray.put("waitTime", ""+waitTime);
		waitArray.put("waitImmunity", ""+waitImmunity);
		return waitArray;
	}
	public static String executeCellParser(String statement, ExcelReader excelReader, int row, int col, String data) throws Exception{
		statement = getArgumentStatement(statement, "parse");
		if(statement.contains("number")){
			data = excelReader.getCellData(row, col, "number");
		}
		return data;
	}
	
	public static String executeTakeScreenshot(String statement, String caseName) throws Exception{
		String[] sParts = statement.split(":");
		if(sParts[1].isEmpty()){
			return caseName;
		}else{
			String[] sArgs = sParts[1].split("-");
			for(String arg : sArgs){
				if(arg.contains("name")){
					String[] nArgs = arg.split("\"");
					return nArgs[1];
				}
			}
		}
		return caseName;
	}
	
	public static List<String> parseCombobox(String name, String locator, String data) throws Exception{
		String id = "";
		//String parseAsA = "";
		List<String> actionSet = new ArrayList<String>();
		String[] g1  = locator.split("\\[");
		for(String g: g1){
			if(g.contains("@id")){
				id = g;
				break;
			}
		}
		String[] g2 = id.split("\\]");
		for(String g: g2){
			if(g.contains("@id")){
				id = g;
				break;
			}
		}
		
		//if(name.contains("Postal Code")) parseAsA += "parse number";
		
		actionSet.add(name+" | button | xpath | //tr["+id+"]//label[text()='"+name+"']");
		actionSet.add(name+" | button-enter | xpath | //tr["+id+"]//a[contains(@title,'"+name+"')]");
		actionSet.add("Find... | button | xpath | //div["+id+"]//a[text()='Search...']");
		//actionSet.add(name+" | textbox-enter | xpath | //table["+id+"]//label[text()=' "+name+"']/..//input");
		actionSet.add(name+" | textbox-enter | xpath | //table["+id+"]//label/..//input");
		//actionSet.add("Find | button-enter | xpath | //table["+id+"]//button[text()='Search'] | wait");
		actionSet.add("Pick | button | xpath | //table["+id+"]//td[text()='"+data+"'] | unstale");
		actionSet.add("OK | button | xpath | //table["+id+"]//button[text()='OK']");
		actionSet.add("Wait to disappear | h1 | xpath | //div[text()='Search and Select: "+name+"']");
		return actionSet;

	}
}
