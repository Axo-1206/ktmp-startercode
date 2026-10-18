package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

@Tag("web")
@DisplayName("FR-LOGIN: Authentication")
class LoginTest extends WebTestBase {

    private LoginPage loginPage;

    /**
     * Thiết lập tiền điều kiện trước mỗi test case: khởi tạo và mở trang đăng nhập.
     */
    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver, baseUrl).open();
    }

    /**
     * Kiểm thử phân lớp tương đương (EP) và bảng quyết định (DT) cho chức năng đăng nhập đọc dữ liệu từ file CSV.
     *
     * @param username tên tài khoản đăng nhập
     * @param password mật khẩu đăng nhập
     * @param expectedAccepted kết quả đăng nhập mong đợi (true nếu thành công, false nếu thất bại)
     * @param expectedEvidence chuỗi bằng chứng mong đợi xuất hiện trong URL hoặc thông báo lỗi
     */
    @ParameterizedTest(name = "TC-LOGIN-{index}: user={0}, accepted={2}")
    @CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
    void loginShouldMatchExpectedOutcome(
            String username,
            String password,
            boolean expectedAccepted,
            String expectedEvidence) {

        LoginResult actual = loginPage.login(username, password);

        assertEquals(expectedAccepted, actual.accepted(),
                () -> "Unexpected result for user: " + username);
        assertTrue(
                actual.currentUrl().contains(expectedEvidence)
                        || actual.message().contains(expectedEvidence),
                () -> "Expected evidence '" + expectedEvidence
                        + "', but URL/message was: "
                        + actual.currentUrl() + " / " + actual.message());
    }
}
