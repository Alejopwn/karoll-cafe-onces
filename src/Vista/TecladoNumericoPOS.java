package Vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Teclado numérico táctil flotante con botones grandes (80x70px).
 */
public class TecladoNumericoPOS extends JDialog {

    private final JTextComponent targetField;

    public TecladoNumericoPOS(Frame parent, String titulo, JTextComponent target) {
        super(parent, titulo, true);
        this.targetField = target;
        initUI();
    }

    public static void mostrar(Frame parent, String titulo, JTextComponent target) {
        TecladoNumericoPOS keypad = new TecladoNumericoPOS(parent, titulo, target);
        keypad.setLocationRelativeTo(parent);
        keypad.setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(320, 420);
        setResizable(false);

        // Display superior
        JTextField display = new JTextField(targetField != null ? targetField.getText() : "");
        display.setFont(new Font("Segoe UI", Font.BOLD, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBackground(new Color(15, 23, 42));
        display.setForeground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(display, BorderLayout.NORTH);

        // Panel de botones (Grid 4x3)
        JPanel panelGrid = new JPanel(new GridLayout(4, 3, 8, 8));
        panelGrid.setBackground(new Color(30, 41, 59));
        panelGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] botones = {
            "7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            ".", "0", "⌫"
        };

        for (String txt : botones) {
            JButton btn = new JButton(txt);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (txt.equals("⌫")) {
                btn.setBackground(new Color(220, 38, 38));
                btn.setForeground(Color.WHITE);
            } else if (txt.equals(".")) {
                btn.setBackground(new Color(71, 85, 105));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(51, 65, 85));
                btn.setForeground(Color.WHITE);
            }

            btn.addActionListener(e -> {
                String cur = display.getText();
                if (txt.equals("⌫")) {
                    if (!cur.isEmpty()) {
                        display.setText(cur.substring(0, cur.length() - 1));
                    }
                } else if (txt.equals(".")) {
                    if (!cur.contains(".")) {
                        display.setText(cur.isEmpty() ? "0." : cur + ".");
                    }
                } else {
                    display.setText(cur + txt);
                }
            });
            panelGrid.add(btn);
        }

        add(panelGrid, BorderLayout.CENTER);

        // Footer con Aceptar / Cancelar
        JPanel panelFooter = new JPanel(new GridLayout(1, 2, 10, 0));
        panelFooter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCancelar.setBackground(new Color(71, 85, 105));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setPreferredSize(new Dimension(0, 50));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnOK = new JButton("Aceptar ✅");
        btnOK.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnOK.setBackground(new Color(16, 185, 129));
        btnOK.setForeground(Color.WHITE);
        btnOK.setPreferredSize(new Dimension(0, 50));
        btnOK.addActionListener(e -> {
            if (targetField != null) {
                targetField.setText(display.getText());
            }
            dispose();
        });

        panelFooter.add(btnCancelar);
        panelFooter.add(btnOK);
        add(panelFooter, BorderLayout.SOUTH);
    }
}
