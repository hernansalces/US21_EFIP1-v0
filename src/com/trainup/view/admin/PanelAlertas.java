package com.trainup.view.admin;

import com.trainup.controller.CtrlAlerta;
import com.trainup.enums.TipoAlerta;
import com.trainup.model.Alerta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelAlertas extends JPanel {

    private final CtrlAlerta ctrl = new CtrlAlerta();

    private JPanel panelLista;
    private JComboBox<String> cboFiltro;
    private JLabel lblConteo;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color NARANJA = new Color(200, 100, 0);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color GRIS    = new Color(150, 150, 150);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelAlertas() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargar();
    }

    private void initComponentes() {
        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblTitulo = new JLabel("Panel de Alertas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setBackground(Color.WHITE);

        derecha.add(new JLabel("Filtrar:"));
        cboFiltro = new JComboBox<>(new String[]{
            "Todas", "CRITICO", "ADVERTENCIA", "INFORMATIVO", "URGENTE", "RECORDATORIO"});
        cboFiltro.setFont(new Font("Arial", Font.PLAIN, 12));
        cboFiltro.addActionListener(e -> cargar());
        derecha.add(cboFiltro);

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRefrescar.addActionListener(e -> cargar());
        derecha.add(btnRefrescar);

        lblConteo = new JLabel();
        lblConteo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblConteo.setForeground(GRIS);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(derecha,   BorderLayout.EAST);
        header.add(lblConteo, BorderLayout.SOUTH);

        // ── Lista de alertas ──
        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Alertas activas", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void cargar() {
        panelLista.removeAll();
        try {
            List<Alerta> alertas = ctrl.obtenerActivasAdmin();
            String filtro = (String) cboFiltro.getSelectedItem();

            List<Alerta> filtradas = alertas.stream()
                .filter(a -> "Todas".equals(filtro) || a.getTipo().name().equals(filtro))
                .toList();

            if (filtradas.isEmpty()) {
                JPanel vacio = new JPanel(new GridBagLayout());
                vacio.setBackground(Color.WHITE);
                vacio.setPreferredSize(new Dimension(0, 120));
                JLabel lbl = new JLabel(alertas.isEmpty()
                    ? "No hay alertas activas. El equipo tiene un buen nivel de engagement."
                    : "No hay alertas del tipo '" + filtro + "'.");
                lbl.setFont(new Font("Arial", Font.PLAIN, 13));
                lbl.setForeground(alertas.isEmpty() ? VERDE : GRIS);
                vacio.add(lbl);
                panelLista.add(vacio);
            } else {
                for (Alerta a : filtradas) {
                    panelLista.add(cardAlerta(a));
                    panelLista.add(Box.createVerticalStrut(6));
                }
            }

            lblConteo.setText(filtradas.size() + " de " + alertas.size() + " alertas activas");
            lblConteo.setForeground(alertas.stream()
                .anyMatch(a -> a.getTipo() == TipoAlerta.CRITICO || a.getTipo() == TipoAlerta.URGENTE)
                ? ROJO : GRIS);

        } catch (SQLException e) {
            panelLista.add(new JLabel("Error al cargar alertas: " + e.getMessage()));
        }
        panelLista.revalidate();
        panelLista.repaint();
    }

    private JPanel cardAlerta(Alerta a) {
        Color accentColor = colorTipo(a.getTipo());

        JPanel card = new JPanel(new BorderLayout(12, 4));
        card.setBackground(new Color(252, 252, 254));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 5, 0, 0, accentColor),
            new EmptyBorder(10, 14, 10, 14)));

        // Badge tipo + fecha
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

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(badge);
        topRow.add(lblFecha);

        // Empleado
        JLabel lblEmpleado = new JLabel("Empleado: " + a.getNombreEmpleado());
        lblEmpleado.setFont(new Font("Arial", Font.BOLD, 12));

        // Mensaje
        JLabel lblMensaje = new JLabel("<html>" + a.getMensaje() + "</html>");
        lblMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMensaje.setForeground(new Color(50, 50, 50));

        JPanel izq = new JPanel(new GridLayout(3, 1, 0, 3));
        izq.setOpaque(false);
        izq.add(topRow);
        izq.add(lblEmpleado);
        izq.add(lblMensaje);

        // Botón marcar revisada
        JButton btnRevisar = new JButton("Marcar revisada");
        btnRevisar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRevisar.setFocusPainted(false);
        btnRevisar.setMargin(new Insets(4, 8, 4, 8));
        btnRevisar.addActionListener(e -> marcarRevisada(a.getId()));

        JPanel der = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        der.setOpaque(false);
        der.add(btnRevisar);

        card.add(izq, BorderLayout.CENTER);
        card.add(der, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(card);
        return wrapper;
    }

    private void marcarRevisada(int idAlerta) {
        try {
            ctrl.marcarLeidaAdmin(idAlerta);
            cargar();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al marcar alerta: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color colorTipo(TipoAlerta tipo) {
        return switch (tipo) {
            case CRITICO, URGENTE          -> ROJO;
            case ADVERTENCIA, RECORDATORIO -> NARANJA;
            default                        -> AZUL;
        };
    }
}
