# 🏥 HMS Backend — Hospital Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.4-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)

**A production-ready, event-driven Microservices Hospital Management System**  
Built with Spring Boot 3 · Spring Cloud · Kafka · WebSocket · JWT

</div>

---

## 📐 System Architecture

```mermaid
graph TB
    subgraph CLIENT["🌐 Client"]
        FE["⚛️ React Frontend<br/>:3000"]
    end

    subgraph GATEWAY["🔀 API Gateway Layer"]
        GW["🛡️ Gateway MS · :9000<br/>JWT Validation · Route Dispatch<br/>Header Injection"]
    end

    subgraph CORE["⚙️ Microservices"]
        direction LR
        US["👤 User MS<br/>:8080<br/>Auth · JWT · Register"]
        PS["🩺 Profile MS<br/>:8081<br/>Doctor · Patient · Admin"]
        AS["📅 Appointment MS<br/>:8082<br/>Schedule · Records · Rx"]
        PH["💊 Pharmacy MS<br/>:8083<br/>Medicine · Inventory · Sales"]
        NS["🔔 Notification MS<br/>:8085<br/>Kafka Consumer · WebSocket"]
        MD["🖼️ Media MS<br/>:9400<br/>Upload · Avatar · Storage"]
    end

    subgraph INFRA["🏗️ Infrastructure"]
        direction LR
        EU["🔍 Eureka<br/>:8761<br/>Service Registry"]
        KF["📨 Kafka<br/>+ Zookeeper<br/>Event Streaming"]
        RD["⚡ Redis<br/>:6379<br/>Cache · Sessions"]
        DB["🗄️ MySQL 8.0<br/>6 Databases"]
    end

    FE -->|"HTTPS / WS"| GW
    GW -->|"route /user/**"| US
    GW -->|"route /profile/**"| PS
    GW -->|"route /appointment/**"| AS
    GW -->|"route /pharmacy/**"| PH
    GW -->|"route /notifications/**"| NS
    GW -->|"route /media/**"| MD

    US & PS & AS & PH & NS & MD -->|"register"| EU
    GW -->|"discover"| EU

    US & PS & AS & PH & MD --> DB
    NS --> DB

    AS -->|"publish events"| KF
    PH -->|"publish events"| KF
    US -->|"publish events"| KF
    KF -->|"consume events"| NS

    NS -->|"🔔 push"| FE

    style CLIENT fill:#3B82F6,color:#fff,stroke:#2563EB
    style GATEWAY fill:#7C3AED,color:#fff,stroke:#6D28D9
    style CORE fill:#059669,color:#fff,stroke:#047857
    style INFRA fill:#D97706,color:#fff,stroke:#B45309
```

---

## 🧩 Services At a Glance

| # | Service | Port | Database | Key Responsibilities |
|---|---------|------|----------|---------------------|
| 🔍 | **Eureka Server** | `8761` | — | Service discovery & registry |
| 🛡️ | **Gateway MS** | `9000` | — | JWT auth, routing, header injection |
| 👤 | **User MS** | `8080` | `userdb` | Register, login, JWT issuance |
| 🩺 | **Profile MS** | `8081` | `profiledb` | Doctor & Patient profiles, avatar |
| 📅 | **Appointment MS** | `8082` | `appointmentdb` | Scheduling, clinical records, prescriptions |
| 💊 | **Pharmacy MS** | `8083` | `pharmacydb` | Medicine catalog, inventory, sales |
| 🔔 | **Notification MS** | `8085` | `notificationsdb` | Kafka consumer, WebSocket push |
| 🖼️ | **Media MS** | `9400` | `mediadb` | File upload, protected image serving |

---

## 📖 Swagger API Documentation

> All services are accessible through the **API Gateway on port `:9000`**

| Service | Swagger UI Link |
|---------|----------------|
| 👤 User MS | [`/user/swagger-ui/index.html`](http://localhost:9000/user/swagger-ui/index.html) |
| 🩺 Profile MS | [`/profile/swagger-ui/index.html`](http://localhost:9000/profile/swagger-ui/index.html) |
| 📅 Appointment MS | [`/appointment/swagger-ui/index.html`](http://localhost:9000/appointment/swagger-ui/index.html) |
| 💊 Pharmacy MS | [`/pharmacy/swagger-ui/index.html`](http://localhost:9000/pharmacy/swagger-ui/index.html) |
| 🔔 Notification MS | [`/notifications/swagger-ui/index.html`](http://localhost:9000/notifications/swagger-ui/index.html) |
| 🖼️ Media MS | [`http://localhost:9400/media/swagger-ui/index.html`](http://localhost:9400/media/swagger-ui/index.html) |
| 🔍 Eureka Dashboard | [`http://localhost:8761`](http://localhost:8761) |

---

## 🔐 Authentication Flow

```mermaid
sequenceDiagram
    actor User
    participant GW as 🛡️ Gateway<br/>:9000
    participant UM as 👤 User MS<br/>:8080
    participant PM as 🩺 Profile MS<br/>:8081

    User->>GW: POST /user/auth/login<br/>{ email, password }
    GW->>UM: Forward request (no auth check)
    UM->>UM: Validate credentials
    UM-->>GW: 200 OK · JWT Token
    GW-->>User: Returns JWT Token 🎫

    Note over User,PM: Subsequent Authenticated Request

    User->>GW: GET /profile/doctor/get/1<br/>Authorization: Bearer <token>
    GW->>GW: ✅ Validate JWT
    GW->>GW: Inject headers:<br/>X-User-Id, X-User-Role<br/>X-Secret-Key
    GW->>PM: Forwarded with injected headers
    PM->>PM: Read X-User-Role for RBAC
    PM-->>GW: 200 OK · Doctor Profile
    GW-->>User: Doctor Profile Data 🏥
```

---

## 📬 Event-Driven Notification Pipeline

```mermaid
sequenceDiagram
    actor Doctor
    participant AS as 📅 Appointment MS
    participant KF as 📨 Kafka
    participant NS as 🔔 Notification MS
    participant DB as 🗄️ notificationsdb
    participant WS as 🌐 WebSocket<br/>Browser

    Doctor->>AS: POST /appointment/book<br/>{ patientId, doctorId, date }
    AS->>AS: Save appointment
    AS->>KF: publish("appointment-events")<br/>{ type: BOOKED, patientId, doctorId }
    AS-->>Doctor: 201 Created ✅

    KF->>NS: consume event
    NS->>NS: Build notification message
    NS->>DB: Save notification record
    NS->>WS: STOMP push to /topic/notifications
    WS-->>Doctor: 🔔 "New appointment booked"
    WS-->>Doctor: 🔔 Patient notified
```

---

## 🚀 Quick Start

### 🐳 Option A — Docker Compose (Recommended)

```bash
# 1. Clone
git clone https://github.com/Leyla-la/hms-backend.git
cd hms-backend

# 2. Create .env from template
cp .env.example .env
# Edit .env with your credentials

# 3. Build all modules
mvn clean package -DskipTests

# 4. Start everything
docker-compose up -d --build

# 5. Import your database (if migrating)
docker exec -i hms-mysql mysql -u root -p < hms_full_backup.sql
```

✅ That's it! Visit `http://localhost:8761` to verify all services are registered.

---

### 💻 Option B — Run Locally (Manual)

**Step 1 — Create databases**
```sql
CREATE DATABASE userdb;
CREATE DATABASE profiledb;
CREATE DATABASE appointmentdb;
CREATE DATABASE pharmacydb;
CREATE DATABASE mediadb;
CREATE DATABASE notificationsdb;
```

**Step 2 — Start infrastructure**
```bash
docker-compose up -d zookeeper kafka redis
```

**Step 3 — Build**
```bash
mvn clean package -DskipTests
```

**Step 4 — Start services in order**
```bash
# Must start first
java -jar Eureka-Server/target/*.jar

# Then Gateway + Core services
java -jar GatewayMS/target/*.jar
java -jar UserMS/target/*.jar
java -jar ProfileMS/target/*.jar
java -jar AppointmentMS/target/*.jar
java -jar PharmacyMS/target/*.jar
java -jar MediaMS/target/*.jar

# Start last (needs Kafka to be ready)
java -jar NotificationMS/target/*.jar
```

**Or use the startup script:**
```bash
python start_optimized.py
```

---

## 🌱 Environment Variables

Create a `.env` file in the root directory (never commit this file):

```env
# Database
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
DB_ROOT_PASSWORD=your_mysql_password

# Kafka
KAFKA_PORT=9093

# Email Notifications (Brevo SMTP)
BREVO_API_KEY=your_brevo_api_key

# Docker
RESTART_POLICY=unless-stopped
```

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | MySQL root username |
| `DB_PASSWORD` | MySQL password |
| `BREVO_API_KEY` | Brevo (Sendinblue) SMTP API key for email notifications |
| `KAFKA_PORT` | External Kafka port |
| `RESTART_POLICY` | Docker container restart behavior |

---

## 🗂️ Project Structure

```
hms-backend/
├── 📁 Eureka-Server/       # Service registry
├── 📁 GatewayMS/           # API gateway + JWT filter
├── 📁 UserMS/              # Authentication service
├── 📁 ProfileMS/           # Doctor & Patient profiles
├── 📁 AppointmentMS/       # Clinical scheduling & records
├── 📁 PharmacyMS/          # Medicine & sales
├── 📁 NotificationMS/      # Event-driven notifications
├── 📁 MediaMS/             # File & avatar storage
├── 🐳 docker-compose.yml   # Full stack orchestration
├── 📦 pom.xml              # Parent Maven POM
└── 🔒 .env                 # Local secrets (gitignored ✅)
```

---

## ⚙️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (JJWT) |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka + Zookeeper |
| Real-time | WebSocket (STOMP protocol) |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| HTTP Client | OpenFeign |
| Caching | Redis 7.2 |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Apache Maven (Multi-Module) |
| Containerization | Docker + Docker Compose |

---

## 🤝 Contributing

```bash
# Create a feature branch
git checkout -b feat/your-feature

# Commit with Conventional Commits
git commit -m "feat(scope): your description"

# Push and open PR to dev branch
git push origin feat/your-feature
```

**Branch naming convention:**
- `feat/` — new features
- `fix/` — bug fixes
- `chore/` — maintenance, config
- `refactor/` — code restructure

---
