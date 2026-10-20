/**
 * @file CheckoutPage.java
 * @brief Page Object đại diện cho quy trình Thanh toán (Checkout Page).
 * Bao gồm 3 bước: Nhập thông tin (Step One), Tổng quan đơn hàng (Step Two) và Hoàn tất (Complete).
 * Phục vụ các test case: FR-03 (TC-CHK-01..06, TC-TAX-B1..B3).
 */

package vn.edu.softwaretesting.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class CheckoutPage {

    // --- LOCATORS ---
    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By CANCEL = By.id("cancel");
    private static final By FINISH = By.id("finish");
    private static final By ERROR = By.cssSelector("[data-test='error']");
    private static final By ITEM_TOTAL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX = By.cssSelector(".summary_tax_label");
    private static final By TOTAL = By.cssSelector(".summary_total_label");
    private static final By COMPLETE_HEADER = By.cssSelector(".complete-header");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Khởi tạo CheckoutPage với WebDriver và cài đặt thời gian chờ tường minh.
     *
     * @param driver instance của {@link WebDriver} dùng để điều khiển trình duyệt
     */
    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    /**
     * Thực hiện thao tác click vào phần tử bằng JavaScript Executor sau khi phần tử
     * xuất hiện trong DOM.
     *
     * @param locator định vị của phần tử cần click
     */
    private void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Điền thông tin cá nhân khách hàng ở bước Checkout đầu tiên (Step One).
     * 
     * Phục vụ các TC: TC-CHK-01, TC-CHK-02, TC-CHK-03, TC-CHK-04, TC-TAX-B1..B3.
     *
     * @param first  tên của khách hàng (First Name)
     * @param last   họ của khách hàng (Last Name)
     * @param postal mã bưu chính (Postal/Zip Code)
     * @return đối tượng {@link CheckoutPage} hiện tại để tiếp tục chuỗi thao tác
     */
    public CheckoutPage fillInformation(String first, String last, String postal) {
        WebElement firstInput = wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME));
        firstInput.clear();
        if (first != null && !first.isEmpty()) {
            firstInput.sendKeys(first);
        }

        WebElement lastInput = driver.findElement(LAST_NAME);
        lastInput.clear();
        if (last != null && !last.isEmpty()) {
            lastInput.sendKeys(last);
        }

        WebElement postalInput = driver.findElement(POSTAL);
        postalInput.clear();
        if (postal != null && !postal.isEmpty()) {
            postalInput.sendKeys(postal);
        }
        return this;
    }

    /**
     * Nhấn nút Continue và đọc thông báo lỗi xuất hiện khi thiếu dữ liệu hoặc thông
     * tin không hợp lệ.
     * 
     * Phục vụ các TC: TC-CHK-02, TC-CHK-03, TC-CHK-04.
     *
     * @return chuỗi thông báo lỗi hiển thị trên giao diện
     */
    public String continueExpectingError() {
        jsClick(CONTINUE);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }

    /**
     * Nhấn nút Continue để chuyển sang trang tóm tắt đơn hàng (Checkout: Overview)
     * và chờ thông tin tổng tiền hiển thị.
     * 
     * Phục vụ các TC: TC-CHK-01, TC-TAX-B1..B3.
     *
     * @return đối tượng {@link CheckoutPage} hiện tại để tiếp tục chuỗi thao tác
     */
    public CheckoutPage continueToOverview() {
        jsClick(CONTINUE);
        wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL));
        return this;
    }

    /**
     * Nhấn nút Continue và đọc giá trị tổng tiền (Total) hiển thị ở trang tóm tắt
     * đơn hàng.
     * 
     * Phục vụ TC: TC-CHK-BUG03 (dùng cho problem_user).
     *
     * @return chuỗi văn bản thể hiện tổng tiền (bao gồm cả thuế)
     */
    public String continueToOverviewAndReadTotal() {
        jsClick(CONTINUE);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL)).getText();
    }

    /**
     * Đọc giá trị tổng tiền sản phẩm (Item total / subtotal) từ trang tóm tắt đơn
     * hàng.
     * 
     * Phục vụ các TC: TC-CHK-01, TC-TAX-B1..B3.
     *
     * @return chuỗi văn bản tiền hàng (ví dụ: "Item total: $29.99")
     */
    public String readItemTotal() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ITEM_TOTAL)).getText();
    }

    /**
     * Đọc giá trị tiền thuế tính toán được (Tax) từ trang tóm tắt đơn hàng.
     * 
     * Phục vụ các TC: TC-CHK-01, TC-TAX-B1..B3.
     *
     * @return chuỗi văn bản tiền thuế (ví dụ: "Tax: $2.40")
     */
    public String readTax() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TAX)).getText();
    }

    /**
     * Đọc tổng số tiền phải thanh toán (Total = Item total + Tax) từ trang tóm tắt
     * đơn hàng.
     * 
     * Phục vụ các TC: TC-CHK-01, TC-TAX-B1..B3.
     *
     * @return chuỗi văn bản tổng tiền (ví dụ: "Total: $32.39")
     */
    public String readTotal() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL)).getText();
    }

    /**
     * Nhấn nút Cancel ở bước Checkout Step One để hủy quá trình thanh toán và quay
     * về giỏ hàng.
     * 
     * Phục vụ TC: TC-CHK-05.
     *
     * @return đối tượng {@link CartPage} đại diện cho trang giỏ hàng
     */
    public CartPage cancelAtStepOne() {
        jsClick(CANCEL);
        return new CartPage(driver);
    }

    /**
     * Nhấn nút Finish để hoàn tất đơn đặt hàng và chờ thông báo hoàn tất hiển thị.
     * 
     * Phục vụ TC: TC-CHK-01.
     *
     * @return đối tượng {@link CheckoutPage} hiện tại để tiếp tục chuỗi thao tác
     */
    public CheckoutPage finishOrder() {
        jsClick(FINISH);
        wait.until(ExpectedConditions.visibilityOfElementLocated(COMPLETE_HEADER));
        return this;
    }

    /**
     * Đọc tiêu đề thông báo đơn hàng hoàn tất (Complete Header).
     * 
     * Phục vụ TC: TC-CHK-01.
     *
     * @return chuỗi tiêu đề thông báo (ví dụ: "Thank you for your order!")
     */
    public String readCompleteHeader() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(COMPLETE_HEADER)).getText();
    }

    /**
     * Kiểm tra URL hiện tại có phải là trang hoàn tất đơn hàng
     * (checkout-complete.html) hay không.
     * 
     * Phục vụ TC: TC-CHK-01.
     *
     * @return true nếu URL chứa "checkout-complete.html", ngược lại false
     */
    public boolean isOrderCompleteUrl() {
        return driver.getCurrentUrl().contains("checkout-complete.html");
    }

    /**
     * Lấy số lượng trên badge giỏ hàng sau khi đã hoàn tất đặt hàng (mong đợi là 0
     * khi giỏ đã reset).
     * 
     * Phục vụ TC: TC-CHK-01.
     *
     * @return số lượng hiển thị trên badge, hoặc 0 nếu không còn badge
     */
    public int getBadgeCountAfterFinish() {
        // Chờ badge biến mất hoàn toàn (nếu có)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(CART_BADGE));
            return 0;
        } catch (Exception e) {
            // Nếu badge vẫn còn, đọc số lượng
            List<WebElement> badges = driver.findElements(CART_BADGE);
            return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText().trim());
        }
    }
}