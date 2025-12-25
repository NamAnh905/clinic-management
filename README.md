# 28Care - Clinic Management System

Dự án Hệ thống quản lý phòng khám (Clinic Management System), phục vụ đồ án tốt nghiệp.

## Công nghệ sử dụng
* **Backend:** Spring Boot (Java)
* **Frontend:** Angular
* **Database:** MySQL (dự kiến)

## Cấu trúc dự án
Dự án được tổ chức theo mô hình Monorepo:
* `backend/`: Chứa source code Spring Boot API.
* `frontend/`: Chứa source code giao diện Angular.

## Hướng dẫn cài đặt (Cơ bản)

### Backend
1.  Di chuyển vào thư mục backend.
2.  Cấu hình Database trong `application.properties`.
3.  Chạy ứng dụng bằng lệnh `./mvnw spring-boot:run` hoặc dùng IDE.

### Frontend
1.  Di chuyển vào thư mục frontend: `cd frontend`
2.  Cài đặt thư viện: `npm install`
3.  Chạy ứng dụng: `npm start`