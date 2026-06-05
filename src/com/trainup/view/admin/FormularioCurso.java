package com.trainup.view.admin;

import com.trainup.controller.CtrlCurso;
import com.trainup.enums.ModalidadCurso;
import com.trainup.model.CapacitadorExterno;
import com.trainup.model.Curso;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FormularioCurso extends JDialog {

    public enum Modo { NUEVO, EDITAR }

    private final Modo modo;
    private final Curso curso;
    private final CtrlCurso ctrl;
    private boolean guardado = false;

    private JTextField txtNombre, txtDescripcion, txtCategoria;
    private JSpinner spnDuracion;
    private JComboBox<ModalidadCurso> cboModalidad;
    private JComboBox<CapacitadorExterno> cboCapacitador;
    private JLabel lblError;

    private static final Color AZUL = new Color(33, 97, 140);

    public FormularioCurso(Frame parent, Modo modo, Curso curso, CtrlCurso ctrl) {
        super(parent, modo == Modo.NUEVO ? "Nuevo curso" : "Editar curso", true);
        this.modo = modo;
        this.curso = curso != null ? curso : new Curso();
        this.ctrl = ctrl;
        initComponentes();
        if (modo == Modo.EDITAR) precargarDatos();
    }

    private void initComponentes() {
        setSize(480, 420);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(new EmptyBorder(15, 20, 15, 20));
        contenido.setBackground(Color.WHITE);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Datos del curso", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre      = agregarCampo(campos, gbc, "Nombre *", 0);
        txtDescripcion = agregarCampo(campos, gbc, "Descripción *", 1);
        txtCategoria   = agregarCampo(campos, gbc, "Categoría temática", 2);

        // Duración en horas (spinner numérico)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        campos.add(etiqueta("Duración (horas) *"), gbc);
        spnDuracion = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 500.0, 0.5));
        spnDuracion.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.65;
        campos.add(spnDuracion, gbc);

        // Modalidad
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        campos.add(etiqueta("Modalidad *"), gbc);
        cboModalidad = new JComboBox<>(ModalidadCurso.values());
        cboModalidad.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.65;
        campos.add(cboModalidad, gbc);

        // Capacitador
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        campos.add(etiqueta("Capacitador"), gbc);
        cboCapacitador = new JComboBox<>();
        cboCapacitador.setFont(new Font("Arial", Font.PLAIN, 12));
        cboCapacitador.addItem(null);
        cargarCapacitadores();
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.65;
        campos.add(cboCapacitador, gbc);

        lblError = new JLabel(" ");
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = new JButton(modo == Modo.NUEVO ? "Guardar curso" : "Guardar cambios");
        btnGuardar.setBackground(AZUL);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.addActionListener(e -> guardar());

        botones.add(btnCancelar);
        botones.add(btnGuardar);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setBackground(Color.WHITE);
        centro.add(campos, BorderLayout.CENTER);
        centro.add(lblError, BorderLayout.SOUTH);

        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        add(contenido);
    }

    private JTextField agregarCampo(JPanel panel, GridBagConstraints gbc, String etiqueta, int fila) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.35;
        panel.add(etiqueta(etiqueta), gbc);
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = fila; gbc.weightx = 0.65;
        panel.add(txt, gbc);
        return txt;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        return lbl;
    }

    private void cargarCapacitadores() {
        try {
            for (CapacitadorExterno c : ctrl.obtenerCapacitadores()) cboCapacitador.addItem(c);
        } catch (SQLException ignored) {}
    }

    private void precargarDatos() {
        txtNombre.setText(curso.getNombre());
        txtDescripcion.setText(curso.getDescripcion() != null ? curso.getDescripcion() : "");
        txtCategoria.setText(curso.getCategoriaTematica() != null ? curso.getCategoriaTematica() : "");
        if (curso.getDuracionHoras() != null) spnDuracion.setValue(curso.getDuracionHoras().doubleValue());
        cboModalidad.setSelectedItem(curso.getModalidad());
        if (curso.getIdCapacitador() != null) {
            for (int i = 0; i < cboCapacitador.getItemCount(); i++) {
                CapacitadorExterno c = cboCapacitador.getItemAt(i);
                if (c != null && c.getId() == curso.getIdCapacitador()) {
                    cboCapacitador.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void guardar() {
        lblError.setText(" ");

        String nombre     = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            lblError.setText("Nombre y descripción son obligatorios.");
            return;
        }

        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setCategoriaTematica(txtCategoria.getText().trim());
        curso.setDuracionHoras(BigDecimal.valueOf((Double) spnDuracion.getValue()));
        curso.setModalidad((ModalidadCurso) cboModalidad.getSelectedItem());
        CapacitadorExterno cap = (CapacitadorExterno) cboCapacitador.getSelectedItem();
        curso.setIdCapacitador(cap != null ? cap.getId() : null);

        try {
            if (modo == Modo.NUEVO) ctrl.registrar(curso);
            else ctrl.actualizar(curso);
            guardado = true;
            dispose();
        } catch (SQLException ex) {
            lblError.setText(ex.getMessage());
        }
    }

    public boolean isGuardado() { return guardado; }
}
