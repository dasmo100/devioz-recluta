# DeviozRecluta Express - Backend API REST

Proyecto base y arquitectura inicial para la API REST del sistema **DeviozRecluta Express**, desarrollado con **Java 21** y **Spring Boot 3.3.x**.

---

## 📋 Resumen del Proyecto y Responsabilidad (Persona 1)

Este repositorio contiene la configuración base estructural, técnica y de conectividad del backend para el equipo de desarrollo:
- Configuración de dependencias Maven (`pom.xml`).
- Conexión y persistencia con **MySQL** (entorno XAMPP) mediante **Spring Data JPA**.
- Configuración y documentación interactiva con **OpenAPI 3 / Swagger UI**.
- Manejo centralizado y estandarizado de excepciones (`@RestControllerAdvice`).
- Configuración de políticas de CORS para integración con clientes web y móviles.
- Estructura limpia por capas lista para el desarrollo colaborativo (Personas 2, 3, 4, etc.).

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología | Versión |
| :--- | :--- | :--- |
| **Lenguaje** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.3.4 |
| **Persistencia** | Spring Data JPA / Hibernate | 3.3.x |
| **Validación** | Spring Boot Starter Validation | Jakarta Bean Validation |
| **Base de Datos** | MySQL | 8.0+ / MariaDB (XAMPP) |
| **Driver BD** | MySQL Connector/J | 8.3.x |
| **Productividad** | Project Lombok | 1.18.x |
| **Documentación** | SpringDoc OpenAPI Starter | 2.5.0 |
| **Testing** | Spring Boot Starter Test (JUnit 5, Mockito) | 3.3.4 |

---

## 📁 Estructura del Proyecto

El código fuente sigue el estándar de empaquetado por capas dentro del package raíz `com.devioz.recluta`:

```text
devioz-recluta/
├── .gitignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── devioz/
    │   │           └── recluta/
    │   │               ├── DeviozReclutaApplication.java  # Clase principal Spring Boot
    │   │               ├── common/                        # Clases transversales y respuestas estándar
    │   │               │   └── ErrorResponse.java
    │   │               ├── config/                        # Configuraciones (OpenAPI, CORS, etc.)
    │   │               │   ├── CorsConfig.java
    │   │               │   └── OpenApiConfig.java
    │   │               ├── controller/                    # Controladores REST (@RestController)
    │   │               │   └── StatusController.java
    │   │               ├── dto/                           # Data Transfer Objects (Requests / Responses)
    │   │               ├── exception/                     # Excepciones personalizadas y Handler global
    │   │               │   ├── BadRequestException.java
    │   │               │   ├── GlobalExceptionHandler.java
    │   │               │   └── ResourceNotFoundException.java
    │   │               ├── model/                         # Entidades JPA (@Entity)
    │   │               ├── repository/                    # Interfaces de persistencia (JpaRepository)
    │   │               └── service/                       # Interfaces e implementación de reglas de negocio
    │   └── resources/
    │       ├── application.properties                     # Configuración de entorno y propiedades Spring
    │       └── data.sql                                   # Script de inicialización de datos (seed)
    └── test/
        └── java/
            └── com/
                └── devioz/
                    └── recluta/
                        └── DeviozReclutaApplicationTests.java
```

---

## 🗄️ Configuración de la Base de Datos (MySQL - XAMPP)

La aplicación está preconfigurada en `src/main/resources/application.properties` para conectarse a una instancia local de MySQL (XAMPP por defecto):

- **Host y Puerto:** `localhost:3306`
- **Base de datos:** `devioz_recluta` (se crea automáticamente si no existe gracias a `createDatabaseIfNotExist=true`)
- **Usuario:** `root`
- **Contraseña:** *(vacía por defecto en XAMPP)*
- **Zona Horaria:** `America/Lima`
- **Estrategia DDL:** `update` (crea o actualiza las tablas según las entidades JPA)
- **Ejecución diferida de datos:** `spring.jpa.defer-datasource-initialization=true` permite que `data.sql` se ejecute después de crear el esquema de tablas.

---

## 🚀 Guía de Instalación y Ejecución

### Prerrequisitos
1. **JDK 21** instalado y configurado en la variable de entorno `JAVA_HOME`.
2. **Apache Maven 3.8+** instalado.
3. **XAMPP / MySQL** en ejecución (puerto 3306).

### Pasos para iniciar la API

1. **Verificar que MySQL esté activo en XAMPP:**
   Asegúrate de que el módulo **MySQL** esté en estado *Running* en el Panel de Control de XAMPP.

2. **Compilar y empaquetar el proyecto:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Ejecutar el servicio:**
   ```bash
   mvn spring-boot:run
   ```

   O ejecutando directamente el archivo JAR generado:
   ```bash
   java -jar target/devioz-recluta-0.0.1-SNAPSHOT.jar
   ```

---

## 🌐 Endpoints de Verificación y Documentación

Una vez iniciada la aplicación en el puerto `8080`:

| Recurso | URL | Descripción |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interfaz gráfica interactiva para consultar y probar la API |
| **OpenAPI Docs (JSON)** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) | Especificación técnica OpenAPI en formato JSON |
| **Status API** | [http://localhost:8080/api/v1/status](http://localhost:8080/api/v1/status) | Endpoint de verificación de salud del servicio |

---

## 👥 Guía de Colaboración para los Integrantes del Equipo

Para los desarrolladores responsables de implementar los módulos funcionales (candidatos, vacantes, procesos de selección, etc.):

1. **Modelos:** Crear las entidades JPA dentro de `com.devioz.recluta.model` usando anotaciones de Jakarta Persistence (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, etc.) y Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`).
2. **Repositorios:** Definir interfaces que extiendan de `JpaRepository<Entidad, ID>` en `com.devioz.recluta.repository`.
3. **Servicios:** Declarar interfaces y sus implementaciones con `@Service` y `@Transactional` en `com.devioz.recluta.service`.
4. **Controladores:** Implementar clases anotadas con `@RestController`, `@RequestMapping` y documentadas con `@Tag` y `@Operation` de Swagger en `com.devioz.recluta.controller`.
5. **Manejo de Errores:**
   - Para elementos no encontrados, lanzar `throw new ResourceNotFoundException("Mensaje o recurso no encontrado");`.
   - Para peticiones inválidas o lógica inconsistente, lanzar `throw new BadRequestException("Motivo del error");`.
   - `GlobalExceptionHandler` interceptará automáticamente estas excepciones y las convertirá en respuestas JSON con el formato estructurado `ErrorResponse`.
