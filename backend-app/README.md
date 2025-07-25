# B2B E-Commerce Platform Backend

A scalable, modular B2B e-commerce backend built with Java, Spring Boot, and best practices for security, maintainability, and extensibility.

## 🚀 Features
- **Multi-User Registration System**: Company (Vendor) & Buyer registration with role-based access
- **Secure Authentication**: JWT-based authentication with enhanced login responses
- **Comprehensive Error Handling**: Professional exception handling for ALL types of wrong request paths
- **Email Verification**: Automated email confirmation for account activation
- **Address Management**: Complete address handling for both personal and company addresses
- **Role-Based Authorization**: RBAC with UserType (BUYER/SELLER/ADMIN) support
- **Professional API Design**: RESTful endpoints with proper HTTP status codes
- **Robust Path Validation**: Handles all kinds of invalid requests professionally

## 🏗️ Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────────────┐
│           Controller Layer              │  ← REST API Endpoints
├─────────────────────────────────────────┤
│       Exception Handling Layer          │  ← Global Error Management
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

### 🛡️ Error Handling Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  HTTP Request   │───▶│ Spring Security │───▶│   Controllers   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ FallbackCtrlr   │───▶│GlobalExceptionHr│───▶│ ApiErrorResponse│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📁 Project Structure

### Controllers
- **`RegisterCompanyController`**: Company/vendor registration endpoints
- **`RegisterBuyerController`**: Buyer registration endpoints  
- **`AuthController`**: Email confirmation and authentication
- **`UserController`**: User profile and settings management
- **`FallbackController`**: Handles ALL unmatched API paths professionally

### Exception Handling System
- **`GlobalExceptionHandler`**: Centralized exception handling for all error types
- **`EndpointNotFoundException`**: Custom exception for non-existent endpoints
- **`ServiceUnavailableException`**: Professional service unavailable responses
- **`InvalidFieldException`**: Field validation error handling
- **`ApiErrorResponse`**: Standardized error response format

### Services & Implementation
- **`RegisterCompanyService`**: Business logic for company registration
- **`RegisterBuyerService`**: Business logic for buyer registration
- **`UserServiceImpl`**: User management with ModelMapper integration

## 🛡️ Comprehensive Error Handling

### Error Types Handled Professionally

#### 1. **Path-Related Errors**
- ✅ Non-existent API endpoints (`/api/nonexistent`)
- ✅ Wrong HTTP methods (`DELETE` on `POST` endpoints)
- ✅ Invalid path parameters (`/api/users/invalid-id`)
- ✅ Missing path variables
- ✅ Type mismatches in URLs (`string` where `number` expected)
- ✅ Paths outside API structure (`/wrong-path`)

#### 2. **Request Format Errors**
- ✅ Unsupported media types (`application/xml` on JSON endpoints)
- ✅ Malformed JSON in request body
- ✅ Missing required request body
- ✅ Missing request parameters
- ✅ Invalid parameter formats

#### 3. **Authentication & Authorization Errors**
- ✅ Invalid credentials
- ✅ Expired JWT tokens
- ✅ Access denied (insufficient permissions)
- ✅ Missing authentication

#### 4. **Business Logic Errors**
- ✅ User already exists
- ✅ Invalid email format
- ✅ Email sending failures
- ✅ Email confirmation errors
- ✅ Role not found errors

#### 5. **Validation Errors**
- ✅ Field validation failures
- ✅ Invalid field values
- ✅ Argument validation errors

### Error Response Format
All errors return a consistent JSON structure:
```json
{
  "error": {
    "message": "Descriptive error message",
    "statusCode": 404,
    "timestamp": "2025-07-25T21:30:45"
  }
}
```

### Professional Error Messages Examples

**Non-existent Endpoint:**
```http
GET /api/nonexistent
HTTP/1.1 404 Not Found
{
  "error": {
    "message": "API endpoint 'GET /api/nonexistent' does not exist. Please check the API documentation for valid endpoints.",
    "statusCode": 404,
    "timestamp": "2025-07-25T21:30:45"
  }
}
```

**Wrong HTTP Method:**
```http
DELETE /api/auth/login
HTTP/1.1 405 Method Not Allowed
{
  "error": {
    "message": "HTTP method 'DELETE' is not supported for this endpoint. Supported methods: GET, POST",
    "statusCode": 405,
    "timestamp": "2025-07-25T21:30:45"
  }
}
```

**Service Unavailable (Admin Paths):**
```http
GET /api/admin/secret-endpoint
HTTP/1.1 503 Service Unavailable
{
  "error": {
    "message": "The requested path '/api/admin/secret-endpoint' is not available. Please check the API documentation for valid endpoints.",
    "statusCode": 503,
    "timestamp": "2025-07-25T21:30:45"
  }
}
```

## 🎯 Best Practices Implemented

### 1. **Exception Hierarchy**
```
Exception
├── RuntimeException
    ├── UserAlreadyExistsException
    ├── EndpointNotFoundException
    ├── ServiceUnavailableException
    ├── InvalidFieldException
    └── ... (business-specific exceptions)
```

### 2. **HTTP Status Code Usage**
- **200 OK**: Successful operations
- **201 Created**: Resource creation success
- **400 Bad Request**: Client-side validation errors
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Access denied
- **404 Not Found**: Resource/endpoint not found
- **405 Method Not Allowed**: Wrong HTTP method
- **409 Conflict**: Resource conflict (user exists)
- **415 Unsupported Media Type**: Wrong content type
- **503 Service Unavailable**: Service temporarily unavailable

### 3. **Error Handling Configuration**
```properties
# Enable comprehensive error handling
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
server.error.include-message=always
server.error.include-binding-errors=always
```

### 4. **Professional Exception Classes**
- Custom exceptions for specific business scenarios
- Factory methods for common error patterns
- Detailed error messages for debugging
- Consistent error response structure

## 🧪 Testing Strategy

### Error Handling Tests
- ✅ Unit tests for each exception type
- ✅ Integration tests for wrong path scenarios
- ✅ Security bypass testing with `@WithMockUser`
- ✅ Response format validation
- ✅ HTTP status code verification

### Test Coverage Areas
1. **Path Validation Tests**
2. **HTTP Method Validation Tests**
3. **Media Type Validation Tests**
4. **Request Body Validation Tests**
5. **Security Exception Tests**
6. **Business Logic Exception Tests**

## 🔧 Configuration

### Application Properties
```properties
# Error Handling Configuration
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-stacktrace=on_param
server.error.include-exception=false
```
