# Payment Service

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring cloud version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
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
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
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

## Agrega propiedades de configuración

En el `application.yml` del microservicio `payment-service` agregamos las siguientes propiedades.

````yml
spring:
  application:
    name: payment-service
  config:
    import: optional:configserver:http://localhost:8888
````

Ahora, en el `payment-service.yml` del servidor de configuraciones agregamos las configuraciones finales de este
microservicio, eso incluye las configuraciones de la aplicación como productor hacia kafka.

````yml
server:
  port: 8084
  error:
    include-message: always

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/db_payment_service
    username: magadiflo
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.type.mapping: paymentNotificationToken:dev.magadiflo.payment.app.models.dtos.PaymentNotification

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

logging:
  level:
    org.hibernate.SQL: DEBUG
````

**DONDE**

- En las configuraciones estamos definiendo la base de datos `db_payment_service`, base de datos que debemos crear antes
  de ejecutar la aplicación.
- Estamos usando la configuración `spring.json.type.mapping` para mapear el token `paymentNotificationToken` al tipo
  `PaymentNotification`. Cualquier consumer que quiera consumir valores de este producer deberá definir la misma
  configuración incluyendo el mismo token `paymentNotificationToken`. **Para más información ver el `README.md`
  del `order-service` donde documento en detalle esta configuración.**

## Crea entidad Payment

Antes de crear la entidad `Payment` debemos crear el enum `PaymentMetod`:

````java
public enum PaymentMethod {
    PAYPAL, CREDIT_CARD, VISA_CARD, MASTER_CARD, BITCOIN
}
````

Ahora, procedemos a crear la entidad `Payment`:

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Long orderId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
````

Nuestra entidad `Payment` llevará anotaciones de auditoría `@CreatedDate` y `@LastModifiedDate`. Para que estas
anotaciones funcionen le agregamos la anotación `@EntityListeners(AuditingEntityListener.class)` a nivel de clase de la
entidad. Además, debemos agregarle una última anotación en la clase principal del microservicio `@EnableJpaAuditing`.

````java

@EnableJpaAuditing
@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
````

## Crea dtos y mapper

Los dtos usados en este microservicio serán las siguientes:

````java
public record PaymentNotification(String orderReference,
                                  BigDecimal amount,
                                  PaymentMethod paymentMethod,
                                  String customerFirstName,
                                  String customerLastName,
                                  String customerEmail) {
}
````

````java
public record Customer(@NotBlank(message = "El nombre es requerido")
                       String firstName,

                       @NotBlank(message = "El apellido es requerido")
                       String lastName,

                       @NotBlank(message = "El correo es requerido")
                       @Email(message = "El correo no tiene un formato válido")
                       String email) {
}
````

````java
public record PaymentRequest(@NotNull(message = "El monto es requerido")
                             @Positive(message = "El monto debe ser positivo")
                             BigDecimal amount,

                             @NotNull(message = "El método de pago es requerido")
                             PaymentMethod paymentMethod,

                             @NotNull(message = "La id de la orden es requerido")
                             Long orderId,

                             String orderReference,

                             @Valid
                             @NotNull(message = "Debe ingresar datos del cliente")
                             Customer customer) {
}
````

Ahora crearemos la clase que mapeará la entidad payment a dto y viceversa:

````java

@Component
public class PaymentMapper {
    public Payment toPayment(PaymentRequest request) {
        return Payment.builder()
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .orderId(request.orderId())
                .build();
    }
}
````

## Crea kafka producer

Antes de crear nuestra clase productora, vamos a crear una clase de configuración para exponer un bean que nos cree el
topic para payment.

````java

@Configuration
public class KafkaPaymentTopicConfig {

    public static final String PAYMENT_TOPIC = "payment-topic";

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(PAYMENT_TOPIC).build();
    }
}
````

Ahora, similar a lo que hicimos en el `order-service`, vamos a crear una clase especializada en el envío o en la
producción de mensajes hacia kafka. En nuestro caso, enviaremos objetos del tipo `PaymentNotification`.

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationProducer {

    public final KafkaTemplate<String, PaymentNotification> kafkaTemplate;

    public void sendNotification(PaymentNotification paymentNotification) {
        log.info("Enviando notificación con la siguiente información: {}", paymentNotification);
        Message<PaymentNotification> message = MessageBuilder
                .withPayload(paymentNotification)
                .setHeader(KafkaHeaders.TOPIC, KafkaPaymentTopicConfig.PAYMENT_TOPIC)
                .build();
        this.kafkaTemplate.send(message);
    }

}
````

## Crea repositorio, servicio y controlador

Empezamos creando el repositorio para nuestra entidad Payment.

````java
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
````

Pasamos a crear el servicio donde definiremos la lógica de la aplicación.

````java
public interface PaymentService {
    Long createPayment(PaymentRequest paymentRequest);
}
````

````java

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public Long createPayment(PaymentRequest paymentRequest) {
        Payment paymentDB = this.paymentRepository.save(this.paymentMapper.toPayment(paymentRequest));
        PaymentNotification paymentNotification =
                new PaymentNotification(paymentRequest.orderReference(),
                        paymentRequest.amount(), paymentRequest.paymentMethod(),
                        paymentRequest.customer().firstName(),
                        paymentRequest.customer().lastName(),
                        paymentRequest.customer().email());
        this.notificationProducer.sendNotification(paymentNotification);
        return paymentDB.getId();
    }
}
````

Finalmente, creamos la clase controladora que llamará al servicio anterior.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> createPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(this.paymentService.createPayment(request), HttpStatus.CREATED);
    }
}
````
