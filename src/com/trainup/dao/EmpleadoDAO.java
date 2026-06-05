package com.trainup.dao;

import com.trainup.model.Empleado;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private static final String SELECT_BASE =
        "SELECT e.id, e.nombre, e.apellido, e.dni, e.email, e.telefono, " +
        "e.fecha_ingreso, e.puesto, e.area, e.id_perfil, e.activo, e.fecha_baja, " +
        "e.created_at, e.updated_at, p.nombre AS nombre_perfil " +
        "FROM empleados e LEFT JOIN perfiles_carrera p ON e.id_perfil = p.id ";

    public List<Empleado> listarActivos() throws SQLException {
        return ejecutarLista(SELECT_BASE + "WHERE e.activo = 1 ORDER BY e.apellido, e.nombre");
    }

    public List<Empleado> listarTodos() throws SQLException {
        return ejecutarLista(SELECT_BASE + "ORDER BY e.activo DESC, e.apellido, e.nombre");
    }

    public List<String> listarAreas() throws SQLException {
        String sql = "SELECT DISTINCT area FROM empleados WHERE activo = 1 ORDER BY area";
        List<String> areas = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) areas.add(rs.getString("area"));
        }
        return areas;
    }

    public Empleado buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE e.id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean existeEmail(String email, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleados WHERE email = ?" +
                     (idExcluir != null ? " AND id <> ?" : "");
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            if (idExcluir != null) ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeDni(String dni, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleados WHERE dni = ?" +
                     (idExcluir != null ? " AND id <> ?" : "");
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            if (idExcluir != null) ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, apellido, dni, email, telefono, " +
                     "fecha_ingreso, puesto, area, id_perfil, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getDni());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getTelefono());
            ps.setDate(6, Date.valueOf(e.getFechaIngreso()));
            ps.setString(7, e.getPuesto());
            ps.setString(8, e.getArea());
            if (e.getIdPerfil() != null) ps.setInt(9, e.getIdPerfil());
            else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizar(Empleado e) throws SQLException {
        String sql = "UPDATE empleados SET nombre=?, apellido=?, dni=?, email=?, telefono=?, " +
                     "fecha_ingreso=?, puesto=?, area=?, id_perfil=? WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getDni());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getTelefono());
            ps.setDate(6, Date.valueOf(e.getFechaIngreso()));
            ps.setString(7, e.getPuesto());
            ps.setString(8, e.getArea());
            if (e.getIdPerfil() != null) ps.setInt(9, e.getIdPerfil());
            else ps.setNull(9, Types.INTEGER);
            ps.setInt(10, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean darDeBaja(int id) throws SQLException {
        String sql = "UPDATE empleados SET activo = 0, fecha_baja = CURDATE() WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean reactivar(int id) throws SQLException {
        String sql = "UPDATE empleados SET activo = 1, fecha_baja = NULL WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Empleado buscarInactivoPorDni(String dni) throws SQLException {
        String sql = SELECT_BASE + "WHERE e.dni = ? AND e.activo = 0";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Empleado buscarInactivoPorEmail(String email) throws SQLException {
        String sql = SELECT_BASE + "WHERE e.email = ? AND e.activo = 0";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public int contarInstanciasFuturas(int idEmpleado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM instancia_empleado ie " +
                     "JOIN instancias_capacitacion ic ON ie.id_instancia = ic.id " +
                     "WHERE ie.id_empleado = ? AND ic.fecha_inicio > NOW() AND ic.estado = 'PROGRAMADA'";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Empleado> ejecutarLista(String sql) throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setId(rs.getInt("id"));
        e.setNombre(rs.getString("nombre"));
        e.setApellido(rs.getString("apellido"));
        e.setDni(rs.getString("dni"));
        e.setEmail(rs.getString("email"));
        e.setTelefono(rs.getString("telefono"));
        e.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
        e.setPuesto(rs.getString("puesto"));
        e.setArea(rs.getString("area"));
        int idPerfil = rs.getInt("id_perfil");
        e.setIdPerfil(rs.wasNull() ? null : idPerfil);
        e.setNombrePerfil(rs.getString("nombre_perfil"));
        e.setActivo(rs.getBoolean("activo"));
        Date fechaBaja = rs.getDate("fecha_baja");
        e.setFechaBaja(fechaBaja != null ? fechaBaja.toLocalDate() : null);
        e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        e.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return e;
    }
}
