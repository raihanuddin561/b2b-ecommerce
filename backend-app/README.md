# B2B E-Commerce Platform Backend

A scalable, modular B2B e-commerce backend built with Java, Spring Boot, and best practices for security, maintainability, and extensibility.

## Features
- Company (Vendor) Registration with admin onboarding
- Buyer Registration with address support
- Role-based authentication and authorization
- Email confirmation and validation
- Global exception handling with professional error responses
- Extensible user and company models

## Architecture Overview

### Layered Structure
```
Controller Layer (REST API)
    |
Service Layer (Business Logic)
    |
Repository Layer (Data Access)
    |
Entity Layer (JPA Entities)
```

### Key Components
- **Controllers:**
  - `RegisterCompanyController`: Handles company/vendor registration.
  - `RegisterBuyerController`: Handles buyer registration.
- **Services:**
  - `RegisterCompanyService`: Business logic for company registration, admin creation, validation.
  - `RegisterBuyerService`: Business logic for buyer registration, validation.
  - `UserService`, `EmailService`, etc.: Shared business logic.
- **DTOs:**
  - `RegisterCompanyRequest`, `RegisterBuyerRequest`: Data transfer objects for registration.
  - Response DTOs for API responses.
- **Entities:**
  - `User`: Represents both buyers and company users/admins, with fields for roles, addresses, etc.
  - `Company`: Represents vendor companies.
  - `Role`, `UserType`, etc.: For RBAC and user management.
- **Repositories:**
  - JPA repositories for data access.
- **Exception Handling:**
  - `GlobalExceptionHandler`: Centralized, professional error handling for all REST endpoints.

### Registration Flows
- **Company Registration:**
  - Collects company info and first admin user in one step.
  - Ensures every company has an owner/admin.
  - Validates, checks for duplicates, and sends confirmation email.
- **Buyer Registration:**
  - Collects buyer info and address.
  - Assigns BUYER or CUSTOMER role and sends confirmation email.

### Security
- Role-based access control (RBAC) using Spring Security.
- Email confirmation for new accounts.
- Passwords securely hashed.

### Extensibility
- Easily add new user types, roles, or registration flows.
- Modular service and controller structure.

## How to Run
1. Clone the repository.
2. Configure your database and email settings in `src/main/resources/application.properties`.
3. Build and run the application:
   ```
   ./gradlew bootRun
   ```
4. Access API endpoints (e.g., `/api/companies/register`, `/api/buyers/register`).

## Testing
- **Unit Tests:**
  - Service and controller layer tests for registration and user management.
- **Integration Tests:**
  - Recommended for end-to-end flow (controller + service + repository).
- Run all tests:
  ```
  ./gradlew test
  ```

## Contributing
- Follow best practices for Java, Spring Boot, and REST API design.
- Write tests for new features.
- Use clear, descriptive commit messages.

## License
This project is licensed under the MIT License.

