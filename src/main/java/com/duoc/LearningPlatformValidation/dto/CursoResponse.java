package com.duoc.LearningPlatformValidation.dto;

import java.math.BigDecimal;

public class CursoResponse {

    private Long id;
    private String nombre;
    private String instructor;
    private String duracion;
    private BigDecimal costo;

    public CursoResponse() {
    }

    public CursoResponse(
            Long id,
            String nombre,
            String instructor,
            String duracion,
            BigDecimal costo) {

        this.id = id;
        this.nombre = nombre;
        this.instructor = instructor;
        this.duracion = duracion;
        this.costo = costo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
}