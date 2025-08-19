# E-Commerce Platform Backend API Specification

## Overview
This document outlines the complete backend API requirements for the E-Commerce Platform. The API should be production-ready with proper authentication, authorization, and data validation.

## Base URL
```
Production: https://api.estore.com/api
Development: http://localhost:8080/api
```

## Authentication
- **Type**: JWT (JSON Web Token)
- **Storage**: HTTP-only cookies
- **Secret**: Environment variable `JWT_SECRET`
- **Expiration**: 24 hours (configurable)

## API Endpoints

### 1. Authentication (`/auth`)

#### 1.1 User Login
- **URL**: `POST /auth/login`
- **Description**: Authenticate user and return JWT token
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "customer",
      "isBlocked": false,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    },
    "token": "jwt_token_here"
  }
}
```

#### 1.2 User Registration
- **URL**: `POST /auth/register`
- **Description**: Create new user account
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "customer"
}
```
- **Response**: Same as login response

#### 1.3 Google OAuth
- **URL**: `POST /auth/google`
- **Description**: Authenticate with Google OAuth token
- **Request Body**:
```json
{
  "token": "google_oauth_token"
}
```
- **Response**: Same as login response

#### 1.4 Forgot Password
- **URL**: `POST /auth/forgot-password`
- **Description**: Send password reset email
- **Request Body**:
```json
{
  "email": "user@example.com"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "message": "Password reset email sent"
  }
}
```

#### 1.5 Logout
- **URL**: `POST /auth/logout`
- **Description**: Invalidate JWT token
- **Request Body**: None
- **Response**:
```json
{
  "success": true,
  "data": {
    "message": "Logged out successfully"
  }
}
```

#### 1.6 Get Current User
- **URL**: `GET /auth/me`
- **Description**: Get current authenticated user info
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "customer",
    "isBlocked": false,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 2. Products (`/products`)

#### 2.1 Get Products (Paginated)
- **URL**: `GET /products`
- **Description**: Get paginated list of products with filters
- **Query Parameters**:
  - `page` (number): Page number (default: 1)
  - `limit` (number): Items per page (default: 12)
  - `category` (string): Category ID filter
  - `minPrice` (number): Minimum price filter
  - `maxPrice` (number): Maximum price filter
  - `rating` (number): Minimum rating filter
  - `inStock` (boolean): In stock filter
- **Response**:
```json
{
  "success": true,
  "data": {
    "data": [
      {
        "id": "uuid",
        "name": "Product Name",
        "description": "Product description",
        "price": 99.99,
        "images": ["url1", "url2"],
        "category": {
          "id": "uuid",
          "name": "Category Name",
          "description": "Category description",
          "image": "url",
          "isActive": true,
          "createdAt": "2024-01-01T00:00:00Z",
          "updatedAt": "2024-01-01T00:00:00Z"
        },
        "seller": {
          "id": "uuid",
          "email": "seller@example.com",
          "firstName": "Jane",
          "lastName": "Smith",
          "role": "seller",
          "isBlocked": false,
          "createdAt": "2024-01-01T00:00:00Z",
          "updatedAt": "2024-01-01T00:00:00Z"
        },
        "stock": 100,
        "rating": 4.5,
        "reviewCount": 25,
        "isActive": true,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 12,
      "total": 150,
      "totalPages": 13
    }
  }
}
```

#### 2.2 Get Single Product
- **URL**: `GET /products/{id}`
- **Description**: Get detailed product information
- **Response**: Single product object (same structure as above)

#### 2.3 Create Product (Seller/Admin Only)
- **URL**: `POST /products`
- **Description**: Create new product listing
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "name": "Product Name",
  "description": "Product description",
  "price": 99.99,
  "categoryId": "uuid",
  "stock": 100,
  "images": ["url1", "url2"]
}
```
- **Response**: Created product object

#### 2.4 Update Product (Owner/Admin Only)
- **URL**: `PUT /products/{id}`
- **Description**: Update existing product
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**: Partial product data
- **Response**: Updated product object

#### 2.5 Delete Product (Owner/Admin Only)
- **URL**: `DELETE /products/{id}`
- **Description**: Delete product listing
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
```json
{
  "success": true,
  "data": {
    "message": "Product deleted successfully"
  }
}
```

### 3. Categories (`/categories`)

#### 3.1 Get Categories
- **URL**: `GET /categories`
- **Description**: Get all active categories
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "Electronics",
      "description": "Electronic devices and accessories",
      "image": "url",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### 3.2 Create Category (Admin Only)
- **URL**: `POST /categories`
- **Description**: Create new product category
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "name": "Category Name",
  "description": "Category description"
}
```
- **Response**: Created category object

#### 3.3 Update Category (Admin Only)
- **URL**: `PUT /categories/{id}`
- **Description**: Update existing category
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**: Partial category data
- **Response**: Updated category object

#### 3.4 Delete Category (Admin Only)
- **URL**: `DELETE /categories/{id}`
- **Description**: Delete category
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
```json
{
  "success": true,
  "data": {
    "message": "Category deleted successfully"
  }
}
```

### 4. Cart (`/cart`)

#### 4.1 Get Cart
- **URL**: `GET /cart`
- **Description**: Get current user's shopping cart
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "items": [
      {
        "id": "uuid",
        "product": {
          "id": "uuid",
          "name": "Product Name",
          "description": "Product description",
          "price": 99.99,
          "images": ["url1"],
          "category": {...},
          "seller": {...},
          "stock": 100,
          "rating": 4.5,
          "reviewCount": 25,
          "isActive": true,
          "createdAt": "2024-01-01T00:00:00Z",
          "updatedAt": "2024-01-01T00:00:00Z"
        },
        "quantity": 2,
        "price": 99.99
      }
    ],
    "total": 199.98,
    "itemCount": 2
  }
}
```

#### 4.2 Add to Cart
- **URL**: `POST /cart`
- **Description**: Add product to cart
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "productId": "uuid",
  "quantity": 2
}
```
- **Response**: Updated cart object

#### 4.3 Update Cart Item
- **URL**: `PUT /cart/{productId}`
- **Description**: Update product quantity in cart
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "quantity": 3
}
```
- **Response**: Updated cart object

#### 4.4 Remove from Cart
- **URL**: `DELETE /cart/{productId}`
- **Description**: Remove product from cart
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Updated cart object

### 5. Orders (`/orders`)

#### 5.1 Get Orders (Paginated)
- **URL**: `GET /orders`
- **Description**: Get user's order history
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Query Parameters**:
  - `page` (number): Page number (default: 1)
  - `limit` (number): Items per page (default: 10)
- **Response**:
```json
{
  "success": true,
  "data": {
    "data": [
      {
        "id": "uuid",
        "customer": {
          "id": "uuid",
          "email": "customer@example.com",
          "firstName": "John",
          "lastName": "Doe",
          "role": "customer",
          "isBlocked": false,
          "createdAt": "2024-01-01T00:00:00Z",
          "updatedAt": "2024-01-01T00:00:00Z"
        },
        "items": [
          {
            "id": "uuid",
            "product": {...},
            "quantity": 2,
            "price": 99.99
          }
        ],
        "total": 199.98,
        "status": "pending",
        "shippingAddress": {
          "street": "123 Main St",
          "city": "New York",
          "state": "NY",
          "zipCode": "10001",
          "country": "USA"
        },
        "paymentStatus": "pending",
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 25,
      "totalPages": 3
    }
  }
}
```

#### 5.2 Create Order
- **URL**: `POST /orders`
- **Description**: Create new order from cart
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```
- **Response**: Created order object

#### 5.3 Update Order Status (Admin/Seller Only)
- **URL**: `PUT /orders/{id}/status`
- **Description**: Update order status
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Request Body**:
```json
{
  "status": "shipped"
}
```
- **Response**: Updated order object

### 6. Users (Admin Only)

#### 6.1 Get Users (Paginated)
- **URL**: `GET /users`
- **Description**: Get all users (admin only)
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Query Parameters**:
  - `page` (number): Page number (default: 1)
  - `limit` (number): Items per page (default: 10)
- **Response**: Paginated user list

#### 6.2 Block User
- **URL**: `PUT /users/{id}/block`
- **Description**: Block user account
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Updated user object

#### 6.3 Unblock User
- **URL**: `PUT /users/{id}/unblock`
- **Description**: Unblock user account
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Updated user object

## Data Models

### User
```typescript
interface User {
  id: string;           // UUID
  email: string;        // Email address
  firstName: string;    // First name
  lastName: string;     // Last name
  role: 'customer' | 'seller' | 'admin';
  isBlocked: boolean;   // Account status
  createdAt: string;    // ISO date string
  updatedAt: string;    // ISO date string
}
```

### Product
```typescript
interface Product {
  id: string;           // UUID
  name: string;         // Product name
  description: string;  // Product description
  price: number;        // Price in USD
  images: string[];     // Array of image URLs
  category: Category;   // Category object
  seller: User;         // Seller user object
  stock: number;        // Available quantity
  rating: number;       // Average rating (1-5)
  reviewCount: number;  // Number of reviews
  isActive: boolean;    // Product status
  createdAt: string;    // ISO date string
  updatedAt: string;    // ISO date string
}
```

### Category
```typescript
interface Category {
  id: string;           // UUID
  name: string;         // Category name
  description?: string; // Optional description
  image?: string;       // Optional image URL
  isActive: boolean;    // Category status
  createdAt: string;    // ISO date string
  updatedAt: string;    // ISO date string
}
```

### Cart
```typescript
interface Cart {
  id: string;           // UUID
  items: CartItem[];    // Array of cart items
  total: number;        // Total cart value
  itemCount: number;    // Total number of items
}

interface CartItem {
  id: string;           // UUID
  product: Product;     // Product object
  quantity: number;     // Item quantity
  price: number;        // Item price
}
```

### Order
```typescript
interface Order {
  id: string;           // UUID
  customer: User;       // Customer user object
  items: OrderItem[];   // Array of order items
  total: number;        // Order total
  status: 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled';
  shippingAddress: Address;
  paymentStatus: 'pending' | 'paid' | 'failed';
  createdAt: string;    // ISO date string
  updatedAt: string;    // ISO date string
}

interface OrderItem {
  id: string;           // UUID
  product: Product;     // Product object
  quantity: number;     // Item quantity
  price: number;        // Item price
}

interface Address {
  street: string;       // Street address
  city: string;         // City
  state: string;        // State/Province
  zipCode: string;      // ZIP/Postal code
  country: string;      // Country
}
```

## Error Handling

### Standard Error Response
```json
{
  "success": false,
  "error": "Error message description"
}
```

### HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `422` - Validation Error
- `500` - Internal Server Error

### Common Error Messages
- `"Invalid credentials"` - Login failed
- `"User already exists"` - Registration failed
- `"Product not found"` - Product doesn't exist
- `"Insufficient stock"` - Not enough inventory
- `"Access denied"` - Insufficient permissions
- `"Invalid token"` - JWT validation failed

## Security Requirements

### Authentication
- JWT tokens with secure secret
- HTTP-only cookies for token storage
- Token expiration and refresh mechanism
- Rate limiting on auth endpoints

### Authorization
- Role-based access control (RBAC)
- Resource ownership validation
- Admin-only endpoints protection
- Seller product management restrictions

### Data Validation
- Input sanitization and validation
- SQL injection prevention
- XSS protection
- CSRF token implementation

### Environment Variables
```bash
JWT_SECRET=your-super-secure-secret-key
DATABASE_URL=your-database-connection-string
REDIS_URL=your-redis-connection-string
SMTP_HOST=your-smtp-host
SMTP_PORT=587
SMTP_USER=your-smtp-username
SMTP_PASS=your-smtp-password
GOOGLE_CLIENT_ID=your-google-oauth-client-id
GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
```

## Performance Requirements

### Response Times
- API endpoints: < 200ms average
- Database queries: < 100ms average
- Image uploads: < 2s for 5MB files

### Caching
- Redis for session storage
- Product catalog caching
- Category list caching
- User data caching

### Database
- Connection pooling
- Query optimization
- Indexing on frequently queried fields
- Read replicas for scaling

## Monitoring & Logging

### Metrics
- Request/response times
- Error rates
- User activity
- Database performance
- API usage statistics

### Logging
- Request/response logging
- Error logging with stack traces
- User action logging
- Security event logging
- Performance metrics logging

## Deployment

### Production Environment
- HTTPS with valid SSL certificate
- Load balancer for traffic distribution
- Auto-scaling based on demand
- Database backup and recovery
- Monitoring and alerting systems

### CI/CD Pipeline
- Automated testing
- Code quality checks
- Security scanning
- Automated deployment
- Rollback capabilities

This API specification provides a comprehensive foundation for building a production-ready e-commerce platform backend that integrates seamlessly with the existing Next.js frontend.
