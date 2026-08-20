package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 * Clase utilitaria para estandarizar estilos, colores y componentes de la interfaz gráfica Swing.
 * Soporta alternancia dinámica entre Modo Oscuro (Titanio) y Modo Claro (Platino).
 */
public class UIUtils {

    // Variable global de tema
    public static boolean IS_DARK = true;

    static {
        try {
            IS_DARK = Preferences.userNodeForPackage(UIUtils.class).getBoolean("app_dark_theme", true);
        } catch (Exception e) {
            IS_DARK = true;
        }
    }

    // Paleta Modo Oscuro (Obsidian & Deep Titanium)
    public static final Color COLOR_BG_DARK = new Color(8, 9, 12);
    public static final Color COLOR_PANEL_DARK = new Color(18, 20, 26);
    public static final Color COLOR_BORDER_DARK = new Color(36, 38, 48);
    public static final Color COLOR_TEXT_PRIMARY = new Color(255, 255, 255);
    public static final Color COLOR_TEXT_MUTED = new Color(161, 161, 170);

    // Paleta Modo Claro (Platinum & Clean Slate)
    public static final Color COLOR_BG_LIGHT = new Color(241, 245, 249);
    public static final Color COLOR_PANEL_LIGHT = new Color(255, 255, 255);
    public static final Color COLOR_BORDER_LIGHT = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_PRIMARY_LIGHT = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED_LIGHT = new Color(100, 116, 139);

    // Colores de Acento Funcionales
    public static final Color COLOR_ACCENT_BLUE = new Color(56, 189, 248);
    public static final Color COLOR_ACCENT_GREEN = new Color(16, 185, 129);
    public static final Color COLOR_ACCENT_RED = new Color(244, 63, 94);
    public static final Color COLOR_ACCENT_ORANGE = new Color(245, 158, 11);
    public static final Color COLOR_ACCENT_PURPLE = new Color(168, 85, 247);

    // Getters dinámicos según el tema activo
    public static Color getBgColor() {
        return IS_DARK ? COLOR_BG_DARK : COLOR_BG_LIGHT;
    }

    public static Color getPanelColor() {
        return IS_DARK ? COLOR_PANEL_DARK : COLOR_PANEL_LIGHT;
    }

    public static Color getBorderColor() {
        return IS_DARK ? COLOR_BORDER_DARK : COLOR_BORDER_LIGHT;
    }

    public static Color getTextPrimary() {
        return IS_DARK ? COLOR_TEXT_PRIMARY : COLOR_TEXT_PRIMARY_LIGHT;
    }

    public static Color getTextMuted() {
        return IS_DARK ? COLOR_TEXT_MUTED : COLOR_TEXT_MUTED_LIGHT;
    }

    public static Color getInputBg() {
        return IS_DARK ? new Color(24, 26, 34) : new Color(248, 250, 252);
    }

    public static Color getCardHover() {
        return IS_DARK ? new Color(28, 32, 44) : new Color(241, 245, 249);
    }

    /**
     * Aplica el cambio de tema en FlatLaf y persiste la preferencia.
     */
    public static void setTema(boolean dark) {
        IS_DARK = dark;
        try {
            Preferences.userNodeForPackage(UIUtils.class).putBoolean("app_dark_theme", dark);
        } catch (Exception ignored) {}

        try {
            if (dark) {
                com.formdev.flatlaf.FlatDarkLaf.setup();
            } else {
                com.formdev.flatlaf.FlatLightLaf.setup();
            }
            com.formdev.flatlaf.FlatLaf.updateUI();
        } catch (Exception e) {
            System.err.println("Error al aplicar FlatLaf: " + e.getMessage());
        }
    }

    public static void alternarTema() {
        setTema(!IS_DARK);
    }

    /**
     * Obtiene el símbolo / icono del logo escalado y adaptado al tema.
     * En Modo Oscuro: blanco/luminoso de alta definición.
     * En Modo Claro: tono oscuro Slate (#0f172a) de alto contraste.
     */
    public static ImageIcon getLogoSymbol(int size) {
        try {
            java.net.URL url = UIUtils.class.getResource("/Img/as_symbol_clean.png");
            if (url != null) {
                java.awt.Image rawImg = new ImageIcon(url).getImage();
                BufferedImage bImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                if (IS_DARK) {
                    g2.drawImage(rawImg, 0, 0, size, size, null);
                } else {
                    BufferedImage temp = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gTemp = temp.createGraphics();
                    gTemp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gTemp.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    gTemp.drawImage(rawImg, 0, 0, size, size, null);
                    gTemp.dispose();

                    // Tinte oscuro Slate (#0f172a)
                    int targetR = 15, targetG = 23, targetB = 42;
                    for (int y = 0; y < size; y++) {
                        for (int x = 0; x < size; x++) {
                            int argb = temp.getRGB(x, y);
                            int alpha = (argb >> 24) & 0xff;
                            if (alpha > 0) {
                                int newArgb = (alpha << 24) | (targetR << 16) | (targetG << 8) | targetB;
                                temp.setRGB(x, y, newArgb);
                            }
                        }
                    }
                    g2.drawImage(temp, 0, 0, null);
                }
                g2.dispose();
                return new ImageIcon(bImg);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Obtiene el banner del logo adaptado al tema actual (Oscuro o Claro).
     */
    public static ImageIcon getBannerLogo(int width, int height) {
        String path = IS_DARK ? "/Img/as_banner_dark.png" : "/Img/as_banner_white.png";
        try {
            java.net.URL url = UIUtils.class.getResource(path);
            if (url != null) {
                java.awt.Image img = new ImageIcon(url).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return getLogoSymbol(height);
    }

    /**
     * Crea un botón estilizado con efectos hover reactivos al tema.
     */
    public static JButton crearBoton(String texto) {
        return crearBoton(texto, null, null);
    }

    public static JButton crearBoton(String texto, Color bg) {
        return crearBoton(texto, null, bg);
    }

    public static JButton crearBoton(String texto, Icon icon, Color bg) {
        JButton btn = new JButton(texto, icon);
        btn.setBackground(bg != null ? bg : getPanelColor());
        btn.setForeground(getTextPrimary());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor(), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        if (bg != null) {
            btn.putClientProperty("customBg", bg);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.isEnabled()) return;
                Boolean isActive = (Boolean) btn.getClientProperty("isNavActive");
                if (isActive != null && isActive) return;
                Color customBg = (Color) btn.getClientProperty("customBg");
                if (customBg != null) {
                    btn.setBackground(IS_DARK ? customBg.brighter() : customBg.darker());
                } else {
                    btn.setBackground(IS_DARK ? new Color(36, 38, 48) : new Color(226, 232, 240));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.isEnabled()) return;
                Boolean isActive = (Boolean) btn.getClientProperty("isNavActive");
                if (isActive != null && isActive) {
                    btn.setBackground(IS_DARK ? Color.WHITE : new Color(226, 232, 240));
                    return;
                }
                Color customBg = (Color) btn.getClientProperty("customBg");
                if (customBg != null) {
                    btn.setBackground(customBg);
                } else {
                    btn.setBackground(getPanelColor());
                }
            }
        });
        return btn;
    }

    /**
     * Crea una tarjeta KPI para paneles de resumen.
     */
    public static JLabel crearKpiCard(String titulo, String valorInicial, Color colorBorde) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(getPanelColor());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setForeground(getTextMuted());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblVal = new JLabel(valorInicial);
        lblVal.setForeground(getTextPrimary());
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 20));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return lblVal;
    }

    public static void estilarTablaOscura(JTable table) {
        estilarTabla(table);
    }

    /**
     * Aplica el estilo estándar a una JTable según el tema.
     */
    public static void estilarTabla(JTable table) {
        if (table == null) return;
        table.setRowHeight(34);
        table.setBackground(getBgColor());
        table.setForeground(getTextPrimary());
        table.setFillsViewportHeight(true);
        table.setGridColor(getBorderColor());
        table.setSelectionBackground(IS_DARK ? new Color(38, 42, 54) : new Color(226, 232, 240));
        table.setSelectionForeground(getTextPrimary());

        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(getPanelColor());
            table.getTableHeader().setForeground(getTextPrimary());
            table.getTableHeader().setOpaque(true);
            table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                    lbl.setBackground(getPanelColor());
                    lbl.setForeground(getTextPrimary());
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lbl.setHorizontalAlignment(JLabel.CENTER);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 1, getBorderColor()),
                            BorderFactory.createEmptyBorder(8, 6, 8, 6)));
                    return lbl;
                }
            });
        }
    }

    /**
     * Estila campos de texto para que se vean modernos y limpios según el tema.
     */
    public static void estilarCampoTexto(JTextComponent txt) {
        if (txt == null) return;
        txt.setBackground(getInputBg());
        txt.setForeground(getTextPrimary());
        txt.setCaretColor(getTextPrimary());
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getBorderColor(), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    /**
     * Estila ComboBox según el tema.
     */
    public static void estilarCombo(JComboBox<?> cbx) {
        if (cbx == null) return;
        cbx.setBackground(getInputBg());
        cbx.setForeground(getTextPrimary());
        cbx.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbx.setBorder(BorderFactory.createLineBorder(getBorderColor(), 1));
    }

    /**
     * Aplica el tema recursivamente a cualquier contenedor o diálogo.
     */
    public static void aplicarTemaRecursivo(Container container) {
        if (container == null) return;
        Color bg = getBgColor();
        Color panelBg = getPanelColor();
        Color text = getTextPrimary();
        Color border = getBorderColor();

        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                p.setBackground(bg);
                aplicarTemaRecursivo(p);
            } else if (c instanceof JTabbedPane) {
                JTabbedPane tp = (JTabbedPane) c;
                tp.setBackground(bg);
                tp.setForeground(text);
                aplicarTemaRecursivo(tp);
            } else if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.setBackground(bg);
                sp.getViewport().setBackground(bg);
                sp.setBorder(BorderFactory.createLineBorder(border, 1));
                aplicarTemaRecursivo(sp);
            } else if (c instanceof JViewport) {
                ((JViewport) c).setBackground(bg);
                aplicarTemaRecursivo((Container) c);
            } else if (c instanceof JTable) {
                estilarTabla((JTable) c);
            } else if (c instanceof JTextComponent) {
                estilarCampoTexto((JTextComponent) c);
            } else if (c instanceof JComboBox) {
                estilarCombo((JComboBox<?>) c);
            } else if (c instanceof JLabel) {
                c.setForeground(text);
            } else if (c instanceof Container) {
                aplicarTemaRecursivo((Container) c);
            }
        }
    }
}
