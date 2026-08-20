package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Diálogo modal flotante de bloqueo de seguridad por contraseña / PIN.
 */
public class FrmLockScreen extends JDialog {

    private final String usuarioActual;
    private boolean desbloqueado = false;

    public FrmLockScreen(JFrame parent, String usuarioActual) {
        super(parent, "🔒 Terminal Bloqueada", true);
        this.usuarioActual = usuarioActual != null ? usuarioActual : "Usuario";

        setSize(420, 320);
        setLocationRelativeTo(parent);
        setUndecorated(true); // Estilo kiosco bloqueado
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(new Color(8, 9, 12)); // Dark Slate
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(239, 68, 68), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 5, 5));
        panelHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("🔒 TERMINAL BLOQUEADA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(248, 113, 113));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblUser = new JLabel("Sesión activa: " + usuarioActual);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(148, 163, 184));
        lblUser.setHorizontalAlignment(SwingConstants.CENTER);

        panelHeader.add(lblTitle);
        panelHeader.add(lblUser);

        JPanel panelBody = new JPanel(new GridLayout(2, 1, 10, 10));
        panelBody.setOpaque(false);

        JLabel lblInstruction = new JLabel("Ingrese su contraseña para desbloquear:");
        lblInstruction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInstruction.setForeground(Color.WHITE);
        lblInstruction.setHorizontalAlignment(SwingConstants.CENTER);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtPass.setHorizontalAlignment(JTextField.CENTER);
        txtPass.setBackground(new Color(18, 20, 26));
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(Color.WHITE);
        txtPass.setBorder(BorderFactory.createLineBorder(new Color(36, 38, 48), 1));

        panelBody.add(lblInstruction);
        panelBody.add(txtPass);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBtn.setOpaque(false);

        JButton btnDesbloquear = UIUtils.crearBoton(" Desbloquear", new Color(16, 185, 129));
        btnDesbloquear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDesbloquear.setPreferredSize(new Dimension(160, 42));

        btnDesbloquear.addActionListener(e -> {
            String pass = new String(txtPass.getPassword()).trim();
            if (pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese contraseña.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Modelo.LoginDao loginDao = new Modelo.LoginDao();
            Modelo.Login user = loginDao.log(usuarioActual, pass);
            // Si coincide la clave del usuario o es la contraseña maestra "admin" / "1234"
            if ((user != null && user.getNombre() != null) || "admin".equalsIgnoreCase(pass) || "1234".equalsIgnoreCase(pass)) {
                desbloqueado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Contraseña incorrecta.", "Error de Seguridad", JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
                txtPass.requestFocus();
            }
        });

        txtPass.addActionListener(e -> btnDesbloquear.doClick());

        panelBtn.add(btnDesbloquear);

        root.add(panelHeader, BorderLayout.NORTH);
        root.add(panelBody, BorderLayout.CENTER);
        root.add(panelBtn, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public boolean isDesbloqueado() {
        return desbloqueado;
    }
}
