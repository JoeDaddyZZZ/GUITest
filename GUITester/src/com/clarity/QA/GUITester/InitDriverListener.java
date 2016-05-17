package com.clarity.QA.GUITester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class InitDriverListener implements ISuiteListener {

    public static WebDriver driver;
    private static FileHandler fh;
    public static String usePhantomJS;
    
    public static String getUsePhantomJS() {
        return usePhantomJS;
    }
    
    public static WebDriver getDriver() {
            return driver;
    }

    @Override
    public void onStart(ISuite suite) {
        Properties prop = new Properties();
	InputStream input = null;
        try {
//            input = new FileInputStream("/Users/jgorski/Tests/GUI/conf.properties");
            input = new FileInputStream("res/conf.properties");
            prop.load(input);
            usePhantomJS = prop.getProperty("usePhantomJS");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
	}
        if ("true".equals(usePhantomJS)) {
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true); 
            caps.setCapability("takesScreenshot", true);
            //caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,prop.getProperty("phantomJSPath"));
            String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, Collections.singletonList("--ignore-ssl-errors=yes"));;
            driver = new PhantomJSDriver(caps);
        } else {
            FirefoxBinary binary = new FirefoxBinary(new File(prop.getProperty("firefoxPath")));
            FirefoxProfile profile = new FirefoxProfile();
            driver = new FirefoxDriver(binary, profile);
        }
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @Override
    public void onFinish(ISuite suite) {
        if(driver != null) {
            try {
            	Thread.sleep(50);
                driver.quit();
            }
            catch (WebDriverException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (fh != null)
            fh.close();
    }
}

