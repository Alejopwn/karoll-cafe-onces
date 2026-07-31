package Vista;

import Modelo.CajaDao;
import Modelo.CierreCaja;
import Modelo.GastoCaja;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo desacoplado de Gestión POS de Caja, Arqueos y Gastos.
 */
public class ModuloCaja {

    private final JFrame parentFrame;
    private final JTabbedPane mainTabbedPane;
    private final JButton btnNavCaja;
    private final JLabel lblVendedor;
    private final Runnable onTabSwitchClear;

    private JPanel panelCajaMain;
    private JLabel lblEstadoCaja;
    private JLabel lblBaseInicialCaja;
    private JLabel lblVentasEfectivoCaja;
    private JLabel lblVentasTransaccionCaja;
    private JLabel lblGastosCaja;
    private JLabel lblSaldoEsperadoCaja;
    private JButton btnAbrirCajaUI;
    private JButton btnGastoCajaUI;
    private JButton btnCerrarCajaUI;
    private JTable tableGastosCaja;
    private JTable tableHistorialCaja;

    private final CajaDao cajaDao = new CajaDao();

    public ModuloCaja(JFrame parentFrame, JTabbedPane mainTabbedPane, JButton btnNavCaja, JLabel lblVendedor, Runnable onTabSwitchClear) {
        this.parentFrame = parentFrame;
        this.mainTabbedPane = mainTabbedPane;
        this.btnNavCaja = btnNavCaja;
        this.lblVendedor = lblVendedor;
        this.onTabSwitchClear = onTabSwitchClear;
    }

    public void abrirComoVentanaModal(JFrame parent) {
        if (panelCajaMain == null) {
            inicializar();
        }
        JDialog dialog = new JDialog(parent, "💰 Control de Caja Chica y Arqueo POS", true);
        dialog.setSize(980, 640);
        dialog.setLocationRelativeTo(parent);
        dialog.setContentPane(panelCajaMain);
        actualizarEstadoCajaUI();
        dialog.setVisible(true);
    }

    public void inicializar() {
        panelCajaMain = new JPanel(new BorderLayout(15, 15));
        panelCajaMain.setBackground(UIUtils.COLOR_BG_DARK);
        panelCajaMain.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelTop = new JPanel(new BorderLayout(15, 15));
        panelTop.setOpaque(false);

        JPanel panelHeader = new JPanel(new BorderLayout(10, 0));
        panelHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("💰 Control de Caja Chica y Arqueo POS");
        lblTitle.setFont(Sistema.getFontBold(18f));
        lblTitle.setForeground(Color.WHITE);

        lblEstadoCaja = new JLabel("ESTADO: CARGANDO...");
        lblEstadoCaja.setFont(Sistema.getFontBold(13f));
        lblEstadoCaja.setOpaque(true);
        lblEstadoCaja.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        lblEstadoCaja.setBackground(UIUtils.COLOR_PANEL_DARK);
        lblEstadoCaja.setForeground(Color.WHITE);

        panelHeader.add(lblTitle, BorderLayout.WEST);
        panelHeader.add(lblEstadoCaja, BorderLayout.EAST);

        JPanel panelKpis = new JPanel(new GridLayout(1, 5, 12, 0));
        panelKpis.setOpaque(false);
        panelKpis.setPreferredSize(new Dimension(0, 75));

        lblBaseInicialCaja = UIUtils.crearKpiCard("Monto Inicial", "$ 0.00", UIUtils.COLOR_ACCENT_BLUE);
        lblVentasEfectivoCaja = UIUtils.crearKpiCard("Ventas Efectivo", "$ 0.00", UIUtils.COLOR_ACCENT_GREEN);
        lblVentasTransaccionCaja = UIUtils.crearKpiCard("Ventas Transf.", "$ 0.00", UIUtils.COLOR_ACCENT_PURPLE);
        lblGastosCaja = UIUtils.crearKpiCard("Gastos / Egresos", "$ 0.00", UIUtils.COLOR_ACCENT_RED);
        lblSaldoEsperadoCaja = UIUtils.crearKpiCard("Efectivo en Caja", "$ 0.00", UIUtils.COLOR_ACCENT_ORANGE);

        panelKpis.add(lblBaseInicialCaja.getParent());
        panelKpis.add(lblVentasEfectivoCaja.getParent());
        panelKpis.add(lblVentasTransaccionCaja.getParent());
        panelKpis.add(lblGastosCaja.getParent());
        panelKpis.add(lblSaldoEsperadoCaja.getParent());

        panelTop.add(panelHeader, BorderLayout.NORTH);
        panelTop.add(panelKpis, BorderLayout.CENTER);

        JPanel panelAccionesIzq = new JPanel();
        panelAccionesIzq.setLayout(new BoxLayout(panelAccionesIzq, BoxLayout.Y_AXIS));
        panelAccionesIzq.setBackground(new Color(22, 30, 50));
        panelAccionesIzq.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.COLOR_BORDER_DARK),
            BorderFactory.createEmptyBorder(12, 8, 12, 8)
        ));
        panelAccionesIzq.setPreferredSize(new Dimension(185, 0));

        JLabel lblAcciones = new JLabel("Operaciones");
        lblAcciones.setFont(Sistema.getFontBold(14f));
        lblAcciones.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblAcciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelAccionesIzq.add(lblAcciones);
        panelAccionesIzq.add(Box.createVerticalStrut(12));

        btnAbrirCajaUI = UIUtils.crearBoton("🔓 Abrir Turno Caja", UIUtils.COLOR_ACCENT_GREEN);
        btnAbrirCajaUI.addActionListener(e -> abrirModalAperturaCaja());
        panelAccionesIzq.add(btnAbrirCajaUI);
        panelAccionesIzq.add(Box.createVerticalStrut(10));

        btnGastoCajaUI = UIUtils.crearBoton("💸 Registrar Gasto", UIUtils.COLOR_ACCENT_ORANGE);
        btnGastoCajaUI.addActionListener(e -> abrirModalGastoCaja());
        panelAccionesIzq.add(btnGastoCajaUI);
        panelAccionesIzq.add(Box.createVerticalStrut(10));

        btnCerrarCajaUI = UIUtils.crearBoton("🔒 Cerrar / Arqueo", UIUtils.COLOR_ACCENT_RED);
        btnCerrarCajaUI.addActionListener(e -> abrirModalCierreCaja());
        panelAccionesIzq.add(btnCerrarCajaUI);
        panelAccionesIzq.add(Box.createVerticalStrut(10));

        JButton btnRefrescar = UIUtils.crearBoton("🔄 Refrescar Datos", UIUtils.COLOR_BORDER_DARK);
        btnRefrescar.addActionListener(e -> actualizarEstadoCajaUI());
        panelAccionesIzq.add(btnRefrescar);

        panelAccionesIzq.add(Box.createVerticalGlue());

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(Sistema.getFontBold(13f));
        subTabs.setBackground(UIUtils.COLOR_BG_DARK);
        subTabs.setForeground(Color.WHITE);

        tableGastosCaja = new JTable();
        UIUtils.estilarTablaOscura(tableGastosCaja);
        JScrollPane scrollGastos = new JScrollPane(tableGastosCaja);
        scrollGastos.setBackground(UIUtils.COLOR_BG_DARK);
        scrollGastos.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scrollGastos.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));
        subTabs.addTab("Gastos / Egresos del Turno Actual", scrollGastos);

        tableHistorialCaja = new JTable();
        UIUtils.estilarTablaOscura(tableHistorialCaja);
        JScrollPane scrollHistorial = new JScrollPane(tableHistorialCaja);
        scrollHistorial.setBackground(UIUtils.COLOR_BG_DARK);
        scrollHistorial.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scrollHistorial.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));
        subTabs.addTab("Historial de Cierres y Arqueos Anteriores", scrollHistorial);

        JPanel panelCenter = new JPanel(new BorderLayout(0, 0));
        panelCenter.setOpaque(false);
        panelCenter.add(panelAccionesIzq, BorderLayout.WEST);
        panelCenter.add(subTabs, BorderLayout.CENTER);

        panelCajaMain.add(panelTop, BorderLayout.NORTH);
        panelCajaMain.add(panelCenter, BorderLayout.CENTER);

        mainTabbedPane.addTab("Caja", panelCajaMain);
        int indexCaja = mainTabbedPane.getTabCount() - 1;

        if (btnNavCaja != null) {
            btnNavCaja.addActionListener(e -> {
                if (onTabSwitchClear != null) onTabSwitchClear.run();
                actualizarEstadoCajaUI();
                mainTabbedPane.setSelectedIndex(indexCaja);
            });
        }

        actualizarEstadoCajaUI();
    }

    public void actualizarEstadoCajaUI() {
        CierreCaja activa = cajaDao.obtenerCajaActiva();
        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        if (activa == null) {
            lblEstadoCaja.setText("STATUS: TURNO CERRADO");
            lblEstadoCaja.setBackground(new Color(153, 27, 27));
            lblEstadoCaja.setForeground(Color.WHITE);

            lblBaseInicialCaja.setText("$ 0.00");
            lblVentasEfectivoCaja.setText("$ 0.00");
            lblVentasTransaccionCaja.setText("$ 0.00");
            lblGastosCaja.setText("$ 0.00");
            lblSaldoEsperadoCaja.setText("$ 0.00");

            btnAbrirCajaUI.setEnabled(true);
            btnGastoCajaUI.setEnabled(false);
            btnCerrarCajaUI.setEnabled(false);

            cargarTablaGastos(0);
        } else {
            lblEstadoCaja.setText("STATUS: CAJA ABIERTA (Turno #" + activa.getId() + ")");
            lblEstadoCaja.setBackground(new Color(6, 95, 70));
            lblEstadoCaja.setForeground(Color.WHITE);

            double ventasEf = cajaDao.calcularVentasEfectivo(activa.getId(), activa.getFechaApertura());
            double ventasTr = cajaDao.calcularVentasTransaccion(activa.getId(), activa.getFechaApertura());
            double gastos = cajaDao.calcularTotalGastos(activa.getId());
            double saldoEsperado = activa.getMontoInicial() + ventasEf - gastos;

            lblBaseInicialCaja.setText(df.format(activa.getMontoInicial()));
            lblVentasEfectivoCaja.setText(df.format(ventasEf));
            lblVentasTransaccionCaja.setText(df.format(ventasTr));
            lblGastosCaja.setText(df.format(gastos));
            lblSaldoEsperadoCaja.setText(df.format(saldoEsperado));

            btnAbrirCajaUI.setEnabled(false);
            btnGastoCajaUI.setEnabled(true);
            btnCerrarCajaUI.setEnabled(true);

            cargarTablaGastos(activa.getId());
        }

        cargarTablaHistorialCierres();
    }

    private void cargarTablaGastos(int idCierre) {
        List<GastoCaja> lista = cajaDao.listarGastosPorCierre(idCierre);
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Categoría", "Descripción / Motivo", "Monto ($)", "Usuario", "Fecha / Hora"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        DecimalFormat df = new DecimalFormat("$ #,##0.00");
        for (GastoCaja g : lista) {
            model.addRow(new Object[]{
                g.getId(),
                g.getCategoria(),
                g.getDescripcion(),
                df.format(g.getMonto()),
                g.getUsuario(),
                g.getFecha()
            });
        }
        tableGastosCaja.setModel(model);
    }

    private void cargarTablaHistorialCierres() {
        List<CierreCaja> lista = cajaDao.listarHistorialCajas();
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Turno #", "Apertura", "Cierre", "Monto Inicial", "Ventas Ef.", "Ventas Tr.", "Gastos", "Esperado", "Contado", "Diferencia", "Usuario Cierre"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        DecimalFormat df = new DecimalFormat("$ #,##0.00");
        for (CierreCaja c : lista) {
            String fFin = c.getFechaCierre() != null ? c.getFechaCierre() : "EN CURSO";
            model.addRow(new Object[]{
                c.getId(),
                c.getFechaApertura(),
                fFin,
                df.format(c.getMontoInicial()),
                df.format(c.getMontoVentasEfectivo()),
                df.format(c.getMontoVentasTransaccion()),
                df.format(c.getMontoGastos()),
                df.format(c.getMontoEsperadoEfectivo()),
                df.format(c.getMontoRealEfectivo()),
                df.format(c.getDiferencia()),
                c.getUsuario() != null ? c.getUsuario() : "N/A"
            });
        }
        tableHistorialCaja.setModel(model);
    }

    private void abrirModalAperturaCaja() {
        if (cajaDao.obtenerCajaActiva() != null) {
            ToastNotification.advertencia(parentFrame, "Ya existe un turno de caja abierto.");
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "🔓 Apertura de Turno de Caja Chica", true);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UIUtils.COLOR_BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("🔓 Apertura de Caja Chica");
        lblHeader.setFont(Sistema.getFontBold(18f));
        lblHeader.setForeground(Color.WHITE);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 10));
        panelForm.setOpaque(false);

        JLabel l1 = new JLabel("Monto Base Inicial ($):");
        l1.setForeground(Color.WHITE);
        l1.setFont(Sistema.getFontBold(13f));
        JTextField txtMonto = new JTextField("50000");
        txtMonto.setFont(Sistema.getFontBold(16f));

        panelForm.add(l1);
        panelForm.add(txtMonto);

        // Denominaciones Rápidas
        JPanel pDenom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        pDenom.setOpaque(false);
        String[] denoms = {"20000", "50000", "100000", "200000"};
        for (String d : denoms) {
            JButton bD = new JButton("$" + String.format("%,d", Integer.parseInt(d)));
            bD.setFont(Sistema.getFontBold(11f));
            bD.setBackground(new Color(30, 41, 59));
            bD.setForeground(new Color(56, 189, 248));
            bD.setFocusPainted(false);
            bD.addActionListener(e -> txtMonto.setText(d));
            pDenom.add(bD);
        }

        JPanel panelCenter = new JPanel(new BorderLayout(0, 10));
        panelCenter.setOpaque(false);
        panelCenter.add(panelForm, BorderLayout.NORTH);
        panelCenter.add(pDenom, BorderLayout.SOUTH);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtn.setOpaque(false);

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnConfirmar = UIUtils.crearBoton("Abrir Caja", UIUtils.COLOR_ACCENT_GREEN);
        btnConfirmar.setFont(Sistema.getFontBold(14f));
        btnConfirmar.addActionListener(e -> {
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";
                if (cajaDao.abrirCaja(usuarioActual, monto)) {
                    SonidoPOS.reproducirChaching();
                    dialog.dispose();
                    ToastNotification.exito(parentFrame, "Turno de caja abierto exitosamente.");
                    actualizarEstadoCajaUI();
                } else {
                    ToastNotification.error(dialog, "No se pudo abrir la caja.");
                }
            } catch (Exception ex) {
                ToastNotification.error(dialog, "Monto inicial numérico inválido.");
            }
        });

        panelBtn.add(btnCancelar);
        panelBtn.add(btnConfirmar);

        root.add(lblHeader, BorderLayout.NORTH);
        root.add(panelCenter, BorderLayout.CENTER);
        root.add(panelBtn, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void abrirModalGastoCaja() {
        CierreCaja activa = cajaDao.obtenerCajaActiva();
        if (activa == null) {
            ToastNotification.advertencia(parentFrame, "Debe abrir caja antes de registrar gastos.");
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "💸 Registrar Salida de Caja Chica", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UIUtils.COLOR_BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("💸 Registrar Gasto / Egreso de Caja");
        lblHeader.setFont(Sistema.getFontBold(18f));
        lblHeader.setForeground(Color.WHITE);

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 10, 12));
        panelForm.setOpaque(false);

        JLabel l1 = new JLabel("Monto Egreso ($):");
        l1.setForeground(Color.WHITE);
        l1.setFont(Sistema.getFontBold(13f));
        JTextField txtMonto = new JTextField();
        txtMonto.setFont(Sistema.getFontBold(14f));

        JLabel l2 = new JLabel("Categoría:");
        l2.setForeground(Color.WHITE);
        l2.setFont(Sistema.getFontBold(13f));
        JComboBox<String> cbxCat = new JComboBox<>(new String[]{"Compra Insumos", "Transporte / Pasajes", "Servicios / Mantenimiento", "Propinas", "Varios"});

        JLabel l3 = new JLabel("Motivo / Descripción:");
        l3.setForeground(Color.WHITE);
        l3.setFont(Sistema.getFontBold(13f));
        JTextField txtDesc = new JTextField();

        panelForm.add(l1); panelForm.add(txtMonto);
        panelForm.add(l2); panelForm.add(cbxCat);
        panelForm.add(l3); panelForm.add(txtDesc);

        // Chips de Gastos Frecuentes
        JPanel pGastosRapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        pGastosRapidos.setOpaque(false);
        JLabel lblQuick = new JLabel("Gastos Frecuentes: ");
        lblQuick.setFont(Sistema.getFontBold(11f));
        lblQuick.setForeground(new Color(148, 163, 184));
        pGastosRapidos.add(lblQuick);

        String[][] gastos = {
            {"Hielo ($5.000)", "5000", "Compra de hielo", "Varios"},
            {"Pasajes ($10.000)", "10000", "Pasajes / Domicilio", "Transporte / Pasajes"},
            {"Insumos ($20.000)", "20000", "Compra de insumos urgentes", "Compra Insumos"}
        };

        for (String[] g : gastos) {
            JButton bG = new JButton(g[0]);
            bG.setFont(Sistema.getFontBold(10f));
            bG.setBackground(new Color(30, 41, 59));
            bG.setForeground(new Color(251, 146, 60)); // Orange accent
            bG.setFocusPainted(false);
            bG.addActionListener(e -> {
                txtMonto.setText(g[1]);
                txtDesc.setText(g[2]);
                cbxCat.setSelectedItem(g[3]);
            });
            pGastosRapidos.add(bG);
        }

        JPanel panelCenterGasto = new JPanel(new BorderLayout(0, 10));
        panelCenterGasto.setOpaque(false);
        panelCenterGasto.add(panelForm, BorderLayout.NORTH);
        panelCenterGasto.add(pGastosRapidos, BorderLayout.SOUTH);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtn.setOpaque(false);

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = UIUtils.crearBoton("Guardar Gasto", UIUtils.COLOR_ACCENT_ORANGE);
        btnGuardar.setFont(Sistema.getFontBold(14f));
        btnGuardar.addActionListener(e -> {
            try {
                double monto = Double.parseDouble(txtMonto.getText().trim());
                String desc = txtDesc.getText().trim();
                String cat = cbxCat.getSelectedItem().toString();
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";

                if (monto <= 0 || desc.isEmpty()) {
                    ToastNotification.error(dialog, "Ingrese monto positivo y descripción.");
                    return;
                }

                if (cajaDao.registrarGasto(activa.getId(), monto, desc, cat, usuarioActual)) {
                    dialog.dispose();
                    ToastNotification.exito(parentFrame, "Gasto de caja registrado.");
                    actualizarEstadoCajaUI();
                } else {
                    ToastNotification.error(dialog, "Error al registrar el gasto.");
                }
            } catch (Exception ex) {
                ToastNotification.error(dialog, "Monto numérico no válido.");
            }
        });

        panelBtn.add(btnCancelar);
        panelBtn.add(btnGuardar);

        root.add(lblHeader, BorderLayout.NORTH);
        root.add(panelCenterGasto, BorderLayout.CENTER);
        root.add(panelBtn, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void abrirModalCierreCaja() {
        CierreCaja activa = cajaDao.obtenerCajaActiva();
        if (activa == null) {
            ToastNotification.advertencia(parentFrame, "No hay turno de caja abierto.");
            return;
        }

        JDialog dialog = new JDialog(parentFrame, "🔒 Arqueo y Cierre de Caja", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(UIUtils.COLOR_BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("🔒 Arqueo y Cierre de Turno de Caja");
        lblHeader.setFont(Sistema.getFontBold(18f));
        lblHeader.setForeground(Color.WHITE);

        double ventasEf = cajaDao.calcularVentasEfectivo(activa.getId(), activa.getFechaApertura());
        double gastos = cajaDao.calcularTotalGastos(activa.getId());
        double esperado = activa.getMontoInicial() + ventasEf - gastos;
        DecimalFormat df = new DecimalFormat("$ #,##0.00");

        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));
        panelCenter.setOpaque(false);

        JPanel panelResumen = new JPanel(new GridLayout(4, 2, 8, 6));
        panelResumen.setBackground(UIUtils.COLOR_PANEL_DARK);
        panelResumen.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel r1a = new JLabel("Base Inicial:"); r1a.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel r1b = new JLabel(df.format(activa.getMontoInicial())); r1b.setForeground(Color.WHITE); r1b.setFont(Sistema.getFontBold(13f));
        JLabel r2a = new JLabel("(+) Ventas Efectivo:"); r2a.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel r2b = new JLabel(df.format(ventasEf)); r2b.setForeground(UIUtils.COLOR_ACCENT_GREEN); r2b.setFont(Sistema.getFontBold(13f));
        JLabel r3a = new JLabel("(-) Gastos / Egresos:"); r3a.setForeground(UIUtils.COLOR_TEXT_MUTED);
        JLabel r3b = new JLabel(df.format(gastos)); r3b.setForeground(UIUtils.COLOR_ACCENT_RED); r3b.setFont(Sistema.getFontBold(13f));
        JLabel r4a = new JLabel("(=) Esperado Efectivo:"); r4a.setForeground(new Color(96, 165, 250)); r4a.setFont(Sistema.getFontBold(13f));
        JLabel r4b = new JLabel(df.format(esperado)); r4b.setForeground(new Color(96, 165, 250)); r4b.setFont(Sistema.getFontBold(14f));

        panelResumen.add(r1a); panelResumen.add(r1b);
        panelResumen.add(r2a); panelResumen.add(r2b);
        panelResumen.add(r3a); panelResumen.add(r3b);
        panelResumen.add(r4a); panelResumen.add(r4b);

        JPanel panelInputs = new JPanel(new GridLayout(3, 2, 10, 10));
        panelInputs.setOpaque(false);
        panelInputs.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel i1 = new JLabel("Efectivo Contado ($):"); i1.setForeground(Color.WHITE); i1.setFont(Sistema.getFontBold(13f));
        JTextField txtContado = new JTextField();
        txtContado.setFont(Sistema.getFontBold(15f));

        JLabel i2 = new JLabel("Resultado Arqueo:"); i2.setForeground(Color.WHITE); i2.setFont(Sistema.getFontBold(13f));
        JLabel lblDiferenciaBadge = new JLabel("$ 0.00 (SIN CONTAR)");
        lblDiferenciaBadge.setFont(Sistema.getFontBold(12f));
        lblDiferenciaBadge.setOpaque(true);
        lblDiferenciaBadge.setBackground(UIUtils.COLOR_PANEL_DARK);
        lblDiferenciaBadge.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblDiferenciaBadge.setHorizontalAlignment(SwingConstants.CENTER);

        txtContado.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                try {
                    double contado = Double.parseDouble(txtContado.getText().trim());
                    double dif = contado - esperado;
                    if (Math.abs(dif) < 0.01) {
                        lblDiferenciaBadge.setText("CUADRE PERFECTO OK");
                        lblDiferenciaBadge.setBackground(new Color(6, 95, 70));
                        lblDiferenciaBadge.setForeground(Color.WHITE);
                    } else if (dif > 0) {
                        lblDiferenciaBadge.setText("SOBRANTE: " + df.format(dif));
                        lblDiferenciaBadge.setBackground(new Color(30, 58, 138));
                        lblDiferenciaBadge.setForeground(Color.WHITE);
                    } else {
                        lblDiferenciaBadge.setText("FALTANTE: " + df.format(Math.abs(dif)));
                        lblDiferenciaBadge.setBackground(new Color(153, 27, 27));
                        lblDiferenciaBadge.setForeground(Color.WHITE);
                    }
                } catch (Exception e) {
                    lblDiferenciaBadge.setText("$ 0.00 (SIN CONTAR)");
                    lblDiferenciaBadge.setBackground(UIUtils.COLOR_PANEL_DARK);
                    lblDiferenciaBadge.setForeground(UIUtils.COLOR_TEXT_MUTED);
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JLabel i3 = new JLabel("Observaciones:"); i3.setForeground(Color.WHITE); i3.setFont(Sistema.getFontBold(13f));
        JTextField txtObs = new JTextField();

        panelInputs.add(i1); panelInputs.add(txtContado);
        panelInputs.add(i2); panelInputs.add(lblDiferenciaBadge);
        panelInputs.add(i3); panelInputs.add(txtObs);

        panelCenter.add(panelResumen);
        panelCenter.add(panelInputs);

        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtn.setOpaque(false);

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnCerrar = UIUtils.crearBoton("Confirmar Cierre de Caja", UIUtils.COLOR_ACCENT_RED);
        btnCerrar.setFont(Sistema.getFontBold(14f));
        btnCerrar.addActionListener(e -> {
            try {
                double contado = Double.parseDouble(txtContado.getText().trim());
                String obs = txtObs.getText().trim();
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";
                if (cajaDao.cerrarCaja(activa.getId(), contado, obs)) {
                    SonidoPOS.reproducirChaching();
                    dialog.dispose();
                    ToastNotification.exito(parentFrame, "Turno de caja cerrado exitosamente.");
                    actualizarEstadoCajaUI();
                } else {
                    ToastNotification.error(dialog, "Error al procesar el cierre de caja.");
                }
            } catch (Exception ex) {
                ToastNotification.error(dialog, "Monto contado numérico inválido.");
            }
        });

        panelBtn.add(btnCancelar);
        panelBtn.add(btnCerrar);

        root.add(lblHeader, BorderLayout.NORTH);
        root.add(panelCenter, BorderLayout.CENTER);
        root.add(panelBtn, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
