package com.duoc.LearningPlatformValidation.controller;

import com.duoc.LearningPlatformValidation.model.ResumenCompraMq;
import com.duoc.LearningPlatformValidation.service.ResumenCompraMqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mq/resumenes")
public class ResumenCompraMqController {

    private final ResumenCompraMqService resumenCompraMqService;

    public ResumenCompraMqController(ResumenCompraMqService resumenCompraMqService) {
        this.resumenCompraMqService = resumenCompraMqService;
    }

    @PostMapping("/consumir")
    public ResponseEntity<Map<String, Object>> consumirResumen() {
        ResumenCompraMq resumenGuardado = resumenCompraMqService.consumirYGuardar();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensaje", "Resumen consumido desde RabbitMQ y guardado en Oracle Cloud");
        response.put("resumenCompraId", resumenGuardado.getId());
        response.put("inscripcionId", resumenGuardado.getInscripcionId());
        response.put("numeroResumen", resumenGuardado.getNumeroResumen());
        response.put("estadoPago", resumenGuardado.getEstadoPago());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumenCompraMq>> listarResumenesGuardados() {
        return ResponseEntity.ok(resumenCompraMqService.listar());
    }
}