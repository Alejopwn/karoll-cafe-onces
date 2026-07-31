package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Clase utilitaria para estandarizar estilos, colores y componentes de la interfaz gráfica Swing.
 */
public class UIUtils {

    // Paleta de Colores Principal (Dark Theme / Slate)
    public static final Color COLOR_BG_DARK = new Color(15, 23, 42);       // Slate 900
    public static final Color COLOR_PANEL_DARK = new Color(30, 41, 59);    // Slate 800
    public static final Color COLOR_BORDER_DARK = new Color(51, 65, 85);    // Slate 700
    public static final Color COLOR_TEXT_PRIMARY = new Color(241, 245, 249);// Slate 100
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);  // Slate 400

    // Colores de Acento
    public static final Color COLOR_ACCENT_BLUE = new Color(37, 99, 235);
    public static final Color COLOR_ACCENT_GREEN = new Color(16, 185, 129);
    public static final Color COLOR_ACCENT_RED = new Color(220, 38, 38);
    public static final Color COLOR_ACCENT_ORANGE = new Color(245, 158, 11);
    public static final Color COLOR_ACCENT_PURPLE = new Color(139, 92, 246);

    /**
     * Crea un botón estilizado con efectos hover.
     */
    public static JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        final Color bgNormal = bg;
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgNormal.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgNormal);
            }
        });
        return btn;
    }

    /**
     * Crea una tarjeta KPI para paneles de resumen.
     */
    public static JLabel crearKpiCard(String titulo, String valorInicial, Color colorBorde) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(COLOR_PANEL_DARK);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setForeground(COLOR_TEXT_MUTED);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblVal = new JLabel(valorInicial);
        lblVal.setForeground(Color.WHITE);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 20));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return lblVal;
    }

    /**
     * Aplica el estilo oscuro estándar a una JTable.
     */
    public static void estilarTablaOscura(JTable table) {
        table.setRowHeight(32);
        table.setBackground(COLOR_BG_DARK);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setGridColor(COLOR_BORDER_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(COLOR_PANEL_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    /**
     * Estila campos de texto para que se vean modernos y limpios en modo oscuro.
     */
    public static void estilarCampoTexto(javax.swing.text.JTextComponent txt) {
        txt.setBackground(COLOR_PANEL_DARK);
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER_DARK, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    /**
     * Estila ComboBox para modo oscuro.
     */
    public static void estilarCombo(javax.swing.JComboBox<?> cbx) {
        cbx.setBackground(COLOR_PANEL_DARK);
        cbx.setForeground(Color.WHITE);
        cbx.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbx.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_DARK, 1));
    }
}
