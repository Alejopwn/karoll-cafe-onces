package Vista;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Notificaciones Toast Modernas y Ultraligeras (Top-Right / Dark Glassmorphism).
 * Dibuja iconos vectoriales nativos de alta definición (sin depender de fuentes emoji),
 * tipografía nítida y posicionamiento superior derecho elegante.
 */
public class ToastNotification extends JWindow {

    public enum Tipo { EXITO, ERROR, ADVERTENCIA, INFO }

    private static final int WIDTH = 380;
    private static final int HEIGHT = 58;

    public ToastNotification(Window parent, String mensaje, Tipo tipo) {
        super(parent);
        setSize(WIDTH, HEIGHT);

        Color accentColor;
        switch (tipo) {
            case EXITO:
                accentColor = new Color(16, 185, 129); // Emerald
                break;
            case ERROR:
                accentColor = new Color(239, 68, 68); // Red / Rose
                break;
            case ADVERTENCIA:
                accentColor = new Color(245, 158, 11); // Amber
                break;
            default:
                accentColor = new Color(56, 189, 248); // Cyan
                break;
        }

        // Posicionamiento moderno: Superior Derecho (Top-Right) de la ventana padre
        if (parent != null && parent.isShowing()) {
            Point pLoc = parent.getLocationOnScreen();
            Dimension pSize = parent.getSize();
            int x = pLoc.x + pSize.width - WIDTH - 24;
            int y = pLoc.y + 68; // Justo debajo de la barra superior
            setLocation(Math.max(10, x), Math.max(10, y));
        } else {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - WIDTH - 24;
            int y = 50;
            setLocation(x, y);
        }

        // Panel principal con dibujo vectorial del fondo y del icono
        JPanel panel = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 1. Fondo Dark Slate (#0f172a / #111827)
                g2.setColor(new Color(15, 23, 42));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                // 2. Borde sutil
                g2.setColor(new Color(30, 41, 59));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 13, 13));

                // 3. Acento luminoso en el borde izquierdo
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(2, 2, 5, h - 4, 4, 4));

                // 4. Badge circular para el icono
                int badgeSize = 34;
                int badgeX = 16;
                int badgeY = (h - badgeSize) / 2;

                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 35));
                g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(badgeX, badgeY, badgeSize, badgeSize);

                // 5. Dibujar icono vectorial nítido
                g2.setColor(accentColor);
                int cx = badgeX + badgeSize / 2;
                int cy = badgeY + badgeSize / 2;

                switch (tipo) {
                    case EXITO:
                        // Checkmark ✓
                        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 6, cy, cx - 2, cy + 5);
                        g2.drawLine(cx - 2, cy + 5, cx + 6, cy - 5);
                        break;
                    case ERROR:
                        // Cruz ✕
                        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                        g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                        break;
                    case ADVERTENCIA:
                        // Signo de exclamación !
                        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx, cy - 6, cx, cy + 1);
                        g2.fillOval(cx - 2, cy + 4, 4, 4);
                        break;
                    default:
                        // Letra 'i'
                        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.fillOval(cx - 2, cy - 7, 4, 4);
                        g2.drawLine(cx, cy - 2, cx, cy + 6);
                        break;
                }

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 62, 8, 16));

        // Texto del Toast limpio y estilizado
        String txtLimpio = mensaje != null ? mensaje.replace("\n", " ") : "";
        JLabel lblTexto = new JLabel("<html><body style='color: #f8fafc; font-family: Segoe UI, -apple-system, sans-serif; font-size: 12px; font-weight: 600; line-height: 1.35;'>" + txtLimpio + "</body></html>");
        panel.add(lblTexto, BorderLayout.CENTER);

        setContentPane(panel);
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 14, 14));
        setAlwaysOnTop(true);
    }

    /**
     * Muestra el toast con auto-cierre suave.
     */
    public static void mostrar(Component parent, String mensaje, Tipo tipo) {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = (parent instanceof Window) ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
            ToastNotification toast = new ToastNotification(parentWindow, mensaje, tipo);
            toast.setVisible(true);

            new javax.swing.Timer(2600, e -> {
                toast.setVisible(false);
                toast.dispose();
            }).start();
        });
    }

    public static void exito(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.EXITO);
    }

    public static void error(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.ERROR);
    }

    public static void advertencia(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.ADVERTENCIA);
    }

    public static void info(Component parent, String mensaje) {
        mostrar(parent, mensaje, Tipo.INFO);
    }
}
