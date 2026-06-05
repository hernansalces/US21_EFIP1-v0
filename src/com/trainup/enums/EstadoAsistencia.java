package com.trainup.enums;

public enum EstadoAsistencia {
    ASISTIO, NO_ASISTIO, PENDIENTE;

    public static EstadoAsistencia fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
