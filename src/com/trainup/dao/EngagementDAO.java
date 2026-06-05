package com.trainup.dao;

import com.trainup.enums.CategoriaEngagement;
import com.trainup.model.HistorialEngagement;
import com.trainup.model.IndiceEngagement;
import com.trainup.util.ConectorBD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EngagementDAO {

    /** Calcula PA para un empleado (últimos 12 meses, excluye resultados pendientes). */
    public BigDecimal calcularPA(int idEmpleado) throws SQLException {
        String sql =
            "SELECT COALESCE( " +
            "  SUM(CASE WHEN ra.estado_asistencia='ASISTIO' AND ra.resultado!='PENDIENTE_RESULTADO' THEN 1 ELSE 0 END) * 100.0 / " +
            "  NULLIF(SUM(CASE WHEN ra.estado_asistencia IN ('ASISTIO','NO_ASISTIO') AND ra.resultado!='PENDIENTE_RESULTADO' THEN 1 ELSE 0 END), 0), " +
            "0) AS pa " +
            "FROM registros_asistencia ra " +
            "JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
            "WHERE ra.id_empleado = ? AND ic.fecha_inicio >= DATE_SUB(NOW(), INTERVAL 12 MONTH)";
        return ejecutarDecimal(sql, idEmpleado);
    }

    /** Calcula PR para un empleado (últimos 12 meses, excluye pendientes). */
    public BigDecimal calcularPR(int idEmpleado) throws SQLException {
        String sql =
            "SELECT COALESCE( " +
            "  SUM(CASE WHEN ra.resultado='APROBADO' THEN 1 ELSE 0 END) * 100.0 / " +
            "  NULLIF(SUM(CASE WHEN ra.estado_asistencia='ASISTIO' AND ra.resultado!='PENDIENTE_RESULTADO' THEN 1 ELSE 0 END), 0), " +
            "0) AS pr " +
            "FROM registros_asistencia ra " +
            "JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
            "WHERE ra.id_empleado = ? AND ic.fecha_inicio >= DATE_SUB(NOW(), INTERVAL 12 MONTH)";
        return ejecutarDecimal(sql, idEmpleado);
    }

    /** Calcula PP: % de cursos obligatorios del perfil aprobados por el empleado. */
    public BigDecimal calcularPP(int idEmpleado, int idPerfil) throws SQLException {
        String sql =
            "SELECT COALESCE( " +
            "  (SELECT COUNT(DISTINCT ic2.id_curso) " +
            "   FROM registros_asistencia ra2 " +
            "   JOIN instancias_capacitacion ic2 ON ra2.id_instancia = ic2.id " +
            "   JOIN perfil_curso pc2 ON ic2.id_curso = pc2.id_curso " +
            "   WHERE ra2.id_empleado = ? AND pc2.id_perfil = ? " +
            "   AND pc2.es_obligatorio = 1 AND ra2.estado_asistencia='ASISTIO' AND ra2.resultado='APROBADO' " +
            "  ) * 100.0 / " +
            "  NULLIF((SELECT COUNT(*) FROM perfil_curso WHERE id_perfil = ? AND es_obligatorio = 1), 0), " +
            "0) AS pp";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idPerfil);
            ps.setInt(3, idPerfil);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal val = rs.getBigDecimal(1);
                    return val != null ? val.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /** Verifica si hay resultados pendientes que hacen al IE provisional. */
    public boolean tieneResultadosPendientes(int idEmpleado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registros_asistencia ra " +
                     "JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
                     "WHERE ra.id_empleado = ? AND ra.resultado = 'PENDIENTE_RESULTADO' " +
                     "AND ra.estado_asistencia = 'ASISTIO' " +
                     "AND ic.fecha_inicio >= DATE_SUB(NOW(), INTERVAL 12 MONTH)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Guarda (upsert) el índice de engagement actual del empleado. */
    public void guardarIndice(IndiceEngagement ie) throws SQLException {
        String sql =
            "INSERT INTO indices_engagement (id_empleado, valor_ie, pa, pr, pp, categoria, es_provisional, fecha_calculo) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW()) " +
            "ON DUPLICATE KEY UPDATE valor_ie=VALUES(valor_ie), pa=VALUES(pa), pr=VALUES(pr), " +
            "pp=VALUES(pp), categoria=VALUES(categoria), es_provisional=VALUES(es_provisional), " +
            "fecha_calculo=NOW()";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ie.getIdEmpleado());
            ps.setBigDecimal(2, ie.getValorIe());
            ps.setBigDecimal(3, ie.getPa());
            ps.setBigDecimal(4, ie.getPr());
            ps.setBigDecimal(5, ie.getPp());
            ps.setString(6, ie.getCategoria().name());
            ps.setBoolean(7, ie.isEsProvisional());
            ps.executeUpdate();
        }
    }

    /** Inserta un registro en el historial de engagement. */
    public void guardarHistorial(IndiceEngagement ie) throws SQLException {
        String sql = "INSERT INTO historial_engagement (id_empleado, valor_ie, pa, pr, pp, categoria) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ie.getIdEmpleado());
            ps.setBigDecimal(2, ie.getValorIe());
            ps.setBigDecimal(3, ie.getPa());
            ps.setBigDecimal(4, ie.getPr());
            ps.setBigDecimal(5, ie.getPp());
            ps.setString(6, ie.getCategoria().name());
            ps.executeUpdate();
        }
    }

    public IndiceEngagement buscarPorEmpleado(int idEmpleado) throws SQLException {
        String sql = "SELECT id_empleado, valor_ie, pa, pr, pp, categoria, es_provisional, " +
                     "fecha_calculo, updated_at FROM indices_engagement WHERE id_empleado = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    IndiceEngagement ie = new IndiceEngagement();
                    ie.setIdEmpleado(rs.getInt("id_empleado"));
                    ie.setValorIe(rs.getBigDecimal("valor_ie"));
                    ie.setPa(rs.getBigDecimal("pa"));
                    ie.setPr(rs.getBigDecimal("pr"));
                    ie.setPp(rs.getBigDecimal("pp"));
                    ie.setCategoria(CategoriaEngagement.fromString(rs.getString("categoria")));
                    ie.setEsProvisional(rs.getBoolean("es_provisional"));
                    Timestamp fc = rs.getTimestamp("fecha_calculo");
                    ie.setFechaCalculo(fc != null ? fc.toLocalDateTime() : null);
                    return ie;
                }
            }
        }
        return null;
    }

    public List<HistorialEngagement> listarHistorial(int idEmpleado) throws SQLException {
        String sql = "SELECT id, id_empleado, valor_ie, pa, pr, pp, categoria, fecha_calculo " +
                     "FROM historial_engagement WHERE id_empleado = ? ORDER BY fecha_calculo DESC LIMIT 24";
        List<HistorialEngagement> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialEngagement h = new HistorialEngagement();
                    h.setId(rs.getInt("id"));
                    h.setIdEmpleado(rs.getInt("id_empleado"));
                    h.setValorIe(rs.getBigDecimal("valor_ie"));
                    h.setPa(rs.getBigDecimal("pa"));
                    h.setPr(rs.getBigDecimal("pr"));
                    h.setPp(rs.getBigDecimal("pp"));
                    h.setCategoria(CategoriaEngagement.fromString(rs.getString("categoria")));
                    h.setFechaCalculo(rs.getTimestamp("fecha_calculo").toLocalDateTime());
                    lista.add(h);
                }
            }
        }
        return lista;
    }

    private BigDecimal ejecutarDecimal(String sql, int idEmpleado) throws SQLException {
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal val = rs.getBigDecimal(1);
                    return val != null ? val.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
