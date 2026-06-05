package com.trainup.dao;

import com.trainup.model.CapacitadorExterno;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CapacitadorExternoDAO {

    public List<CapacitadorExterno> listarActivos() throws SQLException {
        String sql = "SELECT id, nombre, apellido, email, telefono, empresa, especialidad, " +
                     "activo, created_at, updated_at FROM capacitadores_externos " +
                     "WHERE activo = 1 ORDER BY apellido, nombre";
        List<CapacitadorExterno> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private CapacitadorExterno mapear(ResultSet rs) throws SQLException {
        CapacitadorExterno c = new CapacitadorExterno();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmpresa(rs.getString("empresa"));
        c.setEspecialidad(rs.getString("especialidad"));
        c.setActivo(rs.getBoolean("activo"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}
