/**
 * @file ScreenshotWatcher.java
 * @brief JUnit 5 Extension tự động chụp ảnh màn hình khi một
 *        test case FAIL hoặc xảy ra lỗi không mong đợi (ERROR).
 *
 * Cách sử dụng — thêm annotation vào class test:
 * <pre>
 *   {@literal @}ExtendWith(ScreenshotWatcher.class)
 *   class MyTest extends WebTestBase { ... }
 * </pre>
 *
 * Ảnh được lưu vào thư mục:
 *   target/screenshots/<TestClassName>/<testMethodName>_<timestamp>_<STATUS>.png
 *
 * Extension lấy {@link org.openqa.selenium.WebDriver} qua field {@code driver}
 * của {@link WebTestBase} (kế thừa protected). Nếu test class không kế thừa
 * WebTestBase hoặc driver là null tại thời điểm fail, extension bỏ qua (không
 * ném lỗi phụ).
 */

package vn.edu.softwaretesting.core;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotWatcher implements AfterTestExecutionCallback, TestWatcher {

    /** Thư mục gốc lưu ảnh (tương đối với working directory khi chạy Maven). */
    private static final String SCREENSHOTS_DIR = "target/screenshots";

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ScreenshotWatcher.class);

    // -----------------------------------------------------------------------
    // Execution & Watcher callbacks
    // -----------------------------------------------------------------------

    /**
     * Chạy NGAY SAU KHI @Test kết thúc nhưng TRƯỚC KHI @AfterEach đóng trình duyệt.
     * Đây là thời điểm WebDriver vẫn còn sống để chụp ảnh nếu test bị FAIL.
     */
    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            takeScreenshot(context, "FAILED");
            context.getStore(NAMESPACE).put("screenshot_captured", true);
        }
    }

    /**
     * Được gọi khi test FAIL (assertion không thỏa mãn).
     * Chỉ chụp nếu chưa được chụp ở afterTestExecution.
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        Boolean captured = context.getStore(NAMESPACE).get("screenshot_captured", Boolean.class);
        if (Boolean.TRUE.equals(captured)) {
            return;
        }
        takeScreenshot(context, "FAILED");
    }

    /**
     * Được gọi khi test ném ngoại lệ không phải AssertionError (lỗi runtime).
     */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        Boolean captured = context.getStore(NAMESPACE).get("screenshot_captured", Boolean.class);
        if (Boolean.TRUE.equals(captured)) {
            return;
        }
        takeScreenshot(context, "ABORTED");
    }

    // testSuccessful / testDisabled không cần chụp ảnh.

    // -----------------------------------------------------------------------
    // Core screenshot logic
    // -----------------------------------------------------------------------

    /**
     * Lấy WebDriver từ test instance và chụp ảnh màn hình.
     *
     * @param context ngữ cảnh test JUnit 5
     * @param status  nhãn trạng thái ("FAILED" hoặc "ABORTED")
     */
    private void takeScreenshot(ExtensionContext context, String status) {
        WebDriver driver = resolveDriver(context);
        if (driver == null) return;

        if (!(driver instanceof TakesScreenshot ts)) return;

        try {
            byte[] png = ts.getScreenshotAs(OutputType.BYTES);

            String className  = context.getTestClass()
                    .map(Class::getSimpleName).orElse("UnknownClass");
            String methodName = context.getTestMethod()
                    .map(m -> m.getName()).orElse("unknownMethod");
            String timestamp  = LocalDateTime.now().format(TS_FMT);

            // target/screenshots/<ClassName>/<method>_<timestamp>_FAILED.png
            Path dir = Paths.get(SCREENSHOTS_DIR, className);
            Files.createDirectories(dir);

            Path file = dir.resolve(
                    sanitize(methodName) + "_" + timestamp + "_" + status + ".png");
            Files.write(file, png);

            System.out.printf("[ScreenshotWatcher] %s → %s%n", status, file.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[ScreenshotWatcher] Không thể lưu ảnh: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Reflection helper – lấy field `driver` từ WebTestBase
    // -----------------------------------------------------------------------

    /**
     * Truy cập field {@code driver} (protected) từ instance test thông qua
     * reflection. Hỗ trợ kế thừa nhiều cấp.
     *
     * @param context ngữ cảnh test JUnit 5
     * @return instance WebDriver, hoặc null nếu không tìm thấy / chưa khởi tạo
     */
    private WebDriver resolveDriver(ExtensionContext context) {
        Optional<Object> testInstance = context.getTestInstance();
        if (testInstance.isEmpty()) return null;

        Object instance = testInstance.get();
        Class<?> clazz = instance.getClass();

        // Đi ngược lên cây kế thừa để tìm field `driver`
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField("driver");
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value instanceof WebDriver wd) return wd;
            } catch (NoSuchFieldException e) {
                // Thử lớp cha tiếp theo
            } catch (IllegalAccessException e) {
                System.err.println("[ScreenshotWatcher] Không thể truy cập field driver: "
                        + e.getMessage());
                return null;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Loại bỏ ký tự không hợp lệ trong tên file (giữ lại chữ, số, dấu _, -).
     *
     * @param name chuỗi gốc
     * @return chuỗi an toàn để dùng làm tên file
     */
    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
