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

## Crea las entidades Category y Product

Iniciamos creando nuestras dos entidades que estarán dentro de este microservicio. Establecemos la
relación `bidireccional` entre ambas entidades.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}
````

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double availableQuantity;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
````

## Crea fichero SQL para usar con Flyway

Ahora necesitamos crear el script sql donde definiremos las instrucciones para crear las tablas de la base de datos con
las que trabajaremos en este microservicio. Recordemos que estamos trabajando con `Flyway`, quien nos ayudará a ejecutar
nuestras migracionas. Por lo general, en proyectos anteriores, habíamos dejado que hibernate construya las tablas por
nosotros a partir de las entidades anotadas utilizando la propiedad de configuración `ddl-auto: update`
o `ddl-auto: create-drop`, etc., pero esta vez, lo crearemos manualmente y dejaremos que `Flyway` la ejecute
por nosotros.

Aún seguiremos usando la propiedad `ddl-auto` pero esta vez con el valor `validate`. Cuando iniciemos la aplicación,
Hibernate verificará que la estructura de la base de datos coincida exactamente con las definiciones de las entidades.
Cualquier discrepancia o error relacionado con la estructura de la base de datos se mostrará en los registros de la
aplicación.

Usar `spring.jpa.hibernate.ddl-auto=validate` es una buena práctica para entornos de producción donde deseas que
Hibernate solo valide la estructura existente de la base de datos sin realizar cambios automáticos. Esto ayuda a
prevenir modificaciones accidentales en la estructura de la base de datos y asegura que tu aplicación funcione
correctamente con la configuración de base de datos existente.

Entonces, definiremos las instrucciones de creación de las tablas en la siguiente ruta y archivo:

`business-domain/product-service/src/main/resources/db/migration/V1.0__init_database.sql`

````sql
CREATE TABLE IF NOT EXISTS categories(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    available_quantity DOUBLE PRECISION NOT NULL,
    price NUMERIC(38,2),
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_categories_products FOREIGN KEY(category_id) REFERENCES categories(id)
);
````

**IMPORTANTE**

> Para que `Flyway` pueda procesar los ficheros SQL es muy importante nombrar adecuadamente el fichero con la
> siguiente estructura de nombre:
>
> `V{número}__{nombre_del_archivo}.sql`, ojo que después del número son 2 guiones bajos (__).
>
> Por ejemplo:
>
> `V1.0__create_tables.sql`

## Ejecuta aplicación e inicia migración con Flyway

Una vez que tengamos configurado las propiedades de `Flyway` en el `product-service.yml` y creado nuestra primera
migración `V1.0__init_database.sql`, vamos a ejecutar la aplicación.

Al ejecutar la aplicación veremos en el log la siguiente información correspondiente a la ejecución de `flyway` y a la
migración del script que hemos creado.

````bash
org.flywaydb.core.FlywayExecutor         : Database: jdbc:postgresql://localhost:5435/db_product_service (PostgreSQL 15.2)
o.f.c.i.s.JdbcTableSchemaHistory         : Schema history table "public"."flyway_schema_history" does not exist yet
o.f.core.internal.command.DbValidate     : Successfully validated 1 migration (execution time 00:00.045s)
o.f.c.i.s.JdbcTableSchemaHistory         : Creating Schema History table "public"."flyway_schema_history" ...
o.f.core.internal.command.DbMigrate      : Current version of schema "public": << Empty Schema >>
o.f.core.internal.command.DbMigrate      : Migrating schema "public" to version "1.0 - init database"
o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema "public", now at version v1.0 (execution time 00:00.069s)
````

Si revisamos las tablas generadas, veremos las dos tablas definidas en la migración `V1.0__init_database.sql` y una
tercera tabla que nos crea el propio `Flyway` para que lleve el historial de ejecución de las migraciones.

![02.db_product_service.png](assets/02.db_product_service.png)

Si revisamos los datos que contiene la tabla `flyway_schema_history` veremos que está el registro correspondiente a la
ejecución de la primera migración:

![03.first_migration.png](assets/03.first_migration.png)
