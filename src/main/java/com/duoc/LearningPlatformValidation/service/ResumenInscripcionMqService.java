package com.duoc.LearningPlatformValidation.service;

import com.duoc.LearningPlatformValidation.dto.BoletaResponse;
import com.duoc.LearningPlatformValidation.dto.ResumenInscripcionMqMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResumenInscripcionMqService {

    private final InscripcionService inscripcionService;
    private final ResumenInscripcionMqPublisher publisher;

    public ResumenInscripcionMqService(InscripcionService inscripcionService,
                                       ResumenInscripcionMqPublisher publisher) {
        this.inscripcionService = inscripcionService;
        this.publisher = publisher;
    }

    public ResumenInscripcionMqMessage enviarResumenACola(Long inscripcionId) {
        BoletaResponse boleta = inscripcionService.generarBoleta(inscripcionId);

        LocalDateTime fechaInscripcion = boleta.getFechaEmision() != null
                ? boleta.getFechaEmision().atStartOfDay()
                : null;

        ResumenInscripcionMqMessage message = new ResumenInscripcionMqMessage(
                boleta.getInscripcionId(),
                boleta.getEstudianteId(),
                boleta.getNumeroBoleta(),
                fechaInscripcion,
                boleta.getTotal(),
                boleta.getMetodoPago(),
                boleta.getEstadoPago(),
                boleta.getCursos()
        );

        publisher.enviarResumen(message);
        return message;
    }
}