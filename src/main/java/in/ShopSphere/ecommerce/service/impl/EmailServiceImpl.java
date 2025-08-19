package in.ShopSphere.ecommerce.service.impl;

import in.ShopSphere.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.verification.enabled:true}")
    private boolean emailVerificationEnabled;

    @Override
    @Async
    public void sendEmailVerification(String to, String token, String userName) {
        if (!emailVerificationEnabled) {
            log.info("Email verification disabled, skipping email to: {}", to);
            return;
        }

        String subject = "Verify Your Email - ShopSphere";
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        
        String content = String.format(
            "Hello %s,\n\n" +
            "Please verify your email address by clicking the following link:\n\n" +
            "%s\n\n" +
            "If you didn't create an account, please ignore this email.\n\n" +
            "Best regards,\nShopSphere Team",
            userName, verificationUrl
        );
        
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendPasswordReset(String to, String token, String userName) {
        String subject = "Reset Your Password - ShopSphere";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        String content = String.format(
            "Hello %s,\n\n" +
            "You requested a password reset. Click the following link to reset your password:\n\n" +
            "%s\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\nShopSphere Team",
            userName, resetUrl
        );
        
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to ShopSphere!";
        
        String content = String.format(
            "Hello %s,\n\n" +
            "Welcome to ShopSphere! We're excited to have you on board.\n\n" +
            "Start exploring our amazing products and enjoy your shopping experience.\n\n" +
            "Best regards,\nShopSphere Team",
            userName
        );
        
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendOrderConfirmation(String to, String orderNumber, String userName) {
        String subject = "Order Confirmation - ShopSphere";
        
        String content = String.format(
            "Hello %s,\n\n" +
            "Thank you for your order! Your order number is: %s\n\n" +
            "We'll keep you updated on the status of your order.\n\n" +
            "Best regards,\nShopSphere Team",
            userName, orderNumber
        );
        
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendOrderStatusUpdate(String to, String orderNumber, String status, String userName) {
        String subject = "Order Status Update - ShopSphere";
        
        String content = String.format(
            "Hello %s,\n\n" +
            "Your order %s status has been updated to: %s\n\n" +
            "Track your order on our website for more details.\n\n" +
            "Best regards,\nShopSphere Team",
            userName, orderNumber, status
        );
        
        sendEmail(to, subject, content);
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }


}
