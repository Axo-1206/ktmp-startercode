/**
 * @file PerformanceTest.java
 * @brief Chứa test case kiểm thử phi chức năng về hiệu năng đăng nhập (NFR-01).
 * Đo thời gian đăng nhập 5 lần, lấy trung vị; đối chiếu standard_user với
 * performance_glitch_user.
 */

package vn.edu.softwaretesting.tests;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;

@DisplayName("NFR-01: Login Performance")
class PerformanceTest extends WebTestBase {

    private static final int SAMPLES = 5;
    private static final long LIMIT_MS = 3000;

    /**
     * Đo thời gian từ lúc nhấn Login đến khi trang sản phẩm hiển thị, lặp
     * {@value #SAMPLES} lần; mỗi lần bắt đầu từ một phiên sạch (xóa cookie).
     *
     * @param loginPage trang đăng nhập đã mở
     * @param username  tài khoản cần đo
     * @return danh sách thời gian (ms) của từng lần đo
     */
    private List<Long> measureLoginMillis(LoginPage loginPage, String username) {
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < SAMPLES; i++) {
            loginPage.resetSession();

            long start = System.nanoTime();
            var result = loginPage.login(username, "secret_sauce");
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();

            assertTrue(result.accepted(), username + " should be able to log in (sample " + (i + 1) + ")");
            samples.add(millis);
        }
        return samples;
    }

    /**
     * Tính trung vị của danh sách số đo.
     *
     * @param samples danh sách thời gian đo (ms), số phần tử lẻ
     * @return giá trị trung vị (ms)
     */
    private static long median(List<Long> samples) {
        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    /**
     * TC-NFR-01 (Known Bug).
     * 
     * Tiền điều kiện: Chưa đăng nhập, mạng ổn định.
     * Quy trình:
     * 1. Đo 5 lần với standard_user (đối chiếu, chỉ ghi log).
     * 2. Đo 5 lần với performance_glitch_user, mỗi lần từ phiên sạch.
     * 3. Tính trung vị 5 mẫu và kiểm tra <= 3000 ms.
     * 
     * Lưu ý: Test này PHẢI FAIL vì performance_glitch_user bị làm chậm có chủ đích
     * (trung vị khoảng 5,1 s > 3 s). Không nới ngưỡng để test PASS.
     */
    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: median of 5 inventory loads within 3 s")
    void inventoryLoadsWithinThreeSeconds() {
        LoginPage loginPage = new LoginPage(driver, baseUrl).open();

        List<Long> standard = measureLoginMillis(loginPage, "standard_user");
        System.out.printf("[TC-NFR-01] standard_user samples=%s ms, median=%d ms%n", standard, median(standard));

        List<Long> glitch = measureLoginMillis(loginPage, "performance_glitch_user");
        long glitchMedian = median(glitch);
        System.out.printf("[TC-NFR-01] performance_glitch_user samples=%s ms, median=%d ms%n", glitch, glitchMedian);

        assertTrue(glitchMedian <= LIMIT_MS,
                () -> "performance_glitch_user median " + glitchMedian + " ms (samples " + glitch
                        + ") exceeds NFR-01 limit of " + LIMIT_MS + " ms");
    }
}