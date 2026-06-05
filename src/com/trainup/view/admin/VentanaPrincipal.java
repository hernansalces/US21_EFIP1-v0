package com.trainup.view.admin;

import com.trainup.auth.SesionUsuario;
import com.trainup.enums.RolUsuario;
import com.trainup.view.VentanaLogin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private JPanel panelContenido;
    private JButton btnSeleccionado;

    private static final Color AZUL         = new Color(33, 97, 140);
    private static final Color AZUL_OSCURO  = new Color(22, 68, 100);
    private static final Color SIDEBAR_BG   = new Color(28, 78, 116);
    private static final Color SIDEBAR_HOVER = new Color(22, 60, 92);

    public VentanaPrincipal() {
        initComponentes();
    }

    private void initComponentes() {
        setTitle("TrainUp - Panel de administración");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // ── Barra superior ──
        JPanel barraTop = new JPanel(new BorderLayout());
        barraTop.setBackground(AZUL);
        barraTop.setBorder(new EmptyBorder(10, 20, 10, 20));
        barraTop.setPreferredSize(new Dimension(0, 52));

        JLabel lblTitulo = new JLabel("TrainUp");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JPanel infoUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoUsuario.setOpaque(false);

        String rol = SesionUsuario.getUsuarioActual().getUsername() + " · " +
                     SesionUsuario.getRolActual().name();
        JLabel lblUsuario = new JLabel(rol);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(190, 215, 235));

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setFont(new Font("Arial", Font.PLAIN, 11));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        infoUsuario.add(lblUsuario);
        infoUsuario.add(btnCerrarSesion);
        barraTop.add(lblTitulo, BorderLayout.WEST);
        barraTop.add(infoUsuario, BorderLayout.EAST);

        // ── Sidebar ──
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(185, 0));
        sidebar.setBorder(new EmptyBorder(15, 0, 15, 0));

        boolean esGerente = SesionUsuario.getRolActual() == RolUsuario.GERENTE;
        // Nota: ADMIN = administrador, GERENTE = solo lectura dashboard

        if (!esGerente) {
            agregarItemMenu(sidebar, "Empleados",    () -> mostrarPanel(new PanelEmpleados()));
            agregarItemMenu(sidebar, "Perfiles",     () -> mostrarPanel(new PanelPerfiles()));
            agregarItemMenu(sidebar, "Cursos",       () -> mostrarPanel(new PanelCursos()));
            agregarItemMenu(sidebar, "Programacion", () -> mostrarPanel(new PanelProgramacion()));
            agregarItemMenu(sidebar, "Asistencia",   () -> mostrarPanel(new PanelAsistencia()));
            sidebar.add(Box.createVerticalStrut(10));
            agregarSeparador(sidebar);
        }
        agregarItemMenu(sidebar, "Dashboard",   () -> mostrarPanel(new PanelDashboard()));
        agregarItemMenu(sidebar, "Reportes",    () -> mostrarPanel(new PanelReportes()));
        agregarItemMenu(sidebar, "Alertas",     () -> mostrarPanel(new PanelAlertas()));

        // ── Panel de contenido ──
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(Color.WHITE);

        // Vista inicial: dashboard si es gerente, empleados si es admin
        if (esGerente) {
            mostrarPanel(new PanelDashboard());
        } else {
            mostrarPanel(new PanelEmpleados());
        }

        // ── Layout general ──
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.add(sidebar, BorderLayout.WEST);
        cuerpo.add(panelContenido, BorderLayout.CENTER);

        add(barraTop, BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
    }

    private void agregarItemMenu(JPanel sidebar, String texto, Runnable accion) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setForeground(new Color(210, 230, 245));
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

        btn.addActionListener(e -> {
            if (btnSeleccionado != null) {
                btnSeleccionado.setBackground(SIDEBAR_BG);
                btnSeleccionado.setForeground(new Color(210, 230, 245));
            }
            btnSeleccionado = btn;
            btn.setBackground(AZUL_OSCURO);
            btn.setForeground(Color.WHITE);
            accion.run();
        });

        sidebar.add(btn);
    }

    private void agregarSeparador(JPanel sidebar) {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 100, 140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private void mostrarPanel(JPanel panel) {
        panelContenido.removeAll();
        panelContenido.add(panel, BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPanelProximo(String nombre) {
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(new Color(248, 250, 253));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);

        JLabel icono = new JLabel("🚧");
        icono.setFont(new Font("Arial", Font.PLAIN, 40));
        placeholder.add(icono, gbc);

        gbc.gridy = 1;
        JLabel lbl = new JLabel("Módulo: " + nombre);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setForeground(new Color(80, 100, 130));
        placeholder.add(lbl, gbc);

        gbc.gridy = 2;
        JLabel sub = new JLabel("Se implementa en la próxima fase.");
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(new Color(140, 150, 160));
        placeholder.add(sub, gbc);

        mostrarPanel(placeholder);
    }

    private void cerrarSesion() {
        SesionUsuario.cerrar();
        dispose();
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
