package com.trainup.enums;

public enum RolUsuario {
    ADMIN, GERENTE, EMPLEADO;

    public static RolUsuario fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
