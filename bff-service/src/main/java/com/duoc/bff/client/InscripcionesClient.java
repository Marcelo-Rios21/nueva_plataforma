package com.duoc.bff.client;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.duoc.bff.dto.InscripcionRequest;
import com.duoc.bff.exception.RespuestaInvalidaException;
import com.duoc.bff.exception.ServicioInternoException;
import com.duoc.bff.exception.ServicioNoDisponibleException;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class InscripcionesClient {

    private final RestClient restClient;

    public InscripcionesClient(
            RestClient.Builder builder,
            @Value("${services.inscripciones.url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public JsonNode registrarInscripcion(
            InscripcionRequest request) {

        return ejecutarLlamada(
                () -> restClient
                        .post()
                        .uri("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(JsonNode.class),
                "registrar la inscripción"
        );
    }

    public JsonNode enviarResumenRabbitMq(
            Long inscripcionId) {

        return ejecutarLlamada(
                () -> restClient
                        .post()
                        .uri(
                                "/api/mq/inscripciones/{id}/enviar-resumen",
                                inscripcionId
                        )
                        .retrieve()
                        .body(JsonNode.class),
                "enviar el resumen a RabbitMQ"
        );
    }

    public JsonNode consumirResumenRabbitMq() {
        return ejecutarLlamada(
                () -> restClient
                        .post()
                        .uri("/api/mq/resumenes/consumir")
                        .retrieve()
                        .body(JsonNode.class),
                "consumir el resumen desde RabbitMQ"
        );
    }

    public JsonNode listarResumenesRabbitMq() {
        return ejecutarLlamada(
                () -> restClient
                        .get()
                        .uri("/api/mq/resumenes")
                        .retrieve()
                        .body(JsonNode.class),
                "consultar los resúmenes guardados"
        );
    }

    private JsonNode ejecutarLlamada(
            Supplier<JsonNode> llamada,
            String operacion) {

        try {
            JsonNode respuesta = llamada.get();

            if (respuesta == null || respuesta.isNull()) {
                throw new RespuestaInvalidaException(
                        "El servicio de inscripciones no entregó "
                                + "una respuesta al intentar "
                                + operacion + "."
                );
            }

            return respuesta;
        }
        catch (RestClientResponseException ex) {
            throw new ServicioInternoException(
                    "El servicio de inscripciones rechazó la operación: "
                            + operacion + ".",
                    ex.getStatusCode().value(),
                    ex
            );
        }
        catch (RestClientException ex) {
            throw new ServicioNoDisponibleException(
                    "No fue posible comunicarse con el servicio "
                            + "de inscripciones para "
                            + operacion + ".",
                    ex
            );
        }
    }
}