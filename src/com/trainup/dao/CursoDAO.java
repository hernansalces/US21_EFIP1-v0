package com.trainup.dao;

import com.trainup.enums.ModalidadCurso;
import com.trainup.model.Curso;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    private static final String SELECT_BASE =
        "SELECT c.id, c.nombre, c.descripcion, c.duracion_horas, c.modalidad, " +
        "c.categoria_tematica, c.id_capacitador, c.activo, c.created_at, c.updated_at, " +
        "CONCAT(cap.nombre, ' ', cap.apellido) AS nombre_capacitador " +
        "FROM cursos c LEFT JOIN capacitadores_externos cap ON c.id_capacitador = cap.id ";

    public List<Curso> listarActivos() throws SQLException {
        return ejecutarLista(SELECT_BASE + "WHERE c.activo = 1 ORDER BY c.nombre");
    }

    public List<Curso> listarTodos() throws SQLException {
        return ejecutarLista(SELECT_BASE + "ORDER BY c.activo DESC, c.nombre");
    }

    public Curso buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE c.id = ?";
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
        String sql = "SELECT COUNT(*) FROM cursos WHERE nombre = ?" +
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

    public boolean tieneInstancias(int idCurso) throws SQLException {
        String sql = "SELECT COUNT(*) FROM instancias_capacitacion WHERE id_curso = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int insertar(Curso c) throws SQLException {
        String sql = "INSERT INTO cursos (nombre, descripcion, duracion_horas, modalidad, " +
                     "categoria_tematica, id_capacitador, activo) VALUES (?, ?, ?, ?, ?, ?, 1)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setBigDecimal(3, c.getDuracionHoras());
            ps.setString(4, c.getModalidad().name());
            ps.setString(5, c.getCategoriaTematica());
            if (c.getIdCapacitador() != null) ps.setInt(6, c.getIdCapacitador());
            else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean actualizar(Curso c) throws SQLException {
        String sql = "UPDATE cursos SET nombre=?, descripcion=?, duracion_horas=?, " +
                     "modalidad=?, categoria_tematica=?, id_capacitador=? WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setBigDecimal(3, c.getDuracionHoras());
            ps.setString(4, c.getModalidad().name());
            ps.setString(5, c.getCategoriaTematica());
            if (c.getIdCapacitador() != null) ps.setInt(6, c.getIdCapacitador());
            else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE cursos SET activo = 0 WHERE id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Curso> ejecutarLista(String sql) throws SQLException {
        List<Curso> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Curso mapear(ResultSet rs) throws SQLException {
        Curso c = new Curso();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setDuracionHoras(rs.getBigDecimal("duracion_horas"));
        c.setModalidad(ModalidadCurso.fromString(rs.getString("modalidad")));
        c.setCategoriaTematica(rs.getString("categoria_tematica"));
        int idCap = rs.getInt("id_capacitador");
        c.setIdCapacitador(rs.wasNull() ? null : idCap);
        c.setNombreCapacitador(rs.getString("nombre_capacitador"));
        c.setActivo(rs.getBoolean("activo"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    }
}
