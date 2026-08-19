package Modelo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderizador moderno para tablas del POS.
 * Aplica fondo neutro con alternancia cebra elegante y badges visuales
 * únicamente en la columna de estado para evitar saturación visual.
 */
public class EstiloTablas extends DefaultTableCellRenderer {

    private static final Color BG_EVEN = new Color(24, 32, 47);
    private static final Color BG_ODD = new Color(15, 23, 42);
    private static final Color BG_HOVER = new Color(51, 65, 85);
    private static final Color BG_SELECTED = new Color(37, 99, 235);
    private static final Color TEXT_MAIN = new Color(241, 245, 249);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Colores de badges de estado
    private static final Color BADGE_FINALIZADO_BG = new Color(20, 83, 45); // Verde oscuro
    private static final Color BADGE_FINALIZADO_FG = new Color(74, 222, 128); // Verde brillante
    private static final Color BADGE_PREPARADO_BG = new Color(120, 53, 15); // Ámbar oscuro
    private static final Color BADGE_PREPARADO_FG = new Color(252, 211, 77); // Ámbar brillante
    private static final Color BADGE_PENDIENTE_BG = new Color(127, 29, 29); // Rojo oscuro
    private static final Color BADGE_PENDIENTE_FG = new Color(248, 113, 113); // Rojo claro
    private static final Color BADGE_ANULADO_BG = new Color(51, 65, 85); // Gris
    private static final Color BADGE_ANULADO_FG = new Color(148, 163, 184);

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(jtable, o, isSelected, hasFocus, row, col);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Alineación por columna
        if (col == 0 || col == 3) {
            // ID o N° Mesa
            label.setHorizontalAlignment(SwingConstants.CENTER);
        } else if (col == 5) {
            // Total monto
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else if (col == 6) {
            // Estado
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
        }

        // Estado del pedido (columna 6)
        String estado = "";
        if (jtable.getModel().getColumnCount() > 6 && row < jtable.getRowCount()) {
            Object estObj = jtable.getValueAt(row, 6);
            if (estObj != null) estado = estObj.toString().trim().toUpperCase();
        }

        // Si es la columna de Estado, aplicamos estilo Badge tipo Chip
        if (col == 6) {
            if (isSelected) {
                label.setBackground(BG_SELECTED);
                label.setForeground(Color.WHITE);
            } else if (estado.contains("FINALIZADO")) {
                label.setBackground(BADGE_FINALIZADO_BG);
                label.setForeground(BADGE_FINALIZADO_FG);
                label.setText("● " + estado);
            } else if (estado.contains("PREPARADO")) {
                label.setBackground(BADGE_PREPARADO_BG);
                label.setForeground(BADGE_PREPARADO_FG);
                label.setText("🟡 " + estado);
            } else if (estado.contains("PENDIENTE")) {
                label.setBackground(BADGE_PENDIENTE_BG);
                label.setForeground(BADGE_PENDIENTE_FG);
                label.setText("⏱ " + estado);
            } else if (estado.contains("ANULADO")) {
                label.setBackground(BADGE_ANULADO_BG);
                label.setForeground(BADGE_ANULADO_FG);
                label.setText("✕ " + estado);
            } else {
                label.setBackground(row % 2 == 0 ? BG_EVEN : BG_ODD);
                label.setForeground(TEXT_MAIN);
            }
            return label;
        }

        // Para el resto de columnas: Fondo Cebra Limpio o Selección
        if (isSelected) {
            label.setBackground(BG_SELECTED);
            label.setForeground(Color.WHITE);
        } else {
            java.awt.Point p = jtable.getMousePosition();
            int hoverRow = p != null ? jtable.rowAtPoint(p) : -1;
            if (row == hoverRow) {
                label.setBackground(BG_HOVER);
            } else {
                label.setBackground(row % 2 == 0 ? BG_EVEN : BG_ODD);
            }
            label.setForeground(col == 5 ? new Color(56, 189, 248) : TEXT_MAIN); // Total en color cian claro
        }

        return label;
    }
}
