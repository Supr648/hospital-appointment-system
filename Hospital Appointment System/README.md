# MediCare — Hospital Appointment System

A realistic full-stack hospital appointment management system built with **Spring Boot 3**, **Thymeleaf**, **JPA/H2**, and a modern animated UI.

## Features

### Core
- **Dashboard** — live stats, upcoming appointments, today's schedule
- **Doctors** — register, edit, search, toggle availability, experience & qualification
- **Patients** — register with age, gender, blood group, address; search & edit
- **Appointments** — book with reason/notes, filter by status, complete / cancel / no-show
- Conflict detection (same doctor + same time)
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

H2 console (optional): http://localhost:8080/h2-console  
- JDBC URL: `jdbc:h2:mem:hospitaldb`  
- User: `sa` / password: (empty)

### Switch to MySQL
Edit `src/main/resources/application.properties` — comment H2 block and uncomment MySQL settings.

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
- H2 (dev) / MySQL (prod)
- Lombok (optional) · Inter font · Pure CSS animations
