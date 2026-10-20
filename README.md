# Assignment 1 — JUnit 5/Selenium starter

Đây là mã minh họa đi kèm đề bài, không phải lời giải hoàn chỉnh. Sinh viên phải
thay Page Object, dữ liệu và test theo đúng hệ thống đã đăng ký.

## Yêu cầu

- JDK 17 trở lên
- Maven 3.9 trở lên
- Chrome hoặc Firefox

Selenium Manager sẽ tự tìm hoặc tải driver tương thích trong lần chạy đầu.

## Chạy test

```bash
mvn clean test
```

Chạy có giao diện trên Chrome:

```bash
mvn test -Dheadless=false -Dbrowser=chrome
```

Chạy Firefox với một hệ thống khác:

```bash
mvn test \
  -Dbrowser=firefox \
  -DbaseUrl=http://localhost:8080 \
  -Dheadless=true
```

Kết quả XML và log được tạo trong `target/surefire-reports/`.

## Những điểm mẫu đang minh họa

- JUnit Jupiter lifecycle (`@BeforeEach`, `@AfterEach`)
- `@ParameterizedTest` và dữ liệu ngoài bằng `@CsvFileSource`
- Page Object tách locator/thao tác khỏi assertion
- explicit wait thay cho `Thread.sleep`
- cấu hình URL, trình duyệt và chế độ headless qua system property

Tài khoản trong CSV là dữ liệu kiểm thử công khai của
[SauceDemo](https://www.saucedemo.com/). Không dùng thông tin đăng nhập thật
trong mã nguồn hoặc tệp dữ liệu.
# Assignment 1 - Kiểm Thử Hộp Đen & Tự Động Hóa Web với JUnit 5 (SauceDemo)

Dự án kiểm thử tự động hệ thống [SauceDemo](https://www.saucedemo.com) sử dụng mô hình **Page Object Model (POM)**, **JUnit 5 (Jupiter)** và **Selenium WebDriver 4**.

---

## 1. Môi trường & Công nghệ sử dụng
* **Java:** JDK 17+ (Khuyến nghị JDK 21)
* **Maven:** 3.8+
* **Thư viện kiểm thử:** JUnit 5.11, Selenium WebDriver 4.27, Maven Surefire 3.5.2
* **Trình duyệt đã kiểm thử:** Google Chrome (version 155.0.8059.82) trên Windows 11

---

## 2. Cấu trúc thư mục mã nguồn
```text
src/test/
├── java/vn/edu/softwaretesting/
│   ├── core/
│   │   └── WebTestBase.java       # Quản lý khởi tạo và đóng WebDriver (@BeforeEach / @AfterEach)
│   ├── pages/
│   │   ├── LoginPage.java         # Page Object trang Đăng nhập (FR-01)
│   │   ├── InventoryPage.java     # Page Object trang Sản phẩm & Sắp xếp (FR-02, FR-04)
│   │   ├── CartPage.java          # Page Object trang Giỏ hàng (FR-02, FR-03)
│   │   └── CheckoutPage.java      # Page Object quy trình Thanh toán 3 bước (FR-03)
│   └── tests/
│       ├── LoginTest.java         # Kiểm thử FR-01 (EP & Decision Table từ CSV)
│       ├── CartTest.java          # Kiểm thử FR-02 (BVA, Use Case & BUG-02)
│       ├── CheckoutTest.java      # Kiểm thử FR-03 (Use Case, Decision Table, BVA Thuế & BUG-03)
│       ├── SortTest.java          # Kiểm thử FR-04 (4 kiểu sắp xếp tên và giá)
│       └── PerformanceTest.java   # Kiểm thử NFR-01 (Đo hiệu năng đăng nhập <= 3s & BUG-01)
└── resources/
    └── login-data.csv             # Dữ liệu kiểm thử tham số hóa cho LoginTest
```

---

## 3. Hướng dẫn chạy Test (Dành cho NV4 & NV5)

### 3.1. Chạy toàn bộ bộ test (Mặc định)
Lệnh này sẽ chạy **tất cả** các test case (bao gồm cả các test case bị lỗi known-bug). Kết quả sẽ có một số test FAIL đúng như dự kiến.
```bash
mvn clean test
```
*Lưu ý:* Mặc định chạy trên Chrome ở chế độ headless (không hiện giao diện).

### 3.2. Chạy loại trừ các Test Case bị lỗi (Known Bugs)
Theo quy trình NV4/NV5, các test case liên quan đến lỗi hệ thống đã biết (BUG-01, BUG-02, BUG-03) được gắn tag `@Tag("known-bug")`. Để chạy bộ test sạch (chỉ các test case PASS), sử dụng lệnh:
```bash
mvn test -DexcludedGroups=known-bug
```
Lệnh này sẽ bỏ qua các test case:
* `TC-NFR-01` (BUG-01: Hiệu năng chậm)
* `TC-CART-BUG02` (BUG-02: problem_user không xóa được sản phẩm)
* `TC-CHK-BUG03` (BUG-03: problem_user lỗi Last Name)

### 3.3. Chạy riêng các Test Case kiểm tra lỗi (Known Bugs)
Để chạy riêng các test case thuộc nhóm known-bug (phục vụ việc thu thập bằng chứng lỗi):
```bash
mvn test -Dgroups=known-bug
```

### 3.4. Chạy cụ thể từng Test Class (Theo yêu cầu NV4)
Bạn có thể chạy riêng từng nhóm chức năng bằng cách chỉ định tên class:

* **Chạy kiểm thử Đăng nhập (FR-01):**
  ```bash
  mvn test -Dtest=LoginTest
  ```
* **Chạy kiểm thử Giỏ hàng (FR-02):**
  ```bash
  mvn test -Dtest=CartTest
  ```
* **Chạy kiểm thử Thanh toán (FR-03):**
  ```bash
  mvn test -Dtest=CheckoutTest
  ```
* **Chạy kiểm thử Sắp xếp (FR-04):**
  ```bash
  mvn test -Dtest=SortTest
  ```
* **Chạy kiểm thử Hiệu năng (NFR-01):**
  ```bash
  mvn test -Dtest=PerformanceTest
  ```

### 3.5. Chạy một Test Case cụ thể (Dựa vào Mã TC trong @DisplayName)
Nếu bạn muốn chạy đúng một kịch bản (ví dụ: `TC-CHK-01`), hãy dùng cờ `-Dtest=TenClass#tenHam`:
```bash
# Chạy test case TC-CHK-01
mvn test -Dtest=CheckoutTest#mainFlowShowsCorrectTotalAndFinishes
```

### 3.6. Chạy với giao diện (Không headless) để quan sát
Nếu bạn cần quay video hoặc chụp ảnh bằng chứng (evidence) cho NV4:
```bash
mvn test -Dheadless=false -Dbrowser=chrome
```
Hoặc chạy trên Firefox:
```bash
mvn test -Dbrowser=firefox -Dheadless=false
```

---

## 4. Xem báo cáo kết quả (Surefire Reports)
Sau khi chạy lệnh `mvn test`, kết quả chi tiết (bao gồm cả các test case FAIL) sẽ được lưu tại:
```text
target/surefire-reports/
```
* Các file `.txt` và `.xml` chứa log chi tiết của từng test case.
* Bạn có thể mở file `.txt` để xem lý do tại sao test case `known-bug` bị FAIL (ví dụ: thời gian tải trang vượt quá 3000ms, hoặc badge không biến mất) để làm bằng chứng báo cáo lỗi (BUG-01, BUG-02, BUG-03) cho NV4.

---

>[!NOTE]
> * **Dữ liệu kiểm thử:** Tài khoản trong file `login-data.csv` là dữ liệu công khai của SauceDemo. Không sử dụng thông tin đăng nhập thật.
> * **Known Bugs:** Các test case có tag `known-bug` **bắt buộc phải FAIL**. Nếu chúng PASS, có nghĩa là hệ thống đã được fix bug hoặc script của bạn > đang bị sai logic. Tuyệt đối không sửa Assertion để ép các test này PASS.
> * **Evidence:** Để nộp bài NV4, hãy chụp ảnh màn hình kết quả chạy test (giao diện Selenium) và lưu vào thư mục `evidence/NV4/`.