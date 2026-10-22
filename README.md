# digital-pharmacy-system

## Domain class: user and identity management
Implemented domain classes using builder pattern (221066748):
- **`User`**: Represents system users, credentials, contact details, and system roles (`CUSTOMER`, `PHARMACIST`, `COURIER`, `ADMIN`).
- **`PatientProfile`**: Represents patient medical background (allergies, chronic conditions, emergency contacts, date of birth) linked 1-to-1 with a User entity.