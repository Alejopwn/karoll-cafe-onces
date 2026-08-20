package Vista;

import Modelo.Config;
import Modelo.Login;
import Modelo.LoginDao;
import Modelo.Plato;
import Modelo.PlatosDao;
import Modelo.Sala;
import Modelo.SalasDao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo desacoplado de Configuración General y Administración Unificada POS:
 * Empresa, Edición de Salas, Gestión de Carta y Usuarios/Roles del Sistema.
 */
public class ModuloConfig {

    private final JFrame parentFrame;
    private final LoginDao loginDao = new LoginDao();
    private final PlatosDao platosDao = new PlatosDao();
    private final SalasDao salasDao = new SalasDao();

    private JPanel panelMain;
    private JTabbedPane tabbedPane;

    // 1. Campos de Configuración Empresa
    private JTextField txtRuc;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtMensaje;
    private Config configActual;

    // 2. Campos de Edición de Salas
    private JTable tableSalasConfig;
    private JTextField txtSalaId;
    private JTextField txtSalaNombre;
    private JTextField txtSalaMesas;
    private JButton btnSalaGuardar;

    // 3. Campos de Gestión de Platos (Carta)
    private JTable tablePlatosConfig;
    private JTextField txtPlatoId;
    private JTextField txtPlatoNombre;
    private JTextField txtPlatoPrecio;
    private JButton btnPlatoGuardar;

    // 4. Campos de Gestión de Usuarios
    private JTable tableUsuariosConfig;
    private JTextField txtUserId;
    private JTextField txtUserNombre;
    private JTextField txtUserCorreo;
    private JPasswordField txtUserPass;
    private JComboBox<String> cbxUserRol;
    private JButton btnUserGuardar;

    public ModuloConfig(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void abrirComoVentanaModal() {
        inicializar();
        JDialog dialog = new JDialog(parentFrame, "Administración General del Sistema POS", true);
        dialog.setSize(960, 640);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setContentPane(panelMain);
        cargarDatosEmpresa();
        cargarTablaSalas();
        cargarTablaPlatos();
        cargarTablaUsuarios();
        dialog.setVisible(true);
    }

    private void inicializar() {
        panelMain = new JPanel(new BorderLayout(12, 12));
        panelMain.setBackground(UIUtils.getBgColor());
        panelMain.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Administración del Sistema y Configuración");
        lblTitle.setFont(Sistema.getFontBold(18f));
        lblTitle.setForeground(UIUtils.getTextPrimary());

        JLabel lblSub = new JLabel("Gestiona los parámetros de tu negocio, salas, menú de carta y permisos");
        lblSub.setFont(Sistema.getFontRegular(12f));
        lblSub.setForeground(UIUtils.getTextMuted());

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Sistema.getFontBold(13f));
        tabbedPane.setBackground(UIUtils.getBgColor());
        tabbedPane.setForeground(UIUtils.getTextPrimary());

        tabbedPane.addTab("Datos Empresa", crearPanelEmpresa());
        tabbedPane.addTab("Edición de Salas", crearPanelSalas());
        tabbedPane.addTab("Gestión de Carta", crearPanelPlatos());
        tabbedPane.addTab("Usuarios y Roles", crearPanelUsuarios());
        tabbedPane.addTab("Panel Móvil (Meseros)", crearPanelAndroid());

        panelMain.add(headerPanel, BorderLayout.NORTH);
        panelMain.add(tabbedPane, BorderLayout.CENTER);
    }

    // --- 1. PANEL EMPRESA ---
    private JPanel crearPanelEmpresa() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIUtils.getBgColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Tarjeta central elegante con GridBagLayout
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.getPanelColor());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtRuc = new JTextField();
        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        txtMensaje = new JTextField();

        UIUtils.estilarCampoTexto(txtRuc);
        UIUtils.estilarCampoTexto(txtNombre);
        UIUtils.estilarCampoTexto(txtTelefono);
        UIUtils.estilarCampoTexto(txtDireccion);
        UIUtils.estilarCampoTexto(txtMensaje);

        String[] labels = {
            "RUC / NIT:",
            "Nombre Restaurante:",
            "Teléfono de Contacto:",
            "Dirección:",
            "Mensaje Pie de Ticket:"
        };
        JTextField[] fields = { txtRuc, txtNombre, txtTelefono, txtDireccion, txtMensaje };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Sistema.getFontBold(13f));
            lbl.setForeground(UIUtils.getTextPrimary());
            lbl.setPreferredSize(new Dimension(180, 36));
            card.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            card.add(fields[i], gbc);
        }

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panelBtn.setOpaque(false);

        JButton btnBackup = UIUtils.crearBoton("Respaldar Base de Datos", UIUtils.COLOR_ACCENT_GREEN);
        btnBackup.setPreferredSize(new Dimension(210, 42));
        btnBackup.setFont(Sistema.getFontBold(13f));
        btnBackup.addActionListener(e -> {
            try {
                java.io.File dirBackup = new java.io.File("backups");
                if (!dirBackup.exists()) dirBackup.mkdirs();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
                String destPath = "backups/backup_" + sdf.format(new Date()) + ".db";

                java.io.File src = new java.io.File("restaurante.db");
                if (!src.exists()) {
                    src = new java.io.File("sistema.db");
                }

                if (src.exists()) {
                    java.nio.file.Files.copy(src.toPath(), new java.io.File(destPath).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    ToastNotification.exito(parentFrame, "¡Respaldo creado con éxito en " + destPath + "!");
                } else {
                    ToastNotification.advertencia(parentFrame, "Base de datos local no encontrada para copiar.");
                }
            } catch (Exception ex) {
                ToastNotification.error(parentFrame, "Error al respaldar BD: " + ex.getMessage());
            }
        });

        JButton btnGuardar = UIUtils.crearBoton("Actualizar Datos", UIUtils.COLOR_ACCENT_BLUE);
        btnGuardar.setPreferredSize(new Dimension(170, 42));
        btnGuardar.setFont(Sistema.getFontBold(13f));
        btnGuardar.addActionListener(e -> {
            if (txtRuc.getText().isEmpty() || txtNombre.getText().isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "RUC y Nombre son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Config c = configActual != null ? configActual : new Config();
            c.setRuc(txtRuc.getText().trim());
            c.setNombre(txtNombre.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setDireccion(txtDireccion.getText().trim());
            c.setMensaje(txtMensaje.getText().trim());

            boolean ok = loginDao.ModificarDatos(c);
            if (ok) {
                ToastNotification.exito(parentFrame, "Datos de la empresa actualizados correctamente.");
            } else {
                ToastNotification.error(parentFrame, "Error al actualizar configuración.");
            }
        });

        panelBtn.add(btnBackup);
        panelBtn.add(btnGuardar);

        panel.add(card, BorderLayout.CENTER);
        panel.add(panelBtn, BorderLayout.SOUTH);
        return panel;
    }

    // --- 2. PANEL SALAS ---
    private JPanel crearPanelSalas() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.getBgColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario izquierdo apilado
        JPanel leftCard = new JPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBackground(UIUtils.getPanelColor());
        leftCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(18, 16, 18, 16)
        ));
        leftCard.setPreferredSize(new Dimension(300, 300));

        txtSalaId = new JTextField();
        txtSalaId.setVisible(false);

        JLabel lbl1 = new JLabel("Nombre de Sala:");
        lbl1.setForeground(UIUtils.getTextPrimary());
        lbl1.setFont(Sistema.getFontBold(12f));
        lbl1.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSalaNombre = new JTextField();
        UIUtils.estilarCampoTexto(txtSalaNombre);
        txtSalaNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtSalaNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl2 = new JLabel("Cantidad de Mesas:");
        lbl2.setForeground(UIUtils.getTextPrimary());
        lbl2.setFont(Sistema.getFontBold(12f));
        lbl2.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSalaMesas = new JTextField();
        UIUtils.estilarCampoTexto(txtSalaMesas);
        txtSalaMesas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtSalaMesas.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);
        formBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnNuevo = UIUtils.crearBoton("Nueva", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.setPreferredSize(new Dimension(80, 38));
        btnNuevo.addActionListener(e -> limpiarFormularioSala());

        btnSalaGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnSalaGuardar.setPreferredSize(new Dimension(95, 38));
        btnSalaGuardar.addActionListener(e -> guardarOModificarSala());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.setPreferredSize(new Dimension(85, 38));
        btnEliminar.addActionListener(e -> eliminarSalaSeleccionada());

        formBtns.add(btnNuevo);
        formBtns.add(btnSalaGuardar);
        formBtns.add(btnEliminar);

        leftCard.add(lbl1);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(txtSalaNombre);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(lbl2);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(txtSalaMesas);
        leftCard.add(Box.createVerticalStrut(20));
        leftCard.add(formBtns);
        leftCard.add(Box.createVerticalGlue());

        // Tabla derecha estilizada
        tableSalasConfig = new JTable();
        UIUtils.estilarTabla(tableSalasConfig);
        tableSalasConfig.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UIUtils.getBgColor() : UIUtils.getPanelColor());
                    c.setForeground(UIUtils.getTextPrimary());
                } else {
                    c.setBackground(new Color(37, 99, 235));
                    c.setForeground(Color.WHITE);
                }
                setFont(Sistema.getFontRegular(13f));
                if (col == 0 || col == 2) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (col == 2) setFont(Sistema.getFontBold(13f));
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(Sistema.getFontBold(13f));
                }
                return c;
            }
        });

        tableSalasConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableSalasConfig.getSelectedRow();
                if (row >= 0) {
                    txtSalaId.setText(tableSalasConfig.getValueAt(row, 0).toString());
                    txtSalaNombre.setText(tableSalasConfig.getValueAt(row, 1).toString());
                    txtSalaMesas.setText(tableSalasConfig.getValueAt(row, 2).toString());
                    btnSalaGuardar.setText("Modificar");
                    btnSalaGuardar.setBackground(UIUtils.COLOR_ACCENT_BLUE);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableSalasConfig);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));

        panel.add(leftCard, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- 3. PANEL PLATOS ---
    private JPanel crearPanelPlatos() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.getBgColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario izquierdo apilado
        JPanel leftCard = new JPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBackground(UIUtils.getPanelColor());
        leftCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(18, 16, 18, 16)
        ));
        leftCard.setPreferredSize(new Dimension(300, 300));

        txtPlatoId = new JTextField();
        txtPlatoId.setVisible(false);

        JLabel lbl1 = new JLabel("Nombre del Plato / Producto:");
        lbl1.setForeground(UIUtils.getTextPrimary());
        lbl1.setFont(Sistema.getFontBold(12f));
        lbl1.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPlatoNombre = new JTextField();
        UIUtils.estilarCampoTexto(txtPlatoNombre);
        txtPlatoNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPlatoNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl2 = new JLabel("Precio Unitario (COP):");
        lbl2.setForeground(UIUtils.getTextPrimary());
        lbl2.setFont(Sistema.getFontBold(12f));
        lbl2.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPlatoPrecio = new JTextField();
        UIUtils.estilarCampoTexto(txtPlatoPrecio);
        txtPlatoPrecio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPlatoPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);
        formBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnNuevo = UIUtils.crearBoton("Nuevo", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.setPreferredSize(new Dimension(80, 38));
        btnNuevo.addActionListener(e -> limpiarFormularioPlato());

        btnPlatoGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnPlatoGuardar.setPreferredSize(new Dimension(95, 38));
        btnPlatoGuardar.addActionListener(e -> guardarOModificarPlato());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.setPreferredSize(new Dimension(85, 38));
        btnEliminar.addActionListener(e -> eliminarPlatoSeleccionado());

        formBtns.add(btnNuevo);
        formBtns.add(btnPlatoGuardar);
        formBtns.add(btnEliminar);

        leftCard.add(lbl1);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(txtPlatoNombre);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(lbl2);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(txtPlatoPrecio);
        leftCard.add(Box.createVerticalStrut(20));
        leftCard.add(formBtns);
        leftCard.add(Box.createVerticalGlue());

        // Tabla derecha estilizada
        tablePlatosConfig = new JTable();
        UIUtils.estilarTabla(tablePlatosConfig);
        tablePlatosConfig.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UIUtils.getBgColor() : UIUtils.getPanelColor());
                    c.setForeground(UIUtils.getTextPrimary());
                } else {
                    c.setBackground(new Color(37, 99, 235));
                    c.setForeground(Color.WHITE);
                }
                setFont(Sistema.getFontRegular(13f));
                if (col == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col == 1) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(Sistema.getFontBold(13f));
                } else if (col == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(Sistema.getFontBold(13f));
                    if (!isSelected) c.setForeground(new Color(56, 189, 248)); // Cyan
                }
                return c;
            }
        });

        tablePlatosConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tablePlatosConfig.getSelectedRow();
                if (row >= 0) {
                    txtPlatoId.setText(tablePlatosConfig.getValueAt(row, 0).toString());
                    txtPlatoNombre.setText(tablePlatosConfig.getValueAt(row, 1).toString());
                    txtPlatoPrecio.setText(tablePlatosConfig.getValueAt(row, 2).toString().replace("$", "").replace(",", "").trim());
                    btnPlatoGuardar.setText("Modificar");
                    btnPlatoGuardar.setBackground(UIUtils.COLOR_ACCENT_BLUE);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablePlatosConfig);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));

        panel.add(leftCard, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- 4. PANEL USUARIOS ---
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.getBgColor());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario izquierdo apilado
        JPanel leftCard = new JPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
        leftCard.setBackground(UIUtils.getPanelColor());
        leftCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(18, 16, 18, 16)
        ));
        leftCard.setPreferredSize(new Dimension(300, 380));

        txtUserId = new JTextField();
        txtUserId.setVisible(false);

        txtUserNombre = new JTextField();
        txtUserCorreo = new JTextField();
        txtUserPass = new JPasswordField();
        cbxUserRol = new JComboBox<>(new String[]{"Administrador", "Mesero", "Cajero"});

        UIUtils.estilarCampoTexto(txtUserNombre);
        UIUtils.estilarCampoTexto(txtUserCorreo);
        UIUtils.estilarCampoTexto(txtUserPass);
        UIUtils.estilarCombo(cbxUserRol);

        txtUserNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtUserCorreo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtUserPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbxUserRol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel l1 = new JLabel("Nombre Completo:"); l1.setForeground(UIUtils.getTextPrimary()); l1.setFont(Sistema.getFontBold(12f)); l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l2 = new JLabel("Correo / Usuario:"); l2.setForeground(UIUtils.getTextPrimary()); l2.setFont(Sistema.getFontBold(12f)); l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l3 = new JLabel("Contraseña:"); l3.setForeground(UIUtils.getTextPrimary()); l3.setFont(Sistema.getFontBold(12f)); l3.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l4 = new JLabel("Rol / Perfil:"); l4.setForeground(UIUtils.getTextPrimary()); l4.setFont(Sistema.getFontBold(12f)); l4.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUserNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUserCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUserPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbxUserRol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);
        formBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnNuevo = UIUtils.crearBoton("Nuevo", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.setPreferredSize(new Dimension(80, 38));
        btnNuevo.addActionListener(e -> limpiarFormularioUsuario());

        btnUserGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnUserGuardar.setPreferredSize(new Dimension(95, 38));
        btnUserGuardar.addActionListener(e -> guardarOModificarUsuario());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.setPreferredSize(new Dimension(85, 38));
        btnEliminar.addActionListener(e -> eliminarUsuarioSeleccionado());

        formBtns.add(btnNuevo);
        formBtns.add(btnUserGuardar);
        formBtns.add(btnEliminar);

        leftCard.add(l1); leftCard.add(Box.createVerticalStrut(4)); leftCard.add(txtUserNombre);
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(l2); leftCard.add(Box.createVerticalStrut(4)); leftCard.add(txtUserCorreo);
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(l3); leftCard.add(Box.createVerticalStrut(4)); leftCard.add(txtUserPass);
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(l4); leftCard.add(Box.createVerticalStrut(4)); leftCard.add(cbxUserRol);
        leftCard.add(Box.createVerticalStrut(16));
        leftCard.add(formBtns);
        leftCard.add(Box.createVerticalGlue());

        // Tabla derecha estilizada
        tableUsuariosConfig = new JTable();
        UIUtils.estilarTabla(tableUsuariosConfig);
        tableUsuariosConfig.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UIUtils.getBgColor() : UIUtils.getPanelColor());
                    c.setForeground(UIUtils.getTextPrimary());
                } else {
                    c.setBackground(new Color(37, 99, 235));
                    c.setForeground(Color.WHITE);
                }
                setFont(Sistema.getFontRegular(13f));
                if (col == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col == 1) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(Sistema.getFontBold(13f));
                } else if (col == 3) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(Sistema.getFontBold(13f));
                    if (!isSelected) c.setForeground(new Color(251, 191, 36)); // Gold
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        tableUsuariosConfig.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableUsuariosConfig.getSelectedRow();
                if (row >= 0) {
                    txtUserId.setText(tableUsuariosConfig.getValueAt(row, 0).toString());
                    txtUserNombre.setText(tableUsuariosConfig.getValueAt(row, 1).toString());
                    txtUserCorreo.setText(tableUsuariosConfig.getValueAt(row, 2).toString());
                    if (tableUsuariosConfig.getValueAt(row, 3) != null) {
                        cbxUserRol.setSelectedItem(tableUsuariosConfig.getValueAt(row, 3).toString());
                    }
                    btnUserGuardar.setText("Modificar");
                    btnUserGuardar.setBackground(UIUtils.COLOR_ACCENT_BLUE);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableUsuariosConfig);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));

        panel.add(leftCard, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- MÉTODOS DE CARGA ---
    private void cargarDatosEmpresa() {
        configActual = loginDao.datosEmpresa();
        if (configActual != null) {
            txtRuc.setText(configActual.getRuc());
            txtNombre.setText(configActual.getNombre());
            txtTelefono.setText(configActual.getTelefono());
            txtDireccion.setText(configActual.getDireccion());
            txtMensaje.setText(configActual.getMensaje());
        }
    }

    private void cargarTablaSalas() {
        List<Sala> lista = salasDao.Listar();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre Sala", "N° Mesas"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Sala s : lista) {
            model.addRow(new Object[]{ s.getId(), s.getNombre(), s.getMesas() });
        }
        tableSalasConfig.setModel(model);
        if (tableSalasConfig.getColumnModel().getColumnCount() > 0) {
            tableSalasConfig.getColumnModel().getColumn(0).setMaxWidth(60);
            tableSalasConfig.getColumnModel().getColumn(0).setPreferredWidth(50);
            tableSalasConfig.getColumnModel().getColumn(2).setPreferredWidth(90);
        }
    }

    private void cargarTablaPlatos() {
        List<Plato> lista = platosDao.Listar("");
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre del Plato", "Precio (COP)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Plato p : lista) {
            model.addRow(new Object[]{
                p.getId(),
                p.getNombre(),
                String.format("$ %,.0f", p.getPrecio())
            });
        }
        tablePlatosConfig.setModel(model);
        if (tablePlatosConfig.getColumnModel().getColumnCount() > 0) {
            tablePlatosConfig.getColumnModel().getColumn(0).setMaxWidth(60);
            tablePlatosConfig.getColumnModel().getColumn(0).setPreferredWidth(50);
            tablePlatosConfig.getColumnModel().getColumn(2).setPreferredWidth(120);
        }
    }

    private void cargarTablaUsuarios() {
        List<Login> lista = loginDao.ListarUsuarios();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre Completo", "Correo / Usuario", "Rol"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Login u : lista) {
            model.addRow(new Object[]{ u.getId(), u.getNombre(), u.getCorreo(), u.getRol() });
        }
        tableUsuariosConfig.setModel(model);
        if (tableUsuariosConfig.getColumnModel().getColumnCount() > 0) {
            tableUsuariosConfig.getColumnModel().getColumn(0).setMaxWidth(60);
            tableUsuariosConfig.getColumnModel().getColumn(0).setPreferredWidth(50);
            tableUsuariosConfig.getColumnModel().getColumn(3).setPreferredWidth(120);
        }
    }

    // --- ACCIONES SALAS ---
    private void limpiarFormularioSala() {
        txtSalaId.setText("");
        txtSalaNombre.setText("");
        txtSalaMesas.setText("");
        btnSalaGuardar.setText("Guardar");
        btnSalaGuardar.setBackground(UIUtils.COLOR_ACCENT_GREEN);
        tableSalasConfig.clearSelection();
    }

    private void guardarOModificarSala() {
        String nom = txtSalaNombre.getText().trim();
        String mesasStr = txtSalaMesas.getText().trim();

        if (nom.isEmpty() || mesasStr.isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Nombre de Sala y N° de Mesas son obligatorios.");
            return;
        }

        int mesas;
        try {
            mesas = Integer.parseInt(mesasStr);
        } catch (NumberFormatException e) {
            ToastNotification.error(parentFrame, "El número de mesas debe ser un entero.");
            return;
        }

        Sala s = new Sala();
        s.setNombre(nom);
        s.setMesas(mesas);

        if (txtSalaId.getText().isEmpty()) {
            boolean ok = salasDao.RegistrarSala(s);
            if (ok) {
                ToastNotification.exito(parentFrame, "Sala registrada correctamente.");
                limpiarFormularioSala();
                cargarTablaSalas();
            } else {
                ToastNotification.error(parentFrame, "Error al registrar la sala.");
            }
        } else {
            s.setId(Integer.parseInt(txtSalaId.getText()));
            boolean ok = salasDao.Modificar(s);
            if (ok) {
                ToastNotification.exito(parentFrame, "Sala modificada correctamente.");
                limpiarFormularioSala();
                cargarTablaSalas();
            } else {
                ToastNotification.error(parentFrame, "Error al modificar la sala.");
            }
        }
    }

    private void eliminarSalaSeleccionada() {
        if (txtSalaId.getText().isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Seleccione una sala para eliminar.");
            return;
        }
        int id = Integer.parseInt(txtSalaId.getText());
        boolean opt = ModalAlerta.confirmar(parentFrame, "Eliminar Sala", "¿Desea eliminar la sala seleccionada?", "Sí, Eliminar", "Cancelar");
        if (opt) {
            boolean ok = salasDao.Eliminar(id);
            if (ok) {
                ToastNotification.exito(parentFrame, "Sala eliminada.");
                limpiarFormularioSala();
                cargarTablaSalas();
            } else {
                ToastNotification.error(parentFrame, "Error al eliminar la sala.");
            }
        }
    }

    // --- ACCIONES PLATOS ---
    private void limpiarFormularioPlato() {
        txtPlatoId.setText("");
        txtPlatoNombre.setText("");
        txtPlatoPrecio.setText("");
        btnPlatoGuardar.setText("Guardar");
        btnPlatoGuardar.setBackground(UIUtils.COLOR_ACCENT_GREEN);
        tablePlatosConfig.clearSelection();
    }

    private void guardarOModificarPlato() {
        String nom = txtPlatoNombre.getText().trim();
        String precioStr = txtPlatoPrecio.getText().trim();

        if (nom.isEmpty() || precioStr.isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Nombre y Precio son obligatorios.");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            ToastNotification.error(parentFrame, "El precio debe ser un valor numérico válido.");
            return;
        }

        Plato p = new Plato();
        p.setNombre(nom);
        p.setPrecio(precio);
        p.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        if (txtPlatoId.getText().isEmpty()) {
            boolean ok = platosDao.Registrar(p);
            if (ok) {
                ToastNotification.exito(parentFrame, "Plato registrado exitosamente.");
                limpiarFormularioPlato();
                cargarTablaPlatos();
            } else {
                ToastNotification.error(parentFrame, "Error al registrar el plato.");
            }
        } else {
            p.setId(Integer.parseInt(txtPlatoId.getText()));
            boolean ok = platosDao.Modificar(p);
            if (ok) {
                ToastNotification.exito(parentFrame, "Plato modificado correctamente.");
                limpiarFormularioPlato();
                cargarTablaPlatos();
            } else {
                ToastNotification.error(parentFrame, "Error al modificar el plato.");
            }
        }
    }

    private void eliminarPlatoSeleccionado() {
        if (txtPlatoId.getText().isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Seleccione un plato de la lista para eliminar.");
            return;
        }
        int id = Integer.parseInt(txtPlatoId.getText());
        boolean opt = ModalAlerta.confirmar(parentFrame, "Eliminar Plato", "¿Desea eliminar el plato seleccionado?", "Sí, Eliminar", "Cancelar");
        if (opt) {
            boolean ok = platosDao.Eliminar(id);
            if (ok) {
                ToastNotification.exito(parentFrame, "Plato eliminado.");
                limpiarFormularioPlato();
                cargarTablaPlatos();
            } else {
                ToastNotification.error(parentFrame, "Error al eliminar el plato.");
            }
        }
    }

    // --- ACCIONES USUARIOS ---
    private void limpiarFormularioUsuario() {
        txtUserId.setText("");
        txtUserNombre.setText("");
        txtUserCorreo.setText("");
        txtUserPass.setText("");
        btnUserGuardar.setText("Guardar");
        btnUserGuardar.setBackground(UIUtils.COLOR_ACCENT_GREEN);
        tableUsuariosConfig.clearSelection();
    }

    private void guardarOModificarUsuario() {
        String nom = txtUserNombre.getText().trim();
        String mail = txtUserCorreo.getText().trim();
        String pass = new String(txtUserPass.getPassword()).trim();
        String rol = cbxUserRol.getSelectedItem().toString();

        if (nom.isEmpty() || mail.isEmpty() || (txtUserId.getText().isEmpty() && pass.isEmpty())) {
            ToastNotification.advertencia(parentFrame, "Nombre, Correo y Contraseña son obligatorios.");
            return;
        }

        Login u = new Login();
        u.setNombre(nom);
        u.setCorreo(mail);
        if (!pass.isEmpty()) u.setPass(pass);
        u.setRol(rol);

        if (txtUserId.getText().isEmpty()) {
            boolean ok = loginDao.Registrar(u);
            if (ok) {
                ToastNotification.exito(parentFrame, "Usuario registrado exitosamente.");
                limpiarFormularioUsuario();
                cargarTablaUsuarios();
            } else {
                ToastNotification.error(parentFrame, "Error al registrar el usuario.");
            }
        } else {
            u.setId(Integer.parseInt(txtUserId.getText()));
            boolean ok = loginDao.actualizarUsuario(u);
            if (ok) {
                ToastNotification.exito(parentFrame, "Usuario modificado correctamente.");
                limpiarFormularioUsuario();
                cargarTablaUsuarios();
            } else {
                ToastNotification.error(parentFrame, "Error al modificar el usuario.");
            }
        }
    }

    private void eliminarUsuarioSeleccionado() {
        if (txtUserId.getText().isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Seleccione un usuario para eliminar.");
            return;
        }
        int id = Integer.parseInt(txtUserId.getText());
        boolean opt = ModalAlerta.confirmar(parentFrame, "Eliminar Usuario", "¿Desea eliminar el usuario seleccionado?", "Sí, Eliminar", "Cancelar");
        if (opt) {
            boolean ok = loginDao.eliminarUsuario(id);
            if (ok) {
                ToastNotification.exito(parentFrame, "Usuario eliminado.");
                limpiarFormularioUsuario();
                cargarTablaUsuarios();
            } else {
                ToastNotification.error(parentFrame, "No se puede eliminar el usuario (posiblemente tiene pedidos registrados).");
            }
        }
    }

    // --- 5. PANEL CELULARES / ANDROID ---
    private JPanel crearPanelAndroid() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(UIUtils.getBgColor());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIUtils.getPanelColor());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Estado del Servidor
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlStatus.setOpaque(false);
        JLabel lblDot = new JLabel("●");
        lblDot.setFont(Sistema.getFontBold(20f));
        lblDot.setForeground(new Color(16, 185, 129)); // Verde Esmeralda
        JLabel lblStatusText = new JLabel("Servidor Web Activo en Red Local (Puerto " + ServidorWebMesero.getPuerto() + ")");
        lblStatusText.setFont(Sistema.getFontBold(14f));
        lblStatusText.setForeground(UIUtils.getTextPrimary());
        pnlStatus.add(lblDot);
        pnlStatus.add(lblStatusText);

        gbc.gridy = 0;
        card.add(pnlStatus, gbc);

        // Caja destacada de la URL
        JLabel lblUrlTitle = new JLabel("DIRECCIÓN DE ACCESO PARA CELULARES (CHROME / SAFARI):");
        lblUrlTitle.setFont(Sistema.getFontBold(12f));
        lblUrlTitle.setForeground(UIUtils.getTextMuted());
        gbc.gridy = 1;
        card.add(lblUrlTitle, gbc);

        JTextField txtUrl = new JTextField(ServidorWebMesero.getUrlAcceso());
        txtUrl.setEditable(false);
        txtUrl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtUrl.setForeground(UIUtils.IS_DARK ? new Color(56, 189, 248) : new Color(2, 132, 199));
        txtUrl.setBackground(UIUtils.getInputBg());
        txtUrl.setHorizontalAlignment(JTextField.CENTER);
        txtUrl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.IS_DARK ? new Color(56, 189, 248) : new Color(2, 132, 199), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        gbc.gridy = 2;
        card.add(txtUrl, gbc);

        // Botones de acción de la URL
        JPanel pnlBtnsUrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        pnlBtnsUrl.setOpaque(false);

        JButton btnCopiar = UIUtils.crearBoton("Copiar Enlace", UIUtils.COLOR_ACCENT_BLUE);
        btnCopiar.setPreferredSize(new Dimension(160, 40));
        btnCopiar.setFont(Sistema.getFontBold(13f));
        btnCopiar.addActionListener(e -> {
            String url = ServidorWebMesero.getUrlAcceso();
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(url);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            ToastNotification.exito(parentFrame, "¡URL copiada al portapapeles!");
        });

        JButton btnAbrir = UIUtils.crearBoton("Abrir en Navegador", UIUtils.COLOR_ACCENT_GREEN);
        btnAbrir.setPreferredSize(new Dimension(180, 40));
        btnAbrir.setFont(Sistema.getFontBold(13f));
        btnAbrir.addActionListener(e -> {
            try {
                String url = ServidorWebMesero.getUrlAcceso();
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                }
            } catch (Exception ex) {
                ToastNotification.error(parentFrame, "Error al abrir navegador: " + ex.getMessage());
            }
        });

        JButton btnReiniciar = UIUtils.crearBoton("Reiniciar Servidor", UIUtils.COLOR_ACCENT_ORANGE);
        btnReiniciar.setPreferredSize(new Dimension(170, 40));
        btnReiniciar.setFont(Sistema.getFontBold(13f));
        btnReiniciar.addActionListener(e -> {
            ServidorWebMesero.detener();
            ServidorWebMesero.iniciar();
            txtUrl.setText(ServidorWebMesero.getUrlAcceso());
            ToastNotification.exito(parentFrame, "Servidor web reiniciado en " + ServidorWebMesero.getUrlAcceso());
        });

        pnlBtnsUrl.add(btnCopiar);
        pnlBtnsUrl.add(btnAbrir);
        pnlBtnsUrl.add(btnReiniciar);

        gbc.gridy = 3;
        card.add(pnlBtnsUrl, gbc);

        // Tarjeta de Instrucciones y Módulos
        JPanel pnlPasos = new JPanel(new GridLayout(4, 1, 6, 8));
        pnlPasos.setOpaque(false);
        pnlPasos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel p1 = new JLabel("● 1. App Meseros: Abrir la dirección principal para tomar pedidos en mesas.");
        p1.setFont(Sistema.getFontBold(13f));
        p1.setForeground(new Color(56, 189, 248));

        JLabel p2 = new JLabel("● 2. Pantalla Cocina (KDS): Ingresar a " + ServidorWebMesero.getUrlAcceso() + "/cocina en la tablet de cocina.");
        p2.setFont(Sistema.getFontBold(13f));
        p2.setForeground(new Color(251, 191, 36));

        JLabel p3 = new JLabel("● 3. Menú Digital Clientes: Abrir " + ServidorWebMesero.getUrlAcceso() + "/menu para consultar la carta.");
        p3.setFont(Sistema.getFontBold(13f));
        p3.setForeground(new Color(52, 211, 153));

        JLabel p4 = new JLabel("● Requisito: Los celulares y tablets deben estar conectados a la misma red WiFi del POS.");
        p4.setFont(Sistema.getFontRegular(12f));
        p4.setForeground(UIUtils.getTextMuted());

        pnlPasos.add(p1);
        pnlPasos.add(p2);
        pnlPasos.add(p3);
        pnlPasos.add(p4);

        gbc.gridy = 4;
        card.add(pnlPasos, gbc);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }
}
