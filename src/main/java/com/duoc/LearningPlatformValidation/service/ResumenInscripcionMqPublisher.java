package com.duoc.LearningPlatformValidation.service;

import com.duoc.LearningPlatformValidation.dto.ResumenInscripcionMqMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResumenInscripcionMqPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.inscripcion.exchange}")
    private String exchangeName;

    @Value("${app.mq.inscripcion.routing-key}")
    private String routingKey;

    public ResumenInscripcionMqPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void enviarResumen(ResumenInscripcionMqMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, jsonMessage);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No fue posible convertir el resumen de inscripcion a JSON");
        }
    }
}