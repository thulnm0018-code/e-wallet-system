# System Architecture Diagram - Hệ thống E-Wallet

```mermaid
flowchart LR
    Client[User / Admin Browser] --> Frontend[React Frontend]
    Frontend --> API[Spring Boot API]
    API --> Auth[Authentication Service]
    API --> Wallet[Wallet Service]
    API --> UserSvc[User Service]
    API --> AdminSvc[Admin Service]
    API --> NotificationSvc[Notification Service]

    Auth --> DB[(MySQL Database)]
    Wallet --> DB
    UserSvc --> DB
    AdminSvc --> DB
    NotificationSvc --> DB

    NotificationSvc --> MQ[RabbitMQ]
    AdminSvc --> MQ
    MQ --> Notify[Notification Consumer]
```
