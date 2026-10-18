package vn.edu.softwaretesting.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class LoginPage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By INVENTORY = By.id("inventory_container");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    /**
     * Khởi tạo LoginPage với WebDriver và đường dẫn URL cơ sở.
     *
     * @param driver instance của {@link WebDriver} dùng để điều khiển trình duyệt
     * @param baseUrl đường dẫn URL gốc của ứng dụng (ví dụ: https://www.saucedemo.com)
     */
    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    /**
     * Điều hướng trình duyệt tới trang đăng nhập và chờ ô nhập username hiển thị.
     *
     * @return đối tượng {@link LoginPage} hiện tại để hỗ trợ method chaining
     */
    public LoginPage open() {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME));
        return this;
    }

    /**
     * Thực hiện thao tác nhập tài khoản, mật khẩu và nhấn nút đăng nhập.
     *
     * @param username tên người dùng
     * @param password mật khẩu
     * @return kết quả đăng nhập gói gọn trong đối tượng {@link LoginResult}
     */
    public LoginResult login(String username, String password) {
        driver.findElement(USERNAME).sendKeys(username);
        driver.findElement(PASSWORD).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(INVENTORY),
                    ExpectedConditions.visibilityOfElementLocated(ERROR)));
        } catch (TimeoutException exception) {
            return new LoginResult(false, driver.getCurrentUrl(),
                    "No success or error state appeared within the timeout");
        }

        boolean accepted = !driver.findElements(INVENTORY).isEmpty();
        String message = accepted
                ? "Login accepted"
                : driver.findElement(ERROR).getText();
        return new LoginResult(accepted, driver.getCurrentUrl(), message);
    }

    /**
     * Record lưu trữ kết quả sau khi thực hiện đăng nhập.
     *
     * @param accepted true nếu đăng nhập thành công vào được trang inventory, false nếu thất bại
     * @param currentUrl URL hiện tại sau khi đăng nhập
     * @param message thông báo thành công hoặc thông báo lỗi thu được
     */
    public record LoginResult(boolean accepted, String currentUrl,
                              String message) {
    }
}
