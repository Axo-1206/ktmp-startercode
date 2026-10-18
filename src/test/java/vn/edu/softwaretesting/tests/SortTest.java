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

    @BeforeEach
    void loginAndOpenInventory() {
        new LoginPage(driver, baseUrl).open().login("standard_user", "secret_sauce");
        inventoryPage = new InventoryPage(driver);
    }

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