package Vista;

import java.awt.*;
import javax.swing.*;

/**
 * Diálogo emergente interactivo cuando se dispara una alarma de recordatorio o tarea.
 */
public class ModalAlarma extends JDialog {

    private final String titulo;
    private final String descripcion;
    private final Runnable onAceptar;
    private final Runnable onPosponer;

    public ModalAlarma(Frame parent, String titulo, String descripcion, Runnable onAceptar, Runnable onPosponer) {
        super(parent, "🔔 Alarma POS - Recordatorio", true);
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.onAceptar = onAceptar;
        this.onPosponer = onPosponer;
        initUI();
    }

    private void initUI() {
        setSize(450, 260);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(8, 9, 12));

        // Top Banner
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pTop.setBackground(new Color(245, 158, 11)); // Amber
        JLabel lblIcon = new JLabel("🔔 RECORDATORIO ACTIVO");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblIcon.setForeground(Color.WHITE);
        pTop.add(lblIcon);
        add(pTop, BorderLayout.NORTH);

        // Body
        JPanel pBody = new JPanel(new GridLayout(2, 1, 5, 5));
        pBody.setOpaque(false);
        pBody.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTit = new JLabel("📌 " + titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("<html>" + (descripcion != null ? descripcion : "") + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(203, 213, 225));

        pBody.add(lblTit);
        pBody.add(lblDesc);
        add(pBody, BorderLayout.CENTER);

        // Footer buttons
        JPanel pFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pFooter.setOpaque(false);

        JButton btnPosponer = new JButton("⏱ Posponer 10 min");
        btnPosponer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPosponer.setBackground(new Color(71, 85, 105));
        btnPosponer.setForeground(Color.WHITE);
        btnPosponer.addActionListener(e -> {
            dispose();
            if (onPosponer != null) onPosponer.run();
        });

        JButton btnAceptar = new JButton("✅ Entendido / Listo");
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setBackground(new Color(16, 185, 129));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.addActionListener(e -> {
            dispose();
            if (onAceptar != null) onAceptar.run();
        });

        pFooter.add(btnPosponer);
        pFooter.add(btnAceptar);
        add(pFooter, BorderLayout.SOUTH);

        // Reproducir sonido al aparecer
        SonidoPOS.reproducirMonedas();
    }
}
