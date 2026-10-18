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
     * Thiết lập tiền điều kiện trước mỗi test case: đăng nhập tài khoản tiêu chuẩn và mở trang danh mục sản phẩm.
     */
    @BeforeEach
    void loginAndOpenInventory() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        inventoryPage = new InventoryPage(driver);
    }

    /**
     * Kiểm thử chức năng sắp xếp sản phẩm theo tên (TC-SORT-01 và TC-SORT-02: A-Z và Z-A).
     *
     * @param tcId mã định danh test case
     * @param option giá trị value của tùy chọn sắp xếp ("az" hoặc "za")
     * @param ascending true nếu sắp xếp tăng dần theo bảng chữ cái, false nếu giảm dần
     */
    @ParameterizedTest(name = "{0}: sort by Name option={1} (ascending={2})")
    @CsvSource({
            "TC-SORT-01, az, true",
            "TC-SORT-02, za, false"
    })
    void sortByName(String tcId, String option, boolean ascending) {
        List<String> actual = inventoryPage.sortBy(option).names();
        List<String> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(expected, actual, tcId + ": Product names are not sorted correctly!");
    }

    /**
     * Kiểm thử chức năng sắp xếp sản phẩm theo giá (TC-SORT-03 và TC-SORT-04: thấp-cao và cao-thấp).
     *
     * @param tcId mã định danh test case
     * @param option giá trị value của tùy chọn sắp xếp ("lohi" hoặc "hilo")
     * @param ascending true nếu sắp xếp giá từ thấp đến cao, false nếu từ cao xuống thấp
     */
    @ParameterizedTest(name = "{0}: sort by Price option={1} (ascending={2})")
    @CsvSource({
            "TC-SORT-03, lohi, true",
            "TC-SORT-04, hilo, false"
    })
    void sortByPrice(String tcId, String option, boolean ascending) {
        List<Double> actual = inventoryPage.sortBy(option).prices();
        List<Double> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(expected, actual, tcId + ": Product prices are not sorted correctly!");
    }
}