package com.trainup.dao;

import com.trainup.model.PerfilCarrera;
import com.trainup.model.PerfilCurso;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerfilDAO {

    public List<PerfilCarrera> listarActivos() throws SQLException {
        String sql = "SELECT id, nombre, descripcion, activo, created_at, updated_at " +
                     "FROM perfiles_carrera WHERE activo = 1 ORDER BY nombre";
        List<PerfilCarrera> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public PerfilCarrera buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, descripcion, activo, created_at, updated_at " +
                     "FROM perfiles_carrera WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM perfiles_carrera WHERE nombre = ?" +
                     (idExcluir != null ? " AND id <> ?" : "");
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            if (idExcluir != null) ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean tieneEmpleadosActivos(int idPerfil) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empleados WHERE id_perfil = ? AND activo = 1";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int insertar(PerfilCarrera p) throws SQLException {
        String sql = "INSERT INTO perfiles_carrera (nombre, descripcion, activo) VALUES (?, ?, 1)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizar(PerfilCarrera p) throws SQLException {
        String sql = "UPDATE perfiles_carrera SET nombre=?, descripcion=? WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setInt(3, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE perfiles_carrera SET activo = 0 WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<PerfilCurso> listarCursosDePerfil(int idPerfil) throws SQLException {
        String sql = "SELECT pc.id_perfil, pc.id_curso, pc.es_obligatorio, pc.nivel_expertise, " +
                     "pc.orden, c.nombre AS nombre_curso " +
                     "FROM perfil_curso pc JOIN cursos c ON pc.id_curso = c.id " +
                     "WHERE pc.id_perfil = ? ORDER BY pc.orden, c.nombre";
        List<PerfilCurso> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PerfilCurso pc = new PerfilCurso();
                    pc.setIdPerfil(rs.getInt("id_perfil"));
                    pc.setIdCurso(rs.getInt("id_curso"));
                    pc.setNombreCurso(rs.getString("nombre_curso"));
                    pc.setEsObligatorio(rs.getBoolean("es_obligatorio"));
                    pc.setNivelExpertise(com.trainup.enums.NivelExpertise.fromString(rs.getString("nivel_expertise")));
                    int orden = rs.getInt("orden");
                    pc.setOrden(rs.wasNull() ? null : orden);
                    lista.add(pc);
                }
            }
        }
        return lista;
    }

    public void asociarCurso(PerfilCurso pc) throws SQLException {
        String sql = "INSERT INTO perfil_curso (id_perfil, id_curso, es_obligatorio, nivel_expertise, orden) " +
                     "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                     "es_obligatorio=VALUES(es_obligatorio), nivel_expertise=VALUES(nivel_expertise)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pc.getIdPerfil());
            ps.setInt(2, pc.getIdCurso());
            ps.setBoolean(3, pc.isEsObligatorio());
            ps.setString(4, pc.getNivelExpertise().name());
            if (pc.getOrden() != null) ps.setInt(5, pc.getOrden());
            else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public void desasociarCurso(int idPerfil, int idCurso) throws SQLException {
        String sql = "DELETE FROM perfil_curso WHERE id_perfil = ? AND id_curso = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);
            ps.setInt(2, idCurso);
            ps.executeUpdate();
        }
    }

    private PerfilCarrera mapear(ResultSet rs) throws SQLException {
        PerfilCarrera p = new PerfilCarrera();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setActivo(rs.getBoolean("activo"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return p;
    }
}
