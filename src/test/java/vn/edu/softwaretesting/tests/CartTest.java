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
import org.junit.jupiter.params.provider.MethodSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.CartPage;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("FR-02: Cart BVA & Use Cases")
class CartTest extends WebTestBase {

    private static final List<String> ALL_PRODUCTS = List.of(
            "sauce-labs-backpack",
            "sauce-labs-bike-light",
            "sauce-labs-bolt-t-shirt",
            "sauce-labs-fleece-jacket",
            "sauce-labs-onesie",
            "test.allthethings()-t-shirt-(red)"
    );

    private InventoryPage loginAsStandardUser() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        return new InventoryPage(driver);
    }

    static Stream<Arguments> cartBoundaryProvider() {
        return Stream.of(
                Arguments.of("TC-CART-B1", "0 (min): Login without adding items", 0, 0, 0),
                Arguments.of("TC-CART-B2", "1 (min+1): Add 1 item", 1, 0, 1),
                Arguments.of("TC-CART-B3", "5 (max-1): Add 6 items then remove 1", 6, 1, 5),
                Arguments.of("TC-CART-B4", "6 (max): Add all 6 items", 6, 0, 6),
                Arguments.of("TC-CART-B5", "1 -> 0: Add 1 item then remove it", 1, 1, 0)
        );
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("cartBoundaryProvider")
    void testCartBadgeBoundaryValues(String tcId, String description, int addCount, int removeCount, int expectedBadge) {
        InventoryPage inventoryPage = loginAsStandardUser();

        for (int i = 0; i < addCount; i++) {
            inventoryPage.addToCart(ALL_PRODUCTS.get(i));
        }
        for (int i = 0; i < removeCount; i++) {
            inventoryPage.removeFromCart(ALL_PRODUCTS.get(i));
        }

        int actualBadge = inventoryPage.waitForBadgeCount(expectedBadge);
        assertEquals(expectedBadge, actualBadge, tcId + ": Cart badge count mismatch!");
    }

    @Test
    @DisplayName("TC-CART-UC1: Add item, view cart, then Continue Shopping returns to inventory with badge=1")
    void testContinueShoppingFromCart() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage.addToCart("sauce-labs-backpack").openCart();

        assertEquals(1, cartPage.getItemCount());
        assertTrue(cartPage.getItemNames().contains("Sauce Labs Backpack"));

        inventoryPage = cartPage.continueShopping();
        assertEquals(1, inventoryPage.waitForBadgeCount(1));
    }

    @Test
    @DisplayName("TC-CART-UC2: Add item then click Remove directly on Inventory page clears badge")
    void testRemoveDirectlyOnInventory() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart("sauce-labs-backpack");
        assertEquals(1, inventoryPage.waitForBadgeCount(1));

        inventoryPage.removeFromCart("sauce-labs-backpack");
        assertEquals(0, inventoryPage.waitForBadgeCount(0));
    }

    @Test
    @DisplayName("TC-CART-UC3: Add 2 items, open Cart, remove 1 item inside Cart updates count to 1")
    void testRemoveItemInsideCartPage() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage
                .addToCart("sauce-labs-backpack")
                .addToCart("sauce-labs-bike-light")
                .openCart();

        assertEquals(2, cartPage.getItemCount());
        cartPage.removeItem("sauce-labs-bike-light");
        assertEquals(1, cartPage.getItemCount());
    }

    @Test
    @DisplayName("TC-CART-UC4: Open empty cart shows 0 items and Continue Shopping works")
    void testOpenEmptyCartAndContinueShopping() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage.openCart();

        assertEquals(0, cartPage.getItemCount());
        inventoryPage = cartPage.continueShopping();
        assertEquals(0, inventoryPage.getBadgeCount());
    }

    @Test
    @Tag("known-bug")
    @DisplayName("TC-CART-BUG02 [BUG-02]: problem_user cannot remove added item from Inventory page")
    void problemUserCannotRemoveItemFromInventory() {
        new LoginPage(driver, baseUrl).open().login("problem_user", "secret_sauce");
        InventoryPage inventoryPage = new InventoryPage(driver);

        inventoryPage.addToCart("sauce-labs-backpack");
        assertEquals(1, inventoryPage.waitForBadgeCount(1));

        inventoryPage.removeFromCart("sauce-labs-backpack");
        // Hệ thống có lỗi nên badge vẫn là 1 thay vì về 0 -> Test này PHẢI FAIL đúng theo quy trình NV5
        assertEquals(0, inventoryPage.getBadgeCount(), "BUG-02: Remove button on Inventory page did not work for problem_user");
    }
}