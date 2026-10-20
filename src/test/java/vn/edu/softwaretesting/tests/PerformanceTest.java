/**
 * @file PerformanceTest.java
 * @brief Chứa test case kiểm thử phi chức năng về hiệu năng đăng nhập (NFR-01).
 * Đo thời gian phản hồi của tài khoản performance_glitch_user.
 */

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

    /**
     * TC-NFR-01 (Known Bug).
     * 
     * Tiền điều kiện: Chưa đăng nhập, mạng ổn định.
     * Quy trình:
     * 1. Mở trang đăng nhập.
     * 2. Bắt đầu bấm giờ (System.nanoTime()).
     * 3. Đăng nhập bằng tài khoản performance_glitch_user.
     * 4. Dừng bấm giờ khi trang inventory hiển thị.
     * 5. Kiểm tra đăng nhập thành công và thời gian <= 3000 ms.
     * 
     * Lưu ý: Test này PHẢI FAIL vì performance_glitch_user bị làm chậm có chủ đích
     * (> 3s).
     */
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