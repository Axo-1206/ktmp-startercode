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