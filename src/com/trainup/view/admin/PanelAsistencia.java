package com.trainup.view.admin;

import com.trainup.controller.CtrlAsistencia;
import com.trainup.controller.CtrlInstancia;
import com.trainup.enums.EstadoAsistencia;
import com.trainup.enums.ResultadoCurso;
import com.trainup.model.InstanciaCapacitacion;
import com.trainup.model.RegistroAsistencia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanelAsistencia extends JPanel {

    private final CtrlInstancia  ctrlInst = new CtrlInstancia();
    private final CtrlAsistencia ctrlAsis = new CtrlAsistencia();

    private JList<InstanciaCapacitacion> listInstancias;
    private DefaultListModel<InstanciaCapacitacion> modeloInstancias;
    private JPanel panelFilas;
    private JScrollPane scrollFilas;
    private JButton btnGuardar;
    private JLabel lblTituloTabla, lblEstado;
    private List<FilaAsistencia> filasActuales = new ArrayList<>();

    private static final Color AZUL    = new Color(33, 97, 140);
    private static final Color VERDE   = new Color(39, 120, 70);
    private static final Color ROJO    = new Color(180, 30, 30);
    private static final Color BG_FILA = new Color(250, 252, 255);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelAsistencia() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponentes();
        cargarInstancias();
    }

    private void initComponentes() {
        JLabel lblTitulo = new JLabel("Registro de Asistencia");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 8, 0));

        // ── IZQUIERDA: lista de instancias ──
        JPanel panelIzq = new JPanel(new BorderLayout(0, 6));
        panelIzq.setBackground(Color.WHITE);
        panelIzq.setPreferredSize(new Dimension(260, 0));
        panelIzq.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            "Instancias pendientes", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), AZUL));

        modeloInstancias = new DefaultListModel<>();
        listInstancias = new JList<>(modeloInstancias);
        listInstancias.setFont(new Font("Arial", Font.PLAIN, 12));
        listInstancias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listInstancias.setCellRenderer((list, value, idx, sel, foc) -> {
            boolean editable = value.isEditableAsistencia();
            Color bgNormal = editable ? Color.WHITE : new Color(242, 242, 242);
            Color bgSel    = editable ? new Color(210, 230, 250) : new Color(220, 220, 230);

            JPanel cell = new JPanel(new BorderLayout(0, 2));
            cell.setBackground(sel ? bgSel : bgNormal);
            cell.setBorder(new EmptyBorder(6, 10, 6, 10));

            JLabel nombre = new JLabel(value.getNombreCurso());
            nombre.setFont(new Font("Arial", editable ? Font.BOLD : Font.PLAIN, 12));
            nombre.setForeground(editable ? new Color(30, 30, 30) : new Color(140, 140, 140));

            JPanel subInfo = new JPanel(new BorderLayout());
            subInfo.setOpaque(false);

            JLabel fecha = new JLabel(value.getFechaInicio() != null
                ? value.getFechaInicio().format(FMT) : "");
            fecha.setFont(new Font("Arial", Font.PLAIN, 10));
            fecha.setForeground(editable ? Color.GRAY : new Color(170, 170, 170));

            JLabel estadoLbl = new JLabel(editable ? "  Editable" : "  Bloqueado (>48hs)");
            estadoLbl.setFont(new Font("Arial", Font.BOLD, 9));
            estadoLbl.setForeground(editable ? VERDE : new Color(160, 100, 0));

            subInfo.add(fecha,     BorderLayout.WEST);
            subInfo.add(estadoLbl, BorderLayout.EAST);

            cell.add(nombre,  BorderLayout.CENTER);
            cell.add(subInfo, BorderLayout.SOUTH);
            return cell;
        });
        listInstancias.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarRegistros();
        });
        panelIzq.add(new JScrollPane(listInstancias), BorderLayout.CENTER);

        // ── DERECHA: filas de asistencia ──
        JPanel panelDer = new JPanel(new BorderLayout(0, 8));
        panelDer.setBackground(Color.WHITE);

        lblTituloTabla = new JLabel("Selecciona una instancia para registrar asistencia.");
        lblTituloTabla.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTituloTabla.setForeground(Color.GRAY);
        lblTituloTabla.setBorder(new EmptyBorder(2, 4, 6, 4));

        // Cabecera de columnas
        JPanel cabecera = new JPanel(new GridLayout(1, 5, 0, 0));
        cabecera.setBackground(new Color(235, 240, 248));
        cabecera.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220)),
            new EmptyBorder(5, 10, 5, 10)));
        cabecera.add(colHeader("Empleado"));
        cabecera.add(colHeader("Asistencia"));
        cabecera.add(colHeader("Resultado"));
        cabecera.add(colHeader("Observaciones"));
        cabecera.add(colHeader(""));

        // Panel de filas (se reconstruye al seleccionar instancia)
        panelFilas = new JPanel();
        panelFilas.setLayout(new BoxLayout(panelFilas, BoxLayout.Y_AXIS));
        panelFilas.setBackground(Color.WHITE);

        scrollFilas = new JScrollPane(panelFilas);
        scrollFilas.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 220)));
        scrollFilas.getVerticalScrollBar().setUnitIncrement(16);

        JPanel tablaConCabecera = new JPanel(new BorderLayout());
        tablaConCabecera.add(cabecera,   BorderLayout.NORTH);
        tablaConCabecera.add(scrollFilas, BorderLayout.CENTER);

        // Barra inferior
        btnGuardar = new JButton("Guardar asistencia y calcular IE");
        btnGuardar.setBackground(VERDE);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 13));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setEnabled(false);
        btnGuardar.addActionListener(e -> guardarAsistencia());

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEstado.setForeground(Color.GRAY);

        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setBackground(Color.WHITE);
        sur.setBorder(new EmptyBorder(6, 0, 0, 0));
        sur.add(lblEstado,  BorderLayout.CENTER);
        sur.add(btnGuardar, BorderLayout.EAST);

        panelDer.add(lblTituloTabla,    BorderLayout.NORTH);
        panelDer.add(tablaConCabecera,  BorderLayout.CENTER);
        panelDer.add(sur,               BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(265);
        split.setDividerSize(5);
        split.setBorder(null);

        add(lblTitulo, BorderLayout.NORTH);
        add(split,     BorderLayout.CENTER);
    }

    private JLabel colHeader(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 80, 110));
        return lbl;
    }

    // ── Carga ──

    private void cargarInstancias() {
        try {
            modeloInstancias.clear();
            for (InstanciaCapacitacion inst : ctrlInst.obtenerParaAsistencia())
                modeloInstancias.addElement(inst);
            if (modeloInstancias.isEmpty())
                lblTituloTabla.setText("No hay instancias pasadas registradas.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar instancias: " + e.getMessage());
        }
    }

    private void cargarRegistros() {
        InstanciaCapacitacion sel = listInstancias.getSelectedValue();
        if (sel == null) return;
        try {
            ctrlAsis.asegurarRegistros(sel.getId());
            List<RegistroAsistencia> registros = ctrlAsis.obtenerPorInstancia(sel.getId());
            boolean editable = sel.isEditableAsistencia();

            filasActuales.clear();
            panelFilas.removeAll();

            for (RegistroAsistencia ra : registros) {
                FilaAsistencia fila = new FilaAsistencia(ra, editable);
                filasActuales.add(fila);
                panelFilas.add(fila);
            }

            panelFilas.revalidate();
            panelFilas.repaint();

            String titulo = sel.getNombreCurso() + "  —  " + sel.getFechaInicio().format(FMT)
                + "  (" + registros.size() + " participante" + (registros.size() != 1 ? "s" : "") + ")";
            if (!editable) titulo += "  |  BLOQUEADO — ventana de 48hs vencida";
            lblTituloTabla.setText(titulo);
            lblTituloTabla.setForeground(editable ? new Color(40, 40, 40) : new Color(160, 100, 0));

            btnGuardar.setEnabled(editable && !registros.isEmpty());
            lblEstado.setText(editable
                ? " "
                : "Registro bloqueado por compliance. Solo lectura.");
            lblEstado.setForeground(editable ? Color.GRAY : new Color(160, 100, 0));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar registros: " + e.getMessage());
        }
    }

    // ── Guardar ──

    private void guardarAsistencia() {
        List<RegistroAsistencia> registros = new ArrayList<>();
        for (FilaAsistencia fila : filasActuales) registros.add(fila.getRegistro());

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ctrlAsis.guardarAsistencia(registros);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    lblEstado.setText("Asistencia guardada. Indices de engagement actualizados.");
                    cargarInstancias();
                    filasActuales.clear();
                    panelFilas.removeAll();
                    panelFilas.revalidate();
                    panelFilas.repaint();
                    lblTituloTabla.setText("Selecciona una instancia para registrar asistencia.");
                    btnGuardar.setEnabled(false);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    lblEstado.setText("Error: " + causa.getMessage());
                    btnGuardar.setEnabled(true);
                } finally {
                    btnGuardar.setText("Guardar asistencia y calcular IE");
                }
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════════
    //  Fila de asistencia por empleado
    // ══════════════════════════════════════════════════════════
    private class FilaAsistencia extends JPanel {

        private final RegistroAsistencia registro;
        private final JToggleButton btnAsistio;
        private final JToggleButton btnNoAsistio;
        private final JComboBox<ResultadoCurso> cboResultado;
        private final JTextField txtObs;

        FilaAsistencia(RegistroAsistencia ra, boolean editable) {
            this.registro = ra;
            setLayout(new GridLayout(1, 5, 4, 0));
            Color bgFila = editable ? BG_FILA : new Color(245, 245, 245);
            setBackground(bgFila);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 225, 232)));

            // ── Nombre empleado ──
            JLabel lblNombre = new JLabel("  " + ra.getNombreEmpleado());
            lblNombre.setFont(new Font("Arial", Font.PLAIN, 12));

            // ── Botones Asistio / No asistio ──
            ButtonGroup grupo = new ButtonGroup();

            btnAsistio  = crearToggle("Asistio",   VERDE);
            btnNoAsistio = crearToggle("No asistio", ROJO);
            grupo.add(btnAsistio);
            grupo.add(btnNoAsistio);

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            panelBotones.setBackground(BG_FILA);
            panelBotones.add(btnAsistio);
            panelBotones.add(btnNoAsistio);

            // ── Resultado ──
            cboResultado = new JComboBox<>(new ResultadoCurso[]{
                ResultadoCurso.APROBADO,
                ResultadoCurso.DESAPROBADO,
                ResultadoCurso.PENDIENTE_RESULTADO
            });
            cboResultado.setFont(new Font("Arial", Font.PLAIN, 11));
            cboResultado.setEnabled(false);

            JPanel panelResultado = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            panelResultado.setBackground(BG_FILA);
            panelResultado.add(cboResultado);

            // ── Observaciones ──
            txtObs = new JTextField(ra.getObservaciones() != null ? ra.getObservaciones() : "");
            txtObs.setFont(new Font("Arial", Font.PLAIN, 11));
            txtObs.setEnabled(false);

            JPanel panelObs = new JPanel(new BorderLayout());
            panelObs.setBackground(BG_FILA);
            panelObs.setBorder(new EmptyBorder(8, 4, 8, 4));
            panelObs.add(txtObs, BorderLayout.CENTER);

            // ── Botón limpiar (reset a PENDIENTE) ──
            JButton btnLimpiar = new JButton("Limpiar");
            btnLimpiar.setFont(new Font("Arial", Font.PLAIN, 10));
            btnLimpiar.setFocusPainted(false);
            btnLimpiar.setMargin(new Insets(2, 6, 2, 6));
            btnLimpiar.setToolTipText("Volver a estado pendiente");

            JPanel panelAccion = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 10));
            panelAccion.setBackground(BG_FILA);
            panelAccion.add(btnLimpiar);

            add(lblNombre);
            add(panelBotones);
            add(panelResultado);
            add(panelObs);
            add(panelAccion);

            // ── Pre-seleccionar según estado actual ──
            preSeleccionar(ra);

            // ── Bloquear todo si no es editable ──
            if (!editable) {
                btnAsistio.setEnabled(false);
                btnNoAsistio.setEnabled(false);
                cboResultado.setEnabled(false);
                txtObs.setEnabled(false);
                btnLimpiar.setEnabled(false);
                setBackground(new Color(245, 245, 245));
                lblNombre.setForeground(new Color(120, 120, 120));
            }

            // ── Listeners ──
            btnAsistio.addActionListener(e -> {
                cboResultado.setEnabled(true);
                txtObs.setEnabled(true);
                cboResultado.setSelectedItem(ResultadoCurso.APROBADO);
                actualizarColorFila(new Color(235, 250, 238));
            });

            btnNoAsistio.addActionListener(e -> {
                cboResultado.setEnabled(false);
                cboResultado.setSelectedItem(ResultadoCurso.NO_APLICA);
                txtObs.setEnabled(true);
                actualizarColorFila(new Color(255, 240, 240));
            });

            btnLimpiar.addActionListener(e -> {
                grupo.clearSelection();
                cboResultado.setEnabled(false);
                cboResultado.setSelectedItem(ResultadoCurso.PENDIENTE_RESULTADO);
                txtObs.setEnabled(false);
                txtObs.setText("");
                actualizarColorFila(BG_FILA);
            });
        }

        private JToggleButton crearToggle(String texto, Color color) {
            JToggleButton btn = new JToggleButton(texto);
            btn.setFont(new Font("Arial", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(90, 28));
            btn.setBackground(new Color(230, 230, 230));
            btn.setForeground(Color.DARK_GRAY);

            // Colorear al seleccionar/deseleccionar
            btn.addItemListener(e -> {
                if (btn.isSelected()) {
                    btn.setBackground(color);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(new Color(230, 230, 230));
                    btn.setForeground(Color.DARK_GRAY);
                }
            });
            return btn;
        }

        private void preSeleccionar(RegistroAsistencia ra) {
            switch (ra.getEstadoAsistencia()) {
                case ASISTIO -> {
                    btnAsistio.setSelected(true);
                    cboResultado.setEnabled(true);
                    txtObs.setEnabled(true);
                    if (ra.getResultado() != null &&
                        ra.getResultado() != ResultadoCurso.NO_APLICA &&
                        ra.getResultado() != ResultadoCurso.PENDIENTE_RESULTADO) {
                        cboResultado.setSelectedItem(ra.getResultado());
                    }
                    actualizarColorFila(new Color(235, 250, 238));
                }
                case NO_ASISTIO -> {
                    btnNoAsistio.setSelected(true);
                    cboResultado.setSelectedItem(ResultadoCurso.NO_APLICA);
                    txtObs.setEnabled(true);
                    actualizarColorFila(new Color(255, 240, 240));
                }
                default -> {
                    cboResultado.setSelectedItem(ResultadoCurso.PENDIENTE_RESULTADO);
                    actualizarColorFila(BG_FILA);
                }
            }
        }

        private void actualizarColorFila(Color color) {
            setBackground(color);
            for (Component c : getComponents()) {
                if (c instanceof JPanel p) {
                    p.setBackground(color);
                }
            }
        }

        public RegistroAsistencia getRegistro() {
            if (btnAsistio.isSelected()) {
                registro.setEstadoAsistencia(EstadoAsistencia.ASISTIO);
                registro.setResultado((ResultadoCurso) cboResultado.getSelectedItem());
            } else if (btnNoAsistio.isSelected()) {
                registro.setEstadoAsistencia(EstadoAsistencia.NO_ASISTIO);
                registro.setResultado(ResultadoCurso.NO_APLICA);
            } else {
                registro.setEstadoAsistencia(EstadoAsistencia.PENDIENTE);
                registro.setResultado(ResultadoCurso.PENDIENTE_RESULTADO);
            }
            registro.setObservaciones(txtObs.getText().trim().isEmpty() ? null : txtObs.getText().trim());
            return registro;
        }
    }
}
