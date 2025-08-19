package in.ShopSphere.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${spring.application.name:E-Commerce Platform}")
    private String applicationName;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .description("""
                                Complete REST API for the E-Commerce Platform.
                                
                                ## Features
                                - **Authentication**: JWT-based authentication with role-based access control
                                - **Products**: Full CRUD operations for product management
                                - **Categories**: Product categorization and management
                                - **Cart**: Shopping cart functionality
                                - **Orders**: Order processing and management
                                - **Users**: User management and administration
                                
                                ## Authentication
                                The API uses JWT tokens stored in secure HTTP-only cookies.
                                Include the token in the Authorization header: `Bearer <token>`
                                
                                ## Roles
                                - **CUSTOMER**: Browse products, manage cart, place orders
                                - **SELLER**: Manage products, view orders, track sales
                                - **ADMIN**: Full platform management and administration
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("E-Commerce Team")
                                .email("support@ecommerce.com")
                                .url("https://ecommerce.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ecommerce.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }
    
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("JWT token for authentication. Include 'Bearer ' prefix.");
    }
}
