/**
 * @file LoginPage.java
 * @brief Page Object đại diện cho trang Đăng nhập (Login Page) của SauceDemo.
 * Chứa các locator và phương thức thao tác với ô nhập Username, Password và nút Login.
 * Phục vụ các test case: TC-LOGIN-EP1..EP8, TC-LOGIN-DT1..DT5.
 */

package vn.edu.softwaretesting.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class LoginPage {

    // --- LOCATORS ---
    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By INVENTORY = By.id("inventory_container");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait loginWait;
    private final String baseUrl;

    /**
     * Khởi tạo LoginPage với WebDriver và đường dẫn URL cơ sở.
     *
     * @param driver  instance của {@link WebDriver} dùng để điều khiển trình duyệt
     * @param baseUrl đường dẫn URL gốc của ứng dụng (ví dụ:
     *                https://www.saucedemo.com)
     */
    public LoginPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        this.loginWait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
     * Xóa cookie/phiên hiện tại rồi mở lại trang đăng nhập (phiên sạch).
     * 
     * Phục vụ TC: TC-NFR-01 (mỗi lần đo cần một phiên chưa đăng nhập).
     * 
     * @return đối tượng {@link LoginPage} hiện tại để hỗ trợ method chaining
     */
    public LoginPage resetSession() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.clear(); window.sessionStorage.clear();");
        driver.get("about:blank");
        return open();
    }

    /**
     * Thực hiện thao tác nhập tài khoản, mật khẩu và nhấn nút đăng nhập.
     * 
     * Phục vụ các TC: TC-LOGIN-EP1..EP8, TC-LOGIN-DT1..DT5.
     * Quy trình:
     * 1. Nhập username vào ô user-name.
     * 2. Nhập password vào ô password.
     * 3. Nhấn nút login-button.
     * 4. Chờ (Explicit Wait) cho đến khi hoặc là trang inventory hiển thị, hoặc là
     * thông báo lỗi xuất hiện.
     * 5. Trả về đối tượng LoginResult chứa trạng thái (accepted), URL hiện tại và
     * thông báo (message).
     *
     * @param username tên người dùng
     * @param password mật khẩu
     * @return kết quả đăng nhập gói gọn trong đối tượng {@link LoginResult}
     */
    public LoginResult login(String username, String password) {
        return enterCredentials(username, password).submit();
    }

    /**
     * Nhập username và password (có xác nhận giá trị còn nguyên), chưa nhấn Login.
     *
     * @param username tên người dùng
     * @param password mật khẩu
     * @return đối tượng {@link LoginPage} hiện tại để hỗ trợ method chaining
     */
    public LoginPage enterCredentials(String username, String password) {
        typeAndConfirm(USERNAME, username);
        typeAndConfirm(PASSWORD, password);
        return this;
    }

    /**
     * Nhấn nút Login rồi chờ (Explicit Wait, tối đa 15 giây) trang sản phẩm hoặc
     * thông báo lỗi xuất hiện.
     *
     * @return kết quả đăng nhập gói gọn trong {@link LoginResult}
     */
    public LoginResult submit() {
        driver.findElement(LOGIN_BUTTON).click();

        try {
            loginWait.until(ExpectedConditions.or(
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
     * Gõ giá trị vào ô nhập và chờ giá trị còn nguyên (ổn định qua nhiều lần đọc);
     * nếu bị mất do trang vẽ lại thì gõ lại, tối đa 3 lần. Không ném ngoại lệ nếu
     * vẫn không khớp, để lỗi của hệ thống (nếu có) vẫn tái hiện được.
     *
     * @param locator định vị ô nhập
     * @param text    giá trị cần gõ (null hoặc rỗng nghĩa là để trống)
     */
    private void typeAndConfirm(By locator, String text) {
        final String expected = text == null ? "" : text;
        for (int attempt = 0; attempt < 3; attempt++) {
            WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            field.clear();
            if (expected.isEmpty()) {
                return;
            }
            field.sendKeys(expected);

            final int[] stable = { 0 };
            try {
                new WebDriverWait(driver, Duration.ofSeconds(2))
                        .pollingEvery(Duration.ofMillis(150))
                        .until(d -> {
                            String value = d.findElement(locator).getDomAttribute("value");
                            if (expected.equals(value)) {
                                return ++stable[0] >= 3;
                            }
                            stable[0] = 0;
                            return false;
                        });
                return;
            } catch (TimeoutException e) {
                // Giá trị bị mất hoặc sai -> gõ lại
            }
        }
    }

    /**
     * Record lưu trữ kết quả sau khi thực hiện đăng nhập.
     *
     * @param accepted   true nếu đăng nhập thành công vào được trang inventory,
     *                   false nếu thất bại
     * @param currentUrl URL hiện tại sau khi đăng nhập
     * @param message    thông báo thành công hoặc thông báo lỗi thu được
     */
    public record LoginResult(boolean accepted, String currentUrl,
            String message) {
    }
}