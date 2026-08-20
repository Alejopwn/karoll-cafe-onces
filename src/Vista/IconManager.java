package Vista;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Generador y gestor de iconos vectoriales de alta precisión para AS Business Systems.
 * Renderiza iconos 100% transparentes, nítidos (anti-aliased) y dinámicos para Modo Claro y Modo Oscuro.
 */
public class IconManager {

    public static Icon getIcon(String name, int size) {
        return getIcon(name, size, UIUtils.IS_DARK);
    }

    public static Icon getIcon(String name, int size, boolean isDark) {
        if (name == null || name.isEmpty()) return null;
        name = name.toLowerCase().trim();

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Paleta según tema
        Color primary = isDark ? new Color(255, 255, 255, 245) : new Color(15, 23, 42, 245);
        Color secondary = isDark ? new Color(161, 161, 170, 200) : new Color(100, 116, 139, 220);
        Color accent = isDark ? new Color(56, 189, 248, 240) : new Color(37, 99, 235, 240);

        float strokeWidth = Math.max(1.8f, size / 16f);
        Stroke mainStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Stroke thinStroke = new BasicStroke(Math.max(1.2f, strokeWidth * 0.75f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        // Escalar coordenadas base (diseñado sobre cuadrícula 48x48)
        double scale = size / 48.0;
        g.scale(scale, scale);

        drawIconPath(g, name, mainStroke, thinStroke, primary, secondary, accent);

        g.dispose();
        return new ImageIcon(img);
    }

    private static void drawIconPath(Graphics2D g, String name, Stroke ms, Stroke ts, Color pri, Color sec, Color acc) {
        if (name.contains("mesa") || name.contains("sala")) {
            // 🍽️ Mesa de Restaurante Elegante con Sillas
            g.setStroke(ms);
            g.setColor(pri);
            // Tablero de mesa redondeado
            g.drawRoundRect(14, 18, 20, 16, 6, 6);
            // Silla izquierda con respaldo curvado
            g.setStroke(ts);
            g.setColor(sec);
            g.drawRoundRect(6, 21, 5, 10, 3, 3);
            // Silla derecha
            g.drawRoundRect(37, 21, 5, 10, 3, 3);
            // Plato central / acento
            g.setColor(acc);
            g.fillOval(21, 23, 6, 6);

        } else if (name.contains("pedido") || name.contains("venta") || name.contains("historial")) {
            // 🧾 Comanda / Recibo Digital
            g.setStroke(ms);
            g.setColor(pri);
            g.drawRoundRect(12, 10, 24, 30, 6, 6);
            // Clip superior
            g.setStroke(ts);
            g.setColor(sec);
            g.drawRoundRect(18, 7, 12, 6, 3, 3);
            // Líneas de pedido
            g.setColor(pri);
            g.drawLine(17, 20, 31, 20);
            g.drawLine(17, 26, 31, 26);
            g.setColor(acc);
            g.drawLine(17, 32, 25, 32);

        } else if (name.contains("caja") || name.contains("money") || name.contains("carrit")) {
            // 💵 Terminal POS / Caja
            g.setStroke(ms);
            g.setColor(pri);
            g.drawRoundRect(10, 14, 28, 22, 6, 6);
            // Pantalla
            g.setStroke(ts);
            g.setColor(sec);
            g.drawRoundRect(14, 18, 20, 10, 3, 3);
            // Ranura de tarjeta / base
            g.setColor(acc);
            g.drawLine(14, 31, 34, 31);
            g.drawLine(24, 20, 24, 26);

        } else if (name.contains("inv") || name.contains("stock")) {
            // 📦 Caja de Inventario / Almacén
            g.setStroke(ms);
            g.setColor(pri);
            g.drawRoundRect(10, 14, 28, 24, 6, 6);
            // Línea de cinta / apertura
            g.setStroke(ts);
            g.setColor(sec);
            g.drawLine(10, 22, 38, 22);
            g.drawLine(24, 22, 24, 38);
            // Etiqueta / Handle
            g.setColor(acc);
            g.drawRoundRect(19, 27, 10, 5, 2, 2);

        } else if (name.contains("plato") || name.contains("carta") || name.contains("menu")) {
            // 🍔 Campana Cloche de Chef
            g.setStroke(ms);
            g.setColor(pri);
            g.drawLine(8, 35, 40, 35);
            g.drawArc(12, 14, 24, 36, 0, 180);
            // Manija
            g.setStroke(ts);
            g.setColor(sec);
            g.drawOval(21, 10, 6, 6);
            // Puntos de vapor gourmet
            g.setColor(acc);
            g.fillOval(18, 22, 3, 3);
            g.fillOval(24, 19, 3, 3);
            g.fillOval(30, 22, 3, 3);

        } else if (name.contains("admin") || name.contains("config")) {
            // ⚙️ Engranaje de Precisión
            g.setStroke(ms);
            g.setColor(pri);
            g.drawOval(15, 15, 18, 18);
            g.setStroke(ts);
            g.setColor(acc);
            g.drawOval(20, 20, 8, 8);
            // Dientes
            g.setColor(sec);
            g.drawLine(24, 9, 24, 15);
            g.drawLine(24, 33, 24, 39);
            g.drawLine(9, 24, 15, 24);
            g.drawLine(33, 24, 39, 24);
            g.drawLine(14, 14, 18, 18);
            g.drawLine(30, 30, 34, 34);
            g.drawLine(34, 14, 30, 18);
            g.drawLine(18, 30, 14, 34);

        } else if (name.contains("usu") || name.contains("client") || name.contains("user")) {
            // 👤 Usuario / Personal
            g.setStroke(ms);
            g.setColor(pri);
            g.drawOval(18, 10, 12, 12);
            g.drawArc(10, 26, 28, 22, 0, 180);
            g.setStroke(ts);
            g.setColor(acc);
            g.drawLine(24, 27, 24, 35);

        } else if (name.contains("recordator") || name.contains("bell") || name.contains("notif")) {
            // 🔔 Campana de Recordatorio
            g.setStroke(ms);
            g.setColor(pri);
            g.drawArc(14, 12, 20, 22, 0, 180);
            g.drawLine(11, 33, 37, 33);
            g.setStroke(ts);
            g.setColor(sec);
            g.drawArc(21, 33, 6, 6, 180, 180);
            g.setColor(acc);
            g.drawOval(22, 8, 4, 4);

        } else if (name.contains("tarea") || name.contains("check")) {
            // 📋 Tareas / Checklist
            g.setStroke(ms);
            g.setColor(pri);
            g.drawRoundRect(10, 9, 28, 30, 6, 6);
            g.setStroke(ts);
            g.setColor(acc);
            g.drawLine(15, 19, 18, 22);
            g.drawLine(18, 22, 23, 16);
            g.drawLine(15, 29, 18, 32);
            g.drawLine(18, 32, 23, 26);
            g.setColor(sec);
            g.drawLine(26, 19, 33, 19);
            g.drawLine(26, 29, 33, 29);

        } else if (name.contains("sonid") || name.contains("audio") || name.contains("sound")) {
            // 🔊 Altavoz / Audio
            g.setStroke(ms);
            g.setColor(pri);
            g.drawLine(12, 19, 18, 19);
            g.drawLine(18, 19, 26, 13);
            g.drawLine(26, 13, 26, 35);
            g.drawLine(26, 35, 18, 29);
            g.drawLine(18, 29, 12, 29);
            g.drawLine(12, 29, 12, 19);
            g.setStroke(ts);
            g.setColor(acc);
            g.drawArc(29, 18, 8, 12, -60, 120);
            g.drawArc(33, 14, 12, 20, -60, 120);

        } else if (name.contains("domicili") || name.contains("moto") || name.contains("delivery")) {
            // 🛵 Domicilio
            g.setStroke(ms);
            g.setColor(pri);
            g.drawOval(10, 28, 10, 10);
            g.drawOval(28, 28, 10, 10);
            g.setStroke(ts);
            g.setColor(sec);
            g.drawLine(15, 33, 24, 33);
            g.drawLine(24, 33, 33, 19);
            g.drawLine(33, 19, 30, 19);
            g.setColor(acc);
            g.drawRoundRect(12, 17, 11, 11, 3, 3);

        } else if (name.contains("claro") || name.contains("sun") || name.contains("sol")) {
            // ☀️ Sol (Modo Claro)
            g.setStroke(ms);
            g.setColor(new Color(245, 158, 11)); // Amber
            g.drawOval(18, 18, 12, 12);
            g.setStroke(ts);
            g.drawLine(24, 9, 24, 14);
            g.drawLine(24, 34, 24, 39);
            g.drawLine(9, 24, 14, 24);
            g.drawLine(34, 24, 39, 24);
            g.drawLine(13, 13, 17, 17);
            g.drawLine(31, 31, 35, 35);
            g.drawLine(35, 13, 31, 17);
            g.drawLine(17, 31, 13, 35);

        } else if (name.contains("oscur") || name.contains("moon") || name.contains("luna")) {
            // 🌙 Luna (Modo Oscuro)
            g.setStroke(ms);
            g.setColor(new Color(56, 189, 248)); // Sky Blue
            g.drawArc(14, 12, 20, 24, 45, 270);
            g.drawArc(19, 14, 14, 20, 45, 270);
            g.setColor(new Color(251, 191, 36));
            g.fillOval(31, 14, 3, 3);

        } else if (name.contains("salir") || name.contains("logout")) {
            // 🚪 Salir
            g.setStroke(ts);
            g.setColor(sec);
            g.drawLine(23, 11, 12, 11);
            g.drawLine(12, 11, 12, 37);
            g.drawLine(12, 37, 23, 37);
            g.setStroke(ms);
            g.setColor(new Color(244, 63, 94)); // Red accent
            g.drawLine(20, 24, 36, 24);
            g.drawLine(30, 18, 36, 24);
            g.drawLine(30, 30, 36, 24);

        } else if (name.contains("cambiar") || name.contains("switch")) {
            // 🔄 Cambiar Usuario
            g.setStroke(ms);
            g.setColor(pri);
            g.drawArc(12, 14, 24, 20, 45, 180);
            g.drawLine(33, 14, 36, 19);
            g.drawLine(30, 19, 36, 19);
            g.drawArc(12, 14, 24, 20, 225, 180);
            g.drawLine(15, 34, 12, 29);
            g.drawLine(18, 29, 12, 29);

        } else {
            // Default: Cuadrado elegante
            g.setStroke(ms);
            g.setColor(pri);
            g.drawRoundRect(12, 12, 24, 24, 6, 6);
            g.setStroke(ts);
            g.setColor(acc);
            g.drawOval(20, 20, 8, 8);
        }
    }
}
