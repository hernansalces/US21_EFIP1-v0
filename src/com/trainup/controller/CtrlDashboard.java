package com.trainup.controller;

import com.trainup.enums.CategoriaEngagement;
import com.trainup.model.Empleado;
import com.trainup.model.IndiceEngagement;
import com.trainup.util.ConectorBD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CtrlDashboard {

    /** KPIs globales: promedio IE, conteo por categoría, total empleados con IE. */
    public DashboardStats obtenerStats(String area) throws SQLException {
        String where = area != null ? "AND e.area = ?" : "";
        String sql =
            "SELECT COUNT(*) AS total, " +
            "COALESCE(AVG(ie.valor_ie), 0) AS promedio, " +
            "SUM(CASE WHEN ie.categoria = 'ALTO'    THEN 1 ELSE 0 END) AS alto, " +
            "SUM(CASE WHEN ie.categoria = 'MEDIO'   THEN 1 ELSE 0 END) AS medio, " +
            "SUM(CASE WHEN ie.categoria = 'CRITICO' THEN 1 ELSE 0 END) AS critico " +
            "FROM empleados e " +
            "LEFT JOIN indices_engagement ie ON e.id = ie.id_empleado " +
            "WHERE e.activo = 1 " + where;

        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (area != null) ps.setString(1, area);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DashboardStats s = new DashboardStats();
                    s.total   = rs.getInt("total");
                    s.promedio = rs.getBigDecimal("promedio") != null
                        ? rs.getBigDecimal("promedio").setScale(1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    s.alto    = rs.getInt("alto");
                    s.medio   = rs.getInt("medio");
                    s.critico = rs.getInt("critico");
                    return s;
                }
            }
        }
        return new DashboardStats();
    }

    /** Ranking de empleados activos por IE descendente, con filtro opcional de área. */
    public List<FilaRanking> obtenerRanking(String area) throws SQLException {
        String where = area != null ? "AND e.area = ?" : "";
        String sql =
            "SELECT e.id, e.nombre, e.apellido, e.area, e.puesto, " +
            "p.nombre AS perfil, " +
            "COALESCE(ie.valor_ie, -1) AS valor_ie, " +
            "ie.categoria, ie.pa, ie.pr, ie.pp, " +
            "(SELECT COUNT(*) FROM alertas a WHERE a.id_empleado = e.id AND a.leida_admin = 0) AS alertas " +
            "FROM empleados e " +
            "LEFT JOIN indices_engagement ie ON e.id = ie.id_empleado " +
            "LEFT JOIN perfiles_carrera p ON e.id_perfil = p.id " +
            "WHERE e.activo = 1 " + where +
            "ORDER BY COALESCE(ie.valor_ie, -1) DESC";

        List<FilaRanking> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (area != null) ps.setString(1, area);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilaRanking f = new FilaRanking();
                    f.idEmpleado = rs.getInt("id");
                    f.nombre     = rs.getString("nombre") + " " + rs.getString("apellido");
                    f.area       = rs.getString("area");
                    f.puesto     = rs.getString("puesto");
                    f.perfil     = rs.getString("perfil");
                    BigDecimal v = rs.getBigDecimal("valor_ie");
                    f.valorIe    = (v != null && v.compareTo(BigDecimal.ZERO) >= 0) ? v : null;
                    String cat   = rs.getString("categoria");
                    f.categoria  = cat != null ? CategoriaEngagement.fromString(cat) : null;
                    f.alertas    = rs.getInt("alertas");
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    public List<String> obtenerAreas() throws SQLException {
        String sql = "SELECT DISTINCT area FROM empleados WHERE activo = 1 ORDER BY area";
        List<String> areas = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) areas.add(rs.getString("area"));
        }
        return areas;
    }

    // ── DTOs ─────────────────────────────────────────────────────────

    public static class DashboardStats {
        public int total, alto, medio, critico;
        public BigDecimal promedio = BigDecimal.ZERO;
        public int sinDatos() { return total - alto - medio - critico; }
        public int pctAlto()   { return total > 0 ? alto   * 100 / total : 0; }
        public int pctMedio()  { return total > 0 ? medio  * 100 / total : 0; }
        public int pctCritico(){ return total > 0 ? critico* 100 / total : 0; }
    }

    public static class FilaRanking {
        public int idEmpleado;
        public String nombre, area, puesto, perfil;
        public BigDecimal valorIe;
        public CategoriaEngagement categoria;
        public int alertas;
    }
}
