package com.trainup.dao;

import com.trainup.enums.RolUsuario;
import com.trainup.model.UsuarioSistema;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.time.LocalDateTime;

public class UsuarioDAO {

    public UsuarioSistema buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, password_salt, rol, " +
                     "id_empleado, activo, ultimo_acceso, created_at, updated_at " +
                     "FROM usuarios_sistema WHERE username = ? AND activo = 1";

        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public void actualizarUltimoAcceso(int idUsuario) throws SQLException {
        String sql = "UPDATE usuarios_sistema SET ultimo_acceso = NOW() WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private UsuarioSistema mapear(ResultSet rs) throws SQLException {
        UsuarioSistema u = new UsuarioSistema();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPasswordSalt(rs.getString("password_salt"));
        u.setRol(RolUsuario.fromString(rs.getString("rol")));
        int idEmp = rs.getInt("id_empleado");
        u.setIdEmpleado(rs.wasNull() ? null : idEmp);
        u.setActivo(rs.getBoolean("activo"));
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        u.setUltimoAcceso(ultimoAcceso != null ? ultimoAcceso.toLocalDateTime() : null);
        u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        u.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return u;
    }
}
