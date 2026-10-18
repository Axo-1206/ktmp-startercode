package vn.edu.softwaretesting.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class CartPage {
    private static final By CART_CONTENTS = By.id("cart_contents_container");
    private static final By CART_ITEMS = By.cssSelector(".cart_item");
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By CHECKOUT_BUTTON = By.id("checkout");
    private static final By CONTINUE_SHOPPING_BUTTON = By.id("continue-shopping");

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Khởi tạo trang CartPage và chờ container giỏ hàng hiển thị.
     *
     * @param driver instance của {@link WebDriver} dùng để điều khiển trình duyệt
     */
    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_CONTENTS));
    }

    /**
     * Thực hiện thao tác click vào phần tử bằng JavaScript Executor.
     *
     * @param locator định vị của phần tử cần click
     */
    private void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Kiểm tra xem trình duyệt hiện tại có đang ở trang giỏ hàng (cart.html) hay không.
     *
     * @return true nếu URL hiện tại chứa "cart.html", ngược lại false
     */
    public boolean isAtCartPage() {
        return driver.getCurrentUrl().contains("cart.html");
    }

    /**
     * Lấy số lượng sản phẩm đang có trong giỏ hàng.
     *
     * @return số lượng dòng sản phẩm trong giỏ
     */
    public int getItemCount() {
        return driver.findElements(CART_ITEMS).size();
    }

    /**
     * Lấy danh sách tên của tất cả các sản phẩm đang có trong giỏ hàng.
     *
     * @return danh sách tên sản phẩm dạng {@link List} của String
     */
    public List<String> getItemNames() {
        return driver.findElements(ITEM_NAMES)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    /**
     * Xóa một sản phẩm ra khỏi giỏ hàng dựa trên slug (mã định danh) của sản phẩm.
     *
     * @param itemSlug slug của sản phẩm cần xóa (ví dụ: "sauce-labs-backpack")
     * @return đối tượng {@link CartPage} hiện tại để tiếp tục chuỗi thao tác (method chaining)
     */
    public CartPage removeItem(String itemSlug) {
        By removeButton = By.id("remove-" + itemSlug);
        jsClick(removeButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(removeButton));
        return this;
    }

    /**
     * Nhấn nút "Continue Shopping" để quay trở lại trang danh sách sản phẩm (InventoryPage).
     *
     * @return đối tượng {@link InventoryPage} đại diện cho trang danh sách sản phẩm
     */
    public InventoryPage continueShopping() {
        jsClick(CONTINUE_SHOPPING_BUTTON);
        return new InventoryPage(driver);
    }

    /**
     * Nhấn nút "Checkout" để chuyển sang bước thanh toán.
     *
     * @return đối tượng {@link CheckoutPage} đại diện cho trang thanh toán
     */
    public CheckoutPage checkout() {
        jsClick(CHECKOUT_BUTTON);
        return new CheckoutPage(driver);
    }
}