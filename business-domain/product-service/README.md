# Product Service

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring-cloud.version 2023.0.2-->
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
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
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
</dependencies>
````

Por defecto, cuando agregamos la dependencia de `Flyway` se crea en el directorio `/resources` el
directorio `/db/migration`, tal como se ve a continuación:

![01.default-folder-by-flyway.png](assets/01.default-folder-by-flyway.png)

## Configura product service

Vamos a agregar las propiedades de configuración en el `application.yml` del `product service` y también crear un
archivo de configuración para este microservicio que será alojado en el servidor de configuración.

`business-domain/product-service/src/main/resources/application.yml`

````yml
spring:
  application:
    name: product-service
  config:
    import: optional:configserver:http://localhost:8888
````

Ahora, en el servidor de configuraciones creamos el siguiente archivo de configuración y agregamos las propiedades
restantes:

`infrastructure/config-server/src/main/resources/config/product-service.yml`

````yml
server:
  port: 8082
  error:
    include-message: always

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/db_product_service
    username: magadiflo
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  flyway:
    baseline-on-migrate: true
    enabled: true
    baseline-description: 'init'
    baseline-version: 0
    user: ${spring.datasource.username}
    password: ${spring.datasource.password}

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

- `spring.jpa.hibernate.ddl-auto=validate`, la propiedad `spring.jpa.hibernate.ddl-auto` en una aplicación Spring Boot
  se utiliza para definir cómo hibernate debe manejar la creación y actualización de las tablas en la base de datos. El
  valor `validate` significa que Hibernate validará el esquema de la base de datos en comparación con las entidades
  mapeadas, pero no realizará ningún cambio en la base de datos. En nuestro caso,
  con `spring.jpa.hibernate.ddl-auto=validate`, hibernate comprobará que las tablas y columnas en tu base de datos
  coincidan con las entidades definidas en tu aplicación. Si hay alguna discrepancia, Hibernate lanzará una excepción,
  evitando que la aplicación se inicie. Esto es útil para asegurar que la base de datos está en el estado esperado sin
  realizar cambios automáticos.


- Al combinar la propiedad anterior con `Flayway` en el proyecto, `Flayway`  se encargará de manejar las migraciones de
  la base de datos, es decir, las actualizaciones de la estructura de la base de datos a lo largo del tiempo a través de
  scripts SQL predefinidos.


- `spring.flyway.baseline-on-migrate: true`, esta configuración indica a Flyway que debe aplicar una migración base si
  no hay una tabla de control de versiones de Flyway en la base de datos. La migración base se ejecuta la primera vez
  que Flyway se inicia en una base de datos.


- `spring.flyway.enabled: true`, esta propiedad habilita el uso de Flyway para manejar las migraciones de la base de
  datos en tu aplicación. Si se establece en `false`, Flyway no realizará ninguna migración al iniciar la aplicación.


- `spring.flyway.baseline-description: 'init'`, aquí se especifica la descripción que se utilizará para la migración
  base en caso de que se necesite ejecutar. Esta descripción es solo una etiqueta y no afecta el comportamiento de
  Flyway, pero puede ser útil para propósitos de documentación y claridad.


- `spring.flyway.baseline-version: 0`, indica la versión que se asignará a la migración base. En este caso, se establece
  en 0.


- `spring.flyway.user: ${spring.datasource.username}`, esta propiedad indica el usuario de la base de datos que Flyway
  utilizará para conectarse. En este caso, se hace referencia al nombre de usuario definido en las propiedades de
  configuración de la fuente de datos de Spring.


- `spring.flyway.password: ${spring.datasource.password}`, aquí se establece la contraseña del usuario de la base de
  datos que Flyway utilizará para conectarse. Al igual que con el usuario, se hace referencia a la contraseña definida
  en las propiedades de configuración de la fuente de datos de Spring.

