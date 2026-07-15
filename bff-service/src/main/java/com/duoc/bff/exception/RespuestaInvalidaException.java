package com.duoc.bff.exception;

public class RespuestaInvalidaException extends RuntimeException {

    public RespuestaInvalidaException(String mensaje) {
        super(mensaje);
    }
}