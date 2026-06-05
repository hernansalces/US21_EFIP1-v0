package com.trainup.util;

import com.trainup.enums.CategoriaEngagement;
import com.trainup.model.IndiceEngagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class CalculadorIE {

    // Pesos de la fórmula IE = (PA × 0.50) + (PR × 0.30) + (PP × 0.20)
    private static final BigDecimal PESO_PA = new BigDecimal("0.50");
    private static final BigDecimal PESO_PR = new BigDecimal("0.30");
    private static final BigDecimal PESO_PP = new BigDecimal("0.20");

    private static final BigDecimal UMBRAL_ALTO   = new BigDecimal("75");
    private static final BigDecimal UMBRAL_MEDIO  = new BigDecimal("50");

    /**
     * Calcula el IE dado PA, PR y PP (valores entre 0 y 100).
     * Retorna un IndiceEngagement con valor, categoría y componentes completos.
     */
    public static IndiceEngagement calcular(int idEmpleado,
                                             BigDecimal pa,
                                             BigDecimal pr,
                                             BigDecimal pp,
                                             boolean esProvisional) {
        BigDecimal ie = pa.multiply(PESO_PA)
                         .add(pr.multiply(PESO_PR))
                         .add(pp.multiply(PESO_PP))
                         .setScale(2, RoundingMode.HALF_UP);

        CategoriaEngagement categoria;
        if (ie.compareTo(UMBRAL_ALTO) >= 0) {
            categoria = CategoriaEngagement.ALTO;
        } else if (ie.compareTo(UMBRAL_MEDIO) >= 0) {
            categoria = CategoriaEngagement.MEDIO;
        } else {
            categoria = CategoriaEngagement.CRITICO;
        }

        IndiceEngagement indice = new IndiceEngagement();
        indice.setIdEmpleado(idEmpleado);
        indice.setValorIe(ie);
        indice.setPa(pa.setScale(2, RoundingMode.HALF_UP));
        indice.setPr(pr.setScale(2, RoundingMode.HALF_UP));
        indice.setPp(pp.setScale(2, RoundingMode.HALF_UP));
        indice.setCategoria(categoria);
        indice.setEsProvisional(esProvisional);
        indice.setFechaCalculo(LocalDateTime.now());

        return indice;
    }

    public static boolean esCritico(IndiceEngagement ie) {
        return ie.getCategoria() == CategoriaEngagement.CRITICO;
    }
}
