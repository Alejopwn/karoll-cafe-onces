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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
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
        if (panelMain == null) {
            inicializar();
        }
        JDialog dialog = new JDialog(parentFrame, "Administración General del Sistema POS", true);
        dialog.setSize(900, 620);
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
        panelMain.setBackground(UIUtils.COLOR_BG_DARK);
        panelMain.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel lblTitle = new JLabel("Administración del Sistema y Configuración");
        lblTitle.setFont(Sistema.getFontBold(18f));
        lblTitle.setForeground(Color.WHITE);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Sistema.getFontBold(13f));
        tabbedPane.setBackground(UIUtils.COLOR_PANEL_DARK);
        tabbedPane.setForeground(Color.WHITE);

        // Pestañas sin emojis que causaban recuadros vacíos
        tabbedPane.addTab("Datos Empresa", crearPanelEmpresa());
        tabbedPane.addTab("Edición de Salas", crearPanelSalas());
        tabbedPane.addTab("Gestión de Carta", crearPanelPlatos());
        tabbedPane.addTab("Usuarios y Roles", crearPanelUsuarios());

        panelMain.add(lblTitle, BorderLayout.NORTH);
        panelMain.add(tabbedPane, BorderLayout.CENTER);
    }

    // --- 1. PANEL EMPRESA ---
    private JPanel crearPanelEmpresa() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UIUtils.COLOR_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 14));
        form.setOpaque(false);

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

        JLabel l1 = new JLabel("RUC / NIT:"); l1.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l1.setFont(Sistema.getFontBold(13f));
        JLabel l2 = new JLabel("Nombre Restaurante:"); l2.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l2.setFont(Sistema.getFontBold(13f));
        JLabel l3 = new JLabel("Teléfono:"); l3.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l3.setFont(Sistema.getFontBold(13f));
        JLabel l4 = new JLabel("Dirección:"); l4.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l4.setFont(Sistema.getFontBold(13f));
        JLabel l5 = new JLabel("Mensaje Ticket:"); l5.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l5.setFont(Sistema.getFontBold(13f));

        form.add(l1); form.add(txtRuc);
        form.add(l2); form.add(txtNombre);
        form.add(l3); form.add(txtTelefono);
        form.add(l4); form.add(txtDireccion);
        form.add(l5); form.add(txtMensaje);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelBtn.setOpaque(false);

        JButton btnGuardar = UIUtils.crearBoton("Actualizar Datos", UIUtils.COLOR_ACCENT_BLUE);
        btnGuardar.setFont(Sistema.getFontBold(14f));
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
                ToastNotification.exito(parentFrame, "Datos de la empresa actualizados.");
            } else {
                ToastNotification.error(parentFrame, "Error al actualizar configuración.");
            }
        });

        JButton btnBackup = UIUtils.crearBoton("Respaldar Base de Datos", UIUtils.COLOR_ACCENT_GREEN);
        btnBackup.setFont(Sistema.getFontBold(14f));
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

        panelBtn.add(btnBackup);
        panelBtn.add(btnGuardar);
        panel.add(form, BorderLayout.CENTER);
        panel.add(panelBtn, BorderLayout.SOUTH);
        return panel;
    }

    // --- 2. PANEL SALAS ---
    private JPanel crearPanelSalas() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(300, 180));

        txtSalaId = new JTextField();
        txtSalaId.setEditable(false);
        txtSalaId.setVisible(false);

        txtSalaNombre = new JTextField();
        txtSalaMesas = new JTextField();
        UIUtils.estilarCampoTexto(txtSalaNombre);
        UIUtils.estilarCampoTexto(txtSalaMesas);

        JLabel l1 = new JLabel("Nombre de Sala:"); l1.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l1.setFont(Sistema.getFontBold(13f));
        JLabel l2 = new JLabel("Cantidad de Mesas:"); l2.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l2.setFont(Sistema.getFontBold(13f));

        formPanel.add(l1); formPanel.add(txtSalaNombre);
        formPanel.add(l2); formPanel.add(txtSalaMesas);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);

        JButton btnNuevo = UIUtils.crearBoton("Nueva", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.addActionListener(e -> limpiarFormularioSala());

        btnSalaGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnSalaGuardar.addActionListener(e -> guardarOModificarSala());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.addActionListener(e -> eliminarSalaSeleccionada());

        formBtns.add(btnNuevo);
        formBtns.add(btnSalaGuardar);
        formBtns.add(btnEliminar);

        JPanel leftContainer = new JPanel(new BorderLayout(10, 10));
        leftContainer.setOpaque(false);
        leftContainer.add(formPanel, BorderLayout.NORTH);
        leftContainer.add(formBtns, BorderLayout.CENTER);

        tableSalasConfig = new JTable();
        UIUtils.estilarTablaOscura(tableSalasConfig);

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
        scroll.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));

        panel.add(leftContainer, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- 3. PANEL PLATOS ---
    private JPanel crearPanelPlatos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(300, 180));

        txtPlatoId = new JTextField();
        txtPlatoId.setEditable(false);
        txtPlatoId.setVisible(false);

        txtPlatoNombre = new JTextField();
        txtPlatoPrecio = new JTextField();
        UIUtils.estilarCampoTexto(txtPlatoNombre);
        UIUtils.estilarCampoTexto(txtPlatoPrecio);

        JLabel l1 = new JLabel("Nombre del Plato:"); l1.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l1.setFont(Sistema.getFontBold(13f));
        JLabel l2 = new JLabel("Precio (COP):"); l2.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l2.setFont(Sistema.getFontBold(13f));

        formPanel.add(l1); formPanel.add(txtPlatoNombre);
        formPanel.add(l2); formPanel.add(txtPlatoPrecio);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);

        JButton btnNuevo = UIUtils.crearBoton("Nuevo", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.addActionListener(e -> limpiarFormularioPlato());

        btnPlatoGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnPlatoGuardar.addActionListener(e -> guardarOModificarPlato());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.addActionListener(e -> eliminarPlatoSeleccionado());

        formBtns.add(btnNuevo);
        formBtns.add(btnPlatoGuardar);
        formBtns.add(btnEliminar);

        JPanel leftContainer = new JPanel(new BorderLayout(10, 10));
        leftContainer.setOpaque(false);
        leftContainer.add(formPanel, BorderLayout.NORTH);
        leftContainer.add(formBtns, BorderLayout.CENTER);

        tablePlatosConfig = new JTable();
        UIUtils.estilarTablaOscura(tablePlatosConfig);

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
        scroll.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));

        panel.add(leftContainer, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- 4. PANEL USUARIOS ---
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIUtils.COLOR_BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(320, 220));

        txtUserId = new JTextField();
        txtUserId.setEditable(false);
        txtUserId.setVisible(false);

        txtUserNombre = new JTextField();
        txtUserCorreo = new JTextField();
        txtUserPass = new JPasswordField();
        cbxUserRol = new JComboBox<>(new String[]{"Administrador", "Mesero", "Cajero"});

        UIUtils.estilarCampoTexto(txtUserNombre);
        UIUtils.estilarCampoTexto(txtUserCorreo);
        UIUtils.estilarCampoTexto(txtUserPass);
        UIUtils.estilarCombo(cbxUserRol);

        JLabel l1 = new JLabel("Nombre Completo:"); l1.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l1.setFont(Sistema.getFontBold(13f));
        JLabel l2 = new JLabel("Correo / Usuario:"); l2.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l2.setFont(Sistema.getFontBold(13f));
        JLabel l3 = new JLabel("Contraseña:"); l3.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l3.setFont(Sistema.getFontBold(13f));
        JLabel l4 = new JLabel("Rol:"); l4.setForeground(UIUtils.COLOR_TEXT_PRIMARY); l4.setFont(Sistema.getFontBold(13f));

        formPanel.add(l1); formPanel.add(txtUserNombre);
        formPanel.add(l2); formPanel.add(txtUserCorreo);
        formPanel.add(l3); formPanel.add(txtUserPass);
        formPanel.add(l4); formPanel.add(cbxUserRol);

        JPanel formBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        formBtns.setOpaque(false);

        JButton btnNuevo = UIUtils.crearBoton("Nuevo", UIUtils.COLOR_ACCENT_ORANGE);
        btnNuevo.addActionListener(e -> limpiarFormularioUsuario());

        btnUserGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnUserGuardar.addActionListener(e -> guardarOModificarUsuario());

        JButton btnEliminar = UIUtils.crearBoton("Eliminar", UIUtils.COLOR_ACCENT_RED);
        btnEliminar.addActionListener(e -> eliminarUsuarioSeleccionado());

        formBtns.add(btnNuevo);
        formBtns.add(btnUserGuardar);
        formBtns.add(btnEliminar);

        JPanel leftContainer = new JPanel(new BorderLayout(10, 10));
        leftContainer.setOpaque(false);
        leftContainer.add(formPanel, BorderLayout.NORTH);
        leftContainer.add(formBtns, BorderLayout.CENTER);

        tableUsuariosConfig = new JTable();
        UIUtils.estilarTablaOscura(tableUsuariosConfig);

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
        scroll.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));

        panel.add(leftContainer, BorderLayout.WEST);
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
    }

    private void cargarTablaPlatos() {
        List<Plato> lista = platosDao.Listar("");
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Precio (COP)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Plato p : lista) {
            model.addRow(new Object[]{
                p.getId(),
                p.getNombre(),
                String.format("$ %,.2f", p.getPrecio())
            });
        }
        tablePlatosConfig.setModel(model);
    }

    private void cargarTablaUsuarios() {
        List<Login> lista = loginDao.ListarUsuarios();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Correo / Usuario", "Rol"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Login u : lista) {
            model.addRow(new Object[]{ u.getId(), u.getNombre(), u.getCorreo(), u.getRol() });
        }
        tableUsuariosConfig.setModel(model);
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
            JOptionPane.showMessageDialog(parentFrame, "Nombre de Sala y N° de Mesas son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int mesas;
        try {
            mesas = Integer.parseInt(mesasStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentFrame, "El número de mesas debe ser un entero.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(parentFrame, "Seleccione una sala para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(txtSalaId.getText());
        int opt = JOptionPane.showConfirmDialog(parentFrame, "¿Desea eliminar la sala seleccionada?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
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
            JOptionPane.showMessageDialog(parentFrame, "Nombre y Precio son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentFrame, "El precio debe ser un valor numérico válido.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(parentFrame, "Seleccione un plato de la lista para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(txtPlatoId.getText());
        int opt = JOptionPane.showConfirmDialog(parentFrame, "¿Desea eliminar el plato seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
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
            JOptionPane.showMessageDialog(parentFrame, "Nombre, Correo y Contraseña son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(parentFrame, "Seleccione un usuario para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = Integer.parseInt(txtUserId.getText());
        int opt = JOptionPane.showConfirmDialog(parentFrame, "¿Desea eliminar el usuario seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
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
}
