package com.trainup.controller;

import com.trainup.dao.AsistenciaDAO;
import com.trainup.dao.EmpleadoDAO;
import com.trainup.dao.InstanciaDAO;
import com.trainup.enums.EstadoAsistencia;
import com.trainup.model.Empleado;
import com.trainup.model.RegistroAsistencia;

import java.sql.SQLException;
import java.util.List;

public class CtrlAsistencia {

    private final AsistenciaDAO  asistenciaDAO  = new AsistenciaDAO();
    private final EmpleadoDAO    empleadoDAO    = new EmpleadoDAO();
    private final InstanciaDAO   instanciaDAO   = new InstanciaDAO();
    private final CtrlEngagement ctrlEngagement = new CtrlEngagement();

    public List<RegistroAsistencia> obtenerPorInstancia(int idInstancia) throws SQLException {
        return asistenciaDAO.listarPorInstancia(idInstancia);
    }

    /**
     * Crea registros PENDIENTE para empleados asignados que aún no tengan registro.
     * Útil para instancias cargadas manualmente en la BD o por migración de datos.
     */
    public void asegurarRegistros(int idInstancia) throws SQLException {
        List<Empleado> empleados = instanciaDAO.listarEmpleadosAsignados(idInstancia);
        for (Empleado emp : empleados) {
            instanciaDAO.crearRegistroAsistenciaPendiente(idInstancia, emp.getId());
        }
    }

    /**
     * CU-07: Guarda todos los registros de asistencia de una instancia.
     * Luego recalcula el IE (CU-12) de cada empleado con estado definitivo.
     */
    public void guardarAsistencia(List<RegistroAsistencia> registros) throws SQLException {
        asistenciaDAO.guardarTodos(registros);

        // Recalcular IE para empleados con estado definitivo
        for (RegistroAsistencia ra : registros) {
            if (ra.getEstadoAsistencia() != EstadoAsistencia.PENDIENTE) {
                try {
                    Empleado emp = empleadoDAO.buscarPorId(ra.getIdEmpleado());
                    if (emp != null) {
                        ctrlEngagement.recalcular(emp);
                    }
                } catch (SQLException e) {
                    System.err.println("Error al recalcular IE para empleado " +
                                       ra.getIdEmpleado() + ": " + e.getMessage());
                }
            }
        }
    }
}
