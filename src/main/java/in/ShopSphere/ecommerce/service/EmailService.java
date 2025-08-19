package in.ShopSphere.ecommerce.service;

public interface EmailService {
    
    /**
     * Send email verification email
     * @param to recipient email
     * @param token verification token
     * @param userName recipient name
     */
    void sendEmailVerification(String to, String token, String userName);
    
    /**
     * Send password reset email
     * @param to recipient email
     * @param token reset token
     * @param userName recipient name
     */
    void sendPasswordReset(String to, String token, String userName);
    
    /**
     * Send welcome email
     * @param to recipient email
     * @param userName recipient name
     */
    void sendWelcomeEmail(String to, String userName);
    
    /**
     * Send order confirmation email
     * @param to recipient email
     * @param orderNumber order number
     * @param userName recipient name
     */
    void sendOrderConfirmation(String to, String orderNumber, String userName);
    
    /**
     * Send order status update email
     * @param to recipient email
     * @param orderNumber order number
     * @param status new status
     * @param userName recipient name
     */
    void sendOrderStatusUpdate(String to, String orderNumber, String status, String userName);
    
    /**
     * Send generic email
     * @param to recipient email
     * @param subject email subject
     * @param content email content
     */
    void sendEmail(String to, String subject, String content);
}
