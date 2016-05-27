package hcm.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.directory.NoSuchAttributeException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import hcm.seldriver.SeleniumDriver;

/**
 * This class consolidate all commonly used methods
 * 
 * 
 * @author jerrick.m.falogme
 */

public class TaskUtilities{
	
	protected static WebDriver driver = SeleniumDriver.driver;
	
	public static boolean is_element_visible(String type, String value) {
		By by = null;
		
		if(type.contentEquals("id")) by =  By.id(value);
		else if(type.contentEquals("xpath")) by =  By.xpath(value);
		else if(type.contentEquals("tagname")) by = By.xpath(value);
		else if(type.contentEquals("classname")) by = By.className(value);
		else if(type.contentEquals("cssselector")) by = By.cssSelector(value);
		else if(type.contentEquals("name")) by = By.name(value);
		else if(type.contentEquals("linktext")) by = By.linkText(value);
		else by = By.partialLinkText(value);
		
		try {
			driver.findElement(by).isDisplayed();
			return true;
		}

		catch (NoSuchElementException e) {
			return false;
		}

	}
	
	public static By getLocator(String type, String value) {
		if(type.contentEquals("id")) return By.id(value);
		else if(type.contentEquals("xpath")) return By.xpath(value);
		else if(type.contentEquals("tagname")) return By.tagName(value);
		else if(type.contentEquals("classname")) return By.className(value);
		else if(type.contentEquals("cssselector")) return By.cssSelector(value);
		else if(type.contentEquals("name")) return By.name(value);
		else if(type.contentEquals("linktext")) return By.linkText(value);
		else return By.partialLinkText(value);
	}
	
	private static String getJsLocator(String type, String value) throws NoSuchAttributeException{
		
		if(type.contentEquals("id")) return "getElementById";
		else if(type.contentEquals("xpath")) return "getElementByXPath";
		else if(type.contentEquals("tagname")) return "getElementsByTagName";
		else if(type.contentEquals("classname")) return "getElementsByClassName";
		else if(type.contentEquals("cssselector")) throw new NoSuchAttributeException();
		else if(type.contentEquals("name")) throw new NoSuchAttributeException();
		else if(type.contentEquals("linktext")) throw new NoSuchAttributeException();
		else throw new NoSuchAttributeException();
	}
	
	public static int  surveyCurrentTableInputs(String tablePath) throws Exception{
		int afrrkInt = -1;
		boolean isScrollingDown = true;
		
		if(!is_element_visible("xpath", "//div[contains(@style,'overflow:hidden')]"+tablePath+"/..")){
				while(isScrollingDown){
					isScrollingDown = scrollDownToElement(isScrollingDown, "big");
				}
			}
		//List<WebElement> queryFolder = driver.findElements(By.xpath("//table[contains(@summary,'Establish Enterprise Structures')][contains(@summary,'"+currentStep+"')]//tr"));
		List<WebElement> queryFolder = driver.findElements(By.xpath(tablePath));
		System.out.println("folder size is "+queryFolder.size());
		for(WebElement inputEntry : queryFolder){
			
			String afrrk = inputEntry.getAttribute("_afrrk");
			System.out.println("afrrk is "+afrrk);
			
			if(afrrk != null && !afrrk.isEmpty() && !afrrk.contentEquals("")){
				if(Integer.parseInt(afrrk) > afrrkInt){
					afrrkInt =  Integer.parseInt(afrrk);
				}else{
					//Skips...
				}
			}
		}
		
		scrollDownToElement(false, "");
		afrrkInt += 1;
		System.out.print("afrrkInt is now: "+afrrkInt);
		return afrrkInt;
	}
	
	public static String retryingSearchfromDupInput(String dataLocator, String parentPath) throws Exception{

        int attempts = 0;
        int labelAttempts = 0;
        
        String[] labelStructArray = {
        		parentPath+"//td//label[text() ='"+dataLocator+"']",
        };
        
        String[] inputTypesArray = {
        		"/../input",
        		"/../../td/input",
        		"/../../td/span/input",
        		"/../../td/span/span/input",
        		"/../../td/select",
        		"/../../td/table/tbody/tr/td/table/tbody/tr/td/span/select",
        		"/../../td/table/tbody/tr/td/input",
        		"/../../td/table/tbody/tr/td/table/tbody/tr/td/span/input",
        		"/../..//td/textarea",
        		"/../..//input",
        		"/..//input",
        		"/../..//select",
        		"/..//select",
        		
        };
        
        if(dataLocator.isEmpty() || dataLocator == null){
	        	System.out.println("The dataLocator: "+dataLocator+" cannot be a label.");
	        	return null;
	        }
	    
		System.out.println("Validating if dataLocator: "+dataLocator+" is visible.");
        labelloop:
        while(labelAttempts < labelStructArray.length){
	        try{
		        	customWaitForElementVisibility("xpath", labelStructArray[labelAttempts], 10);
		        	jsScrollIntoView("xpath", labelStructArray[labelAttempts]);
		        	break labelloop;
		        } catch(TimeoutException e){
		        	if(labelAttempts >= labelStructArray.length){
		        		
		            	System.out.println("The dataLocator: "+dataLocator+" cannot be a label.");
		            	return null;
		        	}
		        	labelAttempts += 1;
		        }
        }
        
        System.out.println("Attempting to find known valid path for dataLocator: "+dataLocator);
        while(attempts < inputTypesArray.length) {
            try {
            		String compoundPath = labelStructArray[labelAttempts]+inputTypesArray[attempts];
            		jsScrollIntoView("xpath", compoundPath);
	                driver.findElement(By.xpath(compoundPath)).click();
	                System.out.println("Valid Input path has been found"+" after "+attempts+" tries...");
	                System.out.println("Assigned path: \n"+compoundPath);
	                return compoundPath;
	                
	            } catch(Exception e){
	            	
	            }
            attempts++;
        }
        System.out.println("No valid path can be assigned after "+attempts+" tries...");
        return null;
	}
	
	//Steps has been disabled
	public static boolean scrollDownToElement(boolean isScrollingDown, String scrollType) throws Exception{
		
		System.out.println("Initializing scroll down....");
		int scrollValue;
		scrollValue = 50;
		if(scrollType.toLowerCase().contentEquals("big")){
			scrollValue = 400;
		}
		/*switch(scrollType){
			case "small":
				scrollValue = 50;
				break;
			case "normal":
				scrollValue = 150;
				break;
			case "big":
				scrollValue = 400;
				break;
			default:
				scrollValue = 150;
		}*/
		
		System.out.println("Scroll is now moving....");	
		JavascriptExecutor js = (JavascriptExecutor)driver;
		boolean scrollDownAgain = (Boolean) js.executeScript(
				
			"taskFolderArray=[];"+
			"taskFolderInt = -255;"+
			"queryFolderName = [];"+
			"oldScrollerValue = 0;"+
			"queryFolderName = document.querySelectorAll('div');"+

			"for(var i=0; i<queryFolderName.length;i++){"+
			"	curFolderId = queryFolderName[i].id;"+
			"	curFolderId1 = queryFolderName[i].style.overflow;"+
			"	curFolderId2 = queryFolderName[i].style.position;"+
			"	if(taskFolderInt < 0)taskFolderInt = -1;"+
			"	if((curFolderId1 === 'auto' && curFolderId2 === 'absolute') || curFolderId.contains('scroller')){"+
			"		taskFolderInt += 1;	"+
			"		taskFolderArray[taskFolderInt] = [curFolderId, curFolderId1, curFolderId2];"+
			"}}"+
      
			"for(var j =0; j<taskFolderArray.length;j++){"+
			"	  newScroller = document.getElementById(taskFolderArray[j][0]);"+
			"	  if(newScroller.scrollTop != undefined && newScroller != 'null'){"+
			"			if("+isScrollingDown+") {"+
			"				if(taskFolderArray[j][0].contains('scroller')){"+
			"					oldScrollerValue = newScroller.scrollTop;}"+
			
			"				newScroller.scrollTop += "+scrollValue+";}"+
			"			else if(!"+isScrollingDown+") newScroller.scrollTop = 0;"+
			"			if(oldScrollerValue == newScroller.scrollTop"+
			"				&& taskFolderArray[j][0].contains('scroller')"+
			"					&& oldScrollerValue > 0)"+
			"					return false;"+
			"	  }"+
			"}return true;"
		);
		//SLOW INTERNET CONNECTION might REQUIRE -- Higher Wait time: Recommended(5*2)
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
		fluentWaitForElementInvisibility("//div[text()='Fetching Data...']", "Fetching Data...", 10);
		
		return scrollDownAgain;
	}
	
	public static void fluentWaitForElementInvisibility(String xPath, String textValue, int waitTime) throws Exception{
		
		Thread.sleep(250); //Momentary pause.....
		Wait<WebDriver> waitLoadHandler = new FluentWait<WebDriver>(driver)
				.withTimeout(waitTime, TimeUnit.SECONDS)
				.pollingEvery(500, TimeUnit.MILLISECONDS)
				.ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class);
		
		waitLoadHandler.until(ExpectedConditions.invisibilityOfElementWithText(By.xpath(xPath), textValue));
		System.out.println("Page loading has been finished.....");
		//log("Page loading has been finished.....");
	}
	
	public static void customWaitForElementVisibility(String locType, String locator, int waitTime, CustomRunnable runner) throws Exception{
		
		long startTime = System.currentTimeMillis();
		waitTime = waitTime * 1000;
		while(!is_element_visible(locType, locator)){
			try{
					runner.customRun();
				} catch(StaleElementReferenceException e){
					//Skips...
				}
			
			if(System.currentTimeMillis() - startTime > waitTime){
				throw new TimeoutException(waitTime/1000 + " second/s has elapsed after waiting for: "+locType+" "+locator);
			}
			
		}		
		System.out.println("Element is now visible after "+(System.currentTimeMillis() - startTime)/1000+" second/s.....");
	}
	
	public static void customWaitForElementVisibility(String locType, String locator, int waitTime) throws Exception{
		
		long startTime = System.currentTimeMillis();
		waitTime = waitTime * 1000;
		
		while(!is_element_visible(locType, locator)){
			//Just wait here...
			if(System.currentTimeMillis() - startTime > waitTime){
				throw new TimeoutException(waitTime/1000 + " second/s has elapsed after waiting for: "+locType+" "+locator);
			}
			
		}
		Thread.sleep(50);
		System.out.println("Element is now visible after "+(System.currentTimeMillis() - startTime)/1000+" second/s.....");
	}

	public static void customWaitForElementInvisibility(String locType, String locator, int waitTime) throws Exception{
		
		long startTime = System.currentTimeMillis();
		waitTime = waitTime * 1000;
		
		while(is_element_visible(locType, locator)){
			//Just wait here...
			if(System.currentTimeMillis() - startTime > waitTime){
				throw new TimeoutException(waitTime/1000 + " second/s has elapsed after marking: "+locType+" "+locator);
			}
			
		}
		Thread.sleep(250);
		System.out.println("Element is now not visible after "+(System.currentTimeMillis() - startTime)/1000+" second/s.....");
	}
	
	public static void retryingFindClick(By by) throws Exception{

		String text, tag;
        int attempts = 0;
        boolean scrollDown = true;
        
        System.out.print("Attempting to catch element");
        while(attempts < 11) {
            try {
            	System.out.print(".");
                text = driver.findElement(by).getText();
        		tag = driver.findElement(by).getTagName();
                driver.findElement(by).click();
                System.out.println("SUCCESSFUL.\nElement has been refreshed after "+attempts+" tries.....");
        		
        		if(tag.contentEquals("a")){
	        			tag = "link";
	        		} else if(tag.contentEquals("span")){
	        			tag = "button";
	        		}
        		
        		if(text.indexOf("\n") != -1){
        			int enterIndex = text.indexOf("\n");
        			text = text.substring(0, enterIndex);
        		}
        		
        		System.out.println("Clicked "+text+" "+tag+"...");
                
                return ;
            } catch(org.openqa.selenium.StaleElementReferenceException e) {
            
            } catch(WebDriverException e){
            	
            }
            attempts++;
        }
        
        System.out.println("Failed to find path: "+by+"");
        System.out.println("Throwing Error.....");
        throw new StaleElementReferenceException("The Element cannot be clicked...\n");
	}

	//JS functions
	public static void jsCheckMessageContainer() throws Exception{
		int attempts = 0;
		String container = "dummy";
		String errMsg = "";
		
		JavascriptExecutor js = (JavascriptExecutor)driver;

		System.out.print("Inspecting for error dialogs");	
		while(attempts < 5 && (!container.isEmpty() || !container.contentEquals(""))){
			container = (String)js.executeScript(
			"var msgContainer = document.getElementById('d1::msgCtr');"+
			"if(msgContainer != null)"+
			" return msgContainer.innerHTML;"+
			"return ;"
			);
		
		attempts += 1;
		Thread.sleep(75);
		System.out.print(".");
		}
		//System.out.println("Last container value: '"+container+"'\nafter "+attempts+" tries.");
		String tempMsg = "";//driver.findElement(By.id("d1::msgDlg")).getText();
		if(tempMsg.contains("oracle.apps.flex.fnd.applcore")){
			TaskUtilities.jsFindThenClick("xpath", "//button[contains(@id,'msgDlg') and text()='OK']");
		}
		
		if((container.isEmpty() || container.contentEquals("")) && tempMsg.contains("Error") && !tempMsg.contains("oracle.apps.flex.fnd.applcore")){
			errMsg = driver.findElement(By.id("d1::msgDlg")).getText().replaceAll("OK", "").replace("Error", "");
			throw new DuplicateEntryException("Error FOUND: \n"+errMsg);

		}
		System.out.println("CLEARED.");
		
	}
	
	public static void jsCheckInputErrors() throws Exception{
		int attempts = 0;		
		String label = "";
		String knownErrPath, modifiedPath;
		knownErrPath = "//*[contains(@class,'Error')]";
		
		JavascriptExecutor js = (JavascriptExecutor)driver;

		System.out.print("Inspecting error on input");	
		while(attempts < 3 && (label.isEmpty() || label.contentEquals(""))){
			
			modifiedPath = knownErrPath+"//label";
			if(TaskUtilities.is_element_visible("xpath", knownErrPath+"//label")){
				
				label = (String)js.executeScript(
						"function getElementByXPath(xPath){"+
								"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
								"}"+
								"var label = getElementByXPath(\""+modifiedPath+"\");"+
								"if(label != null)"+		
								"{return label.textContent;}"+
								"else{return '';}"
						);
				//return label;
			}
		attempts += 1;
		System.out.print(".");
		}
		if(!label.isEmpty()){
			System.out.println("Caught Error label: "+label);
			throw new InputErrorException("Invalid input was found on: "+label.toUpperCase());
		}
		System.out.println("CLEARED");
	}
	
	public static void jsFindThenClick(String type, String locator) throws Exception{
		String text = "", tag = "", id = "";
		String by = null;
		
		if(type.contentEquals("id")) by = "getElementById";
		else if(type.contentEquals("xpath")) by = "getElementByXPath";
		else if(type.contentEquals("tagname")) by = "getElementsByTagName";
		else if(type.contentEquals("classname")) by = "getElementsByClassName";
		else if(type.contentEquals("cssselector")) throw new NoSuchAttributeException();
		else if(type.contentEquals("name")) throw new NoSuchAttributeException();
		else if(type.contentEquals("linktext")) throw new NoSuchAttributeException();
		else throw new NoSuchAttributeException();
		
		JavascriptExecutor js = (JavascriptExecutor)driver;
		js.executeScript(
			"function getElementByXPath(xPath){"+
					"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
					"}"+
			by+"(\""+locator+"\").click();"
		);
		
		By by2 = getLocator(type, locator);
		
		try{
				text = driver.findElement(by2).getText();
				tag = driver.findElement(by2).getTagName();
				id = driver.findElement(by2).getAttribute("id");
			}catch(Exception e){
				//Skips the process..
			}
		
		if(tag.contentEquals("a")){
				tag = "link";
				if(text.isEmpty() || text.contentEquals("")){//new addtion to link
					text = driver.findElement(by2).getAttribute("title");
				}
			} else if(tag.contentEquals("span")){
				tag = "button";
			} else if(tag.contentEquals("select")){
				text = "\b";
			} else if(tag.contentEquals("input")){
				try{
						text = driver.findElement(By.xpath("//label[contains(@for,'"+id+"')]")).getText();
					} catch(NoSuchElementException e){
						text = "checkbox";
					}
			}
		
		if(text.indexOf("\n") != -1){
			int enterIndex = text.indexOf("\n");
			text = text.substring(0, enterIndex);
		}
		
		System.out.println("adding tags done...");
		//log("Clicking "+text+" "+tag+"...");
		System.out.println("Clicking "+text+" "+tag+"...");
	}

	public static void jsScrollIntoView(String type, String value) throws Exception{
		//System.out.println("Adjusting view...");
		
		String jsLocator = getJsLocator(type, value);
		
		JavascriptExecutor js = (JavascriptExecutor)driver;
		js.executeScript(
			"function getElementByXPath(xPath){"+
					"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
					"}"+
			jsLocator+"(\""+value+"\").scrollIntoView(true);"
		);
		
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
		fluentWaitForElementInvisibility("//div[text()='Fetching Data...']", "Fetching Data...", 10);
	}

	public static boolean jsSideScroll(boolean isScrollingLeft){
		
		int scrollValue = 50;
		boolean scrollLeftAgain = true;
		String scrollerPath = "//div[contains(@id,'scroller')]";
		List<WebElement> scrollers = driver.findElements(By.xpath(scrollerPath));
		String sID;
		
		JavascriptExecutor js = (JavascriptExecutor)driver;
		for(WebElement scroller: scrollers){
			sID = scroller.getAttribute("id");
			
			scrollLeftAgain = (Boolean) js.executeScript(
				"scrollMove = document.getElementById(\""+sID+"\").scrollLeft;"+
				"var oldScrollValue = scrollMove;"+
				"var newScrollValue = 0;"+
				"if(scrollMove != null || scrollMove != undefined){"+
				"	if("+isScrollingLeft+"){"+
				"		scrollMove += "+scrollValue+";"+
				"		newScrollValue = scrollMove;"+
				"	} else if(!"+isScrollingLeft+"){ scrollMove = 0; }"+
				
				"	if(oldScrollValue == newScrollValue){"+
				"		return false;}"+
				"}"+
				"return true;"
			);
		}
		
		return scrollLeftAgain;
	}
	
	public static String jsGetInputValue(String type, String locator) throws Exception{
		String value ="";
		String jsLocator = getJsLocator(type, locator);
		
		JavascriptExecutor js = (JavascriptExecutor)driver;
		value = (String)js.executeScript(
				"function getElementByXPath(xPath){"+
				"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
				"}"+
				"var inputContainer = "+jsLocator+"(\""+locator+"\");"+
				"var inputValue = inputContainer.value;"+
				"return inputValue;"
				);
		return value;
	}
	
	//Input Box Utilities...
	public static boolean jsGetCheckboxTickStatus(String type, String value) throws NoSuchAttributeException{
		boolean isChecked = false;
		String jsLocator = getJsLocator(type, value);
		
		JavascriptExecutor js = (JavascriptExecutor)driver;
		isChecked = (Boolean)js.executeScript(
				"function getElementByXPath(xPath){"+
				"	return document.evaluate(xPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"+
				"}"+
				"var inputContainer = "+jsLocator+"(\""+value+"\");"+
				"var isChecked = inputContainer.checked;"+
				"return isChecked;"
				);
		System.out.println("Checkbox is currently checked: "+isChecked);
		return isChecked;
	}

	public static String manageNavigation(String navigationPath, int navPointer) throws Exception{
		int gtIndex, navLoc = -1;
		String targetNavPath = "";
		navigationPath = navigationPath .replaceAll("->", ">")
										.replaceAll(" >", ">")
										.replaceAll("> ", ">")
										.replaceAll("HCM", "Human Capital Management");
		String[] navArray = navigationPath.split(">");
		int gtCount = navArray.length + 1;
		//Skips the whole sequence...
		if(navPointer > gtCount)
			return "";
		
		gtIndex = navigationPath.indexOf(">");
		while(navLoc < navPointer){
			
			if(gtIndex != -1){
						targetNavPath = navigationPath.substring(0, gtIndex);
						navigationPath = navigationPath.substring(gtIndex+1);
					} else if(gtIndex == -1){
						targetNavPath = navigationPath;
					}	
			
			gtIndex = navigationPath.indexOf(">");
			navLoc += 1;
		}
		
		return targetNavPath;
	}

}
