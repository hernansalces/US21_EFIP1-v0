package com.trainup.controller;

import com.trainup.dao.CursoDAO;
import com.trainup.dao.EmpleadoDAO;
import com.trainup.dao.InstanciaDAO;
import com.trainup.model.Curso;
import com.trainup.model.Empleado;
import com.trainup.model.InstanciaCapacitacion;
import com.trainup.util.ConectorBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CtrlInstancia {

    private final InstanciaDAO instanciaDAO = new InstanciaDAO();
    private final EmpleadoDAO  empleadoDAO  = new EmpleadoDAO();
    private final CursoDAO     cursoDAO     = new CursoDAO();

    public List<InstanciaCapacitacion> obtenerTodas() throws SQLException {
        return instanciaDAO.listarTodas();
    }

    public List<InstanciaCapacitacion> obtenerParaAsistencia() throws SQLException {
        return instanciaDAO.listarParaAsistencia();
    }

    public List<Empleado> obtenerEmpleadosActivos() throws SQLException {
        return empleadoDAO.listarActivos();
    }

    public List<Empleado> obtenerEmpleadosAsignados(int idInstancia) throws SQLException {
        return instanciaDAO.listarEmpleadosAsignados(idInstancia);
    }

    public List<Curso> obtenerCursosActivos() throws SQLException {
        return cursoDAO.listarActivos();
    }

    /**
     * CU-06: Programa una instancia y asigna empleados.
     * Crea registros de asistencia en estado PENDIENTE para cada empleado.
     * Todo en una sola transacción.
     */
    public int programar(InstanciaCapacitacion inst, List<Integer> idsEmpleados) throws SQLException {
        if (inst.getFechaInicio().isAfter(inst.getFechaFin())) {
            throw new SQLException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }
        if (idsEmpleados.isEmpty()) {
            throw new SQLException("Debe asignar al menos un empleado a la instancia.");
        }

        Connection con = ConectorBD.getInstancia().getConexion();
        con.setAutoCommit(false);
        try {
            int idInstancia = instanciaDAO.insertar(inst);
            for (int idEmp : idsEmpleados) {
                instanciaDAO.asignarEmpleado(idInstancia, idEmp);
                instanciaDAO.crearRegistroAsistenciaPendiente(idInstancia, idEmp);
            }
            con.commit();
            return idInstancia;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public void marcarRealizada(int idInstancia) throws SQLException {
        instanciaDAO.marcarRealizada(idInstancia);
    }

    public void cancelar(int idInstancia, String motivo) throws SQLException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new SQLException("Ingresá un motivo de cancelación.");
        }
        instanciaDAO.cancelar(idInstancia, motivo.trim());
    }
}
