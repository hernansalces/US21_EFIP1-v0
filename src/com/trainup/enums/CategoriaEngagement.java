package com.trainup.enums;

public enum CategoriaEngagement {
    ALTO, MEDIO, CRITICO;

    public static CategoriaEngagement fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
