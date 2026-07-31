package Modelo;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderizador de celda de tabla para aplicar colores y estilos dinámicos a pedidos según su estado y tipo de pago.
 */
public class EstiloTablas extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row, int col) {
        super.getTableCellRendererComponent(jtable, o, bln, bln1, row, col);
        Color textColor = new Color(241, 245, 249); // Texto claro (Slate 100)

        // Hover
        java.awt.Point p = jtable.getMousePosition();
        int hoverRow = p != null ? jtable.rowAtPoint(p) : -1;

        if (bln) {
            // Fila seleccionada
            this.setBackground(new Color(59, 130, 246)); // Azul
            this.setForeground(Color.WHITE);
            return this;
        }

        String estado = jtable.getValueAt(row, 6) != null ? jtable.getValueAt(row, 6).toString() : "";
        double pagoEfectivo = 0.0;
        double pagoTransaccion = 0.0;
        try {
            int colCount = jtable.getModel().getColumnCount();
            if (colCount > 7 && jtable.getModel().getValueAt(row, 7) != null)
                pagoEfectivo = Double.parseDouble(jtable.getModel().getValueAt(row, 7).toString());
            if (colCount > 8 && jtable.getModel().getValueAt(row, 8) != null)
                pagoTransaccion = Double.parseDouble(jtable.getModel().getValueAt(row, 8).toString());
        } catch (NumberFormatException ignored) {}

        if (row == hoverRow) {
            this.setBackground(new Color(51, 65, 85)); // Hover (Slate 700)
            this.setForeground(textColor);
        } else if (estado.equals("PENDIENTE")) {
            this.setBackground(new Color(120, 80, 20)); // Naranja oscuro
            this.setForeground(new Color(253, 224, 171)); // Texto naranja claro
        } else if (estado.equals("FINALIZADO")) {
            if (pagoEfectivo > 0 && pagoTransaccion > 0) {
                this.setBackground(new Color(88, 28, 135)); // Morado (Mixto)
                this.setForeground(new Color(233, 213, 255));
            } else if (pagoEfectivo > 0) {
                this.setBackground(new Color(20, 83, 45)); // Verde (Efectivo)
                this.setForeground(new Color(187, 247, 208));
            } else if (pagoTransaccion > 0) {
                this.setBackground(new Color(30, 58, 138)); // Azul (Transferencia)
                this.setForeground(new Color(191, 219, 254));
            } else {
                if (row % 2 == 0) {
                    this.setBackground(new Color(30, 41, 59));
                } else {
                    this.setBackground(new Color(15, 23, 42));
                }
                this.setForeground(textColor);
            }
        } else {
            if (row % 2 == 0) {
                this.setBackground(new Color(30, 41, 59)); // Cebra par (Slate 800)
            } else {
                this.setBackground(new Color(15, 23, 42)); // Cebra impar (Slate 900)
            }
            this.setForeground(textColor);
        }

        return this;
    }
}
