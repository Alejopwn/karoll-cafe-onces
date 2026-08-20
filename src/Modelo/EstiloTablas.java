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

    private Color getBgEven() { return Vista.UIUtils.IS_DARK ? new Color(18, 20, 26) : new Color(255, 255, 255); }
    private Color getBgOdd() { return Vista.UIUtils.IS_DARK ? new Color(10, 11, 14) : new Color(248, 250, 252); }
    private Color getBgHover() { return Vista.UIUtils.IS_DARK ? new Color(28, 30, 38) : new Color(241, 245, 249); }
    private Color getBgSelected() { return Vista.UIUtils.IS_DARK ? new Color(38, 42, 54) : new Color(226, 232, 240); }
    private Color getTextMain() { return Vista.UIUtils.IS_DARK ? new Color(255, 255, 255) : new Color(15, 23, 42); }

    // Colores de badges de estado
    private static final Color BADGE_FINALIZADO_BG = new Color(16, 185, 129, 35);
    private static final Color BADGE_FINALIZADO_FG = new Color(16, 185, 129);
    private static final Color BADGE_PREPARADO_BG = new Color(245, 158, 11, 35);
    private static final Color BADGE_PREPARADO_FG = new Color(245, 158, 11);
    private static final Color BADGE_PENDIENTE_BG = new Color(244, 63, 94, 35);
    private static final Color BADGE_PENDIENTE_FG = new Color(244, 63, 94);
    private static final Color BADGE_ANULADO_BG = new Color(100, 116, 139, 35);
    private static final Color BADGE_ANULADO_FG = new Color(100, 116, 139);

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
                label.setBackground(getBgSelected());
                label.setForeground(getTextMain());
            } else if (estado.contains("FINALIZADO")) {
                label.setBackground(BADGE_FINALIZADO_BG);
                label.setForeground(BADGE_FINALIZADO_FG);
                label.setText("● " + estado);
            } else if (estado.contains("PREPARADO")) {
                label.setBackground(BADGE_PREPARADO_BG);
                label.setForeground(BADGE_PREPARADO_FG);
                label.setText("● " + estado);
            } else if (estado.contains("PENDIENTE")) {
                label.setBackground(BADGE_PENDIENTE_BG);
                label.setForeground(BADGE_PENDIENTE_FG);
                label.setText("● " + estado);
            } else if (estado.contains("ANULADO")) {
                label.setBackground(BADGE_ANULADO_BG);
                label.setForeground(BADGE_ANULADO_FG);
                label.setText("✕ " + estado);
            } else {
                label.setBackground(row % 2 == 0 ? getBgEven() : getBgOdd());
                label.setForeground(getTextMain());
            }
            return label;
        }

        // Para el resto de columnas: Fondo Cebra Limpio o Selección
        if (isSelected) {
            label.setBackground(getBgSelected());
            label.setForeground(getTextMain());
        } else {
            java.awt.Point p = jtable.getMousePosition();
            int hoverRow = p != null ? jtable.rowAtPoint(p) : -1;
            if (row == hoverRow) {
                label.setBackground(getBgHover());
            } else {
                label.setBackground(row % 2 == 0 ? getBgEven() : getBgOdd());
            }
            label.setForeground(col == 5 ? (Vista.UIUtils.IS_DARK ? new Color(56, 189, 248) : new Color(37, 99, 235)) : getTextMain());
        }

        return label;
    }
}
