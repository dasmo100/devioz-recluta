# DeviozRecluta Express - Backend API REST

API REST integral para la gestión ágil y eficiente del proceso de reclutamiento y selección de talento, desarrollada con **Java 21**, **Spring Boot 3.3.4** y **Spring Data JPA**.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología | Versión |
| :--- | :--- | :--- |
| **Lenguaje** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.3.4 |
| **Persistencia** | Spring Data JPA / Hibernate ORM | 6.5.x |
| **Validación** | Jakarta Bean Validation (Hibernate Validator) | 3.0.x |
| **Base de Datos** | MySQL / MariaDB (entorno XAMPP) | 8.0+ / 10.4+ |
| **Driver BD** | MySQL Connector/J | 8.3.x |
| **Productividad** | Project Lombok | 1.18.x |
| **Documentación** | SpringDoc OpenAPI Starter (Swagger UI 3) | 2.5.0 |
| **Testing** | JUnit 5, Mockito, Spring Boot Test (MockMvc) | 3.3.4 |

---

## 🗄️ Configuración de Base de Datos y Modelo de Datos

La aplicación utiliza **MySQL** configurado en `src/main/resources/application.properties`:
- **URL JDBC:** `jdbc:mysql://localhost:3306/devioz_recluta?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true`
- **Usuario:** `root`
- **Contraseña:** *(vacía por defecto en XAMPP)*
- **Hibernate DDL Auto:** `update` (crea o actualiza el esquema relacional de forma automática).

### Entidades y Tablas Principales (5 tablas generadas por Hibernate):
1. **`vacantes`**: Catálogo de oportunidades laborales (`titulo`, `cliente`, `seniority`, `salario_minimo`, `salario_maximo`, `modalidad`, `estado`).
2. **`postulantes`**: Registro de candidatos (`nombres`, `apellidos`, `dni` [único, 8 dígitos], `email` [único], `telefono`, `anios_experiencia`, `habilidades`, `fecha_registro`).
3. **`postulaciones`**: Vínculo entre un candidato y una vacante (`postulante_id`, `vacante_id`, `estado`, `fecha`). Posee restricción única de postulación por vacante.
4. **`historial_estados`**: Auditoría y trazabilidad cronológica de cambios de estado (`postulacion_id`, `estado_anterior`, `estado_nuevo`, `fecha_cambio`, `comentario`).
5. **`entrevistas`**: Gestión de agenda técnica y RRHH (`postulacion_id`, `entrevistador`, `fecha_hora`, `duracion_minutos`, `estado`, `puntaje`, `comentario`).

### Inicialización de Datos (`data.sql`):
Al iniciar la aplicación con `spring.jpa.defer-datasource-initialization=true`, se ejecuta automáticamente `src/main/resources/data.sql` precargando:
- **3 Vacantes de prueba** con distintos niveles de seniority (`SENIOR`, `SEMI_SENIOR`, `JUNIOR`) y modalidades.
- **8 Postulantes de prueba** con perfiles técnicos completos.

---

## 🚀 Guía de Instalación y Ejecución

### Prerrequisitos
1. **Java Development Kit (JDK) 21** instalado y configurado en `JAVA_HOME`.
2. **Apache Maven 3.8+** instalado.
3. **MySQL (XAMPP)** en ejecución escuchando en el puerto `3306`.

### Pasos para Levantar la Aplicación
1. **Iniciar el servicio MySQL:**
   - Asegúrate de que MySQL esté activo en el Panel de Control de XAMPP (puerto 3306).
2. **Compilar y verificar el proyecto:**
   ```bash
   mvn clean compile
   ```
3. **Iniciar el servidor Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```
   *La aplicación arrancará por defecto en el puerto `8080`.*

---

## 🧪 Ejecución de Pruebas Unitarias y de Integración

El proyecto cuenta con una completa suite de pruebas automatizadas con **JUnit 5**, **Mockito** y **MockMvc** para controladores y servicios:
- **80 pruebas automatizadas** que cubren validaciones de campos, reglas de negocio, cruces de horarios y respuestas REST.

Para ejecutar toda la suite de pruebas:
```bash
mvn test
```

Para generar el empaquetado JAR validando las pruebas:
```bash
mvn clean package
```

---

## 📖 Documentación de la API (Swagger UI / OpenAPI)

Una vez levantada la aplicación, accede a la documentación interactiva y sandbox de pruebas:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Schema (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📌 Catálogo de Endpoints por Módulos

### 🔷 MÓDULO 1: Gestión de Vacantes (`com.devioz.recluta.vacantes`)

| Método | Endpoint | Descripción | Respuestas |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/vacantes` | Lista todas las vacantes. Permite filtrar por `?estado=ABIERTA` o `CERRADA`. | `200 OK` |
| `GET` | `/api/vacantes/{id}` | Obtiene el detalle de una vacante por su ID. | `200 OK`, `404 Not Found` |
| `POST` | `/api/vacantes` | Registra una nueva vacante en estado `ABIERTA`. Valida salarios (`min <= max`). | `201 Created`, `400 Bad Request` |
| `PUT` | `/api/vacantes/{id}` | Modifica los datos de una vacante. No permite modificar vacantes `CERRADA`. | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `PATCH` | `/api/vacantes/{id}/cerrar` | Cierra administrativamente una vacante abierta. | `200 OK`, `400 Bad Request`, `404 Not Found` |

---

### 🔷 MÓDULO 2: Gestión de Postulantes (`com.devioz.recluta.postulantes`)

| Método | Endpoint | Descripción | Respuestas |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/postulantes` | Listado paginado de candidatos con filtro opcional de búsqueda `?q=` (nombre, apellido o DNI). | `200 OK` |
| `GET` | `/api/postulantes/{id}` | Consulta el perfil completo de un candidato por ID. | `200 OK`, `404 Not Found` |
| `POST` | `/api/postulantes` | Registra un nuevo candidato. Valida formato de DNI (8 dígitos) y unicidad de DNI y email. | `201 Created`, `400 Bad Request` |
| `PUT` | `/api/postulantes/{id}` | Actualiza información del candidato validando colisiones de DNI o correo. | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `DELETE`| `/api/postulantes/{id}` | Elimina un postulante si no tiene postulaciones registradas en el sistema. | `204 No Content`, `400 Bad Request`, `404 Not Found` |

---

### 🔷 MÓDULO 3: Postulaciones y Ciclo de Estados (`com.devioz.recluta.postulaciones`)

| Método | Endpoint | Descripción | Respuestas |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/vacantes/{id}/postulaciones` | Registra la postulación de un candidato a una vacante. Valida vacante `ABIERTA` y postulación única. | `201 Created`, `400 Bad Request`, `404 Not Found` |
| `GET` | `/api/vacantes/{id}/postulaciones` | Lista todas las postulaciones asociadas a una vacante específica. | `200 OK`, `404 Not Found` |
| `PATCH`| `/api/postulaciones/{id}/estado` | Transición controlada en el embudo (`POSTULADO` ➔ `EN_REVISION` ➔ `ENTREVISTA` ➔ `CONTRATADO` / `RECHAZADO`). Si se marca `CONTRATADO`, la vacante se cierra automáticamente. | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `GET` | `/api/postulaciones/{id}/historial` | Consulta la bitácora cronológica de auditoría de cambios de estado. | `200 OK`, `404 Not Found` |

---

### 🔷 MÓDULO 4: Entrevistas y Resumen Ejecutivo (`com.devioz.recluta.entrevistas`)

| Método | Endpoint | Descripción | Respuestas |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/postulaciones/{id}/entrevistas` | Agenda una entrevista. Requiere que la postulación esté en `ENTREVISTA`, fecha futura y sin cruces de horario para el entrevistador. | `201 Created`, `400 Bad Request`, `404 Not Found` |
| `PATCH`| `/api/entrevistas/{id}/resultado` | Registra resultado (`REALIZADA` o `CANCELADA`). Para `REALIZADA` exige puntaje obligatorio de 1 a 5. | `200 OK`, `400 Bad Request`, `404 Not Found` |
| `GET` | `/api/entrevistas` | Lista entrevistas ordenadas cronológicamente. Soporta filtro opcional `?fecha=YYYY-MM-DD`. | `200 OK` |
| `GET` | `/api/resumen` | Genera dashboard con total de vacantes abiertas, postulaciones en los 5 estados y entrevistas del día de hoy. | `200 OK` |

---

## 🛡️ Manejo Global de Errores

El proyecto cuenta con un `@RestControllerAdvice` (`GlobalExceptionHandler`) que estandariza todas las respuestas de error en un formato JSON consistente (`ErrorResponse`):
- `400 Bad Request`: Validaciones de campos de entrada (`MethodArgumentNotValidException`) e incumplimiento de reglas del negocio (`BusinessRuleException`).
- `404 Not Found`: Entidades o recursos no localizados (`ResourceNotFoundException`).
- `500 Internal Server Error`: Fallos no controlados del servidor.
