package com.trainup.enums;

public enum TipoAlerta {
    CRITICO, ADVERTENCIA, INFORMATIVO, URGENTE, RECORDATORIO;

    public static TipoAlerta fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
