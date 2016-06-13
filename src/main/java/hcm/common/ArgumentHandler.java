package hcm.common;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;

public class ArgumentHandler {
	protected static WebDriver driver = SeleniumDriver.driver;
	private static boolean isScrollingDown = true;
	
	public static Map<String, String> executeFourthArgument(String current, ExcelReader excelReader, int rowNum, int rowGroup, int colNum, String data) throws Exception{
		String[] step = current.split(" \\| ");
		//String data = excelReader.getCellData(rowNum, colNum);
		boolean isFound = false, waitImmunity = false, hasSetCase = false;
		int sleepTime=0, iteration=0;
		String savedEntry="", endingArg="", caseId=""; 
		Map<String, String> retrievingMap = new HashMap<String, String>();
		
		if(step[4].contains("wait")){
				Map<String, String> waitParams = ArgumentExecutor.executeWait(current);
				sleepTime = Integer.parseInt(waitParams.get("waitTime"));
				caseId = waitParams.get("caseId").replace("case ", "");
				waitImmunity = Boolean.parseBoolean(waitParams.get("waitImmunity"));
				if(!caseId.isEmpty() && !caseId.contentEquals("")) hasSetCase = true;
			}
		if(step[4].contains("save")){
				savedEntry = step[0]+","+data;
			}
		if(step[4].contains("trigger")){
				String label = excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum);
				Map<String, String> nextStep = ArgumentExecutor.executeTrigger(step[4], excelReader, label, rowNum, data);
				if(!nextStep.get("e").isEmpty()) colNum += Integer.parseInt(nextStep.get("e"))-1;
				System.out.println("Skips on excel: "+Integer.parseInt(nextStep.get("e")));
				if(!nextStep.get("t").isEmpty()) iteration += Integer.parseInt(nextStep.get("t"))-1;
				System.out.println("Skips on text: "+Integer.parseInt(nextStep.get("t")));
			}
		if(step[4].contains("label")){
			data = excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum);
		}
		if(step[4].contains("missing")){
				isFound = ArgumentExecutor.executeMissing(step[2], step[3]);
				if(!isFound){
					iteration += 1;
					endingArg = "continue fieldloop";
					//continue fieldloop;
				}
		}
		
		if(step[4].contains("encode")){
			String dataRead = ArgumentExecutor.getArgumentStatement(step[4], "encode");
			String[] read = dataRead.split(":");
			if(!read[1].isEmpty())data = read[1].trim();
			else data = ""; 
		}
		
		if(step[4].contains("locate")){
			Thread.sleep(3500);
			TaskUtilities.scrollDownToElement(false, "big");
			Thread.sleep(1500);
			TaskUtilities.customWaitForElementVisibility(step[2], step[3], 60, new CustomRunnable() {
				
				public void customRun() throws Exception {
					// TODO Auto-generated method stub
					isScrollingDown = TaskUtilities.scrollDownToElement(isScrollingDown, "big");
				}
			});
			TaskUtilities.jsScrollIntoView(step[2], step[3]);
		}
		
		if(step[4].contains("unstale")){
			TaskUtilities.customWaitForElementVisibility(step[2], step[3], 60, new CustomRunnable() {
				
				public void customRun() throws Exception {
					// TODO Auto-generated method stub
					TaskUtilities.jsCheckMessageContainer();
					TaskUtilities.jsCheckInputErrors();
				}
			});
			TaskUtilities.jsScrollIntoView(step[2], step[3]);
			TaskUtilities.retryingFindClick(TaskUtilities.getLocator(step[2], step[3]));
		}
		//if(step[4].contains("parse")){
		//	data = ArgumentExecutor.executeCellParser(step[4], excelReader, rowNum, colNum, data);
		//}
		
		if(step[4].contains("case") && !current.startsWith("case:") && !hasSetCase){
			if(current.contains("$.s")){
				System.out.println("Argument type: String");
				int trueColNum = colNum;
				colNum = 0;
				String strArg = "";
				String[] Sarg = current.split("'");
				for(String arg : Sarg){
						if(arg.contains("$.s")){
							strArg = arg.replace("case ", "");
							if(arg.contains("[i]")) colNum = trueColNum-1;
							if(colNum < 0) colNum = 0;
						}
					}
				
				System.out.print("Processing Data");
				inputloop:
				//while(excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum).length()>0){
				while(excelReader.getCellData(rowGroup+1, colNum).length()>0){
					System.out.print(".");
					//if(excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum).contentEquals(strArg.replaceAll("\\$\\.s",""))){
					if(excelReader.getCellData(rowGroup+1, colNum).contentEquals(strArg.replaceAll("\\$\\.s",""))){
						System.out.print("DONE. Formerly "+current);
						current = current.replace(strArg, excelReader.getCellData(rowNum, colNum));
						step = current.split(" \\| ");
						System.out.println(" is now: "+current);
						break inputloop;
					}
					colNum += 1;
				}
				colNum = trueColNum;
			}
			if(step[4].indexOf(",") != -1){
				String[] fourthBox = step[4].split(",");
				for(String aCase: fourthBox){
					if(aCase.contains("case")){
						caseId = aCase.replace("case", "").replace("'", "").trim();
					}
				}
			}
			
			if(step[4].indexOf("'") != -1){
				String[] fourthBox = step[4].split("'");
				for(String aCase: fourthBox){
					if(aCase.contains("case")){
						caseId = aCase.replace("case", "").replace("'", "").trim();
					}
				}
			}
			//if(!step[1].contains("button")) colNum += 1;
		}
		
		retrievingMap.put("sleepTime", ""+sleepTime);
		retrievingMap.put("waitImmunity", ""+waitImmunity);
		retrievingMap.put("savedEntry", savedEntry);
		retrievingMap.put("data", data);
		retrievingMap.put("isFound", ""+isFound);
		retrievingMap.put("caseId", caseId);
		retrievingMap.put("iteration", ""+iteration);
		retrievingMap.put("colNum", ""+colNum);
		retrievingMap.put("endingArg", endingArg);
		
		return retrievingMap;
	}

	public static String executeArgumentConverter(String current, ExcelReader excelReader, int rowNum, int rowGroup, int truecolNum) throws Exception{
		int colNum = 0;
		String[] step = current.split(" \\| ");
		
		if (current.contains("$.s")) {
			System.out.println("Argument type: String");
			//int trueColNum = colNum;
			colNum = 0;
			String strArg = "";
			String[] Sarg = current.split("'");
			for (String arg : Sarg) {
				if (arg.contains("$.s")){
					if(arg.contains("[i]")) colNum = truecolNum-1;
					if(colNum < 0) colNum = 0;
					strArg = arg.replace("case ", "");
				}
			}
			System.out.println("Now holding: " + strArg);
			System.out.print("Appropriate data search in progress");
			inputloop: 
				//while (excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum).length() > 0) {
				while (excelReader.getCellData(rowGroup+1, colNum).length() > 0) {
				System.out.print(".");
				//if (excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum).trim().contentEquals(strArg.replaceAll("\\$\\.s", ""))) {
				if (excelReader.getCellData(rowGroup+1, colNum).trim().contentEquals(strArg.replaceAll("\\$\\.s", "").replaceAll("\\[i\\]", ""))) {
					System.out.print("DONE. Formerly "+ current);
					current = current.replace(strArg, excelReader.getCellData(rowNum,colNum));
					step = current.split(" \\| ");
					System.out.println(" is now: " + current);
					break inputloop;
				}
				if(excelReader.getCellData(rowGroup+1, colNum+1).isEmpty()){
					if(excelReader.getCellData(rowGroup+1, colNum+2).length()>0){
						colNum += 1;
					}
				}
				colNum += 1;
			}
			//colNum = trueColNum;
		}
		if (step[3].contains("$String")) {
			//int trueColNum = colNum;
			colNum = 0;
			System.out.println("element has a String argument: ");
			System.out.print("Looking for appropriate data");
			inputloop: 
			while (excelReader.getCellData(SeleniumDriver.defaultLabelRow, colNum).length() > 0) {
				System.out.print(".");
				if (excelReader.getCellData(SeleniumDriver.defaultLabelRow,colNum).contentEquals(step[0])) {
					System.out.print("DONE. Formerly "+ step[3]);
					//step[3] = step[3].replace("$String",excelReader.getCellData(rowNum,colNum));
					current = current.replace("$String",excelReader.getCellData(rowNum,colNum));
					System.out.println(" is now: " + step[3]);
					break inputloop;
				}
				colNum += 1;
			}
			//colNum = trueColNum;
		}
		
		return current;
	}
}
