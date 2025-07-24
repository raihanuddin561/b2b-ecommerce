# B2B E-Commerce Platform Backend

A scalable, modular B2B e-commerce backend built with Java, Spring Boot, and best practices for security, maintainability, and extensibility.

## 🚀 Features
- **Multi-User Registration System**: Company (Vendor) & Buyer registration with role-based access
- **Secure Authentication**: JWT-based authentication with enhanced login responses
- **Professional Exception Handling**: Comprehensive error handling for all API endpoints
- **Email Verification**: Automated email confirmation for account activation
- **Address Management**: Complete address handling for both personal and company addresses
- **Role-Based Authorization**: RBAC with UserType (BUYER/SELLER/ADMIN) support
- **Professional API Design**: RESTful endpoints with proper HTTP status codes

## 🏗️ Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────────┐
│           Controller Layer              │  ← REST API Endpoints
├─────────────────────────────────────────┤
│            Service Layer                │  ← Business Logic
├─────────────────────────────────────────┤
│           Repository Layer              │  ← Data Access (JPA)
├─────────────────────────────────────────┤
│            Entity Layer                 │  ← Domain Models
└─────────────────────────────────────────┘
```

### 🔐 Security Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Authentication │───▶│  Authorization  │───▶│   Resource      │
│     Filter      │    │     Filter      │    │   Controller    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
    JWT Validation          Role Checking          Business Logic
```

## 📁 Project Structure

### Controllers
- **`RegisterCompanyController`**: Company/vendor registration endpoints
- **`RegisterBuyerController`**: Buyer registration endpoints  
- **`AuthController`**: Email confirmation and authentication
- **`UserController`**: User profile and settings management

### Services & Implementation
- **`RegisterCompanyService`**: Business logic for company registration
- **`RegisterBuyerService`**: Business logic for buyer registration
- **`UserServiceImpl`**: User management with ModelMapper integration
- **`EmailConfirmationService`**: Email verification workflows

### Security Components
- **`CustomAuthenticationFilter`**: JWT-based login with enhanced responses
- **`CustomAuthorizationFilter`**: Token validation and user context
- **`JwtUtil`**: JWT token generation and validation utilities

### Exception Handling
- **`GlobalExceptionHandler`**: Centralized exception handling
- **Custom Exceptions**: Professional error responses for different scenarios
  - `ResourceNotFoundException`: For missing resources (404)
  - `InvalidPathException`: For invalid API paths (400)
  - `UserAlreadyExistsException`: For duplicate registrations (409)
  - `InvalidFieldException`: For validation errors (400)

## 🔑 Authentication & Authorization

### Enhanced Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 123,
  "name": "John Doe",
  "username": "john.doe",
  "roles": ["BUYER"],
  "userType": "BUYER", 
  "expiresAt": "2025-07-25T21:30:00",
  "emailVerified": true,
  "profileInitials": "JD"
}
```

### Security Features
- **JWT Token Security**: No email exposure in login responses
- **Profile Initials**: Auto-generated for avatar display
- **Token Expiration**: Clear expiry time for frontend handling
- **Role-Based Access**: Comprehensive RBAC implementation

## 🛠️ Exception Handling

### Professional Error Responses
All API errors return consistent JSON format:
```json
{
  "error": {
    "message": "User not found with ID: 123",
    "status": 404,
    "timestamp": "2025-07-24T21:30:00"
  }
}
```

### Supported Error Scenarios
- **404 Not Found**: Missing resources or endpoints
- **400 Bad Request**: Invalid path parameters, malformed requests
- **405 Method Not Allowed**: Wrong HTTP methods
- **409 Conflict**: Duplicate resources (email already exists)
- **415 Unsupported Media Type**: Wrong Content-Type headers
- **500 Internal Server Error**: Unexpected server errors

## 📋 Registration Flows

### Company Registration Process
1. **Input Validation**: Company details + admin user information
2. **Duplicate Check**: Verify unique company name and admin email
3. **Entity Creation**: Create Company and Admin User entities
4. **Role Assignment**: Assign appropriate roles (COMPANY_ADMIN)
5. **Email Confirmation**: Send verification email to admin
6. **Response**: Professional success/error responses

### Buyer Registration Process  
1. **Input Validation**: Personal details + address information
2. **Duplicate Check**: Verify unique email address
3. **User Creation**: Create User entity with BUYER role
4. **Address Handling**: Store complete address details
5. **Email Confirmation**: Send account activation email
6. **Response**: Secure registration confirmation

## 🗃️ Data Models

### User Entity
```java
@Entity
public class User {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private UserType userType;      // BUYER, SELLER, ADMIN
    private AccountStatus status;   // ACTIVE, PENDING, SUSPENDED
    
    // Address fields for buyers
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    
    // Relationships
    @ManyToOne Company company;     // For company users
    @ManyToMany Set<Role> roles;    // Role-based permissions
}
```

### Company Entity
```java
@Entity
public class Company {
    private Long id;
    private String name;
    private String description;
    private String industry;
    
    // Company address
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPostalCode;
    private String addressCountry;
    
    // Business details
    private String website;
    private String phone;
}
```

## 🔧 Development Best Practices

### ModelMapper Integration
- **Hybrid Approach**: ModelMapper for simple fields + custom logic for complex mappings
- **Configuration**: Strict matching strategy to prevent unexpected mappings
- **Custom Mappings**: Proper handling of field name mismatches (fullName → name)

### Exception Strategy
- **Custom Exceptions**: Specific exceptions for different business scenarios
- **Global Handler**: Centralized exception handling with consistent responses
- **Security**: No stack trace exposure to clients
- **Logging**: Comprehensive error logging for debugging

### API Design Principles
- **RESTful URLs**: Clear, resource-based endpoint structure
- **HTTP Status Codes**: Proper status codes for different scenarios
- **Request/Response DTOs**: Clean separation between API and domain models
- **Validation**: Comprehensive input validation with descriptive error messages

## 🧪 Testing Strategy

### Test Structure
- **Unit Tests**: Service layer business logic testing
- **Integration Tests**: Controller and repository testing
- **Mock Testing**: Using Mockito for dependency isolation

### Test Examples
```java
@ExtendWith(MockitoExtension.class)
class RegisterBuyerControllerTest {
    @Mock RegisterBuyerService service;
    @InjectMocks RegisterBuyerController controller;
    
    @Test
    void registerBuyer_success() throws Exception {
        // Test implementation
    }
}
```

## 🚀 Getting Started

### Prerequisites
- Java 21+
- PostgreSQL 12+
- Gradle 8.x

### Setup Instructions
1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd backend-app
   ```

2. **Database Configuration**
   ```properties
   # application.properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/b2b_ecommerce
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build & Run**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

4. **Run Tests**
   ```bash
   ./gradlew test
   ```

## 📚 API Documentation

### Authentication Endpoints
- `POST /auth/login` - User authentication with enhanced response
- `GET /auth/confirm-email?token=...` - Email confirmation
- `POST /auth/resend-confirmation` - Resend confirmation email

### Registration Endpoints
- `POST /api/companies/register` - Company registration
- `POST /api/buyers/register` - Buyer registration

### User Management
- `GET /api/user/{userId}/profile` - Get user profile
- `PUT /api/user/{userId}/profile` - Update user profile
- `GET /api/user/{userId}/settings` - Get user settings

### Error Handling Examples
```bash
# Invalid user ID
GET /api/user/-5/profile
# Response: 400 Bad Request

# Non-existent endpoint  
GET /api/user/123/invalidpath
# Response: 400 Bad Request with helpful message

# Wrong HTTP method
DELETE /api/user/123/profile  
# Response: 405 Method Not Allowed
```

## 🔒 Security Considerations

### JWT Token Security
- **No Email Exposure**: Login responses exclude sensitive email information
- **Token Expiration**: Clear expiry handling for frontend applications
- **Secure Claims**: Minimal claims in JWT payload

### Input Validation
- **Path Parameter Validation**: User ID range and format validation
- **Business Rule Validation**: Reasonable limits and constraints
- **SQL Injection Prevention**: Parameterized queries via JPA

### Error Response Security
- **No Stack Traces**: Clean error messages without technical details
- **Consistent Format**: Standardized error response structure
- **Audit Logging**: Request tracking for security monitoring

## 🤝 Contributing

### Code Standards
- **Java Conventions**: Follow standard Java naming conventions
- **Documentation**: Comprehensive JavaDoc for public APIs
- **Testing**: Minimum 80% code coverage for new features
- **Exception Handling**: Use appropriate custom exceptions

### Git Workflow
- **Feature Branches**: Create branches for new features
- **Descriptive Commits**: Clear, descriptive commit messages
- **Code Review**: All changes require review before merge

---

**Built with ❤️ using Spring Boot, following enterprise-grade best practices for B2B e-commerce solutions.**
