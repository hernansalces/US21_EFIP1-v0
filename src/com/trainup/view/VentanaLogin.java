package com.trainup.view;

import com.trainup.auth.CtrlAutenticacion;
import com.trainup.auth.SesionUsuario;
import com.trainup.model.UsuarioSistema;
import com.trainup.view.admin.VentanaPrincipal;
import com.trainup.view.portal.PortalEmpleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public VentanaLogin() {
        initComponentes();
    }

    private void initComponentes() {
        setTitle("TrainUp - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 340);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel exterior con fondo
        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(new Color(240, 244, 248));

        // Encabezado azul
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(33, 97, 140));
        encabezado.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("TrainUp");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Sistema de gestión de capacitaciones");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(180, 210, 230));

        encabezado.add(lblTitulo, BorderLayout.CENTER);
        encabezado.add(lblSubtitulo, BorderLayout.SOUTH);

        // Panel formulario
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(new Color(240, 244, 248));
        formulario.setBorder(new EmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campo usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formulario.add(crearLabel("Usuario:"), gbc);

        txtUsuario = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        formulario.add(txtUsuario, gbc);

        // Campo contraseña
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formulario.add(crearLabel("Contraseña:"), gbc);

        txtPassword = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        formulario.add(txtPassword, gbc);

        // Mensaje de error
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(180, 30, 30));
        lblError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(8, 5, 2, 5);
        formulario.add(lblError, gbc);

        // Botón login
        btnLogin = new JButton("Ingresar");
        btnLogin.setBackground(new Color(33, 97, 140));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 36));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(5, 20, 5, 20);
        formulario.add(btnLogin, gbc);

        btnLogin.addActionListener(this::login);

        // Enter en cualquier campo dispara el login
        txtPassword.addActionListener(this::login);
        txtUsuario.addActionListener(e -> txtPassword.requestFocus());

        fondo.add(encabezado, BorderLayout.NORTH);
        fondo.add(formulario, BorderLayout.CENTER);
        add(fondo);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    private void login(ActionEvent e) {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Ingresá usuario y contraseña.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");
        lblError.setText(" ");

        // Autenticar en hilo separado para no bloquear la UI
        SwingWorker<UsuarioSistema, Void> worker = new SwingWorker<>() {
            @Override
            protected UsuarioSistema doInBackground() {
                return CtrlAutenticacion.autenticar(usuario, password);
            }

            @Override
            protected void done() {
                try {
                    UsuarioSistema usuarioObj = get();
                    if (usuarioObj == null) {
                        lblError.setText("Usuario o contraseña incorrectos.");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    } else {
                        SesionUsuario.iniciar(usuarioObj);
                        dispose();
                        abrirVentanaSegunRol(usuarioObj);
                    }
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    lblError.setText("<html><center>Error: " + causa.getMessage() + "</center></html>");
                    causa.printStackTrace();
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Ingresar");
                }
            }
        };
        worker.execute();
    }

    private void abrirVentanaSegunRol(UsuarioSistema usuario) {
        switch (usuario.getRol()) {
            case EMPLEADO -> new PortalEmpleado().setVisible(true);
            case ADMIN, GERENTE -> new VentanaPrincipal().setVisible(true);
        }
    }
}
