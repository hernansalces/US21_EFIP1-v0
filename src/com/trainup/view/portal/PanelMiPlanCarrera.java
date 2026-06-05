package com.trainup.view.portal;

import com.trainup.auth.SesionUsuario;
import com.trainup.controller.CtrlPortalEmpleado;
import com.trainup.controller.CtrlPortalEmpleado.CursoConEstado;
import com.trainup.enums.NivelExpertise;
import com.trainup.model.PerfilCarrera;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelMiPlanCarrera extends JPanel {

    private final CtrlPortalEmpleado ctrl;
    private final int idEmpleado;
    private final Integer idPerfil;

    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color GRIS    = new Color(150, 150, 150);
    private static final Color NARANJA = new Color(200, 100, 0);

    public PanelMiPlanCarrera(CtrlPortalEmpleado ctrl) {
        this.ctrl       = ctrl;
        this.idEmpleado = SesionUsuario.getIdEmpleadoActual();
        this.idPerfil   = SesionUsuario.getUsuarioActual() != null
            ? obtenerIdPerfil() : null;
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        construir();
    }

    private Integer obtenerIdPerfil() {
        try {
            java.sql.Connection con = com.trainup.util.ConectorBD.getInstancia().getConexion();
            try (java.sql.PreparedStatement ps = con.prepareStatement(
                    "SELECT id_perfil FROM empleados WHERE id = ?")) {
                ps.setInt(1, idEmpleado);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int val = rs.getInt(1);
                        return rs.wasNull() ? null : val;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void construir() {
        removeAll();
        if (idPerfil == null) {
            add(sinPerfil(), BorderLayout.CENTER);
            return;
        }
        try {
            PerfilCarrera perfil = ctrl.obtenerPerfil(idPerfil);
            List<CursoConEstado> cursos = ctrl.obtenerCursosConEstado(idPerfil, idEmpleado);

            long obligatorios  = cursos.stream().filter(CursoConEstado::isEsObligatorio).count();
            long completados   = cursos.stream().filter(c -> c.isEsObligatorio() && c.isCompletado()).count();
            int  pct           = obligatorios > 0 ? (int)(completados * 100 / obligatorios) : 0;

            // ── Encabezado ──
            JPanel header = new JPanel(new BorderLayout(0, 6));
            header.setBackground(Color.WHITE);

            JLabel lblTitulo = new JLabel("Mi Plan de Carrera");
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
            lblTitulo.setForeground(AZUL);

            JLabel lblPerfil = new JLabel("Perfil: " + perfil.getNombre());
            lblPerfil.setFont(new Font("Arial", Font.PLAIN, 13));
            lblPerfil.setForeground(new Color(80, 80, 80));

            if (perfil.getDescripcion() != null && !perfil.getDescripcion().isBlank()) {
                JLabel lblDesc = new JLabel(perfil.getDescripcion());
                lblDesc.setFont(new Font("Arial", Font.ITALIC, 12));
                lblDesc.setForeground(GRIS);
                header.add(lblDesc, BorderLayout.SOUTH);
            }
            header.add(lblTitulo, BorderLayout.NORTH);
            header.add(lblPerfil, BorderLayout.CENTER);

            // ── Barra de progreso ──
            JPanel panelProgreso = new JPanel(new BorderLayout(8, 0));
            panelProgreso.setBackground(new Color(245, 248, 252));
            panelProgreso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 230)),
                new EmptyBorder(10, 14, 10, 14)));

            JLabel lblPct = new JLabel(pct + "%  completado");
            lblPct.setFont(new Font("Arial", Font.BOLD, 14));
            lblPct.setForeground(pct == 100 ? VERDE : AZUL);

            JProgressBar barra = new JProgressBar(0, 100);
            barra.setValue(pct);
            barra.setStringPainted(false);
            barra.setForeground(pct == 100 ? VERDE : AZUL);
            barra.setBackground(new Color(220, 228, 238));
            barra.setPreferredSize(new Dimension(0, 14));
            barra.setBorder(null);

            JLabel lblDetalle = new JLabel(completados + " de " + obligatorios + " cursos obligatorios completados");
            lblDetalle.setFont(new Font("Arial", Font.PLAIN, 11));
            lblDetalle.setForeground(GRIS);

            JPanel textoProg = new JPanel(new BorderLayout(0, 4));
            textoProg.setOpaque(false);
            textoProg.add(lblPct,    BorderLayout.NORTH);
            textoProg.add(barra,     BorderLayout.CENTER);
            textoProg.add(lblDetalle, BorderLayout.SOUTH);
            panelProgreso.add(textoProg, BorderLayout.CENTER);

            // ── Lista de cursos ──
            JPanel listaCursos = new JPanel();
            listaCursos.setLayout(new BoxLayout(listaCursos, BoxLayout.Y_AXIS));
            listaCursos.setBackground(Color.WHITE);

            NivelExpertise nivelActual = null;
            for (CursoConEstado c : cursos) {
                if (!c.getNivelExpertise().equals(nivelActual)) {
                    nivelActual = c.getNivelExpertise();
                    listaCursos.add(separadorNivel(nivelActual.name()));
                }
                listaCursos.add(filaCurso(c));
            }

            JScrollPane scroll = new JScrollPane(listaCursos);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 228)));
            scroll.getVerticalScrollBar().setUnitIncrement(16);

            add(header,        BorderLayout.NORTH);
            add(panelProgreso, BorderLayout.CENTER);
            add(scroll,        BorderLayout.SOUTH);
            // Ajustar para que la lista ocupe el espacio disponible
            setLayout(new BorderLayout(0, 12));
            removeAll();
            add(header,       BorderLayout.NORTH);

            JPanel central = new JPanel(new BorderLayout(0, 10));
            central.setBackground(Color.WHITE);
            central.add(panelProgreso, BorderLayout.NORTH);
            central.add(scroll,        BorderLayout.CENTER);
            add(central, BorderLayout.CENTER);

        } catch (SQLException e) {
            add(error(e.getMessage()), BorderLayout.CENTER);
        }
        revalidate(); repaint();
    }

    private JPanel separadorNivel(String nivel) {
        JPanel sep = new JPanel(new BorderLayout());
        sep.setBackground(new Color(235, 240, 250));
        sep.setBorder(new EmptyBorder(5, 12, 5, 12));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lbl = new JLabel("Nivel: " + nivel);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(AZUL);
        sep.add(lbl);
        return sep;
    }

    private JPanel filaCurso(CursoConEstado c) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(Color.WHITE);
        fila.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(235, 238, 242)),
            new EmptyBorder(8, 14, 8, 14)));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Estado visual
        String estadoTxt = c.isCompletado() ? "Completado" : "Pendiente";
        Color  estadoColor = c.isCompletado() ? VERDE : (c.isEsObligatorio() ? NARANJA : GRIS);
        JLabel lblEstado = new JLabel(estadoTxt);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 11));
        lblEstado.setForeground(estadoColor);
        lblEstado.setHorizontalAlignment(SwingConstants.RIGHT);

        // Nombre + tipo
        JLabel lblNombre = new JLabel(c.getNombreCurso());
        lblNombre.setFont(new Font("Arial", Font.PLAIN, 13));

        String tipoBadge = c.isEsObligatorio() ? "Obligatorio" : "Opcional";
        JLabel lblTipo = new JLabel(tipoBadge);
        lblTipo.setFont(new Font("Arial", Font.BOLD, 10));
        lblTipo.setForeground(c.isEsObligatorio() ? NARANJA : GRIS);

        JPanel izq = new JPanel(new BorderLayout(4, 0));
        izq.setOpaque(false);
        izq.add(lblNombre, BorderLayout.CENTER);
        izq.add(lblTipo,   BorderLayout.EAST);

        fila.add(izq,       BorderLayout.CENTER);
        fila.add(lblEstado, BorderLayout.EAST);
        return fila;
    }

    private JPanel sinPerfil() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("<html><center>Aun no tenes un plan de carrera asignado.<br>" +
            "Consulta con tu responsable de RRHH.</center></html>");
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(GRIS);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl);
        return p;
    }

    private JPanel error(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("Error al cargar el plan de carrera: " + msg);
        lbl.setForeground(Color.RED);
        p.add(lbl);
        return p;
    }
}
