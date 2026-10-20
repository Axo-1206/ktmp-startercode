/**
 * @file SortTest.java
 * @brief Chứa các test case kiểm thử chức năng Sắp xếp sản phẩm (FR-04).
 * Kiểm tra 4 kiểu sắp xếp: Name (A-Z, Z-A) và Price (low-high, high-low).
 */

package vn.edu.softwaretesting.tests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("FR-04: Product Sorting")
class SortTest extends WebTestBase {

    private InventoryPage inventoryPage;

    /**
     * Thiết lập tiền điều kiện trước mỗi test case: đăng nhập tài khoản tiêu chuẩn
     * và mở trang danh mục sản phẩm.
     */
    @BeforeEach
    void loginAndOpenInventory() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        inventoryPage = new InventoryPage(driver);
    }

    /**
     * TC-SORT-1, TC-SORT-2.
     * 
     * Quy trình:
     * 1. Chọn kiểu sắp xếp theo tên ("az" hoặc "za").
     * 2. Lấy danh sách tên sản phẩm thực tế.
     * 3. Kiểm tra số lượng sản phẩm vẫn là 6 (tập sản phẩm không đổi).
     * 4. Tự sắp xếp danh sách tên mong đợi và so sánh với thực tế.
     * 
     * @param tcId      mã định danh test case
     * @param option    giá trị value của tùy chọn sắp xếp ("az" hoặc "za")
     * @param ascending true nếu sắp xếp tăng dần theo bảng chữ cái, false nếu giảm
     *                  dần
     */
    @ParameterizedTest(name = "{0}: sort by Name option={1} (ascending={2})")
    @CsvSource({
            "TC-SORT-1, az, true",
            "TC-SORT-2, za, false"
    })
    void sortByName(String tcId, String option, boolean ascending) {
        List<String> actual = inventoryPage.sortBy(option).names();

        // Kiểm tra tập sản phẩm không đổi (FR-04.5)
        assertEquals(6, actual.size(), tcId + ": Số lượng sản phẩm phải là 6");

        List<String> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(expected, actual, tcId + ": Product names are not sorted correctly!");
    }

    /**
     * TC-SORT-3, TC-SORT-4.
     * 
     * Quy trình:
     * 1. Chọn kiểu sắp xếp theo giá ("lohi" hoặc "hilo").
     * 2. Lấy danh sách giá sản phẩm thực tế.
     * 3. Kiểm tra số lượng sản phẩm vẫn là 6 (tập sản phẩm không đổi).
     * 4. Tự sắp xếp danh sách giá mong đợi và so sánh với thực tế.
     * 
     * @param tcId      mã định danh test case
     * @param option    giá trị value của tùy chọn sắp xếp ("lohi" hoặc "hilo")
     * @param ascending true nếu sắp xếp giá từ thấp đến cao, false nếu từ cao xuống
     *                  thấp
     */
    @ParameterizedTest(name = "{0}: sort by Price option={1} (ascending={2})")
    @CsvSource({
            "TC-SORT-3, lohi, true",
            "TC-SORT-4, hilo, false"
    })
    void sortByPrice(String tcId, String option, boolean ascending) {
        List<Double> actual = inventoryPage.sortBy(option).prices();

        // Kiểm tra tập sản phẩm không đổi (FR-04.5)
        assertEquals(6, actual.size(), tcId + ": Số lượng sản phẩm phải là 6");

        List<Double> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(expected, actual, tcId + ": Product prices are not sorted correctly!");
    }
}