package com.duoc.LearningPlatformValidation.controller;

import com.duoc.LearningPlatformValidation.dto.ResumenInscripcionMqMessage;
import com.duoc.LearningPlatformValidation.service.ResumenInscripcionMqService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mq/inscripciones")
public class ResumenInscripcionMqController {

    private final ResumenInscripcionMqService resumenInscripcionMqService;

    @Value("${app.mq.inscripcion.queue}")
    private String queueName;

    public ResumenInscripcionMqController(ResumenInscripcionMqService resumenInscripcionMqService) {
        this.resumenInscripcionMqService = resumenInscripcionMqService;
    }

    @PostMapping("/{inscripcionId}/enviar-resumen")
    public ResponseEntity<Map<String, Object>> enviarResumen(@PathVariable Long inscripcionId) {
        ResumenInscripcionMqMessage message = resumenInscripcionMqService.enviarResumenACola(inscripcionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensaje", "Resumen enviado correctamente a RabbitMQ");
        response.put("inscripcionId", message.getInscripcionId());
        response.put("numeroResumen", message.getNumeroResumen());
        response.put("queue", queueName);

        return ResponseEntity.ok(response);
    }
}