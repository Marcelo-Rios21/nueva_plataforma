package com.duoc.bff.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.bff.dto.InscripcionRequest;
import com.duoc.bff.service.InscripcionOrquestacionService;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/bff")
public class BffInscripcionController {

    private final InscripcionOrquestacionService orquestacionService;

    public BffInscripcionController(
            InscripcionOrquestacionService orquestacionService) {

        this.orquestacionService = orquestacionService;
    }

    @PostMapping("/inscripciones")
    public ResponseEntity<Map<String, Object>> registrarInscripcion(
            @RequestBody InscripcionRequest request) {

        Map<String, Object> respuesta =
                orquestacionService.registrarYPublicarResumen(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @PostMapping("/inscripciones/{id}/resumen/s3")
    public ResponseEntity<JsonNode> subirResumenS3(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orquestacionService.subirResumenS3(id)
        );
    }

    @GetMapping("/inscripciones/{id}/resumen/s3/download")
    public ResponseEntity<byte[]> descargarResumenS3(
            @PathVariable Long id) {

        return orquestacionService.descargarResumenS3(id);
    }

    @PostMapping("/mq/resumenes/consumir")
    public ResponseEntity<JsonNode> consumirResumenRabbitMq() {
        return ResponseEntity.ok(
                orquestacionService.consumirResumenRabbitMq()
        );
    }

    @GetMapping("/mq/resumenes")
    public ResponseEntity<JsonNode> listarResumenesRabbitMq() {
        return ResponseEntity.ok(
                orquestacionService.listarResumenesRabbitMq()
        );
    }
}