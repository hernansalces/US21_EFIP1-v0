package com.trainup.view.admin;

import com.trainup.controller.CtrlReporte;
import com.trainup.controller.CtrlReporte.FilaReporte;
import com.trainup.enums.CategoriaEngagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PanelReportes extends JPanel {

    private final CtrlReporte ctrl = new CtrlReporte();

    private JComboBox<String> cboArea;
    private JSpinner spnDesde, spnHasta;
    private ReporteTableModel tableModel;
    private JTable tabla;
    private JLabel lblEstado;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color AMARILLO = new Color(190, 140, 0);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color GRIS    = new Color(150, 150, 150);

    public PanelReportes() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
    }

    private void initComponentes() {
        // ── Título ──
        JLabel lblTitulo = new JLabel("Reportes de Capacitacion");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));

        // ── Filtros ──
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelFiltros.setBackground(new Color(245, 248, 252));
        panelFiltros.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 230)),
            new EmptyBorder(6, 10, 6, 10)));

        panelFiltros.add(bold("Area:"));
        cboArea = new JComboBox<>();
        cboArea.setFont(new Font("Arial", Font.PLAIN, 12));
        cboArea.addItem("Todas");
        cargarAreas();
        panelFiltros.add(cboArea);

        panelFiltros.add(bold("Desde:"));
        spnDesde = crearSpinnerFecha(-365);
        panelFiltros.add(spnDesde);

        panelFiltros.add(bold("Hasta:"));
        spnHasta = crearSpinnerFecha(0);
        panelFiltros.add(spnHasta);

        JButton btnGenerar = new JButton("Generar reporte");
        btnGenerar.setBackground(AZUL);
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGenerar.setFocusPainted(false);
        btnGenerar.setBorderPainted(false);
        btnGenerar.addActionListener(e -> generarReporte());
        panelFiltros.add(btnGenerar);

        // ── Tabla ──
        tableModel = new ReporteTableModel();
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(235, 240, 250));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] anchos = {180, 100, 120, 80, 80, 65, 90, 60};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderer IE
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                String txt = v != null ? v.toString() : "Sin datos";
                setText(txt);
                try {
                    double val = Double.parseDouble(txt);
                    setForeground(val >= 75 ? VERDE : val >= 50 ? AMARILLO : ROJO);
                    setFont(new Font("Arial", Font.BOLD, 11));
                } catch (NumberFormatException ex) {
                    setForeground(GRIS);
                }
                return this;
            }
        });

        // Renderer Categoría
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Arial", Font.BOLD, 11));
                if ("ALTO".equals(v))    setForeground(VERDE);
                else if ("MEDIO".equals(v)) setForeground(AMARILLO);
                else if ("CRITICO".equals(v)) setForeground(ROJO);
                else { setForeground(GRIS); setText("Sin datos"); }
                return this;
            }
        });

        // Renderer alertas
        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                if (v instanceof Integer n && n > 0) {
                    setForeground(ROJO);
                    setFont(new Font("Arial", Font.BOLD, 11));
                } else {
                    setForeground(GRIS);
                }
                return this;
            }
        });

        TableRowSorter<ReporteTableModel> sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Resultados", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        lblEstado = new JLabel("Aplica filtros y presiona 'Generar reporte'.");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(GRIS);

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setBackground(Color.WHITE);
        norte.add(lblTitulo,    BorderLayout.NORTH);
        norte.add(panelFiltros, BorderLayout.CENTER);

        add(norte,   BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private void cargarAreas() {
        try { for (String a : ctrl.obtenerAreas()) cboArea.addItem(a); }
        catch (SQLException ignored) {}
    }

    private void generarReporte() {
        String areaSeleccionada = (String) cboArea.getSelectedItem();
        String area = "Todas".equals(areaSeleccionada) ? null : areaSeleccionada;
        LocalDate desde = spinnerToLocalDate(spnDesde);
        LocalDate hasta = spinnerToLocalDate(spnHasta);

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this,
                "La fecha 'Desde' no puede ser posterior a 'Hasta'.",
                "Error en filtros", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<FilaReporte> datos = ctrl.generarPorArea(area, desde, hasta);
            tableModel.setDatos(datos);

            long criticos = datos.stream()
                .filter(f -> f.categoria == CategoriaEngagement.CRITICO).count();
            lblEstado.setText(datos.size() + " empleados   |   "
                + criticos + " en estado CRITICO   |   Periodo: " + desde + " a " + hasta);
            lblEstado.setForeground(criticos > 0 ? ROJO : GRIS);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JSpinner crearSpinnerFecha(int offsetDias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, offsetDias);
        SpinnerDateModel model = new SpinnerDateModel(
            cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor ed = new JSpinner.DateEditor(sp, "dd/MM/yyyy");
        sp.setEditor(ed);
        sp.setFont(new Font("Arial", Font.PLAIN, 12));
        ed.getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
        sp.setPreferredSize(new Dimension(110, 26));
        return sp;
    }

    private LocalDate spinnerToLocalDate(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JLabel bold(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        return l;
    }

    // ── Modelo ──
    private static class ReporteTableModel extends AbstractTableModel {
        private static final String[] COLS =
            {"Empleado","Area","Perfil","Asistencia%","Completados","IE","Categoria","Alertas"};
        private List<FilaReporte> datos = new ArrayList<>();

        public void setDatos(List<FilaReporte> d) { datos = d; fireTableDataChanged(); }
        @Override public int getRowCount()    { return datos.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            FilaReporte f = datos.get(r);
            return switch (c) {
                case 0 -> f.nombre;
                case 1 -> f.area;
                case 2 -> f.perfil != null ? f.perfil : "Sin perfil";
                case 3 -> f.pctAsistencia != null ? f.pctAsistencia + "%" : "0%";
                case 4 -> f.getPctCompletados();
                case 5 -> f.valorIe != null ? f.valorIe.toString() : "Sin IE";
                case 6 -> f.categoria != null ? f.categoria.name() : null;
                case 7 -> f.alertasActivas;
                default -> "";
            };
        }
        @Override public Class<?> getColumnClass(int c) {
            return c == 7 ? Integer.class : String.class;
        }
    }
}
