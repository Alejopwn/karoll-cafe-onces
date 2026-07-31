package Vista;

import Modelo.Login;
import Modelo.LoginDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo desacoplado de Gestión de Personal / Usuarios POS.
 */
public class ModuloUsuarios {

    private final JFrame parentFrame;
    private final LoginDao loginDao = new LoginDao();
    private JPanel panelMain;
    private JTable tableUsuarios;

    public ModuloUsuarios(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void abrirComoVentanaModal() {
        if (panelMain == null) {
            inicializar();
        }
        JDialog dialog = new JDialog(parentFrame, "👥 Gestión de Personal y Usuarios POS", true);
        dialog.setSize(850, 550);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setContentPane(panelMain);
        cargarTabla();
        dialog.setVisible(true);
    }

    private void inicializar() {
        panelMain = new JPanel(new BorderLayout(15, 15));
        panelMain.setBackground(UIUtils.COLOR_BG_DARK);
        panelMain.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("👥 Gestión de Usuarios y Roles de Sistema");
        lblTitle.setFont(Sistema.getFontBold(18f));
        lblTitle.setForeground(Color.WHITE);

        tableUsuarios = new JTable();
        UIUtils.estilarTablaOscura(tableUsuarios);

        JScrollPane scroll = new JScrollPane(tableUsuarios);
        scroll.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setOpaque(false);

        JButton btnNuevo = UIUtils.crearBoton("＋ Nuevo Usuario", UIUtils.COLOR_ACCENT_GREEN);
        btnNuevo.addActionListener(e -> abrirModalNuevoUsuario(null));

        JButton btnEditar = UIUtils.crearBoton("✏️ Editar Seleccionado", UIUtils.COLOR_ACCENT_BLUE);
        btnEditar.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(parentFrame, "Seleccione un usuario de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = Integer.parseInt(tableUsuarios.getValueAt(row, 0).toString());
            Login u = loginDao.buscarUsuarioPorId(id);
            if (u != null) abrirModalNuevoUsuario(u);
        });

        JButton btnRefrescar = UIUtils.crearBoton("🔄 Refrescar", new Color(51, 65, 85));
        btnRefrescar.addActionListener(e -> cargarTabla());

        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnRefrescar);

        panelMain.add(lblTitle, BorderLayout.NORTH);
        panelMain.add(scroll, BorderLayout.CENTER);
        panelMain.add(panelAcciones, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        List<Login> lista = loginDao.ListarUsuarios();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Correo / Usuario", "Rol"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Login u : lista) {
            model.addRow(new Object[]{
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getRol()
            });
        }
        tableUsuarios.setModel(model);
    }

    private void abrirModalNuevoUsuario(Login userEditar) {
        JDialog dialog = new JDialog(parentFrame, userEditar == null ? "Nuevo Usuario" : "Editar Usuario", true);
        dialog.setSize(400, 360);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UIUtils.COLOR_BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setOpaque(false);

        JTextField txtNombre = new JTextField(userEditar != null ? userEditar.getNombre() : "");
        JTextField txtCorreo = new JTextField(userEditar != null ? userEditar.getCorreo() : "");
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cbxRol = new JComboBox<>(new String[]{"Administrador", "Mesero", "Cajero"});
        if (userEditar != null && userEditar.getRol() != null) cbxRol.setSelectedItem(userEditar.getRol());

        JLabel l1 = new JLabel("Nombre Completo:"); l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Correo / Usuario:"); l2.setForeground(Color.WHITE);
        JLabel l3 = new JLabel("Contraseña:"); l3.setForeground(Color.WHITE);
        JLabel l4 = new JLabel("Rol de Sistema:"); l4.setForeground(Color.WHITE);

        form.add(l1); form.add(txtNombre);
        form.add(l2); form.add(txtCorreo);
        form.add(l3); form.add(txtPass);
        form.add(l4); form.add(cbxRol);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnGuardar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String mail = txtCorreo.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String rol = cbxRol.getSelectedItem().toString();

            if (nom.isEmpty() || mail.isEmpty() || (userEditar == null && pass.isEmpty())) {
                JOptionPane.showMessageDialog(dialog, "Complete los campos obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Login u = userEditar != null ? userEditar : new Login();
            u.setNombre(nom);
            u.setCorreo(mail);
            if (!pass.isEmpty()) u.setPass(pass);
            u.setRol(rol);

            boolean ok = userEditar == null ? loginDao.Registrar(u) : loginDao.actualizarUsuario(u);
            if (ok) {
                ToastNotification.exito(parentFrame, "Usuario guardado exitosamente.");
                cargarTabla();
                dialog.dispose();
            } else {
                ToastNotification.error(dialog, "Error al guardar el usuario.");
            }
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        root.add(form, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
