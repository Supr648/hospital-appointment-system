# MediCare — Hospital Appointment System

A realistic full-stack hospital appointment management system built with **Spring Boot 3**, **Thymeleaf**, **MySQL/JPA**, **MongoDB activity history**, and a modern animated UI.

## Features

### Core
- **Dashboard** — live stats, upcoming appointments, today's schedule
- **Doctors** — register, edit, search, toggle availability, experience & qualification
- **Patients** — register with age, gender, blood group, address; search & edit
- **Appointments** — book with reason/notes, search and filter, reschedule, complete / cancel / no-show
- Appointment activity history stored in MongoDB; operational records remain in MySQL
- Conflict detection for scheduled visits (same doctor + same time)
- Past-date validation
- Only available doctors shown when booking

### Architecture (realistic structure)
```
controller/   → WebController (UI) + REST API controllers
service/      → Business logic layer
repository/   → Spring Data JPA
entity/       → Doctor, Patient, Appointment
config/       → DataLoader (sample data on startup)
```

### UI / UX
- Sidebar admin layout
- Smooth page & card animations
- Color transitions & hover effects
- Animated stat counters
- Toast notifications
- Status badges & filter pills
- Responsive design
- Button ripple effect

## Run

### Requirements
- Java 17+
- Maven 3.8+

```bash
cd "Hospital Appointment System"
mvn spring-boot:run
```

Open: **http://localhost:8080**

The app uses the MySQL connection configured in `src/main/resources/application.properties`.

### Configure MongoDB Atlas
Set `MONGODB_URI` to your Atlas connection string. Include a database name such as `hospital_activity` in the URI path. Do not commit credentials.

PowerShell:
```powershell
$env:MONGODB_URI = "mongodb+srv://USER:PASSWORD@CLUSTER.mongodb.net/hospital_activity?retryWrites=true&w=majority"
mvn spring-boot:run
```

For deployment, add `MONGODB_URI` as a private environment variable in the hosting provider settings. Atlas must allow network access from the deployed application.

## Sample data
On first run, 5 doctors, 5 patients and 5 appointments are loaded automatically.

## REST API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/doctors` | List / search doctors |
| GET | `/api/patients` | List / search patients |
| GET | `/api/appointments` | List / filter appointments |
| POST | `/api/doctors` | Create doctor |
| PUT | `/api/doctors/{id}` | Update doctor |
| DELETE | `/api/doctors/{id}` | Delete doctor |
| … | similar for patients & appointments | |

## Tech stack
- Spring Boot 3.3 · Spring Web · Spring Data JPA · Thymeleaf · Validation
- MySQL for doctors, patients, and appointments; MongoDB for appointment activity
- Lombok (optional) · Inter font · Pure CSS animations
