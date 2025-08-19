# E-Commerce Platform Backend

A production-ready, scalable Spring Boot backend for the E-Commerce Platform built with Java 21, Spring Boot 3.x, and modern best practices.

## 🚀 Features

### 🔐 Security & Authentication
- **JWT-based authentication** with secure httpOnly cookies
- **Role-based access control** (Admin, Seller, Customer)
- **Spring Security** with custom filters and providers
- **CSRF protection** and input validation
- **Password encryption** with BCrypt

### 🗄️ Database & Caching
- **PostgreSQL** as primary database with Flyway migrations
- **Redis** for caching and session management
- **JPA/Hibernate** with optimized entity relationships
- **Database indexing** for performance optimization

### 🛍️ E-Commerce Core
- **Product management** with variants and images
- **Category hierarchy** with parent-child relationships
- **Shopping cart** with real-time calculations
- **Order processing** with status tracking
- **User management** with role-based permissions

### 🔄 Real-time Features
- **WebSocket support** for live notifications
- **Order status updates** in real-time
- **Sales analytics** with live updates

### 📚 API Documentation
- **Swagger/OpenAPI 3** with comprehensive documentation
- **Standardized API responses** with proper error handling
- **RESTful design** following best practices

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │     Services    │    │   Repositories  │
│   (REST API)    │◄──►│  (Business      │◄──►│   (Data Access) │
│                 │    │    Logic)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      DTOs       │    │    Security     │    │    Entities     │
│  (Data Transfer)│    │  (JWT, Auth)    │    │   (JPA Models)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📋 Prerequisites

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Redis 7+**
- **Docker & Docker Compose** (optional)

## 🚀 Quick Start

### 1. Clone and Setup
```bash
git clone <repository-url>
cd ecommerce
```

### 2. Database Setup
```bash
# Create PostgreSQL database
createdb ecommerce_db_dev

# Or use Docker
docker-compose up postgres redis -d
```

### 3. Environment Configuration
```bash
# Copy and configure environment variables
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# Update database credentials and other settings
```

### 4. Run Application
```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run
mvn clean package
java -jar target/ecommerce-0.0.1-SNAPSHOT.jar
```

### 5. Access Application
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Health Check**: http://localhost:8080/api/health

## 🐳 Docker Deployment

### Local Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Production Build
```bash
# Build image
docker build -t ecommerce-backend .

# Run container
docker run -d \
  --name ecommerce-app \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  ecommerce-backend
```

## 📊 Database Schema

### Core Tables
- **users** - User accounts with role-based access
- **products** - Product catalog with variants
- **categories** - Product categorization
- **carts** - Shopping cart management
- **orders** - Order processing and tracking
- **reviews** - Product ratings and feedback

### Key Features
- **UUID primary keys** for security
- **Audit trails** with timestamps
- **Soft deletes** for data integrity
- **Optimized indexes** for performance
- **Foreign key constraints** for data consistency

## 🔐 Authentication & Authorization

### JWT Token Structure
```json
{
  "sub": "user_id",
  "role": "CUSTOMER|SELLER|ADMIN",
  "iat": "issued_at_timestamp",
  "exp": "expiration_timestamp"
}
```

### Role Permissions
- **CUSTOMER**: Browse products, manage cart, place orders
- **SELLER**: Manage products, view orders, track sales
- **ADMIN**: Full platform management and administration

### Security Headers
- JWT stored in secure httpOnly cookies
- CORS configured for frontend integration
- CSRF protection enabled
- Rate limiting on sensitive endpoints

## 📡 API Endpoints

### Authentication (`/api/auth`)
- `POST /login` - User login
- `POST /register` - User registration
- `POST /logout` - User logout
- `GET /me` - Get current user info
- `POST /forgot-password` - Password reset
- `POST /google` - Google OAuth

### Products (`/api/products`)
- `GET /` - List products with pagination and filters
- `GET /{id}` - Get product details
- `POST /` - Create product (Seller/Admin)
- `PUT /{id}` - Update product (Owner/Admin)
- `DELETE /{id}` - Delete product (Owner/Admin)

### Categories (`/api/categories`)
- `GET /` - List all categories
- `POST /` - Create category (Admin)
- `PUT /{id}` - Update category (Admin)
- `DELETE /{id}` - Delete category (Admin)

### Cart (`/api/cart`)
- `GET /` - Get user's cart
- `POST /` - Add item to cart
- `PUT /{productId}` - Update cart item quantity
- `DELETE /{productId}` - Remove item from cart

### Orders (`/api/orders`)
- `GET /` - List user's orders
- `POST /` - Create order from cart
- `PUT /{id}/status` - Update order status (Seller/Admin)

### Users (`/api/users`)
- `GET /` - List users (Admin)
- `PUT /{id}/block` - Block user (Admin)
- `PUT /{id}/unblock` - Unblock user (Admin)

## 🧪 Testing

### Run Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=ProductServiceTest

# Integration tests
mvn test -Dtest=*IntegrationTest
```

### Test Coverage
```bash
# Generate coverage report
mvn jacoco:report

# View in browser
open target/site/jacoco/index.html
```

## 📈 Performance & Monitoring

### Caching Strategy
- **Redis** for session storage
- **Product catalog** caching (30 min TTL)
- **Category list** caching (2 hour TTL)
- **User data** caching (1 hour TTL)

### Database Optimization
- **Connection pooling** with HikariCP
- **Query optimization** with proper indexing
- **Batch operations** for bulk operations
- **Read replicas** support for scaling

### Monitoring
- **Health checks** for all services
- **Logging** with structured format
- **Metrics** for performance tracking
- **Audit trails** for security compliance

## 🔧 Configuration

### Profiles
- **dev** - Development with debug logging
- **prod** - Production with optimized settings

### Environment Variables
```bash
# Database
DATABASE_URL=jdbc:postgresql://host:5432/db
DATABASE_USERNAME=user
DATABASE_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Email
SMTP_HOST=smtp.gmail.com
SMTP_USERNAME=user@gmail.com
SMTP_PASSWORD=password
```

## 🚀 Deployment

### Production Checklist
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] SSL certificates installed
- [ ] Load balancer configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy implemented
- [ ] Security audit completed

### CI/CD Pipeline
```yaml
# Example GitHub Actions workflow
name: Deploy Backend
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build and Test
        run: mvn clean package
      - name: Deploy to Production
        run: ./deploy.sh
```

## 🔒 Security Best Practices

### Implemented
- JWT with secure secret management
- Role-based access control
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CSRF token implementation
- Secure cookie configuration

### Recommendations
- Regular security audits
- Dependency vulnerability scanning
- Rate limiting on public endpoints
- API key rotation
- Security headers configuration
- Log monitoring for suspicious activity

## 📚 Additional Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/site/docs/current/reference/html5/)
- [JPA/Hibernate Documentation](https://hibernate.org/orm/documentation/)

### Tools
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Docker Documentation](https://docs.docker.com/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the API specification
- Contact the development team

---

**Built with ❤️ using Spring Boot, Java 21, and modern best practices**
