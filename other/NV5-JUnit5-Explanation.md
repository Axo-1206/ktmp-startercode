# Giải thích về JUnit 5 & Các công cụ Tự động hóa (NV5)

Tài liệu này giải thích chi tiết về kiến trúc **JUnit 5**, các **Annotation (thuộc tính)** được sử dụng trong mã nguồn, các **thư viện bên ngoài (External Tools)** và trả lời câu hỏi: *"Nếu không dùng JUnit 5 thì có phải chạy thủ công không?"*.

---

## 1. JUnit 5 là gì? Tại sao phải dùng nó?

**JUnit 5** là framework kiểm thử tự động phổ biến nhất cho Java. Nó cung cấp một môi trường để:
*   **Viết test case** dưới dạng các hàm Java (thay vì làm bằng tay trên Excel).
*   **Tự động chạy** tất cả các test case cùng lúc.
*   **Tự động so sánh** kết quả thực tế với kết quả mong đợi (Assertions).
*   **Tự động tạo báo cáo** (PASS/FAIL) sau khi chạy xong.

Nếu **không có JUnit 5** (hoặc các framework tương tự như TestNG), bạn sẽ phải:
1.  Mở trình duyệt bằng tay.
2.  Nhập dữ liệu, click từng nút.
3.  Dùng mắt để nhìn xem kết quả có đúng không.
4.  Tự ghi chép lại PASS/FAIL vào file Excel.
5.  Lặp lại quy trình này cho hàng chục test case khác nhau.

**Kết luận:** JUnit 5 giúp tự động hóa hoàn toàn quy trình trên, tiết kiệm thời gian và giảm sai sót do con người gây ra.

---

## 2. Các Annotation (Thuộc tính) của JUnit 5 được sử dụng

Dưới đây là bảng liệt kê các annotation quan trọng có trong mã nguồn của bạn và chức năng của chúng:

| Annotation           | Vị trí sử dụng                         | Chức năng                                                                                                    | Ví dụ trong code                                              |
| :------------------- | :------------------------------------- | :----------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------ |
| `@Test`              | Trước một hàm                          | Đánh dấu hàm đó là một test case độc lập. JUnit sẽ tự động chạy hàm này.                                     | `void testContinueShoppingFromCart()`                         |
| `@BeforeEach`        | Trước một hàm                          | Hàm này sẽ **tự động chạy trước mỗi** test case. Dùng để khởi tạo môi trường (như mở trình duyệt).           | `void openBrowser()` trong `WebTestBase`                      |
| `@AfterEach`         | Trước một hàm                          | Hàm này sẽ **tự động chạy sau mỗi** test case. Dùng để dọn dẹp (như đóng trình duyệt).                       | `void closeBrowser()` trong `WebTestBase`                     |
| `@ParameterizedTest` | Trước một hàm                          | Đánh dấu hàm đó là test case **chạy lặp lại nhiều lần** với các bộ dữ liệu khác nhau.                        | `void loginShouldMatchExpectedOutcome(...)` trong `LoginTest` |
| `@DisplayName`       | Trước class hoặc hàm                   | Đặt tên hiển thị cho test case (thay vì tên hàm khó đọc). Thường dùng để ghi **Mã TC** (ví dụ: `TC-CHK-01`). | `@DisplayName("TC-CHK-01: ...")`                              |
| `@Tag`               | Trước class hoặc hàm                   | Gắn nhãn cho test case (ví dụ: `"web"`, `"known-bug"`). Dùng để lọc test khi chạy bằng Maven.                | `@Tag("known-bug")` trong `PerformanceTest`                   |
| `@CsvFileSource`     | Trước tham số của `@ParameterizedTest` | Đọc dữ liệu từ file CSV (bên ngoài) để làm tham số đầu vào cho test.                                         | `resources = "/login-data.csv"`                               |
| `@CsvSource`         | Trước tham số của `@ParameterizedTest` | Cung cấp dữ liệu trực tiếp dưới dạng chuỗi CSV ngay trong code.                                              | `"TC-CHK-02, '', Nguyen, ..."`                                |
| `@MethodSource`      | Trước tham số của `@ParameterizedTest` | Cung cấp dữ liệu từ một hàm Java khác (trả về `Stream<Arguments>`).                                          | `cartBoundaryProvider()` trong `CartTest`                     |

---

## 3. Các thành phần cốt lõi khác của JUnit 5

Ngoài Annotation, mã nguồn còn sử dụng các class có sẵn của JUnit 5:

| Thành phần                                              | Chức năng                                                          | Ví dụ                                               |
| :------------------------------------------------------ | :----------------------------------------------------------------- | :-------------------------------------------------- |
| `Assertions.assertEquals(expected, actual)`             | So sánh 2 giá trị có bằng nhau không. Nếu không bằng -> Test FAIL. | `assertEquals(1, cartPage.getItemCount())`          |
| `Assertions.assertTrue(condition)`                      | Kiểm tra điều kiện có đúng không. Nếu sai -> Test FAIL.            | `assertTrue(cartPage.isAtCartPage())`               |
| `Assertions.assertFalse(condition)`                     | Kiểm tra điều kiện có sai không. Nếu đúng -> Test FAIL.            | `assertFalse(cartPage.isCartEmpty())`               |
| `Assertions.assertThrows(Exception.class, () -> {...})` | Kiểm tra xem một đoạn code có ném ra ngoại lệ như mong đợi không.  | (Dùng trong test `problemUserFailsCheckoutStepOne`) |

---

## 4. Các External Tools (Thư viện bên ngoài) được sử dụng

Ngoài JUnit 5, dự án của bạn còn sử dụng các thư viện mã nguồn mở khác (được khai báo trong `pom.xml`):

### 4.1. Selenium WebDriver 4.27
*   **Mục đích:** Điều khiển trình duyệt (Chrome, Firefox) tự động.
*   **Các class/thành phần được dùng trong code:**
    *   `WebDriver`: Interface điều khiển trình duyệt (mở trang, click, nhập liệu).
    *   `ChromeDriver`, `FirefoxDriver`: Các driver cụ thể cho từng trình duyệt.
    *   `By`: Dùng để tìm phần tử trên trang (By.id, By.cssSelector).
    *   `WebElement`: Đại diện cho một phần tử HTML (nút bấm, ô nhập).
    *   `JavascriptExecutor`: Thực thi mã JavaScript (dùng cho hàm `jsClick`).
    *   `WebDriverWait` & `ExpectedConditions`: **Explicit Wait** - Chờ cho đến khi một điều kiện xảy ra (ví dụ: chờ nút hiển thị, chờ URL thay đổi) thay vì dùng `Thread.sleep` (gây chậm và không ổn định).

### 4.2. Maven Surefire Plugin 3.5.2
*   **Mục đích:** Đây là "nhạc trưởng" của Maven. Nó tự động tìm tất cả các file Test trong dự án, chạy chúng, và tạo báo cáo kết quả.
*   **Cấu hình trong `pom.xml`:** Truyền các tham số `baseUrl`, `browser`, `headless` vào code Java thông qua `systemPropertyVariables`.

### 4.3. Selenium Manager (Tích hợp sẵn trong Selenium 4.27)
*   **Mục đích:** Tự động tải và quản lý phiên bản ChromeDriver/FirefoxDriver phù hợp với trình duyệt đang cài trên máy. Bạn không cần phải tải driver thủ công.

### 4.4. Java CSV (Không dùng thư viện ngoài, dùng JUnit tích hợp)
*   **Mục đích:** Đọc file `login-data.csv`. JUnit 5 có sẵn annotation `@CsvFileSource` để làm việc này, không cần thêm thư viện CSV nào khác.

---

## 5. Trả lời câu hỏi: "Nếu không sử dụng JUnit 5 thì có phải chạy thủ công không?"

**Đúng vậy.** Nếu không có JUnit 5 (hoặc một framework tương tự), bạn sẽ không có:
*   Cơ chế `@BeforeEach` / `@AfterEach` để tự động mở/đóng trình duyệt.
*   Cơ chế `@ParameterizedTest` để chạy 1 hàm với 10 bộ dữ liệu khác nhau.
*   Hệ thống `Assertions` để tự động so sánh kết quả.
*   Cơ chế `@Tag` để lọc các test case bị lỗi (known-bug) ra khỏi bộ test chính.

**Tuy nhiên**, bạn vẫn có thể tự động hóa một phần bằng cách viết một hàm `main()` trong Java, gọi đến Selenium WebDriver để điều khiển trình duyệt. Nhưng bạn sẽ phải tự viết logic so sánh, tự quản lý việc mở/đóng trình duyệt, tự tạo báo cáo... JUnit 5 sinh ra để giải quyết tất cả những vấn đề đó một cách chuẩn hóa.

---

## 6. Tóm tắt luồng hoạt động của JUnit 5 trong dự án

1.  Bạn gõ lệnh `mvn clean test`.
2.  **Maven Surefire** quét thư mục `src/test/java` và tìm thấy các class có chứa `@Test`.
3.  Với mỗi class Test, JUnit 5 thực hiện:
    *   Chạy hàm `@BeforeEach` (Mở trình duyệt Chrome).
    *   Chạy hàm `@Test` (Thực hiện kịch bản test, dùng Selenium để click/nhập liệu).
    *   Chạy các `Assertions` (So sánh kết quả). Nếu sai -> Ghi nhận FAIL.
    *   Chạy hàm `@AfterEach` (Đóng trình duyệt).
4.  Lặp lại bước 3 cho tất cả các test case và các bộ dữ liệu của `@ParameterizedTest`.
5.  Xuất báo cáo ra thư mục `target/surefire-reports/`.
