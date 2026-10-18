package vn.edu.softwaretesting.tests;

import java.util.List;
import java.util.stream.Stream;

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

    private CheckoutPage startCheckoutWithSingleBackpack() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        new InventoryPage(driver).addToCart("sauce-labs-backpack").openCart();
        return new CartPage(driver).checkout();
    }

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

    @ParameterizedTest(name = "{0}: first=''{1}'', last=''{2}'', postal=''{3}'' -> {4}")
    @CsvSource({
            "TC-CHK-02, '', Nguyen, 700000, Error: First Name is required",
            "TC-CHK-03, An, '', 700000, Error: Last Name is required",
            "TC-CHK-04, An, Nguyen, '', Error: Postal Code is required",
            "TC-CHK-DT1, '', '', '', Error: First Name is required",
            "TC-CHK-DT2, An, '', '', Error: Last Name is required"
    })
    void missingFieldShowsExpectedValidationError(String id, String first, String last,
                                                  String postal, String expected) {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        String message = checkoutPage
                .fillInformation(first, last, postal)
                .continueExpectingError();

        assertTrue(message.contains(expected), () -> id + " expected [" + expected + "] but got: [" + message + "]");
    }

    @Test
    @DisplayName("TC-CHK-05: Cancel at checkout step one returns to cart.html with item preserved")
    void cancelAtStepOneReturnsToCartWithItem() {
        CheckoutPage checkoutPage = startCheckoutWithSingleBackpack();
        CartPage cartPage = checkoutPage.cancelAtStepOne();

        assertTrue(cartPage.isAtCartPage());
        assertEquals(1, cartPage.getItemCount());
    }

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
                        "Item total: $33.97", "Tax: $2.72", "Total: $36.69")
        );
    }

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

    @Test
    @Tag("known-bug")
    @DisplayName("TC-CHK-BUG03 [BUG-03]: problem_user cannot proceed past checkout step one due to Last Name field bug")
    void problemUserFailsCheckoutStepOne() {
        new LoginPage(driver, baseUrl).open().login("problem_user", "secret_sauce");
        new InventoryPage(driver).addToCart("sauce-labs-backpack").openCart();
        CheckoutPage checkoutPage = new CartPage(driver).checkout();

        // Với problem_user, nhập Last Name bị ghi đè vào First Name dẫn tới báo lỗi Last Name is required
        String total = checkoutPage
                .fillInformation("An", "Nguyen", "700000")
                .continueToOverviewAndReadTotal();
        assertEquals("Total: $32.39", total);
    }
}