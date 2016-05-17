package com.clarity.QA.GUITester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.uncommons.reportng.HTMLReporter;
import org.uncommons.reportng.JUnitXMLReporter;

public class GUIMain {
	static String guiClassPath = "com.clarity.QA.GUITester";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        XmlSuite suite = new XmlSuite();
        suite.setName("Clarity Test Suite");
        suite.addListener(guiClassPath + ".InitDriverListener");
//        String[] fileArray = {"/Users/jgorski/Tests/GUI/login.csv","/Users/jgorski/Tests/GUI/newgui.csv"};
        String[] fileArray = {"/Users/jgorski/Tests/GUI/firstTouch.csv"};
        String fileName;
        if(args.length > 0) {
        	fileName = args[0];
        	fileArray = args;
        } else {
        	fileName = fileArray[0];
        }
        File testFile = new File(fileName);
        System.out.println("Running test suite with file: "+testFile.getAbsolutePath());
        FileInputStream file = new FileInputStream(testFile);
        if (".csv".equals(fileName.substring(fileName.lastIndexOf('.')))) {
            for (String name : fileArray) {
                XmlTest newTest = new XmlTest(suite);
                newTest.setName(name.substring(0,name.lastIndexOf('.'))+" Test");
                List<XmlClass> testClasses = new ArrayList<>();
                testClasses.add(new XmlClass(guiClassPath+".TestCreator"));
                newTest.setXmlClasses(testClasses);
                newTest.addParameter("fileName",name.substring(name.lastIndexOf("\\")+1, name.length()));
            }
        } else if (".xlsx".equals(fileName.substring(fileName.lastIndexOf('.')))){
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            for (int i=0;i<workbook.getNumberOfSheets();i++) {
                String sheetName = workbook.getSheetAt(i).getSheetName();
                XmlTest newTest = new XmlTest(suite);
                newTest.setName(sheetName+" Test");
                List<XmlClass> testClasses = new ArrayList<>();
                testClasses.add(new XmlClass(guiClassPath+".TestCreator"));
                newTest.setXmlClasses(testClasses);
                newTest.addParameter("fileName",fileName);
                newTest.addParameter("sheetName", sheetName);
            }
        } else {
            System.out.println("Invalid test file format. Only .csv and .xlsx formats are accepted.");
            return;
        }
        List<XmlSuite> suites = new ArrayList<>();
        suites.add(suite);
        TestNG tng = new TestNG();
        tng.setUseDefaultListeners(false);
        tng.addListener(new HTMLReporter());
        tng.addListener(new JUnitXMLReporter());
        tng.setXmlSuites(suites);
        tng.setOutputDirectory("output/results/"+new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss").format(new Date()));
        tng.run();
    }
    
}
