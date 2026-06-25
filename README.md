# Java Deserialization Vulnerability — Lab Demo

> ⚠️ **Cảnh báo:** Dự án này được xây dựng **hoàn toàn cho mục đích học tập và nghiên cứu bảo mật**. Toàn bộ lỗ hổng được cố ý tạo ra trong môi trường lab kiểm soát. **Không triển khai lên môi trường production.**

Dự án minh hoạ lỗ hổng **Insecure Deserialization** — một trong OWASP Top 10 — thông qua cơ chế "Remember Me" của Spring Security bị cài đặt sai.

---

## Kiến trúc dự án

```
Java-Deserialization/
├── web-app/      # Ứng dụng Spring Boot có lỗ hổng (nạn nhân)
└── attacker/     # Module tạo payload khai thác (kẻ tấn công)
```

| Module | Vai trò | Port |
|---|---|---|
| `web-app` | Ứng dụng bán hàng đơn giản, dùng cookie Remember Me serialize Java object | `8080` |
| `attacker` | Tạo payload độc hại dạng Base64 để inject vào cookie `user` | — |

---

## Lỗ hổng hoạt động như thế nào?

### Luồng bình thường

Khi user đăng nhập với "Remember Me":

1. `web-app` serialize object `VulnerableRememberMeToken` (chứa username/password) thành bytes
2. Encode Base64 và lưu vào cookie `user`
3. Mỗi request tiếp theo, server **deserialize trực tiếp** cookie này mà không kiểm tra

### Luồng tấn công

```
Attacker tạo payload độc hại
        ↓
Serialize command OS vào VulnerableRememberMeToken.username
        ↓
Encode Base64 → inject vào cookie "user"
        ↓
Server gọi readObject() → thực thi Runtime.getRuntime().exec(username)
        ↓
RCE (Remote Code Execution) thành công
```

## Yêu cầu

| Công cụ | Phiên bản |
|---|---|
| JDK | 17+ |
| Maven | 3.9+ (hoặc dùng `mvnw`) |
| MySQL | 8.0+ |

---

## Cài đặt & Chạy

### 1. Chuẩn bị database

```sql
CREATE DATABASE deserialization_lab CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Cấu hình `web-app`

Tạo hoặc chỉnh file `web-app/src/main/resources/application.properties`:

```properties
spring.application.name=web-app
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/deserialization_lab?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA — để Hibernate tự tạo bảng
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Chạy `web-app`

```bash
cd web-app
./mvnw spring-boot:run       # Linux/macOS
mvnw.cmd spring-boot:run     # Windows
```

Ứng dụng chạy tại: **http://localhost:8080**

Truy cập `/register` để tạo tài khoản, `/login` để đăng nhập (tick "Remember Me").

---

## Thực hiện tấn công

### Bước 1 — Tạo payload

```bash
cd attacker
./mvnw spring-boot:run
```

Module `attacker` sẽ in ra chuỗi Base64 là serialized payload độc hại. Payload mặc định chứa lệnh PowerShell đọc `application.yaml` và gửi nội dung đến webhook.

Để thay đổi lệnh, sửa biến `command` trong `AttackerApplication.java`:

```java
String command = "calc.exe";  // Ví dụ: mở Calculator trên Windows để PoC
```

Rồi chạy lại để sinh payload mới.

### Bước 2 — Inject cookie

Sau khi có chuỗi Base64 từ bước 1, dùng DevTools trình duyệt hoặc Burp Suite:

1. Mở **DevTools → Application → Cookies → localhost:8080**
2. Tìm cookie tên `user`
3. Thay giá trị bằng chuỗi Base64 payload vừa tạo
4. Reload trang bất kỳ cần authentication

### Bước 3 — Quan sát kết quả

Server sẽ deserialize cookie → trigger `readObject()` → thực thi lệnh OS.

---

## Điểm lỗ hổng trong code

**`CustomRememberMeServices.java` — dòng deserialize:**

```java
// KHÔNG AN TOÀN: deserialize trực tiếp input từ client
Object obj = ois.readObject();
```

**`VulnerableRememberMeToken.java` — gadget trigger:**

```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Runtime.getRuntime().exec(username); // ← Thực thi lệnh OS khi deserialize
}
```

## Tài liệu tham khảo

- [OWASP A08:2021 — Software and Data Integrity Failures](https://owasp.org/Top10/A08_2021-Software_and_Data_Integrity_Failures/)
- [ysoserial — Java Deserialization Payload Generator](https://github.com/frohoff/ysoserial)
- [CWE-502: Deserialization of Untrusted Data](https://cwe.mitre.org/data/definitions/502.html)
