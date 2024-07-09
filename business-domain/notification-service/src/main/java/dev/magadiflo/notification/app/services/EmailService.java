package dev.magadiflo.notification.app.services;

import dev.magadiflo.notification.app.models.dtos.Product;
import dev.magadiflo.notification.app.models.enums.EmailTemplates;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendPaymentSuccessEmail(String destinationEmail, String customerName, BigDecimal amount, String orderReference) {
        try {
            Map<String, Object> variablesMap = new HashMap<>();
            variablesMap.put("customerName", customerName);
            variablesMap.put("amount", amount);
            variablesMap.put("orderReference", orderReference);

            Context context = new Context();
            context.setVariables(variablesMap);

            final String templateName = EmailTemplates.PAYMENT_CONFIRMATION.getTemplate();
            String htmlTemplate = this.templateEngine.process(templateName, context);

            MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setSubject(EmailTemplates.PAYMENT_CONFIRMATION.getSubject());
            helper.setFrom("contact@magadiflo.com");
            helper.setTo(destinationEmail);
            helper.setText(htmlTemplate, true);

            this.javaMailSender.send(mimeMessage);
            log.info("Se ha enviado exitosamente un correo a {} con la plantilla {}", destinationEmail, templateName);
        } catch (MessagingException e) {
            log.warn("No se pudo enviar el correo de pago de confirmación a {}", destinationEmail);
            e.printStackTrace();
        }
    }

    @Async
    public void sendOrderConfirmationEmail(String destinationEmail, String customerName, BigDecimal totalAmount, String orderReference, List<Product> products) {
        try {
            Map<String, Object> variablesMap = new HashMap<>();
            variablesMap.put("customerName", customerName);
            variablesMap.put("totalAmount", totalAmount);
            variablesMap.put("orderReference", orderReference);
            variablesMap.put("products", products);

            Context context = new Context();
            context.setVariables(variablesMap);

            final String templateName = EmailTemplates.ORDER_CONFIRMATION.getTemplate();
            String htmlTemplate = this.templateEngine.process(templateName, context);

            MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setSubject(EmailTemplates.ORDER_CONFIRMATION.getSubject());
            helper.setFrom("contact@magadiflo.com");
            helper.setTo(destinationEmail);
            helper.setText(htmlTemplate, true);

            this.javaMailSender.send(mimeMessage);
            log.info("Envío exitoso de correo correo a {} con la plantilla {}", destinationEmail, templateName);
        } catch (MessagingException e) {
            log.warn("Error al enviar el correo de orden de confirmación a {}", destinationEmail);
            e.printStackTrace();
        }
    }

}
