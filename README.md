# 28Care - Hệ thống Quản lý Phòng khám đa khoa

**28Care** là một hệ thống quản lý phòng khám full-stack hiện đại, được thiết kế để tối ưu hóa quy trình vận hành của cơ sở y tế và nâng cao trải nghiệm của bệnh nhân. Hệ thống sử dụng Backend Spring Boot mạnh mẽ, Frontend Angular mượt mà và tích hợp Trợ lý AI (sử dụng mô hình RAG) để tư vấn khám bệnh tự động.

## 🌐 Link Demo & Truy cập
* **Live Application:** [https://clinic-frontend-43k7jjz71-nam-anhs-projects-224564cd.vercel.app/](https://clinic-frontend-43k7jjz71-nam-anhs-projects-224564cd.vercel.app/)

**Tài khoản Demo (Mật khẩu: `thanghoi`):**

| Role | Email | Quyền hạn |
| :--- | :--- | :--- |
| **Admin** | `admin@clinic.com` | Toàn quyền hệ thống, Xem thống kê doanh thu, Quản lý nhân sự |
| **Bác sĩ** | `doctor@clinic.com` | Quản lý lịch làm việc, Lịch khám, Kê đơn thuốc |
| **Lễ tân** | `receptionist@clinic.com` | Quản lý đặt lịch của bệnh nhân và vận hành chung |
| **Bệnh nhân** | `patient@clinic.com` | Đặt lịch khám, Xem lịch sử, Chat với AI |

---

## Tính năng chính

### Dành cho Bệnh nhân
* **Đặt lịch khám thông minh:** Đặt lịch trực tuyến dễ dàng, hệ thống tự động gửi email xác nhận thông tin cuộc hẹn khi đặt thành công.
* **Trợ lý y tế AI (RAG):** Chatbot thông minh tích hợp Google Gemini và Vector DB (Qdrant) giúp tư vấn khám bệnh và trả lời các câu hỏi sức khỏe cơ bản.
* **Quản lý hồ sơ cá nhân:** Xem và chỉnh sửa thông tin cá nhân, theo dõi lịch sử thanh toán, lịch sử đặt khám và xem lại các đơn thuốc đã kê.
* **Bảo mật:** Quản lý tài khoản và thay đổi mật khẩu an toàn.

### Dành cho Quản trị viên & Nhân viên
* **Thống kê & Tài chính:** Dashboard trực quan để quản lý doanh thu và theo dõi số liệu phòng khám.
* **Quản lý Nhân sự & Người dùng:** Quản lý tập trung tài khoản người dùng, bác sĩ, và nhân viên phòng khám.
* **Quản lý Danh mục:** Thêm, sửa, xóa (CRUD) thuốc trong kho, các chuyên khoa, và dịch vụ khám bệnh.
* **Quản lý Lịch trình:** Giao diện lịch trực quan để sắp xếp lịch làm việc cho bác sĩ và lịch hẹn của bệnh nhân.
* **Bệnh án điện tử:** Quản lý số hóa hồ sơ bệnh án, lịch sử khám và đơn thuốc của bệnh nhân.

---

## Công nghệ sử dụng

### Backend
* **Core:** Java 21, Spring Boot 3.5.7
* **Bảo mật:** Spring Security, OAuth2 Resource Server, Nimbus JOSE JWT
* **Database Access:** Spring Data JPA, Hibernate, MySQL Connector
* **AI & LLM:** LangChain4j (0.35.0), Gemini API, Qdrant Vector Database
* **Tiện ích:** MapStruct 1.6.2, Lombok, Apache POI, Spring Boot Mail

### Frontend
* **Core:** Angular 21.0.5, TypeScript 5.9.2
* **UI/UX Frameworks:** PrimeNG 17.18.11, PrimeFlex 4.0.0, PrimeIcons
* **Biểu đồ:** Chart.js 4.5.1

### Infrastructure & Cloud
* **Đóng gói Database:** Docker
* **Lưu trữ hình ảnh:** Cloudinary (HTTP44)

---

## Kiến trúc Hệ thống & Database

Dự án được tổ chức theo mô hình Monorepo, bao gồm các thư mục riêng biệt cho API backend (`clinic-management`) và giao diện frontend (`clinic-frontend`).

## Hướng dẫn Cài đặt

### Yêu cầu hệ thống
* Java Development Kit (JDK) 21
* Node.js & npm
* Docker & Docker Compose (để chạy Database)
* MySQL (nếu không dùng Docker)

### Các bước Cài đặt

**1. Khởi chạy Database (qua Docker)**
Khởi động container cho MySQL và Qdrant:
```bash
docker-compose up -d

**2. Cài đặt Backend (`clinic-management`)**
Di chuyển vào thư mục backend, cấu hình các biến môi trường (Thông tin Database, Gemini API key, Cloudinary keys) trong file `application.yaml`,và chạy ứng dụng:
```bash
cd clinic-management
./mvnw clean install
./mvnw spring-boot:run

**3. Cài đặt Frontend (clinic-frontend)**
Di chuyển vào thư mục frontend, cài đặt các thư viện cần thiết và khởi chạy server phát triển:
```bash
cd clinic-frontend
npm install
npm start