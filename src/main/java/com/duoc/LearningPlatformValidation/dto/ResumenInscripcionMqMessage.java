package com.duoc.LearningPlatformValidation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ResumenInscripcionMqMessage {

    private Long inscripcionId;
    private Long estudianteId;
    private String numeroResumen;
    private LocalDateTime fechaInscripcion;
    private BigDecimal total;
    private String metodoPago;
    private String estadoPago;
    private List<CursoInscritoResponse> cursos;

    public ResumenInscripcionMqMessage() {
    }

    public ResumenInscripcionMqMessage(Long inscripcionId, Long estudianteId, String numeroResumen,
                                       LocalDateTime fechaInscripcion, BigDecimal total,
                                       String metodoPago, String estadoPago,
                                       List<CursoInscritoResponse> cursos) {
        this.inscripcionId = inscripcionId;
        this.estudianteId = estudianteId;
        this.numeroResumen = numeroResumen;
        this.fechaInscripcion = fechaInscripcion;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.cursos = cursos;
    }

    public Long getInscripcionId() {
        return inscripcionId;
    }

    public void setInscripcionId(Long inscripcionId) {
        this.inscripcionId = inscripcionId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getNumeroResumen() {
        return numeroResumen;
    }

    public void setNumeroResumen(String numeroResumen) {
        this.numeroResumen = numeroResumen;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public List<CursoInscritoResponse> getCursos() {
        return cursos;
    }

    public void setCursos(List<CursoInscritoResponse> cursos) {
        this.cursos = cursos;
    }
}