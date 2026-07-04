package com.duoc.LearningPlatformValidation.service;

import com.duoc.LearningPlatformValidation.model.ResumenCompraMq;
import com.duoc.LearningPlatformValidation.repository.ResumenCompraMqRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumenCompraMqService {

    private final RabbitTemplate rabbitTemplate;
    private final ResumenCompraMqRepository resumenCompraMqRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.inscripcion.queue}")
    private String queueName;

    public ResumenCompraMqService(RabbitTemplate rabbitTemplate,
                                  ResumenCompraMqRepository resumenCompraMqRepository,
                                  ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.resumenCompraMqRepository = resumenCompraMqRepository;
        this.objectMapper = objectMapper;
    }

    public ResumenCompraMq consumirYGuardar() {
        Message rawMessage = rabbitTemplate.receive(queueName);

        if (rawMessage == null) {
            throw new IllegalArgumentException("No hay mensajes pendientes en la cola RabbitMQ");
        }

        String jsonMessage = new String(rawMessage.getBody(), StandardCharsets.UTF_8);
        JsonNode json = convertirJson(jsonMessage);

        ResumenCompraMq resumen = new ResumenCompraMq();
        resumen.setInscripcionId(json.path("inscripcionId").asLong());
        resumen.setEstudianteId(json.path("estudianteId").asLong());
        resumen.setNumeroResumen(json.path("numeroResumen").asText());
        resumen.setFechaInscripcion(LocalDateTime.parse(json.path("fechaInscripcion").asText()));
        resumen.setTotal(new BigDecimal(json.path("total").asText()));
        resumen.setMetodoPago(json.path("metodoPago").asText());
        resumen.setEstadoPago(json.path("estadoPago").asText());
        resumen.setContenidoJson(jsonMessage);
        resumen.setFechaGuardado(LocalDateTime.now());

        return resumenCompraMqRepository.save(resumen);
    }

    public List<ResumenCompraMq> listar() {
        return resumenCompraMqRepository.findAll();
    }

    private JsonNode convertirJson(String jsonMessage) {
        try {
            return objectMapper.readTree(jsonMessage);
        } catch (Exception ex) {
            throw new IllegalArgumentException("El mensaje recibido desde RabbitMQ no tiene un JSON valido: " + jsonMessage);
        }
    }
}