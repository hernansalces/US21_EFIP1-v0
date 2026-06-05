package com.trainup.controller;

import com.trainup.dao.EmpleadoDAO;
import com.trainup.dao.PerfilDAO;
import com.trainup.model.Empleado;
import com.trainup.model.PerfilCarrera;

import java.sql.SQLException;
import java.util.List;

public class CtrlEmpleado {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();

    public List<Empleado> obtenerActivos() throws SQLException {
        return empleadoDAO.listarActivos();
    }

    public List<Empleado> obtenerTodos() throws SQLException {
        return empleadoDAO.listarTodos();
    }

    public List<PerfilCarrera> obtenerPerfiles() throws SQLException {
        return perfilDAO.listarActivos();
    }

    public List<String> obtenerAreas() throws SQLException {
        return empleadoDAO.listarAreas();
    }

    /**
     * Registra un nuevo empleado. Retorna el ID generado o lanza excepción con mensaje descriptivo.
     */
    public int registrar(Empleado e) throws SQLException {
        validar(e, null);
        return empleadoDAO.insertar(e);
    }

    /**
     * Actualiza un empleado existente. Lanza excepción con mensaje descriptivo si hay error de validación.
     */
    public void actualizar(Empleado e) throws SQLException {
        validar(e, e.getId());
        empleadoDAO.actualizar(e);
    }

    /**
     * Da de baja lógica al empleado. Retorna la cantidad de instancias futuras afectadas.
     */
    public int darDeBaja(int idEmpleado) throws SQLException {
        int instanciasFuturas = empleadoDAO.contarInstanciasFuturas(idEmpleado);
        empleadoDAO.darDeBaja(idEmpleado);
        return instanciasFuturas;
    }

    public void reactivar(int idEmpleado) throws SQLException {
        empleadoDAO.reactivar(idEmpleado);
    }

    public Empleado buscarInactivoPorDni(String dni) throws SQLException {
        return empleadoDAO.buscarInactivoPorDni(dni);
    }

    public Empleado buscarInactivoPorEmail(String email) throws SQLException {
        return empleadoDAO.buscarInactivoPorEmail(email);
    }

    private void validar(Empleado e, Integer idExcluir) throws SQLException {
        if (empleadoDAO.existeEmail(e.getEmail(), idExcluir)) {
            throw new SQLException("El email '" + e.getEmail() + "' ya está registrado en el sistema.");
        }
        if (empleadoDAO.existeDni(e.getDni(), idExcluir)) {
            throw new SQLException("El DNI '" + e.getDni() + "' ya está registrado en el sistema.");
        }
    }
}
