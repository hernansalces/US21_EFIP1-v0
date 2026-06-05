package com.trainup.view.admin;

import com.trainup.controller.CtrlPerfil;
import com.trainup.enums.NivelExpertise;
import com.trainup.model.Curso;
import com.trainup.model.PerfilCarrera;
import com.trainup.model.PerfilCurso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PanelPerfiles extends JPanel {

    private final CtrlPerfil ctrl = new CtrlPerfil();

    // Panel izquierdo — listado de perfiles
    private JList<PerfilCarrera> listPerfiles;
    private DefaultListModel<PerfilCarrera> modeloPerfiles;
    private JButton btnNuevoPerfil, btnEditarPerfil, btnDesactivarPerfil;
    private JLabel lblEstadoPerfil;

    // Panel derecho — cursos del perfil seleccionado
    private CursosPerfilTableModel tablaCursosModel;
    private JTable tablaCursos;
    private JComboBox<Curso> cboCursoAgregar;
    private JComboBox<NivelExpertise> cboNivel;
    private JCheckBox chkObligatorio;
    private JButton btnAgregar, btnQuitar;
    private JLabel lblTituloCursos;

    private static final Color AZUL = new Color(33, 97, 140);
    private static final Color ROJO = new Color(180, 30, 30);

    public PanelPerfiles() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargarPerfiles();
    }

    private void initComponentes() {
        JLabel lblTitulo = new JLabel("Perfiles de Carrera");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));

        // ── Panel IZQUIERDO: listado de perfiles ──
        JPanel panelIzq = new JPanel(new BorderLayout(0, 6));
        panelIzq.setBackground(Color.WHITE);
        panelIzq.setPreferredSize(new Dimension(240, 0));
        panelIzq.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Perfiles activos", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        modeloPerfiles = new DefaultListModel<>();
        listPerfiles = new JList<>(modeloPerfiles);
        listPerfiles.setFont(new Font("Arial", Font.PLAIN, 13));
        listPerfiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPerfiles.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value.getNombre() : "");
            lbl.setBorder(new EmptyBorder(5, 10, 5, 10));
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? new Color(210, 230, 250) : Color.WHITE);
            lbl.setForeground(Color.DARK_GRAY);
            return lbl;
        });
        listPerfiles.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSeleccionPerfil();
        });

        JScrollPane scrollPerfiles = new JScrollPane(listPerfiles);
        scrollPerfiles.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        JPanel botonesIzq = new JPanel(new GridLayout(1, 3, 4, 0));
        botonesIzq.setBackground(Color.WHITE);
        btnNuevoPerfil     = crearBotonPeq("+ Nuevo", AZUL);
        btnEditarPerfil    = crearBotonPeq("Editar", new Color(80, 120, 160));
        btnDesactivarPerfil = crearBotonPeq("Eliminar", ROJO);
        btnEditarPerfil.setEnabled(false);
        btnDesactivarPerfil.setEnabled(false);
        botonesIzq.add(btnNuevoPerfil);
        botonesIzq.add(btnEditarPerfil);
        botonesIzq.add(btnDesactivarPerfil);

        lblEstadoPerfil = new JLabel(" ");
        lblEstadoPerfil.setFont(new Font("Arial", Font.PLAIN, 10));
        lblEstadoPerfil.setForeground(Color.GRAY);

        panelIzq.add(scrollPerfiles, BorderLayout.CENTER);
        panelIzq.add(botonesIzq, BorderLayout.SOUTH);

        // ── Panel DERECHO: cursos del perfil ──
        JPanel panelDer = new JPanel(new BorderLayout(0, 8));
        panelDer.setBackground(Color.WHITE);
        panelDer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Trayectoria formativa", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        lblTituloCursos = new JLabel("Seleccioná un perfil para ver sus cursos.");
        lblTituloCursos.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTituloCursos.setForeground(Color.GRAY);
        lblTituloCursos.setBorder(new EmptyBorder(4, 6, 4, 6));

        // Tabla de cursos del perfil
        tablaCursosModel = new CursosPerfilTableModel();
        tablaCursos = new JTable(tablaCursosModel);
        tablaCursos.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaCursos.setRowHeight(24);
        tablaCursos.setGridColor(new Color(220, 225, 230));
        tablaCursos.setSelectionBackground(new Color(210, 230, 250));
        tablaCursos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaCursos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] anchos = {200, 100, 100};
        for (int i = 0; i < anchos.length; i++)
            tablaCursos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Columna Obligatorio con color
        tablaCursos.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(CENTER);
                boolean oblig = "Obligatorio".equals(v);
                setForeground(oblig ? ROJO : new Color(39, 120, 70));
                setFont(new Font("Arial", Font.BOLD, 11));
                return this;
            }
        });

        JScrollPane scrollCursos = new JScrollPane(tablaCursos);
        scrollCursos.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));

        // Panel para agregar curso al perfil
        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelAgregar.setBackground(new Color(245, 248, 252));
        panelAgregar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 218, 228)));

        panelAgregar.add(new JLabel("Agregar:"));
        cboCursoAgregar = new JComboBox<>();
        cboCursoAgregar.setPreferredSize(new Dimension(200, 26));
        cboCursoAgregar.setFont(new Font("Arial", Font.PLAIN, 12));
        panelAgregar.add(cboCursoAgregar);

        panelAgregar.add(new JLabel("Nivel:"));
        cboNivel = new JComboBox<>(NivelExpertise.values());
        cboNivel.setFont(new Font("Arial", Font.PLAIN, 12));
        panelAgregar.add(cboNivel);

        chkObligatorio = new JCheckBox("Obligatorio");
        chkObligatorio.setFont(new Font("Arial", Font.PLAIN, 12));
        chkObligatorio.setBackground(new Color(245, 248, 252));
        chkObligatorio.setSelected(true);
        panelAgregar.add(chkObligatorio);

        btnAgregar = crearBotonPeq("Agregar →", AZUL);
        btnAgregar.setEnabled(false);
        panelAgregar.add(btnAgregar);

        btnQuitar = crearBotonPeq("← Quitar", ROJO);
        btnQuitar.setEnabled(false);
        panelAgregar.add(btnQuitar);

        panelDer.add(lblTituloCursos, BorderLayout.NORTH);
        panelDer.add(scrollCursos, BorderLayout.CENTER);
        panelDer.add(panelAgregar, BorderLayout.SOUTH);

        // Acciones botones izquierda
        btnNuevoPerfil.addActionListener(e -> nuevoPerfil());
        btnEditarPerfil.addActionListener(e -> editarPerfil());
        btnDesactivarPerfil.addActionListener(e -> desactivarPerfil());
        btnAgregar.addActionListener(e -> agregarCurso());
        btnQuitar.addActionListener(e -> quitarCurso());

        tablaCursos.getSelectionModel().addListSelectionListener(e ->
            btnQuitar.setEnabled(tablaCursos.getSelectedRow() >= 0));

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(250);
        split.setDividerSize(5);
        split.setBorder(null);

        add(lblTitulo, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(lblEstadoPerfil, BorderLayout.SOUTH);
    }

    private JButton crearBotonPeq(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void cargarPerfiles() {
        try {
            modeloPerfiles.clear();
            for (PerfilCarrera p : ctrl.obtenerActivos()) modeloPerfiles.addElement(p);
            lblEstadoPerfil.setText(modeloPerfiles.size() + " perfiles activos");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar perfiles: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCursosDisponibles() {
        try {
            cboCursoAgregar.removeAllItems();
            for (Curso c : ctrl.obtenerCursosDisponibles()) cboCursoAgregar.addItem(c);
        } catch (SQLException ignored) {}
    }

    private void onSeleccionPerfil() {
        PerfilCarrera sel = listPerfiles.getSelectedValue();
        boolean haySel = sel != null;
        btnEditarPerfil.setEnabled(haySel);
        btnDesactivarPerfil.setEnabled(haySel);
        btnAgregar.setEnabled(haySel);

        if (!haySel) {
            tablaCursosModel.setCursos(new ArrayList<>());
            lblTituloCursos.setText("Seleccioná un perfil para ver sus cursos.");
            return;
        }

        lblTituloCursos.setText("Cursos de: " + sel.getNombre());
        cargarCursosDisponibles();
        try {
            tablaCursosModel.setCursos(ctrl.obtenerCursosDePerfil(sel.getId()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar cursos del perfil: " + e.getMessage());
        }
    }

    private void nuevoPerfil() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo perfil:", "Nuevo perfil", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;
        String desc = JOptionPane.showInputDialog(this, "Descripción (opcional):", "Nuevo perfil", JOptionPane.PLAIN_MESSAGE);
        PerfilCarrera p = new PerfilCarrera();
        p.setNombre(nombre.trim());
        p.setDescripcion(desc != null ? desc.trim() : "");
        try {
            ctrl.registrar(p);
            cargarPerfiles();
            lblEstadoPerfil.setText("Perfil '" + nombre + "' creado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarPerfil() {
        PerfilCarrera sel = listPerfiles.getSelectedValue();
        if (sel == null) return;
        String nombre = JOptionPane.showInputDialog(this, "Nombre del perfil:", sel.getNombre());
        if (nombre == null || nombre.trim().isEmpty()) return;
        String desc = JOptionPane.showInputDialog(this, "Descripción:", sel.getDescripcion() != null ? sel.getDescripcion() : "");
        sel.setNombre(nombre.trim());
        sel.setDescripcion(desc != null ? desc.trim() : "");
        try {
            ctrl.actualizar(sel);
            cargarPerfiles();
            lblEstadoPerfil.setText("Perfil actualizado.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarPerfil() {
        PerfilCarrera sel = listPerfiles.getSelectedValue();
        if (sel == null) return;
        int resp = JOptionPane.showConfirmDialog(this,
            "<html>¿Eliminar el perfil <b>" + sel.getNombre() + "</b>?<br>" +
            "Solo es posible si no tiene empleados activos asignados.</html>",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ctrl.desactivar(sel.getId());
                cargarPerfiles();
                tablaCursosModel.setCursos(new ArrayList<>());
                lblTituloCursos.setText("Seleccioná un perfil.");
                lblEstadoPerfil.setText("Perfil eliminado.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede eliminar", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void agregarCurso() {
        PerfilCarrera perfil = listPerfiles.getSelectedValue();
        Curso curso = (Curso) cboCursoAgregar.getSelectedItem();
        if (perfil == null || curso == null) return;

        PerfilCurso pc = new PerfilCurso();
        pc.setIdPerfil(perfil.getId());
        pc.setIdCurso(curso.getId());
        pc.setNombreCurso(curso.getNombre());
        pc.setEsObligatorio(chkObligatorio.isSelected());
        pc.setNivelExpertise((NivelExpertise) cboNivel.getSelectedItem());

        try {
            ctrl.asociarCurso(pc);
            tablaCursosModel.setCursos(ctrl.obtenerCursosDePerfil(perfil.getId()));
            lblEstadoPerfil.setText("Curso '" + curso.getNombre() + "' agregado al perfil.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al agregar curso: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarCurso() {
        PerfilCarrera perfil = listPerfiles.getSelectedValue();
        int fila = tablaCursos.getSelectedRow();
        if (perfil == null || fila < 0) return;

        PerfilCurso pc = tablaCursosModel.getCurso(fila);
        int resp = JOptionPane.showConfirmDialog(this,
            "¿Quitar '" + pc.getNombreCurso() + "' del perfil?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ctrl.desasociarCurso(perfil.getId(), pc.getIdCurso());
                tablaCursosModel.setCursos(ctrl.obtenerCursosDePerfil(perfil.getId()));
                lblEstadoPerfil.setText("Curso quitado del perfil.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Modelo tabla cursos del perfil ──
    private static class CursosPerfilTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Curso", "Tipo", "Nivel"};
        private List<PerfilCurso> cursos = new ArrayList<>();

        public void setCursos(List<PerfilCurso> lista) { this.cursos = lista; fireTableDataChanged(); }
        public PerfilCurso getCurso(int fila) { return cursos.get(fila); }
        @Override public int getRowCount() { return cursos.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int fila, int col) {
            PerfilCurso pc = cursos.get(fila);
            return switch (col) {
                case 0 -> pc.getNombreCurso();
                case 1 -> pc.isEsObligatorio() ? "Obligatorio" : "Opcional";
                case 2 -> pc.getNivelExpertise() != null ? pc.getNivelExpertise().name() : "-";
                default -> "";
            };
        }
    }
}
