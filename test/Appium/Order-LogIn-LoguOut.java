import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;

public class {scriptName} {
	public static void main(String[] args) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("appium-version", "1.0");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("platformVersion", "4.4");
		capabilities.setCapability("app", "/Users/APPSORAMA/Documents/Apps-O-Rama/BentoCorp/Code/app/build/outputs/apk/app-stage-debug.apk");
		wd = new AppiumDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
		wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.widget.ImageView[2]")).click();
(JavascriptExecutor)wd.executeScript("mobile: tap", new HashMap<String, Double>() {{ put("tapCount", 1); put("touchCount", 1); put("duration", 0.5); put("x", 570); put("y", 675); }});
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.ScrollView[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.EditText[1]")).sendKeys("kokushos@gmail.com");
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.ScrollView[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[2]/android.widget.EditText[1]")).sendKeys("colossus");
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.ScrollView[1]/android.widget.LinearLayout[1]/android.widget.Button[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[2]/android.widget.LinearLayout[1]/android.widget.ImageButton[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[2]/android.widget.LinearLayout[1]/android.widget.EditText[1]")).sendKeys("21 isis St, San Francisco, California");
(JavascriptExecutor)wd.executeScript("mobile: tap", new HashMap<String, Double>() {{ put("tapCount", 1); put("touchCount", 1); put("duration", 0.5); put("x", 237); put("y", 299); }});
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[3]/android.widget.LinearLayout[1]/android.widget.CheckBox[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.Button[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout[1]/android.widget.Button[2]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[4]/android.widget.LinearLayout[3]/android.widget.LinearLayout[1]/android.widget.Button[2]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[2]/android.widget.Button[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[2]/android.widget.Button[1]")).click();
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.RelativeLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.widget.ImageView[1]")).click();
(JavascriptExecutor)wd.executeScript("mobile: tap", new HashMap<String, Double>() {{ put("tapCount", 1); put("touchCount", 1); put("duration", 0.5); put("x", 684); put("y", 278); }});
(JavascriptExecutor)wd.executeScript("mobile: tap", new HashMap<String, Double>() {{ put("tapCount", 1); put("touchCount", 1); put("duration", 0.5); put("x", 572); put("y", 702); }});
		wd.findElement(By.xpath("//android.widget.LinearLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.RelativeLayout[1]/android.widget.ImageView[1]")).click();
		wd.close();
	}
}
