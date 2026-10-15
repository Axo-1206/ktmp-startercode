# Kế hoạch thực hiện Assignment 1 (VS Code + Maven)

## 0. Quy ước chung

**Mã định danh** phải giống nhau ở 3 nơi (báo cáo, Excel, `@DisplayName`). Mình đề xuất bộ mã sau:

| Nhóm                                  | Mã                                                          | Số lượng |
| ------------------------------------- | ----------------------------------------------------------- | -------- |
| EP username                           | TC-LOGIN-EP1..EP5                                           | 5        |
| EP password (tự làm thêm)             | TC-LOGIN-EP6..EP8                                           | 3        |
| Decision table login                  | TC-LOGIN-DT1..DT5 (+ DT1b: `""`/`secret_sauce`)             | 5-6      |
| BVA giỏ hàng                          | TC-CART-B1..B5                                              | 5        |
| BVA làm tròn thuế (tự làm thêm)       | TC-TAX-B1..B3                                               | 3        |
| Use case thanh toán                   | TC-CHK-01..05                                               | 5        |
| Use case quản lý giỏ (tự làm thêm)    | TC-CART-UC1..UC4                                            | 4        |
| Decision table checkout (tự làm thêm) | dùng lại TC-CHK-02..04 và thêm TC-CHK-06 (cả 3 trường rỗng) | 1-2      |
| Sắp xếp FR-04                         | TC-SORT-1..4                                                | 4        |
| Hiệu năng                             | TC-NFR-01                                                   | 1        |

Tổng khoảng 35 test case thiết kế, và tất cả đều có thể tự động hóa (đề chỉ yêu cầu ≥ 15).

**Thư mục làm việc trên máy:**
```
group-XX-assignment1/
├── report/            (Overleaf, tải về khi xong)
├── test-cases.xlsx
├── evidence/
│   ├── NV1/  NV4/  NV5/  BUG-01/ ...
└── automation/        (project Maven hiện tại)
```

**Công cụ VS Code:** cài *Extension Pack for Java* (có Test Runner for Java và Maven for Java). Bạn có thể bấm nút ▶ cạnh từng test để chạy riêng, hoặc dùng `mvn test -Dtest=CartTest`.

---

## NV1: Chạy và mở rộng mã mẫu

**Code cần làm**
- Mở `src/test/resources/login-data.csv`, thêm 2 dòng, ví dụ:
  ```
  "",secret_sauce,false,Username is required
  standard_user,"",false,Password is required
  ```
  Dấu `""` là bắt buộc để không bị hiểu thành `null`.
- Chạy lại `mvn clean test`. Kết quả phải là `Tests run: 5`.

**Evidence cần lưu** (`evidence/NV1/`)
- Ảnh chụp terminal sau `mvn clean test` lần đầu (3 test) và sau khi thêm dữ liệu (5 test).
- Ảnh phiên bản: chạy `java -version`, `mvn -v` và lấy phiên bản Chrome (`chrome://version`).
- Ảnh/tệp `login-data.csv` sau khi sửa.

**Viết vào báo cáo:** phiên bản Java/Maven/Chrome/OS, vai trò ngắn gọn của `@BeforeEach`, `@ParameterizedTest`, `@CsvFileSource`.

---

## NV2: Phân tích yêu cầu (không cần code)

**Việc làm**
1. Viết đặc tả FR-01..FR-04 và NFR-01. Bạn có thể bám bảng ở mục 4.1 của đề nhưng nên chia nhỏ thành các quy tắc đánh số được (ví dụ FR-01.1: username phân biệt hoa/thường; FR-03.2: Tax = round(8% × Item total, 2)).
2. Chấm điểm rủi ro P × I cho cả 5 yêu cầu và giải thích lý do. FR-02 và NFR-01 đề chưa chấm, bạn phải tự chấm.
3. Lập ma trận truy vết (yêu cầu → kỹ thuật → test case → lớp test → BUG). Lúc này cột BUG để trống.

**Evidence/Lưu lại:** sheet `Requirements` và `Traceability` trong Excel, cùng bảng rủi ro trong báo cáo.

Lưu ý: ngay lúc này hãy khóa bộ mã TC (bảng ở mục 0), vì các bước sau chỉ cập nhật chứ không đổi mã.

---

## NV3: Thiết kế test case (chưa có code)

**Việc làm.** Mỗi kỹ thuật làm 1 ví dụ của đề cộng 1 phần "tự làm thêm":

| Kỹ thuật | Ví dụ có sẵn          | Phần bạn phải tự làm                                                         |
| -------- | --------------------- | ---------------------------------------------------------------------------- |
| EP       | Username              | EP cho Password (và/hoặc Postal Code)                                        |
| BVA      | Số sản phẩm trong giỏ | BVA cho làm tròn thuế 8% (chọn tổ hợp sản phẩm sao cho 8% × tổng gần xx.xx5) |
| DT       | Login (4 điều kiện)   | DT cho 3 trường First/Last/Postal Code                                       |
| UC       | Checkout              | UC "Quản lý giỏ hàng" kèm sơ đồ hoạt động                                    |

Mỗi test case trong Excel phải có đủ: Mã/Tên, Yêu cầu/Kỹ thuật, Tiền điều kiện, Dữ liệu, bảng Bước – Thao tác – Kết quả mong đợi. Kết quả mong đợi phải tính từ đặc tả (ví dụ tính thuế bằng tay), không chép từ màn hình.

**Gợi ý BVA thuế:** viết một đoạn code nhỏ (JShell hoặc Python) duyệt 63 tổ hợp sản phẩm, tính `tax*100` rồi chọn các tổ hợp có phần thập phân gần 0.5. Giá sản phẩm bạn cần đọc trực tiếp từ website, đừng lấy từ trí nhớ.

**Evidence/Lưu lại**
- Sheet `TestCases` trong Excel (dạng 1 dòng/bước, hoặc 1 dòng/TC kèm cột steps).
- Bảng EP, bảng BVA, bảng quyết định, và 2 sơ đồ hoạt động (vẽ bằng draw.io hoặc TikZ), để đưa vào báo cáo.
- Bảng đếm số TC theo kỹ thuật để đối chiếu với mục 5.5 (tối thiểu 8/8/8/8/1).

---

## NV4: Thực thi thủ công và báo lỗi

**Việc làm.** Chạy tay từng test case trên website, mỗi lần chạy ghi 1 dòng vào sheet `Execution`: Mã TC | Ngày/Người | Môi trường | Kết quả thực tế | Trạng thái (chỉ PASS/FAIL/BLOCKED).

Nên chạy thêm các trường hợp sau, vì đề gợi ý đây là nơi có lỗi:
- TC-CHK-01 và các TC giỏ hàng với `problem_user`.
- TC-NFR-01 với `performance_glitch_user` (5 lần bấm giờ, lấy trung vị).
- Nếu thấy hành vi sai (ví dụ `STANDARD_USER` vẫn đăng nhập được), ghi thành lỗi.

**Mỗi lỗi** là 1 dòng trong sheet `Bugs` với đủ trường: Mã/Tiêu đề, mức nghiêm trọng, môi trường, tiền điều kiện, bước tái hiện, mong đợi, thực tế, bằng chứng. Một lỗi xuất hiện ở nhiều TC thì chỉ ghi 1 BUG, các TC cùng trỏ về mã đó.

**Evidence cần lưu** (`evidence/NV4/` và `evidence/BUG-xx/`)
- Ảnh chụp hoặc quay màn hình cho từng TC FAIL (đặt tên `TC-XXX.png`) và cho từng BUG (`BUG-01.mp4`).
- Với TC-NFR-01: ghi đủ 5 số đo, và ảnh/video có đồng hồ bấm giờ.
- Ảnh chụp thông tin môi trường (phiên bản Chrome, OS).
- Với TC BLOCKED: ghi lý do.

---

## NV5: Tự động hóa bằng JUnit 5

### 5.1 Code cần tạo

```
src/test/java/vn/edu/softwaretesting/
├── pages/  InventoryPage, CartPage, CheckoutPage   (mới)
└── tests/  CartTest, CheckoutTest, SortTest, PerformanceTest   (mới)
            LoginTest   (mở rộng)
```

**Locator SauceDemo thường dùng** (hãy kiểm tra lại bằng DevTools):

| Page      | Phần tử         | Locator gợi ý                                                           |
| --------- | --------------- | ----------------------------------------------------------------------- |
| Inventory | nút Add         | `By.id("add-to-cart-" + slug)`                                          |
| Inventory | nút Remove      | `By.id("remove-" + slug)`                                               |
| Inventory | badge           | `.shopping_cart_badge`                                                  |
| Inventory | link giỏ        | `.shopping_cart_link`                                                   |
| Inventory | dropdown sort   | `.product_sort_container` (values: `az`, `za`, `lohi`, `hilo`)          |
| Inventory | tên / giá       | `.inventory_item_name`, `.inventory_item_price`                         |
| Cart      | Checkout        | `By.id("checkout")`                                                     |
| Checkout  | Cancel / Finish | `By.id("cancel")`, `By.id("finish")`                                    |
| Overview  | tổng tiền       | `.summary_subtotal_label`, `.summary_tax_label`, `.summary_total_label` |
| Complete  | thông báo       | `.complete-header`                                                      |

**Khung `InventoryPage`** (các method trả về `this` để gọi chuỗi, như ví dụ của đề):
```java
public InventoryPage addToCart(String slug)      // click add-to-cart-<slug>
public InventoryPage removeFromCart(String slug) // click remove-<slug>
public Optional<Integer> badgeCount()            // findElements rồi kiểm tra rỗng, KHÔNG dùng findElement
public InventoryPage sortBy(String value)        // new Select(...).selectByValue(value)
public List<String> names()
public List<Double> prices()                     // bỏ ký tự '$' rồi Double.parseDouble
public CartPage openCart()
```
Chú ý `badgeCount()`: khi giỏ rỗng badge không tồn tại, nên dùng `findElements(...).isEmpty()`. Sau khi Remove, dùng `wait.until(ExpectedConditions.invisibilityOfElementLocated(...))`.

### 5.2 Gợi ý test cho từng lớp

| Lớp               | Test                                                                    | Nguồn dữ liệu                                                                   |
| ----------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| `LoginTest`       | EP1..8, DT1..5                                                          | `@CsvFileSource` (mở rộng `login-data.csv`; thêm cột mã TC để đưa vào tên test) |
| `CartTest`        | B1..B5, UC1..UC4                                                        | `@MethodSource` (danh sách slug thêm/xóa) hoặc `@ValueSource`                   |
| `CheckoutTest`    | CHK-01 (`@Test`), CHK-02..04 (`@CsvSource`), CHK-05 (Cancel), TAX-B1..3 | `@CsvSource` / `@MethodSource`                                                  |
| `SortTest`        | SORT-1..4                                                               | `@CsvSource`; tự sắp xếp danh sách đọc được để so sánh                          |
| `PerformanceTest` | NFR-01                                                                  | `@Test` + `@Tag("known-bug")`                                                   |

Điểm đáng chú ý:
- Tính thuế trong test bằng `BigDecimal` với `RoundingMode.HALF_UP`, đừng ghi cứng số từ màn hình.
- Test NFR-01 theo đúng đặc tả là trung vị của 5 lần đo. Mẫu của đề chỉ đo 1 lần, nên bạn có thể cải tiến bằng vòng lặp 5 lần rồi sắp xếp lấy phần tử giữa. Mỗi lần đo cần driver/phiên mới.
- `@DisplayName` hoặc `name=` của `@ParameterizedTest` phải chứa mã TC. Với `@CsvFileSource` thì dùng `name = "{0}: ..."` để mã TC lấy từ cột đầu của CSV.
- Không dùng `Thread.sleep`, chỉ dùng `WebDriverWait`. Trước khi nộp hãy chạy tìm kiếm `Thread.sleep` toàn project.
- Test FAIL do lỗi hệ thống thì gắn `@Tag("known-bug")` và `[BUG-xx]` trong `@DisplayName`. Tuyệt đối không nới assertion cho PASS.

### 5.3 Các lệnh cần chạy

```bash
mvn clean test                              # toàn bộ (known-bug sẽ FAIL, đúng như đề cho phép)
mvn test -DexcludedGroups=known-bug         # phải xanh hoàn toàn
mvn test -Dgroups=known-bug                 # chỉ các test lỗi đã biết
mvn test -Dtest=CheckoutTest                # chạy riêng 1 lớp
mvn test -Dheadless=false                   # xem trình duyệt chạy khi debug
```

### 5.4 Evidence cần lưu (`evidence/NV5/`)
- Ảnh terminal của 3 lệnh đầu ở trên (bắt buộc thấy dòng `Tests run: …`).
- Thư mục `target/surefire-reports/` (giữ lại để đối chiếu, nhưng không nộp `target/`).
- Mỗi test FAIL bạn phải ghi phân loại: lỗi script (đã sửa gì) hay lỗi hệ thống (BUG-xx). Làm thành bảng "FAIL triage" trong báo cáo.
- Cập nhật cột "Lỗi" của ma trận truy vết và cột "Automated?" trong Excel.
- Nếu làm bonus: ảnh `TestWatcher` chụp màn hình khi FAIL, link CI pipeline, artifact surefire.

---

## Mục 8: Sản phẩm nộp (cụ thể hóa)

```
group-XX-assignment1/
├── report.pdf
├── test-cases.xlsx
├── evidence/
└── automation/            (pom.xml, README.md, src/test/...; xóa target/)
```

### report.pdf (Overleaf), mục lục đề xuất

| Chương         | Nội dung                                                         | Evidence cần chèn                                               |
| -------------- | ---------------------------------------------------------------- | --------------------------------------------------------------- |
| Thông tin nhóm | Tên, MSSV, phân công                                             |                                                                 |
| NV1            | Môi trường, vai trò 3 annotation                                 | Ảnh `mvn clean test` (3 và 5 test), phiên bản Java/Maven/Chrome |
| NV2            | Đặc tả, bảng rủi ro, ma trận truy vết                            | Bảng; bản cuối của ma trận                                      |
| NV3            | EP, BVA, DT, 2 sơ đồ UC, bảng đếm                                | Bảng/sơ đồ; một test case mẫu đầy đủ                            |
| NV4            | Bảng thực thi, báo cáo lỗi                                       | Ảnh/video mỗi BUG, ảnh TC FAIL/BLOCKED                          |
| NV5            | Kiến trúc Page Object, bảng ánh xạ TC → method, bảng FAIL triage | Ảnh `mvn test` các chế độ, báo cáo surefire                     |
| Kết luận       | Số TC thiết kế/tự động/PASS/FAIL, bài học                        |                                                                 |
| Phụ lục        | Link GitHub nếu có                                               |                                                                 |

### test-cases.xlsx (5 sheet đúng tên đề yêu cầu)

| Sheet        | Cột gợi ý                                                                                                  |
| ------------ | ---------------------------------------------------------------------------------------------------------- |
| Requirements | Mã, mô tả, quy tắc kiểm tra được, P, I, R                                                                  |
| Traceability | Yêu cầu, kỹ thuật, mã TC, lớp test, mã BUG                                                                 |
| TestCases    | Mã/tên, yêu cầu/kỹ thuật, tiền điều kiện, dữ liệu, bước, kết quả mong đợi, automated (Y/N), method Java    |
| Execution    | Mã TC, ngày/người, môi trường, kết quả thực tế, trạng thái, mã BUG                                         |
| Bugs         | Mã/tiêu đề, nghiêm trọng, môi trường, tiền điều kiện, bước tái hiện, mong đợi, thực tế, đường dẫn evidence |

### evidence/ (đặt tên theo mã)

```
evidence/NV1/ mvn-3tests.png, mvn-5tests.png, versions.png
evidence/NV4/ TC-CHK-01.png, TC-NFR-01-5runs.png, ...
evidence/BUG-01/ BUG-01.mp4, BUG-01-log.txt
evidence/NV5/ mvn-all.png, mvn-exclude-known-bug.png, mvn-known-bug.png
```

### Checklist cuối (mục 9 của đề)
1. Giải nén bản nộp ra thư mục mới, chạy `mvn clean test` được, chỉ test `known-bug` FAIL.
2. Mỗi FR/NFR có ≥ 1 test case, và mỗi TC FAIL có mã BUG.
3. Kết quả mong đợi tính từ đặc tả.
4. Mã TC giống nhau ở Excel, báo cáo và `@DisplayName` (bạn có thể grep mã `TC-` trong `src/` rồi đối chiếu với Excel).
5. Không còn `Thread.sleep`.
6. `README.md` trong `automation/` ghi lệnh chạy và phiên bản Java/Chrome.
7. Không nộp `target/`.

---

## Gợi ý thứ tự làm việc

1. NV1 (nửa buổi), rồi chốt bộ mã TC.
2. NV2 và NV3 làm song song trong Excel, ưu tiên FR-03 (R=15) và FR-01 (R=12) để thiết kế nhiều TC nhất.
3. NV4 chạy tay. Việc này nên làm trước khi viết automation vì bạn sẽ biết trước các lỗi hệ thống.
4. NV5 viết theo thứ tự: `LoginTest` → `InventoryPage` + `CartTest` → `CartPage`/`CheckoutPage` + `CheckoutTest` → `SortTest` → `PerformanceTest`.
5. Cập nhật ma trận truy vết và hoàn thiện báo cáo.

## Mình cần thêm gì từ bạn?

Mình chưa thấy `pom.xml` và `login-data.csv` (bạn có nêu trong danh sách tải lên nhưng nội dung chưa hiện trong tin nhắn). Nếu bạn gửi thêm các tệp đó, cùng template Overleaf, mình có thể:
- kiểm tra cấu hình Surefire để lọc `known-bug`;
- viết sẵn `InventoryPage`, `CartPage`, `CheckoutPage` và các lớp test hoàn chỉnh;
- điền sẵn khung Excel và các bảng EP/BVA/DT bằng LaTeX cho báo cáo.

Bạn muốn mình bắt đầu từ phần nào trước?