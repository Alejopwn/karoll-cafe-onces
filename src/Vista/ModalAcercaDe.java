package Vista;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import javax.swing.*;

public class ModalAcercaDe extends JDialog {

    public ModalAcercaDe(Frame parent) {
        super(parent, true);
        setUndecorated(true);
        setSize(520, 465);
        setLocationRelativeTo(parent);

        JPanel contentPane = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Fondo degradado
                GradientPaint bgPaint = new GradientPaint(
                        0, 0, UIUtils.getBgColor(),
                        0, h, UIUtils.getPanelColor()
                );
                g2.setPaint(bgPaint);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));

                // Borde exterior
                g2.setColor(UIUtils.getBorderColor());
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 20, 20));

                // Resplandor detrás del logo
                RadialGradientPaint glow = new RadialGradientPaint(
                        w / 2f, 55f, 65f,
                        new float[]{0f, 0.6f, 1f},
                        new Color[]{
                            new Color(56, 189, 248, 40),
                            new Color(14, 165, 233, 15),
                            new Color(8, 9, 12, 0)
                        }
                );
                g2.setPaint(glow);
                g2.fillOval((w / 2) - 70, 10, 140, 90);

                // Marco de la ficha de información
                g2.setColor(UIUtils.getInputBg());
                g2.fill(new RoundRectangle2D.Float(30, 165, w - 60, 95, 14, 14));
                g2.setColor(UIUtils.getBorderColor());
                g2.draw(new RoundRectangle2D.Float(30, 165, w - 60, 95, 14, 14));

                g2.dispose();
            }
        };
        contentPane.setOpaque(false);
        setContentPane(contentPane);

        // 1. Logo
        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(UIUtils.getLogoSymbol(54));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(0, 18, 520, 56);
        contentPane.add(lblLogo);

        // 2. Título de la marca
        JLabel lblTitle = new JLabel("AS BUSINESS SYSTEMS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UIUtils.getTextPrimary());
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 80, 520, 26);
        contentPane.add(lblTitle);

        // 3. Subtítulo
        JLabel lblSub = new JLabel("ENTERPRISE POINT OF SALE & MANAGEMENT");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSub.setForeground(new Color(148, 163, 184));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(0, 106, 520, 18);
        contentPane.add(lblSub);

        // 4. Chip de versión y estado
        JLabel lblVersion = new JLabel("● Versión " + Modelo.AutoUpdater.CURRENT_VERSION + " • Licencia Comercial Activa");
        lblVersion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVersion.setForeground(new Color(52, 211, 153));
        lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        lblVersion.setBounds(0, 130, 520, 20);
        contentPane.add(lblVersion);

        // 5. Ficha informativa dentro del marco
        int infoY = 175;
        int rowH = 26;

        agregarFilaInfo(contentPane, "Ubicación:", "Bucaramanga, Santander, Colombia", infoY);
        agregarFilaInfo(contentPane, "Edición:", "Restaurante, Bar & Comercio", infoY + rowH);
        agregarFilaInfo(contentPane, "Sincronización:", "Comandas & Meseros en Tiempo Real", infoY + (rowH * 2));

        // 6. Botón de WhatsApp de Soporte
        JButton btnWhatsApp = crearBotonContacto(
                "WhatsApp Soporte: +57 333 6170518",
                new Color(16, 185, 129),
                new Color(6, 78, 59),
                e -> abrirEnlace("https://wa.me/573336170518?text=Hola%2C%20solicito%20soporte%20t%C3%A9cnico%20para%20el%20sistema%20AS%20Business%20Systems")
        );
        btnWhatsApp.setBounds(30, 275, 460, 36);
        contentPane.add(btnWhatsApp);

        // 7. Botón de Correo Electrónico
        JButton btnEmail = crearBotonContacto(
                "Correo: alejohd2020@gmail.com",
                new Color(56, 189, 248),
                new Color(12, 74, 110),
                e -> abrirEnlace("mailto:alejohd2020@gmail.com?subject=Soporte%20Tecnico%20AS%20Business%20Systems")
        );
        btnEmail.setBounds(30, 320, 460, 36);
        contentPane.add(btnEmail);

        // 8. Botón Buscar Actualizaciones y Botón Cerrar
        JButton btnUpdate = UIUtils.crearBoton("Buscar Actualizaciones", UIUtils.COLOR_PANEL_DARK);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBounds(30, 370, 220, 36);
        btnUpdate.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Verificando versión en el servidor...\nVersión actual: " + Modelo.AutoUpdater.CURRENT_VERSION,
                    "Actualizaciones", JOptionPane.INFORMATION_MESSAGE);
        });
        contentPane.add(btnUpdate);

        JButton btnCerrar = UIUtils.crearBoton("Cerrar", new Color(28, 18, 22));
        btnCerrar.setForeground(new Color(244, 63, 94));
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setBounds(270, 370, 220, 36);
        btnCerrar.addActionListener(e -> dispose());
        contentPane.add(btnCerrar);

        // 9. Copyright inferior
        JLabel lblCopy = new JLabel("© 2026 AS Business Systems • Todos los derechos reservados");
        lblCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCopy.setForeground(new Color(100, 116, 139));
        lblCopy.setHorizontalAlignment(SwingConstants.CENTER);
        lblCopy.setBounds(0, 425, 520, 18);
        contentPane.add(lblCopy);

        // Estilo de ventana redondeada
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Double(0, 0, 520, 465, 20, 20));

        // Cerrar con tecla ESC
        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void agregarFilaInfo(JPanel panel, String label, String value, int y) {
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblKey.setForeground(UIUtils.getTextMuted());
        lblKey.setBounds(48, y, 120, 22);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVal.setForeground(UIUtils.getTextPrimary());
        lblVal.setBounds(175, y, 300, 22);

        panel.add(lblKey);
        panel.add(lblVal);
    }

    private JButton crearBotonContacto(String texto, Color textColor, Color bgColor, ActionListener action) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(textColor);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.addActionListener(action);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private void abrirEnlace(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir automáticamente el enlace:\n" + url,
                    "Enlace", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
