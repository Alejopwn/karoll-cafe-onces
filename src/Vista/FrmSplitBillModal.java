package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Diálogo modal para división de cuenta (Split Bill / Split Check).
 */
public class FrmSplitBillModal extends JDialog {

    private final double totalMesa;
    private final int numMesa;

    public FrmSplitBillModal(JFrame parent, int numMesa, double totalMesa) {
        super(parent, "💳 División de Cuenta — Mesa " + numMesa, true);
        this.numMesa = numMesa;
        this.totalMesa = totalMesa;

        setSize(440, 320);
        setLocationRelativeTo(parent);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UIUtils.COLOR_BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("💳 División de Cuenta (Split Check)");
        lblTitle.setFont(Sistema.getFontBold(18f));
        lblTitle.setForeground(Color.WHITE);

        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        JPanel panelCenter = new JPanel(new GridLayout(3, 2, 10, 15));
        panelCenter.setOpaque(false);

        JLabel l1 = new JLabel("Mesa N°:");
        l1.setFont(Sistema.getFontBold(14f));
        l1.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel v1 = new JLabel(String.valueOf(numMesa));
        v1.setFont(Sistema.getFontBold(16f));
        v1.setForeground(Color.WHITE);

        JLabel l2 = new JLabel("Total Consumo ($):");
        l2.setFont(Sistema.getFontBold(14f));
        l2.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel v2 = new JLabel(df.format(totalMesa));
        v2.setFont(Sistema.getFontBold(18f));
        v2.setForeground(UIUtils.COLOR_ACCENT_GREEN);

        JLabel l3 = new JLabel("Número de Personas:");
        l3.setFont(Sistema.getFontBold(14f));
        l3.setForeground(Color.WHITE);

        JSpinner spinnerPersonas = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
        spinnerPersonas.setFont(Sistema.getFontBold(16f));

        panelCenter.add(l1); panelCenter.add(v1);
        panelCenter.add(l2); panelCenter.add(v2);
        panelCenter.add(l3); panelCenter.add(spinnerPersonas);

        JLabel lblResultado = new JLabel("Cuota por Persona: " + df.format(totalMesa / 2));
        lblResultado.setFont(Sistema.getFontBold(16f));
        lblResultado.setForeground(new Color(96, 165, 250));
        lblResultado.setHorizontalAlignment(SwingConstants.CENTER);

        spinnerPersonas.addChangeListener(e -> {
            int num = (int) spinnerPersonas.getValue();
            double cuota = totalMesa / num;
            lblResultado.setText("Cuota por Persona: " + df.format(cuota));
        });

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtn.setOpaque(false);

        JButton btnCerrar = UIUtils.crearBoton("Cerrar", new Color(107, 114, 128));
        btnCerrar.addActionListener(e -> dispose());

        JButton btnAplicar = UIUtils.crearBoton("Aceptar División", UIUtils.COLOR_ACCENT_BLUE);
        btnAplicar.setFont(Sistema.getFontBold(13f));
        btnAplicar.addActionListener(e -> {
            int num = (int) spinnerPersonas.getValue();
            double cuota = totalMesa / num;
            JOptionPane.showMessageDialog(this,
                    "Dividido entre " + num + " personas.\nValor individual a cobrar: " + df.format(cuota),
                    "División Lista", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        panelBtn.add(btnCerrar);
        panelBtn.add(btnAplicar);

        JPanel panelBody = new JPanel(new BorderLayout(10, 10));
        panelBody.setOpaque(false);
        panelBody.add(panelCenter, BorderLayout.CENTER);
        panelBody.add(lblResultado, BorderLayout.SOUTH);

        root.add(lblTitle, BorderLayout.NORTH);
        root.add(panelBody, BorderLayout.CENTER);
        root.add(panelBtn, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
