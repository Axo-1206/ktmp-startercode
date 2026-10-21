/**
 * @file WebTestBase.java
 * @brief Lớp cơ sở (Base Class) cho tất cả các bài test Selenium.
 * Chịu trách nhiệm khởi tạo và giải phóng WebDriver trước/sau mỗi test case.
 * Hỗ trợ chạy đa trình duyệt (Chrome/Firefox) và chế độ headless thông qua system properties.
 */

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
     * Khởi tạo WebDriver trước mỗi test case.
     * 
     * Quy trình:
     * 1. Đọc cấu hình từ System Properties (baseUrl, browser, headless) do Maven
     * Surefire truyền vào.
     * 2. Khởi tạo trình duyệt tương ứng (Chrome hoặc Firefox).
     * 3. Cấu hình thời gian chờ load trang tối đa (pageLoadTimeout) là 20 giây.
     * 4. Thiết lập kích thước cửa sổ trình duyệt cố định (1440x900) để đảm bảo giao
     * diện hiển thị nhất quán.
     * 
     * @see pom.xml (phần <systemPropertyVariables>)
     */
    @BeforeEach
    void openBrowser() {
        startBrowser();
    }

    /**
     * Đóng trình duyệt hiện tại và mở một phiên trình duyệt mới hoàn toàn sạch.
     * Dùng cho test cần nhiều phiên độc lập trong cùng một test (ví dụ NFR-01).
     */
    protected void restartBrowser() {
        if (driver != null) {
            driver.quit();
        }
        startBrowser();
    }

    private void startBrowser() {
        // Đọc cấu hình từ Maven (mặc định: baseUrl=https://www.saucedemo.com,
        // browser=chrome, headless=true)
        baseUrl = System.getProperty("baseUrl", "https://www.saucedemo.com");
        String browser = System.getProperty("browser", "chrome")
                .toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "true"));

        // Khởi tạo WebDriver dựa trên cấu hình browser
        driver = switch (browser) {
            case "chrome" -> new ChromeDriver(chromeOptions(headless));
            case "firefox" -> new FirefoxDriver(firefoxOptions(headless));
            default -> throw new IllegalArgumentException(
                    "Unsupported browser: " + browser);
        };

        // Cấu hình timeout và kích thước cửa sổ
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(1440, 900));
    }

    /**
     * Đóng trình duyệt và giải phóng tài nguyên WebDriver sau khi hoàn thành mỗi
     * test case.
     * 
     * Quy trình:
     * 1. Kiểm tra xem driver có null hay không (tránh NullPointerException).
     * 2. Gọi driver.quit() để đóng tất cả các cửa sổ và kết thúc tiến trình trình
     * duyệt.
     * 
     * Việc này đảm bảo mỗi test case chạy trên một phiên trình duyệt sạch, không bị
     * ảnh hưởng bởi test trước đó.
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
     * @param headless true nếu chạy ở chế độ headless (không giao diện đồ họa),
     *                 false nếu có giao diện
     * @return đối tượng {@link ChromeOptions} đã được cấu hình
     */
    private ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        // Tắt màn hình chọn search engine mặc định của Chrome để tránh popup che khuất
        options.addArguments("--disable-search-engine-choice-screen");
        return options;
    }

    /**
     * Thiết lập các tùy chọn khởi chạy cho trình duyệt Firefox.
     *
     * @param headless true nếu chạy ở chế độ headless (không giao diện đồ họa),
     *                 false nếu có giao diện
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