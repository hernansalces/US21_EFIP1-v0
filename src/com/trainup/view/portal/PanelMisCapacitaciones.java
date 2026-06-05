package com.trainup.view.portal;

import com.trainup.auth.SesionUsuario;
import com.trainup.controller.CtrlPortalEmpleado;
import com.trainup.controller.CtrlPortalEmpleado.RegistroConInstancia;
import com.trainup.enums.EstadoAsistencia;
import com.trainup.enums.ResultadoCurso;
import com.trainup.model.InstanciaCapacitacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelMisCapacitaciones extends JPanel {

    private final CtrlPortalEmpleado ctrl;
    private final int idEmpleado;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color NARANJA = new Color(200, 100, 0);
    private static final Color GRIS    = new Color(150, 150, 150);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_CORTO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PanelMisCapacitaciones(CtrlPortalEmpleado ctrl) {
        this.ctrl       = ctrl;
        this.idEmpleado = SesionUsuario.getIdEmpleadoActual();
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        construir();
    }

    private void construir() {
        JLabel lblTitulo = new JLabel("Mis Capacitaciones");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));

        try {
            // Tab Proximas
            List<InstanciaCapacitacion> proximas = ctrl.obtenerProximas(idEmpleado);
            tabs.addTab("Proximas (" + proximas.size() + ")", construirProximas(proximas));

            // Tab Historial
            List<RegistroConInstancia> historial = ctrl.obtenerHistorialCapacitaciones(idEmpleado);
            tabs.addTab("Historial (" + historial.size() + ")", construirHistorial(historial));

        } catch (SQLException e) {
            tabs.addTab("Error", new JLabel("Error al cargar: " + e.getMessage()));
        }

        add(lblTitulo, BorderLayout.NORTH);
        add(tabs,      BorderLayout.CENTER);
    }

    private JPanel construirProximas(List<InstanciaCapacitacion> lista) {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(Color.WHITE);

        if (lista.isEmpty()) {
            JPanel vacio = new JPanel(new GridBagLayout());
            vacio.setBackground(Color.WHITE);
            JLabel lbl = new JLabel("No tenes capacitaciones programadas proximas.");
            lbl.setFont(new Font("Arial", Font.PLAIN, 13));
            lbl.setForeground(GRIS);
            vacio.add(lbl);
            return vacio;
        }

        LocalDateTime ahora   = LocalDateTime.now();
        LocalDateTime en7dias = ahora.plusDays(7);

        for (InstanciaCapacitacion inst : lista) {
            boolean urgente = inst.getFechaInicio().isBefore(en7dias);
            contenedor.add(cardProxima(inst, urgente));
        }

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scroll);
        return wrapper;
    }

    private JPanel cardProxima(InstanciaCapacitacion inst, boolean urgente) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(urgente ? new Color(255, 250, 235) : new Color(245, 249, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 4, 0, 0, urgente ? NARANJA : AZUL),
            new EmptyBorder(12, 14, 12, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblNombre = new JLabel(inst.getNombreCurso());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblFecha = new JLabel(inst.getFechaInicio().format(FMT)
            + " al " + inst.getFechaFin().format(FMT));
        lblFecha.setFont(new Font("Arial", Font.PLAIN, 12));
        lblFecha.setForeground(new Color(80, 80, 80));

        String lugar = inst.getLugarUrl() != null && !inst.getLugarUrl().isBlank()
            ? inst.getLugarUrl() : "-";
        JLabel lblLugar = new JLabel("Lugar/URL: " + lugar + "  |  " + inst.getModalidad().name());
        lblLugar.setFont(new Font("Arial", Font.PLAIN, 11));
        lblLugar.setForeground(GRIS);

        JPanel izq = new JPanel(new GridLayout(3, 1, 0, 2));
        izq.setOpaque(false);
        izq.add(lblNombre);
        izq.add(lblFecha);
        izq.add(lblLugar);

        if (urgente) {
            JLabel badge = new JLabel("En menos de 7 dias");
            badge.setFont(new Font("Arial", Font.BOLD, 10));
            badge.setForeground(NARANJA);
            badge.setHorizontalAlignment(SwingConstants.RIGHT);
            card.add(badge, BorderLayout.EAST);
        }

        card.add(izq, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(0, 0, 6, 0));
        wrapper.add(card);
        return wrapper;
    }

    private JPanel construirHistorial(List<RegistroConInstancia> lista) {
        if (lista.isEmpty()) {
            JPanel vacio = new JPanel(new GridBagLayout());
            vacio.setBackground(Color.WHITE);
            JLabel lbl = new JLabel("Aun no tenes capacitaciones registradas en tu historial.");
            lbl.setFont(new Font("Arial", Font.PLAIN, 13));
            lbl.setForeground(GRIS);
            vacio.add(lbl);
            return vacio;
        }

        String[] columnas = {"Curso", "Fecha", "Modalidad", "Asistencia", "Resultado"};
        Object[][] datos = new Object[lista.size()][5];
        for (int i = 0; i < lista.size(); i++) {
            RegistroConInstancia r = lista.get(i);
            datos[i][0] = r.nombreCurso;
            datos[i][1] = r.fechaInstancia.format(FMT_CORTO);
            datos[i][2] = r.modalidad != null ? r.modalidad.name() : "-";
            datos[i][3] = r.estadoAsistencia;
            datos[i][4] = r.resultado;
        }

        JTable tabla = new JTable(datos, columnas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(235, 240, 250));

        // Colores en columna Asistencia
        tabla.getColumnModel().getColumn(3).setCellRenderer(
            (t, val, sel, foc, row, col) -> {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 6, 0, 0));
                lbl.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                if (val instanceof EstadoAsistencia ea) {
                    lbl.setForeground(switch (ea) {
                        case ASISTIO    -> VERDE;
                        case NO_ASISTIO -> ROJO;
                        default         -> GRIS;
                    });
                    lbl.setFont(new Font("Arial", Font.BOLD, 11));
                }
                return lbl;
            });

        // Colores en columna Resultado
        tabla.getColumnModel().getColumn(4).setCellRenderer(
            (t, val, sel, foc, row, col) -> {
                JLabel lbl = new JLabel(val != null ? val.toString() : "");
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 6, 0, 0));
                lbl.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                if (val instanceof ResultadoCurso rc) {
                    lbl.setForeground(switch (rc) {
                        case APROBADO    -> VERDE;
                        case DESAPROBADO -> ROJO;
                        case NO_APLICA   -> GRIS;
                        default          -> NARANJA;
                    });
                    lbl.setFont(new Font("Arial", Font.BOLD, 11));
                }
                return lbl;
            });

        int[] anchos = {220, 90, 120, 100, 140};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 228)));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scroll);
        return wrapper;
    }
}
