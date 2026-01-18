-- =======================================================
-- TẮT KIỂM TRA KHÓA NGOẠI ĐỂ TRÁNH LỖI INSERT
-- =======================================================
SET FOREIGN_KEY_CHECKS = 0;
SET NAMES 'utf8mb4';

-- =======================================================
-- 1. DỮ LIỆU CẤU HÌNH (ROLES, PERMISSIONS)
-- =======================================================

-- 1.1 Tạo Roles
-- Table: role
DELETE FROM role;
INSERT INTO role (name, description) VALUES 
('ADMIN','Quản trị viên'),
('DOCTOR','Bác sĩ'),
('PATIENT','Bệnh nhân'),
('RECEPTIONIST','Lễ tân');

-- 1.2 Tạo Permissions
-- Table: permission
DELETE FROM permission;
INSERT INTO permission (name, description) VALUES 
('CREATE_APPOINTMENT','Tạo cuộc hẹn'),
('CREATE_INVOICE','Tạo Hóa đơn'),
('CREATE_MEDICAL_RECORD','Tạo Hồ sơ bệnh án'),
('CREATE_PRESCRIPTION','Tạo Đơn thuốc'),
('CREATE_SCHEDULE','Tạo lịch làm việc'),
('DELETE_PRESCRIPTION','Xóa đơn thuốc'),
('FULL_ACCESS','Toàn bộ Hệ thống'),
('READ_ALL_PATIENT','Xem danh sách Bệnh nhân'),
('READ_APPOINTMENT','Xem tất cả cuộc hẹn'),
('READ_DOCTOR','Xem danh sách Bác sĩ'),
('READ_DRUG','Xem danh mục Thuốc'),
('READ_INVOICE','Xem tất cả Hóa đơn'),
('READ_MEDICAL_RECORD','Xem tất cả Hồ sơ bệnh án'),
('READ_OWN_APPOINTMENT','Xem các cuộc hẹn của chính mình'),
('READ_OWN_INVOICE','Xem hóa đơn của chính mình'),
('READ_OWN_MEDICAL_RECORD','Xem HSBA của chính mình'),
('READ_OWN_PRESCRIPTION','Xem đơn thuốc của chính mình'),
('READ_PRESCRIPTION','Xem tất cả Đơn thuốc'),
('READ_SCHEDULE','Xem lịch làm việc'),
('READ_SERVICE','Xem danh mục Dịch vụ'),
('READ_SPECIALTY','Xem danh mục Chuyên khoa'),
('UPDATE_APPOINTMENT','Cập nhật cuộc hẹn'),
('UPDATE_INVOICE','Cập nhật Hóa đơn'),
('UPDATE_MEDICAL_RECORD','Cập nhật Hồ sơ bệnh án'),
('UPDATE_PRESCRIPTION','Cập nhật Đơn thuốc');

-- 1.3 Phân quyền cho Role
-- Table: role_permissions
DELETE FROM role_permissions;
INSERT INTO role_permissions (role_name, permissions_name) VALUES 
('ADMIN','FULL_ACCESS'),
('DOCTOR','CREATE_APPOINTMENT'),('DOCTOR','CREATE_MEDICAL_RECORD'),('DOCTOR','CREATE_PRESCRIPTION'),
('DOCTOR','READ_ALL_PATIENT'),('DOCTOR','READ_DRUG'),('DOCTOR','READ_MEDICAL_RECORD'),
('DOCTOR','READ_PRESCRIPTION'),('DOCTOR','READ_SCHEDULE'),('DOCTOR','READ_SERVICE'),
('DOCTOR','UPDATE_MEDICAL_RECORD'),('DOCTOR','UPDATE_PRESCRIPTION'),
('PATIENT','CREATE_APPOINTMENT'),('PATIENT','READ_DOCTOR'),('PATIENT','READ_OWN_APPOINTMENT'),
('PATIENT','READ_OWN_INVOICE'),('PATIENT','READ_OWN_MEDICAL_RECORD'),('PATIENT','READ_OWN_PRESCRIPTION'),
('PATIENT','READ_SPECIALTY'),
('RECEPTIONIST','CREATE_APPOINTMENT'),('RECEPTIONIST','CREATE_INVOICE'),('RECEPTIONIST','READ_ALL_PATIENT'),
('RECEPTIONIST','READ_APPOINTMENT'),('RECEPTIONIST','READ_DRUG'),('RECEPTIONIST','READ_INVOICE'),
('RECEPTIONIST','READ_SCHEDULE'),('RECEPTIONIST','UPDATE_APPOINTMENT'),('RECEPTIONIST','UPDATE_INVOICE');

-- =======================================================
-- 2. DỮ LIỆU DANH MỤC (DỊCH VỤ, CHUYÊN KHOA, THUỐC)
-- Lưu ý: Insert Service trước vì Specialty tham chiếu tới Service
-- =======================================================

-- 2.1 Dịch vụ khám
-- Table: services
DELETE FROM services;
INSERT INTO services (service_id, name, price, type, deleted, image) VALUES 
(1,'Khám Nội tổng quát',100000.00,'CONSULTATION',0, NULL),
(2,'Khám Ngoại khoa',200000.00,'CONSULTATION',0, NULL),
(3,'Khám Nhi',150000.00,'CONSULTATION',0, NULL),
(4,'Khám Mắt',200000.00,'CONSULTATION',0, NULL),
(5,'Khám Răng Hàm Mặt',200000.00,'CONSULTATION',0, NULL),
(6,'Chụp X-Quang',500000.00,'PARACLINICAL',0, NULL);

-- 2.2 Chuyên khoa
-- Table: specialties
DELETE FROM specialties;
INSERT INTO specialties (specialty_id, name, description, default_service_id, deleted, image) VALUES 
(1,'Nội tổng quát','Khám các bệnh lý thông thường',1,0, NULL),
(2,'Nhi khoa','Khám và điều trị cho trẻ em',3,0, NULL),
(3,'Mắt (Nhãn khoa)','Điều trị các bệnh về mắt',4,0, NULL),
(4,'Răng Hàm Mặt','Điều trị nha khoa',5,0, NULL),
(5,'Tai Mũi Họng','Điều trị bệnh tai mũi họng',1,0, NULL);

-- 2.3 Danh mục Thuốc
-- Table: drugs
DELETE FROM drugs;
INSERT INTO drugs (drug_id, name, unit, price, stock_quantity, instructions, deleted) VALUES 
(1, 'Paracetamol 500mg','Viên',1000.00,500,'Uống khi sốt hoặc đau, tối đa 4v/ngày',0),
(2, 'Ibuprofen 400mg','Viên',1500.00,300,'Uống sau ăn, giảm đau kháng viêm',0),
(3, 'Amoxicillin 500mg','Viên',1500.00,600,'Kháng sinh, uống theo đơn',0),
(4, 'Vitamin C 500mg','Viên',1500.00,800,'Tăng sức đề kháng',0),
(5, 'Berberin','Lọ',25000.00,100,'Điều trị tiêu hóa',0);

-- =======================================================
-- 3. DỮ LIỆU NGƯỜI DÙNG VÀ NHÂN SỰ (USERS, DOCTORS, PATIENTS)
-- Password mặc định: 123456
-- =======================================================

-- 3.1 Tạo User
-- Table: users
DELETE FROM users;
INSERT INTO users (user_id, email, full_name, password_hash, phone_number, gender, date_of_birth, is_active, address, image) VALUES 
(1, 'admin@clinic.com', 'Administrator', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpFTEzzlctnF9k5ZbUyKhC.JGr3ncQ.y', '0900000001', 'MALE', '1990-01-01', 1, 'Hệ thống', NULL),
(2, 'doctor@clinic.com', 'Bác sĩ Kiểm thử', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpFTEzzlctnF9k5ZbUyKhC.JGr3ncQ.y', '0900000002', 'MALE', '1985-05-05', 1, 'Hà Nội', NULL),
(3, 'patient@clinic.com', 'Nguyễn Văn Bệnh Nhân', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpFTEzzlctnF9k5ZbUyKhC.JGr3ncQ.y', '0900000003', 'FEMALE', '2000-10-10', 1, 'Hồ Chí Minh', NULL),
(4, 'receptionist@clinic.com', 'Lễ Tân Vui Vẻ', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpFTEzzlctnF9k5ZbUyKhC.JGr3ncQ.y', '0900000004', 'FEMALE', '1998-12-12', 1, 'Đà Nẵng', NULL);

-- 3.2 Gán Role cho User
-- Table: users_roles
DELETE FROM users_roles;
INSERT INTO users_roles (user_user_id, roles_name) VALUES 
(1, 'ADMIN'),
(2, 'DOCTOR'),
(3, 'PATIENT'),
(4, 'RECEPTIONIST');

-- 3.3 Thông tin chi tiết Bác sĩ
-- Table: doctors
DELETE FROM doctors;
-- Lưu ý: doctor_id là AUTO_INCREMENT, nhưng ta set cứng ID=1 để dễ quản lý dữ liệu mẫu nếu cần
INSERT INTO doctors (doctor_id, user_id, license_number, specialty_id, employee_code) VALUES 
(1, 2, 'VN-TEST-001', 1, 'DOC001');

-- 3.4 Thông tin chi tiết Bệnh nhân
-- Table: patients
DELETE FROM patients;
INSERT INTO patients (patient_id, user_id, medical_history) VALUES 
(1, 3, 'Không có tiền sử bệnh lý đặc biệt.');

-- 3.5 Thông tin chi tiết Lễ tân
-- Table: receptionists
DELETE FROM receptionists;
INSERT INTO receptionists (receptionist_id, user_id, employee_code, hire_date) VALUES 
(1, 4, 'REC001', '2025-01-01');

-- =======================================================
-- BẬT LẠI KIỂM TRA KHÓA NGOẠI
-- =======================================================
SET FOREIGN_KEY_CHECKS = 1;