package com.trainup.view.portal;

import com.trainup.auth.SesionUsuario;
import com.trainup.controller.CtrlPortalEmpleado;
import com.trainup.enums.CategoriaEngagement;
import com.trainup.model.HistorialEngagement;
import com.trainup.model.IndiceEngagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelMiIE extends JPanel {

    private final CtrlPortalEmpleado ctrl;
    private final int idEmpleado;

    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color AMARILLO = new Color(190, 140, 0);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color GRIS    = new Color(150, 150, 150);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelMiIE(CtrlPortalEmpleado ctrl) {
        this.ctrl       = ctrl;
        this.idEmpleado = SesionUsuario.getIdEmpleadoActual();
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        removeAll();
        try {
            IndiceEngagement ie = ctrl.obtenerIndice(idEmpleado);
            if (ie == null) {
                add(sinDatos(), BorderLayout.CENTER);
                return;
            }

            Color colorIE = colorCategoria(ie.getCategoria());

            // ── Título ──
            JLabel lblTitulo = new JLabel("Mi Indice de Engagement");
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
            lblTitulo.setForeground(AZUL);

            // ── Panel principal IE ──
            JPanel panelIE = new JPanel(new BorderLayout(24, 0));
            panelIE.setBackground(new Color(248, 250, 254));
            panelIE.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 230)),
                new EmptyBorder(16, 20, 16, 20)));

            // Valor grande
            JPanel panelValor = new JPanel(new GridBagLayout());
            panelValor.setOpaque(false);
            panelValor.setPreferredSize(new Dimension(170, 0));

            JLabel lblValor = new JLabel(ie.getValorIe().toString());
            lblValor.setFont(new Font("Arial", Font.BOLD, 52));
            lblValor.setForeground(colorIE);

            JLabel lblCategoria = new JLabel(ie.getCategoria().name());
            lblCategoria.setFont(new Font("Arial", Font.BOLD, 16));
            lblCategoria.setForeground(colorIE);
            lblCategoria.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lblFecha = new JLabel(ie.getFechaCalculo() != null
                ? "Calculado: " + ie.getFechaCalculo().format(FMT) : "");
            lblFecha.setFont(new Font("Arial", Font.PLAIN, 10));
            lblFecha.setForeground(GRIS);

            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0; g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
            panelValor.add(lblValor, g);
            g.gridy = 1;
            panelValor.add(lblCategoria, g);
            g.gridy = 2; g.insets = new Insets(6, 0, 0, 0);
            panelValor.add(lblFecha, g);

            // Componentes PA / PR / PP
            JPanel panelComponentes = new JPanel(new GridBagLayout());
            panelComponentes.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(5, 4, 5, 4);

            agregarComponente(panelComponentes, gc, 0, "Asistencia (PA)",
                ie.getPa(), 50, "peso 50%");
            agregarComponente(panelComponentes, gc, 1, "Rendimiento (PR)",
                ie.getPr(), 30, "peso 30%");
            agregarComponente(panelComponentes, gc, 2, "Progreso en perfil (PP)",
                ie.getPp(), 20, "peso 20%");

            if (ie.isEsProvisional()) {
                gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 3;
                JLabel prov = new JLabel("Indice provisional: hay resultados pendientes de confirmacion.");
                prov.setFont(new Font("Arial", Font.ITALIC, 11));
                prov.setForeground(AMARILLO);
                panelComponentes.add(prov, gc);
            }

            panelIE.add(panelValor,       BorderLayout.WEST);
            panelIE.add(panelComponentes, BorderLayout.CENTER);
            panelIE.add(mensajeMotivacional(ie.getCategoria()), BorderLayout.EAST);

            // ── Historial ──
            JPanel panelHistorial = new JPanel(new BorderLayout(0, 6));
            panelHistorial.setBackground(Color.WHITE);

            JLabel lblHist = new JLabel("Evolucion historica");
            lblHist.setFont(new Font("Arial", Font.BOLD, 14));
            lblHist.setForeground(AZUL);

            List<HistorialEngagement> historial = ctrl.obtenerHistorialIE(idEmpleado);
            JPanel filaHist = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            filaHist.setBackground(Color.WHITE);

            if (historial.isEmpty()) {
                filaHist.add(new JLabel("Sin historial previo."));
            } else {
                for (int i = 0; i < Math.min(historial.size(), 12); i++) {
                    HistorialEngagement h = historial.get(i);
                    filaHist.add(chipHistorial(h));
                }
            }

            JScrollPane scrollHist = new JScrollPane(filaHist,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollHist.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 228)));
            scrollHist.setPreferredSize(new Dimension(0, 72));

            panelHistorial.add(lblHist,     BorderLayout.NORTH);
            panelHistorial.add(scrollHist,  BorderLayout.CENTER);

            JPanel central = new JPanel(new BorderLayout(0, 12));
            central.setBackground(Color.WHITE);
            central.add(panelIE,       BorderLayout.NORTH);
            central.add(panelHistorial, BorderLayout.CENTER);

            add(lblTitulo, BorderLayout.NORTH);
            add(central,   BorderLayout.CENTER);

        } catch (SQLException e) {
            JLabel err = new JLabel("Error al cargar IE: " + e.getMessage());
            err.setForeground(ROJO);
            add(err);
        }
        revalidate(); repaint();
    }

    private void agregarComponente(JPanel p, GridBagConstraints gc,
                                    int fila, String label, BigDecimal valor, int peso, String pesoLabel) {
        Color color = colorPorValor(valor);

        gc.gridx = 0; gc.gridy = fila; gc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        p.add(lbl, gc);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(valor != null ? valor.intValue() : 0);
        bar.setStringPainted(false);
        bar.setForeground(color);
        bar.setBackground(new Color(220, 228, 238));
        bar.setPreferredSize(new Dimension(0, 12));
        bar.setBorder(null);
        gc.gridx = 1; gc.gridy = fila; gc.weightx = 0.45;
        p.add(bar, gc);

        JLabel lblVal = new JLabel(String.format("%.1f%%  (%s)", valor != null ? valor : BigDecimal.ZERO, pesoLabel));
        lblVal.setFont(new Font("Arial", Font.BOLD, 11));
        lblVal.setForeground(color);
        gc.gridx = 2; gc.gridy = fila; gc.weightx = 0.20;
        p.add(lblVal, gc);
    }

    private JPanel mensajeMotivacional(CategoriaEngagement cat) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(160, 0));
        p.setBorder(new EmptyBorder(0, 12, 0, 0));

        String[] msg = switch (cat) {
            case ALTO   -> new String[]{"Excelente!", "Segui asi, tu compromiso\nes muy valorado."};
            case MEDIO  -> new String[]{"Vas bien!", "Podes mejorar asistiendo\na mas capacitaciones."};
            case CRITICO -> new String[]{"Atencion!", "Tu nivel de compromiso\nnecesita mejora urgente."};
        };

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0, 0, 6, 0);
        JLabel emoji = new JLabel(cat == CategoriaEngagement.ALTO ? "🌟"
            : cat == CategoriaEngagement.MEDIO ? "📈" : "⚠");
        emoji.setFont(new Font("Arial", Font.PLAIN, 28));
        p.add(emoji, gc);

        gc.gridy = 1;
        JLabel titulo = new JLabel(msg[0]);
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        titulo.setForeground(colorCategoria(cat));
        p.add(titulo, gc);

        gc.gridy = 2;
        JLabel texto = new JLabel("<html><center>" + msg[1].replace("\n","<br>") + "</center></html>");
        texto.setFont(new Font("Arial", Font.PLAIN, 11));
        texto.setForeground(GRIS);
        p.add(texto, gc);

        return p;
    }

    private JPanel chipHistorial(HistorialEngagement h) {
        JPanel chip = new JPanel(new BorderLayout(2, 0));
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 230)),
            new EmptyBorder(4, 8, 4, 8)));
        chip.setBackground(new Color(245, 248, 254));

        JLabel val = new JLabel(h.getValorIe().toString());
        val.setFont(new Font("Arial", Font.BOLD, 13));
        val.setForeground(colorCategoria(h.getCategoria()));

        JLabel fecha = new JLabel(h.getFechaCalculo().format(DateTimeFormatter.ofPattern("dd/MM")));
        fecha.setFont(new Font("Arial", Font.PLAIN, 9));
        fecha.setForeground(GRIS);
        fecha.setHorizontalAlignment(SwingConstants.CENTER);

        chip.add(val,   BorderLayout.CENTER);
        chip.add(fecha, BorderLayout.SOUTH);
        return chip;
    }

    private JPanel sinDatos() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("<html><center>Tu indice de engagement aun no fue calculado.<br>" +
            "Se calculara automaticamente cuando tengas capacitaciones registradas.</center></html>");
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(GRIS);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl);
        return p;
    }

    private Color colorCategoria(CategoriaEngagement cat) {
        return switch (cat) {
            case ALTO    -> VERDE;
            case MEDIO   -> AMARILLO;
            case CRITICO -> ROJO;
        };
    }

    private Color colorPorValor(BigDecimal val) {
        if (val == null) return GRIS;
        int v = val.intValue();
        if (v >= 75) return VERDE;
        if (v >= 50) return AMARILLO;
        return ROJO;
    }
}
