*Read this in other languages: [Vietnamese](README-vi.md)*

# 28Care - Polyclinic Management System

**28Care** is a modern full-stack clinic management system designed to optimize healthcare facility operations and enhance the patient experience. The system utilizes a robust Spring Boot Backend, a smooth Angular Frontend, and an integrated AI Assistant (using the RAG model) for automated medical consultation.

## 🌐 Demo Link & Access
* **Live Application:** [https://clinic-frontend-43k7jjz71-nam-anhs-projects-224564cd.vercel.app/](https://clinic-frontend-43k7jjz71-nam-anhs-projects-224564cd.vercel.app/)

**Demo Accounts (Password: `thanghoi`):**

| Role | Email | Permissions |
| :--- | :--- | :--- |
| **Admin** | `admin@clinic.com` | Full system access, View revenue statistics, Personnel management |
| **Doctor** | `doctor@clinic.com` | Manage work schedules, Appointments, Write prescriptions |
| **Receptionist** | `receptionist@clinic.com` | Manage patient bookings and general operations |
| **Patient** | `patient@clinic.com` | Book appointments, View history, Chat with AI |

---

## Key Features

### For Patients
* **Smart Appointment Booking:** Easy online booking; the system automatically sends a confirmation email upon successful scheduling.
* **AI Medical Assistant (RAG):** A smart chatbot integrated with Google Gemini and a Vector DB (Qdrant) to provide medical consultation and answer basic health questions.
* **Personal Profile Management:** View and edit personal information, track payment and appointment history, and review prescribed medications.
* **Security:** Secure account management and password changing.

### For Administrators & Staff
* **Statistics & Finance:** Intuitive dashboard to manage revenue and track clinic metrics.
* **Personnel & User Management:** Centralized management of user accounts, doctors, and clinic staff.
* **Category Management:** Add, edit, delete (CRUD) medications in inventory, medical specialties, and services.
* **Schedule Management:** Intuitive calendar interface to arrange doctor work schedules and patient appointments.
* **Electronic Medical Records (EMR):** Digitized management of medical records, examination history, and patient prescriptions.

---

## Technologies Used

### Backend
* **Core:** Java 21, Spring Boot 3.5.7
* **Security:** Spring Security, OAuth2 Resource Server, Nimbus JOSE JWT
* **Database Access:** Spring Data JPA, Hibernate, MySQL Connector
* **AI & LLM:** LangChain4j (0.35.0), Gemini API, Qdrant Vector Database
* **Utilities:** MapStruct 1.6.2, Lombok, Apache POI, Spring Boot Mail

### Frontend
* **Core:** Angular 21.0.5, TypeScript 5.9.2
* **UI/UX Frameworks:** PrimeNG 17.18.11, PrimeFlex 4.0.0, PrimeIcons
* **Charts:** Chart.js 4.5.1

### Infrastructure & Cloud
* **Database Containerization:** Docker
* **Image Storage:** Cloudinary (HTTP44)

---

## System Architecture & Database

The project is organized using a Monorepo model, including separate directories for the backend API (`clinic-management`) and the frontend interface (`clinic-frontend`).

## Installation Guide

### System Requirements
* Java Development Kit (JDK) 21
* Node.js & npm
* Docker & Docker Compose (to run the Database)
* MySQL (if not using Docker)

### Installation Steps

**1. Run Database (via Docker)**
Start containers for MySQL and Qdrant:
```bash
docker-compose up -d
```

**2. Setup Backend (`clinic-management`)**
Navigate to the backend directory, configure environment variables (Database credentials, Gemini API key, Cloudinary keys) in the `application.yaml` file, and run the application:
```bash
cd clinic-management
./mvnw clean install
./mvnw spring-boot:run
```

**3. Setup Frontend (`clinic-frontend`)**
Navigate to the frontend directory, install necessary dependencies, and start the development server:
```bash
cd clinic-frontend
npm install
npm start
```