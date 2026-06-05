package com.trainup.dao;

import com.trainup.enums.EstadoInstancia;
import com.trainup.enums.EstadoAsistencia;
import com.trainup.enums.ModalidadCurso;
import com.trainup.enums.ResultadoCurso;
import com.trainup.model.Empleado;
import com.trainup.model.InstanciaCapacitacion;
import com.trainup.model.RegistroAsistencia;
import com.trainup.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstanciaDAO {

    private static final String SELECT_BASE =
        "SELECT ic.id, ic.id_curso, ic.fecha_inicio, ic.fecha_fin, ic.modalidad, " +
        "ic.lugar_url, ic.estado, ic.motivo_cancelacion, ic.created_at, ic.updated_at, " +
        "c.nombre AS nombre_curso, " +
        "(SELECT COUNT(*) FROM instancia_empleado ie WHERE ie.id_instancia = ic.id) AS total_empleados " +
        "FROM instancias_capacitacion ic JOIN cursos c ON ic.id_curso = c.id ";

    public List<InstanciaCapacitacion> listarTodas() throws SQLException {
        return ejecutarLista(SELECT_BASE + "ORDER BY ic.fecha_inicio DESC");
    }

    public List<InstanciaCapacitacion> listarParaAsistencia() throws SQLException {
        // Muestra TODAS las instancias pasadas con empleados asignados (PROGRAMADA o REALIZADA).
        // Calcula si es editable: tiene registros PENDIENTE O el último updated_at fue hace menos de 48hs.
        String sql =
            "SELECT ic.id, ic.id_curso, ic.fecha_inicio, ic.fecha_fin, ic.modalidad, " +
            "ic.lugar_url, ic.estado, ic.motivo_cancelacion, ic.created_at, ic.updated_at, " +
            "c.nombre AS nombre_curso, " +
            "(SELECT COUNT(*) FROM instancia_empleado ie2 WHERE ie2.id_instancia = ic.id) AS total_empleados, " +
            "( " +
            "  EXISTS (SELECT 1 FROM registros_asistencia ra2 " +
            "          WHERE ra2.id_instancia = ic.id AND ra2.estado_asistencia = 'PENDIENTE') " +
            "  OR " +
            "  EXISTS (SELECT 1 FROM registros_asistencia ra3 " +
            "          WHERE ra3.id_instancia = ic.id " +
            "          AND ra3.updated_at > DATE_SUB(NOW(), INTERVAL 48 HOUR)) " +
            "  OR " +
            "  NOT EXISTS (SELECT 1 FROM registros_asistencia ra4 WHERE ra4.id_instancia = ic.id) " +
            ") AS es_editable " +
            "FROM instancias_capacitacion ic JOIN cursos c ON ic.id_curso = c.id " +
            "WHERE ic.fecha_inicio <= NOW() " +
            "AND ic.estado IN ('PROGRAMADA','REALIZADA') " +
            "AND EXISTS (SELECT 1 FROM instancia_empleado ie WHERE ie.id_instancia = ic.id) " +
            "ORDER BY ic.fecha_inicio DESC";

        List<InstanciaCapacitacion> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InstanciaCapacitacion inst = mapear(rs);
                inst.setEditableAsistencia(rs.getBoolean("es_editable"));
                lista.add(inst);
            }
        }
        return lista;
    }

    public InstanciaCapacitacion buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE ic.id = ?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public int insertar(InstanciaCapacitacion inst) throws SQLException {
        String sql = "INSERT INTO instancias_capacitacion " +
                     "(id_curso, fecha_inicio, fecha_fin, modalidad, lugar_url, estado) " +
                     "VALUES (?, ?, ?, ?, ?, 'PROGRAMADA')";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inst.getIdCurso());
            ps.setTimestamp(2, Timestamp.valueOf(inst.getFechaInicio()));
            ps.setTimestamp(3, Timestamp.valueOf(inst.getFechaFin()));
            ps.setString(4, inst.getModalidad().name());
            ps.setString(5, inst.getLugarUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void asignarEmpleado(int idInstancia, int idEmpleado) throws SQLException {
        String sql = "INSERT IGNORE INTO instancia_empleado (id_instancia, id_empleado) VALUES (?, ?)";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstancia);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public void crearRegistroAsistenciaPendiente(int idInstancia, int idEmpleado) throws SQLException {
        String sql = "INSERT IGNORE INTO registros_asistencia " +
                     "(id_instancia, id_empleado, estado_asistencia, resultado) " +
                     "VALUES (?, ?, 'PENDIENTE', 'PENDIENTE_RESULTADO')";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstancia);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
        }
    }

    public List<Empleado> listarEmpleadosAsignados(int idInstancia) throws SQLException {
        String sql = "SELECT e.id, e.nombre, e.apellido, e.dni, e.email, e.puesto, e.area " +
                     "FROM empleados e " +
                     "JOIN instancia_empleado ie ON e.id = ie.id_empleado " +
                     "WHERE ie.id_instancia = ? ORDER BY e.apellido, e.nombre";
        List<Empleado> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idInstancia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Empleado e = new Empleado();
                    e.setId(rs.getInt("id"));
                    e.setNombre(rs.getString("nombre"));
                    e.setApellido(rs.getString("apellido"));
                    e.setDni(rs.getString("dni"));
                    e.setEmail(rs.getString("email"));
                    e.setPuesto(rs.getString("puesto"));
                    e.setArea(rs.getString("area"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    public boolean marcarRealizada(int id) throws SQLException {
        String sql = "UPDATE instancias_capacitacion SET estado='REALIZADA' WHERE id=? AND estado='PROGRAMADA'";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelar(int id, String motivo) throws SQLException {
        String sql = "UPDATE instancias_capacitacion SET estado='CANCELADA', motivo_cancelacion=? WHERE id=?";
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<InstanciaCapacitacion> ejecutarLista(String sql) throws SQLException {
        List<InstanciaCapacitacion> lista = new ArrayList<>();
        Connection con = ConectorBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private InstanciaCapacitacion mapear(ResultSet rs) throws SQLException {
        InstanciaCapacitacion inst = new InstanciaCapacitacion();
        inst.setId(rs.getInt("id"));
        inst.setIdCurso(rs.getInt("id_curso"));
        inst.setNombreCurso(rs.getString("nombre_curso"));
        inst.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        inst.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());
        inst.setModalidad(ModalidadCurso.fromString(rs.getString("modalidad")));
        inst.setLugarUrl(rs.getString("lugar_url"));
        inst.setEstado(EstadoInstancia.fromString(rs.getString("estado")));
        inst.setMotivoCancelacion(rs.getString("motivo_cancelacion"));
        inst.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        inst.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return inst;
    }
}
