# Notification Service

---

## Dependencias

````xml
<!--Java 21-->
<!--Spring Boot 3.3.1-->
<!--Spring Cloud Version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configura aplicación

En el archivo `application.yml` del microservicio `notification-service` agregamos la siguiente configuración:

````yml
spring:
  application:
    name: notification-service
  config:
    import: optional:configserver:http://localhost:8888
````

Así mismo, en el servidor de configuraciones creamos un archivo de configuración `notification-service.yml` para
nuestro microservicio:

````yml
server:
  port: 8085
  error:
    include-message: always

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: db_notifications
      username: magadiflo
      password: magadiflo
      authentication-database: admin

  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: '*'
        spring.json.type.mapping: paymentNotificationToken:dev.magadiflo.notification.app.models.dtos.PaymentConfirmation, orderConfirmationToken:dev.magadiflo.notification.app.models.dtos.OrderConfirmation

  mail:
    host: localhost
    port: 1025
    username: magadiflo
    password: magadiflo
    default-encoding: UTF-8
    properties:
      mail:
        mime:
          charset: UTF-8
        smtp:
          trust: '*'
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000
          auth: true
          starttls:
            enable: true

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
````

De las configuraciones anteriores, es importante hablar de la configuración de `kafka consumer`, para ser más exactos
de la configuración `spring.json.type.mapping` **¿de dónde sale los valores para esa configuración?**.

Si recordamos, en los microservicios `order-service/README.md` y `payment-service/README.md` documentamos algo similar
para el `kafka producer`.

Dijimos que la configuración `spring.json.type.mapping`, va a consistir en una lista delimitada por
comas de pares `token:className`. Para nuestro caso (notification-service), tenemos dos pares de `token:className`,
cada uno corresponde a los objetos de los topics que vamos a consumir a través de este microservicio.

En estas configuraciones tenemos dos pares de `token:className`. A continuación las mostramos.

1. `paymentNotificationToken:dev.magadiflo.notification.app.models.dtos.PaymentConfirmation`
2. `orderConfirmationToken:dev.magadiflo.notification.app.models.dtos.OrderConfirmation`

Analicemos el primer par. El `paymentNotificationToken` es el token que hemos establecido en el microservicio
`payment-service`, para ser exactos en su archivo `payment-service.yml`. Ese token del `payment-service` está mapeado
a una clase de ese microservicio llamada `PaymentNotification`, mientras que en este microservicio de notificaciones el
token `paymentNotificationToken` lo mapearemos a una clase llamada `PaymentConfirmation` ubicada en la dirección que
se muestra. Lo importante aquí, es que las clases que están mapeadas al token `paymentNotificationToken` tengan las
mismas propiedades con los mismos tipos de datos, es decir, el nombre de las clases pueden ser distintas, incluso la
ubicación de las mismas, pero el nombre de los atributos y sus tipos sí deben ser los mismos.

Analicemos el segundo par. El `orderConfirmationToken` es el token que hemos establecido en el microservicio
`order-service`, para ser exactos en su archivo `order-service.yml`. Ese token del `order-service` está mapeado
a una clase de ese microservicio llamado `OrderConfirmation`, mientras que en este microservicio de notificaciones
el token `orderConfirmationToken` lo mapearemos a una clase llamada `OrderConfirmation` ubicada en la dirección que se
muestra. Aquí es importante notar, que a diferencia del primer par de `token:className`, las clases que se mapean al
token `orderConfirmationToken` de ambos microservicios tienen el mismo nombre, aunque podrían haber tenido distintos
nombres similar al caso anterior, lo importante, vuelvo a recalcar es que el nombre de los atributos y el tipo de dato
deben ser el mismo en ambas clases.

Como conclusión, podríamos decir que tanto el `producer` como el `consumer` deben usar el mismo `token`, mientras
que su `className` debe tener los mismos atributos con los mismos tipos de datos.

**¿Para qué realizamos ese mapeo?** para que el proceso de `serialización/deserialización` funcione correctamente, dado
que estamos usando el `JsonDeserializer` en este `consumer` y el `JsonSeralizer` en los `producer`.

Para finalizar este apartado, las clases que crearemos `PaymentConfirmation` y `OrderConfirmation` serán las que
utilicemos como parámetro de los métodos anotados con `@KafkaListener`. Esto lo veremos más adelante cuando
documentemos la clase `NotificationConsumer`.

## Crea documentos, dtos, y enums

Como estamos trabajando con `MongoDB` creamos nuestro documento `Notification` que estará mapeada a una colección
llamada `notifications`. Recordemos que el `notifications` es como si fuera el nombre de la tabla en la base de datos.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private NotificationType type;
    private LocalDateTime notificationDate;
    private OrderConfirmation orderConfirmation;
    private PaymentConfirmation paymentConfirmation;
}
````

El documento anterior usa dos records `OrderConfirmation` y `PaymentConfirmation`:

````java
public record OrderConfirmation(String orderReference,
                                BigDecimal totalAmount,
                                PaymentMethod paymentMethod,
                                Customer customer,
                                List<Product> products) {
}
````

````java
public record PaymentConfirmation(String orderReference,
                                  BigDecimal amount,
                                  PaymentMethod paymentMethod,
                                  String customerFirstName,
                                  String customerLastName,
                                  String customerEmail) {
}
````

El record `OrderConfirmation` usa dos records adicionales:

````java
public record Customer(String id,
                       String firstName,
                       String lastName,
                       String email) {
}
````

````java
public record Product(Long productId,
                      String name,
                      String description,
                      BigDecimal price,
                      double quantity) {
}
````

Finalmente, definiremos los enums que usaremos en la aplicación:

````java

@Getter
public enum EmailTemplates {
    PAYMENT_CONFIRMATION("payment-confirmation.html", "Pago procesado exitosamente"),
    ORDER_CONFIRMATION("order-confirmation.html", "Confirmación de orden");

    private final String template;
    private final String subject;

    EmailTemplates(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
````

````java
public enum NotificationType {
    ORDER_CONFIRMATION, PAYMENT_CONFIRMATION
}
````

````java
public enum PaymentMethod {
    PAYPAL, CREDIT_CARD, VISA_CARD, MASTER_CARD, BITCOIN
}
````

## Crea repositorio con MongoRepository

Como estamos trabajando con `MondoDB` y hemos agregado como dependencia al `pom.xml`
`spring-boot-starter-data-mongodb`, vamos a crear nuestro repositorio para interactuar fácilmente con la base de datos
no relacional.

````java
public interface NotificationRepository extends MongoRepository<Notification, String> {
}
````

## Crea el servicio para enviar correo

Nuestra clase de servicio tendrá dos métodos, uno para enviar correo cuando el proceso de pago se ha realizado
correctamente y el otro método para confirmar la orden.

````java

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
````

Como hemos creado métodos que usan la anotación `@Async`, debemos habilitar el uso de dichas anotaciones para el
procesamiento asíncrono de los métodos anotados. En ese sentido, agregamos la anotación `@EnableAsync` a la clase
principal de este microservicio.

````java

@EnableAsync
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
````

Los métodos que envían correo necesitan una plantilla cada uno para construir el cuerpo del mismo, en ese sentido,
debemos crear un directorio `/resources/templates` y dentro de él las dos plantillas para construir el cuerpo de
nuestros correos.

`business-domain/notification-service/src/main/resources/templates/order-confirmation.html`

````html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Details</title>

    <style>
        body {
          font-family: Arial, sans-serif;
          line-height: 1.6;
          background-color: #f4f4f4;
          margin: 0;
          padding: 0;
        }

        .container {
          max-width: 800px;
          margin: 0 auto;
          padding: 20px;
          background-color: #fff;
          border-radius: 8px;
          box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        h1 {
          color: #333;
        }

        table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 20px;
        }

        th, td {
          padding: 12px;
          border: 1px solid #ddd;
          text-align: left;
        }

        th {
          background-color: #007BFF;
          color: #fff;
        }

        .footer {
          margin-top: 20px;
          padding-top: 10px;
          border-top: 1px solid #ddd;
          text-align: center;
        }
    </style>
</head>

<body>
<div class="container">
    <h1>Order Details</h1>
    <p>Customer: <span th:text="${customerName}"></span></p>
    <p>Order ID: <span th:text="${orderReference}"></span></p>

    <table>
        <thead>
        <tr>
            <th>Product Name</th>
            <th>Quantity</th>
            <th>Price</th>
        </tr>
        </thead>
        <tbody>
        <tr th:each="product : ${products}">
            <td th:text="${product.name}"></td>
            <td th:text="${product.quantity}"></td>
            <td th:text="${product.price}"></td>
        </tr>
        </tbody>
    </table>

    <div class="footer">
        <p>Total Amount: S/ <span th:text="${totalAmount}"></span></p>
        <p>This is an automated message. Please do not reply to this email.</p>
        <p>&copy; 2024 Magadiflo</span>. All rights reserved.</p>
    </div>
</div>
</body>

</html>
````

`business-domain/notification-service/src/main/resources/templates/payment-confirmation.html`

````html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Confirmation</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        h1 {
            color: #333;
        }

        p {
            color: #555;
        }

        .button {
            display: inline-block;
            padding: 10px 20px;
            text-align: center;
            text-decoration: none;
            color: #fff;
            background-color: #007BFF;
            border-radius: 5px;
        }

        .footer {
            margin-top: 20px;
            padding-top: 10px;
            border-top: 1px solid #ddd;
            text-align: center;
        }
    </style>
</head>

<body>
<div class="container">
    <h1>Payment Confirmation</h1>
    <p>Dear <span th:text="${customerName}"></span>,</p>
    <p>Your payment of S/ <span th:text="${amount}"></span> has been successfully processed.</p>
    <p>Order reference: <span th:text="${orderReference}"></span></p>
    <p>Thank you for choosing our service. If you have any questions, feel free to contact us.</p>

    <div class="footer">
        <p>This is an automated message. Please do not reply to this email.</p>
        <p>&copy; 2024 Magadiflo. All rights reserved.</p>
    </div>
</div>
</body>

</html>
````

## Crea métodos con @KafkaListener

Creamos una clase que definirá métodos anotados con `@KafkaListener`. Estos métodos estarán pendientes de los topics
para el payment y para el order.

````java

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-topic", groupId = "payment-notification")
    public void consumePaymentConfirmationNotification(PaymentConfirmation paymentConfirmation) {
        log.info("Consumiendo mensaje desde el topic payment-topic: {}", paymentConfirmation);
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .paymentConfirmation(paymentConfirmation)
                .build();
        this.notificationRepository.save(notification);

        String destinationEmail = paymentConfirmation.customerEmail();
        String customerName = "%s %s".formatted(paymentConfirmation.customerFirstName(), paymentConfirmation.customerLastName());
        BigDecimal amount = paymentConfirmation.amount();
        String orderReference = paymentConfirmation.orderReference();

        this.emailService.sendPaymentSuccessEmail(destinationEmail, customerName, amount, orderReference);
    }

    @KafkaListener(topics = "order-topic", groupId = "order-notification")
    public void consumeOrderConfirmationNotification(OrderConfirmation orderConfirmation) {
        log.info("Consumiendo mensaje desde el topic order-topic: {}", orderConfirmation);
        Notification notification = Notification.builder()
                .type(NotificationType.ORDER_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .orderConfirmation(orderConfirmation)
                .build();
        this.notificationRepository.save(notification);

        String destinationEmail = orderConfirmation.customer().email();
        String customerName = "%s %s".formatted(orderConfirmation.customer().firstName(), orderConfirmation.customer().lastName());
        BigDecimal totalAmount = orderConfirmation.totalAmount();
        String orderReference = orderConfirmation.orderReference();
        List<Product> products = orderConfirmation.products();

        this.emailService.sendOrderConfirmationEmail(destinationEmail, customerName, totalAmount, orderReference, products);
    }

}
````
