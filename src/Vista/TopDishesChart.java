package Vista;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Componente visual de barras horizontales para mostrar el top 5 de platos más vendidos.
 */
public class TopDishesChart extends JPanel {

    private List<Object[]> data = new ArrayList<>();

    public TopDishesChart() {
        setOpaque(false);
    }

    public void setData(List<Object[]> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(18, 20, 26));
        g2.fillRoundRect(0, 0, w, h, 16, 16);
        g2.setColor(new Color(71, 85, 105, 120));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        g2.setColor(Color.WHITE);
        g2.setFont(Sistema.getFontBold(15f));
        g2.drawString("Top 5 Platos más Vendidos", 20, 28);

        if (data == null || data.isEmpty()) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(Sistema.getFontRegular(14f));
            String msg = "Sin datos disponibles";
            int mw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (w - mw) / 2, h / 2);
            g2.dispose();
            return;
        }

        int startY = 60, barH = 28, gap = 50, labelW = 150, maxBarW = w - labelW - 70;
        int maxQty = 0;
        for (Object[] it : data) {
            int q = (int) it[1];
            if (q > maxQty) maxQty = q;
        }
        if (maxQty == 0) maxQty = 1;

        Color[][] palette = {
            {new Color(16, 185, 129), new Color(5, 150, 105)},
            {new Color(59, 130, 246), new Color(37, 99, 235)},
            {new Color(245, 158, 11), new Color(217, 119, 6)},
            {new Color(139, 92, 246), new Color(109, 40, 217)},
            {new Color(236, 72, 153), new Color(219, 39, 119)}
        };

        for (int i = 0; i < data.size(); i++) {
            Object[] it = data.get(i);
            String name = (String) it[0];
            int qty = (int) it[1];
            int y = startY + i * gap;

            g2.setFont(Sistema.getFontRegular(12f));
            g2.setColor(new Color(226, 232, 240));
            String disp = name;
            if (g2.getFontMetrics().stringWidth(disp) > labelW - 10) {
                while (disp.length() > 0 && g2.getFontMetrics().stringWidth(disp + "..") > labelW - 10) {
                    disp = disp.substring(0, disp.length() - 1);
                }
                disp += "..";
            }
            g2.drawString(disp, 20, y + 19);

            int bW = Math.max(8, qty * maxBarW / maxQty);
            Color[] c = palette[i % palette.length];
            g2.setPaint(new GradientPaint(labelW, 0, c[0], labelW + bW, 0, c[1]));
            g2.fillRoundRect(labelW, y, bW, barH, 8, 8);

            g2.setFont(Sistema.getFontBold(12f));
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(qty), labelW + bW + 10, y + 19);
        }
        g2.dispose();
    }
}
