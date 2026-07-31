package Vista;

import Modelo.DetallePedido;
import Modelo.Pedido;
import Modelo.PedidosDao;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Modal dinámico para cobrar una mesa con soporte para:
 * 1. Todo Junto (Acumulado de todas las rondas)
 * 2. Partes Iguales
 * 3. Por Rondas
 * 4. Cada Quien lo Suyo (Por Persona)
 */
public class ModalCobrarMesa extends JDialog {

    private final Frame parentFrame;
    private final int numMesa;
    private final int idSala;
    private final String etiquetaMesa;
    private final PedidosDao pedDao = new PedidosDao();
    private final Sistema sistemaInstance;

    private List<Pedido> listaRondas;
    private List<DetallePedido> detallesAcumulados;
    private double totalAcumulado;

    public ModalCobrarMesa(Frame parent, Sistema sistemaInstance, int numMesa, int idSala, String etiquetaMesa) {
        super(parent, "Gestión de Pedidos / Cobro - " + etiquetaMesa, true);
        this.parentFrame = parent;
        this.sistemaInstance = sistemaInstance;
        this.numMesa = numMesa;
        this.idSala = idSala;
        this.etiquetaMesa = etiquetaMesa;
        cargarDatos();
        initUI();
    }

    private void cargarDatos() {
        listaRondas = pedDao.getRondasMesa(numMesa, idSala);
        detallesAcumulados = pedDao.getDetallesAcumuladosMesa(numMesa, idSala);
        totalAcumulado = 0.0;
        for (DetallePedido d : detallesAcumulados) {
            totalAcumulado += d.getCantidad() * d.getPrecio();
        }
    }

    private void initUI() {
        setSize(820, 580);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(15, 23, 42));

        // --- Header ---
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        pHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel lblTitle = new JLabel("🍽️ " + etiquetaMesa + " (" + listaRondas.size() + " ronda" + (listaRondas.size() > 1 ? "s" : "") + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblTotal = new JLabel(String.format("Total Consolidado: $%,.2f COP", totalAcumulado));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(52, 211, 153));

        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(lblTotal, BorderLayout.EAST);
        add(pHeader, BorderLayout.NORTH);

        // --- Tabbed Pane de Modos ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("👥 Por Rondas", crearPanelRondas());
        tabs.addTab("🧾 Cada quien lo suyo (Por Persona)", crearPanelPorPersona());

        add(tabs, BorderLayout.CENTER);

        // --- Footer Acciones Rápidas ---
        JPanel pFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pFooter.setOpaque(false);
        pFooter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton btnCobrarTodo = new JButton("💰 Cobrar Todo Junto ($" + String.format("%,.0f", totalAcumulado) + ")");
        btnCobrarTodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCobrarTodo.setBackground(new Color(16, 185, 129));
        btnCobrarTodo.setForeground(Color.WHITE);
        btnCobrarTodo.setPreferredSize(new Dimension(280, 48));
        btnCobrarTodo.setFocusPainted(false);
        btnCobrarTodo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrarTodo.addActionListener(e -> {
            dispose();
            sistemaInstance.abrirCobroMesaAcumulado(numMesa, idSala);
        });

        JButton btnSplitEqual = new JButton("÷ Partes Iguales");
        btnSplitEqual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSplitEqual.setBackground(new Color(59, 130, 246));
        btnSplitEqual.setForeground(Color.WHITE);
        btnSplitEqual.setPreferredSize(new Dimension(170, 48));
        btnSplitEqual.setFocusPainted(false);
        btnSplitEqual.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSplitEqual.addActionListener(e -> {
            if (totalAcumulado > 0) {
                FrmSplitBillModal modalSplit = new FrmSplitBillModal((JFrame) parentFrame, numMesa, totalAcumulado);
                modalSplit.setVisible(true);
            }
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnCerrar.setBackground(new Color(71, 85, 105));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setPreferredSize(new Dimension(100, 48));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        pFooter.add(btnCobrarTodo);
        pFooter.add(btnSplitEqual);
        pFooter.add(btnCerrar);
        add(pFooter, BorderLayout.SOUTH);
    }

    private JPanel crearPanelRondas() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Ronda", "Fecha / Hora", "Estado", "Total Ronda"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < listaRondas.size(); i++) {
            Pedido r = listaRondas.get(i);
            model.addRow(new Object[]{
                "Ronda " + (i + 1) + " (#" + r.getId() + ")",
                r.getFecha(),
                r.getEstado(),
                String.format("$%,.2f COP", r.getTotal())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(226, 232, 240));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Renderizado con color en la columna "Estado"
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String estadoStr = String.valueOf(value);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("PREPARADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(new Color(120, 53, 15)); // Amber oscuro
                    c.setForeground(new Color(252, 211, 77)); // Amber claro
                } else if ("FINALIZADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(new Color(6, 78, 59)); // Verde oscuro
                    c.setForeground(new Color(52, 211, 153)); // Verde claro
                } else {
                    c.setBackground(new Color(127, 29, 29)); // Rojo oscuro
                    c.setForeground(new Color(252, 165, 165)); // Rojo claro
                }
                return c;
            }
        });

        TouchScrollHelper.aplicar(new JScrollPane(table));
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pActions.setOpaque(false);

        JButton btnNuevaRonda = new JButton("+ Agregar Nueva Ronda");
        btnNuevaRonda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNuevaRonda.setBackground(new Color(245, 158, 11));
        btnNuevaRonda.setForeground(Color.WHITE);
        btnNuevaRonda.setFocusPainted(false);
        btnNuevaRonda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevaRonda.addActionListener(e -> {
            dispose();
            sistemaInstance.crearNuevaRondaMesa(numMesa, idSala);
        });
        pActions.add(btnNuevaRonda);
        p.add(pActions, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearPanelPorPersona() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control de comensales
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        pTop.setOpaque(false);
        JLabel lblNumPer = new JLabel("Comensales:");
        lblNumPer.setForeground(Color.WHITE);
        lblNumPer.setFont(new Font("Segoe UI", Font.BOLD, 14));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 2, 20, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 14));

        pTop.add(lblNumPer);
        pTop.add(spinner);

        // Tabla de productos acumulados
        String[] cols = {"Producto Consolidado", "Cantidad Total", "Precio Unitario", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (DetallePedido d : detallesAcumulados) {
            model.addRow(new Object[]{
                d.getNombre(),
                d.getCantidad(),
                String.format("$%,.2f", d.getPrecio()),
                String.format("$%,.2f COP", d.getCantidad() * d.getPrecio())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(226, 232, 240));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        TouchScrollHelper.aplicar(new JScrollPane(table));
        p.add(pTop, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        return p;
    }
}
