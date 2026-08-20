package Vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Componente visual de gráfica de línea para mostrar la tendencia de ventas por hora.
 */
public class SalesTrendChart extends JPanel {

    private Map<String, Double> data = new LinkedHashMap<>();

    public SalesTrendChart() {
        setOpaque(false);
    }

    public void setData(Map<String, Double> data) {
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
        g2.drawString("Tendencia de Ventas por Hora", 20, 28);

        if (data == null || data.isEmpty()) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(Sistema.getFontRegular(14f));
            String msg = "Sin ventas en este turno";
            int mw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (w - mw) / 2, h / 2);
            g2.dispose();
            return;
        }

        int pL = 65, pR = 25, pT = 55, pB = 35;
        int cW = w - pL - pR, cH = h - pT - pB;

        double maxVal = 0;
        for (double v : data.values()) {
            if (v > maxVal) maxVal = v;
        }
        if (maxVal == 0) maxVal = 10000;
        maxVal = Math.ceil(maxVal / 10000.0) * 10000;

        g2.setFont(Sistema.getFontRegular(10f));
        for (int i = 0; i <= 4; i++) {
            int y = pT + cH - (i * cH / 4);
            double val = i * maxVal / 4;
            g2.setColor(new Color(71, 85, 105, 60));
            g2.drawLine(pL, y, pL + cW, y);
            g2.setColor(new Color(148, 163, 184));
            String lbl = val == 0 ? "$0" : String.format("$%.0fk", val / 1000.0);
            g2.drawString(lbl, pL - 55, y + 4);
        }

        List<String> keys = new ArrayList<>(data.keySet());
        int n = keys.size();
        int[] xP = new int[n], yP = new int[n];
        for (int i = 0; i < n; i++) {
            xP[i] = pL + (n > 1 ? i * cW / (n - 1) : cW / 2);
            yP[i] = (int) (pT + cH - (data.get(keys.get(i)) * cH / maxVal));
        }

        g2.setFont(Sistema.getFontRegular(9f));
        for (int i = 0; i < n; i++) {
            g2.setColor(new Color(148, 163, 184));
            String hr = keys.get(i);
            int lw = g2.getFontMetrics().stringWidth(hr);
            g2.drawString(hr, xP[i] - lw / 2, pT + cH + 18);
        }

        if (n > 1) {
            Path2D.Float curve = new Path2D.Float();
            curve.moveTo(xP[0], yP[0]);
            for (int i = 1; i < n; i++) {
                float cx = (xP[i - 1] + xP[i]) / 2.0f;
                curve.curveTo(cx, yP[i - 1], cx, yP[i], xP[i], yP[i]);
            }
            Path2D.Float area = (Path2D.Float) curve.clone();
            area.lineTo(xP[n - 1], pT + cH);
            area.lineTo(xP[0], pT + cH);
            area.closePath();

            g2.setPaint(new GradientPaint(0, pT, new Color(59, 130, 246, 70), 0, pT + cH, new Color(59, 130, 246, 0)));
            g2.fill(area);

            g2.setPaint(new GradientPaint(pL, 0, new Color(59, 130, 246), pL + cW, 0, new Color(147, 197, 253)));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(curve);
        }

        for (int i = 0; i < n; i++) {
            g2.setColor(new Color(59, 130, 246));
            g2.fillOval(xP[i] - 5, yP[i] - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(xP[i] - 2, yP[i] - 2, 4, 4);
        }
        g2.dispose();
    }
}
