package com.ahorra.common.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.ahorra.common.constants.GeneralConstants.REQUEST_TIMEOUT;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue="mailjet")
public class MailjetDeliveryService implements EmailDeliveryService {

    private static final String SEND_URL = "https://api.mailjet.com/v3.1/send";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mailjet.api-key:}")
    private String apiKey;

    @Value("${mailjet.secret-key:}")
    private String secretKey;

    @Value("${mailjet.sender-email:}")
    private String senderEmail;

    @Value("${mailjet.sender-name:}")
    private String defaultSenderName;

    @Override
    public void sendVerificationOtpEmail(String email, String otp, String appName) {
        String senderName = senderNameFor(appName);
        String subject = "Verify your " + senderName + " email address";
        String textPart = "Your " + senderName + " email verification code is: " + otp
                + "\n\nIt expires shortly. If you didn't request this, you can safely ignore this message.";
        String htmlPart = "<p>Your <strong>" + senderName + "</strong> email verification code is:</p>"
                + "<p style=\"font-size:24px;letter-spacing:4px\"><strong>" + otp + "</strong></p>"
                + "<p>It expires shortly. If you didn't request this, you can safely ignore this message.</p>";

        send(email, senderName, subject, textPart, htmlPart, "Verification");
    }

    @Override
    public void sendPasswordResetOtpEmail(String email, String otp, String appName) {
        String senderName = senderNameFor(appName);
        String subject = "Your " + senderName + " password reset code";
        String textPart = "Your " + senderName + " password reset code is: " + otp
                + "\n\nIt expires shortly. If you didn't request this, you can safely ignore this message.";
        String htmlPart = "<p>Your <strong>" + senderName + "</strong> password reset code is:</p>"
                + "<p style=\"font-size:24px;letter-spacing:4px\"><strong>" + otp + "</strong></p>"
                + "<p>It expires shortly. If you didn't request this, you can safely ignore this message.</p>";

        send(email, senderName, subject, textPart, htmlPart, "Password reset");
    }

    private String senderNameFor(String appName) {
        return (appName == null || appName.isBlank()) ? defaultSenderName : appName;
    }

    private void send(String email, String senderName, String subject, String textPart, String htmlPart, String kind) {
        if (apiKey == null || apiKey.isBlank() || secretKey == null || secretKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            log.error("Mailjet is selected as the email provider but its api-key/secret-key/sender-email are not fully configured.");
            return;
        }

        try {
            Map<String, Object> message = Map.of(
                    "From", Map.of("Email", senderEmail, "Name", senderName),
                    "To", List.of(Map.of("Email", email)),
                    "Subject", subject,
                    "TextPart", textPart,
                    "HTMLPart", htmlPart
            );
            String requestBody = objectMapper.writeValueAsString(Map.of("Messages", List.of(message)));

            String basicAuth = Base64.getEncoder().encodeToString((apiKey + ":" + secretKey).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SEND_URL))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("{} email sent via Mailjet (app={})", kind, senderName);
            } else {
                log.error("Mailjet {} email failed with status {}: {}", kind, response.statusCode(), safeErrorSummary(response.body()));
            }
        } catch (Exception ex) {
            log.error("Failed to send {} email via Mailjet", kind, ex);
        }
    }

    private String safeErrorSummary(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode errorMessage = node.path("ErrorMessage");
            if (!errorMessage.isMissingNode()) {
                return errorMessage.asText();
            }
            JsonNode messages = node.path("Messages");
            if (messages.isArray() && !messages.isEmpty()) {
                return messages.get(0).path("Errors").toString();
            }
            return "unknown error";
        } catch (Exception e) {
            return "unparseable error response";
        }
    }
}