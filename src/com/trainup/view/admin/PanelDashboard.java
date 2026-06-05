package com.trainup.view.admin;

import com.trainup.controller.CtrlDashboard;
import com.trainup.controller.CtrlDashboard.DashboardStats;
import com.trainup.controller.CtrlDashboard.FilaRanking;
import com.trainup.enums.CategoriaEngagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PanelDashboard extends JPanel {

    private final CtrlDashboard ctrl = new CtrlDashboard();

    private JComboBox<String> cboArea;
    private JPanel panelKpis;
    private RankingTableModel tableModel;
    private JTable tabla;
    private JLabel lblEstado;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color AMARILLO = new Color(190, 140, 0);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color GRIS    = new Color(150, 150, 150);

    public PanelDashboard() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargar(null);
    }

    private void initComponentes() {
        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Dashboard de Engagement");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(AZUL);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setBackground(Color.WHITE);
        filtros.add(new JLabel("Filtrar por area:"));
        cboArea = new JComboBox<>();
        cboArea.setFont(new Font("Arial", Font.PLAIN, 12));
        cboArea.addItem("Todas las areas");
        cargarAreas();
        cboArea.addActionListener(e -> {
            String sel = (String) cboArea.getSelectedItem();
            cargar("Todas las areas".equals(sel) ? null : sel);
        });
        filtros.add(cboArea);

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRefrescar.addActionListener(e -> cboArea.getActionListeners()[0]
            .actionPerformed(null));
        filtros.add(btnRefrescar);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(filtros,   BorderLayout.EAST);

        // ── KPIs ──
        panelKpis = new JPanel(new GridLayout(1, 4, 12, 0));
        panelKpis.setBackground(Color.WHITE);
        panelKpis.setPreferredSize(new Dimension(0, 100));

        // ── Tabla de ranking ──
        tableModel = new RankingTableModel();
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(28);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(235, 240, 250));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] anchos = {30, 180, 100, 120, 100, 80, 70, 70, 70, 60};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderer IE
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                if (v instanceof BigDecimal bd) {
                    setForeground(colorIE(bd.intValue()));
                    setFont(new Font("Arial", Font.BOLD, 12));
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
                if (v instanceof CategoriaEngagement cat) {
                    setForeground(colorCategoria(cat));
                    setFont(new Font("Arial", Font.BOLD, 11));
                    setText(cat.name());
                } else if (v == null) {
                    setForeground(GRIS); setText("Sin datos");
                }
                return this;
            }
        });

        // Renderer alertas
        tabla.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                if (v instanceof Integer alerts && alerts > 0) {
                    setForeground(ROJO);
                    setFont(new Font("Arial", Font.BOLD, 11));
                } else {
                    setForeground(GRIS);
                }
                return this;
            }
        });

        TableRowSorter<RankingTableModel> sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Ranking de empleados por IE", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(GRIS);

        JPanel central = new JPanel(new BorderLayout(0, 10));
        central.setBackground(Color.WHITE);
        central.add(panelKpis, BorderLayout.NORTH);
        central.add(scroll,    BorderLayout.CENTER);
        central.add(lblEstado, BorderLayout.SOUTH);

        add(header,  BorderLayout.NORTH);
        add(central, BorderLayout.CENTER);
    }

    private void cargarAreas() {
        try {
            for (String a : ctrl.obtenerAreas()) cboArea.addItem(a);
        } catch (SQLException ignored) {}
    }

    private void cargar(String area) {
        try {
            DashboardStats stats = ctrl.obtenerStats(area);
            List<FilaRanking> ranking = ctrl.obtenerRanking(area);

            // Reconstruir KPIs
            panelKpis.removeAll();
            panelKpis.add(kpiCard("IE Promedio", stats.promedio.toString(), AZUL));
            panelKpis.add(kpiCard("ALTO (" + stats.pctAlto() + "%)",
                String.valueOf(stats.alto), VERDE));
            panelKpis.add(kpiCard("MEDIO (" + stats.pctMedio() + "%)",
                String.valueOf(stats.medio), AMARILLO));
            panelKpis.add(kpiCard("CRITICO (" + stats.pctCritico() + "%)",
                String.valueOf(stats.critico), ROJO));
            panelKpis.revalidate();
            panelKpis.repaint();

            tableModel.setDatos(ranking);
            lblEstado.setText(stats.total + " empleados activos   |   "
                + ranking.stream().filter(f -> f.categoria == CategoriaEngagement.CRITICO).count()
                + " en estado CRITICO");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar dashboard: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel kpiCard(String titulo, String valor, Color color) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(
            Math.min(color.getRed()   + 220, 255),
            Math.min(color.getGreen() + 220, 255),
            Math.min(color.getBlue()  + 220, 255)));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            new EmptyBorder(8, 12, 8, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Arial", Font.BOLD, 34));
        lblVal.setForeground(color.darker());
        card.add(lblVal, gbc);

        gbc.gridy = 1;
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTit.setForeground(color.darker());
        card.add(lblTit, gbc);

        return card;
    }

    private Color colorIE(int val) {
        if (val >= 75) return VERDE;
        if (val >= 50) return AMARILLO;
        return ROJO;
    }

    private Color colorCategoria(CategoriaEngagement cat) {
        return switch (cat) {
            case ALTO    -> VERDE;
            case MEDIO   -> AMARILLO;
            case CRITICO -> ROJO;
        };
    }

    // ── Modelo ──
    private static class RankingTableModel extends AbstractTableModel {
        private static final String[] COLS =
            {"#","Empleado","Area","Puesto","Perfil","IE","Categoria","PA","PR","Alertas"};
        private List<FilaRanking> datos = new ArrayList<>();

        public void setDatos(List<FilaRanking> d) { datos = d; fireTableDataChanged(); }

        @Override public int getRowCount()    { return datos.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            FilaRanking f = datos.get(r);
            return switch (c) {
                case 0 -> r + 1;
                case 1 -> f.nombre;
                case 2 -> f.area;
                case 3 -> f.puesto;
                case 4 -> f.perfil != null ? f.perfil : "Sin perfil";
                case 5 -> f.valorIe != null ? f.valorIe : null;
                case 6 -> f.categoria;
                case 7 -> f.valorIe != null ? "-" : "-";
                case 8 -> "-";
                case 9 -> f.alertas;
                default -> "";
            };
        }
        @Override public Class<?> getColumnClass(int c) {
            return switch (c) {
                case 0, 9 -> Integer.class;
                case 5    -> BigDecimal.class;
                case 6    -> CategoriaEngagement.class;
                default   -> String.class;
            };
        }
    }
}
