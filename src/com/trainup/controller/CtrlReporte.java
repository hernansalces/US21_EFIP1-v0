package com.trainup.controller;

import com.trainup.enums.CategoriaEngagement;
import com.trainup.util.ConectorBD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CtrlReporte {

    public List<FilaReporte> generarPorArea(String area, LocalDate desde, LocalDate hasta) throws SQLException {
        String whereArea = area != null ? "AND e.area = ?" : "";
        String sql =
            "SELECT e.id, e.nombre, e.apellido, e.area, e.puesto, " +
            "p.nombre AS perfil, " +
            "COALESCE(ie.valor_ie, 0) AS valor_ie, " +
            "ie.categoria, " +
            // % asistencia en período
            "COALESCE((" +
            "  SELECT ROUND(SUM(CASE WHEN ra.estado_asistencia='ASISTIO' THEN 1 ELSE 0 END)*100.0 / " +
            "  NULLIF(COUNT(CASE WHEN ra.estado_asistencia IN ('ASISTIO','NO_ASISTIO') THEN 1 END),0),1) " +
            "  FROM registros_asistencia ra " +
            "  JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
            "  WHERE ra.id_empleado = e.id AND ic.fecha_inicio BETWEEN ? AND ?" +
            "),0) AS pct_asistencia, " +
            // cursos obligatorios completados / total
            "COALESCE((" +
            "  SELECT COUNT(DISTINCT ic2.id_curso) " +
            "  FROM registros_asistencia ra2 " +
            "  JOIN instancias_capacitacion ic2 ON ra2.id_instancia = ic2.id " +
            "  JOIN perfil_curso pc2 ON ic2.id_curso = pc2.id_curso " +
            "  WHERE ra2.id_empleado = e.id AND pc2.id_perfil = e.id_perfil " +
            "  AND ra2.estado_asistencia='ASISTIO' AND ra2.resultado='APROBADO'" +
            "),0) AS completados, " +
            "COALESCE((SELECT COUNT(*) FROM perfil_curso WHERE id_perfil=e.id_perfil AND es_obligatorio=1),0) AS total_obligatorios, " +
            "(SELECT COUNT(*) FROM alertas WHERE id_empleado=e.id AND leida_admin=0) AS alertas_activas " +
            "FROM empleados e " +
            "LEFT JOIN indices_engagement ie ON e.id = ie.id_empleado " +
            "LEFT JOIN perfiles_carrera p ON e.id_perfil = p.id " +
            "WHERE e.activo = 1 " + whereArea +
            " ORDER BY e.area, ie.valor_ie ASC";

        List<FilaReporte> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            ps.setDate(idx++, Date.valueOf(desde));
            ps.setDate(idx++, Date.valueOf(hasta));
            if (area != null) ps.setString(idx, area);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FilaReporte f = new FilaReporte();
                    f.nombre      = rs.getString("nombre") + " " + rs.getString("apellido");
                    f.area        = rs.getString("area");
                    f.puesto      = rs.getString("puesto");
                    f.perfil      = rs.getString("perfil");
                    f.valorIe     = rs.getBigDecimal("valor_ie");
                    String cat    = rs.getString("categoria");
                    f.categoria   = cat != null ? CategoriaEngagement.fromString(cat) : null;
                    f.pctAsistencia = rs.getBigDecimal("pct_asistencia");
                    f.completados  = rs.getInt("completados");
                    f.totalOblig   = rs.getInt("total_obligatorios");
                    f.alertasActivas = rs.getInt("alertas_activas");
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

    public static class FilaReporte {
        public String nombre, area, puesto, perfil;
        public BigDecimal valorIe, pctAsistencia;
        public CategoriaEngagement categoria;
        public int completados, totalOblig, alertasActivas;

        public String getPctCompletados() {
            if (totalOblig == 0) return "S/P";
            return Math.round(completados * 100.0 / totalOblig) + "%";
        }
    }
}
