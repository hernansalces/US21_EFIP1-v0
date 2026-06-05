package com.trainup.controller;

import com.trainup.dao.CapacitadorExternoDAO;
import com.trainup.dao.CursoDAO;
import com.trainup.model.CapacitadorExterno;
import com.trainup.model.Curso;

import java.sql.SQLException;
import java.util.List;

public class CtrlCurso {

    private final CursoDAO cursoDAO = new CursoDAO();
    private final CapacitadorExternoDAO capacitadorDAO = new CapacitadorExternoDAO();

    public List<Curso> obtenerActivos() throws SQLException {
        return cursoDAO.listarActivos();
    }

    public List<Curso> obtenerTodos() throws SQLException {
        return cursoDAO.listarTodos();
    }

    public List<CapacitadorExterno> obtenerCapacitadores() throws SQLException {
        return capacitadorDAO.listarActivos();
    }

    public int registrar(Curso c) throws SQLException {
        validar(c, null);
        return cursoDAO.insertar(c);
    }

    public void actualizar(Curso c) throws SQLException {
        validar(c, c.getId());
        cursoDAO.actualizar(c);
    }

    public void desactivar(int idCurso) throws SQLException {
        if (cursoDAO.tieneInstancias(idCurso)) {
            throw new SQLException("No se puede eliminar un curso con instancias registradas. " +
                                   "Podés desactivarlo para que no aparezca en nuevas programaciones.");
        }
        cursoDAO.desactivar(idCurso);
    }

    private void validar(Curso c, Integer idExcluir) throws SQLException {
        if (cursoDAO.existeNombre(c.getNombre(), idExcluir)) {
            throw new SQLException("Ya existe un curso con el nombre '" + c.getNombre() + "'.");
        }
    }
}
