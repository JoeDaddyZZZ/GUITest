package com.clarity.QA.GUITester;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Reporter;

public class CommandExecutor {
    WebDriver driver;
    String baseWindowHdl = null;
    Map<String,String> paramMap = new HashMap<>();
    
    public CommandExecutor(WebDriver driver) {
        this.driver = driver;
        baseWindowHdl = driver.getWindowHandle();
    }
    
    public void execute(List<String> params) throws Exception {
        String command = params.get(0);
        //System.out.println(" command " + command);
        String elementIdentifier = params.get(1);
        //System.out.println(" elementId " + elementIdentifier);
        String elementType = params.get(2);
        //System.out.println(" elementType " + elementType);
        String identifierType = params.get(3);
        //System.out.println(" identifierType " + identifierType);
        String parameter = params.get(4);
        //System.out.println(" parameter " + parameter);
        switch(command) {
            case "Access URL": Reporter.log("Acessing URL: '"+parameter+"'");
                              driver.get(parameter);
                              break;
            case "Click": Reporter.log("Clicking on element with "+identifierType+" = '"+elementIdentifier+"'");
                          driver.findElement(getBy(elementIdentifier,identifierType)).click();
                          driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
                          break;
            case "Write Text": Reporter.log("Writing text '"+parameter+"' to field with "+identifierType+" = '"+elementIdentifier+"'");
                              driver.findElement(getBy(elementIdentifier,identifierType)).sendKeys(parameter); 
                              break;
            case "Check URL": Reporter.log("Verifying Current URL = '"+parameter+"'");
                              assertEquals(driver.getCurrentUrl(),parameter); 
                              break; 
            case "Check Text": Reporter.log("Verifying element with "+identifierType+" = '"+elementIdentifier+"' has text = '"+parameter+"'");
                               assertEquals(driver.findElement(getBy(elementIdentifier,identifierType)).getText(),parameter); 
                               break;  
            case "Has Text": Reporter.log("Verifying element with "+identifierType+" = '"+elementIdentifier+"' has text = '"+parameter+"'");
                               assertTrue(driver.findElement(getBy(elementIdentifier,identifierType)).getText().contains(parameter)); 
                               break;  
            case "Get Text": Reporter.log("Saving text with "+identifierType+" = '"+elementIdentifier+"' to variable name = '"+parameter+"'");
                             paramMap.put(parameter, driver.findElement(getBy(elementIdentifier,identifierType)).getText());
                             break;
            case "Write Saved Text": String savedText = paramMap.get(parameter);
                                     Reporter.log("Writing text '"+savedText+"' to field with "+identifierType+" = '"+elementIdentifier+"'");
                                     driver.findElement(getBy(elementIdentifier,identifierType)).sendKeys(savedText);
                                     break;
            case "Check Saved Text": String savedText2 = paramMap.get(parameter);
                                     Reporter.log("Verifying element with "+identifierType+" = '"+elementIdentifier+"' has text = '"+savedText2+"'");
                                     assertEquals(driver.findElement(getBy(elementIdentifier,identifierType)).getText(),savedText2);
                                     break;
            case "Switch Window": Reporter.log("Switching windows");
                                  for (String winHandle : driver.getWindowHandles())
                                      driver.switchTo().window(winHandle);
                                  break;
            case "Close Window": Reporter.log("Closing current window");
                                 driver.close();
                                 driver.switchTo().window(baseWindowHdl);
                                 break;
            case "Print": Reporter.log("Printing element of type "+elementType+" with "+identifierType+" = '"+elementIdentifier);
                          printElement(elementIdentifier,elementType,identifierType);
                                 break;
            case "Compare Table": Reporter.log("Compare element of type "+elementType+" with "+identifierType+" = '"+elementIdentifier);
                          boolean isSame = compareElement(elementIdentifier,elementType,identifierType,parameter);
                          assertEquals(isSame, true);
        }
    }
    
    private boolean compareElement(String elementIdentifier, String elementType,
			String identifierType, String parameter) {
    	boolean isSame = true;
    	/*
    	 * collect table from database using parameter as sql
    	 */
    	DBSQL jobwalker = new DBSQL("qatest.clarityssi.local","jobwalker","qauser","ClAr1ty!");
    	System.out.println(parameter);
    	ResultSet rs = jobwalker.getDBRow(parameter);
    	ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			int colCount=rsmd.getColumnCount();
			int j = 1;
			for (WebElement row : driver.findElements(getBy(elementIdentifier+"> tr",identifierType))) {
				int i = 1;
				for (WebElement cell : row.findElements(getBy("td",identifierType))) {
					String colType = rsmd.getColumnTypeName(i);
					if(rs.isBeforeFirst()) rs.next();
					String colAnswer = getAnswer(colType,i,rs);
					if(!cell.getText().equalsIgnoreCase(colAnswer)) {
						Reporter.log("Line " + j + " Found |" + cell.getText() + "| Should Be |" + colAnswer + "| Type is " + colType);
						isSame=false;
					}
					if(i==colCount) break;
					i++;
				}
				j++;
				if(rs.isLast()) break;
				rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		jobwalker.closeCon();
		return isSame;
	}
   

    /***
     * extract sql data as type given in database and convert to string.
     * @param colType
     * @param i
     * @param rs
     * @return
     */
	private String getAnswer(String colType, int i, ResultSet rs) {
		String answer = "";
		try {
				if(colType.contains("INT")) {
					answer = String.valueOf(rs.getInt(i));
				} else if(colType.contains("TIMESTAMP")) {
					Date date = rs.getTimestamp(i);
					if(date!=null) {
					//if(date.after(new Date(10000))) {
						answer = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(date);
					} else {
						answer = " ";
					}
				} else {
					answer = rs.getString(i);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return answer;
	}

	private By getBy(String identifier, String identifierType) {
        if (identifierType.contains("table")) {
            identifierType = identifierType.substring(0,identifierType.lastIndexOf('-'));
            char cellRow = identifier.charAt(identifier.lastIndexOf('(')+1);
            char cellCol = identifier.charAt(identifier.lastIndexOf(')')-1);
            identifier = identifier.substring(0,identifier.lastIndexOf('(')) + " > tbody > tr:nth-child("+cellRow+") > td:nth-child("+cellCol+")";
            System.out.println(identifierType +", "+identifier);
        }
        switch(identifierType) {
            case "id": return By.id(identifier);
            case "name": return By.name(identifier);
            case "css": return By.cssSelector(identifier);
            case "tagname": return By.tagName(identifier);
            case "classname": return By.className(identifier);
            case "xpath": return By.xpath(identifier);  
            case "linktext": return By.linkText(identifier);
            default: return null;
        }
    }
    
    private void printElement(String elementIdentifier, String elementType, String identifierType) {
        switch (elementType) {
            case "Div":
                WebElement div = driver.findElement(getBy(elementIdentifier,identifierType));
                System.out.println(div.getText());
                break;
            case "List":
                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> li",identifierType)))
                    System.out.println(e.getText());
                break;
            case "Link":
//                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> a",identifierType)))
                for (WebElement e : driver.findElements(getBy(elementIdentifier,identifierType)))
                    System.out.println(e.getText());
                break;
            case "Table Header":
                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> th",identifierType)))
                    System.out.println(e.getText());
                break;
            case "Table Body":
                for (WebElement row : driver.findElements(getBy(elementIdentifier+"> tr",identifierType)))
                    for (WebElement cell : row.findElements(getBy("td",identifierType)))
                        System.out.println(cell.getText());
                break;
        }
    }
  
}
