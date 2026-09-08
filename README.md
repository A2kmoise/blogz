# Blog Platform

A full-stack blog platform built with a Spring Boot backend.

## Features

### Backend (Spring Boot)
- JWT-based authentication with password encryption
- User registration and login
- Blog CRUD operations with admin controls
- Tag management system
- Category-based filtering
- Admin dashboard functionality
- RESTful API endpoints
- PostgreSQL database integration
- BCrypt password hashing
- JWT token generation and validation

## Tech Stack

- **Backend**: Spring Boot 4.0.6, Spring Data JPA, Spring Security, PostgreSQL
- **Authentication**: JWT tokens, BCrypt password hashing
- **Database**: PostgreSQL 15
- **Build Tool**: Maven

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for PostgreSQL and Redis)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd blog-backend
```

### 2. Start PostgreSQL Database
```bash
docker-compose up -d
```

### 3. Run the Application
```bash
./mvnw spring-boot: run
```

The application will start on `http://localhost:8080`

### 4. Access the Application
- **API Documentation**: http://localhost:8080/swagger-ui.html (if configured)

## Default Admin Account
- **Email**: admin@blog.com
- **Password**: admin123

## API Endpoints

### Authentication
- `POST /auth/register` - User registration (returns JWT token)
- `POST /auth/login` - User login (returns JWT token)
- `PUT /auth/profile/{userId}` - Update user profile
- `POST /auth/logout` - User logout

### Blogs
- `GET /api/blogs` - Get all blogs
- `GET /api/blogs/{id}` - Get blog by ID
- `GET /api/blogs/user/{userId}` - Get blogs by user
- `POST /api/blogs` - Create new blog (requires authentication)
- `PUT /api/blogs/{id}` - Update blog (requires authentication)
- `DELETE /api/blogs/{id}` - Delete blog (requires admin role)

## Database Schema

### Users Table
- id (Primary Key)
- email (Unique)
- password (BCrypt hashed)
- role (ADMIN/USER)
- created_at
- updated_at

### Blogs Table
- id (Primary Key)
- title
- content (TEXT)
- category (ENUM)
- user_id (Foreign Key)
- created_at
- updated_at

### Tags Table
- id (Primary Key)
- name (Unique)

### Blog_Tags Table (Many-to-Many)
- blog_id (Foreign Key)
- tag_id (Foreign Key)

## Security Features

### JWT Authentication
- Secure token generation with configurable expiration
- Token validation on protected endpoints
- Role-based access control
- Automatic token refresh handling

### Password Security
- BCrypt password hashing with salt
- Password strength validation
- Secure password storage

### API Security
- CORS configuration for cross-origin requests
- JWT-based stateless authentication
- Protected admin endpoints
- Input validation and sanitisation

## Configuration

### JWT Settings
```properties
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Database Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blogdb
spring.datasource.username=bloguser
spring.datasource.password=secret
```

## Development

### Adding New Features
1. Create new DTOs in `src/main/java/.../DTO/`
2. Add service methods in `src/main/java/.../services/`
3. Create controller endpoints in `src/main/java/.../controllers/`

### Security Utilities
- **JwtUtil**: Token generation, validation, and claims extraction
- **PasswordEncoder**: BCrypt password hashing and verification
- **JwtAuthenticationFilter**: Request filtering for JWT validation

## Production Considerations

### Security Enhancements
1. Use environment variables for JWT secret
2. Implement token blacklisting for logout
3. Add rate limiting for authentication endpoints
4. Configure HTTPS in production
5. Implement refresh token mechanism

### Performance Optimisations
1. Add database indexing for frequently queried fields
2. Implement caching for blog listings
3. Add pagination for large datasets
4. Optimise database queries with proper joins

### Monitoring & Logging
1. Add structured logging with correlation IDs
2. Implement health checks and metrics
3. Set up error tracking and monitoring
4. Add audit logging for admin actions

## License
This project is licensed under the MIT License.
