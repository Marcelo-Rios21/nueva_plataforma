# Integración RabbitMQ para resúmenes de inscripción

## Estado actual

La aplicación incorpora una integración con RabbitMQ para desacoplar el proceso de generación y persistencia de resúmenes de inscripción.

Actualmente, el sistema permite tomar una inscripción existente, generar su resumen a partir de la boleta de inscripción, enviar ese resumen a una cola RabbitMQ y posteriormente consumirlo desde un endpoint para guardarlo en una nueva tabla de Oracle Cloud.

El flujo implementado complementa la funcionalidad principal de inscripción, pago simulado y generación de resumen, agregando una comunicación asíncrona basada en mensajes.

## Flujo general

```text
Inscripción existente
        |
        v
Generación de boleta / resumen
        |
        v
Publicación del resumen en RabbitMQ
        |
        v
Cola: inscripciones.resumen.queue
        |
        v
Consumo del mensaje desde endpoint
        |
        v
Persistencia en Oracle Cloud
        |
        v
Tabla: RESUMEN_COMPRA_MQ
```

## Componentes incorporados

### RabbitMQ

RabbitMQ se utiliza como servicio de mensajería para almacenar temporalmente los resúmenes de inscripción antes de ser consumidos y guardados en la base de datos.

El servicio se encuentra definido en `docker-compose.yml` con los siguientes puertos:

```text
5672  -> comunicación entre Spring Boot y RabbitMQ
15672 -> panel web de administración de RabbitMQ
```

Panel de administración:

```text
http://localhost:15672
```

Credenciales configuradas para ambiente local:

```text
usuario: admin
password: admin
```

### Configuración Spring Boot

La conexión con RabbitMQ se define en `application.properties` mediante variables de entorno con valores por defecto para desarrollo local:

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:admin}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:admin}
```

La mensajería de resúmenes de inscripción utiliza:

```properties
app.mq.inscripcion.exchange=inscripciones.exchange
app.mq.inscripcion.queue=inscripciones.resumen.queue
app.mq.inscripcion.routing-key=inscripcion.resumen.creado
```

### Exchange, cola y binding

La clase `RabbitMQConfig` declara los componentes necesarios para el intercambio de mensajes:

```text
Exchange:    inscripciones.exchange
Cola:        inscripciones.resumen.queue
Routing key: inscripcion.resumen.creado
```

Esto permite que los mensajes enviados por la aplicación sean dirigidos correctamente hacia la cola de resúmenes de inscripción.

## Envío de resúmenes a RabbitMQ

El sistema expone un endpoint para enviar el resumen de una inscripción existente hacia RabbitMQ.

```http
POST /api/mq/inscripciones/{inscripcionId}/enviar-resumen
```

Este endpoint realiza las siguientes acciones:

```text
1. Recibe el ID de una inscripción existente.
2. Genera la boleta o resumen usando la lógica actual de inscripción.
3. Construye un mensaje con los datos principales del resumen.
4. Convierte el mensaje a JSON.
5. Publica el mensaje en RabbitMQ.
6. Devuelve una confirmación del envío.
```

Respuesta esperada:

```json
{
  "mensaje": "Resumen enviado correctamente a RabbitMQ",
  "inscripcionId": 1,
  "numeroResumen": "BOL-00001",
  "queue": "inscripciones.resumen.queue"
}
```

## Consumo de resúmenes desde RabbitMQ

El sistema expone un endpoint para consumir un mensaje pendiente desde RabbitMQ y guardarlo en Oracle Cloud.

```http
POST /api/mq/resumenes/consumir
```

Este endpoint realiza las siguientes acciones:

```text
1. Lee un mensaje pendiente desde la cola de RabbitMQ.
2. Convierte el JSON recibido a un objeto de resumen de inscripción.
3. Crea un registro de resumen de compra.
4. Guarda el registro en Oracle Cloud.
5. Devuelve una confirmación con el ID del resumen guardado.
```

Respuesta esperada:

```json
{
  "mensaje": "Resumen consumido desde RabbitMQ y guardado en Oracle Cloud",
  "resumenCompraId": 1,
  "inscripcionId": 1,
  "numeroResumen": "BOL-00001",
  "estadoPago": "APROBADO_SIMULADO"
}
```

## Consulta de resúmenes guardados

Para revisar los resúmenes ya persistidos en Oracle Cloud, la aplicación expone el siguiente endpoint:

```http
GET /api/mq/resumenes
```

Este endpoint permite validar que los mensajes consumidos desde RabbitMQ fueron guardados correctamente en la base de datos.

## Tabla Oracle Cloud

Los resúmenes consumidos desde RabbitMQ se almacenan en la tabla:

```text
RESUMEN_COMPRA_MQ
```

Campos principales:

```text
ID
INSCRIPCION_ID
ESTUDIANTE_ID
NUMERO_RESUMEN
FECHA_INSCRIPCION
TOTAL
METODO_PAGO
ESTADO_PAGO
CONTENIDO_JSON
FECHA_GUARDADO
```

El campo `CONTENIDO_JSON` conserva el mensaje completo recibido desde RabbitMQ, incluyendo el detalle de los cursos inscritos.

## Archivos principales modificados o agregados

```text
docker-compose.yml
pom.xml
src/main/resources/application.properties
src/main/java/com/duoc/LearningPlatformValidation/config/RabbitMQConfig.java
src/main/java/com/duoc/LearningPlatformValidation/dto/ResumenInscripcionMqMessage.java
src/main/java/com/duoc/LearningPlatformValidation/model/ResumenCompraMq.java
src/main/java/com/duoc/LearningPlatformValidation/repository/ResumenCompraMqRepository.java
src/main/java/com/duoc/LearningPlatformValidation/service/ResumenInscripcionMqPublisher.java
src/main/java/com/duoc/LearningPlatformValidation/service/ResumenInscripcionMqService.java
src/main/java/com/duoc/LearningPlatformValidation/service/ResumenCompraMqService.java
src/main/java/com/duoc/LearningPlatformValidation/controller/ResumenInscripcionMqController.java
src/main/java/com/duoc/LearningPlatformValidation/controller/ResumenCompraMqController.java
```

## Secuencia de prueba recomendada

```text
1. Levantar RabbitMQ.
2. Crear o identificar una inscripción existente.
3. Enviar el resumen de la inscripción a RabbitMQ.
4. Verificar el mensaje pendiente en la cola.
5. Consumir el mensaje desde el endpoint consumidor.
6. Consultar los resúmenes guardados.
7. Confirmar el registro en Oracle Cloud.
```

Comando para levantar RabbitMQ:

```bash
docker compose up -d rabbitmq
```

Endpoint de envío:

```http
POST /api/mq/inscripciones/{inscripcionId}/enviar-resumen
```

Endpoint de consumo:

```http
POST /api/mq/resumenes/consumir
```

Endpoint de consulta:

```http
GET /api/mq/resumenes
```

## Resultado esperado

Con esta integración, la aplicación queda preparada para manejar el resumen de inscripción mediante una cola MQ, evitando que el proceso de persistencia del resumen dependa directamente del flujo inicial de inscripción.

El sistema mantiene la inscripción como operación principal y agrega RabbitMQ como mecanismo intermedio para transportar el resumen hacia una nueva persistencia en Oracle Cloud.