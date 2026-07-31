package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componente visual interactivo que representa una tarjeta de plato en el menú.
 */
public class TarjetaPlato extends JPanel {

    public TarjetaPlato(int id, String nombre, double precio, Runnable onClick) {
        setLayout(new BorderLayout(6, 4));
        setBackground(new Color(30, 41, 59)); // Slate 800
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 70));

        // Color de acento según rango de precio
        Color accentColor;
        if (precio < 8000) {
            accentColor = new Color(96, 165, 250);   // Azul (económico)
        } else if (precio < 20000) {
            accentColor = new Color(52, 211, 153);   // Verde (estándar)
        } else {
            accentColor = new Color(251, 191, 36);   // Ámbar (premium)
        }

        // Barra lateral de acento
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 4, 4);
                g2.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(4, 0));
        accentBar.setOpaque(false);
        add(accentBar, BorderLayout.WEST);

        // Panel de texto
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 0));

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(Sistema.getFontBold(13f));
        lblNombre.setForeground(new Color(241, 245, 249)); // Slate 100
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(lblNombre);
        textPanel.add(Box.createVerticalStrut(3));

        JLabel lblPrecio = new JLabel(String.format("$%,.0f", precio));
        lblPrecio.setFont(Sistema.getFontBold(12f));
        lblPrecio.setForeground(accentColor);
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(lblPrecio);

        add(textPanel, BorderLayout.CENTER);

        // Icono '+' a la derecha
        JLabel lblAdd = new JLabel("+");
        lblAdd.setFont(Sistema.getFontBold(20f));
        lblAdd.setForeground(new Color(71, 85, 105)); // Slate 600
        lblAdd.setHorizontalAlignment(JLabel.CENTER);
        lblAdd.setPreferredSize(new Dimension(28, 28));
        add(lblAdd, BorderLayout.EAST);

        // Efectos de hover y click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                setBackground(new Color(51, 65, 85)); // Slate 700
                lblAdd.setForeground(Color.WHITE);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                setBackground(new Color(30, 41, 59));
                lblAdd.setForeground(new Color(71, 85, 105));
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent evt) {
                if (onClick != null) onClick.run();
                setBackground(new Color(71, 85, 105)); // Slate 500
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent evt) {
                setBackground(new Color(51, 65, 85));
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
    }
}
