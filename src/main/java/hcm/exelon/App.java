package hcm.exelon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.TakesScreenshot;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;
import hcm.utilities.TextUtility;


/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws Exception{
    	
    	Options options = new Options();
    	options.addOption("r", true, "service request");
    	options.addOption("w", true, "current workspace");
    	options.addOption("e", true, "excelfile with path");
    	options.addOption("h", true, "selenium hub");
    	options.addOption("b", true, "browser");
    	String dataObject = null;
    	String workspace = null;
		String excel = null;
		String hub = null;
		String browser = null;
    	
    	CommandLineParser parser = new DefaultParser();
    	try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("r")) dataObject = cmd.getOptionValue("r");
			if(cmd.hasOption("w")) workspace = cmd.getOptionValue("w");
			if(cmd.hasOption("e")) excel = cmd.getOptionValue("e");
			if(cmd.hasOption("h")) hub = cmd.getOptionValue("h");
			if(cmd.hasOption("b")) browser = cmd.getOptionValue("b");
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	    	
    	SeleniumDriver sel = new SeleniumDriver();
    	
    	System.out.println("Initializing drivers...");
    	sel.initializeDriver(hub, browser, workspace, excel);
		System.out.println("Running Test in " + browser);
		
    	System.out.println("Drivers Initialized.");
    	
    	sel.login(sel.getLoginCredentials("URL"), sel.getLoginCredentials("USERID"), sel.getLoginCredentials("PASSWORD"));
    	
    	//sel.gotoConfigurations();
    	    	  	
    	try{
	    		sel.runServiceRequest(dataObject);    	
	    		Thread.sleep(10000);
	        	sel.dispose();
	        	System.out.println("Service Request has been managed successfully.");
	    	} catch(Exception e){
	    		e.printStackTrace();
	    		sel.takeScreenShot("App");
	    		sel.dispose();
	    		System.out.println("Error Encountered. Aborted Service Request.");
	    	}
    }
}