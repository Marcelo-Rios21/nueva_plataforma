package com.duoc.bff.exception;

public class ServicioInternoException extends RuntimeException {

    private final int statusCode;

    public ServicioInternoException(
            String mensaje,
            int statusCode,
            Throwable causa) {

        super(mensaje, causa);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}