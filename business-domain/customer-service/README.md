# Customer Microservice

**Referencias**

- [spring-microservices-in-action-2021](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/06.on-service-discovery.md)

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring-cloud.version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
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

## Agrega propiedades de configuración

En el `application.yml` de este microservicio agregaremos el nombre del microservicio y la dirección para comunicarnos
con el servidor de configuraciones:

````yml
spring:
  application:
    name: customer-service
  config:
    import: optional:configserver:http://localhost:8888
````

Las otras configuraciones que tendrá este microservicio lo agregaremos al archivo de la siguiente ruta del servidor de
configuraciones:

`infrastructure/config-server/src/main/resources/config/customer-service.yml`

````yml
server:
  port: 8081
  error:
    include-message: always

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: db_customers
      username: magadiflo
      password: magadiflo
      authentication-database: admin

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
````

**IMPORTANTE**

La configuración `spring.data.mongodb.authentication-database=admin` es importante que lo tengamos definido, porque
sino, nos mostrará el siguiente stack de errores:

````bash
Caused by: com.mongodb.MongoSecurityException: 
Exception authenticating MongoCredential{mechanism=SCRAM-SHA-1, userName='magadiflo', source='db_customers', password=<hidden>, mechanismProperties=<hidden>}

Caused by: com.mongodb.MongoCommandException: 
Command failed with error 18 (AuthenticationFailed): 'Authentication failed.' on server localhost:27017. The full response is {"ok": 0.0, "errmsg": "Authentication failed.", "code": 18, "codeName": "AuthenticationFailed"}
````

La propiedad `authentication-database` en la configuración de MongoDB es importante cuando necesitas especificar la base
de datos que contiene las credenciales de usuario que se utilizarán para autenticarte. Por defecto, MongoDB utiliza la
base de datos `admin` para autenticar a los usuarios si no se especifica otra base de datos.

En nuestra configuración de `Docker Compose`, hemos especificado las credenciales `magadiflo` para el usuario
raíz de MongoDB `(MONGO_INITDB_ROOT_USERNAME)`, que típicamente se almacenan en la base de datos `admin`.
Aquí es donde entra la propiedad `authentication-database` en tu configuración de Spring Boot.

Esta configuración `sí es necesaria` porque le dice a Spring Boot que debe autenticar al usuario `magadiflo` utilizando
las credenciales almacenadas en la base de datos `admin`. Una vez autenticado, el usuario puede acceder a la base de
datos `db_customers` para realizar operaciones.

Si omitimos esta propiedad y las credenciales están en la base de datos `admin` (lo cual es común para las credenciales
de usuario raíz), podríamos encontrar problemas de autenticación, ya que Spring Boot intentaría autenticar al usuario en
la base de datos `db_customers` en lugar de la base de datos `admin`.

### Configura Studio 3T

Para interactuar con la base de datos de MongoDB utilizaremos `Studio 3T`. A continuación se muestra los pasos para
conectarnos a la base de datos de mongodb que se está ejecutando en docker.

Creamos una nueva conexión seleccionando la opción `Manually configure my connection settings`:

![01.new-connection.png](assets/01.new-connection.png)

Luego, en la pestaña de `Authentication` agregamos las credenciales de conexión que están ubicadas en la base de datos
de autenticación `admin`.

![02.test-connection.png](assets/02.test-connection.png)

Aprovechando que establecimos la conexión, podemos ingresar a la base de datos `admin` y ver que efectivamente nuestro
usuario `magadiflo` está registrado en dicha base de datos.

![03.view-user.png](assets/03.view-user.png)

