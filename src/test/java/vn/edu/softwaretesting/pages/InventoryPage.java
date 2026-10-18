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

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
    }

    private void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public InventoryPage addToCart(String itemSlug) {
        jsClick(By.id("add-to-cart-" + itemSlug));
        return this;
    }

    public InventoryPage removeFromCart(String itemSlug) {
        jsClick(By.id("remove-" + itemSlug));
        return this;
    }

    public int waitForBadgeCount(int expectedCount) {
        if (expectedCount == 0) {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(CART_BADGE));
            return 0;
        } else {
            wait.until(ExpectedConditions.textToBe(CART_BADGE, String.valueOf(expectedCount)));
            return Integer.parseInt(driver.findElement(CART_BADGE).getText().trim());
        }
    }

    public int getBadgeCount() {
        List<WebElement> badges = driver.findElements(CART_BADGE);
        if (badges.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(badges.get(0).getText().trim());
    }

    public CartPage openCart() {
        jsClick(CART_LINK);
        return new CartPage(driver);
    }

    public InventoryPage sortBy(String optionValue) {
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_DROPDOWN));
        new Select(dropdown).selectByValue(optionValue);
        return this;
    }

    public List<String> names() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(ITEM_NAMES))
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public List<Double> prices() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(ITEM_PRICES))
                .stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "").trim()))
                .toList();
    }
}