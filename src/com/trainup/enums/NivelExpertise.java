package com.trainup.enums;

public enum NivelExpertise {
    BASICO, INTERMEDIO, AVANZADO;

    public static NivelExpertise fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
