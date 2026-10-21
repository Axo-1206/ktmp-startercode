/**
 * @file CartTest.java
 * @brief Chứa các test case kiểm thử chức năng Giỏ hàng (FR-02).
 * Bao gồm kiểm thử giá trị biên (BVA) cho số lượng badge và các Use Case (UC).
 */

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
            "test.allthethings()-t-shirt-(red)");

    /**
     * Phương thức tiện ích hỗ trợ đăng nhập với tài khoản người dùng tiêu chuẩn
     * (standard_user).
     *
     * @return đối tượng {@link InventoryPage} sau khi đăng nhập thành công
     */
    private InventoryPage loginAsStandardUser() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        return new InventoryPage(driver);
    }

    /**
     * Nguồn cung cấp dữ liệu kiểm thử giá trị biên (BVA) cho số lượng sản phẩm
     * trong giỏ hàng.
     *
     * @return luồng {@link Stream} chứa các bộ đối số kiểm thử gồm mã TC, mô tả, số
     *         lượng thêm, số lượng xóa, số badge mong đợi
     */
    static Stream<Arguments> cartBoundaryProvider() {
        return Stream.of(
                Arguments.of("TC-CART-B1", "0 (min): Login without adding items", 0, 0, 0),
                Arguments.of("TC-CART-B2", "1 (min+1): Add 1 item", 1, 0, 1),
                Arguments.of("TC-CART-B3", "5 (max-1): Add 6 items then remove 1", 6, 1, 5),
                Arguments.of("TC-CART-B4", "6 (max): Add all 6 items", 6, 0, 6),
                Arguments.of("TC-CART-B5", "1 -> 0: Add 1 item then remove it", 1, 1, 0));
    }

    /**
     * TC-CART-B1, TC-CART-B2, TC-CART-B3, TC-CART-B4, TC-CART-B5.
     * 
     * Quy trình:
     * 1. Đăng nhập bằng standard_user.
     * 2. Thực hiện thêm (addCount) sản phẩm và xóa (removeCount) sản phẩm theo kịch
     * bản.
     * 3. Chờ và kiểm tra số lượng hiển thị trên badge giỏ hàng khớp với
     * expectedBadge.
     * 
     * @param tcId          mã định danh test case
     * @param description   mô tả ca kiểm thử
     * @param addCount      số lượng sản phẩm thêm vào giỏ
     * @param removeCount   số lượng sản phẩm xóa khỏi giỏ
     * @param expectedBadge số lượng hiển thị mong đợi trên badge giỏ hàng
     */
    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("cartBoundaryProvider")
    void testCartBadgeBoundaryValues(String tcId, String description, int addCount, int removeCount,
            int expectedBadge) {
        InventoryPage inventoryPage = loginAsStandardUser();

        // Bước 1: Thêm sản phẩm
        for (int i = 0; i < addCount; i++) {
            inventoryPage.addToCart(ALL_PRODUCTS.get(i));
        }
        // Bước 2: Xóa sản phẩm
        for (int i = 0; i < removeCount; i++) {
            inventoryPage.removeFromCart(ALL_PRODUCTS.get(i));
        }

        // Bước 3: Kiểm tra badge
        int actualBadge = inventoryPage.waitForBadgeCount(expectedBadge);
        assertEquals(expectedBadge, actualBadge, tcId + ": Cart badge count mismatch!");
    }

    /**
     * TC-CART-UC1.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ rỗng, đang ở inventory.html.
     * Quy trình:
     * 1. Nhấn Add to cart ở Sauce Labs Backpack.
     * 2. Kiểm tra nút đổi thành Remove và badge = 1.
     * 3. Nhấn biểu tượng giỏ hàng.
     * 4. Kiểm tra URL cart.html, giỏ có đúng 1 dòng: Sauce Labs Backpack, $29.99.
     */
    @Test
    @DisplayName("TC-CART-UC1: Add item from Inventory -> button becomes Remove, badge=1, cart.html lists Backpack $29.99")
    void testAddItemAndViewCart() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart("sauce-labs-backpack");

        assertEquals(1, inventoryPage.waitForBadgeCount(1));
        assertTrue(inventoryPage.isRemoveButtonVisible("sauce-labs-backpack"), "Nút phải đổi thành Remove sau khi Add");

        CartPage cartPage = inventoryPage.openCart();
        assertTrue(cartPage.isAtCartPage(), "URL phải là cart.html");
        assertEquals(1, cartPage.getItemCount());
        assertEquals(List.of("Sauce Labs Backpack"), cartPage.getItemNames());
        assertEquals(List.of("$29.99"), cartPage.getItemPrices());
    }

    /**
     * TC-CART-UC2.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, đã add Sauce Labs Backpack.
     * Quy trình:
     * 1. Nhấn Remove trực tiếp trên trang Inventory.
     * 2. Kiểm tra badge biến mất.
     * 3. Kiểm tra nút đổi lại thành Add to cart.
     */
    @Test
    @DisplayName("TC-CART-UC2: Add item then click Remove directly on Inventory page clears badge")
    void testRemoveDirectlyOnInventory() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart("sauce-labs-backpack");
        assertEquals(1, inventoryPage.waitForBadgeCount(1));
        assertTrue(inventoryPage.isRemoveButtonVisible("sauce-labs-backpack"), "Nút Remove phải hiển thị sau khi Add");

        inventoryPage.removeFromCart("sauce-labs-backpack");
        assertEquals(0, inventoryPage.waitForBadgeCount(0));
        assertTrue(inventoryPage.isAddToCartButtonVisible("sauce-labs-backpack"), "Nút phải đổi lại thành Add to cart");
    }

    /**
     * TC-CART-UC3.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ có Sauce Labs Backpack, đang
     * ở cart.html.
     * Quy trình:
     * 1. Nhấn Remove ở Sauce Labs Backpack trong trang giỏ hàng.
     * 2. Kiểm tra danh sách giỏ trống và badge biến mất.
     */
    @Test
    @DisplayName("TC-CART-UC3: Remove the only item inside cart.html -> cart list empty and badge disappears")
    void testRemoveItemInsideCartPage() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage.addToCart("sauce-labs-backpack").openCart();
        assertEquals(1, cartPage.getItemCount());

        cartPage.removeItem("sauce-labs-backpack");

        assertTrue(cartPage.isCartEmpty(), "Giỏ hàng phải trống sau khi xóa");
        assertTrue(cartPage.isBadgeHidden(), "Badge phải biến mất khi giỏ trống");
    }

    /**
     * TC-CART-UC4.
     * 
     * Tiền điều kiện: Đã đăng nhập standard_user, giỏ có 2 sản phẩm (N = 2), đang ở
     * cart.html.
     * Quy trình:
     * 1. Ghi nhận badge (N) và tên các sản phẩm trong giỏ.
     * 2. Nhấn Continue Shopping.
     * 3. Kiểm tra URL inventory.html và badge vẫn = N.
     * 4. Mở lại giỏ hàng, kiểm tra vẫn đúng các sản phẩm đã thêm.
     */
    @Test
    @DisplayName("TC-CART-UC4: Continue Shopping keeps the cart (URL inventory.html, badge=N, same items on return)")
    void testContinueShoppingKeepsCart() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart("sauce-labs-backpack").addToCart("sauce-labs-bike-light");
        int badgeBefore = inventoryPage.waitForBadgeCount(2);

        CartPage cartPage = inventoryPage.openCart();
        List<String> namesBefore = cartPage.getItemNames();

        inventoryPage = cartPage.continueShopping();
        assertTrue(inventoryPage.isAtInventoryPage(), "URL phải là inventory.html");
        assertEquals(badgeBefore, inventoryPage.waitForBadgeCount(badgeBefore));

        assertEquals(namesBefore, inventoryPage.openCart().getItemNames());
    }

    /**
     * TC-CART-BUG02 (Known Bug).
     * 
     * Tiền điều kiện: Đăng nhập problem_user.
     * Quy trình:
     * 1. Thêm Sauce Labs Backpack (badge = 1).
     * 2. Nhấn Remove trên trang Inventory.
     * 3. Kiểm tra badge phải về 0.
     * 
     * Lưu ý: Test này PHẢI FAIL vì problem_user bị lỗi không xóa được sản phẩm.
     */
    @Test
    @Tag("known-bug")
    @DisplayName("TC-CART-BUG02 [BUG-02]: problem_user cannot remove added item from Inventory page")
    void problemUserCannotRemoveItemFromInventory() {
        new LoginPage(driver, baseUrl).open().login("problem_user", "secret_sauce");
        InventoryPage inventoryPage = new InventoryPage(driver);

        inventoryPage.addToCart("sauce-labs-backpack");
        assertEquals(1, inventoryPage.waitForBadgeCount(1));

        inventoryPage.removeFromCart("sauce-labs-backpack");
        // Hệ thống có lỗi nên badge vẫn là 1 thay vì về 0 -> Test này PHẢI FAIL đúng
        // theo quy trình NV5
        assertEquals(0, inventoryPage.getBadgeCount(),
                "BUG-02: Remove button on Inventory page did not work for problem_user");
    }
}