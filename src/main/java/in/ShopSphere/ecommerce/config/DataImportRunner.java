package in.ShopSphere.ecommerce.config;

import in.ShopSphere.ecommerce.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.data.import.enabled", havingValue = "true")
public class DataImportRunner implements CommandLineRunner {

    private final DataImportService dataImportService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data import process...");
        
        try {
            // Try to import demo products
            String demoFilePath = "demo.json";
            int importedCount = dataImportService.importProductsFromJson(demoFilePath);
            
            log.info("Data import completed successfully. Imported {} products.", importedCount);
            
        } catch (Exception e) {
            log.error("Error during data import: {}", e.getMessage(), e);
        }
    }
}
