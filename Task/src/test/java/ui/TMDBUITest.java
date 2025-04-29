package ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.*;

import java.time.Duration;

import static org.testng.Assert.*;

public class TMDBUITest {

    WebDriver driver;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
    }

    @Test
    public void verifyMovieInUIList() throws InterruptedException {
        driver.get("https://www.themoviedb.org/login");

        driver.findElement(By.id("username")).sendKeys("YOUR_USERNAME");
        driver.findElement(By.id("password")).sendKeys("YOUR_PASSWORD");
        driver.findElement(By.id("login_button")).click();

        driver.findElement(By.xpath("//li[@class='user']/a")).click();
        driver.findElement(By.linkText("Lists")).click();
        driver.findElement(By.xpath("//div[@class='details']")).click();

        String movieName = driver.findElement(By.xpath("(//div[@class='list_items']//a)[1]")).getText();
        assertEquals(movieName, "Inception");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) driver.quit();
    }
}

