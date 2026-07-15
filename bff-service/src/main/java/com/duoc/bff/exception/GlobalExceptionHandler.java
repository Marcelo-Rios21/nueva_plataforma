package com.duoc.bff.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioNoDisponible(
            ServicioNoDisponibleException ex,
            HttpServletRequest request) {

        return construirRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Servicio interno no disponible",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ServicioInternoException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioInterno(
            ServicioInternoException ex,
            HttpServletRequest request) {

        String mensaje = ex.getMessage()
                + " Código recibido: "
                + ex.getStatusCode();

        return construirRespuesta(
                HttpStatus.BAD_GATEWAY,
                "Error de servicio interno",
                mensaje,
                request
        );
    }

    @ExceptionHandler(RespuestaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarRespuestaInvalida(
            RespuestaInvalidaException ex,
            HttpServletRequest request) {

        return construirRespuesta(
                HttpStatus.BAD_GATEWAY,
                "Respuesta interna inválida",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del BFF",
                ex.getMessage(),
                request
        );
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", status.value());
        respuesta.put("error", error);
        respuesta.put("message", message);
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity.status(status).body(respuesta);
    }
}