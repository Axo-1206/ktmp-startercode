/**
 * @file CheckoutTest.java
 * @brief Chứa các test case kiểm thử chức năng Thanh toán (FR-03).
 * Bao gồm Use Case (UC), Bảng quyết định (DT) và BVA cho quy tắc làm tròn thuế 8%.
 */

package vn.edu.softwaretesting.tests;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.CheckoutPage;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("FR-03: Checkout Use Cases, Decision Table & Tax BVA")
class CheckoutTest extends WebTestBase {

    /**
     * Phương thức tiện ích đăng nhập standard_user, thêm sản phẩm balo vào giỏ và
     * bắt đầu quy trình checkout.
     *
     * @return đối tượng {@link CheckoutPage} ở bước thanh toán Step One
     */
    private CheckoutPage startCheckoutWithSingleBackpack() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        new InventoryPage(driver).addToCart("sauce-labs-backpack").openCart();
        return new CartPage(driver).checkout();
    }

    /**
     * TC-CHK-01.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ có 1 sản phẩm Backpack.
     * Quy trình:
     * 1. Nhập thông tin hợp lệ (An / Nguyen / 700000), nhấn Continue.
     * 2. Đọc và kiểm tra Item total, Tax, Total trên trang Overview.
     * 3. Nhấn Finish.
     * 4. Kiểm tra URL, thông báo hoàn tất và badge giỏ hàng đã biến mất.
     */
    @Test
    @DisplayName("TC-CHK-01: Main flow computes Item total, 8% Tax, Total and completes order")
    void mainFlowShowsCorrectTotalAndFinishes() {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();

        checkoutPage.fillInformation("An", "Nguyen", "700000").continueToOverview();

        assertEquals("Item total: $29.99", checkoutPage.readItemTotal());
        assertEquals("Tax: $2.40", checkoutPage.readTax());
        assertEquals("Total: $32.39", checkoutPage.readTotal());

        checkoutPage.finishOrder();
        assertTrue(checkoutPage.isOrderCompleteUrl());
        assertEquals("Thank you for your order!", checkoutPage.readCompleteHeader());
        assertEquals(0, checkoutPage.getBadgeCountAfterFinish());
    }

    /**
     * TC-CHK-02, TC-CHK-03, TC-CHK-04, TC-CHK-02b, TC-CHK-03b.
     * 
     * Quy trình:
     * 1. Điền thông tin (có thể để trống một số trường) theo dữ liệu từ CsvSource.
     * 2. Nhấn Continue.
     * 3. Đọc thông báo lỗi và kiểm tra xem có chứa chuỗi mong đợi không.
     * 
     * @param id       mã định danh test case
     * @param first    tên khách hàng nhập vào (hoặc rỗng)
     * @param last     họ khách hàng nhập vào (hoặc rỗng)
     * @param postal   mã bưu điện nhập vào (hoặc rỗng)
     * @param expected thông báo lỗi xác thực mong đợi xuất hiện
     */
    @ParameterizedTest(name = "{0}: first=''{1}'', last=''{2}'', postal=''{3}'' -> {4}")
    @CsvSource({
            "TC-CHK-02, '', Nguyen, 700000, Error: First Name is required",
            "TC-CHK-03, An, '', 700000, Error: Last Name is required",
            "TC-CHK-04, An, Nguyen, '', Error: Postal Code is required",
            "TC-CHK-02b, '', '', '', Error: First Name is required"
    })
    void missingFieldShowsExpectedValidationError(String id, String first, String last,
            String postal, String expected) {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        String message = checkoutPage
                .fillInformation(first, last, postal)
                .continueExpectingError();

        assertTrue(message.contains(expected), () -> id + " expected [" + expected + "] but got: [" + message + "]");
    }

    /**
     * TC-CHK-05.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ có 1 sản phẩm.
     * Quy trình:
     * 1. Ở bước Checkout Step One, nhấn Cancel.
     * 2. Kiểm tra URL quay về cart.html.
     * 3. Kiểm tra sản phẩm vẫn còn trong giỏ.
     */
    @Test
    @DisplayName("TC-CHK-05: Cancel at checkout step one returns to cart.html with item preserved")
    void cancelAtStepOneReturnsToCartWithItem() {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        CartPage cartPage = checkoutPage.cancelAtStepOne();

        assertTrue(cartPage.isAtCartPage());
        assertEquals(1, cartPage.getItemCount());
    }

    /**
     * Nguồn cung cấp dữ liệu kiểm thử giá trị biên (BVA) cho quy tắc làm tròn thuế
     * 8%.
     *
     * @return luồng {@link Stream} chứa các bộ đối số gồm mã TC, danh sách sản
     *         phẩm, subtotal, thuế và tổng tiền mong đợi
     */
    static Stream<Arguments> taxRoundingBvaProvider() {
        return Stream.of(
                Arguments.of("TC-TAX-B1",
                        List.of("sauce-labs-bike-light", "sauce-labs-bolt-t-shirt"),
                        "Item total: $25.98", "Tax: $2.08", "Total: $28.06"),
                Arguments.of("TC-TAX-B2",
                        List.of("sauce-labs-backpack"),
                        "Item total: $29.99", "Tax: $2.40", "Total: $32.39"),
                Arguments.of("TC-TAX-B3",
                        List.of("sauce-labs-bike-light", "sauce-labs-onesie", "sauce-labs-bolt-t-shirt"),
                        "Item total: $33.97", "Tax: $2.72", "Total: $36.69"));
    }

    /**
     * TC-TAX-B1, TC-TAX-B2, TC-TAX-B3.
     * 
     * Quy trình:
     * 1. Thêm các sản phẩm theo danh sách.
     * 2. Checkout và nhập thông tin hợp lệ.
     * 3. Đọc và kiểm tra Item total, Tax (8% làm tròn), Total.
     * 
     * @param tcId             mã định danh test case
     * @param items            danh sách các slug sản phẩm cần thêm vào giỏ
     * @param expectedSubtotal tổng tiền hàng mong đợi
     * @param expectedTax      tiền thuế 8% mong đợi sau khi làm tròn
     * @param expectedTotal    tổng số tiền thanh toán mong đợi
     */
    @ParameterizedTest(name = "{0}: Verify 8% tax rounding ({3}) and {4}")
    @MethodSource("taxRoundingBvaProvider")
    void testTaxRoundingBoundaries(String tcId, List<String> items,
            String expectedSubtotal, String expectedTax, String expectedTotal) {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        InventoryPage inventoryPage = new InventoryPage(driver);
        for (String item : items) {
            inventoryPage.addToCart(item);
        }

        CheckoutPage checkoutPage = inventoryPage.openCart().checkout();
        checkoutPage.fillInformation("An", "Nguyen", "700000").continueToOverview();

        assertEquals(expectedSubtotal, checkoutPage.readItemTotal(), tcId + " subtotal mismatch");
        assertEquals(expectedTax, checkoutPage.readTax(), tcId + " tax rounding mismatch");
        assertEquals(expectedTotal, checkoutPage.readTotal(), tcId + " total mismatch");
    }

    /**
     * TC-CHK-BUG03 (Known Bug).
     * 
     * Tiền điều kiện: Đăng nhập problem_user, giỏ có 1 sản phẩm.
     * Quy trình:
     * 1. Nhập thông tin (An / Nguyen / 700000), nhấn Continue.
     * 2. Kiểm tra Total trên trang Overview.
     * 
     * Lưu ý: Test này PHẢI FAIL vì problem_user bị lỗi trường Last Name.
     */
    @Test
    @Tag("known-bug")
    @DisplayName("TC-CHK-BUG03 [BUG-03]: problem_user cannot proceed past checkout step one due to Last Name field bug")
    void problemUserFailsCheckoutStepOne() {
        new LoginPage(driver, baseUrl).open().login("problem_user", "secret_sauce");
        new InventoryPage(driver).addToCart("sauce-labs-backpack").openCart();
        CheckoutPage checkoutPage = new CartPage(driver).checkout();

        // Test này PHẢI FAIL vì hệ thống có bug (không cho qua bước 1)
        // Ta assert rằng nó ném ra RuntimeException (Validation Error)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            checkoutPage.fillInformation("An", "Nguyen", "700000")
                    .continueToOverviewAndReadTotal();
        });

        assertTrue(exception.getMessage().contains("Validation Error"),
                "BUG-03: problem_user should get validation error at step one");
    }
}