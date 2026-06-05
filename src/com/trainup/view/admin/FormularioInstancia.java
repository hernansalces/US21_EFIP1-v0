package com.trainup.view.admin;

import com.trainup.controller.CtrlInstancia;
import com.trainup.enums.ModalidadCurso;
import com.trainup.model.Curso;
import com.trainup.model.Empleado;
import com.trainup.model.InstanciaCapacitacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FormularioInstancia extends JDialog {

    private final CtrlInstancia ctrl;
    private boolean guardado = false;

    private JComboBox<Curso> cboCurso;
    private JTextField txtLugarUrl;
    private JComboBox<ModalidadCurso> cboModalidad;
    private DefaultListModel<Empleado> modeloDisponibles, modeloAsignados;
    private JList<Empleado> listDisponibles, listAsignados;
    private JLabel lblError;

    // Spinners fecha inicio
    private JSpinner spnFechaInicio;
    private JSpinner spnHoraInicio;
    private JSpinner spnMinInicio;

    // Spinners fecha fin
    private JSpinner spnFechaFin;
    private JSpinner spnHoraFin;
    private JSpinner spnMinFin;

    private static final Color AZUL = new Color(33, 97, 140);

    public FormularioInstancia(Frame parent, CtrlInstancia ctrl) {
        super(parent, "Nueva instancia de capacitacion", true);
        this.ctrl = ctrl;
        initComponentes();
    }

    private void initComponentes() {
        setSize(720, 580);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(new EmptyBorder(12, 16, 12, 16));
        contenido.setBackground(Color.WHITE);

        // ── Panel datos ──
        JPanel panelDatos = new JPanel(new GridBagLayout());
        panelDatos.setBackground(Color.WHITE);
        panelDatos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Datos de la instancia", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Curso (ocupa toda la fila)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15; gbc.gridwidth = 1;
        panelDatos.add(lbl("Curso *"), gbc);
        cboCurso = new JComboBox<>();
        cboCurso.setFont(new Font("Arial", Font.PLAIN, 12));
        cargarCursos();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.85; gbc.gridwidth = 5;
        panelDatos.add(cboCurso, gbc);

        // ── Fila de fecha inicio ──
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        panelDatos.add(lbl("Inicio *"), gbc);

        // Spinner fecha inicio
        spnFechaInicio = crearSpinnerFecha(1);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.30;
        panelDatos.add(spnFechaInicio, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.05;
        panelDatos.add(lbl("Hora:"), gbc);

        spnHoraInicio = crearSpinnerEntero(0, 23, 9);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.10;
        panelDatos.add(spnHoraInicio, gbc);

        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.03;
        panelDatos.add(lbl(":"), gbc);

        spnMinInicio = crearSpinnerMinutos();
        gbc.gridx = 5; gbc.gridy = 1; gbc.weightx = 0.10;
        panelDatos.add(spnMinInicio, gbc);

        // ── Fila de fecha fin ──
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        panelDatos.add(lbl("Fin *"), gbc);

        spnFechaFin = crearSpinnerFecha(1);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.30;
        panelDatos.add(spnFechaFin, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.05;
        panelDatos.add(lbl("Hora:"), gbc);

        spnHoraFin = crearSpinnerEntero(0, 23, 11);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.10;
        panelDatos.add(spnHoraFin, gbc);

        gbc.gridx = 4; gbc.gridy = 2; gbc.weightx = 0.03;
        panelDatos.add(lbl(":"), gbc);

        spnMinFin = crearSpinnerMinutos();
        gbc.gridx = 5; gbc.gridy = 2; gbc.weightx = 0.10;
        panelDatos.add(spnMinFin, gbc);

        // ── Modalidad y Lugar/URL ──
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        panelDatos.add(lbl("Modalidad *"), gbc);
        cboModalidad = new JComboBox<>(ModalidadCurso.values());
        cboModalidad.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.30;
        panelDatos.add(cboModalidad, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.15; gbc.gridwidth = 2;
        panelDatos.add(lbl("Lugar / URL"), gbc);
        txtLugarUrl = new JTextField();
        txtLugarUrl.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 4; gbc.gridy = 3; gbc.weightx = 0.40; gbc.gridwidth = 2;
        panelDatos.add(txtLugarUrl, gbc);

        // ── Panel shuttle de empleados ──
        JPanel panelEmpleados = new JPanel(new BorderLayout(8, 0));
        panelEmpleados.setBackground(Color.WHITE);
        panelEmpleados.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Empleados asignados *", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        modeloDisponibles = new DefaultListModel<>();
        modeloAsignados   = new DefaultListModel<>();
        listDisponibles   = new JList<>(modeloDisponibles);
        listAsignados     = new JList<>(modeloAsignados);
        listDisponibles.setFont(new Font("Arial", Font.PLAIN, 12));
        listAsignados.setFont(new Font("Arial", Font.PLAIN, 12));
        cargarEmpleados();

        JScrollPane scrollDis = new JScrollPane(listDisponibles);
        scrollDis.setBorder(BorderFactory.createTitledBorder("Disponibles"));
        JScrollPane scrollAsi = new JScrollPane(listAsignados);
        scrollAsi.setBorder(BorderFactory.createTitledBorder("Asignados"));
        scrollDis.setPreferredSize(new Dimension(265, 150));
        scrollAsi.setPreferredSize(new Dimension(265, 150));

        JPanel panelShuttle = new JPanel(new GridLayout(4, 1, 0, 6));
        panelShuttle.setBackground(Color.WHITE);
        panelShuttle.setBorder(new EmptyBorder(25, 4, 25, 4));
        JButton btnAdd    = shuttleBtn(">");
        JButton btnAddAll = shuttleBtn(">>");
        JButton btnRem    = shuttleBtn("<");
        JButton btnRemAll = shuttleBtn("<<");
        btnAdd.addActionListener(e    -> mover(listDisponibles, modeloDisponibles, modeloAsignados));
        btnAddAll.addActionListener(e -> moverTodos(modeloDisponibles, modeloAsignados));
        btnRem.addActionListener(e    -> mover(listAsignados, modeloAsignados, modeloDisponibles));
        btnRemAll.addActionListener(e -> moverTodos(modeloAsignados, modeloDisponibles));
        panelShuttle.add(btnAdd);
        panelShuttle.add(btnAddAll);
        panelShuttle.add(btnRem);
        panelShuttle.add(btnRemAll);

        panelEmpleados.add(scrollDis,    BorderLayout.WEST);
        panelEmpleados.add(panelShuttle, BorderLayout.CENTER);
        panelEmpleados.add(scrollAsi,    BorderLayout.EAST);

        // ── Barra inferior ──
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBackground(Color.WHITE);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        JButton btnGuardar = new JButton("Programar instancia");
        btnGuardar.setBackground(AZUL);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBackground(Color.WHITE);
        sur.add(lblError, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(Color.WHITE);
        centro.add(panelDatos,     BorderLayout.NORTH);
        centro.add(panelEmpleados, BorderLayout.CENTER);

        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(sur,    BorderLayout.SOUTH);
        add(contenido);
    }

    // ── Helpers para spinners ──

    /** Spinner de fecha (sin hora). offsetDias = días desde hoy para el valor inicial. */
    private JSpinner crearSpinnerFecha(int offsetDias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, offsetDias);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        SpinnerDateModel model = new SpinnerDateModel(
            cal.getTime(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        editor.getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
        return spinner;
    }

    /** Spinner numérico entero (para horas 0-23). */
    private JSpinner crearSpinnerEntero(int min, int max, int valorInicial) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valorInicial, min, max, 1));
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "00");
        spinner.setEditor(editor);
        editor.getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
        spinner.setPreferredSize(new Dimension(55, 26));
        return spinner;
    }

    /** Spinner de minutos con step de 5. */
    private JSpinner crearSpinnerMinutos() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 55, 5));
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "00");
        spinner.setEditor(editor);
        editor.getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
        spinner.setPreferredSize(new Dimension(55, 26));
        return spinner;
    }

    /** Convierte los spinners de fecha + hora + minutos a LocalDateTime. */
    private LocalDateTime spinnerALocalDateTime(JSpinner spnFecha, JSpinner spnHora, JSpinner spnMin) {
        Date fecha = (Date) spnFecha.getValue();
        int hora   = (int)  spnHora.getValue();
        int min    = (int)  spnMin.getValue();
        return fecha.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(hora, min);
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    private JButton shuttleBtn(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(48, 28));
        return btn;
    }

    private void cargarCursos() {
        try {
            for (Curso c : ctrl.obtenerCursosActivos()) cboCurso.addItem(c);
        } catch (SQLException ignored) {}
    }

    private void cargarEmpleados() {
        try {
            for (Empleado e : ctrl.obtenerEmpleadosActivos()) modeloDisponibles.addElement(e);
        } catch (SQLException ignored) {}
    }

    private void mover(JList<Empleado> source, DefaultListModel<Empleado> from, DefaultListModel<Empleado> to) {
        List<Empleado> sel = source.getSelectedValuesList();
        for (Empleado e : sel) { from.removeElement(e); to.addElement(e); }
    }

    private void moverTodos(DefaultListModel<Empleado> from, DefaultListModel<Empleado> to) {
        while (!from.isEmpty()) { to.addElement(from.remove(0)); }
    }

    private void guardar() {
        lblError.setText(" ");

        Curso curso = (Curso) cboCurso.getSelectedItem();
        if (curso == null) { lblError.setText("Selecciona un curso."); return; }

        LocalDateTime inicio = spinnerALocalDateTime(spnFechaInicio, spnHoraInicio, spnMinInicio);
        LocalDateTime fin    = spinnerALocalDateTime(spnFechaFin,    spnHoraFin,    spnMinFin);

        List<Integer> idsEmpleados = new ArrayList<>();
        for (int i = 0; i < modeloAsignados.size(); i++)
            idsEmpleados.add(modeloAsignados.get(i).getId());

        InstanciaCapacitacion inst = new InstanciaCapacitacion();
        inst.setIdCurso(curso.getId());
        inst.setFechaInicio(inicio);
        inst.setFechaFin(fin);
        inst.setModalidad((ModalidadCurso) cboModalidad.getSelectedItem());
        inst.setLugarUrl(txtLugarUrl.getText().trim());

        try {
            ctrl.programar(inst, idsEmpleados);
            guardado = true;
            dispose();
        } catch (SQLException ex) {
            lblError.setText(ex.getMessage());
        }
    }

    public boolean isGuardado() { return guardado; }
}
