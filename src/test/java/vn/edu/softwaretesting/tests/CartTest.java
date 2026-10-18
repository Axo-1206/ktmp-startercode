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

    /**
     * Phương thức tiện ích hỗ trợ đăng nhập với tài khoản người dùng tiêu chuẩn (standard_user).
     *
     * @return đối tượng {@link InventoryPage} sau khi đăng nhập thành công
     */
    private InventoryPage loginAsStandardUser() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        return new InventoryPage(driver);
    }

    /**
     * Nguồn cung cấp dữ liệu kiểm thử giá trị biên (BVA) cho số lượng sản phẩm trong giỏ hàng.
     *
     * @return luồng {@link Stream} chứa các bộ đối số kiểm thử gồm mã TC, mô tả, số lượng thêm, số lượng xóa, số badge mong đợi
     */
    static Stream<Arguments> cartBoundaryProvider() {
        return Stream.of(
                Arguments.of("TC-CART-B1", "0 (min): Login without adding items", 0, 0, 0),
                Arguments.of("TC-CART-B2", "1 (min+1): Add 1 item", 1, 0, 1),
                Arguments.of("TC-CART-B3", "5 (max-1): Add 6 items then remove 1", 6, 1, 5),
                Arguments.of("TC-CART-B4", "6 (max): Add all 6 items", 6, 0, 6),
                Arguments.of("TC-CART-B5", "1 -> 0: Add 1 item then remove it", 1, 1, 0)
        );
    }

    /**
     * Kiểm thử giá trị biên (BVA) của số lượng hiển thị trên badge giỏ hàng (TC-CART-B1 đến B5).
     *
     * @param tcId mã định danh test case
     * @param description mô tả ca kiểm thử
     * @param addCount số lượng sản phẩm thêm vào giỏ
     * @param removeCount số lượng sản phẩm xóa khỏi giỏ
     * @param expectedBadge số lượng hiển thị mong đợi trên badge giỏ hàng
     */
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

    /**
     * Kiểm thử use case tiếp tục mua hàng từ giỏ hàng (TC-CART-UC1):
     * Thêm sản phẩm, mở giỏ hàng, nhấn 'Continue Shopping' và kiểm tra số badge được giữ nguyên.
     */
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

    /**
     * Kiểm thử use case xóa sản phẩm trực tiếp trên trang Inventory (TC-CART-UC2):
     * Thêm sản phẩm rồi nhấn nút Remove trực tiếp trên trang Inventory, kiểm tra badge biến mất.
     */
    @Test
    @DisplayName("TC-CART-UC2: Add item then click Remove directly on Inventory page clears badge")
    void testRemoveDirectlyOnInventory() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart("sauce-labs-backpack");
        assertEquals(1, inventoryPage.waitForBadgeCount(1));

        inventoryPage.removeFromCart("sauce-labs-backpack");
        assertEquals(0, inventoryPage.waitForBadgeCount(0));
    }

    /**
     * Kiểm thử use case xóa sản phẩm từ bên trong giỏ hàng (TC-CART-UC3):
     * Thêm 2 sản phẩm, mở trang giỏ hàng, xóa 1 sản phẩm và kiểm tra số lượng còn lại là 1.
     */
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

    /**
     * Kiểm thử mở giỏ hàng khi đang rỗng (TC-CART-UC4):
     * Mở giỏ hàng rỗng, kiểm tra số lượng là 0 và nhấn 'Continue Shopping' trở về Inventory an toàn.
     */
    @Test
    @DisplayName("TC-CART-UC4: Open empty cart shows 0 items and Continue Shopping works")
    void testOpenEmptyCartAndContinueShopping() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage.openCart();

        assertEquals(0, cartPage.getItemCount());
        inventoryPage = cartPage.continueShopping();
        assertEquals(0, inventoryPage.getBadgeCount());
    }

    /**
     * Kiểm thử lỗi hệ thống đã biết BUG-02 với tài khoản problem_user (TC-CART-BUG02):
     * problem_user không thể xóa sản phẩm khỏi trang Inventory (nút Remove không hoạt động).
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
        // Hệ thống có lỗi nên badge vẫn là 1 thay vì về 0 -> Test này PHẢI FAIL đúng theo quy trình NV5
        assertEquals(0, inventoryPage.getBadgeCount(), "BUG-02: Remove button on Inventory page did not work for problem_user");
    }
}