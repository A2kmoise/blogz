inpackage codewithmoise.org.blogbackend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;

    /**
     * Send an email with a Thymeleaf template
     *
     * @param to recipient email address
     * @param subject email subject
     * @param templateName name of the Thymeleaf template (without .html extension)
     * @param variables map of variables to pass to the template
     */
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // Process Thymeleaf template
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(String to, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName
        );
        sendEmail(to, "Welcome to Blog Backend", "emails/welcome", variables);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetLink, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "resetLink", resetLink,
                "expirationTime", "24 hours"
        );
        sendEmail(to, "Reset Your Password", "emails/password-reset", variables);
    }

    /**
     * Send account verification email
     */
    public void sendVerificationEmail(String to, String verificationLink, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "verificationLink", verificationLink
        );
        sendEmail(to, "Verify Your Email Address", "emails/verification", variables);
    }

    /**
     * Send OTP email
     */
    public void sendOtpEmail(String to, String otp, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "otp", otp,
                "expirationTime", "10 minutes"
        );
        sendEmail(to, "Your OTP Code", "emails/otp", variables);
    }

    /**
     * Send blog published notification
     */
    public void sendBlogPublishedNotification(String to, String blogTitle, String blogLink, String authorName) {
        Map<String, Object> variables = Map.of(
                "authorName", authorName,
                "blogTitle", blogTitle,
                "blogLink", blogLink
        );
        sendEmail(to, "New Blog Published: " + blogTitle, "emails/blog-published", variables);
    }
}
