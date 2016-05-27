package hcm.common;

import java.util.Enumeration;
import java.util.Vector;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;
import hcm.utilities.TextUtility;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class InputErrorHandler {
	protected static WebDriver driver = SeleniumDriver.driver;
	
	public static String identifyInputErrors(TextUtility textReader, ExcelReader excelReader, String name, int rowNum) throws Exception{
		String appendPath, knownErrPath, modifiedPath;
		String label = "";
		Thread.sleep(5000);
		
			knownErrPath = "//*[contains(@class,'Error')]";
			try{
					TaskUtilities.customWaitForElementVisibility("xpath", knownErrPath, 5);
					System.out.println("Input error has been found...");
				} catch(TimeoutException te){
					//Skips...
				}
			modifiedPath = knownErrPath+"//label";
			//System.out.print("New modified path..."+modifiedPath+" ");
			
			if(TaskUtilities.is_element_visible("xpath", knownErrPath+"//label")){
				JavascriptExecutor js = (JavascriptExecutor)driver;
				label = (String)js.executeScript(
						"function getElementByXPath(xPath){"+
								"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
								"}"+
								"var label = getElementByXPath(\""+modifiedPath+"\");"+
								"if(label != null)"+		
								"{return label.textContent;}"+
								"else{return '';}"
						);
				System.out.println("Error label is "+label);
				return label;
			}
		return "";		
	}
}
