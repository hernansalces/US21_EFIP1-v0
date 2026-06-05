package com.trainup.enums;

public enum ModalidadCurso {
    PRESENCIAL, VIRTUAL_SINCRONICO, ELEARNING, MIXTA;

    public static ModalidadCurso fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
