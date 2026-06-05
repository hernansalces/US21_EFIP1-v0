package com.trainup.view.admin;

import com.trainup.controller.CtrlInstancia;
import com.trainup.enums.EstadoInstancia;
import com.trainup.model.InstanciaCapacitacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelProgramacion extends JPanel {

    private final CtrlInstancia ctrl = new CtrlInstancia();
    private InstanciaTableModel tableModel;
    private JTable tabla;
    private JButton btnNueva, btnRealizada, btnCancelar;
    private JLabel lblEstado;

    private static final Color AZUL  = new Color(33, 97, 140);
    private static final Color ROJO  = new Color(180, 30, 30);
    private static final Color VERDE = new Color(39, 120, 70);
    private static final Color GRIS  = new Color(120, 120, 120);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelProgramacion() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargarDatos();
    }

    private void initComponentes() {
        JPanel barraTop = new JPanel(new BorderLayout(10, 0));
        barraTop.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Programacion de Instancias");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotones.setBackground(Color.WHITE);

        btnNueva     = crearBoton("+ Nueva instancia", AZUL);
        btnRealizada = crearBoton("Marcar realizada",  VERDE);
        btnCancelar  = crearBoton("Cancelar instancia", ROJO);
        btnRealizada.setEnabled(false);
        btnCancelar.setEnabled(false);

        panelBotones.add(btnNueva);
        panelBotones.add(btnRealizada);
        panelBotones.add(btnCancelar);
        barraTop.add(lblTitulo, BorderLayout.WEST);
        barraTop.add(panelBotones, BorderLayout.EAST);

        // Tabla
        tableModel = new InstanciaTableModel();
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] anchos = {40, 200, 140, 140, 110, 70, 80};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Color por estado
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                String val = String.valueOf(v);
                setForeground(switch (val) {
                    case "PROGRAMADA" -> AZUL;
                    case "REALIZADA"  -> VERDE;
                    case "CANCELADA"  -> ROJO;
                    default           -> GRIS;
                });
                setFont(new Font("Arial", Font.BOLD, 11));
                return this;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                InstanciaCapacitacion sel = tableModel.getInstancia(tabla.convertRowIndexToModel(fila));
                boolean esProgramada = sel.getEstado() == EstadoInstancia.PROGRAMADA;
                btnRealizada.setEnabled(esProgramada);
                btnCancelar.setEnabled(esProgramada);
            } else {
                btnRealizada.setEnabled(false);
                btnCancelar.setEnabled(false);
            }
        });

        TableRowSorter<InstanciaTableModel> sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(GRIS);

        btnNueva.addActionListener(e     -> abrirNueva());
        btnRealizada.addActionListener(e -> marcarRealizada());
        btnCancelar.addActionListener(e  -> cancelarInstancia());

        add(barraTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 30));
        return btn;
    }

    private void cargarDatos() {
        try {
            tableModel.setInstancias(ctrl.obtenerTodas());
            lblEstado.setText(tableModel.getRowCount() + " instancias registradas");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar instancias: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirNueva() {
        FormularioInstancia form = new FormularioInstancia(
            (Frame) SwingUtilities.getWindowAncestor(this), ctrl);
        form.setVisible(true);
        if (form.isGuardado()) {
            cargarDatos();
            lblEstado.setText("Instancia programada correctamente.");
        }
    }

    private void marcarRealizada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        InstanciaCapacitacion sel = tableModel.getInstancia(tabla.convertRowIndexToModel(fila));

        int resp = JOptionPane.showConfirmDialog(this,
            "<html>¿Marcar como <b>REALIZADA</b> la instancia:<br>" +
            "<b>" + sel.getNombreCurso() + "</b> (" +
            (sel.getFechaInicio() != null ? sel.getFechaInicio().format(FMT) : "") + ")?<br><br>" +
            "Podrá registrar asistencia desde el módulo Asistencia.</html>",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                ctrl.marcarRealizada(sel.getId());
                cargarDatos();
                lblEstado.setText("Instancia marcada como REALIZADA.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelarInstancia() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        InstanciaCapacitacion sel = tableModel.getInstancia(tabla.convertRowIndexToModel(fila));
        String motivo = JOptionPane.showInputDialog(this,
            "Motivo de cancelacion de: " + sel.getNombreCurso(), "Cancelar instancia", JOptionPane.PLAIN_MESSAGE);
        if (motivo == null) return;
        try {
            ctrl.cancelar(sel.getId(), motivo);
            cargarDatos();
            lblEstado.setText("Instancia cancelada.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class InstanciaTableModel extends AbstractTableModel {
        private static final String[] COLS = {"ID","Curso","Inicio","Fin","Modalidad","Empleados","Estado"};
        private List<InstanciaCapacitacion> lista = new ArrayList<>();

        public void setInstancias(List<InstanciaCapacitacion> l) { lista = l; fireTableDataChanged(); }
        public InstanciaCapacitacion getInstancia(int fila) { return lista.get(fila); }
        @Override public int getRowCount() { return lista.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            InstanciaCapacitacion i = lista.get(fila);
            return switch (col) {
                case 0 -> i.getId();
                case 1 -> i.getNombreCurso();
                case 2 -> i.getFechaInicio() != null ? i.getFechaInicio().format(FMT) : "-";
                case 3 -> i.getFechaFin()    != null ? i.getFechaFin().format(FMT)    : "-";
                case 4 -> i.getModalidad() != null ? i.getModalidad().name() : "-";
                case 5 -> "-";  // conteo se puede agregar si se incluye en el mapeo
                case 6 -> i.getEstado() != null ? i.getEstado().name() : "-";
                default -> "";
            };
        }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
    }
}
