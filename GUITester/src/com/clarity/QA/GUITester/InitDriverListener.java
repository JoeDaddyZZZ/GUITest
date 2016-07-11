package com.clarity.QA.GUITester;

import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.common.device.DeviceTargetPlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.uiautomation.ios.IOSCapabilities;

public class InitDriverListener implements ISuiteListener {

    public static WebDriver driver;
//    public static WebDriver driver;
//    private static FileHandler fh;
    private  FileHandler fh;
    public String  useDriver;
//    public static String useDriver;
    
 //   public static String getUseDriver() {
    public String getUseDriver() {
        return useDriver;
    }
    
//    public static WebDriver getDriver() {
    public static  WebDriver getDriver() {
            return driver;
    }

    @Override
    public void onStart(ISuite suite) {
        Properties prop = new Properties();
        prop = readProps("res/conf.properties");
        useDriver = prop.getProperty("useDriver");
        /*
         * open drivers
         */
        System.out.println( " using driver " + useDriver);
        if (useDriver.contains("PhantomJS")) {
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true); 
            caps.setCapability("takesScreenshot", true);
            //caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,prop.getProperty("phantomJSPath"));
            String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, Collections.singletonList("--ignore-ssl-errors=yes"));;
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
       		prop.getProperty("phantomjsPath"));
            caps.setJavascriptEnabled(true); // enabled by default
            driver = new PhantomJSDriver(caps);
        } else if (useDriver.contains("Android")) {
//        	SelendroidCapabilities capa = new SelendroidCapabilities("io.selendroid.testapp:0.17.0");
        	SelendroidCapabilities capa = new SelendroidCapabilities("android");
        	capa.setPlatformVersion(DeviceTargetPlatform.ANDROID23);
        	capa.setEmulator(true);
        	capa.setModel("Nexus 6");
//        	driver = new RemoteWebDriver(DesiredCapabilities.android());
        	driver = new RemoteWebDriver(capa);
        } else if (useDriver.contains("FireFox")) {
            FirefoxBinary binary = new FirefoxBinary(new File(prop.getProperty("firefoxPath")));
            FirefoxProfile profile = new FirefoxProfile();
            driver = new FirefoxDriver(binary, profile);
            driver.get("https://www.clarityssi.com/");
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        } else if (useDriver.contains("IEWin32")) {
        	File IEexeFile = new File(prop.getProperty("IEWin32Path"));
        	System.setProperty("webdriver.ie.driver",IEexeFile.getAbsolutePath());
            driver = new InternetExplorerDriver();
            driver.get("https://www.clarityssi.com/");
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        } else if (useDriver.contains("IEx64")) {
        	File IEexeFile = new File(prop.getProperty("IEx64Path"));
        	System.setProperty("webdriver.ie.driver",IEexeFile.getAbsolutePath());
            driver = new InternetExplorerDriver();
            driver.get("https://www.clarityssi.com/");
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        } else if (useDriver.contains("Chrome")) {
        	System.setProperty("webdriver.chrome.driver", prop.getProperty("chromePath"));
            driver = new ChromeDriver();
        } else if (useDriver.contains("iPhone")) {
        	try {
        		DesiredCapabilities safari = IOSCapabilities.iphone("Safari");
        		safari.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				driver = new RemoteWebDriver(new URL("http://localhost:4723/wd/hub"),safari);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        } else if (useDriver.contains("Safari")) {
        	SafariOptions options = new SafariOptions();
        	 options.setUseCleanSession(true);
        	driver = new SafariDriver(options);
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	driver.manage().window().setSize(new Dimension(1024, 850));
       		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        }
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public static Properties readProps(String propFileName) {
        Properties prop = new Properties();
        InputStream input = null;
	/*
	 * process properties
	 */
        try {
            input = new FileInputStream(propFileName);
            prop.load(input);
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
		return prop;
	}

	@Override
    public void onFinish(ISuite suite) {
        if(driver != null) {
            try {
            	Thread.sleep(5);
                driver.quit();
            }
            catch (WebDriverException | InterruptedException e) {
//            catch (WebDriverException e) {
                e.printStackTrace();
            }
        }
        if (fh != null)
            fh.close();
    }
}

