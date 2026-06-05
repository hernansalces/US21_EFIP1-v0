package com.trainup.view.portal;

import com.trainup.auth.SesionUsuario;
import com.trainup.controller.CtrlPortalEmpleado;
import com.trainup.view.VentanaLogin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PortalEmpleado extends JFrame {

    private final CtrlPortalEmpleado ctrl = new CtrlPortalEmpleado();

    private JPanel panelContenido;
    private JButton btnSeleccionado;
    private JButton btnNotificaciones;

    private static final Color VERDE        = new Color(39, 120, 70);
    private static final Color VERDE_OSCURO = new Color(28, 90, 52);
    private static final Color SIDEBAR_BG   = new Color(33, 100, 58);
    private static final Color SIDEBAR_HOVER = new Color(25, 78, 45);

    public PortalEmpleado() {
        initComponentes();
    }

    private void initComponentes() {
        String usuario = SesionUsuario.getUsuarioActual().getUsername();

        setTitle("TrainUp - Portal del Empleado");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);

        // ── Barra superior ──
        JPanel barraTop = new JPanel(new BorderLayout());
        barraTop.setBackground(VERDE);
        barraTop.setBorder(new EmptyBorder(10, 20, 10, 20));
        barraTop.setPreferredSize(new Dimension(0, 52));

        JLabel lblTitulo = new JLabel("TrainUp   Mi Portal");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JPanel infoUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoUsuario.setOpaque(false);
        JLabel lblUsuario = new JLabel(usuario);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(200, 240, 210));
        JButton btnLogout = new JButton("Cerrar sesion");
        btnLogout.setFont(new Font("Arial", Font.PLAIN, 11));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> cerrarSesion());
        infoUsuario.add(lblUsuario);
        infoUsuario.add(btnLogout);

        barraTop.add(lblTitulo,   BorderLayout.WEST);
        barraTop.add(infoUsuario, BorderLayout.EAST);

        // ── Sidebar ──
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(185, 0));
        sidebar.setBorder(new EmptyBorder(15, 0, 15, 0));

        agregarItemMenu(sidebar, "Mi Plan de Carrera",
            () -> mostrarPanel(new PanelMiPlanCarrera(ctrl)));
        agregarItemMenu(sidebar, "Mi Indice de Engagement",
            () -> mostrarPanel(new PanelMiIE(ctrl)));
        agregarItemMenu(sidebar, "Mis Capacitaciones",
            () -> mostrarPanel(new PanelMisCapacitaciones(ctrl)));

        // Notificaciones con badge
        btnNotificaciones = crearBtnMenu("Notificaciones");
        actualizarBadge();
        btnNotificaciones.addActionListener(e -> {
            if (btnSeleccionado != null) {
                btnSeleccionado.setBackground(SIDEBAR_BG);
                btnSeleccionado.setForeground(new Color(210, 240, 220));
            }
            btnSeleccionado = btnNotificaciones;
            btnNotificaciones.setBackground(VERDE_OSCURO);
            btnNotificaciones.setForeground(Color.WHITE);
            PanelMisNotificaciones panel = new PanelMisNotificaciones(ctrl, this::actualizarBadge);
            mostrarPanel(panel);
        });
        sidebar.add(btnNotificaciones);

        // ── Panel de contenido ──
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(Color.WHITE);

        // Vista inicial: Plan de carrera
        mostrarPanel(new PanelMiPlanCarrera(ctrl));

        // ── Layout ──
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.add(sidebar,        BorderLayout.WEST);
        cuerpo.add(panelContenido, BorderLayout.CENTER);

        add(barraTop, BorderLayout.NORTH);
        add(cuerpo,   BorderLayout.CENTER);
    }

    private void agregarItemMenu(JPanel sidebar, String texto, Runnable accion) {
        JButton btn = crearBtnMenu(texto);
        btn.addActionListener(e -> {
            if (btnSeleccionado != null) {
                btnSeleccionado.setBackground(SIDEBAR_BG);
                btnSeleccionado.setForeground(new Color(210, 240, 220));
            }
            btnSeleccionado = btn;
            btn.setBackground(VERDE_OSCURO);
            btn.setForeground(Color.WHITE);
            accion.run();
        });
        sidebar.add(btn);
    }

    private JButton crearBtnMenu(String texto) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setForeground(new Color(210, 240, 220));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 20, 8, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != btnSeleccionado) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != btnSeleccionado) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    private void mostrarPanel(JPanel panel) {
        panelContenido.removeAll();
        panelContenido.add(panel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void actualizarBadge() {
        try {
            int noLeidas = ctrl.contarNoLeidas(SesionUsuario.getIdEmpleadoActual());
            String texto = noLeidas > 0
                ? "Notificaciones (" + noLeidas + ")"
                : "Notificaciones";
            btnNotificaciones.setText(texto);
            btnNotificaciones.setForeground(noLeidas > 0
                ? new Color(255, 200, 100) : new Color(210, 240, 220));
        } catch (Exception ignored) {
            btnNotificaciones.setText("Notificaciones");
        }
    }

    private void cerrarSesion() {
        SesionUsuario.cerrar();
        dispose();
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
