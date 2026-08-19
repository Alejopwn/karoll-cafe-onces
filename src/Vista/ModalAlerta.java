package Vista;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Sistema de Avisos, Alertas y Cuadros de Diálogo Modernos y Estéticos.
 * Sustituye los JOptionPane nativos por diálogos modernos con temática Dark Glassmorphism,
 * bordes redondeados, tipografía estilizada, badges con brillo y botones animados.
 */
public class ModalAlerta extends JDialog {

    public enum TipoAlerta {
        ADVERTENCIA,
        ERROR,
        EXITO,
        INFORMACION,
        CONFIRMACION,
        INPUT
    }

    private boolean respuestaConfirmacion = false;
    private String respuestaInput = null;
    private JTextField txtInput;

    public ModalAlerta(Window parent, String titulo, String mensaje, TipoAlerta tipo, String btnPrincipalTexto, String btnSecundarioTexto, String valorInicialInput) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparente para esquinas redondeadas
        setResizable(false);

        // Colores y acentos según tipo
        Color accentColor;
        Color badgeBg;
        String iconoStr;
        String defaultTitulo = titulo;

        switch (tipo) {
            case ADVERTENCIA:
                accentColor = new Color(245, 158, 11); // Amber
                badgeBg = new Color(245, 158, 11, 40);
                iconoStr = "⚠️";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Atención Requerida";
                break;
            case ERROR:
                accentColor = new Color(239, 68, 68); // Red / Rose
                badgeBg = new Color(239, 68, 68, 40);
                iconoStr = "❌";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Error del Sistema";
                break;
            case EXITO:
                accentColor = new Color(16, 185, 129); // Emerald
                badgeBg = new Color(16, 185, 129, 40);
                iconoStr = "✅";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Operación Exitosa";
                break;
            case CONFIRMACION:
                accentColor = new Color(99, 102, 241); // Indigo
                badgeBg = new Color(99, 102, 241, 40);
                iconoStr = "❓";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Confirmar Acción";
                break;
            case INPUT:
                accentColor = new Color(56, 189, 248); // Cyan
                badgeBg = new Color(56, 189, 248, 40);
                iconoStr = "✏️";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Ingresar Datos";
                break;
            default: // INFORMACION
                accentColor = new Color(56, 189, 248); // Cyan
                badgeBg = new Color(56, 189, 248, 40);
                iconoStr = "ℹ️";
                if (defaultTitulo == null || defaultTitulo.isEmpty()) defaultTitulo = "Información";
                break;
        }

        // Panel Principal con fondo Slate 900 y borde estilizado
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra y fondo oscuro premium
                g2.setColor(new Color(15, 23, 42)); // Slate 900
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
                
                // Borde suave con acento sutil
                g2.setColor(new Color(30, 41, 59)); // Slate 800
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));

                // Acento luminoso superior
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(24, 1, Math.max(20, getWidth() - 48), 3, 2, 2);

                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(22, 24, 20, 24));

        // --- ENCABEZADO (Badge + Título + Botón Cerrar) ---
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setOpaque(false);

        // Badge de Icono Vectorial
        JPanel badgePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(badgeBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 140));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 16, 16));

                g2.setColor(accentColor);
                int cx = w / 2;
                int cy = h / 2;

                switch (tipo) {
                    case EXITO:
                        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 8, cy, cx - 2, cy + 6);
                        g2.drawLine(cx - 2, cy + 6, cx + 8, cy - 6);
                        break;
                    case ERROR:
                        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - 7, cy - 7, cx + 7, cy + 7);
                        g2.drawLine(cx + 7, cy - 7, cx - 7, cy + 7);
                        break;
                    case ADVERTENCIA:
                        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx, cy - 8, cx, cy + 2);
                        g2.fillOval(cx - 2, cy + 6, 5, 5);
                        break;
                    case CONFIRMACION:
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = cx - fm.stringWidth("?") / 2;
                        int ty = cy + fm.getAscent() / 2 - 2;
                        g2.drawString("?", tx, ty);
                        break;
                    case INPUT:
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                        FontMetrics fmIn = g2.getFontMetrics();
                        int txi = cx - fmIn.stringWidth("✎") / 2;
                        int tyi = cy + fmIn.getAscent() / 2 - 2;
                        g2.drawString("✎", txi, tyi);
                        break;
                    default:
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                        FontMetrics fmi = g2.getFontMetrics();
                        int txf = cx - fmi.stringWidth("i") / 2;
                        int tyf = cy + fmi.getAscent() / 2 - 2;
                        g2.drawString("i", txf, tyf);
                        break;
                }

                g2.dispose();
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.setPreferredSize(new Dimension(48, 48));

        // Título
        JLabel lblTitulo = new JLabel(defaultTitulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(new Color(248, 250, 252)); // Slate 50

        // Botón Cerrar "✕" en la esquina
        JButton btnCerrarX = new JButton("✕");
        btnCerrarX.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarX.setForeground(new Color(148, 163, 184));
        btnCerrarX.setBackground(new Color(0, 0, 0, 0));
        btnCerrarX.setBorder(null);
        btnCerrarX.setFocusPainted(false);
        btnCerrarX.setContentAreaFilled(false);
        btnCerrarX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarX.addActionListener(e -> dispose());

        JPanel titleAndClose = new JPanel(new BorderLayout());
        titleAndClose.setOpaque(false);
        titleAndClose.add(lblTitulo, BorderLayout.CENTER);
        titleAndClose.add(btnCerrarX, BorderLayout.EAST);

        headerPanel.add(badgePanel, BorderLayout.WEST);
        headerPanel.add(titleAndClose, BorderLayout.CENTER);

        // --- CUERPO (Mensaje + Input si aplica) ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(10, 2, 10, 2));

        // Formateo del mensaje
        String mensajeFormateado = mensaje != null ? mensaje.replace("\n", "<br>") : "";
        JLabel lblMensaje = new JLabel("<html><body style='width: 380px; color: #cbd5e1; font-family: Segoe UI, sans-serif; font-size: 13px; line-height: 1.45;'>" + mensajeFormateado + "</body></html>");
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(lblMensaje);

        // Campo de entrada si es de tipo INPUT
        if (tipo == TipoAlerta.INPUT) {
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            txtInput = new JTextField(valorInicialInput != null ? valorInicialInput : "");
            txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtInput.setForeground(Color.WHITE);
            txtInput.setBackground(new Color(11, 17, 32));
            txtInput.setCaretColor(accentColor);
            txtInput.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            txtInput.setMaximumSize(new Dimension(440, 40));
            txtInput.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(txtInput);
        }

        // --- BOTONES INFERIORES ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setOpaque(false);

        // Botón Secundario (Cancelar / No)
        if (btnSecundarioTexto != null && !btnSecundarioTexto.isEmpty()) {
            JButton btnSecundario = new JButton(btnSecundarioTexto);
            btnSecundario.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnSecundario.setForeground(new Color(148, 163, 184)); // Slate 400
            btnSecundario.setBackground(new Color(30, 41, 59)); // Slate 800
            btnSecundario.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                    BorderFactory.createEmptyBorder(9, 18, 9, 18)
            ));
            btnSecundario.setFocusPainted(false);
            btnSecundario.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSecundario.addActionListener(e -> {
                respuestaConfirmacion = false;
                respuestaInput = null;
                dispose();
            });
            footerPanel.add(btnSecundario);
        }

        // Botón Principal (Aceptar / Sí / Confirmar)
        String textoBotonP = (btnPrincipalTexto != null && !btnPrincipalTexto.isEmpty()) ? btnPrincipalTexto : "Aceptar";
        JButton btnPrincipal = new JButton(textoBotonP) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnPrincipal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrincipal.setForeground(Color.WHITE);
        btnPrincipal.setOpaque(false);
        btnPrincipal.setContentAreaFilled(false);
        btnPrincipal.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        btnPrincipal.setFocusPainted(false);
        btnPrincipal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrincipal.addActionListener(e -> {
            respuestaConfirmacion = true;
            if (txtInput != null) {
                respuestaInput = txtInput.getText();
            }
            dispose();
        });
        footerPanel.add(btnPrincipal);

        // Teclas rápidas: ENTER -> Principal, ESC -> Cancelar/Cerrar
        mainPanel.registerKeyboardAction(e -> btnPrincipal.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainPanel.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Armar el diálogo
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        
        // Dimensiones mínimas y centrado
        int ancho = Math.max(460, getWidth());
        int alto = Math.max(190, getHeight());
        setSize(ancho, alto);

        if (parent != null) {
            setLocationRelativeTo(parent);
        } else {
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((scr.width - ancho) / 2, (scr.height - alto) / 2);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTODOS ESTÁTICOS CONVENIENTES
    // ─────────────────────────────────────────────────────────────────────────

    public static void advertencia(Component parent, String titulo, String mensaje) {
        mostrarModal(parent, titulo, mensaje, TipoAlerta.ADVERTENCIA, "Entendido", null, null);
    }

    public static void advertencia(Component parent, String mensaje) {
        advertencia(parent, "Atención", mensaje);
    }

    public static void error(Component parent, String titulo, String mensaje) {
        mostrarModal(parent, titulo, mensaje, TipoAlerta.ERROR, "Cerrar", null, null);
    }

    public static void error(Component parent, String mensaje) {
        error(parent, "Error", mensaje);
    }

    public static void exito(Component parent, String titulo, String mensaje) {
        mostrarModal(parent, titulo, mensaje, TipoAlerta.EXITO, "Aceptar", null, null);
    }

    public static void exito(Component parent, String mensaje) {
        exito(parent, "Éxito", mensaje);
    }

    public static void informacion(Component parent, String titulo, String mensaje) {
        mostrarModal(parent, titulo, mensaje, TipoAlerta.INFORMACION, "Aceptar", null, null);
    }

    public static void informacion(Component parent, String mensaje) {
        informacion(parent, "Información", mensaje);
    }

    public static boolean confirmar(Component parent, String titulo, String mensaje, String btnSi, String btnNo) {
        Window w = getWindow(parent);
        ModalAlerta modal = new ModalAlerta(w, titulo, mensaje, TipoAlerta.CONFIRMACION, btnSi, btnNo, null);
        modal.setVisible(true);
        return modal.respuestaConfirmacion;
    }

    public static boolean confirmar(Component parent, String titulo, String mensaje) {
        return confirmar(parent, titulo, mensaje, "Sí, Confirmar", "Cancelar");
    }

    public static boolean confirmar(Component parent, String mensaje) {
        return confirmar(parent, "Confirmar Acción", mensaje, "Sí", "No");
    }

    public static String input(Component parent, String titulo, String mensaje, String valorInicial) {
        Window w = getWindow(parent);
        ModalAlerta modal = new ModalAlerta(w, titulo, mensaje, TipoAlerta.INPUT, "Aceptar", "Cancelar", valorInicial);
        modal.setVisible(true);
        return modal.respuestaInput;
    }

    private static void mostrarModal(Component parent, String titulo, String mensaje, TipoAlerta tipo, String btnP, String btnS, String inputVal) {
        Window w = getWindow(parent);
        ModalAlerta modal = new ModalAlerta(w, titulo, mensaje, tipo, btnP, btnS, inputVal);
        modal.setVisible(true);
    }

    private static Window getWindow(Component c) {
        if (c == null) return null;
        if (c instanceof Window) return (Window) c;
        return SwingUtilities.getWindowAncestor(c);
    }
}
