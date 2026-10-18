package vn.edu.softwaretesting.core;

import java.time.Duration;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public abstract class WebTestBase {

    protected WebDriver driver;
    protected String baseUrl;

    /**
     * Khởi tạo WebDriver trước mỗi test case (hỗ trợ Chrome/Firefox, cấu hình headless, timeout và kích thước cửa sổ).
     */
    @BeforeEach
    void openBrowser() {
        baseUrl = System.getProperty("baseUrl", "https://www.saucedemo.com");
        String browser = System.getProperty("browser", "chrome")
                .toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "true"));

        driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser);
        };

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(1440, 900));
    }

    /**
     * Đóng trình duyệt và giải phóng tài nguyên WebDriver sau khi hoàn thành mỗi test case.
     */
    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Thiết lập các tùy chọn khởi chạy cho trình duyệt Chrome.
     *
     * @param headless true nếu chạy ở chế độ headless (không giao diện đồ họa), false nếu có giao diện
     * @return đối tượng {@link ChromeOptions} đã được cấu hình
     */
    private ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-search-engine-choice-screen");
        return options;
    }

    /**
     * Thiết lập các tùy chọn khởi chạy cho trình duyệt Firefox.
     *
     * @param headless true nếu chạy ở chế độ headless (không giao diện đồ họa), false nếu có giao diện
     * @return đối tượng {@link FirefoxOptions} đã được cấu hình
     */
    private FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }
}
