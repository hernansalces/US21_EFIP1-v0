package com.trainup.view.admin;

import com.trainup.controller.CtrlEmpleado;
import com.trainup.model.Empleado;
import com.trainup.model.PerfilCarrera;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

public class FormularioEmpleado extends JDialog {

    public enum Modo { NUEVO, EDITAR }

    private final Modo modo;
    private final Empleado empleado;
    private final CtrlEmpleado ctrl;
    private boolean guardado = false;

    private JTextField txtNombre, txtApellido, txtDni, txtEmail, txtTelefono;
    private JTextField txtFechaIngreso, txtPuesto, txtArea;
    private JComboBox<PerfilCarrera> cboPerfil;
    private JLabel lblError;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color AZUL     = new Color(33, 97, 140);
    private static final Color NARANJA  = new Color(230, 120, 0);

    // Teléfono: opcional, pero si se completa debe tener formato válido
    // Acepta: dígitos, espacios, +, -, (, ) — mínimo 7 y máximo 20 caracteres
    private static final Pattern PATRON_TEL = Pattern.compile("^[+]?[\\d\\s\\-().]{7,20}$");

    public FormularioEmpleado(Frame parent, Modo modo, Empleado empleado, CtrlEmpleado ctrl) {
        super(parent, modo == Modo.NUEVO ? "Nuevo empleado" : "Editar empleado", true);
        this.modo = modo;
        this.empleado = empleado != null ? empleado : new Empleado();
        this.ctrl = ctrl;
        initComponentes();
        if (modo == Modo.EDITAR) precargarDatos();
    }

    private void initComponentes() {
        setSize(520, 570);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setBorder(new EmptyBorder(12, 20, 12, 20));
        contenido.setBackground(Color.WHITE);

        // Banner de empleado inactivo (solo en EDITAR de inactivos)
        if (modo == Modo.EDITAR && !empleado.isActivo()) {
            JPanel bannerInactivo = new JPanel(new BorderLayout(10, 0));
            bannerInactivo.setBackground(new Color(255, 243, 220));
            bannerInactivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NARANJA),
                new EmptyBorder(8, 12, 8, 12)));

            JLabel lblAviso = new JLabel("⚠️  Este empleado está inactivo (dado de baja el "
                + (empleado.getFechaBaja() != null ? empleado.getFechaBaja().format(FMT_FECHA) : "?") + ")");
            lblAviso.setFont(new Font("Arial", Font.BOLD, 12));
            lblAviso.setForeground(new Color(150, 80, 0));

            JButton btnReactivar = new JButton("Reactivar empleado");
            btnReactivar.setBackground(NARANJA);
            btnReactivar.setForeground(Color.WHITE);
            btnReactivar.setFont(new Font("Arial", Font.BOLD, 11));
            btnReactivar.setFocusPainted(false);
            btnReactivar.setBorderPainted(false);
            btnReactivar.addActionListener(e -> reactivar());

            bannerInactivo.add(lblAviso, BorderLayout.CENTER);
            bannerInactivo.add(btnReactivar, BorderLayout.EAST);
            contenido.add(bannerInactivo, BorderLayout.NORTH);
        }

        // Panel de campos
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Datos del empleado",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre       = agregarCampo(campos, gbc, "Nombre *",                    0);
        txtApellido     = agregarCampo(campos, gbc, "Apellido *",                  1);
        txtDni          = agregarCampo(campos, gbc, "DNI *",                       2);
        txtEmail        = agregarCampo(campos, gbc, "Email *",                     3);
        txtTelefono     = agregarCampo(campos, gbc, "Teléfono  (ej: +54 9 11…)",  4);
        txtFechaIngreso = agregarCampo(campos, gbc, "Fecha ingreso * (dd/MM/yyyy)", 5);
        txtPuesto       = agregarCampo(campos, gbc, "Puesto *",                    6);
        txtArea         = agregarCampo(campos, gbc, "Área *",                      7);

        // Combo perfil de carrera
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.35;
        JLabel lblPerfil = new JLabel("Perfil de carrera");
        lblPerfil.setFont(new Font("Arial", Font.PLAIN, 12));
        campos.add(lblPerfil, gbc);

        cboPerfil = new JComboBox<>();
        cboPerfil.setFont(new Font("Arial", Font.PLAIN, 12));
        cboPerfil.addItem(null);
        cargarPerfiles();
        gbc.gridx = 1; gbc.gridy = 8; gbc.weightx = 0.65;
        campos.add(cboPerfil, gbc);

        // Label de error
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setBackground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = new JButton(modo == Modo.NUEVO ? "Guardar empleado" : "Guardar cambios");
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

        if (modo == Modo.NUEVO) {
            txtFechaIngreso.setText(LocalDate.now().format(FMT_FECHA));
        }
    }

    private JTextField agregarCampo(JPanel panel, GridBagConstraints gbc, String etiqueta, int fila) {
        gbc.gridx = 0; gbc.gridy = fila; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(lbl, gbc);

        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = fila; gbc.weightx = 0.65;
        panel.add(txt, gbc);
        return txt;
    }

    private void cargarPerfiles() {
        try {
            for (PerfilCarrera p : ctrl.obtenerPerfiles()) cboPerfil.addItem(p);
        } catch (SQLException e) {
            // Si no se pueden cargar perfiles, el combo queda solo con la opción vacía
        }
    }

    private void precargarDatos() {
        txtNombre.setText(empleado.getNombre());
        txtApellido.setText(empleado.getApellido());
        txtDni.setText(empleado.getDni());
        txtEmail.setText(empleado.getEmail());
        txtTelefono.setText(empleado.getTelefono() != null ? empleado.getTelefono() : "");
        txtFechaIngreso.setText(empleado.getFechaIngreso().format(FMT_FECHA));
        txtPuesto.setText(empleado.getPuesto());
        txtArea.setText(empleado.getArea());

        if (empleado.getIdPerfil() != null) {
            for (int i = 0; i < cboPerfil.getItemCount(); i++) {
                PerfilCarrera p = cboPerfil.getItemAt(i);
                if (p != null && p.getId() == empleado.getIdPerfil()) {
                    cboPerfil.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void guardar() {
        lblError.setText(" ");

        String nombre    = txtNombre.getText().trim();
        String apellido  = txtApellido.getText().trim();
        String dni       = txtDni.getText().trim();
        String email     = txtEmail.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String fechaStr  = txtFechaIngreso.getText().trim();
        String puesto    = txtPuesto.getText().trim();
        String area      = txtArea.getText().trim();

        // Campos obligatorios
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
            email.isEmpty() || fechaStr.isEmpty() || puesto.isEmpty() || area.isEmpty()) {
            lblError.setText("Completá todos los campos obligatorios (*).");
            return;
        }

        // Email básico
        if (!email.contains("@") || !email.contains(".")) {
            lblError.setText("El formato del email no es válido.");
            return;
        }

        // Teléfono: si se completó, validar formato
        if (!telefono.isEmpty() && !PATRON_TEL.matcher(telefono).matches()) {
            lblError.setText("Teléfono inválido. Usá solo dígitos, espacios, +, -, ( ). Mínimo 7 caracteres.");
            return;
        }

        // Fecha
        LocalDate fechaIngreso;
        try {
            fechaIngreso = LocalDate.parse(fechaStr, FMT_FECHA);
        } catch (DateTimeParseException ex) {
            lblError.setText("La fecha debe tener formato dd/MM/yyyy.");
            return;
        }

        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setDni(dni);
        empleado.setEmail(email);
        empleado.setTelefono(telefono.isEmpty() ? null : telefono);
        empleado.setFechaIngreso(fechaIngreso);
        empleado.setPuesto(puesto);
        empleado.setArea(area);
        PerfilCarrera perfilSel = (PerfilCarrera) cboPerfil.getSelectedItem();
        empleado.setIdPerfil(perfilSel != null ? perfilSel.getId() : null);

        try {
            if (modo == Modo.NUEVO) {
                ctrl.registrar(empleado);
            } else {
                ctrl.actualizar(empleado);
            }
            guardado = true;
            dispose();

        } catch (SQLException ex) {
            // Si es duplicado de DNI o email, verificar si hay un inactivo para ofrecer reactivación
            String msg = ex.getMessage();
            if (modo == Modo.NUEVO && (msg.contains("DNI") || msg.contains("email"))) {
                ofrecerReactivacion(dni, email, msg);
            } else {
                lblError.setText(msg);
            }
        }
    }

    /**
     * Cuando el alta falla por duplicado, busca si el conflicto es con un empleado inactivo
     * y ofrece reactivarlo en vez de crear uno nuevo.
     */
    private void ofrecerReactivacion(String dni, String email, String msgOriginal) {
        try {
            Empleado inactivo = ctrl.buscarInactivoPorDni(dni);
            if (inactivo == null) inactivo = ctrl.buscarInactivoPorEmail(email);

            if (inactivo == null) {
                // El duplicado es con un empleado activo → mostrar error normal
                lblError.setText(msgOriginal);
                return;
            }

            String msg = "<html>Ya existe un empleado inactivo con ese dato:<br>" +
                         "<b>" + inactivo.getNombreCompleto() + "</b> (dado de baja: " +
                         (inactivo.getFechaBaja() != null ? inactivo.getFechaBaja().format(FMT_FECHA) : "?") + ")<br><br>" +
                         "¿Querés reactivar ese empleado en lugar de crear uno nuevo?</html>";

            int resp = JOptionPane.showConfirmDialog(this, msg,
                "Empleado inactivo encontrado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (resp == JOptionPane.YES_OPTION) {
                ctrl.reactivar(inactivo.getId());
                guardado = true;
                dispose();
            }
        } catch (SQLException e) {
            lblError.setText("Error al verificar empleados inactivos.");
        }
    }

    /** Reactivar desde el banner del formulario de edición */
    private void reactivar() {
        String msg = "<html>¿Reactivar a <b>" + empleado.getNombreCompleto() + "</b>?<br>" +
                     "El empleado volverá a estar activo en el sistema.</html>";
        int resp = JOptionPane.showConfirmDialog(this, msg,
            "Confirmar reactivación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ctrl.reactivar(empleado.getId());
                guardado = true;
                dispose();
            } catch (SQLException e) {
                lblError.setText("Error al reactivar: " + e.getMessage());
            }
        }
    }

    public boolean isGuardado() { return guardado; }
}
