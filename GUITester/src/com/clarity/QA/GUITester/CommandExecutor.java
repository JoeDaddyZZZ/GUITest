package com.clarity.QA.GUITester;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Reporter;

import com.clarity.QA.API.AVPairSet;
import com.clarityservicelayer.portal.AvPair;

public class CommandExecutor {
    WebDriver driver;
    String baseWindowHdl = null;
    Map<String,String> paramMap = new HashMap<>();
    String DBHost= "qatest.clarityssi.local";
    String DBSchema= "fmi2";
    String DBUser= "qauser";
//    String DBPassword= "ClAr1ty!";
    String DBPassword= "";
    
    HashMap<String,String> varsToUse = new HashMap<String,String>();
    
    public CommandExecutor(WebDriver driver) {
        this.driver = driver;
        System.out.println(driver.getTitle());
		baseWindowHdl = driver.getWindowHandle();
		varsToUse.put("XXXYYYZZZ", "");
    }
    public CommandExecutor(WebDriver driver, 
    		String DBHost, String DBSchema, String DBUser, String DBPassword) {
        this.driver = driver;
        this.DBHost = DBHost;
        this.DBSchema = DBSchema;
        this.DBUser = DBUser;
        this.DBPassword = DBPassword;
        System.out.println(driver.getTitle());
		baseWindowHdl = driver.getWindowHandle();
		varsToUse.put("XXXYYYZZZ", "");
    }
    
    public void execute(List<String> params) throws Exception {
    	Thread.sleep(3);
		varsToUse.put("XXXYYYZZZ", "");
        String command = params.get(0);
        //System.out.println(" command " + command);
        String elementIdentifier = (params.get(1) != null) ? params.get(1) : " ";
        //System.out.println(" elementId " + elementIdentifier);
 //       String elementType = params.get(2);
        String elementType = (params.get(2) != null) ? params.get(2) : " ";
        //System.out.println(" elementType " + elementType);
        String identifierType = (params.get(3) != null) ? params.get(3) : " ";
        //System.out.println(" identifierType " + identifierType);
        String parameter = (params.get(4) != null) ? params.get(4) : " ";
        //System.out.println(" parameter " + parameter);
        /*
         * set question/answer data
         */
       	AVPairSet avSet = new AVPairSet();
//       	avSet.loadQuestions(DBHost,DBSchema,DBUser,DBPassword);
//      	avSet.loadChoices(DBHost,DBSchema,DBUser,DBPassword);
       	avSet.loadQuestions("qatest.clarityssi.local","fmi2",DBUser,DBPassword);
       	avSet.loadChoices("qatest.clarityssi.local","fmi2",DBUser,DBPassword);
        /*
         * loop through collected variables stored and replace
         */
        for (String varKey : varsToUse.keySet()) {
        	if(parameter.contains(varKey)) {
        		String p = parameter.replaceAll(varKey,varsToUse.get(varKey));
        		parameter = p;
//        		System.out.println(" replaced in parameter " + parameter);
        	}
        	if(elementIdentifier.contains(varKey)) {
        		String e = elementIdentifier.replaceAll(varKey,varsToUse.get(varKey));
        		elementIdentifier= e;
//        		System.out.println(" replaced in parameter " + parameter);
        	}
        }
   		//System.out.println(" replaced in parameters count " + varsToUse.size());
        switch(command) {
        	case "Variable": varsToUse.put(elementIdentifier, parameter);
        				break;
            case "Access URL": Reporter.log("Acessing URL: '"+parameter+"'");
                        driver.get(parameter);
                        break;
            case "Click": Reporter.log("Clicking on element with "+identifierType+" = '"+elementIdentifier+"'");
                          WebElement elem = driver.findElement(getBy(elementIdentifier,identifierType));
                          if(elem.getTagName().equals("a")) {
                        	  Reporter.log("Click href = " + elem.getAttribute("href"));
                        	  driver.get(elem.getAttribute("href"));
                          } else if(elementType.contains("odal")) {
                        	  /*
                        	   * code to open and switch to modal
                        	   */
                        	 driver.findElement(getBy(elementIdentifier,identifierType)).click();
                              String parent = driver.getWindowHandle();
                        	  System.out.println(" Switching to Modal " + parent);
                        	  Reporter.log(" Switching to Modal " + parent);
    //                          WebDriverWait wait = new WebDriverWait(driver, 10);
     //                         WebElement push_to_create = wait.until(ExpectedConditions.elementToBeClickable(
      //                              		  getBy(elementIdentifier,identifierType) ));
                  //            push_to_create.click();
                              waitForWindow(driver);
                              switchToModalDialog(driver, parent);
                              driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
                          } else if(elementType.contains("ox")) {
                       		  System.out.println("Box ");
                        	  List<WebElement> oCheckBox = driver.findElements( getBy(elementIdentifier,identifierType) );
                        	  for(WebElement box:oCheckBox) {
                        		  System.out.println("Box Option "+ box.getTagName() + " : "+ box.getText() +  " value " + box.getAttribute("value"));
                        		  Reporter.log("Box Option "+ box.getTagName() + " : "+ box.getText() +  " value " + box.getAttribute("value"));
                        		  if(box.getAttribute("value").contains(parameter)) {
                        			  box.click();
                        			  break;
                        		  }
                        	  }
                          } else {
                        	  System.out.println("Click Element "+ elem.getTagName() + " " + elem.getAttribute("name"));
                        	  elem.click();
                          }
                          if(parameter.isEmpty()) parameter ="0";
            			  Thread.sleep(Integer.parseInt(parameter));
                          break;
            case "Sleep": Reporter.log("Sleeping for  "+parameter+" seconds ");
                          if(parameter.isEmpty()) parameter ="0";
                          driver.manage().timeouts().implicitlyWait(Integer.parseInt(parameter), TimeUnit.SECONDS);
            			  //Thread.sleep(Integer.parseInt(parameter));
                          System.out.println("Sleeping " + parameter);
                          break;
            case "Write Text": Reporter.log("Writing text '"+parameter+"' to field with "+identifierType+" = '"+elementIdentifier+"'");
                              driver.findElement(getBy(elementIdentifier,identifierType)).clear();
                              driver.findElement(getBy(elementIdentifier,identifierType)).sendKeys(parameter); 
                              break;
            case "Check URL": Reporter.log("Verifying Current URL = '"+parameter+"'");
                              assertEquals(driver.getCurrentUrl(),parameter); 
                              break; 
            case "Check Text": Reporter.log("Verifying element with "+identifierType+" = '"+elementIdentifier+"' has text = '"+parameter+"'");
                               assertEquals(driver.findElement(getBy(elementIdentifier,identifierType)).getText(),parameter); 
                               break;  
            case "Has Text": Reporter.log("Verifying element with "+identifierType+" = '"+elementIdentifier+"' has text = '"+parameter+"'");
                               String textFound = driver.findElement(getBy(elementIdentifier,identifierType)).getText(); 
                               if(textFound.length() < 1) {
                            	   textFound = driver.findElement(getBy(elementIdentifier,identifierType)).getAttribute("innerHTML");
                               }
                               if(textFound.length() < 1) {
                            	   textFound = driver.findElement(getBy(elementIdentifier,identifierType)).getAttribute("innerText");
                               }
                               if(textFound.length() < 1) {
                            	   textFound = driver.switchTo().activeElement().getAttribute("id");
                               }
            					Reporter.log("Found element with "+identifierType+" = '"+elementIdentifier+"' with text = '"+textFound+"'");
                               assertTrue(textFound.contains(parameter)); 
                               break;  
            case "Get Text": Reporter.log("Saving text with "+identifierType+" = '"+elementIdentifier+"' to variable name = '"+parameter+"'");
                             paramMap.put(parameter, driver.findElement(getBy(elementIdentifier,identifierType)).getText());
                             break;
            case "Write Saved Text": String savedText = paramMap.get(parameter);
                                     Reporter.log("Writing text '"+savedText+"' to field with "+identifierType+" = '"+elementIdentifier+"'");
                                     driver.findElement(getBy(elementIdentifier,identifierType)).clear();
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
            case "Compare SQL": Reporter.log("Compare element of type "+elementType+" with "+identifierType+" = '"+elementIdentifier);
                          boolean isSame = compareElement(elementIdentifier,elementType,identifierType,parameter);
                       	  assertEquals(isSame, true);
                                 break;
            case "Answer Question": 
            	Reporter.log("Answer element of type "+elementType+" with "+identifierType+" = '"+elementIdentifier);
            	List<String> items = Arrays.asList(parameter.split("\\s*,\\s*"));
            	AvPair av = new AvPair();
            	String newParameter = items.get(3);
            	if(items.get(3).equals("\"MAX\"")) newParameter = "MAX";
            	if(items.get(3).equals("\"MIN\"")) newParameter = "MIN";
            	av=avSet.answerQuestion(Integer.parseInt(items.get(0)), items.get(1), Boolean.valueOf(items.get(2)), newParameter);
            	String answer = av.getValue();
           		if(answer==null || answer.isEmpty()) answer=".";
            	Reporter.log(" Answer =  "+ answer + " for " + items.get(3));
            	if(elementType.equals("Select")) {
            		Select select = new Select(driver.findElement(getBy(elementIdentifier,identifierType)));
           			select.selectByValue(answer);
            	} else {
            		driver.findElement(getBy(elementIdentifier,identifierType)).clear();
            		driver.findElement(getBy(elementIdentifier,identifierType)).sendKeys(answer); 
            	}
            	break;
            case "List Children": 
            	List<WebElement> allFormChildElements = 
            		driver.findElements(getBy(elementIdentifier,identifierType)); 
            	for(WebElement item : allFormChildElements )
            	{
            		System.out.println(" Children Found " + item.getTagName() 
            				+ " type = " + item.getAttribute("type")
            				+ " name = " + item.getAttribute("name")
            				+ " id = " + item.getAttribute("id")
            				+ " value = " + item.getAttribute("value")
            				+ " text = " + item.getText());
            	}
            	break;
            	
        }
    }
    
	private boolean compareElement(String elementIdentifier,
			String elementType, String identifierType, String parameter) {
		boolean isSame = true;
		/*
		 * collect table from database using parameter as sql
		 */
		DBSQL masterIndex = new DBSQL(this.DBHost, this.DBSchema, this.DBUser,
				this.DBPassword);
		//System.out.println(this.DBHost + " " +  this.DBSchema + " " +  this.DBUser + " " +
				//this.DBPassword);
		//System.out.println(parameter);
		/*
		 * TABLE compare
		 */
		if (elementType.contains("Table")) {
			System.out.println("table search ");
			ResultSet rs = masterIndex.getDBRow(parameter);
			ResultSetMetaData rsmd;
			try {
				rsmd = rs.getMetaData();
				int colCount = rsmd.getColumnCount();
				int j = 1;
				for (WebElement row : driver.findElements(getBy(
						elementIdentifier + "> tr", identifierType))) {
					int i = 1;
					for (WebElement cell : row.findElements(getBy("td",
							identifierType))) {
						String colType = rsmd.getColumnTypeName(i);
						if (rs.isBeforeFirst())
							rs.next();
						String colAnswer = getAnswer(colType, i, rs);
						if (!cell.getText().equalsIgnoreCase(colAnswer)) {
							Reporter.log("Line " + j + " Found |"
									+ cell.getText() + "| Should Be |"
									+ colAnswer + "| Type is " + colType);
							isSame = false;
						}
						if (i == colCount)
							break;
						i++;
					}
					j++;
					if (rs.isLast())
						break;
					rs.next();
				}
				Reporter.log("Table Compared Lines  " + (j - 1));

			} catch (SQLException e) {
				e.printStackTrace();
			}
			/*
			 * SELECT compare
			 */
		} else if (elementType.contains("Select")) {
			isSame=false;
			Select select = new Select(driver.findElement(getBy(elementIdentifier,identifierType)));
			List<WebElement> options = select.getOptions();
			//System.out.println(" select found values = " + options.size());
			ResultSet rs = masterIndex.getDBRow(parameter);
			int sqlCount = 0;
			int optionCount = 0;
			for(WebElement option:options) {
				if(option.getText().length()>0) optionCount++;
			}
			try {
				//System.out.println("Select DB "+ rs.getFetchSize() + " " + parameter);
				while(rs.next()) {
					String dbValue = rs.getString(1);
					//System.out.println(" select dbValue = " + dbValue);
					sqlCount++;
					for(WebElement option:options) {
						String opt = option.getText();
						//System.out.println(" select opt = |" + opt +"|");
						if(opt.contains(dbValue)) {
							Reporter.log(" Select options found database match "+ dbValue );
							isSame=true;
							break;
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(sqlCount != optionCount) {
				System.out.println("select options count "+ optionCount + " not same as database option count " +sqlCount);
				Reporter.log(" FAILED select options count "+ optionCount + " not same as database option count " +sqlCount);
				isSame=false;
			} else {
				Reporter.log(" select options count "+ optionCount + " matches database option count " +sqlCount);
			}
			/*
			 * FIELD Compare
			 */
		} else {
			/*
			 * non table block
			 */
			isSame=false;
			boolean isBeforeFirst = true;
			ResultSet rs = masterIndex.getDBRow(parameter);
			try {
				//System.out.println("Field DB "+ rs.getFetchSize() + " " + parameter);
				while(rs.isBeforeFirst()) {
					isBeforeFirst = false;
					rs.next();
					String dbValue = rs.getString(1);
					//System.out.println("dbValue = " +dbValue);
					String GUIValueA = driver.findElement(getBy(elementIdentifier,identifierType)).getAttribute("innerHTML");
					if(GUIValueA.length()<1) {
						GUIValueA = driver.findElement(getBy(elementIdentifier,identifierType)).getAttribute("innerText");
					}
					if(GUIValueA.length()<1) {
						//System.out.println(" FAIL GUIValue = " +GUIValueA);
						Reporter.log(" FAIL GUIValue is empty |" + GUIValueA);
					}
					String GUIValueB = GUIValueA.replace("amp;", "");
					String GUIValueC = GUIValueB.replace("&gt;", ">");
					String GUIValue = GUIValueC.replace("&lt;", "<");
					isSame= GUIValue.contains(dbValue); 
					if(isSame) {
						//System.out.println("GUIValue = " +GUIValue);
						Reporter.log(" Values Match |" + GUIValue);
					} else {
						//System.out.println("dbValue = " +dbValue);
						//System.out.println("GUIValue = " +GUIValue);
						Reporter.log(" FAIL GUIValue is |" + GUIValue);
						Reporter.log(" FAIL dbValue is |" + dbValue);
					}
				}
				if(isBeforeFirst) {
					System.out.println("No query run " + parameter);
					Reporter.log("No query run " + parameter);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		masterIndex.closeCon();
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
            //System.out.println(identifierType +", "+identifier);
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
                String textOut = div.getText();
                System.out.println(textOut);
                Reporter.log(textOut);
                break;
            case "List":
                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> li",identifierType)))
                    System.out.println(e.getText());
                break;
            case "Link":
//                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> a",identifierType)))
                for (WebElement e : driver.findElements(getBy(elementIdentifier,identifierType))) {
                	System.out.println("Element "+ elementIdentifier + " " + identifierType);
                	System.out.println("enabled "+ e.isEnabled());
                	System.out.println("tag "+ e.getTagName());
                	System.out.println("href "+ e.getAttribute("href"));
                	System.out.println("class "+ e.getClass().getName());
                    System.out.println("text " + e.getText());
                }
                break;
            case "Table Header":
                for (WebElement e : driver.findElements(getBy(elementIdentifier+"> th",identifierType)))
                    System.out.println(e.getText());
                break;
            case "Table Body":
                for (WebElement row : driver.findElements(getBy(elementIdentifier+"> tr",identifierType)))
                    for (WebElement cell : row.findElements(getBy("td",identifierType)))
                        System.out.println(cell.getText());
            case "Page":
                        System.out.println(driver.getPageSource());
                break;
        }
    }
    public static void waitForWindow(WebDriver driver)
            throws InterruptedException {
        //wait until number of window handles become 2 or until 6 seconds are completed.
        int timecount = 1;
        do {
            driver.getWindowHandles();
            Thread.sleep(200);
            timecount++;
            if (timecount > 30) {
                break;
            }
        } while (driver.getWindowHandles().size() != 2);

    }

    public static void switchToModalDialog(WebDriver driver, String parent) { 
            //Switch to Modal dialog
        System.out.println(" Looking for Modal dialogs  " + driver.getWindowHandles().size());
        if (driver.getWindowHandles().size() == 2) {
            for (String window : driver.getWindowHandles()) {
            	System.out.println(" Looking for Modal dialog " + driver.getWindowHandle());
//                if (!window.equals(parent)) {
                if (window.equals(parent)) {
                    driver.switchTo().window(window);
                    System.out.println("Modal dialog found " + driver.getWindowHandle());
                    Reporter.log("Modal dialog found " + driver.getWindowHandle());
                    //System.out.println(driver.getPageSource());
                    break;
                }
            }
        }
    }
    
    public void closeDriver() {
    	driver.quit();
    }

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public String getDBHost() {
		return DBHost;
	}

	public void setDBHost(String dBHost) {
		DBHost = dBHost;
	}

	public String getDBSchema() {
		return DBSchema;
	}

	public void setDBSchema(String dBSchema) {
		DBSchema = dBSchema;
	}

	public String getDBUser() {
		return DBUser;
	}

	public void setDBUser(String dBUser) {
		DBUser = dBUser;
	}

	public String getDBPassword() {
		return DBPassword;
	}

	public void setDBPassword(String dBPassword) {
		DBPassword = dBPassword;
	}

	public HashMap<String, String> getVarsToUse() {
		return varsToUse;
	}

	public void setVarsToUse(HashMap<String, String> varsToUse) {
		if(varsToUse != null) this.varsToUse = varsToUse;
	}
    
}
