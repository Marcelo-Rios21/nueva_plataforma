package com.duoc.LearningPlatformValidation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESUMEN_COMPRA_MQ")
public class ResumenCompraMq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "INSCRIPCION_ID", nullable = false)
    private Long inscripcionId;

    @Column(name = "ESTUDIANTE_ID", nullable = false)
    private Long estudianteId;

    @Column(name = "NUMERO_RESUMEN", nullable = false, length = 50)
    private String numeroResumen;

    @Column(name = "FECHA_INSCRIPCION")
    private LocalDateTime fechaInscripcion;

    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "METODO_PAGO", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "ESTADO_PAGO", nullable = false, length = 50)
    private String estadoPago;

    @Lob
    @Column(name = "CONTENIDO_JSON")
    private String contenidoJson;

    @Column(name = "FECHA_GUARDADO", nullable = false)
    private LocalDateTime fechaGuardado;

    public ResumenCompraMq() {
    }

    public Long getId() {
        return id;
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

    public String getContenidoJson() {
        return contenidoJson;
    }

    public void setContenidoJson(String contenidoJson) {
        this.contenidoJson = contenidoJson;
    }

    public LocalDateTime getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(LocalDateTime fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }
}