package com.trainup.view.admin;

import com.trainup.controller.CtrlEmpleado;
import com.trainup.model.Empleado;

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

public class PanelEmpleados extends JPanel {

    private final CtrlEmpleado ctrl = new CtrlEmpleado();
    private EmpleadoTableModel tableModel;
    private JTable tabla;
    private JTextField txtBuscar;
    private JButton btnNuevo, btnEditar, btnBaja, btnToggleInactivos;
    private JLabel lblEstado;
    private boolean mostrандоInactivos = false;

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color GRIS_BG = new Color(245, 248, 252);

    public PanelEmpleados() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargarDatos();
    }

    private void initComponentes() {
        // ── Barra superior: título + búsqueda + botones ──
        JPanel barraTop = new JPanel(new BorderLayout(10, 0));
        barraTop.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Gestión de Empleados");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBusqueda.setBackground(Color.WHITE);

        txtBuscar = new JTextField(22);
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 13));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre, apellido o DNI...");
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotones.setBackground(Color.WHITE);

        btnNuevo = crearBoton("+ Nuevo", AZUL);
        btnEditar = crearBoton("Editar", new Color(80, 120, 160));
        btnBaja = crearBoton("Dar de baja", ROJO);
        btnToggleInactivos = crearBoton("Ver inactivos", new Color(100, 100, 100));

        btnEditar.setEnabled(false);
        btnBaja.setEnabled(false);

        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnBaja);
        panelBotones.add(btnToggleInactivos);

        barraTop.add(lblTitulo, BorderLayout.WEST);
        barraTop.add(panelBusqueda, BorderLayout.CENTER);
        barraTop.add(panelBotones, BorderLayout.EAST);

        // ── Tabla ──
        tableModel = new EmpleadoTableModel();
        tabla = new JTable(tableModel);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(220, 225, 230));
        tabla.setSelectionBackground(new Color(210, 230, 250));
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(GRIS_BG);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Ancho de columnas
        int[] anchos = {40, 120, 120, 90, 180, 80, 140, 100, 70};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // Columna Estado: colorear según activo/inactivo
        tabla.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                if ("Activo".equals(value)) {
                    setForeground(VERDE);
                    setFont(new Font("Arial", Font.BOLD, 11));
                } else {
                    setForeground(ROJO);
                    setFont(new Font("Arial", Font.PLAIN, 11));
                }
                return this;
            }
        });

        // Habilitar/deshabilitar botones según selección
        tabla.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = tabla.getSelectedRow() >= 0;
            btnEditar.setEnabled(sel);
            boolean esActivo = sel && tableModel.getEmpleado(tabla.getSelectedRow()).isActivo();
            btnBaja.setEnabled(sel && esActivo);
        });

        // Doble clic = editar
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) abrirEditar();
            }
        });

        // Ordenamiento
        TableRowSorter<EmpleadoTableModel> sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        // ── Barra inferior: estado ──
        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(new Color(100, 100, 100));

        // Acciones de botones
        btnNuevo.addActionListener(e -> abrirNuevo());
        btnEditar.addActionListener(e -> abrirEditar());
        btnBaja.addActionListener(e -> darDeBaja());
        btnToggleInactivos.addActionListener(e -> toggleInactivos());

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
            List<Empleado> lista = mostrандоInactivos
                    ? ctrl.obtenerTodos()
                    : ctrl.obtenerActivos();
            tableModel.setEmpleados(lista);
            actualizarEstado();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar empleados: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1, 2, 3));
        }
        actualizarEstado();
    }

    private void actualizarEstado() {
        int total = tableModel.getRowCount();
        int visibles = tabla.getRowCount();
        String modo = mostrандоInactivos ? "todos" : "activos";
        lblEstado.setText(visibles == total
            ? total + " empleados " + modo
            : visibles + " de " + total + " empleados " + modo);
    }

    private void abrirNuevo() {
        FormularioEmpleado form = new FormularioEmpleado(
            (Frame) SwingUtilities.getWindowAncestor(this),
            FormularioEmpleado.Modo.NUEVO, null, ctrl);
        form.setVisible(true);
        if (form.isGuardado()) {
            cargarDatos();
            lblEstado.setText("Empleado registrado correctamente.");
        }
    }

    private void abrirEditar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Empleado sel = tableModel.getEmpleado(tabla.convertRowIndexToModel(fila));
        FormularioEmpleado form = new FormularioEmpleado(
            (Frame) SwingUtilities.getWindowAncestor(this),
            FormularioEmpleado.Modo.EDITAR, sel, ctrl);
        form.setVisible(true);
        if (form.isGuardado()) {
            cargarDatos();
            lblEstado.setText("Datos del empleado actualizados correctamente.");
        }
    }

    private void darDeBaja() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        Empleado sel = tableModel.getEmpleado(tabla.convertRowIndexToModel(fila));

        try {
            int instanciasFuturas = ctrl.obtenerActivos().size(); // solo para la advertencia
            // Verificamos instancias futuras directamente
            String msg = "<html>¿Dar de baja a <b>" + sel.getNombreCompleto() + "</b>?<br>" +
                         "Su historial formativo será conservado.<br>" +
                         "Esta acción desactivará su acceso al sistema.</html>";

            int resultado = JOptionPane.showConfirmDialog(this, msg,
                "Confirmar baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (resultado == JOptionPane.YES_OPTION) {
                int futuras = ctrl.darDeBaja(sel.getId());
                cargarDatos();
                String info = futuras > 0
                    ? "Empleado dado de baja. Se removió de " + futuras + " instancia(s) futura(s)."
                    : "Empleado dado de baja correctamente. Historial conservado.";
                lblEstado.setText(info);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al dar de baja: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleInactivos() {
        mostrандоInactivos = !mostrандоInactivos;
        btnToggleInactivos.setText(mostrандоInactivos ? "Solo activos" : "Ver inactivos");
        cargarDatos();
    }

    // ── Modelo de tabla interno ──
    private static class EmpleadoTableModel extends AbstractTableModel {

        private static final String[] COLUMNAS =
            {"ID", "Nombre", "Apellido", "DNI", "Email", "Área", "Puesto", "Perfil", "Estado"};
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private List<Empleado> empleados = new ArrayList<>();

        public void setEmpleados(List<Empleado> lista) {
            this.empleados = lista;
            fireTableDataChanged();
        }

        public Empleado getEmpleado(int fila) {
            return empleados.get(fila);
        }

        @Override public int getRowCount() { return empleados.size(); }
        @Override public int getColumnCount() { return COLUMNAS.length; }
        @Override public String getColumnName(int col) { return COLUMNAS[col]; }

        @Override
        public Object getValueAt(int fila, int col) {
            Empleado e = empleados.get(fila);
            return switch (col) {
                case 0 -> e.getId();
                case 1 -> e.getNombre();
                case 2 -> e.getApellido();
                case 3 -> e.getDni();
                case 4 -> e.getEmail();
                case 5 -> e.getArea();
                case 6 -> e.getPuesto();
                case 7 -> e.getNombrePerfil() != null ? e.getNombrePerfil() : "Sin asignar";
                case 8 -> e.isActivo() ? "Activo" : "Inactivo";
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }
    }
}
