package com.trainup.enums;

public enum ResultadoCurso {
    APROBADO, DESAPROBADO, PENDIENTE_RESULTADO, NO_APLICA;

    public static ResultadoCurso fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
