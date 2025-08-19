package in.ShopSphere.ecommerce.exception;

public class ValidationException extends RuntimeException {
    
    private final String field;
    private final String code;
    
    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.code = "VALIDATION_ERROR";
    }
    
    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
        this.code = "VALIDATION_ERROR";
    }
    
    public ValidationException(String message, String field, String code) {
        super(message);
        this.field = field;
        this.code = code;
    }
    
    public String getField() {
        return field;
    }
    
    public String getCode() {
        return code;
    }
    
    public static ValidationException invalidEmail(String email) {
        return new ValidationException("Invalid email format", "email", "INVALID_EMAIL");
    }
    
    public static ValidationException invalidPassword(String reason) {
        return new ValidationException("Invalid password: " + reason, "password", "INVALID_PASSWORD");
    }
    
    public static ValidationException invalidPrice(String price) {
        return new ValidationException("Invalid price: " + price, "price", "INVALID_PRICE");
    }
    
    public static ValidationException invalidQuantity(int quantity) {
        return new ValidationException("Invalid quantity: " + quantity, "quantity", "INVALID_QUANTITY");
    }
    
    public static ValidationException requiredField(String fieldName) {
        return new ValidationException("Field '" + fieldName + "' is required", fieldName, "REQUIRED_FIELD");
    }
    
    public static ValidationException fieldTooLong(String fieldName, int maxLength) {
        return new ValidationException(
            "Field '" + fieldName + "' exceeds maximum length of " + maxLength, 
            fieldName, 
            "FIELD_TOO_LONG"
        );
    }
}
