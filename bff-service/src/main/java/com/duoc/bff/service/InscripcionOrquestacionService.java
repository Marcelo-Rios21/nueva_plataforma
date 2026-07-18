package com.duoc.bff.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.duoc.bff.client.InscripcionesClient;
import com.duoc.bff.dto.InscripcionRequest;
import com.duoc.bff.exception.RespuestaInvalidaException;
import tools.jackson.databind.JsonNode;

@Service
public class InscripcionOrquestacionService {

    private final InscripcionesClient inscripcionesClient;

    public InscripcionOrquestacionService(
            InscripcionesClient inscripcionesClient) {

        this.inscripcionesClient = inscripcionesClient;
    }

    public Map<String, Object> registrarYPublicarResumen(
            InscripcionRequest request) {

        validarRequest(request);

        JsonNode inscripcion =
                inscripcionesClient.registrarInscripcion(request);

        JsonNode idNode = inscripcion.get("inscripcionId");

        if (idNode == null
                || !idNode.canConvertToLong()
                || idNode.asLong() <= 0) {

            throw new RespuestaInvalidaException(
                    "La respuesta de inscripción no contiene "
                            + "un inscripcionId válido."
            );
        }

        long inscripcionId = idNode.asLong();

        JsonNode rabbitMq =
                inscripcionesClient.enviarResumenRabbitMq(
                        inscripcionId
                );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(
                "mensaje",
                "Inscripción creada y resumen enviado a RabbitMQ"
        );
        respuesta.put("inscripcion", inscripcion);
        respuesta.put("rabbitmq", rabbitMq);

        return respuesta;
    }

    public JsonNode consumirResumenRabbitMq() {
        return inscripcionesClient.consumirResumenRabbitMq();
    }

    public JsonNode listarResumenesRabbitMq() {
        return inscripcionesClient.listarResumenesRabbitMq();
    }

    public JsonNode subirResumenS3(Long inscripcionId) {
        validarInscripcionId(inscripcionId);

        return inscripcionesClient.subirResumenS3(
                inscripcionId
        );
    }

    public ResponseEntity<byte[]> descargarResumenS3(
            Long inscripcionId) {

        validarInscripcionId(inscripcionId);

        return inscripcionesClient.descargarResumenS3(
                inscripcionId
        );
    }

    private void validarInscripcionId(Long inscripcionId) {
        if (inscripcionId == null || inscripcionId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de inscripción debe ser mayor que cero."
            );
        }
    }

    private void validarRequest(InscripcionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la inscripción son obligatorios."
            );
        }

        if (request.getEstudianteId() == null) {
            throw new IllegalArgumentException(
                    "El estudiante es obligatorio."
            );
        }

        if (request.getCursoIds() == null
                || request.getCursoIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un curso."
            );
        }

        if (request.getMetodoPago() == null
                || request.getMetodoPago().isBlank()) {

            throw new IllegalArgumentException(
                    "El método de pago es obligatorio."
            );
        }
    }
}