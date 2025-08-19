package in.ShopSphere.ecommerce.exception;

public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static BusinessException insufficientStock(String productName, int requested, int available) {
        return new BusinessException(
            String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", 
                productName, requested, available)
        );
    }
    
    public static BusinessException productNotActive(String productName) {
        return new BusinessException(String.format("Product '%s' is not active", productName));
    }
    
    public static BusinessException userBlocked(String email) {
        return new BusinessException(String.format("User account '%s' is blocked", email));
    }
    
    public static BusinessException orderCannotBeModified(String orderId, String reason) {
        return new BusinessException(
            String.format("Order '%s' cannot be modified: %s", orderId, reason)
        );
    }
    
    public static BusinessException invalidOrderStatus(String orderId, String currentStatus, String requiredStatus) {
        return new BusinessException(
            String.format("Order '%s' has status '%s' but requires status '%s'", 
                orderId, currentStatus, requiredStatus)
        );
    }
}
