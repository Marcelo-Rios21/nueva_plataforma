package com.duoc.LearningPlatformValidation.exception;

public class ServicioCursosNoDisponibleException extends RuntimeException {

    public ServicioCursosNoDisponibleException(
            String mensaje,
            Throwable causa) {

        super(mensaje, causa);
    }
}