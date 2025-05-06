package com.CapstoneProject.capstone.service.impl.otp;

import com.CapstoneProject.capstone.service.IEmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class EmailService implements IEmailService {

    private final String sendGridApiKey;
    private SendGrid sendGridClient;

    public EmailService(@Value("${sendgrid.api.key}") String sendGridApiKey) {
        this.sendGridApiKey = sendGridApiKey;
    }

    @PostConstruct
    public void init() {
        this.sendGridClient = new SendGrid(sendGridApiKey);
    }

    @Override
    public void sendVerificationEmail(String email, Integer otp) {
        String subject = "OTP Verification";

        String htmlBody = """
            <div style='font-family: Arial, sans-serif; color: #333;'>
                <img src='https://cloud.appwrite.io/v1/storage/buckets/67f65743001a663a4f45/files/67fd23ad946fbf28befe/view?project=67f65524001e056bc8f0' alt='Company Logo' style='max-width: 150px; margin-bottom: 20px;'>
                <h2>Your OTP Code</h2>
                <p>Dear User,</p>
                <p>Thank you for using our service! To complete your verification process, please use the following OTP (One-Time Password) code:</p>
                <h1>%s</h1>
                <p>This code is valid for the next 5 minutes. Please do not share this code with anyone.</p>
            
                <h3>What happens next?</h3>
                <p>Once you've entered the OTP code, you'll be able to complete the verification process and access your account.</p>
            
                <p>If you did not request this code, please disregard this message. If you keep receiving OTPs without making any requests, we recommend updating your account security.</p>
            
                <p>Thank you for choosing our service!</p>
            
                <p>Best regards,<br>Your Company Name Support Team</p>
            
                <p style='font-size: 12px; color: #888;'>If you have any questions, feel free to <a href='mailto:support@yourcompany.com'>contact our support team</a>.</p>
            </div>
            """.formatted(otp);

        sendEmail(email, subject, htmlBody);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            Email from = new Email("ngotungduong121@gmail.com");
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGridClient.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Email sent successfully to " + to);
            } else {
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send email: " + ex.getMessage(), ex);
        }
    }
}