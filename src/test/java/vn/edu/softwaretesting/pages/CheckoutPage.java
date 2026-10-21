/**
 * @file CheckoutPage.java
 * @brief Page Object đại diện cho quy trình Thanh toán (Checkout Page).
 * Bao gồm 3 bước: Nhập thông tin (Step One), Tổng quan đơn hàng (Step Two) và Hoàn tất (Complete).
 * Phục vụ các test case: FR-03 (TC-CHK-01..06, TC-CHK-DT1..DT2, TC-CHK-BUG03, TC-TAX-B1..B3).
 */

package vn.edu.softwaretesting.pages;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
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

    // Locators cho trang Overview
    private static final By ITEM_TOTAL = By.cssSelector(".summary_subtotal_label");
    private static final By TAX = By.cssSelector(".summary_tax_label");
    private static final By TOTAL = By.cssSelector(".summary_total_label");

    // Locators cho trang Complete
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
        typeAndConfirm(FIRST_NAME, first);
        typeAndConfirm(LAST_NAME, last);
        typeAndConfirm(POSTAL, postal);
        return this;
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
     * Nhấn nút Continue để chuyển sang trang tóm tắt đơn hàng (Checkout: Overview).
     * 
     * Phục vụ các TC: TC-CHK-01, TC-TAX-B1..B3.
     * 
     * Hàm này xử lý cả 2 trường hợp:
     * 1. Chuyển trang thành công -> Chờ Total xuất hiện.
     * 2. Có lỗi validate hiện ra (dành cho problem_user) -> Ném RuntimeException.
     *
     * @return đối tượng {@link CheckoutPage} hiện tại để tiếp tục chuỗi thao tác
     */
    public CheckoutPage continueToOverview() {
        jsClick(CONTINUE);

        // Chờ trong 3 giây xem có lỗi hiện ra không (dành cho problem_user)
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement errorElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(ERROR));
            // Nếu có lỗi, ném ngoại lệ để test case biết
            throw new RuntimeException("Validation Error: " + errorElement.getText());
        } catch (TimeoutException e) {
            // Không có lỗi, chờ trang Overview hiển thị (Total)
            wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL));
        }

        return this;
    }

    /**
     * Nhấn nút Continue và đọc giá trị tổng tiền (Total) hiển thị ở trang tóm tắt
     * đơn hàng.
     * 
     * Phục vụ TC: TC-CHK-BUG03 (dùng cho problem_user để kiểm tra xem có bị lỗi
     * không).
     * 
     * Hàm này sẽ ném RuntimeException nếu có lỗi validate.
     *
     * @return chuỗi văn bản thể hiện tổng tiền (bao gồm cả thuế)
     */
    public String continueToOverviewAndReadTotal() {
        jsClick(CONTINUE);

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement errorElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(ERROR));
            throw new RuntimeException("Validation Error: " + errorElement.getText());
        } catch (TimeoutException e) {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL)).getText();
        }
    }

    /**
     * Kiểm tra xem có thông báo lỗi validate nào đang hiển thị trên trang không.
     * 
     * Phục vụ TC: TC-CHK-BUG03 (dùng để assert problem_user gặp lỗi).
     *
     * @return true nếu có thông báo lỗi, ngược lại false
     */
    public boolean isValidationErrorVisible() {
        try {
            return !driver.findElements(ERROR).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Kiểm tra URL hiện tại có phải bước nhập thông tin (checkout-step-one.html).
     * 
     * Phục vụ TC: TC-CHK-02..04, TC-CHK-DT1, TC-CHK-DT2.
     *
     * @return true nếu URL chứa "checkout-step-one.html"
     */
    public boolean isAtInformationPage() {
        return driver.getCurrentUrl().contains("checkout-step-one.html");
    }

    /**
     * Kiểm tra URL hiện tại có phải trang tổng quan (checkout-step-two.html).
     * 
     * Phục vụ TC: TC-CHK-06, TC-CHK-BUG03.
     *
     * @return true nếu URL chứa "checkout-step-two.html"
     */
    public boolean isAtOverviewPage() {
        return driver.getCurrentUrl().contains("checkout-step-two.html");
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