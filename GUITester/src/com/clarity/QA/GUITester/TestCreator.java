package com.clarity.QA.GUITester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


public class TestCreator {
    
    @Test(description="Generate test from spreadsheet")
    @Parameters({"fileName","sheetName"})
    public static void runTest(String fileName, @Optional String sheetName) throws Exception {
        WebDriver driver = InitDriverListener.getDriver();
        CommandExecutor executor = new CommandExecutor(driver);
        if (".csv".equals(fileName.substring(fileName.lastIndexOf('.')))) {
            String line = "";
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
            int rowNum = 0;
            while ((line = fileReader.readLine()) != null) {
                try {
                    String[] tokens = line.split(",");
                    executor.execute(Arrays.asList(tokens));
                    rowNum++;
                } catch(Exception e) {
                    Reporter.log("Test Failed at step: "+rowNum);
                    throw e;
                }
            }
            fileReader.close();
        } else {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(fileName)));
            XSSFSheet sheet = workbook.getSheet(sheetName);
            sheet.removeRow(sheet.getRow(0));
            int rowNum = 1;
            for (Row row: sheet) {
                try {
                    List<String> params = new ArrayList<>();
                    for (int i=0;i<5;i++) {
                        Cell cell = row.getCell(i,Row.RETURN_NULL_AND_BLANK);
                        if (cell == null)
                            params.add(null);
                        else {
                            cell.setCellType(Cell.CELL_TYPE_STRING); 
                            params.add(cell.getStringCellValue());
                        }
                    }
                    executor.execute(params);
                    rowNum++;
                } catch(Exception e) {
                    Reporter.log("Test Failed at step: "+rowNum);
                    throw e;
                }
            }
        }
    }
}
