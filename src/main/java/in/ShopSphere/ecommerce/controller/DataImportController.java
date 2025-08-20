package in.ShopSphere.ecommerce.controller;

import in.ShopSphere.ecommerce.dto.common.ApiResponse;
import in.ShopSphere.ecommerce.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/data-import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Import", description = "Data import operations for administrators")
@PreAuthorize("hasRole('ADMIN')")
public class DataImportController {

    private final DataImportService dataImportService;
    
    private static final String UPLOAD_DIR = "temp-uploads";

    @PostMapping("/products/upload")
    @Operation(summary = "Upload and import products from JSON file", 
               description = "Upload a JSON file containing product data and import it into the database")
    public ResponseEntity<ApiResponse<String>> uploadAndImportProducts(@RequestParam("file") MultipartFile file) {
        try {
            // Create temp directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".json";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save uploaded file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("File uploaded successfully: {}", filePath);
            
            // Import products from the uploaded file
            int importedCount = dataImportService.importProductsFromJson(filePath.toString());
            
            // Clean up uploaded file
            Files.deleteIfExists(filePath);
            
            String message = String.format("Successfully imported %d products from file: %s", 
                importedCount, originalFilename);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .data("Import completed successfully")
                .build());
                
        } catch (IOException e) {
            log.error("Error processing uploaded file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                .success(false)
                .message("Error processing uploaded file: " + e.getMessage())
                .build());
        } catch (Exception e) {
            log.error("Error during product import: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                .success(false)
                .message("Error during product import: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/products/demo")
    @Operation(summary = "Import demo products", 
               description = "Import the demo.json file that contains sample product data")
    public ResponseEntity<ApiResponse<String>> importDemoProducts() {
        try {
            String demoFilePath = "demo.json";
            File demoFile = new File(demoFilePath);
            
            if (!demoFile.exists()) {
                return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Demo file not found. Please ensure demo.json exists in the project root.")
                    .build());
            }
            
            log.info("Starting import of demo products from: {}", demoFilePath);
            int importedCount = dataImportService.importProductsFromJson(demoFilePath);
            
            String message = String.format("Successfully imported %d demo products", importedCount);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .data("Demo import completed successfully")
                .build());
                
        } catch (Exception e) {
            log.error("Error during demo product import: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                .success(false)
                .message("Error during demo product import: " + e.getMessage())
                .build());
        }
    }

    @PostMapping("/products/file")
    @Operation(summary = "Import products from specific file path", 
               description = "Import products from a JSON file at a specific path on the server")
    public ResponseEntity<ApiResponse<String>> importProductsFromPath(@RequestParam String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("File not found: " + filePath)
                    .build());
            }
            
            log.info("Starting import of products from: {}", filePath);
            int importedCount = dataImportService.importProductsFromJson(filePath);
            
            String message = String.format("Successfully imported %d products from: %s", 
                importedCount, filePath);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .data("Import completed successfully")
                .build());
                
        } catch (Exception e) {
            log.error("Error during product import from path: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                .success(false)
                .message("Error during product import: " + e.getMessage())
                .build());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get import status", 
               description = "Check the status of data import operations")
    public ResponseEntity<ApiResponse<String>> getImportStatus() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
            .success(true)
            .message("Data import service is running")
            .data("Ready for import operations")
            .build());
    }
}
