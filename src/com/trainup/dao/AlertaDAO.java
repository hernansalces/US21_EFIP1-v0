package com.trainup.dao;

import com.trainup.enums.OrigenAlerta;
import com.trainup.enums.TipoAlerta;
import com.trainup.model.Alerta;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    public void insertar(Alerta a) throws SQLException {
        String sql = "INSERT INTO alertas (id_empleado, tipo, origen, mensaje, leida_empleado, leida_admin) " +
                     "VALUES (?, ?, ?, ?, 0, 0)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getIdEmpleado());
            ps.setString(2, a.getTipo().name());
            ps.setString(3, a.getOrigen().name());
            ps.setString(4, a.getMensaje());
            ps.executeUpdate();
        }
    }

    public List<Alerta> listarActivasAdmin() throws SQLException {
        String sql = "SELECT a.*, CONCAT(e.nombre,' ',e.apellido) AS nombre_empleado " +
                     "FROM alertas a JOIN empleados e ON a.id_empleado = e.id " +
                     "WHERE a.leida_admin = 0 ORDER BY a.fecha_generacion DESC";
        return ejecutarLista(sql, -1);
    }

    public List<Alerta> listarActivasEmpleado(int idEmpleado) throws SQLException {
        String sql = "SELECT a.*, CONCAT(e.nombre,' ',e.apellido) AS nombre_empleado " +
                     "FROM alertas a JOIN empleados e ON a.id_empleado = e.id " +
                     "WHERE a.id_empleado = ? AND a.leida_empleado = 0 ORDER BY a.fecha_generacion DESC";
        return ejecutarLista(sql, idEmpleado);
    }

    public int contarNoLeidasEmpleado(int idEmpleado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM alertas WHERE id_empleado = ? AND leida_empleado = 0";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void marcarLeidaAdmin(int id) throws SQLException {
        String sql = "UPDATE alertas SET leida_admin=1, fecha_lectura_admin=NOW() WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void marcarLeidaEmpleado(int id) throws SQLException {
        String sql = "UPDATE alertas SET leida_empleado=1, fecha_lectura_empleado=NOW() WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Alerta> ejecutarLista(String sql, int idEmpleado) throws SQLException {
        List<Alerta> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (idEmpleado > 0) ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Alerta mapear(ResultSet rs) throws SQLException {
        Alerta a = new Alerta();
        a.setId(rs.getInt("id"));
        a.setIdEmpleado(rs.getInt("id_empleado"));
        a.setNombreEmpleado(rs.getString("nombre_empleado"));
        a.setTipo(TipoAlerta.fromString(rs.getString("tipo")));
        a.setOrigen(OrigenAlerta.fromString(rs.getString("origen")));
        a.setMensaje(rs.getString("mensaje"));
        a.setLeidaEmpleado(rs.getBoolean("leida_empleado"));
        a.setLeidaAdmin(rs.getBoolean("leida_admin"));
        a.setFechaGeneracion(rs.getTimestamp("fecha_generacion").toLocalDateTime());
        Timestamp tEmp = rs.getTimestamp("fecha_lectura_empleado");
        a.setFechaLecturaEmpleado(tEmp != null ? tEmp.toLocalDateTime() : null);
        Timestamp tAdm = rs.getTimestamp("fecha_lectura_admin");
        a.setFechaLecturaAdmin(tAdm != null ? tAdm.toLocalDateTime() : null);
        return a;
    }
}
