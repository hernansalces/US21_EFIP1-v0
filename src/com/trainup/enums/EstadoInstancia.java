package com.trainup.enums;

public enum EstadoInstancia {
    PROGRAMADA, REALIZADA, CANCELADA;

    public static EstadoInstancia fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
