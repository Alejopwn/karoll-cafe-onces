package Vista;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * Sistema de notificaciones Toast modernas (sin bloqueo, auto-cierre).
 * Reemplaza JOptionPane.showMessageDialog para mensajes de éxito/info/error.
 */
public class ToastNotification extends JWindow {

    public enum Tipo { EXITO, ERROR, ADVERTENCIA, INFO }

    private static final int WIDTH = 340;
    private static final int HEIGHT = 70;

    public ToastNotification(Window parent, String mensaje, Tipo tipo) {
        super(parent);
        setSize(WIDTH, HEIGHT);

        Color bgColor;
        Color iconColor;
        String emoji;
        switch (tipo) {
            case EXITO:     bgColor = new Color(39, 174, 96);  iconColor = Color.WHITE; emoji = "✅"; break;
            case ERROR:     bgColor = new Color(192, 57, 43);  iconColor = Color.WHITE; emoji = "❌"; break;
            case ADVERTENCIA: bgColor = new Color(230, 126, 34); iconColor = Color.WHITE; emoji = "⚠️"; break;
            default:        bgColor = new Color(41, 128, 185); iconColor = Color.WHITE; emoji = "ℹ️"; break;
        }

        // Posicionar abajo a la derecha del parent
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - WIDTH - 20;
        int y = screenSize.height - HEIGHT - 60;
        setLocation(x, y);

        // Panel principal con fondo redondeado
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Icono
        JLabel lblIcono = new JLabel(emoji);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblIcono.setForeground(iconColor);

        // Texto
        JLabel lblTexto = new JLabel("<html><b>" + mensaje + "</b></html>");
        lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTexto.setForeground(iconColor);

        panel.add(lblIcono, BorderLayout.WEST);
        panel.add(lblTexto, BorderLayout.CENTER);

        setContentPane(panel);
        setBackground(new Color(0, 0, 0, 0)); // Fondo transparente
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 16, 16));
    }

    /**
     * Muestra el toast y lo cierra automáticamente después de duración ms.
     */
    public static void mostrar(Component parent, String mensaje, Tipo tipo) {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(parent);
            ToastNotification toast = new ToastNotification(parentWindow, mensaje, tipo);
            toast.setVisible(true);

            // Auto-cierre con Timer
            new javax.swing.Timer(2800, e -> {
                toast.setVisible(false);
                toast.dispose();
            }).start();
        });
    }

    /** Sobrecarga conveniente para botones y paneles */
    public static void exito(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.EXITO);
    }

    public static void error(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.ERROR);
    }

    public static void advertencia(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.ADVERTENCIA);
    }
}
