package com.trainup.controller;

import com.trainup.dao.AlertaDAO;
import com.trainup.model.Alerta;

import java.sql.SQLException;
import java.util.List;

public class CtrlAlerta {

    private final AlertaDAO alertaDAO = new AlertaDAO();

    public List<Alerta> obtenerActivasAdmin() throws SQLException {
        return alertaDAO.listarActivasAdmin();
    }

    public void marcarLeidaAdmin(int idAlerta) throws SQLException {
        alertaDAO.marcarLeidaAdmin(idAlerta);
    }

    public int contarActivasAdmin() throws SQLException {
        return alertaDAO.listarActivasAdmin().size();
    }
}
