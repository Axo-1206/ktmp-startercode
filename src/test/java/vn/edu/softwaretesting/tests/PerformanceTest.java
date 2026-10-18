package vn.edu.softwaretesting.tests;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("NFR-01: Login Performance")
class PerformanceTest extends WebTestBase {

    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: inventory loads within 3 s")
    void inventoryLoadsWithinThreeSeconds() {
        LoginPage loginPage = new LoginPage(driver, baseUrl).open();

        long start = System.nanoTime();
        var result = loginPage.login("performance_glitch_user", "secret_sauce");
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        assertTrue(result.accepted(), "User should be able to log in");
        assertTrue(millis <= 3000, () -> "Inventory took " + millis + " ms (exceeds NFR-01 limit of 3000 ms)");
    }
}