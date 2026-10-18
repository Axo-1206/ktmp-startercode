package vn.edu.softwaretesting.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class InventoryPage {
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By SORT_DROPDOWN = By.cssSelector("select.product_sort_container, [data-test='product-sort-container']");
    private static final By ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICES = By.cssSelector(".inventory_item_price");

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Khởi tạo InventoryPage và chờ container danh sách sản phẩm hiển thị.
     *
     * @param driver instance của {@link WebDriver} dùng để điều khiển trình duyệt
     */
    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
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
     * Thêm một sản phẩm vào giỏ hàng dựa trên slug của sản phẩm.
     *
     * @param itemSlug mã định danh (slug) của sản phẩm (ví dụ: "sauce-labs-backpack")
     * @return đối tượng {@link InventoryPage} hiện tại để hỗ trợ method chaining
     */
    public InventoryPage addToCart(String itemSlug) {
        jsClick(By.id("add-to-cart-" + itemSlug));
        return this;
    }

    /**
     * Xóa một sản phẩm đã thêm khỏi giỏ hàng trực tiếp từ trang Inventory.
     *
     * @param itemSlug mã định danh (slug) của sản phẩm cần xóa
     * @return đối tượng {@link InventoryPage} hiện tại để hỗ trợ method chaining
     */
    public InventoryPage removeFromCart(String itemSlug) {
        jsClick(By.id("remove-" + itemSlug));
        return this;
    }

    /**
     * Chờ cho đến khi số lượng hiển thị trên badge giỏ hàng khớp với số lượng mong đợi.
     * Nếu mong đợi 0, sẽ chờ cho đến khi badge biến mất khỏi giao diện.
     *
     * @param expectedCount số lượng mong đợi trên badge giỏ hàng
     * @return số lượng thực tế đọc được trên badge (hoặc 0 nếu badge biến mất)
     */
    public int waitForBadgeCount(int expectedCount) {
        if (expectedCount == 0) {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(CART_BADGE));
            return 0;
        } else {
            wait.until(ExpectedConditions.textToBe(CART_BADGE, String.valueOf(expectedCount)));
            return Integer.parseInt(driver.findElement(CART_BADGE).getText().trim());
        }
    }

    /**
     * Lấy số lượng sản phẩm hiển thị trên badge giỏ hàng tại thời điểm hiện tại.
     *
     * @return số lượng trên badge nếu có, hoặc 0 nếu giỏ hàng rỗng (không có badge)
     */
    public int getBadgeCount() {
        List<WebElement> badges = driver.findElements(CART_BADGE);
        if (badges.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(badges.get(0).getText().trim());
    }

    /**
     * Nhấn vào biểu tượng giỏ hàng để chuyển hướng tới trang giỏ hàng (CartPage).
     *
     * @return đối tượng {@link CartPage} đại diện cho trang giỏ hàng
     */
    public CartPage openCart() {
        jsClick(CART_LINK);
        return new CartPage(driver);
    }

    /**
     * Chọn tiêu chí sắp xếp sản phẩm từ dropdown (ví dụ: "az", "za", "lohi", "hilo").
     *
     * @param optionValue giá trị value của tùy chọn sắp xếp cần chọn
     * @return đối tượng {@link InventoryPage} hiện tại để hỗ trợ method chaining
     */
    public InventoryPage sortBy(String optionValue) {
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_DROPDOWN));
        new Select(dropdown).selectByValue(optionValue);
        return this;
    }

    /**
     * Lấy danh sách tên của toàn bộ sản phẩm đang hiển thị trên trang theo thứ tự hiện tại.
     *
     * @return danh sách chuỗi tên sản phẩm
     */
    public List<String> names() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(ITEM_NAMES))
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    /**
     * Lấy danh sách đơn giá của toàn bộ sản phẩm đang hiển thị trên trang theo thứ tự hiện tại.
     *
     * @return danh sách giá sản phẩm dạng số thực {@link Double}
     */
    public List<Double> prices() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(ITEM_PRICES))
                .stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "").trim()))
                .toList();
    }
}