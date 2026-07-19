# Plataforma de Aprendizaje - Spring Boot

## Descripción

Este proyecto corresponde a una plataforma de aprendizaje en línea desarrollada con Spring Boot. La aplicación permite gestionar usuarios, cursos, inscripciones, evaluaciones, pagos y notificaciones mediante endpoints REST.

Los servicios backend mantienen una arquitectura por capas, separando controladores, servicios, repositorios, modelos, DTO, configuración y manejo de errores. El sistema utiliza una arquitectura distribuida basada en microservicios, con API Gateway, BFF, autenticación mediante Microsoft Entra ID y despliegue mediante contenedores.

La plataforma utiliza AWS S3 para almacenar archivos físicos asociados al resumen de inscripción. Esta funcionalidad permite generar, subir, descargar, modificar y eliminar el archivo del resumen desde un bucket en la nube.

---

## Arquitectura

La plataforma se compone de los siguientes servicios y componentes:

- **Microsoft Entra ID**: autenticación y emisión de tokens OAuth2/JWT.
- **Curso Service**: gestión del catálogo y CRUD de cursos.
- **Inscripciones Service**: gestión de inscripciones, evaluaciones, pagos, notificaciones, Oracle y AWS S3.
- **BFF Service**: coordinación del flujo integrado de inscripción y mensajería.
- **Frontend, API Gateway y Azure API Management**: interfaz web, seguridad y enrutamiento de solicitudes.

La comunicación entre los servicios utiliza REST con JSON y mensajería asíncrona mediante RabbitMQ.

La explicación completa de la arquitectura y el diagrama se encuentran en:

```txt
docs/arquitectura_micro.md
```

---

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Cloud Gateway
- Spring Security y OAuth2 Resource Server
- Spring Data JPA
- Spring Boot Actuator
- Spring AOP
- Oracle Database
- Oracle JDBC Driver
- Hibernate
- HikariCP
- Maven Wrapper
- AWS S3
- AWS SDK for Java
- Spring AMQP
- RabbitMQ
- Docker y Docker Compose
- Docker Hub
- GitHub Actions
- AWS EC2
- Microsoft Entra ID
- Azure API Management
- Vite, JavaScript, MSAL y Nginx
- Postman / PowerShell

---

## Estructura principal

```txt
src/main/java/com/duoc/LearningPlatformValidation
├── aspect
├── config
├── controller
├── dto
├── exception
├── model
├── repository
└── service
```

La aplicación mantiene una separación por capas:

- `controller`: recibe las solicitudes HTTP.
- `service`: contiene la lógica de negocio.
- `repository`: comunica la aplicación con la base de datos.
- `model`: define las entidades del sistema.
- `dto`: define objetos de transferencia de datos.
- `exception`: centraliza el manejo de errores.
- `aspect`: contiene la lógica transversal de logging.
- `config`: contiene configuraciones adicionales, como la conexión con AWS S3.

---

## Configuración de base de datos

El proyecto utiliza Oracle Database como motor principal. La conexión se realiza mediante Spring Data JPA, Hibernate y HikariCP.

Para evitar dejar credenciales directamente en el repositorio, la configuración usa variables de entorno:

```txt
DB_URL
DB_USERNAME
DB_PASSWORD
DB_DRIVER
DB_DIALECT
```

Ejemplo en PowerShell:

```powershell
$env:DB_URL="jdbc:oracle:thin:@..."
$env:DB_USERNAME="LPV_APP"
$env:DB_PASSWORD="********"
$env:DB_DRIVER="oracle.jdbc.OracleDriver"
$env:DB_DIALECT="org.hibernate.dialect.OracleDialect"
```

Aunque en algunos recursos se menciona H2 como alternativa para pruebas locales, en este proyecto se utiliza Oracle Database, ya que corresponde al motor usado durante la asignatura.

---

## Configuración de AWS S3

El proyecto incorpora almacenamiento de archivos en AWS S3 para guardar los resúmenes de inscripción generados por el sistema.

Bucket utilizado:

```txt
lpv-resumenes-inscripcion
```

Región:

```txt
us-east-1
```

La configuración se realiza mediante variables de entorno:

```txt
AWS_REGION
AWS_S3_BUCKET
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
```

Ejemplo en PowerShell:

```powershell
$env:AWS_REGION="us-east-1"
$env:AWS_S3_BUCKET="lpv-resumenes-inscripcion"

$env:AWS_ACCESS_KEY_ID="********"
$env:AWS_SECRET_ACCESS_KEY="********"
$env:AWS_SESSION_TOKEN="********"
```

Las credenciales de AWS no se almacenan en el código fuente. En este proyecto se utilizaron credenciales temporales entregadas por el entorno académico AWS Learner Lab.

La configuración del cliente S3 se encuentra en:

```txt
src/main/java/com/duoc/LearningPlatformValidation/config/S3Config.java
```

El servicio encargado de operar con S3 se encuentra en:

```txt
src/main/java/com/duoc/LearningPlatformValidation/service/S3StorageService.java
```

---

## Ejecución del proyecto

Compilar el servicio de inscripciones:

```bash
./mvnw clean compile
```

En Windows PowerShell:

```powershell
.\mvnw clean compile
```

Ejecutar el servicio de inscripciones:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

El servicio de inscripciones queda disponible en:

```txt
http://localhost:8081
```

El acceso unificado a los servicios se realiza mediante el API Gateway:

```txt
http://localhost:8080
```

Los servicios `curso-service`, `bff-service` y `api-gateway` se compilan desde sus respectivos directorios. El frontend se compila mediante `npm run build`.

---

## Endpoints principales

### Cursos

```txt
GET    /api/cursos
GET    /api/cursos/{id}
POST   /api/cursos
PUT    /api/cursos/{id}
DELETE /api/cursos/{id}
```

### Usuarios

```txt
GET    /api/usuarios
GET    /api/usuarios/{id}
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

### Inscripciones

```txt
GET    /api/inscripciones
GET    /api/inscripciones/{id}
GET    /api/inscripciones/estudiante/{estudianteId}
POST   /api/inscripciones
GET    /api/inscripciones/{id}/boleta
DELETE /api/inscripciones/{id}
```

### BFF

```txt
POST   /api/bff/inscripciones
POST   /api/bff/mq/resumenes/consumir
GET    /api/bff/mq/resumenes
```

El BFF coordina la creación de la inscripción y la publicación de su resumen en RabbitMQ.

### Mensajería RabbitMQ

```txt
POST   /api/mq/inscripciones/{inscripcionId}/enviar-resumen
POST   /api/mq/resumenes/consumir
GET    /api/mq/resumenes
```

El productor publica el resumen en la cola y el consumidor lo guarda en Oracle Cloud.

### Resumen de inscripción y AWS S3

```txt
GET    /api/inscripciones/{id}/resumen/archivo
POST   /api/inscripciones/{id}/resumen/s3
GET    /api/inscripciones/{id}/resumen/s3/download
PUT    /api/inscripciones/{id}/resumen/s3
DELETE /api/inscripciones/{id}/resumen/s3
```

Detalle de los endpoints:

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/inscripciones/{id}/resumen/archivo` | Genera y descarga el resumen como archivo físico `.txt`. |
| POST | `/api/inscripciones/{id}/resumen/s3` | Genera el resumen y lo sube al bucket S3. |
| GET | `/api/inscripciones/{id}/resumen/s3/download` | Descarga desde S3 el archivo del resumen. |
| PUT | `/api/inscripciones/{id}/resumen/s3` | Reemplaza/modifica el archivo del resumen en S3. |
| DELETE | `/api/inscripciones/{id}/resumen/s3` | Elimina el archivo del resumen desde S3. |

Cada archivo se almacena dentro del bucket usando una carpeta cuyo nombre corresponde al número del resumen:

```txt
BOL-00001/resumen-inscripcion-BOL-00001.txt
```

### Evaluaciones

```txt
GET    /api/evaluaciones
GET    /api/evaluaciones/{id}
GET    /api/evaluaciones/curso/{cursoId}
POST   /api/evaluaciones
PUT    /api/evaluaciones/{id}
DELETE /api/evaluaciones/{id}
```

### Pagos

```txt
GET    /api/pagos
GET    /api/pagos/{id}
GET    /api/pagos/inscripcion/{inscripcionId}
GET    /api/pagos/estado/{estado}
POST   /api/pagos
PUT    /api/pagos/{id}
PUT    /api/pagos/{id}/aprobar
PUT    /api/pagos/{id}/rechazar
DELETE /api/pagos/{id}
```

### Notificaciones

```txt
GET    /api/notificaciones
GET    /api/notificaciones/{id}
GET    /api/notificaciones/usuario/{usuarioId}
GET    /api/notificaciones/leida/{leida}
GET    /api/notificaciones/tipo/{tipo}
POST   /api/notificaciones
PUT    /api/notificaciones/{id}
PUT    /api/notificaciones/{id}/leer
DELETE /api/notificaciones/{id}
```

---

## Ejemplo de flujo probado

Las rutas protegidas requieren un token válido emitido por Microsoft Entra ID mediante la cabecera `Authorization: Bearer <token>`. Los valores sensibles no se almacenan en el repositorio.

```powershell
$headers = @{
    Authorization = "Bearer <token>"
}
```

### 1. Crear usuario

```powershell
$body = @{
    nombre = "Juan Perez"
    correo = "juan.perez@test.cl"
    contrasena = "123456"
    rol = "ESTUDIANTE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios" -Method Post -Headers $headers -ContentType "application/json" -Body $body
```

### 2. Crear curso

```powershell
$body = @{
    nombre = "Spring Boot desde cero"
    instructor = "Carlos Soto"
    duracion = "40 horas"
    costo = 50000
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/cursos" -Method Post -Headers $headers -ContentType "application/json" -Body $body
```

### 3. Crear inscripción

```powershell
$body = @{
    estudianteId = 1
    cursoIds = @(1)
    metodoPago = "TARJETA"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/bff/inscripciones" -Method Post -Headers $headers -ContentType "application/json" -Body $body
```

### 4. Descargar resumen como archivo físico

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/inscripciones/1/resumen/archivo" -Headers $headers -OutFile "resumen-inscripcion-1.txt"
```

### 5. Subir resumen a S3

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/inscripciones/1/resumen/s3" -Method Post -Headers $headers
```

Respuesta esperada:

```txt
archivo                           numeroResumen mensaje                                           rutaS3
-------                           ------------- -------                                           ------
resumen-inscripcion-BOL-00001.txt BOL-00001     Resumen de inscripción subido correctamente a S3 BOL-00001/resumen-inscripcion-BOL-00001.txt
```

### 6. Descargar resumen desde S3

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/inscripciones/1/resumen/s3/download" -Headers $headers -OutFile "resumen-descargado-s3.txt"
```

### 7. Modificar resumen en S3

```powershell
$nuevoContenido = @"
RESUMEN DE INSCRIPCION MODIFICADO
=================================

Numero de resumen: BOL-00001
ID de inscripcion: 1
ID estudiante: 1
Fecha de emision: 2026-05-31

Cursos inscritos:
- Spring Boot desde cero | ID curso: 1 | Costo: `$50000

Total pagado: `$50000
Metodo de pago: TARJETA
Estado de pago: APROBADO_SIMULADO

Observacion: Archivo modificado correctamente desde el endpoint PUT.
"@

Invoke-RestMethod -Uri "http://localhost:8080/api/inscripciones/1/resumen/s3" -Method Put -Headers $headers -ContentType "text/plain; charset=utf-8" -Body $nuevoContenido
```

### 8. Eliminar resumen desde S3

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/inscripciones/1/resumen/s3" -Method Delete -Headers $headers
```

---

## Pruebas con Postman / PowerShell

Se realizaron pruebas sobre el CRUD de cursos:

```txt
GET    /api/cursos
POST   /api/cursos
GET    /api/cursos/{id}
PUT    /api/cursos/{id}
DELETE /api/cursos/{id}
```

También se probaron flujos adicionales:

```txt
POST /api/pagos
PUT  /api/pagos/{id}/aprobar

POST /api/notificaciones
PUT  /api/notificaciones/{id}/leer
```

Para la integración con AWS S3 se validó el siguiente flujo:

```txt
GET    /api/inscripciones/{id}/resumen/archivo
POST   /api/inscripciones/{id}/resumen/s3
GET    /api/inscripciones/{id}/resumen/s3/download
PUT    /api/inscripciones/{id}/resumen/s3
DELETE /api/inscripciones/{id}/resumen/s3
```

Estas pruebas permitieron validar la persistencia en base de datos, la generación de archivos físicos, la subida al bucket S3, la descarga desde S3, la modificación del archivo y la eliminación del objeto almacenado.

---

## CI/CD

El proyecto cuenta con un pipeline de GitHub Actions que automatiza:

1. Descarga del código fuente.
2. Configuración de Java 21.
3. Compilación de los servicios Spring Boot usando Maven Wrapper.
4. Construcción de las imágenes Docker del API Gateway, BFF, cursos, inscripciones y frontend.
5. Inicio de sesión en Docker Hub.
6. Publicación de las imágenes en Docker Hub.
7. Despliegue en una instancia EC2 mediante SSH.
8. Despliegue de los servicios mediante Docker Compose con variables de entorno y secretos.

El pipeline utiliza:

```txt
actions/checkout@v4
actions/setup-java@v4
docker/login-action
```

El uso de `docker/login-action` permite reemplazar el login manual a Docker Hub por una acción oficial, dejando el pipeline más ordenado y alineado con buenas prácticas.

El pipeline empaqueta los servicios utilizando:

```txt
-DskipTests
```

---

## Docker

El proyecto incluye un `Dockerfile` para cada servicio desplegable: inscripciones, cursos, BFF, API Gateway y frontend.

El despliegue en EC2 utiliza `docker-compose.prod.yml` para ejecutar los servicios, RabbitMQ y la red interna, usando variables de entorno para Oracle, AWS S3, Microsoft Entra ID y la comunicación entre contenedores.

Además, se utiliza:

```txt
--restart unless-stopped
```

Esto permite que los servicios se reinicien automáticamente si un contenedor se detiene o si la instancia EC2 se reinicia.

---

## Actuator

El proyecto incluye Spring Boot Actuator para revisar el estado del servicio.

Endpoint principal:

```txt
GET /actuator/health
```

---

## Logging con Spring AOP

El proyecto incorpora Spring AOP mediante la clase `LoggingAspect`.

Esta clase registra automáticamente la ejecución de métodos en los paquetes `controller` y `service`, mostrando información como:

- método ejecutado,
- inicio y fin de ejecución,
- tiempo de ejecución,
- errores producidos.

Esto permite separar el logging de la lógica principal de negocio.

---

## Manejo global de errores

El manejo de errores se centraliza mediante:

```txt
GlobalExceptionHandler.java
RecursoNoEncontradoException.java
```

Cuando un recurso no existe, el sistema responde con un `404 Not Found` y un JSON controlado.

Ejemplo:

```json
{
  "timestamp": "2026-05-04T00:08:12",
  "status": 404,
  "error": "Recurso no encontrado",
  "message": "Curso no encontrado con ID: 3",
  "path": "/api/cursos/3"
}
```

También se manejan errores de solicitud inválida y errores generales del servidor.

---

## Relación con los requerimientos

| Requerimiento | Estado en el proyecto |
|---|---|
| Usuarios y autenticación | Implementado con Microsoft Entra ID, OAuth2, JWT y Spring Security. |
| Gestión de cursos | Implementado mediante CRUD de cursos. |
| Inscripción en cursos | Implementado mediante módulo de inscripciones. |
| Generación de resumen de inscripción | Implementado mediante endpoint de boleta/resumen. |
| Descarga de resumen como archivo físico | Implementado mediante archivo `.txt`. |
| Subida de resumen a AWS S3 | Implementado mediante endpoint dedicado. |
| Carpeta por número de resumen en S3 | Implementado con ruta `BOL-00001/resumen-inscripcion-BOL-00001.txt`. |
| Descarga de resumen desde S3 | Implementado. |
| Modificación de resumen en S3 | Implementado. |
| Eliminación de resumen en S3 | Implementado. |
| Evaluaciones | Implementado mediante módulo de evaluaciones. |
| Pagos | Implementado mediante módulo de pagos con estados. |
| Notificaciones | Implementado mediante módulo de notificaciones. |
| Comunicación entre microservicios | Implementada mediante REST, API Gateway, BFF y mensajería RabbitMQ. |
| API Gateway y BFF | Implementados para centralizar el acceso y coordinar el flujo de inscripción. |
| Azure API Management | Implementado como acceso externo protegido con validación de tokens JWT. |
| Mensajería asíncrona | Implementada con productor, consumidor y cola RabbitMQ para resúmenes de inscripción. |
| Persistencia de mensajes | Implementada en Oracle Cloud mediante la tabla de resúmenes consumidos. |
| Frontend SPA | Implementado con Vite, JavaScript y autenticación mediante MSAL. |
| CI/CD con GitHub Actions, Docker Hub y EC2 | Implementado para construir y publicar cinco imágenes y desplegarlas mediante Docker Compose. |

---