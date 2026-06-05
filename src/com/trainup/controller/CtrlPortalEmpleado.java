package com.trainup.controller;

import com.trainup.dao.AlertaDAO;
import com.trainup.dao.EngagementDAO;
import com.trainup.dao.PerfilDAO;
import com.trainup.model.*;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del Portal del Empleado.
 * Todas las consultas filtran por id_empleado de la sesión activa.
 */
public class CtrlPortalEmpleado {

    private final EngagementDAO engDAO   = new EngagementDAO();
    private final AlertaDAO     alertaDAO = new AlertaDAO();
    private final PerfilDAO     perfilDAO = new PerfilDAO();

    // ── CU-13: Plan de carrera ────────────────────────────────────────

    /** Perfil de carrera asignado al empleado. */
    public PerfilCarrera obtenerPerfil(int idPerfil) throws SQLException {
        return perfilDAO.buscarPorId(idPerfil);
    }

    /**
     * Cursos del perfil con estado de completitud para el empleado.
     * Retorna lista de PerfilCurso con campo 'nombreCurso' y un campo extra
     * de completado (extendido via subclase inline).
     */
    public List<CursoConEstado> obtenerCursosConEstado(int idPerfil, int idEmpleado) throws SQLException {
        String sql =
            "SELECT pc.id_curso, pc.es_obligatorio, pc.nivel_expertise, pc.orden, " +
            "c.nombre AS nombre_curso, c.duracion_horas, c.modalidad, " +
            "EXISTS ( " +
            "  SELECT 1 FROM registros_asistencia ra " +
            "  JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
            "  WHERE ra.id_empleado = ? AND ic.id_curso = pc.id_curso " +
            "  AND ra.estado_asistencia = 'ASISTIO' AND ra.resultado = 'APROBADO' " +
            ") AS completado " +
            "FROM perfil_curso pc JOIN cursos c ON pc.id_curso = c.id " +
            "WHERE pc.id_perfil = ? " +
            "ORDER BY pc.es_obligatorio DESC, pc.nivel_expertise, c.nombre";

        List<CursoConEstado> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idPerfil);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CursoConEstado c = new CursoConEstado();
                    c.setIdCurso(rs.getInt("id_curso"));
                    c.setNombreCurso(rs.getString("nombre_curso"));
                    c.setEsObligatorio(rs.getBoolean("es_obligatorio"));
                    c.setNivelExpertise(com.trainup.enums.NivelExpertise.fromString(rs.getString("nivel_expertise")));
                    c.setCompletado(rs.getBoolean("completado"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    // ── CU-14: Índice de engagement ───────────────────────────────────

    public IndiceEngagement obtenerIndice(int idEmpleado) throws SQLException {
        return engDAO.buscarPorEmpleado(idEmpleado);
    }

    public List<HistorialEngagement> obtenerHistorialIE(int idEmpleado) throws SQLException {
        return engDAO.listarHistorial(idEmpleado);
    }

    // ── CU-15: Mis capacitaciones ─────────────────────────────────────

    public List<InstanciaCapacitacion> obtenerProximas(int idEmpleado) throws SQLException {
        String sql =
            "SELECT ic.id, ic.id_curso, ic.fecha_inicio, ic.fecha_fin, ic.modalidad, " +
            "ic.lugar_url, ic.estado, ic.motivo_cancelacion, ic.created_at, ic.updated_at, " +
            "c.nombre AS nombre_curso " +
            "FROM instancias_capacitacion ic " +
            "JOIN instancia_empleado ie ON ic.id = ie.id_instancia " +
            "JOIN cursos c ON ic.id_curso = c.id " +
            "WHERE ie.id_empleado = ? AND ic.fecha_inicio > NOW() AND ic.estado = 'PROGRAMADA' " +
            "ORDER BY ic.fecha_inicio";
        return ejecutarListaInstancias(sql, idEmpleado);
    }

    public List<RegistroConInstancia> obtenerHistorialCapacitaciones(int idEmpleado) throws SQLException {
        String sql =
            "SELECT ra.estado_asistencia, ra.resultado, ra.fecha_registro, " +
            "ic.fecha_inicio, c.nombre AS nombre_curso, ic.modalidad " +
            "FROM registros_asistencia ra " +
            "JOIN instancias_capacitacion ic ON ra.id_instancia = ic.id " +
            "JOIN cursos c ON ic.id_curso = c.id " +
            "WHERE ra.id_empleado = ? AND ic.fecha_inicio <= NOW() " +
            "ORDER BY ic.fecha_inicio DESC";

        List<RegistroConInstancia> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistroConInstancia r = new RegistroConInstancia();
                    r.nombreCurso    = rs.getString("nombre_curso");
                    r.fechaInstancia = rs.getTimestamp("fecha_inicio").toLocalDateTime();
                    r.estadoAsistencia = com.trainup.enums.EstadoAsistencia.fromString(rs.getString("estado_asistencia"));
                    r.resultado      = com.trainup.enums.ResultadoCurso.fromString(rs.getString("resultado"));
                    r.modalidad      = com.trainup.enums.ModalidadCurso.fromString(rs.getString("modalidad"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    // ── CU-16: Notificaciones ─────────────────────────────────────────

    public List<Alerta> obtenerAlertasActivas(int idEmpleado) throws SQLException {
        return alertaDAO.listarActivasEmpleado(idEmpleado);
    }

    public int contarNoLeidas(int idEmpleado) throws SQLException {
        return alertaDAO.contarNoLeidasEmpleado(idEmpleado);
    }

    public void marcarLeidaEmpleado(int idAlerta) throws SQLException {
        alertaDAO.marcarLeidaEmpleado(idAlerta);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private List<InstanciaCapacitacion> ejecutarListaInstancias(String sql, int idEmpleado) throws SQLException {
        List<InstanciaCapacitacion> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InstanciaCapacitacion inst = new InstanciaCapacitacion();
                    inst.setId(rs.getInt("id"));
                    inst.setIdCurso(rs.getInt("id_curso"));
                    inst.setNombreCurso(rs.getString("nombre_curso"));
                    inst.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
                    inst.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
                    inst.setModalidad(com.trainup.enums.ModalidadCurso.fromString(rs.getString("modalidad")));
                    inst.setLugarUrl(rs.getString("lugar_url"));
                    inst.setEstado(com.trainup.enums.EstadoInstancia.fromString(rs.getString("estado")));
                    lista.add(inst);
                }
            }
        }
        return lista;
    }

    // ── DTOs internos ─────────────────────────────────────────────────

    public static class CursoConEstado {
        private int idCurso;
        private String nombreCurso;
        private boolean esObligatorio;
        private com.trainup.enums.NivelExpertise nivelExpertise;
        private boolean completado;

        public int getIdCurso() { return idCurso; }
        public void setIdCurso(int id) { this.idCurso = id; }
        public String getNombreCurso() { return nombreCurso; }
        public void setNombreCurso(String n) { this.nombreCurso = n; }
        public boolean isEsObligatorio() { return esObligatorio; }
        public void setEsObligatorio(boolean b) { this.esObligatorio = b; }
        public com.trainup.enums.NivelExpertise getNivelExpertise() { return nivelExpertise; }
        public void setNivelExpertise(com.trainup.enums.NivelExpertise n) { this.nivelExpertise = n; }
        public boolean isCompletado() { return completado; }
        public void setCompletado(boolean b) { this.completado = b; }
    }

    public static class RegistroConInstancia {
        public String nombreCurso;
        public java.time.LocalDateTime fechaInstancia;
        public com.trainup.enums.EstadoAsistencia estadoAsistencia;
        public com.trainup.enums.ResultadoCurso resultado;
        public com.trainup.enums.ModalidadCurso modalidad;
    }
}
