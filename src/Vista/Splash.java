package Vista;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class Splash extends JWindow {

    private JProgressBar progressBar;
    private JLabel lblStatus;
    private Timer animTimer;
    private float alpha = 0f;

    public Splash() {
        setSize(480, 280);
        setLocationRelativeTo(null);

        // Fondo principal con gradiente
        JPanel contentPane = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado de fondo
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), getHeight(), new Color(30, 64, 175));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Círculo decorativo superior derecho
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(320, -60, 220, 220);
                // Círculo decorativo inferior izquierdo
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(-80, 160, 200, 200);
                g2.dispose();
            }
        };
        contentPane.setOpaque(false);
        setContentPane(contentPane);

        // Icono decorativo
        JLabel lblEmoji = new JLabel();
        lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        lblEmoji.setHorizontalAlignment(SwingConstants.CENTER);
        lblEmoji.setBounds(0, 30, 480, 70);
        contentPane.add(lblEmoji);

        // Título principal
        JLabel lblTitle = new JLabel("COMUNEROS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 105, 480, 48);
        contentPane.add(lblTitle);

        JLabel lblSub = new JLabel("Puente Nacional");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(new Color(147, 197, 253));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        lblSub.setBounds(0, 150, 480, 24);
        contentPane.add(lblSub);

        // Estado de carga
        lblStatus = new JLabel("Iniciando...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(148, 163, 184));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setBounds(0, 198, 480, 20);
        contentPane.add(lblStatus);

        // Barra de progreso
        progressBar = new JProgressBar() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Track
                g2.setColor(new Color(30, 41, 59));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                // Fill
                if (getValue() > 0) {
                    int w = (int) (getWidth() * getValue() / 100.0);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246), w, 0, new Color(147, 197, 253));
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, getHeight(), 8, 8));
                }
                g2.dispose();
            }
        };
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setBounds(60, 225, 360, 10);
        contentPane.add(progressBar);

        // Borde redondeado de la ventana
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Double(0, 0, 480, 280, 20, 20));
    }

    public void startSplash(Runnable onComplete) {
        setVisible(true);

        new SwingWorker<Void, int[]>() {
            @Override
            protected Void doInBackground() throws Exception {
                // --- Buscar Actualizaciones ---
                boolean actualizando = Modelo.AutoUpdater.checkAndApply((status, percentage) -> {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText(status);
                        progressBar.setValue(percentage);
                    });
                });

                if (actualizando) {
                    // Si se está actualizando, detenemos el flujo normal de carga
                    // ya que el script externo reiniciará el programa.
                    return null;
                }

                publish(new int[]{10, 0}); // {progreso, sleep}
                Thread.sleep(350);

                try { Class.forName("Modelo.Conexion"); } catch (Exception ignored) {}
                publish(new int[]{35, 0});
                Thread.sleep(300);

                publish(new int[]{60, 0});
                Thread.sleep(350);

                publish(new int[]{80, 0});
                Thread.sleep(250);

                publish(new int[]{100, 0});
                Thread.sleep(250);
                return null;
            }

            final String[] mensajes = {
                "Conectando a base de datos...",
                "Verificando estructura...",
                "Cargando componentes...",
                "Iniciando servicios...",
                "Todo listo!"
            };
            int msgIdx = 0;

            @Override
            protected void process(java.util.List<int[]> chunks) {
                int prog = chunks.get(chunks.size() - 1)[0];
                progressBar.setValue(prog);
                if (msgIdx < mensajes.length) {
                    lblStatus.setText(mensajes[msgIdx++]);
                }
            }

            @Override
            protected void done() {
                dispose();
                // Solo llamamos onComplete (mostrar Login) si no se cerró la JVM por actualización
                onComplete.run();
            }
        }.execute();
    }
}
