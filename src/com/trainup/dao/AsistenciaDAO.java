package com.trainup.dao;

import com.trainup.enums.EstadoAsistencia;
import com.trainup.enums.ResultadoCurso;
import com.trainup.model.RegistroAsistencia;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    public List<RegistroAsistencia> listarPorInstancia(int idInstancia) throws SQLException {
        String sql = "SELECT ra.id, ra.id_instancia, ra.id_empleado, " +
                     "ra.estado_asistencia, ra.resultado, ra.observaciones, " +
                     "ra.fecha_registro, ra.updated_at, " +
                     "CONCAT(e.nombre, ' ', e.apellido) AS nombre_empleado, " +
                     "e.area, e.puesto " +
                     "FROM registros_asistencia ra " +
                     "JOIN empleados e ON ra.id_empleado = e.id " +
                     "WHERE ra.id_instancia = ? ORDER BY e.apellido, e.nombre";
        List<RegistroAsistencia> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstancia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void guardar(RegistroAsistencia ra) throws SQLException {
        // UPSERT: crea el registro si no existe, lo actualiza si ya existe
        String sql = "INSERT INTO registros_asistencia " +
                     "(id_instancia, id_empleado, estado_asistencia, resultado, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "estado_asistencia=VALUES(estado_asistencia), " +
                     "resultado=VALUES(resultado), " +
                     "observaciones=VALUES(observaciones)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ra.getIdInstancia());
            ps.setInt(2, ra.getIdEmpleado());
            ps.setString(3, ra.getEstadoAsistencia().name());
            ps.setString(4, ra.getResultado().name());
            ps.setString(5, ra.getObservaciones());
            ps.executeUpdate();
        }
    }

    public void guardarTodos(List<RegistroAsistencia> registros) throws SQLException {
        Connection con = ConectorBD.getInstancia().getConexion();
        con.setAutoCommit(false);
        try {
            for (RegistroAsistencia ra : registros) guardar(ra);
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    private RegistroAsistencia mapear(ResultSet rs) throws SQLException {
        RegistroAsistencia ra = new RegistroAsistencia();
        ra.setId(rs.getInt("id"));
        ra.setIdInstancia(rs.getInt("id_instancia"));
        ra.setIdEmpleado(rs.getInt("id_empleado"));
        ra.setNombreEmpleado(rs.getString("nombre_empleado"));
        ra.setEstadoAsistencia(EstadoAsistencia.fromString(rs.getString("estado_asistencia")));
        ra.setResultado(ResultadoCurso.fromString(rs.getString("resultado")));
        ra.setObservaciones(rs.getString("observaciones"));
        ra.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        ra.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return ra;
    }
}
