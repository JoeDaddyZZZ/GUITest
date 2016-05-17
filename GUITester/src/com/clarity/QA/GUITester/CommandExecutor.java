package com.clarity.QA.GUITester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        }
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
