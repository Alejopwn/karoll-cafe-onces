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
 * Totalmente adaptativo a Modo Oscuro y Modo Claro.
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
    private JTable table;

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
        if (listaRondas.isEmpty()) {
            int altId = pedDao.verificarStado(numMesa, idSala);
            if (altId > 0) {
                Pedido p = pedDao.verPedido(altId);
                if (p != null && p.getId() > 0) {
                    listaRondas.add(p);
                }
            }
        }
        detallesAcumulados = pedDao.getDetallesAcumuladosMesa(numMesa, idSala);
        if (detallesAcumulados.isEmpty() && !listaRondas.isEmpty()) {
            for (Pedido r : listaRondas) {
                detallesAcumulados.addAll(pedDao.verPedidoDetalle(r.getId()));
            }
        }
        totalAcumulado = 0.0;
        for (DetallePedido d : detallesAcumulados) {
            totalAcumulado += d.getCantidad() * d.getPrecio();
        }
    }

    private void initUI() {
        setSize(980, 590);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIUtils.getBgColor());

        // --- Header ---
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setOpaque(false);
        pHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JLabel lblTitle = new JLabel(etiquetaMesa + " (" + listaRondas.size() + " ronda" + (listaRondas.size() > 1 ? "s" : "") + ")");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIUtils.getTextPrimary());

        JLabel lblTotal = new JLabel(String.format("Total Consolidado: $%,.0f COP", totalAcumulado));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(16, 185, 129));

        pHeader.add(lblTitle, BorderLayout.WEST);
        pHeader.add(lblTotal, BorderLayout.EAST);
        add(pHeader, BorderLayout.NORTH);

        // --- Tabbed Pane de Modos ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(UIUtils.getBgColor());
        tabs.setForeground(UIUtils.getTextPrimary());
        tabs.addTab("Por Rondas", crearPanelRondas());
        tabs.addTab("Cada quien lo suyo (Por Persona)", crearPanelPorPersona());

        add(tabs, BorderLayout.CENTER);

        // --- Footer Acciones Rápidas ---
        JPanel pFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pFooter.setOpaque(false);
        pFooter.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton btnMarcarRondaSelec = new JButton("Preparar Ronda");
        btnMarcarRondaSelec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnMarcarRondaSelec.setBackground(new Color(217, 119, 6));
        btnMarcarRondaSelec.setForeground(Color.WHITE);
        btnMarcarRondaSelec.setPreferredSize(new Dimension(155, 48));
        btnMarcarRondaSelec.setFocusPainted(false);
        btnMarcarRondaSelec.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarcarRondaSelec.setToolTipText("Marca ÚNICAMENTE la ronda seleccionada en la tabla como lista");
        btnMarcarRondaSelec.addActionListener(e -> {
            if (table != null && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                if (row < listaRondas.size()) {
                    Pedido r = listaRondas.get(row);
                    if ("PREPARADO".equalsIgnoreCase(r.getEstado())) {
                        ToastNotification.advertencia(parentFrame, "La ronda #" + r.getId() + " ya está PREPARADA.");
                        return;
                    }
                    if (pedDao.marcarPreparado(r.getId())) {
                        r.setEstado("PREPARADO");
                        table.setValueAt("PREPARADO", row, 2);
                        ToastNotification.exito(parentFrame, "¡Ronda #" + r.getId() + " marcada como PREPARADA!");
                    }
                }
            } else {
                ToastNotification.advertencia(parentFrame, "Seleccione una ronda en la tabla para marcar como preparada.");
            }
        });

        JButton btnCobrarRondaSelec = new JButton("Cobrar Solo Esta Ronda");
        btnCobrarRondaSelec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCobrarRondaSelec.setBackground(new Color(2, 132, 199)); // Ocean Blue
        btnCobrarRondaSelec.setForeground(Color.WHITE);
        btnCobrarRondaSelec.setPreferredSize(new Dimension(195, 48));
        btnCobrarRondaSelec.setFocusPainted(false);
        btnCobrarRondaSelec.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrarRondaSelec.setToolTipText("Cobrar ÚNICAMENTE los productos de la ronda seleccionada");
        btnCobrarRondaSelec.addActionListener(e -> {
            if (table != null && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                if (row < listaRondas.size()) {
                    Pedido r = listaRondas.get(row);
                    if ("FINALIZADO".equalsIgnoreCase(r.getEstado())) {
                        ToastNotification.advertencia(parentFrame, "Esta ronda ya fue cobrada y finalizada.");
                        return;
                    }
                    dispose();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        sistemaInstance.abrirCobroPedido(r.getId());
                    });
                }
            } else {
                ToastNotification.advertencia(parentFrame, "Seleccione una ronda en la tabla para cobrar individualmente.");
            }
        });

        JButton btnCobrarTodo = new JButton("Cobrar Todo Junto ($" + String.format("%,.0f", totalAcumulado) + ")");
        btnCobrarTodo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCobrarTodo.setBackground(new Color(16, 185, 129));
        btnCobrarTodo.setForeground(Color.WHITE);
        btnCobrarTodo.setPreferredSize(new Dimension(235, 48));
        btnCobrarTodo.setFocusPainted(false);
        btnCobrarTodo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrarTodo.addActionListener(e -> {
            dispose();
            javax.swing.SwingUtilities.invokeLater(() -> {
                sistemaInstance.abrirCobroMesaAcumulado(numMesa, idSala);
            });
        });

        JButton btnSplitEqual = new JButton("Partes Iguales");
        btnSplitEqual.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSplitEqual.setBackground(new Color(59, 130, 246));
        btnSplitEqual.setForeground(Color.WHITE);
        btnSplitEqual.setPreferredSize(new Dimension(135, 48));
        btnSplitEqual.setFocusPainted(false);
        btnSplitEqual.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSplitEqual.addActionListener(e -> {
            if (totalAcumulado > 0) {
                FrmSplitBillModal modalSplit = new FrmSplitBillModal((JFrame) parentFrame, numMesa, totalAcumulado);
                modalSplit.setVisible(true);
            }
        });

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCerrar.setBackground(UIUtils.getBorderColor());
        btnCerrar.setForeground(UIUtils.getTextPrimary());
        btnCerrar.setPreferredSize(new Dimension(80, 48));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        pFooter.add(btnMarcarRondaSelec);
        pFooter.add(btnCobrarRondaSelec);
        pFooter.add(btnCobrarTodo);
        pFooter.add(btnSplitEqual);
        pFooter.add(btnCerrar);
        add(pFooter, BorderLayout.SOUTH);
    }

    private JPanel crearPanelRondas() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(UIUtils.getBgColor());
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

        table = new JTable(model);
        UIUtils.estilarTabla(table);

        // Renderizado con color en la columna "Estado"
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String estadoStr = String.valueOf(value);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("PREPARADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(UIUtils.IS_DARK ? new Color(120, 53, 15) : new Color(254, 243, 199));
                    c.setForeground(UIUtils.IS_DARK ? new Color(252, 211, 77) : new Color(146, 64, 14));
                } else if ("FINALIZADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(UIUtils.IS_DARK ? new Color(6, 78, 59) : new Color(220, 252, 231));
                    c.setForeground(UIUtils.IS_DARK ? new Color(52, 211, 153) : new Color(22, 101, 52));
                } else {
                    c.setBackground(UIUtils.IS_DARK ? new Color(127, 29, 29) : new Color(254, 226, 226));
                    c.setForeground(UIUtils.IS_DARK ? new Color(252, 165, 165) : new Color(153, 27, 27));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));
        TouchScrollHelper.aplicar(scroll);
        p.add(scroll, BorderLayout.CENTER);

        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pActions.setOpaque(false);

        JButton btnNuevaRonda = new JButton("+ Agregar Nueva Ronda");
        btnNuevaRonda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNuevaRonda.setBackground(new Color(245, 158, 11));
        btnNuevaRonda.setForeground(Color.WHITE);
        btnNuevaRonda.setFocusPainted(false);
        btnNuevaRonda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevaRonda.addActionListener(e -> {
            Modelo.CajaDao cajaDao = new Modelo.CajaDao();
            if (!cajaDao.hayCajaAbierta()) {
                ToastNotification.advertencia(parentFrame, "La caja está cerrada. Debe abrir caja para agregar rondas.");
                return;
            }
            dispose();
            sistemaInstance.crearNuevaRondaMesa(numMesa, idSala);
        });
        pActions.add(btnNuevaRonda);
        p.add(pActions, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearPanelPorPersona() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(UIUtils.getBgColor());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control de comensales
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        pTop.setOpaque(false);
        JLabel lblNumPer = new JLabel("Comensales:");
        lblNumPer.setForeground(UIUtils.getTextPrimary());
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

        JTable tablePer = new JTable(model);
        UIUtils.estilarTabla(tablePer);

        JScrollPane scroll = new JScrollPane(tablePer);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));
        TouchScrollHelper.aplicar(scroll);
        p.add(pTop, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }
}
