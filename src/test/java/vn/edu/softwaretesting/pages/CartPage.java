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

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_CONTENTS));
    }

    private void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public boolean isAtCartPage() {
        return driver.getCurrentUrl().contains("cart.html");
    }

    public int getItemCount() {
        return driver.findElements(CART_ITEMS).size();
    }

    public List<String> getItemNames() {
        return driver.findElements(ITEM_NAMES)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public CartPage removeItem(String itemSlug) {
        By removeButton = By.id("remove-" + itemSlug);
        jsClick(removeButton);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(removeButton));
        return this;
    }

    public InventoryPage continueShopping() {
        jsClick(CONTINUE_SHOPPING_BUTTON);
        return new InventoryPage(driver);
    }

    public CheckoutPage checkout() {
        jsClick(CHECKOUT_BUTTON);
        return new CheckoutPage(driver);
    }
}