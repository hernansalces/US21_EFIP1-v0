package com.trainup.controller;

import com.trainup.dao.CursoDAO;
import com.trainup.dao.PerfilDAO;
import com.trainup.model.Curso;
import com.trainup.model.PerfilCarrera;
import com.trainup.model.PerfilCurso;

import java.sql.SQLException;
import java.util.List;

public class CtrlPerfil {

    private final PerfilDAO perfilDAO = new PerfilDAO();
    private final CursoDAO cursoDAO = new CursoDAO();

    public List<PerfilCarrera> obtenerActivos() throws SQLException {
        return perfilDAO.listarActivos();
    }

    public List<Curso> obtenerCursosDisponibles() throws SQLException {
        return cursoDAO.listarActivos();
    }

    public List<PerfilCurso> obtenerCursosDePerfil(int idPerfil) throws SQLException {
        return perfilDAO.listarCursosDePerfil(idPerfil);
    }

    public int registrar(PerfilCarrera p) throws SQLException {
        validar(p, null);
        return perfilDAO.insertar(p);
    }

    public void actualizar(PerfilCarrera p) throws SQLException {
        validar(p, p.getId());
        perfilDAO.actualizar(p);
    }

    public void desactivar(int idPerfil) throws SQLException {
        if (perfilDAO.tieneEmpleadosActivos(idPerfil)) {
            throw new SQLException("No se puede eliminar un perfil con empleados activos asignados. " +
                                   "Reasigná los empleados antes de eliminarlo.");
        }
        perfilDAO.desactivar(idPerfil);
    }

    public void asociarCurso(PerfilCurso pc) throws SQLException {
        perfilDAO.asociarCurso(pc);
    }

    public void desasociarCurso(int idPerfil, int idCurso) throws SQLException {
        perfilDAO.desasociarCurso(idPerfil, idCurso);
    }

    private void validar(PerfilCarrera p, Integer idExcluir) throws SQLException {
        if (perfilDAO.existeNombre(p.getNombre(), idExcluir)) {
            throw new SQLException("Ya existe un perfil de carrera con el nombre '" + p.getNombre() + "'.");
        }
    }
}
