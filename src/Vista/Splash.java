package Vista;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class Splash extends JWindow {

    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JLabel lblPercent;
    private JLabel lblTip;
    private int currentProgress = 0;
    private int targetProgress = 0;
    private Timer progressTimer;
    private float windowOpacity = 0.0f;

    public Splash() {
        setSize(490, 420);
        setLocationRelativeTo(null);

        // Panel principal con fondo de cristal oscuro y bordes redondeados
        JPanel contentPane = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 1. Fondo degradado profundo (Obsidiana a Titanio)
                GradientPaint bgPaint = new GradientPaint(
                        0, 0, new Color(10, 11, 15),
                        0, h, new Color(18, 20, 26)
                );
                g2.setPaint(bgPaint);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));

                // 2. Borde exterior de Titanio
                g2.setColor(new Color(36, 38, 48));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, 24, 24));

                // 3. Marco sutil para el contenedor del video
                g2.setColor(new Color(28, 32, 42));
                g2.draw(new RoundRectangle2D.Float(24, 15, 442, 250, 16, 16));

                // 4. Badge Enterprise en la esquina superior del panel de control
                int badgeW = 200;
                int badgeX = (w - badgeW) / 2;
                int badgeY = 272;
                g2.setColor(new Color(24, 28, 38));
                g2.fill(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, 18, 10, 10));
                g2.setColor(new Color(42, 46, 60));
                g2.draw(new RoundRectangle2D.Float(badgeX, badgeY, badgeW, 18, 10, 10));

                // Punto verde de estado "Online / Activo"
                g2.setColor(new Color(16, 185, 129));
                g2.fillOval(badgeX + 8, badgeY + 5, 8, 8);

                g2.dispose();
            }
        };
        contentPane.setOpaque(false);
        setContentPane(contentPane);

        // 1. Contenedor del video centrado con bordes redondeados suaves
        JLabel lblVideo = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        try {
            ImageIcon iconVideo = new ImageIcon(getClass().getResource("/Img/as_brand_video_splash.gif"));
            lblVideo.setIcon(iconVideo);
        } catch (Exception e) {
            try {
                ImageIcon iconBackup = new ImageIcon(getClass().getResource("/Img/as_logo_animated.gif"));
                lblVideo.setIcon(iconBackup);
            } catch (Exception ex) {}
        }
        lblVideo.setHorizontalAlignment(SwingConstants.CENTER);
        lblVideo.setBounds(25, 16, 440, 248);
        contentPane.add(lblVideo);

        // 2. Badge de Licencia Enterprise
        JLabel lblBadge = new JLabel("ENTERPRISE • RESTAURANTE & BAR");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblBadge.setForeground(new Color(203, 213, 225));
        lblBadge.setHorizontalAlignment(SwingConstants.CENTER);
        lblBadge.setBounds(0, 272, 490, 18);
        contentPane.add(lblBadge);

        // 3. Título corporativo
        JLabel lblTitle = new JLabel("AS BUSINESS SYSTEMS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 294, 490, 22);
        contentPane.add(lblTitle);

        // 4. Estado de carga y porcentaje numérico
        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusPanel.setOpaque(false);
        statusPanel.setBounds(45, 318, 400, 18);

        lblStatus = new JLabel("Iniciando servicios del sistema...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(56, 189, 248));

        lblPercent = new JLabel("0%");
        lblPercent.setFont(new Font("Consolas", Font.BOLD, 12));
        lblPercent.setForeground(Color.WHITE);
        lblPercent.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(lblStatus, BorderLayout.CENTER);
        statusPanel.add(lblPercent, BorderLayout.EAST);
        contentPane.add(statusPanel);

        // 5. Barra de progreso moderna con cabezal brillante
        progressBar = new JProgressBar() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Track
                g2.setColor(new Color(24, 26, 34));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                g2.setColor(new Color(36, 38, 48));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, h, h));

                // Fill
                if (getValue() > 0) {
                    int fillW = (int) ((w - 4) * (getValue() / 100.0));
                    if (fillW > 0) {
                        GradientPaint gp = new GradientPaint(
                                2, 0, new Color(255, 255, 255),
                                fillW, 0, new Color(56, 189, 248)
                        );
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Float(2, 2, fillW, h - 4, h - 4, h - 4));

                        // Glow head en la punta de la barra
                        g2.setColor(Color.WHITE);
                        g2.fillOval(fillW - 2, 1, h, h - 2);
                    }
                }
                g2.dispose();
            }
        };
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setBounds(45, 340, 400, 8);
        contentPane.add(progressBar);

        // 6. Tips y Atajos de teclado rotativos
        lblTip = new JLabel("Tip: Usa [F1] Salas, [F2] Caja, [F3] Inventario y [F5] Configuración.");
        lblTip.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblTip.setForeground(new Color(148, 163, 184));
        lblTip.setHorizontalAlignment(SwingConstants.CENTER);
        lblTip.setBounds(0, 356, 490, 18);
        contentPane.add(lblTip);

        // 7. Pie de versión y copyright
        JLabel lblVer = new JLabel("v" + Modelo.AutoUpdater.CURRENT_VERSION + " • AS Business Systems • Bucaramanga, Santander");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblVer.setForeground(new Color(100, 116, 139));
        lblVer.setHorizontalAlignment(SwingConstants.CENTER);
        lblVer.setBounds(0, 390, 490, 14);
        contentPane.add(lblVer);

        // Ventana redondeada con transparencia y opacidad inicial
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Double(0, 0, 490, 420, 24, 24));

        try {
            setOpacity(0.0f);
        } catch (Exception ignored) {}

        // Timer de interpolación suave de progreso
        progressTimer = new Timer(20, e -> {
            if (currentProgress < targetProgress) {
                currentProgress += 1;
                progressBar.setValue(currentProgress);
                lblPercent.setText(currentProgress + "%");
            }
        });
    }

    public void startSplash(Runnable onComplete) {
        setVisible(true);

        // Fade-In suave
        Timer fadeInTimer = new Timer(20, null);
        fadeInTimer.addActionListener(e -> {
            windowOpacity += 0.08f;
            if (windowOpacity >= 1.0f) {
                windowOpacity = 1.0f;
                fadeInTimer.stop();
            }
            try {
                setOpacity(windowOpacity);
            } catch (Exception ignored) {}
        });
        fadeInTimer.start();

        if (progressTimer != null) {
            progressTimer.start();
        }

        new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws Exception {
                // --- Buscar Actualizaciones ---
                boolean actualizando = Modelo.AutoUpdater.checkAndApply((status, percentage) -> {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText(status);
                        targetProgress = percentage;
                    });
                });

                if (actualizando) {
                    return null;
                }

                // Secuencia con Tips y estados sincronizados con el video (~6.0s)
                publish(new Object[]{15, "Iniciando módulos del sistema...", "Tip: Usa [F1] Salas, [F2] Caja, [F3] Inventario y [F5] Admin."});
                Thread.sleep(1200);

                try { Class.forName("Modelo.Conexion"); } catch (Exception ignored) {}
                publish(new Object[]{40, "Conectando con base de datos...", "Tip: Las comandas de meseros se sincronizan en tiempo real."});
                Thread.sleep(1300);

                publish(new Object[]{65, "Cargando catálogo e inventario...", "Tip: Puedes imprimir comandas a cocina y bar por separado."});
                Thread.sleep(1300);

                publish(new Object[]{85, "Sincronizando mesas y comandas...", "Tip: El cierre de turno calcula arqueos y ventas automáticamente."});
                Thread.sleep(1200);

                publish(new Object[]{100, "¡Bienvenido a AS Business Systems!", "Sistema listo para operar."});
                Thread.sleep(1000);

                return null;
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                if (!chunks.isEmpty()) {
                    Object[] last = chunks.get(chunks.size() - 1);
                    targetProgress = (Integer) last[0];
                    lblStatus.setText((String) last[1]);
                    if (last.length > 2) {
                        lblTip.setText((String) last[2]);
                    }
                }
            }

            @Override
            protected void done() {
                if (progressTimer != null) {
                    progressTimer.stop();
                }

                // Fade-Out suave antes de abrir el Login
                Timer fadeOutTimer = new Timer(20, null);
                fadeOutTimer.addActionListener(e -> {
                    windowOpacity -= 0.08f;
                    if (windowOpacity <= 0.05f) {
                        windowOpacity = 0.0f;
                        fadeOutTimer.stop();
                        dispose();
                        onComplete.run();
                    } else {
                        try {
                            setOpacity(windowOpacity);
                        } catch (Exception ignored) {}
                    }
                });
                fadeOutTimer.start();
            }
        }.execute();
    }
}
