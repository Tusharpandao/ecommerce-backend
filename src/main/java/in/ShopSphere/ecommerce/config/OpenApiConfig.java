package in.ShopSphere.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class OpenApiConfig {
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Create JavaTimeModule with custom serializers
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Configure date/time serializers
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        
        mapper.registerModule(javaTimeModule);
        
        return mapper;
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Commerce Platform API")
                .version("1.0.0")
                .description("A comprehensive REST API for the E-Commerce Platform")
                .contact(new Contact()
                    .name("ShopSphere Team")
                    .email("support@shopsphere.com")
                    .url("https://shopsphere.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addServersItem(new Server()
                .url("http://localhost:8080")
                .description("Development server"))
            .addServersItem(new Server()
                .url("https://api.shopsphere.com")
                .description("Production server"));
    }
}
