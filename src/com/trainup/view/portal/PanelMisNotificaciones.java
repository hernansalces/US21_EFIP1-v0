package com.trainup.view.portal;

import com.trainup.auth.SesionUsuario;
import com.trainup.controller.CtrlPortalEmpleado;
import com.trainup.enums.TipoAlerta;
import com.trainup.model.Alerta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelMisNotificaciones extends JPanel {

    private final CtrlPortalEmpleado ctrl;
    private final int idEmpleado;
    private final Runnable onContadorCambia;   // callback para actualizar el badge

    private JPanel panelLista;
    private JLabel lblConteo;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color NARANJA = new Color(200, 100, 0);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color GRIS    = new Color(150, 150, 150);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelMisNotificaciones(CtrlPortalEmpleado ctrl, Runnable onContadorCambia) {
        this.ctrl              = ctrl;
        this.idEmpleado        = SesionUsuario.getIdEmpleadoActual();
        this.onContadorCambia  = onContadorCambia;
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JLabel lblTitulo = new JLabel("Mis Notificaciones");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL);

        lblConteo = new JLabel();
        lblConteo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblConteo.setForeground(GRIS);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblConteo,  BorderLayout.EAST);

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        cargarNotificaciones();
    }

    public void cargarNotificaciones() {
        panelLista.removeAll();
        try {
            List<Alerta> alertas = ctrl.obtenerAlertasActivas(idEmpleado);

            if (alertas.isEmpty()) {
                JPanel vacio = new JPanel(new GridBagLayout());
                vacio.setBackground(Color.WHITE);
                vacio.setPreferredSize(new Dimension(0, 120));
                JLabel lbl = new JLabel("Todo en orden. No tenes notificaciones pendientes.");
                lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                lbl.setForeground(VERDE);
                vacio.add(lbl);
                panelLista.add(vacio);
                lblConteo.setText("Sin notificaciones");
            } else {
                lblConteo.setText(alertas.size() + " no leida" + (alertas.size() != 1 ? "s" : ""));
                for (Alerta a : alertas) {
                    panelLista.add(cardNotificacion(a));
                    panelLista.add(Box.createVerticalStrut(6));
                }
            }
        } catch (SQLException e) {
            panelLista.add(new JLabel("Error al cargar notificaciones: " + e.getMessage()));
        }
        panelLista.revalidate();
        panelLista.repaint();
    }

    private JPanel cardNotificacion(Alerta a) {
        Color accentColor = colorTipo(a.getTipo());

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(new Color(250, 250, 252));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 5, 0, 0, accentColor),
            new EmptyBorder(10, 14, 10, 14)));

        // Badge tipo
        JLabel badge = new JLabel(a.getTipo().name());
        badge.setFont(new Font("Arial", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(accentColor);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel lblFecha = new JLabel(a.getFechaGeneracion() != null
            ? a.getFechaGeneracion().format(FMT) : "");
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 10));
        lblFecha.setForeground(GRIS);

        JPanel topIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topIzq.setOpaque(false);
        topIzq.add(badge);
        topIzq.add(lblFecha);

        JLabel lblMensaje = new JLabel("<html>" + a.getMensaje() + "</html>");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMensaje.setForeground(new Color(50, 50, 50));

        JPanel izq = new JPanel(new BorderLayout(0, 4));
        izq.setOpaque(false);
        izq.add(topIzq,     BorderLayout.NORTH);
        izq.add(lblMensaje, BorderLayout.CENTER);

        JButton btnLeida = new JButton("Marcar leida");
        btnLeida.setFont(new Font("Arial", Font.PLAIN, 11));
        btnLeida.setFocusPainted(false);
        btnLeida.setMargin(new Insets(3, 8, 3, 8));
        btnLeida.addActionListener(e -> marcarLeida(a.getId()));

        card.add(izq,      BorderLayout.CENTER);
        card.add(btnLeida, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(card);
        return wrapper;
    }

    private void marcarLeida(int idAlerta) {
        try {
            ctrl.marcarLeidaEmpleado(idAlerta);
            cargarNotificaciones();
            if (onContadorCambia != null) onContadorCambia.run();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al marcar la notificacion: " + e.getMessage());
        }
    }

    private Color colorTipo(TipoAlerta tipo) {
        return switch (tipo) {
            case CRITICO, URGENTE       -> ROJO;
            case ADVERTENCIA, RECORDATORIO -> NARANJA;
            default                     -> AZUL;
        };
    }
}
