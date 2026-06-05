package com.trainup.enums;

public enum OrigenAlerta {
    CALCULO_IE, VENCIMIENTO_CAPACITACION, PROXIMO_VENCIMIENTO;

    public static OrigenAlerta fromString(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
