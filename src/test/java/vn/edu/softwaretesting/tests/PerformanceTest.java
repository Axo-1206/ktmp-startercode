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
import static org.junit.jupiter.api.Assertions.fail;
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
     * {@value #SAMPLES} lần; mỗi lần mở một trình duyệt mới (phiên sạch). Thời gian
     * gõ username/password không được tính. Nếu đăng nhập không phản hồi thì thử
     * lại
     * 1 lần (ghi log lần bị loại).
     *
     * @param username    tài khoản cần đo
     * @param mustSucceed true: không đo được mẫu sau 2 lần thử thì test FAIL;
     *                    false (mẫu đối chiếu): bỏ qua mẫu đó
     * @return danh sách thời gian (ms) của các lần đo hợp lệ
     */
    private List<Long> measureLoginMillis(String username, boolean mustSucceed) {
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < SAMPLES; i++) {
            final int sample = i + 1;
            Long millis = null;
            String lastProblem = "";

            for (int attempt = 1; attempt <= 2 && millis == null; attempt++) {
                restartBrowser();
                LoginPage loginPage = new LoginPage(driver, baseUrl).open()
                        .enterCredentials(username, "secret_sauce");

                long start = System.nanoTime();
                var result = loginPage.submit();
                long elapsed = Duration.ofNanos(System.nanoTime() - start).toMillis();

                if (result.accepted()) {
                    millis = elapsed;
                } else {
                    lastProblem = "url=" + result.currentUrl() + ", message=" + result.message();
                    System.out.printf("[TC-NFR-01] %s sample %d attempt %d discarded: %s%n",
                            username, sample, attempt, lastProblem);
                }
            }

            if (millis != null) {
                samples.add(millis);
            } else if (mustSucceed) {
                fail(username + " could not log in (sample " + sample + ") after 2 attempts: " + lastProblem);
            }
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
     * 2. Đo 5 lần với performance_glitch_user, mỗi lần mở trình duyệt mới, bấm giờ
     * từ lúc nhấn Login.
     * 3. Tính trung vị 5 mẫu và kiểm tra <= 3000 ms.
     * 
     * Lưu ý: Test này PHẢI FAIL vì performance_glitch_user bị làm chậm có chủ đích
     * (trung vị khoảng 5,1 s > 3 s). Không nới ngưỡng để test PASS.
     */
    @Test
    @Tag("known-bug")
    @DisplayName("TC-NFR-01 [BUG-01]: median of 5 inventory loads within 3 s")
    void inventoryLoadsWithinThreeSeconds() {
        List<Long> standard = measureLoginMillis("standard_user", false);
        System.out.printf("[TC-NFR-01] standard_user samples=%s ms, median=%s%n", standard,
                standard.isEmpty() ? "n/a" : median(standard) + " ms");

        List<Long> glitch = measureLoginMillis("performance_glitch_user", true);
        long glitchMedian = median(glitch);
        System.out.printf("[TC-NFR-01] performance_glitch_user samples=%s ms, median=%d ms%n", glitch, glitchMedian);

        assertTrue(glitchMedian <= LIMIT_MS,
                () -> "performance_glitch_user median " + glitchMedian + " ms (samples " + glitch
                        + ") exceeds NFR-01 limit of " + LIMIT_MS + " ms");
    }
}