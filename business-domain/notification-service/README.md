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