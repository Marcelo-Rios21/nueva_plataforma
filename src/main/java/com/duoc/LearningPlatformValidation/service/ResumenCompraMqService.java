package com.duoc.LearningPlatformValidation.service;

import com.duoc.LearningPlatformValidation.dto.ResumenInscripcionMqMessage;
import com.duoc.LearningPlatformValidation.model.ResumenCompraMq;
import com.duoc.LearningPlatformValidation.repository.ResumenCompraMqRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        Object rawMessage = rabbitTemplate.receiveAndConvert(queueName);

        if (rawMessage == null) {
            throw new IllegalArgumentException("No hay mensajes pendientes en la cola RabbitMQ");
        }

        ResumenInscripcionMqMessage message = convertirMensaje(rawMessage);

        ResumenCompraMq resumen = new ResumenCompraMq();
        resumen.setInscripcionId(message.getInscripcionId());
        resumen.setEstudianteId(message.getEstudianteId());
        resumen.setNumeroResumen(message.getNumeroResumen());
        resumen.setFechaInscripcion(message.getFechaInscripcion());
        resumen.setTotal(message.getTotal());
        resumen.setMetodoPago(message.getMetodoPago());
        resumen.setEstadoPago(message.getEstadoPago());
        resumen.setContenidoJson(convertirMensajeAJson(message));
        resumen.setFechaGuardado(LocalDateTime.now());

        return resumenCompraMqRepository.save(resumen);
    }

    public List<ResumenCompraMq> listar() {
        return resumenCompraMqRepository.findAll();
    }

    private ResumenInscripcionMqMessage convertirMensaje(Object rawMessage) {
        if (rawMessage instanceof ResumenInscripcionMqMessage message) {
            return message;
        }

        if (rawMessage instanceof String jsonMessage) {
            try {
                return objectMapper.readValue(jsonMessage, ResumenInscripcionMqMessage.class);
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException("El mensaje recibido desde RabbitMQ no tiene el formato esperado");
            }
        }

        throw new IllegalArgumentException("El mensaje recibido desde RabbitMQ no tiene el formato esperado");
    }

    private String convertirMensajeAJson(ResumenInscripcionMqMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No fue posible convertir el resumen consumido a JSON");
        }
    }
}