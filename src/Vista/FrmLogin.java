package Vista;

import Modelo.LoginDao;
import Modelo.Login;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.Timer;

public class FrmLogin extends javax.swing.JFrame {

    Login lg = new Login();
    LoginDao loginDao = new LoginDao();
    private javax.swing.Timer shakeTimer;
    int shakeCount = 0;

    // Componentes de la interfaz
    private JPanel panelLeft;
    private JPanel panelRight;
    private JLabel lblLogo;
    private JLabel lblSubtitle;
    private JLabel lblUser;
    private JLabel lblPass;
    private JTextField txtCorreo;
    private JPasswordField txtPass;
    private JButton btnIniciar;
    private JButton jButton1;
    public JProgressBar barra;

    public FrmLogin() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 460);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        // Panel izquierdo decorativo
        panelLeft = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(5, 5, 8), getWidth(), getHeight(), new Color(20, 22, 28));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panelLeft.setPreferredSize(new Dimension(290, 460));
        panelLeft.setOpaque(false);

        // Círculos decorativos de fondo
        JLabel decorCircle1 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(0, 0, 200, 200);
                g2.dispose();
            }
        };
        decorCircle1.setBounds(-50, -50, 200, 200);
        panelLeft.add(decorCircle1);

        JLabel decorCircle2 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillOval(0, 0, 180, 180);
                g2.dispose();
            }
        };
        decorCircle2.setBounds(120, 300, 180, 180);
        panelLeft.add(decorCircle2);

        // Logo del establecimiento
        JLabel lblLogoImg = new JLabel();
        try {
            java.awt.Image imgLogo = new javax.swing.ImageIcon(
                getClass().getResource("/Img/as_symbol_clean.png")
            ).getImage().getScaledInstance(95, 95, java.awt.Image.SCALE_SMOOTH);
            lblLogoImg.setIcon(new javax.swing.ImageIcon(imgLogo));
        } catch (Exception ex) {
            lblLogoImg.setText("");
            lblLogoImg.setFont(Sistema.getFontRegular(60f));
        }
        lblLogoImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogoImg.setBounds(0, 65, 290, 105);
        panelLeft.add(lblLogoImg);

        lblLogo = new JLabel("AS BUSINESS");
        lblLogo.setFont(Sistema.getFontBold(24f));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(0, 185, 290, 36);
        panelLeft.add(lblLogo);

        lblSubtitle = new JLabel("BUSINESS SOFTWARE SOLUTIONS");
        lblSubtitle.setFont(Sistema.getFontRegular(11f));
        lblSubtitle.setForeground(new Color(161, 161, 170));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitle.setBounds(0, 220, 290, 22);
        panelLeft.add(lblSubtitle);

        JLabel lblCopyright = new JLabel("© 2026 AS Business Systems");
        lblCopyright.setFont(Sistema.getFontRegular(10f));
        lblCopyright.setForeground(new Color(113, 113, 122));
        lblCopyright.setHorizontalAlignment(SwingConstants.CENTER);
        lblCopyright.setBounds(0, 395, 290, 20);
        panelLeft.add(lblCopyright);

        JLabel lblVersion = new JLabel("Versión " + Modelo.AutoUpdater.CURRENT_VERSION);
        lblVersion.setFont(Sistema.getFontRegular(10f));
        lblVersion.setForeground(new Color(113, 113, 122));
        lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        lblVersion.setBounds(0, 415, 290, 20);
        panelLeft.add(lblVersion);

        // Panel derecho del formulario
        panelRight = new JPanel(null);
        panelRight.setBackground(new Color(10, 11, 14)); // Deep Titanium Black

        JLabel lblBienvenido = new JLabel("Bienvenido");
        lblBienvenido.setFont(Sistema.getFontBold(22f));
        lblBienvenido.setForeground(new Color(255, 255, 255));
        lblBienvenido.setHorizontalAlignment(SwingConstants.CENTER);
        lblBienvenido.setBounds(0, 50, 410, 30);
        panelRight.add(lblBienvenido);

        JLabel lblHint = new JLabel("Ingrese sus credenciales de acceso");
        lblHint.setFont(Sistema.getFontRegular(12f));
        lblHint.setForeground(new Color(161, 161, 170));
        lblHint.setHorizontalAlignment(SwingConstants.CENTER);
        lblHint.setBounds(0, 82, 410, 20);
        panelRight.add(lblHint);

        // Label Correo
        JLabel lblEmailLbl = new JLabel("Correo electrónico");
        lblEmailLbl.setFont(Sistema.getFontBold(12f));
        lblEmailLbl.setForeground(new Color(228, 228, 231)); // Titanium 200
        lblEmailLbl.setBounds(40, 130, 200, 20);
        panelRight.add(lblEmailLbl);

        // Campo correo con borde redondeado
        txtCorreo = new JTextField() {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? Color.WHITE : new Color(38, 40, 50));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        txtCorreo.setFont(Sistema.getFontRegular(14f));
        txtCorreo.setForeground(Color.WHITE);
        txtCorreo.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        txtCorreo.setOpaque(true);
        txtCorreo.setBackground(new Color(20, 22, 28)); // Deep Titanium
        txtCorreo.setCaretColor(Color.WHITE);
        txtCorreo.setBounds(40, 155, 330, 45);
        panelRight.add(txtCorreo);

        // Placeholder
        txtCorreo.setText("usuario@restaurante.com");
        txtCorreo.setForeground(new Color(113, 113, 122));
        txtCorreo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtCorreo.getText().equals("usuario@restaurante.com")) {
                    txtCorreo.setText("");
                    txtCorreo.setForeground(Color.WHITE);
                }
                txtCorreo.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtCorreo.getText().isEmpty()) {
                    txtCorreo.setText("usuario@restaurante.com");
                    txtCorreo.setForeground(new Color(113, 113, 122));
                }
                txtCorreo.repaint();
            }
        });

        // Label Pass
        JLabel lblPassLbl = new JLabel("Contraseña");
        lblPassLbl.setFont(Sistema.getFontBold(12f));
        lblPassLbl.setForeground(new Color(228, 228, 231));
        lblPassLbl.setBounds(40, 215, 200, 20);
        panelRight.add(lblPassLbl);

        // Campo contraseña
        txtPass = new JPasswordField() {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? Color.WHITE : new Color(38, 40, 50));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        txtPass.setFont(Sistema.getFontRegular(14f));
        txtPass.setForeground(new Color(113, 113, 122));
        txtPass.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        txtPass.setBackground(new Color(20, 22, 28));
        txtPass.setCaretColor(Color.WHITE);
        txtPass.setEchoChar((char) 0);
        txtPass.setText("••••••••");
        txtPass.setBounds(40, 240, 330, 45);
        panelRight.add(txtPass);
        txtPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPass.getPassword()).equals("••••••••")) {
                    txtPass.setText("");
                    txtPass.setForeground(Color.WHITE);
                    txtPass.setEchoChar('●');
                }
                txtPass.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPass.getPassword()).isEmpty()) {
                    txtPass.setEchoChar((char) 0);
                    txtPass.setText("••••••••");
                    txtPass.setForeground(new Color(113, 113, 122));
                }
                txtPass.repaint();
            }
        });

        // Barra de progreso (quitada por solicitud - acceso directo)
        barra = new JProgressBar(); // Se mantiene como variable pero NO se agrega al panel

        // Botón Ingresar
        btnIniciar = new JButton("Ingresar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? new Color(212, 212, 216) :
                             getModel().isRollover() ? new Color(244, 244, 245) : Color.WHITE;
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnIniciar.setFont(Sistema.getFontBold(15f));
        btnIniciar.setForeground(Color.BLACK);
        btnIniciar.setContentAreaFilled(false);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIniciar.setBounds(40, 315, 330, 50);
        btnIniciar.addActionListener(e -> validar());
        panelRight.add(btnIniciar);

        // Botón Salir (link-style)
        jButton1 = new JButton("Salir del sistema");
        jButton1.setFont(Sistema.getFontRegular(12f));
        jButton1.setForeground(new Color(113, 113, 122));
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jButton1.setBounds(130, 375, 150, 30);
        jButton1.addActionListener(e -> System.exit(0));
        panelRight.add(jButton1);

        // Atajos de teclado
        txtPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) validar();
            }
        });
        txtCorreo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) txtPass.requestFocus();
            }
        });

        // Ensamblar ventana
        getContentPane().add(panelLeft, BorderLayout.WEST);
        getContentPane().add(panelRight, BorderLayout.CENTER);

        // Borde redondeado de la ventana
        setShape(new RoundRectangle2D.Double(0, 0, 700, 460, 20, 20));
        ImageIcon img = new ImageIcon(getClass().getResource("/Img/Login.png"));
        this.setIconImage(img.getImage());
    }



    private void shakeWindow() {
        int originalX = getX();
        shakeCount = 0;
        shakeTimer = new Timer(25, e -> {
            shakeCount++;
            int offset = (shakeCount % 2 == 0) ? 8 : -8;
            setLocation(originalX + offset, getY());
            if (shakeCount >= 8) {
                ((Timer) e.getSource()).stop();
                setLocation(originalX, getY());
            }
        });
        shakeTimer.start();
    }

    public void validar() {
        String correo = txtCorreo.getText();
        String pass = String.valueOf(txtPass.getPassword());

        if (correo.equals("usuario@restaurante.com") || pass.equals("••••••••") || correo.isEmpty() || pass.isEmpty()) {
            shakeWindow();
            ToastNotification.advertencia(btnIniciar, "Por favor ingrese sus credenciales.");
            return;
        }

        // Feedback de carga en el botón
        btnIniciar.setText("Verificando...");
        btnIniciar.setEnabled(false);

        new SwingWorker<Modelo.Login, Void>() {
            @Override
            protected Modelo.Login doInBackground() {
                return loginDao.log(correo, pass);
            }
            @Override
            protected void done() {
                try {
                    lg = get();
                    if (lg.getCorreo() != null && lg.getPass() != null) {
                        // Acceso directo sin barra de carga
                        btnIniciar.setText("Bienvenido!");
                        javax.swing.Timer t = new javax.swing.Timer(400, null);
                        t.setRepeats(false); // Solo una vez
                        t.addActionListener(evt -> {
                            try {
                                Sistema sis = new Sistema(lg);
                                sis.setVisible(true);
                                dispose();
                            } catch (Throwable ex) {
                                ex.printStackTrace();
                                String errDetail = ex.toString() + (ex.getStackTrace().length > 0 ? " en " + ex.getStackTrace()[0] : "");
                                JOptionPane.showMessageDialog(FrmLogin.this, "Error al abrir sistema:\n" + errDetail, "Error de inicio", JOptionPane.ERROR_MESSAGE);
                                btnIniciar.setText("Ingresar");
                                btnIniciar.setEnabled(true);
                            }
                        });
                        t.start();
                    } else {
                        shakeWindow();
                        ToastNotification.error(btnIniciar, "Correo o contraseña incorrectos");
                        btnIniciar.setText("Ingresar");
                        btnIniciar.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ToastNotification.error(btnIniciar, "Error de conexión con la base de datos");
                    btnIniciar.setText("Ingresar");
                    btnIniciar.setEnabled(true);
                }
            }
        }.execute();
    }

    // Fin de la clase
    // (sin GEN-BEGIN para permitir edición completa)
}
