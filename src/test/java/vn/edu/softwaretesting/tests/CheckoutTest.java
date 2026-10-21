/**
 * @file CheckoutTest.java
 * @brief Chứa các test case kiểm thử chức năng Thanh toán (FR-03).
 * Bao gồm Use Case (UC), Bảng quyết định (DT) và BVA cho quy tắc làm tròn thuế 8%.
 */

package vn.edu.softwaretesting.tests;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
     * TC-CHK-02, TC-CHK-03, TC-CHK-04, TC-CHK-DT1, TC-CHK-DT2.
     * 
     * Quy trình:
     * 1. Điền thông tin (có thể để trống một số trường) theo dữ liệu từ CsvSource.
     * 2. Nhấn Continue.
     * 3. Kiểm tra vẫn ở checkout-step-one.html.
     * 4. Đọc thông báo lỗi và kiểm tra có chứa chuỗi mong đợi (chỉ báo lỗi trường
     * rỗng đầu tiên theo thứ tự First -> Last -> Postal, FR-03.2).
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
            "TC-CHK-DT1, '', '', 700000, Error: First Name is required",
            "TC-CHK-DT2, An, '', '', Error: Last Name is required"
    })
    void missingFieldShowsExpectedValidationError(String id, String first, String last,
            String postal, String expected) {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        String message = checkoutPage
                .fillInformation(first, last, postal)
                .continueExpectingError();

        assertTrue(checkoutPage.isAtInformationPage(), id + ": phải vẫn ở checkout-step-one.html");
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
     * TC-CHK-06.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ có 1 sản phẩm, ở
     * checkout-step-one.html.
     * Quy trình:
     * 1. Nhập đầy đủ First/Last/Postal (An / Nguyen / 700000), nhấn Continue.
     * 2. Kiểm tra URL checkout-step-two.html và trang hiển thị Item total / Tax /
     * Total.
     */
    @Test
    @DisplayName("TC-CHK-06: All fields filled -> Continue goes to checkout-step-two.html showing Item total, Tax, Total")
    void validInformationGoesToOverview() {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        checkoutPage.fillInformation("An", "Nguyen", "700000").continueToOverview();

        assertTrue(checkoutPage.isAtOverviewPage(), "URL phải là checkout-step-two.html");
        assertTrue(checkoutPage.readItemTotal().startsWith("Item total:"));
        assertTrue(checkoutPage.readTax().startsWith("Tax:"));
        assertTrue(checkoutPage.readTotal().startsWith("Total:"));
    }

    /**
     * Nguồn cung cấp dữ liệu kiểm thử giá trị biên (BVA) cho quy tắc làm tròn thuế
     * 8% (khớp sheet TestCases: TC-TAX-B1..B3).
     *
     * @return luồng {@link Stream} chứa các bộ đối số gồm mã TC, danh sách sản
     *         phẩm, subtotal, thuế và tổng tiền mong đợi
     */
    static Stream<Arguments> taxRoundingBvaProvider() {
        return Stream.of(
                // 29.99 x 8% = 2.3992 -> 2.40
                Arguments.of("TC-TAX-B1",
                        List.of("sauce-labs-backpack"),
                        "Item total: $29.99", "Tax: $2.40", "Total: $32.39"),
                // 9.99 x 8% = 0.7992 -> 0.80
                Arguments.of("TC-TAX-B2",
                        List.of("sauce-labs-bike-light"),
                        "Item total: $9.99", "Tax: $0.80", "Total: $10.79"),
                // 129.94 x 8% = 10.3952 -> 10.40 (sát biên làm tròn 0.005)
                Arguments.of("TC-TAX-B3",
                        List.of("sauce-labs-backpack", "sauce-labs-bike-light", "sauce-labs-bolt-t-shirt",
                                "sauce-labs-fleece-jacket", "sauce-labs-onesie",
                                "test.allthethings()-t-shirt-(red)"),
                        "Item total: $129.94", "Tax: $10.40", "Total: $140.34"));
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
     * Tiền điều kiện: Đăng nhập problem_user, giỏ có 1 sản phẩm, ở
     * checkout-step-one.html.
     * Quy trình:
     * 1. Nhập thông tin hợp lệ (An / Nguyen / 700000), nhấn Continue.
     * 2. Mong đợi (theo đặc tả, như standard_user): sang checkout-step-two.html và
     * Total = $32.39.
     * 
     * Lưu ý: Test này PHẢI FAIL vì problem_user bị lỗi trường Last Name nên hệ
     * thống
     * hiện lỗi validate thay vì chuyển trang. Assertion KHÔNG được đảo để test
     * PASS.
     */
    @Test
    @Tag("known-bug")
    @DisplayName("TC-CHK-BUG03 [BUG-03]: problem_user can continue past checkout step one with valid data")
    void problemUserCanContinueCheckoutStepOne() {
        new LoginPage(driver, baseUrl).open().login("problem_user", "secret_sauce");
        new InventoryPage(driver).addToCart("sauce-labs-backpack").openCart();
        CheckoutPage checkoutPage = new CartPage(driver).checkout();
        checkoutPage.fillInformation("An", "Nguyen", "700000");

        // continueToOverviewAndReadTotal() ném RuntimeException khi hiện lỗi validate;
        // assertDoesNotThrow biến nó thành FAIL (không phải ERROR) kèm mã BUG-03.
        String total = assertDoesNotThrow(checkoutPage::continueToOverviewAndReadTotal,
                "BUG-03: valid data was rejected for problem_user at checkout step one");

        assertTrue(checkoutPage.isAtOverviewPage(), "BUG-03: URL phải là checkout-step-two.html");
        assertEquals("Total: $32.39", total);
    }
}