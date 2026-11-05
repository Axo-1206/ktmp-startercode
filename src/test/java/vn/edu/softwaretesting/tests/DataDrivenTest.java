/**
 * @file DataDrivenTest.java
 * @brief Data-driven mức 2: toàn bộ URL, locator và dữ liệu kịch bản được
 *        đọc từ checkout-flow.csv. Để đổi sang website khác, chỉ cần sửa
 *        tệp CSV — không cần sửa Java.
 *
 * Cấu trúc CSV (src/test/resources/checkout-flow.csv):
 *   page, locator, action, value
 *
 * Các action được hỗ trợ:
 *   type           – xóa rồi gõ giá trị vào ô nhập
 *   click          – nhấn phần tử
 *   assertContains – kiểm tra text của phần tử chứa chuỗi value
 *
 * Cú pháp locator:
 *   id:<id>        – By.id(...)
 *   css:<selector> – By.cssSelector(...)
 *   xpath:<expr>   – By.xpath(...)
 *   name:<name>    – By.name(...)
 */

package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import vn.edu.softwaretesting.core.ScreenshotWatcher;
import vn.edu.softwaretesting.core.WebTestBase;

@Tag("web")
@Tag("data-driven-l2")
@DisplayName("Data-Driven L2: Checkout flow driven entirely by checkout-flow.csv")
@ExtendWith(ScreenshotWatcher.class)
class DataDrivenTest extends WebTestBase {

    /** Đường dẫn tệp CSV (classpath resource). */
    private static final String CSV_RESOURCE = "/checkout-flow.csv";

    /**
     * TC-DD-L2-01: Đọc từng dòng trong checkout-flow.csv và thực thi
     * tuần tự trên trình duyệt. Mỗi dòng là một bước (step) của kịch bản.
     *
     * Để đổi sang website khác hoặc thay đổi luồng:
     *   1. Sửa checkout-flow.csv – đổi URL base qua Maven property baseUrl,
     *      đổi locator/action/value trong CSV.
     *   2. Không cần sửa file Java này.
     */
    @Test
    @DisplayName("TC-DD-L2-01: Full checkout flow driven by checkout-flow.csv")
    void checkoutFlowFromCsv() throws IOException {
        List<FlowStep> steps = loadCsv(CSV_RESOURCE);
        assertTrue(!steps.isEmpty(), "checkout-flow.csv phải có ít nhất 1 bước");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Mở trang đầu tiên (baseUrl được Maven truyền vào)
        driver.get(baseUrl);

        int stepNumber = 0;
        for (FlowStep step : steps) {
            stepNumber++;
            String context = "Bước " + stepNumber + " [" + step.page() + "] "
                    + step.action() + " on " + step.locator();

            By by = resolveLocator(step.locator());

            switch (step.action().toLowerCase()) {
                case "type" -> {
                    WebElement field = wait.until(
                            ExpectedConditions.elementToBeClickable(by));
                    typeViaJs(field, step.value());
                }
                case "click" -> {
                    WebElement btn = wait.until(
                            ExpectedConditions.elementToBeClickable(by));
                    ((JavascriptExecutor) driver)
                            .executeScript("arguments[0].click();", btn);
                }
                case "assertcontains" -> {
                    WebElement el = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(by));
                    String actual = el.getText();
                    assertTrue(
                            actual.contains(step.value()),
                            context + " — expected to contain [" + step.value()
                                    + "] but got [" + actual + "]");
                }
                default -> throw new IllegalArgumentException(
                        "Action không được hỗ trợ: [" + step.action()
                                + "] tại dòng " + stepNumber);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Phân tích cú pháp locator string thành đối tượng {@link By}.
     *
     * Định dạng: {@code <prefix>:<value>}
     * Prefix được hỗ trợ: id, css, xpath, name.
     *
     * @param raw chuỗi locator từ CSV
     * @return đối tượng {@link By} tương ứng
     */
    private By resolveLocator(String raw) {
        int colon = raw.indexOf(':');
        if (colon < 0) {
            throw new IllegalArgumentException(
                    "Locator phải có định dạng <prefix>:<value>, nhận được: " + raw);
        }
        String prefix = raw.substring(0, colon).trim().toLowerCase();
        String value  = raw.substring(colon + 1).trim();
        return switch (prefix) {
            case "id"    -> By.id(value);
            case "css"   -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "name"  -> By.name(value);
            default      -> throw new IllegalArgumentException(
                    "Prefix locator không hỗ trợ: " + prefix);
        };
    }

    /**
     * Gõ giá trị vào input/textarea theo cách thân thiện với React/framework:
     * đặt native setter → dispatch input + change event.
     *
     * @param field phần tử nhập liệu
     * @param text  giá trị cần gõ
     */
    private void typeViaJs(WebElement field, String text) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "var el = arguments[0];" +
                "var value = arguments[1];" +
                "var tag = el.tagName;" +
                "var proto = (tag === 'TEXTAREA') " +
                "  ? window.HTMLTextAreaElement.prototype " +
                "  : window.HTMLInputElement.prototype;" +
                "var setter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
                "setter.call(el, value);" +
                "el.dispatchEvent(new Event('input',  { bubbles: true }));" +
                "el.dispatchEvent(new Event('change', { bubbles: true }));",
                field, text == null ? "" : text);
    }

    /**
     * Đọc tệp CSV từ classpath, bỏ qua dòng tiêu đề và dòng trống/comment.
     *
     * @param resource đường dẫn classpath (bắt đầu bằng /)
     * @return danh sách các {@link FlowStep} theo thứ tự trong tệp
     */
    private List<FlowStep> loadCsv(String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);
        if (is == null) {
            throw new IllegalStateException(
                    "Không tìm thấy tệp CSV trên classpath: " + resource);
        }
        List<FlowStep> steps = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // bỏ dòng tiêu đề
            if (header == null) return steps;

            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Tách thủ công để xử lý đúng value có thể chứa dấu phẩy
                // Định dạng: page,locator,action,value
                // value là mọi thứ sau dấu phẩy thứ 3
                String[] parts = line.split(",", 4);
                if (parts.length < 3) {
                    throw new IllegalArgumentException(
                            "Dòng " + lineNo + " trong CSV không hợp lệ: " + line);
                }
                String page    = parts[0].trim();
                String locator = parts[1].trim();
                String action  = parts[2].trim();
                String value   = parts.length > 3 ? parts[3].trim() : "";

                steps.add(new FlowStep(page, locator, action, value));
            }
        }
        return steps;
    }

    /**
     * Record đại diện cho một bước trong tệp CSV.
     *
     * @param page    nhãn trang (chỉ dùng để hiển thị trong log / thông báo lỗi)
     * @param locator chuỗi locator dạng {@code prefix:value}
     * @param action  hành động cần thực hiện (type/click/assertContains)
     * @param value   giá trị tương ứng với action
     */
    record FlowStep(String page, String locator, String action, String value) {}
}
