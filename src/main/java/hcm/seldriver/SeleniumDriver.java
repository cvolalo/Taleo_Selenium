package hcm.seldriver;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import hcm.common.ExtendedFirefoxDriver;
import hcm.common.ArgumentExecutor;
import hcm.common.ArgumentHandler;
import hcm.common.ArrayHandler;
import hcm.common.CustomRunnable;
import hcm.common.DuplicateEntryException;
import hcm.common.InputErrorHandler;
import hcm.common.TaskUtilities;
import hcm.utilities.ExcelReader;
import hcm.utilities.TextUtility;

public class SeleniumDriver {

	private static final long ELEMENT_APPEAR = 60L;
	private static final int MAX_TIMEOUT = 60;
	public static final int navRow = 1;
	public static final int defaultLabelRow = 2;
	public static int rowGroup = defaultLabelRow - 1;// 2 : Oracle 1 : Exelon
	public static final int defaultInputRow = defaultLabelRow + 1;
	public static final int defaulColNum = 0;

	private WebDriverWait wait = null;
	private TextUtility textReader = null;
	private ExcelReader excelReader = null;
	public static WebDriver driver;
	private static WebDriver augmentedDriver;
	private static Actions performer; 
	WebElement frame;
	private String caseName;
	private boolean windowSwitched = false;
	private String parentWindow;
	private String workspace_path;
	// Case-dependent variables
	private int afrrkInt = 0;
	// Dependency files
	private String configPath = "lib/customization_file.txt";
	private String screenShotPath = "target/screenshots/";

	public void initializeDriver(String hubURL, String browser,	String workspace, String excel) {
		try {
			workspace_path = workspace + "/";
			if(hubURL.contentEquals("local"))
			{
				if(browser.contentEquals("firefox"))
					driver = new ExtendedFirefoxDriver(getCapability(browser));
				else {
					System.setProperty("webdriver.chrome.driver", workspace + "/lib/chromedriver_win32/chromedriver.exe");
					driver = new ChromeDriver();
				}
			} else
				driver = new RemoteWebDriver(new URL(hubURL),getCapability(browser));

			// Set window size base on the remote server
			driver.manage().window().maximize();
			//driver.manage().window().setSize(new Dimension(1020, 737));
			System.out.println("Browser size: "	+ driver.manage().window().getSize());
			augmentedDriver = new Augmenter().augment(driver);
			
			parentWindow = driver.getWindowHandle();
			performer = new Actions(driver); 

			// Read text file
			textReader = new TextUtility();
			textReader.read(workspace_path + configPath);

			// Read Excel file
			excelReader = new ExcelReader();
			System.out.println(excel);
			excelReader.loadExcelFile(excel);

			wait = new WebDriverWait(driver, ELEMENT_APPEAR);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected DesiredCapabilities getCapability(String browser) {
		try {
			if (browser.contentEquals("firefox"))
				return DesiredCapabilities.firefox();
			else
				return DesiredCapabilities.firefox();

		} catch (Exception e) {

		}
		return null;
	}

	public String getLoginCredentials(String input) {
		excelReader.setActiveSheet("Configurations");
		int rowNum = 0;
		int colNum = 0;
		String data = null;

		if (input.contentEquals("URL")) {
			rowNum = 0;
			colNum = 1;
		} else if (input.contentEquals("USERID")) {
			rowNum = 1;
			colNum = 1;
		} else if (input.contentEquals("PASSWORD")) {
			rowNum = 2;
			colNum = 1;
		}
		data = excelReader.getCellData(rowNum, colNum);

		return data;
	}

	public void login(String siteURL, String username, String password) {
		driver.get(siteURL);
		System.out.println(siteURL);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[2]/input")));
		System.out.println("Loading URL..");
		// takeScreenShot(caseName);
		driver.findElement(By.xpath("//span[2]/input")).clear();
		System.out.println("Waiting for User Id...");
		driver.findElement(By.xpath("//span[2]/input")).sendKeys(username);
		System.out.println("User Id " + username + " entered.");

		driver.findElement(By.xpath("//span[3]/input")).clear();
		System.out.println("Waiting for Password...");
		driver.findElement(By.xpath("//span[3]/input")).sendKeys(password);
		System.out.println("Password ******* entered.");

		driver.findElement(By.xpath("//span[2]/a/span/span/span/span")).click();
		System.out.println("Logging in...");
	}

	public void gotoConfigurations() throws Exception{
		System.out.println("Loading page..");
		//TaskUtilities.customWaitForElementVisibility("xpath", "//img[@alt='Navigator]", 120);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='applicationNavItem']//span[text()='Configuration']"))).click();
		// driver.findElement(By.xpath("//img[@alt='Navigator']")).click();
		System.out.println("Navigating to Configurations");
		//TaskUtilities.customWaitForElementVisibility("xpath", "//a[text()='Setup and Maintenance']", 120);
		//wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Setup and Maintenance"))).click();
		// driver.findElement(By.linkText("Setup and Maintenance")).click();
	}

	public void searchTask(String key) throws Exception{
		// got to Setup and Maintenance
		// driver.get("https://fs-aufsn4x0cba.oracleoutsourcing.com/setup/faces/TaskListManagerTop?fnd=%3B%3B%3B%3Bfalse%3B256%3B%3B%3B&_adf.no-new-window-redirect=true&_adf.ctrl-state=s6qt8bhoo_5&_afrLoop=1042465641683653&_afrWindowMode=2&_afrWindowId=1zd8d2b3t");
		TaskUtilities.customWaitForElementVisibility("xpath", "//span/label[text()='Search']/../input", MAX_TIMEOUT);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span/label[text()='Search']/../input")));
		// Search
		driver.findElement(By.xpath("//span/label[text()='Search']/../input")).clear();
		driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(key);
		driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(Keys.ENTER);
		System.out.println("Searching for task " + key + ".");
		// takeScreenShot(caseName); 
	}

	public void goToTask(String key) {
		//wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='" + key+ "']/../../../td/a/img[@title='Go to Task']"))).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[text()='" + key+ "']"))).click();
		driver.findElement(By.xpath("//a[text()='" + key+ "']")).sendKeys(Keys.ENTER);;
		System.out.println("Go to task " + key + ".");
	}

	public void navigateToTask(String name) throws Exception {
		System.out.println("Task navigation started...");
		String divPath;
		int intLoc = 1;
		String currentLoc = "", searchData = "", labelLocator = "", labelLocatorPath = "";

		getSheetName(name);
		String fullNavPath = excelReader.getCellData(navRow, defaulColNum);
		System.out.println("Nav Path is..." + fullNavPath);
		String[] navStep = fullNavPath.split(" > ");

		// Span Manage Implementation Project -- > Implementation Projects
		// TaskUtilities.customWaitForElementVisibility("xpath", "//a[text()='"+ navStep[intLoc] + "']", MAX_TIMEOUT);
		// TaskUtilities.jsFindThenClick("xpath", "//a[text()='" + navStep[intLoc] + "']");
		TaskUtilities.customWaitForElementVisibility("xpath", "//span[text()='"+ navStep[intLoc] + "']/..", MAX_TIMEOUT);
		Thread.sleep(3500);
		TaskUtilities.jsFindThenClick("xpath", "//span[text()='"+ navStep[intLoc] + "']/..");
		TaskUtilities.customWaitForElementVisibility("xpath", "//h1[text()='"+ navStep[intLoc] + "']", MAX_TIMEOUT);
		// Setting project name...
		if (!name.contentEquals("Manage Implementation Project")) {
			excelReader.setActiveSheet("Create Implementation Project");
			String projectName = excelReader.getCellData(defaultInputRow,defaulColNum);
			System.out.println("Project Name is " + projectName);

			searchData = projectName;
			labelLocator = "Name";
			labelLocatorPath = TaskUtilities.retryingSearchfromDupInput(labelLocator, "");

			action(labelLocator, "textbox", "xpath", labelLocatorPath,searchData);
			TaskUtilities.jsFindThenClick("xpath", "//button[text()='Search']");
			Thread.sleep(3500);
			TaskUtilities.customWaitForElementVisibility("xpath","//a[text()='" + searchData + "']", MAX_TIMEOUT);
			TaskUtilities.jsFindThenClick("xpath", "//a[text()='" + searchData+ "']");

			TaskUtilities.customWaitForElementVisibility("xpath","//h1[contains(text(),'" + searchData + "')]", MAX_TIMEOUT);
		}

		intLoc += 1;
		TaskUtilities.scrollDownToElement(false, "");
		while (intLoc < navStep.length) {
			currentLoc = navStep[intLoc].replace("HCM","Human Capital Management");
			System.out.println("We are now at: " + currentLoc);
			divPath = "//div[text()='" + currentLoc + "']";

			TaskUtilities.customWaitForElementVisibility("xpath", divPath, MAX_TIMEOUT);
			TaskUtilities.jsScrollIntoView("xpath", divPath);

			if (TaskUtilities.is_element_visible("xpath", divPath+ "//a[@title='Expand']")) {
				// TaskUtilities.retryingFindClick(By.xpath(divPath+
				// "//a[@title='Expand']"));
				driver.findElement(By.xpath(divPath + "//a[@title='Expand']")).sendKeys(Keys.ENTER);
				TaskUtilities.customWaitForElementVisibility("xpath", divPath + "//a[@title='Collapse']", MAX_TIMEOUT);
			}

			if (TaskUtilities.is_element_visible("xpath", divPath + "/../..//a[@title='Go to Task']")) {
				TaskUtilities.jsFindThenClick("xpath", divPath);

				// Open for improvement...
				String href = driver.findElement(By.xpath(divPath + "/../..//a[@title='Go to Task']")).getAttribute("href");
				System.out.println("Obtained href is: " + href);
				String isRedirecting = getPropertiesRedirect(name);

				// if (href.contentEquals("#")) {
				if (isRedirecting.toLowerCase().contentEquals("false")) {
					TaskUtilities.jsFindThenClick("xpath", divPath
							+ "/../..//a[@title='Go to Task']");
					// } else if (href.contains("http")) {
				} else if (isRedirecting.toLowerCase().contentEquals("true")) {
					openAdminLink(href);
				}
			}

			intLoc += 1;
		}
	}

	public void openAdminLink(String href) throws Exception {

		WebElement body = driver.findElement(By.cssSelector("body"));
		// String newTabAction = Keys.chord(Keys.COMMAND, "t");
		String newTabAction = Keys.chord(Keys.CONTROL, "t");
		body.sendKeys(newTabAction);

		String chooseTab = Keys.chord(Keys.COMMAND, "2");// on my pc 3 others should be 2;
		// String switchTab = Keys.chord(Keys.CONTROL, Keys.TAB);
		body.sendKeys(chooseTab);

		driver.get(href);
	}

	public void search(String data, String locatorType, String locator) {
		driver.findElement(getLocator(locatorType, locator)).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Search Results')]/../../../../../../..//tbody/tr/td[text()='" + data + "']"))).click();
	}

	public void runServiceRequest(String sr) throws Exception {
		caseName = sr;

		// Detect the Action to perform
		if (getPropertiesAction(sr).contentEquals("Search Task")) {
			searchTask(sr);

		} else if (getPropertiesAction(sr).contentEquals("Manual")) {
			System.out.println("Proceeding with manual steps");
		} else if (getPropertiesAction(sr).contentEquals(null)) {
			System.out.print("No Action indicated in Properties. Please set the Action in the config_file.txt");
			System.out.print("Terminating transaction..");
			return;
		} else {
			System.out.print(" Action not available. Please set Search Task or Navigate only");
			System.out.print("Terminating transaction..");
			return;
		}

		runServiceRequestSearchTask(sr);
		// Screenshot
		takeScreenShot(sr);
	}

	public void runServiceRequestSearchTask(String sr) throws Exception {

		getSheetName(sr);
		int rowNum = 0;
		boolean hasRunGoToTask = false, hasPrepared = false, hasCheckedFields= false, hasArray = false;;

		if (getPropertiesAction(sr).contentEquals("Search Task"))
			rowNum = defaultInputRow;
		if (getPropertiesAction(sr).contentEquals("Navigate"))
			rowNum = defaultInputRow;
		if (getPropertiesAction(sr).contentEquals("Manual"))
			rowNum = defaultInputRow;
		// int pivotRow = rowNum;
		int pivotIndex = 1;
		System.out.println("First Excel Entry: " + excelReader.getCellData(rowNum, 0));
		
		readerloop: while (!excelReader.getCellData(rowNum, 0).isEmpty()) {

			if(getPropertiesSMReturnee(sr).toLowerCase().contentEquals("true")){
				gotoConfigurations();
				searchTask(sr);
			}
			if (getPropertiesRecursive(sr).toLowerCase().contentEquals("true")) {
				if (getPropertiesAction(sr).contentEquals("Search Task")) {
					goToTask(sr);
				} else if (getPropertiesAction(sr).contentEquals("Navigate")) {
					navigateToTask(sr);
					getSheetName(sr);
				}
			} else {
				if (getPropertiesAction(sr).contentEquals("Search Task") && !hasRunGoToTask) {
					goToTask(sr);
					hasRunGoToTask = true;
				} else if (getPropertiesAction(sr).contentEquals("Navigate") && !hasRunGoToTask) {
					navigateToTask(sr);
					getSheetName(sr);
					hasRunGoToTask = true;
				}
			}
			if (!hasPrepared) {
				runPrePrep(sr);
				hasPrepared = true;
			}
			takeScreenShot(caseName);

			runSteps(sr, "Pre-Steps", rowNum);

			Vector<String> fields = textReader.getCollection(sr, "Fields");
			Map<String, String> savedEntry = new HashMap<String, String>();
			Map<String, String> fieldVariables = new HashMap<String, String>();
			List<String> arrayDataHolder = new ArrayList<String>();
			List<Integer> arrayColHolder = new ArrayList<Integer>();
			
			String delimiter = "chr1$t1anX";
			
			int fieldSize = fields.size();
			int colNum = 0;
			// int rowGroup = defaultLabelRow-1;
			int iteration = 0;
			int checkpoint = 0;
			int rowInputs = 0, nextPivotIndex = 0;
			String arrayCol = null;
			int arrayRow = 0;
			System.out.println("Field size: " + fieldSize);
			System.out.println("Pivot Index: " + pivotIndex);
			
			//Array verifier...
			System.out.print("Verifying if fields has array");
			if(!hasCheckedFields){
				for(String statement : fields){
					if(statement.startsWith("array")){
						hasArray = true;
					}
					System.out.print(".");
				}
				hasCheckedFields = true;
			}
			System.out.print(""+hasArray);
			
			fieldloop: while (iteration < fieldSize) { // while (iteration != fieldSize) {
				System.out.println("Field Iteration is now currently at:"+ iteration);
				String current = fields.elementAt(iteration);
				String[] step = current.split(" \\| ");
				int sleepTime = 0;
				String caseId = "";
				boolean isFound = false, waitImmunity = false, isAnArrayAction = false;
				// boolean array = false;
				//Comment Parser
				if (step[0].startsWith("//")){
					iteration += 1;
					continue fieldloop;
				} 
				// Case Handler
				if (current.contains("esac")) {
					iteration += 1;
					continue fieldloop;
				}
				if (current.startsWith("case:")) {
					while (!current.contains("esac")) {
						iteration += 1;
						current = fields.elementAt(iteration);
						if ((iteration + 1) < fieldSize && fields.elementAt(iteration + 1).startsWith("case:")) {
							iteration += 1;
							current = fields.elementAt(iteration);
						}
					}
					iteration += 1;
					continue fieldloop;
				}
				// Case Handler

				//array handler
				if (step[0].contains("array") && !step[0].contains("col")) {
					arrayCol = step[1];
					arrayColHolder.add(Integer.parseInt(step[1]));
					colNum = Integer.parseInt(arrayCol);
					arrayRow = rowNum;
					if (step.length < 3)isAnArrayAction = true;
					else {//cellArray
						String data = excelReader.getCellData(rowNum, colNum);
						Map<String, String> arrayMap = ArrayHandler.getArrayArgument(step[2]);
						int increment = Integer.parseInt(arrayMap.get("increment"));
						delimiter = arrayMap.get("delimiter");
						
						String[] arrayData = data.split(delimiter);
						for(String datum: arrayData){
							arrayDataHolder.add(datum.trim());
						}
					}
					checkpoint = iteration + 1;
					iteration++;
					continue fieldloop;
				} else if (step[0].contains("stop") && !step[0].contains("col")) {
					int nextRow = rowNum + 1;
					
					//add
					if (arrayDataHolder.size() > 0){
						colNum = Integer.parseInt(arrayCol);
						iteration = checkpoint;
						System.out.println("Continuing single-data multi-input array..");
						continue fieldloop;
						
					} 
					if (excelReader.getCellData(nextRow, Integer.valueOf(arrayCol)).length()>0){
						if (excelReader.getCellData(rowNum, 0).contentEquals(excelReader.getCellData(nextRow, 0))) {
							rowNum++;
							colNum = Integer.valueOf(arrayCol);
							rowInputs += 1; //dynamic movement of row after the array..
							iteration = checkpoint;
							continue fieldloop;
						}
						if (excelReader.getCellData(nextRow, 0).length() > 0 && step[0].contains("non-identical")) {
							rowNum++;
							colNum = Integer.valueOf(arrayCol);
							rowInputs += 1; //dynamic movement of row after the array..
							iteration = checkpoint;
							continue fieldloop;
						}
					}

					//Refresh data...
					if(nextPivotIndex < rowInputs) nextPivotIndex = rowInputs;
					rowInputs = 0;
					isAnArrayAction = false;
					arrayColHolder.remove(arrayColHolder.size()-1);
					rowNum = arrayRow; //Reverts the row back..
					iteration++;
					continue fieldloop;
				}
				//array handler: Column version
				if (step[0].contains("colArray")) {
					//arrayRow = step[1];
					isAnArrayAction = true;
					checkpoint = iteration + 1;
					iteration++;
					continue fieldloop;
				} else if (step[0].contains("colStop")) {
					int nextCol = colNum+1;

					if ((excelReader.getCellData(rowNum, nextCol)).length()>0) {
						colNum++;
						//rowNum = Integer.valueOf(arrayRow);
						iteration = checkpoint;
						continue fieldloop;
					}

					isAnArrayAction = false;
					iteration += 1;
					continue fieldloop;
				}
				
				// Action Reader...
				if (step[0].contains("setExcelRow:")) {
					colNum = 0;
					Map<String, String> cellProp = ArgumentExecutor.executeSetExcelRow(current, excelReader, pivotIndex);
					rowNum = Integer.parseInt(cellProp.get("rowNum"));
					rowGroup = Integer.parseInt(cellProp.get("rowGroup"));
					iteration += 1;
					continue fieldloop;
				}
				if (step[0].contains("setExcelCol:")) {
					colNum = ArgumentExecutor.executeSetExcelColumn(current, excelReader, rowGroup);
					iteration += 1;
					continue fieldloop;
				}
				if (step[0].contains("takeScreenshot:")) {
					String sName = ArgumentExecutor.executeTakeScreenshot(step[0], caseName);
					takeScreenShot(sName);
					iteration += 1;
					continue fieldloop;
				}
				// Table Inputs...
				if (step[3].contains("$afrrkInt")) {
					System.out.print("Formerly " + step[3]);
					step[3] = step[3].replace("$afrrkInt", "" + afrrkInt);
					System.out.println(" is now: " + step[3]);
				}

				// Data conditions...
				String data = "";
				if (arrayDataHolder.size() > 0 && colNum == arrayColHolder.get(arrayColHolder.size()-1) && 
						!current.contains("button") && !current.contains("nullable") && !current.contains("drop")){//cellArray
					data = arrayDataHolder.get(0);
					System.out.println("Processing action data: "+data);
					current = current.replace("$.s"+excelReader.getCellData(rowGroup+1, colNum).trim(), data);//cellArray data correction...
					System.out.println("New current: "+current);
					arrayDataHolder.remove(0);
				} else if (step[0].toLowerCase().contains("time") && !step[0].toLowerCase().contains("zone")) {
					data = excelReader.getCellData(rowNum, colNum, "time");
				} else if (step.length > 4 && step[4].toLowerCase().contains("parse number")) {
					data = ArgumentExecutor.executeCellParser(step[4], excelReader, rowNum, colNum, data);
				} else {
					data = excelReader.getCellData(rowNum, colNum);
					if(arrayDataHolder.size() > 0 && (data.contains(",") || data.contains("\n"))){//cellArrayif
						List<String> dataList = ArgumentExecutor.getDataSet(data);
						while(dataList.size() > arrayDataHolder.size()) dataList.remove(0);
						data = dataList.get(0);
					}
				}

				if (step[0].toUpperCase().contains("WAIT TO APPEAR")) {
					System.out.println("Waiting for " + step[1]+ " to appear...");
					wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(step[2],step[3])));
					colNum--;
				} else if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
					System.out.println("Waiting for " + step[1]+ " to dissappear...");
					wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
					colNum--;
				} else {

					if ((data.isEmpty() || data.contains("blank"))&& !step[1].contains("button") 
							&& !step[1].contains("skippable") && !current.contains("trigger:") && !current.contains("switch") && !current.contains("drop")) {
						System.out.print(step[0] + " is empty.\n");
					} else {
						current = ArgumentHandler.executeArgumentConverter(current, excelReader, rowNum, rowGroup, colNum, arrayDataHolder.size());
						step = current.split(" \\| ");

						try {

							if (step.length > 4) {// Fourth step handler...

								Map<String, String> retrievingMap = ArgumentHandler.executeFourthArgument(current,excelReader, rowNum, rowGroup,colNum, data);
								sleepTime = Integer.parseInt(retrievingMap.get("sleepTime"));
								waitImmunity = Boolean.parseBoolean(retrievingMap.get("waitImmunity"));
								data = retrievingMap.get("data");
								isFound = Boolean.parseBoolean(retrievingMap.get("isFound"));
								caseId = retrievingMap.get("caseId");
								iteration += Integer.parseInt(retrievingMap.get("iteration"));
								colNum = Integer.parseInt(retrievingMap.get("colNum"));
								// Case Handler...
								if (!caseId.isEmpty() && !caseId.contentEquals("")) {
									System.out.println("Case Scenario: "+ caseId);
									
									String caseHolder = ArgumentExecutor.getCaseStatement(fields, caseId); 

									if (!step[1].contains("button"))
										colNum += 1;
									if (!waitImmunity) action(step[0], step[1], step[2],step[3], data);
									while (!current.contentEquals(caseHolder)) {
										iteration += 1;
										current = fields.elementAt(iteration);
									}
									iteration += 1;
									continue fieldloop;
								}
								if (waitImmunity && step[4].contains(":") && step[4].contains("wait")) {// Recent change...
									iteration += 1;
									continue fieldloop;
								}
								if (!retrievingMap.get("savedEntry").isEmpty()) {
									String[] entry = retrievingMap.get("savedEntry").split(",");
									savedEntry.put(entry[0], entry[1]);
								}
								if (retrievingMap.get("endingArg").contains("continue")) {
									if (retrievingMap.get("endingArg").contains("fieldloop")) {
										continue fieldloop;
									}
								}
							}

							if (isFound) {
								throw new TimeoutException();
							} else {
								//TaskUtilities.jsCheckMessageContainer();
							}

							Thread.sleep(sleepTime);
							action(step[0], step[1], step[2], step[3], data);
						} catch (UnhandledAlertException ue) {
							driver.findElement(By.cssSelector("body")).sendKeys(Keys.ENTER);
							iteration = 0;
							colNum = 0;
							continue fieldloop;
						} catch (Exception e) {
							e.printStackTrace();
							takeScreenShot(caseName);
							// String errMsg = ""+e;
							String errKey = InputErrorHandler.identifyInputErrors(textReader,excelReader, sr, rowNum);
							String caseType = ":Undo-Steps";
							fieldVariables.put("isAnArrayAction", ""+isAnArrayAction);
							fieldVariables.put("sr", sr);
							fieldVariables.put("caseType", caseType);
							fieldVariables.put("rowNum", ""+rowNum);
							System.out.println("Input has errors on: " + errKey);

							if (step.length > 4) {
								if (step[4].contains("throws")) {
									if (("" + e).contains("\"throws")) {
										String[] throwSet = ("" + e)
												.split("\"");
										for (String throwCase : throwSet) {
											if (throwCase.contains("throws ")) {
												step[4] = throwCase;
											}
										}
									}
									String caseItem = ArgumentExecutor.executeThrower(step[4]);
									Map<String, String> exceptionVariables = runUndoCaseSteps(fieldVariables, caseItem, savedEntry.get(caseItem), rowNum);
									boolean isResuming = Boolean.parseBoolean(exceptionVariables.get("isResuming"));
									nextPivotIndex = 0;
									if (!isResuming) {
										rowNum += 1 + nextPivotIndex;
										pivotIndex += 1 + nextPivotIndex;
										continue readerloop;
									} else {
										colNum += 1;
										iteration += 1;
										continue fieldloop;
									}
								}
							}

							for (String key : savedEntry.keySet()) {
								String newKey = key.toLowerCase().replaceAll("\\*", "");
								System.out.println("newKey is now " + newKey);
								// if((""+e).toLowerCase().contains(newKey)){
								if (errKey.toLowerCase().contentEquals(newKey)) {
									System.out.println("Exception Found.. running case: "+ key);
									Map<String, String> exceptionVariables = runUndoCaseSteps(fieldVariables, key, savedEntry.get(key), rowNum);
									boolean isResuming = Boolean.parseBoolean(exceptionVariables.get("isResuming"));
									nextPivotIndex = 0;
									if (!isResuming) {
										rowNum += 1 + nextPivotIndex;
										pivotIndex += 1 + nextPivotIndex;
										continue readerloop;
									} else {
										colNum += 1;
										iteration += 1;
										continue fieldloop;
									}
									// break sentryloop;
								}
							}

							System.out.println("No available error handler found: Will now enforce default Undo-Steps...");
							takeScreenShot(caseName);
							runUndoSteps(sr);
							if(isAnArrayAction || hasArray){
								while(excelReader.getCellData(rowNum, 0).contentEquals(excelReader.getCellData(rowNum+1, 0))){
									rowNum += 1;
								}
								nextPivotIndex = 0;
							}
							rowNum += 1 + nextPivotIndex;
							pivotIndex += 1 + nextPivotIndex;
							continue readerloop;
						}

						// action(step[0].trim(),step[1].trim(),step[2].trim(),step[3].trim(),data.trim());
					}

				}

				if (step[1].contains("button") || step[1].contains("nullable") || step[1].contains("switch") || step[1].contains("drop"))
					colNum--;

				colNum++;
				iteration++;
				System.out.println("Final iteration line has been reached...");
			}

			takeScreenShot(caseName);
			try {
				runSteps(sr, "Post-Steps", rowNum);
				//Additions...
				if(excelReader.getCellData(rowNum+1, 0).isEmpty()) runSteps(sr, "Penultimate-Steps", rowNum); 

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR HAS BEEN DETECTED...");
				takeScreenShot(caseName);
				runUndoSteps(sr);
				if(hasArray){
					while(excelReader.getCellData(rowNum, 0).contentEquals(excelReader.getCellData(rowNum+1, 0))){
						rowNum += 1;
					}
					nextPivotIndex = 0;
				}
			}

			rowNum += 1 + nextPivotIndex;
			pivotIndex += 1 + nextPivotIndex;
			nextPivotIndex = 0;
		}
	}

	// Get the Excel Sheet Name in the config_file.txt
	private void getSheetName(String name) {
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();

		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Sheetname")) {
				String[] sheetname = current.split(" \\| ");
				excelReader.setActiveSheet(sheetname[1]);
				break;
			}
		}
	}

	private String getPropertiesAction(String name) {
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String action = null;
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Action") && !current.contains("Sheetname")) {
				String[] Action = current.split(" \\| ");
				action = Action[1];
				break;	
			}
		}
		return action;
	}

	private String getPropertiesRecursive(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String recursive = "";
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Recursive")) {
				String[] Recursive = current.split(" \\| ");
				recursive = Recursive[1];
				break;
			}
		}
		if (recursive.isEmpty())
			recursive = "false";
		return recursive;
	}

	private String getPropertiesRedirect(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String redirect = null;
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Redirect-Enabled")) {
				String[] Redirect = current.split(" \\| ");
				redirect = Redirect[1];
				break;
			}
		}
		if (redirect.isEmpty())
			redirect = "false";
		return redirect;
	}
	
	private String getPropertiesSMReturnee(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String smr = "";
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("SetupAndMaintenanceReturnee")) {
				String[] SMR = current.split(" \\| ");
				smr = SMR[1];
				break;
			}
		}
		if (smr.isEmpty())
			smr = "false";
		return smr;
	}

	// Runs the Pre-Steps or Post-Steps set in the config_file.text
	private void runSteps(String name, String steps, int curRowNum)
			throws Exception {
		Vector<String> Steps = textReader.getCollection(name, steps);
		Enumeration<String> elements = Steps.elements();
		int STEP_TIMEOUT = MAX_TIMEOUT;
		boolean hasElements = false;
		System.out.println("Executing " + steps + ".");

		stepsloop: while (elements.hasMoreElements()) {

			String current = elements.nextElement();
			String[] step = current.split(" \\| ");
			int colNum = 0;
			hasElements = true;
			
			if (current.isEmpty()){
				System.out.println("Skipping empty "+steps+" ...");
				break;
			}
			if (current.contains("skip:")) {
				System.out.println("Skipping "+steps+" ..."); 
				break stepsloop;
			}

			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current, afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue stepsloop;
			}

			current = ArgumentHandler.executeArgumentConverter(current, excelReader, curRowNum, rowGroup, colNum, 0);
			step = current.split(" \\| ");
			// Third step checker..
			if (step[3].contains("$afrrkInt")) {
				step[3] = step[3].replace("$afrrkInt", "" + afrrkInt);
			}
			// Waiting variations...
			if (step[0].equalsIgnoreCase("row")) {
				TaskUtilities.customWaitForElementVisibility(step[2], step[3],15, new CustomRunnable() {

							public void customRun() throws Exception {
								// TODO Auto-generated method stub
								TaskUtilities.scrollDownToElement(false, "");
							}
						});
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1] + " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue stepsloop;
			}

			if (step.length > 4) {
				if (step[4].contains("wait")) {
					Map<String, String> waitParams = ArgumentExecutor.executeWait(current);
					if (Integer.parseInt(waitParams.get("waitTime")) / 1000 > STEP_TIMEOUT) {
						STEP_TIMEOUT = Integer.parseInt(waitParams.get("waitTime")) / 1000;
					}
				}
			}
			TaskUtilities.customWaitForElementVisibility(step[2], step[3], STEP_TIMEOUT, new CustomRunnable() {

						public void customRun() throws Exception {
							// TODO Auto-generated method stub
							//TaskUtilities.jsCheckMessageContainer();
							TaskUtilities.jsCheckInputErrors();
						}
					});

			WebElement we = getElement(step[2], step[3]);
			TaskUtilities.jsScrollIntoView(step[2], step[3]);

			// Wait for element to be present
			// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(step[2],step[3])));

			/*
			 * //Auto scroll to element Coordinates coordinate =
			 * ((Locatable)getLocator(step[2],step[3])).getCoordinates();
			 * coordinate.onPage(); coordinate.inViewPort();
			 */

			if (step[1].contains("js")) {
				TaskUtilities.jsFindThenClick(step[2], step[3]);
			} else {
				TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			}
			if (step[1].contains("enter")) {
				we.sendKeys(Keys.ENTER);
			}
			// Wait for element to be clickable
			// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3])));

			// Click Element
			// WebElement elmnt =
			// driver.findElement(getLocator(step[2],step[3]));
			// JavascriptExecutor executor = (JavascriptExecutor)driver;
			// executor.executeScript("arguments[0].click();", elmnt);

			// Display console
			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);

			// Take screenshot
			takeScreenShot(caseName);
		}
		if (!hasElements){
			System.out.println(steps+" doesn't contains any steps.");
		}
	}

	// Runs Pre-prep steps in the config_file.text
	private void runPrePrep(String name) throws Exception {
		System.out.print("Preparation steps in progress...");
		Vector<String> prePeps = textReader.getCollection(name, "Pre-Prep");
		Enumeration<String> elements = prePeps.elements();

		preploop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			String[] step = current.split(" \\| ");
			if (current.isEmpty())
				break;

			System.out.println("current pre-pep is: " + current);
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue preploop;
			} else if (current.contains("skip:")) {
				break preploop;
			}
			// First step checker...
			if (step[0].equalsIgnoreCase("table")) {
				String dummy = step[3].substring(0, step[3].indexOf("//tr"))+ "/..";
				// step[3] = step[3].substring(0,
				// step[3].indexOf("//tr"))+"/..";
				System.out.println("locator path is now: " + step[3]);
				// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3])));
				TaskUtilities.customWaitForElementVisibility(step[2], dummy,MAX_TIMEOUT);
				TaskUtilities.retryingFindClick(getLocator(step[2], dummy));
				afrrkInt = TaskUtilities.surveyCurrentTableInputs(step[3]);
			}

			if (!step[0].contentEquals("table")) {
				TaskUtilities.customWaitForElementVisibility(step[2], step[3], MAX_TIMEOUT);
				// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3]))).click();
				if(step[1].contains("js")){
					TaskUtilities.jsFindThenClick(step[2], step[3]);
				} else{
					TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
				}
				if(step[1].contains("enter")){
					driver.findElement(getLocator(step[2], step[3]));
				}
			}
		}
		System.out.print("Preparation steps has been done...");
	}

	// Determine the appropriate actions based on the type of element set in the config_file.txt
	public void action(String name, String type, String locatorType, String locator, String data) throws Exception {
		System.out.println("Performing actions...");
		int TIME_OUT = MAX_TIMEOUT;
		WebElement element = null;
		int attempts = 0;

		if (type.contentEquals("skippable")) {
			if (data.isEmpty() || data.contains("blank")) {
				return;
			} else {
				// Skips
			}
		}
		actionloop: while (attempts < 3) {
			try {

				if (name.contentEquals("Cancel"))
					TIME_OUT = 10;
				
				if(!type.contains("window")){
					System.out.println("Waiting for element to be found...");
						TaskUtilities.customWaitForElementVisibility(locatorType,locator, TIME_OUT, new CustomRunnable() {
	
								public void customRun() throws Exception {
									// TODO Auto-generated method stub
									//TaskUtilities.jsCheckMessageContainer();
									//TaskUtilities.jsCheckInputErrors();
								}
							});
					TaskUtilities.jsScrollIntoView(locatorType, locator);
					element = getElement(locatorType, locator);
				}
				// Coordinates coordinate =
				// ((Locatable)element).getCoordinates();
				// coordinate.onPage();
				// coordinate.inViewPort();

				if (type.contains("textbox") || type.contains("dropdown")) {
					// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorType,locator)));

					element.click();
					element.clear();
					element.click();
					System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = " + locator);
					if (data.toLowerCase().contains("yes,")) {
						data = data.substring(data.indexOf(",") + 1);
						data = data.trim();
					}else if(data.contains("blank")){
						data = "";
					}
					element.sendKeys(data);
					System.out.println(name + " = " + data);
					if (type.contentEquals("dropdown")) element.sendKeys(Keys.ENTER);
					element.sendKeys(Keys.TAB);

				} else if (type.contains("select")) {
					// wait.until(ExpectedConditions.elementToBeClickable(getLocator(locatorType,locator)));
					Select select = new Select(element);
					
					if (type.contentEquals("select")) {
						select.selectByVisibleText(data);
						System.out.println(data + " is selected.");
						element.sendKeys(Keys.ENTER);
					}else if (type.contains("multiple")){
						select.deselectAll();
						String multipleSel[] = data.split(",");
						for (String valueToBeSelected : multipleSel) {
							select.selectByVisibleText(valueToBeSelected);
							System.out.println(valueToBeSelected + " is selected.");
							element.sendKeys(Keys.CONTROL);
						   }
					}else if(type.contains("index")){
						String value[] = data.split("-");
						int index = Integer.parseInt(value[0]);
						if(value[1].equals(select.getFirstSelectedOption().getText()));
						else select.selectByIndex(index);
						element.sendKeys(Keys.TAB);
					}else {
						int curOps = 0;
						WebElement option = getElement(locatorType, locator + "/option[text()='" + data + "']");
						int opsValue = Integer.parseInt(option.getAttribute("value"));
						element.sendKeys(Keys.PAGE_UP);
						while (curOps < opsValue) {
							element.sendKeys(Keys.ARROW_DOWN);
							curOps += 1;
						}
						element.sendKeys(Keys.TAB);
					}
					System.out.println(name + " = " + data);
					Thread.sleep(1000);

				} else if (type.contentEquals("button")) {
					// wait.until(ExpectedConditions.elementToBeClickable(getLocator(locatorType,locator)));
					if (type.contains("js")) {
						TaskUtilities.jsFindThenClick(locatorType, locator);
					} else {
						element.click();
					}
					System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = " + locator);
					Thread.sleep(1000);

				} else if (type.contentEquals("radio")) {
					element.click();

				} else if (type.contains("switch")) {
					if (type.contains("default")) {
						driver.switchTo().defaultContent();
						System.out.println("Frame switch to default.");
					} else if (type.contains("child window")) {
						// Switch to new window opened
						int loopCount = 0;
						System.out.println("Switching to child window...");
						while (driver.getWindowHandles().size() == 1){
							//waiting for the new window
							 System.out.println("Waiting for the new window...");
							 Thread.sleep(850);
							 if (loopCount >= 10) break;
							 loopCount ++;
						}
						for(String winHandle : driver.getWindowHandles()){
							if(!winHandle.equals(parentWindow)){
							driver.switchTo().window(winHandle);
						    windowSwitched = true;
						    System.out.println("Switched to child window.");
						    break;
							}
						}
					} else if (type.contains("parent window")){
						System.out.println("Switching to parent window...");
						try{
							driver.getWindowHandles(); //test if other window still opens
						if (windowSwitched = true) driver.close();
						} catch (NoSuchWindowException e){
							System.out.println("Child window is already closed.");
							//child window is closed
						}
						driver.switchTo().window(parentWindow);
						System.out.println("Switched to parent window");
						windowSwitched = false;
					} else{
					
					frame = driver.findElement(getLocator(locatorType,locator));
					driver.switchTo().frame(frame);
					System.out.println("Frame switched to " + locator);
					}
					
				} else if (type.contentEquals("checkbox")) {
					// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorType,locator)));
					boolean isChecked = TaskUtilities.jsGetCheckboxTickStatus(locatorType, locator);
				
					if ((data.toUpperCase().contentEquals("FALSE") || data.toUpperCase().contains("NO")) && isChecked) {
						element.click();
						System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = "+ locator);
						System.out.println(name + " = " + data);
					}
					else if ((data.toUpperCase().contentEquals("TRUE") || data.toUpperCase().contains("YES") || !data.isEmpty()) && !isChecked) {
						if(!data.isEmpty() && (data.toUpperCase().contentEquals("FALSE") || data.toUpperCase().contentEquals("NO"))){
							//Skips since data is No or false...
						}else{
							element.click();
							System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = "+ locator);
							System.out.println(name + " = " + data);
						}
					}
					
				} else if (type.contentEquals("combobox")) {
					List<String> actions = ArgumentExecutor.parseCombobox(name,locator, data);
					for (String act : actions) {
						System.out.println("Will now process..." + act);
						String[] step = act.split(" \\| ");
						action(step[0], step[1], step[2], step[3], data);
					}
				} else if (type.contentEquals("nullable")) {
					// Skips for now...
				} else if(type.contentEquals("drag")){
					performer.clickAndHold(element);
					System.out.println(name + " " + type + " "+ " is now dragged using " + locatorType + " = "+ locator);
					System.out.println(name + " = " + data);
				} else if (type.contentEquals("drop")){
					try{
						performer.moveToElement(element).release().perform();
					}catch(WebDriverException e){
						performer.perform();
					}
					System.out.println("Dragged element is now dropped to " + name + " using " + locatorType + " = "+ locator); 
				}
				if (type.contains("enter")) {
					element.sendKeys(Keys.ENTER);
				}

				break actionloop;
			} catch (StaleElementReferenceException e) {
				e.printStackTrace();
				attempts += 1;
				if (attempts == 3)
					throw e;
			} catch (TimeoutException te) {
				if (attempts > 0) {
					wait = new WebDriverWait(driver, ELEMENT_APPEAR);
					throw te;
				}
				attempts += 1;
				wait = new WebDriverWait(driver, ELEMENT_APPEAR / 2);
			} catch (UnhandledAlertException ue) {
				throw new UnhandledAlertException("Unexpected modal dialog appeared. Reverting steps.");
			} catch (WebDriverException we) {
				we.printStackTrace();
				TaskUtilities.jsCheckMessageContainer();
				TaskUtilities.jsCheckInputErrors();
				attempts += 1;
				if (attempts == 3)
					throw we;
			}
		}
	}

	// Runs the Undo-steps set in the config_file.text
	private void runUndoSteps(String name) throws Exception {
		Vector<String> undoSteps = textReader.getCollection(name, "Undo-Steps");
		Enumeration<String> elements = undoSteps.elements();

		unsloop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			String[] step = current.split(" \\| ");

			if (current.isEmpty() || current.contains("case:"))
				break;
			// Execute entry...
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue unsloop;
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1]	+ " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue unsloop;
			}

			TaskUtilities.customWaitForElementVisibility(step[2], step[3],MAX_TIMEOUT);
			
			if (step[1].contentEquals("clear")) {
				driver.findElement(getLocator(step[2], step[3])).clear();
			}
			
			TaskUtilities.jsScrollIntoView(step[2], step[3]);
			if(step[1].contains("retry")){
				TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			}else{
				TaskUtilities.jsFindThenClick(step[2], step[3]);
			}

			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);
		}

		System.out.println("Fallback steps has been executed...");
	}

	// Supports Fallback methods for Exception Cases
	private Map<String, String> runUndoCaseSteps(Map<String,String> fieldVariables, String caseItem, String data, int curRowNum) throws Exception {
		int colNum = 0;
		boolean isResuming = false;
		String name = fieldVariables.get("sr");
		String caseType = fieldVariables.get("caseType");
		boolean isAnArrayAction = Boolean.parseBoolean(fieldVariables.get("isAnArrayAction"));
		Map<String, String> exceptionArray = new HashMap<String,String>();
		System.out.print("Setting Case elements");
		Vector<String> undoCaseSteps = textReader.getCaseCollection(name, caseItem, caseType);
		Enumeration<String> elements = undoCaseSteps.elements();
		System.out.println(" ...DONE.");
		exceptionArray.put("rowNum", ""+curRowNum);
		
		uncaseloop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			System.out.println("Starting the loop for: " + current);
			// current = current.replace("\t", "");
			String[] step = current.split(" \\| ");
			
			if(isAnArrayAction){
				if(excelReader.getCellData(curRowNum, 0).contentEquals(excelReader.getCellData(curRowNum+1, 0))){
					curRowNum += 1;
				}
				exceptionArray.put("rowNum", ""+curRowNum);
			}

			if (current.isEmpty()){
				exceptionArray.put("isResuming", ""+true);
				return exceptionArray;
				//return true;
			}
			// Decision here...
			if (step[0].contentEquals("resume esac")) {
				System.out.println("Case steps has been executed...");
				exceptionArray.put("isResuming", ""+true);
				return exceptionArray;
				//return true;
			} else if (step[0].contentEquals("end esac")) {
				System.out.println("Case steps has been executed...");
				exceptionArray.put("isResuming", ""+false);
				return exceptionArray;
				//return false;
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1] + " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue uncaseloop;
			}

			// Validation actions...
			current = ArgumentHandler.executeArgumentConverter(current, excelReader, curRowNum, rowGroup, colNum, 0);
			step = current.split(" \\| ");

			if (step.length > 4) {
				if (step[4].contains("encode")) {
					String[] read = step[4].split(":");
					data = read[1];
				}
			}
			// pseudo perform actions...
			if (step[1].contentEquals("textbox")) {
				action(step[0], step[1], step[2], step[3], data);
			} else if (step[1].contentEquals("clear")) {
				driver.findElement(getLocator(step[2], step[3])).clear();
			}

			// Execute entry...
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue uncaseloop;
			}

			TaskUtilities.customWaitForElementVisibility(step[2], step[3], MAX_TIMEOUT);
			TaskUtilities.jsScrollIntoView(step[2], step[3]);

			TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3]))).click();

			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);
		}

		exceptionArray.put("isResuming", ""+false);
		return exceptionArray;
		//return true;
	}

	public WebElement getElement(String type, String value) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(type, value)));
	}

	public void clearAndType(WebElement element, String data) {
		element.click();
		element.clear();
		element.click();
		element.sendKeys(data.trim());
		element.sendKeys(Keys.TAB);

	}

	public By getLocator(String type, String value) {
		if (type.contentEquals("id"))
			return By.id(value);
		else if (type.contentEquals("xpath"))
			return By.xpath(value);
		else if (type.contentEquals("tagname"))
			return By.xpath(value);
		else if (type.contentEquals("classname"))
			return By.className(value);
		else if (type.contentEquals("cssselector"))
			return By.cssSelector(value);
		else if (type.contentEquals("name"))
			return By.name(value);
		else if (type.contentEquals("linktext"))
			return By.linkText(value);
		else
			return By.partialLinkText(value);
	}

	// Screenshot
	public void takeScreenShot(String caseName) {
		String datePrefix = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		String path = workspace_path + screenShotPath+ caseName.replace(" ", "_") + "";
		String ssPath = workspace_path + screenShotPath;
		try {
			File ssDir = new File(ssPath);
			if (!ssDir.exists())
				ssDir.mkdir();
			File dir = new File(path);
			if (!dir.exists()) {
				System.out.println("The location " + path + " does not exist.");
				dir.mkdir();
				System.out.println("A directory " + path + " is created.");
			}

			byte[] screenshot;

			screenshot = ((org.openqa.selenium.TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);

			File screenshotFile = new File(MessageFormat.format("{0}/{1}-{2}",path, datePrefix, caseName.replace(" ", "_") + ".png"));

			FileOutputStream outputStream = new FileOutputStream(screenshotFile);
			try {
				outputStream.write(screenshot);
				System.out.println("Screen shot "+ screenshotFile.toString().substring(path.length() + 1) + " saved in "+ path);
			} finally {
				outputStream.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		driver.close();
		driver.quit();
	}

}
