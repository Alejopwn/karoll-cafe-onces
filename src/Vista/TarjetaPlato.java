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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Componente visual interactivo que representa una tarjeta de plato en el menú,
 * totalmente adaptativo a los temas Claro y Oscuro.
 */
public class TarjetaPlato extends JPanel {

    private boolean isHovered = false;

    public TarjetaPlato(int id, String nombre, double precio, Runnable onClick) {
        setLayout(new BorderLayout(8, 4));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 72));

        // Color de acento según rango de precio y tema
        Color accentColor;
        if (UIUtils.IS_DARK) {
            if (precio < 8000) {
                accentColor = new Color(96, 165, 250);   // Azul
            } else if (precio < 20000) {
                accentColor = new Color(52, 211, 153);   // Verde
            } else {
                accentColor = new Color(251, 191, 36);   // Ámbar
            }
        } else {
            if (precio < 8000) {
                accentColor = new Color(37, 99, 235);    // Azul
            } else if (precio < 20000) {
                accentColor = new Color(5, 150, 105);    // Verde
            } else {
                accentColor = new Color(217, 119, 6);    // Ámbar
            }
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
        textPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 0));

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(Sistema.getFontBold(13f));
        lblNombre.setForeground(UIUtils.getTextPrimary());
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(lblNombre);
        textPanel.add(Box.createVerticalStrut(4));

        JLabel lblPrecio = new JLabel(String.format("$%,.0f", precio));
        lblPrecio.setFont(Sistema.getFontBold(12.5f));
        lblPrecio.setForeground(accentColor);
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(lblPrecio);

        add(textPanel, BorderLayout.CENTER);

        // Icono '+' a la derecha
        JLabel lblAdd = new JLabel("+");
        lblAdd.setFont(Sistema.getFontBold(20f));
        lblAdd.setForeground(UIUtils.getTextMuted());
        lblAdd.setHorizontalAlignment(JLabel.CENTER);
        lblAdd.setPreferredSize(new Dimension(32, 32));
        add(lblAdd, BorderLayout.EAST);

        // Efectos de hover y click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                isHovered = true;
                lblAdd.setForeground(UIUtils.getTextPrimary());
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                isHovered = false;
                lblAdd.setForeground(UIUtils.getTextMuted());
                repaint();
            }
            @Override
            public void mousePressed(MouseEvent evt) {
                if (onClick != null) onClick.run();
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent evt) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isHovered ? UIUtils.getCardHover() : UIUtils.getPanelColor());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(isHovered ? (UIUtils.IS_DARK ? new Color(56, 189, 248) : new Color(37, 99, 235)) : UIUtils.getBorderColor());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
    }
}
