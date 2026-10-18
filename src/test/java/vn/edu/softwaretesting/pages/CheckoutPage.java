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

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    private void jsClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

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

    public String continueExpectingError() {
        jsClick(CONTINUE);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }

    public CheckoutPage continueToOverview() {
        jsClick(CONTINUE);
        wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL));
        return this;
    }

    public String continueToOverviewAndReadTotal() {
        jsClick(CONTINUE);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL)).getText();
    }

    public String readItemTotal() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ITEM_TOTAL)).getText();
    }

    public String readTax() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TAX)).getText();
    }

    public String readTotal() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TOTAL)).getText();
    }

    public CartPage cancelAtStepOne() {
        jsClick(CANCEL);
        return new CartPage(driver);
    }

    public CheckoutPage finishOrder() {
        jsClick(FINISH);
        wait.until(ExpectedConditions.visibilityOfElementLocated(COMPLETE_HEADER));
        return this;
    }

    public String readCompleteHeader() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(COMPLETE_HEADER)).getText();
    }

    public boolean isOrderCompleteUrl() {
        return driver.getCurrentUrl().contains("checkout-complete.html");
    }

    public int getBadgeCountAfterFinish() {
        List<WebElement> badges = driver.findElements(CART_BADGE);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText().trim());
    }
}