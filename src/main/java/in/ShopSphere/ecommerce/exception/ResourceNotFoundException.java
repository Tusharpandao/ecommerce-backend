package in.ShopSphere.ecommerce.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
    
    public static ResourceNotFoundException forEntity(String entityName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(entityName, fieldName, fieldValue);
    }
    
    public static ResourceNotFoundException forEntity(String entityName, String id) {
        return new ResourceNotFoundException(entityName, "id", id);
    }
}
