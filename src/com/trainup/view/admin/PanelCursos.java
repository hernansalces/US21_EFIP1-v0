package com.trainup.view.admin;

import com.trainup.controller.CtrlCurso;
import com.trainup.model.Curso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PanelCursos extends JPanel {

    private final CtrlCurso ctrl = new CtrlCurso();
    private CursoTableModel tableModel;
    private JTable tabla;
    private JTextField txtBuscar;
    private JButton btnNuevo, btnEditar, btnDesactivar;
    private JLabel lblEstado;

    private static final Color AZUL  = new Color(33, 97, 140);
    private static final Color ROJO  = new Color(180, 30, 30);
    private static final Color VERDE = new Color(39, 120, 70);

    public PanelCursos() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargarDatos();
    }

    private void initComponentes() {
        // Barra superior
        JPanel barraTop = new JPanel(new BorderLayout(10, 0));
        barraTop.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Catálogo de Cursos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBusqueda.setBackground(Color.WHITE);
        txtBuscar = new JTextField(20);
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 13));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotones.setBackground(Color.WHITE);

        btnNuevo      = crearBoton("+ Nuevo", AZUL);
        btnEditar     = crearBoton("Editar", new Color(80, 120, 160));
        btnDesactivar = crearBoton("Desactivar", ROJO);
        btnEditar.setEnabled(false);
        btnDesactivar.setEnabled(false);

        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnDesactivar);

        barraTop.add(lblTitulo, BorderLayout.WEST);
        barraTop.add(panelBusqueda, BorderLayout.CENTER);
        barraTop.add(panelBotones, BorderLayout.EAST);

        // Tabla
        tableModel = new CursoTableModel();
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] anchos = {40, 200, 70, 120, 130, 150, 70};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Estado con color
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                setForeground("Activo".equals(v) ? VERDE : ROJO);
                setFont(new Font("Arial", "Activo".equals(v) ? Font.BOLD : Font.PLAIN, 11));
                return this;
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = tabla.getSelectedRow() >= 0;
            btnEditar.setEnabled(sel);
            btnDesactivar.setEnabled(sel && tableModel.getCurso(tabla.getSelectedRow()).isActivo());
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) abrirEditar();
            }
        });

        TableRowSorter<CursoTableModel> sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(new Color(100, 100, 100));

        btnNuevo.addActionListener(e -> abrirNuevo());
        btnEditar.addActionListener(e -> abrirEditar());
        btnDesactivar.addActionListener(e -> desactivar());

        add(barraTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 30));
        return btn;
    }

    private void cargarDatos() {
        try {
            tableModel.setCursos(ctrl.obtenerTodos());
            actualizarEstado();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 1, 4, 5));
        actualizarEstado();
    }

    private void actualizarEstado() {
        lblEstado.setText(tabla.getRowCount() + " de " + tableModel.getRowCount() + " cursos");
    }

    private void abrirNuevo() {
        FormularioCurso form = new FormularioCurso(
            (Frame) SwingUtilities.getWindowAncestor(this), FormularioCurso.Modo.NUEVO, null, ctrl);
        form.setVisible(true);
        if (form.isGuardado()) { cargarDatos(); lblEstado.setText("Curso registrado correctamente."); }
    }

    private void abrirEditar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Curso sel = tableModel.getCurso(tabla.convertRowIndexToModel(fila));
        FormularioCurso form = new FormularioCurso(
            (Frame) SwingUtilities.getWindowAncestor(this), FormularioCurso.Modo.EDITAR, sel, ctrl);
        form.setVisible(true);
        if (form.isGuardado()) { cargarDatos(); lblEstado.setText("Curso actualizado correctamente."); }
    }

    private void desactivar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Curso sel = tableModel.getCurso(tabla.convertRowIndexToModel(fila));
        int resp = JOptionPane.showConfirmDialog(this,
            "<html>¿Desactivar el curso <b>" + sel.getNombre() + "</b>?<br>" +
            "No podrá asignarse a nuevas instancias de capacitación.</html>",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ctrl.desactivar(sel.getId());
                cargarDatos();
                lblEstado.setText("Curso desactivado.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede desactivar", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ── Modelo de tabla ──
    private static class CursoTableModel extends AbstractTableModel {
        private static final String[] COLS = {"ID","Nombre","Horas","Modalidad","Categoría","Capacitador","Estado"};
        private List<Curso> cursos = new ArrayList<>();

        public void setCursos(List<Curso> lista) { this.cursos = lista; fireTableDataChanged(); }
        public Curso getCurso(int fila) { return cursos.get(fila); }
        @Override public int getRowCount() { return cursos.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            Curso c = cursos.get(fila);
            return switch (col) {
                case 0 -> c.getId();
                case 1 -> c.getNombre();
                case 2 -> c.getDuracionHoras() != null ? c.getDuracionHoras() + "h" : "-";
                case 3 -> c.getModalidad() != null ? c.getModalidad().name() : "-";
                case 4 -> c.getCategoriaTematica() != null ? c.getCategoriaTematica() : "-";
                case 5 -> c.getNombreCapacitador() != null ? c.getNombreCapacitador() : "Sin asignar";
                case 6 -> c.isActivo() ? "Activo" : "Inactivo";
                default -> "";
            };
        }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
    }
}
