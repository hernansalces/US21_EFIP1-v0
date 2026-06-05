package com.trainup.controller;

import com.trainup.dao.AlertaDAO;
import com.trainup.dao.EngagementDAO;
import com.trainup.enums.OrigenAlerta;
import com.trainup.enums.TipoAlerta;
import com.trainup.model.Alerta;
import com.trainup.model.Empleado;
import com.trainup.model.IndiceEngagement;
import com.trainup.util.CalculadorIE;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * CU-12: Calcular índice de engagement.
 * Invocado automáticamente por CtrlAsistencia al guardar asistencia.
 */
public class CtrlEngagement {

    private final EngagementDAO engDAO  = new EngagementDAO();
    private final AlertaDAO alertaDAO   = new AlertaDAO();

    /**
     * Recalcula y persiste el IE de un empleado.
     * Si no tiene perfil asignado, no calcula.
     * Si el resultado es CRITICO, genera alerta.
     */
    public IndiceEngagement recalcular(Empleado empleado) throws SQLException {
        if (empleado.getIdPerfil() == null) {
            // Sin perfil: no se puede calcular IE
            return null;
        }

        BigDecimal pa = engDAO.calcularPA(empleado.getId());
        BigDecimal pr = engDAO.calcularPR(empleado.getId());
        BigDecimal pp = engDAO.calcularPP(empleado.getId(), empleado.getIdPerfil());
        boolean provisional = engDAO.tieneResultadosPendientes(empleado.getId());

        IndiceEngagement ie = CalculadorIE.calcular(empleado.getId(), pa, pr, pp, provisional);

        // Persistir índice actual e historial
        engDAO.guardarIndice(ie);
        engDAO.guardarHistorial(ie);

        // Generar alerta si es CRITICO
        if (CalculadorIE.esCritico(ie)) {
            generarAlertaCritica(empleado, ie);
        }

        return ie;
    }

    private void generarAlertaCritica(Empleado empleado, IndiceEngagement ie) {
        try {
            Alerta alerta = new Alerta();
            alerta.setIdEmpleado(empleado.getId());
            alerta.setTipo(TipoAlerta.CRITICO);
            alerta.setOrigen(OrigenAlerta.CALCULO_IE);
            alerta.setMensaje(
                empleado.getNombreCompleto() + " tiene un IE CRITICO de " +
                ie.getValorIe() + " (PA=" + ie.getPa() + "%, PR=" + ie.getPr() +
                "%, PP=" + ie.getPp() + "%)."
            );
            alertaDAO.insertar(alerta);
        } catch (SQLException e) {
            System.err.println("No se pudo generar alerta para " + empleado.getNombreCompleto() + ": " + e.getMessage());
        }
    }

    public IndiceEngagement obtenerIndice(int idEmpleado) throws SQLException {
        return engDAO.buscarPorEmpleado(idEmpleado);
    }
}
