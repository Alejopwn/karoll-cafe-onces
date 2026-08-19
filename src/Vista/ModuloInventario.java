package Vista;

import Modelo.Inventario;
import Modelo.InventarioDao;
import Modelo.MovimientoInventario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo desacoplado de Gestión de Inventario para Restaurante Comuneros POS.
 */
public class ModuloInventario {

    private final JFrame parentFrame;
    private final JTabbedPane mainTabbedPane;
    private final JButton btnNavInventario;
    private final JLabel lblVendedor;
    private final Runnable onTabSwitchClear;

    private JPanel panelInventarioMain;
    private JTable tableInventario;
    private JTable tableMovimientos;
    private JTextField txtBuscarInventario;
    private JComboBox<String> cbxCategoriaFilter;
    private JLabel lblTotalProdInv;
    private JLabel lblAlertasStockInv;
    private JLabel lblValorTotalInv;
    private final InventarioDao invDao = new InventarioDao();

    public ModuloInventario(JFrame parentFrame, JTabbedPane mainTabbedPane, JButton btnNavInventario, JLabel lblVendedor, Runnable onTabSwitchClear) {
        this.parentFrame = parentFrame;
        this.mainTabbedPane = mainTabbedPane;
        this.btnNavInventario = btnNavInventario;
        this.lblVendedor = lblVendedor;
        this.onTabSwitchClear = onTabSwitchClear;
    }

    public void abrirComoVentanaModal(JFrame parent) {
        if (panelInventarioMain == null) {
            inicializar();
        }
        JDialog dialog = new JDialog(parent, "📦 Catálogo e Inventario de Productos", true);
        dialog.setSize(1020, 660);
        dialog.setLocationRelativeTo(parent);
        dialog.setContentPane(panelInventarioMain);
        actualizarComboCategoriasInv();
        cargarTablaInventario("");
        cargarTablaMovimientos("");
        dialog.setVisible(true);
    }

    public void inicializar() {
        panelInventarioMain = new JPanel(new BorderLayout(10, 10));
        panelInventarioMain.setBackground(UIUtils.COLOR_BG_DARK);
        panelInventarioMain.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelTop = new JPanel(new BorderLayout(10, 10));
        panelTop.setOpaque(false);

        JPanel panelKpis = new JPanel(new GridLayout(1, 3, 15, 0));
        panelKpis.setOpaque(false);
        panelKpis.setPreferredSize(new Dimension(0, 65));

        lblTotalProdInv = UIUtils.crearKpiCard("Total Productos", "0", UIUtils.COLOR_ACCENT_BLUE);
        lblAlertasStockInv = UIUtils.crearKpiCard("Stock Bajo / Agotado", "0", UIUtils.COLOR_ACCENT_RED);
        lblValorTotalInv = UIUtils.crearKpiCard("Valor del Inventario", "$ 0.00", UIUtils.COLOR_ACCENT_GREEN);

        panelKpis.add(lblTotalProdInv.getParent());
        panelKpis.add(lblAlertasStockInv.getParent());
        panelKpis.add(lblValorTotalInv.getParent());

        panelTop.add(panelKpis, BorderLayout.NORTH);

        // Barra de Búsqueda
        JPanel panelSearchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSearchBar.setBackground(UIUtils.COLOR_PANEL_DARK);
        panelSearchBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));

        JLabel lblSearch = new JLabel("🔍 Buscar (SKU / Nombre):");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtBuscarInventario = new JTextField(18);
        txtBuscarInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscarInventario.setForeground(Color.WHITE);
        txtBuscarInventario.setBackground(UIUtils.COLOR_BG_DARK);
        txtBuscarInventario.setCaretColor(Color.WHITE);
        txtBuscarInventario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtBuscarInventario.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                cargarTablaInventario(txtBuscarInventario.getText());
            }
        });

        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setForeground(Color.WHITE);
        lblCat.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cbxCategoriaFilter = new JComboBox<>();
        cbxCategoriaFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbxCategoriaFilter.setBackground(UIUtils.COLOR_PANEL_DARK);
        cbxCategoriaFilter.setForeground(Color.WHITE);
        cbxCategoriaFilter.addActionListener(e -> cargarTablaInventario(txtBuscarInventario.getText()));

        JButton btnFilterBajoStock = new JButton("⚠️ Solo Stock Bajo");
        btnFilterBajoStock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFilterBajoStock.setBackground(new Color(220, 38, 38)); // Red
        btnFilterBajoStock.setForeground(Color.WHITE);
        btnFilterBajoStock.setFocusPainted(false);
        btnFilterBajoStock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFilterBajoStock.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) tableInventario.getModel();
            model.setRowCount(0);
            List<Inventario> bajoStock = invDao.obtenerProductosStockBajo();
            for (Inventario inv : bajoStock) {
                Object[] fila = {
                    inv.getId(), inv.getCodigo(), inv.getNombre(), inv.getCategoria(),
                    inv.getStock(), inv.getStockMinimo(), inv.getUnidadMedida(), inv.getPrecioCompra(), inv.getFechaActualizacion()
                };
                model.addRow(fila);
            }
            ToastNotification.advertencia(parentFrame, "Filtrando: " + bajoStock.size() + " producto(s) en alerta de stock.");
        });

        panelSearchBar.add(lblSearch);
        panelSearchBar.add(txtBuscarInventario);
        panelSearchBar.add(lblCat);
        panelSearchBar.add(cbxCategoriaFilter);
        panelSearchBar.add(btnFilterBajoStock);

        panelTop.add(panelSearchBar, BorderLayout.SOUTH);

        // Sidebar Acciones
        JPanel panelAccionesIzq = new JPanel();
        panelAccionesIzq.setLayout(new BoxLayout(panelAccionesIzq, BoxLayout.Y_AXIS));
        panelAccionesIzq.setBackground(new Color(22, 30, 50));
        panelAccionesIzq.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.COLOR_BORDER_DARK),
            BorderFactory.createEmptyBorder(12, 8, 12, 8)
        ));
        panelAccionesIzq.setPreferredSize(new Dimension(170, 0));

        JLabel lblAcciones = new JLabel("Acciones");
        lblAcciones.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAcciones.setForeground(UIUtils.COLOR_TEXT_MUTED);
        lblAcciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelAccionesIzq.add(lblAcciones);
        panelAccionesIzq.add(Box.createVerticalStrut(12));

        JButton btnNuevo = UIUtils.crearBoton("＋ Nuevo Producto", UIUtils.COLOR_ACCENT_GREEN);
        btnNuevo.addActionListener(e -> abrirModalNuevoProducto(null));
        panelAccionesIzq.add(btnNuevo);
        panelAccionesIzq.add(Box.createVerticalStrut(8));

        JButton btnEntrada = UIUtils.crearBoton("▲ Entrada Stock", UIUtils.COLOR_ACCENT_BLUE);
        btnEntrada.addActionListener(e -> abrirModalAjusteStock(true));
        panelAccionesIzq.add(btnEntrada);
        panelAccionesIzq.add(Box.createVerticalStrut(8));

        JButton btnSalida = UIUtils.crearBoton("▼ Salida / Merma", UIUtils.COLOR_ACCENT_RED);
        btnSalida.addActionListener(e -> abrirModalAjusteStock(false));
        panelAccionesIzq.add(btnSalida);
        panelAccionesIzq.add(Box.createVerticalStrut(8));

        JButton btnReload = UIUtils.crearBoton("🔄 Recargar", UIUtils.COLOR_BORDER_DARK);
        btnReload.addActionListener(e -> {
            txtBuscarInventario.setText("");
            actualizarComboCategoriasInv();
            cargarTablaInventario("");
            cargarTablaMovimientos("");
        });
        panelAccionesIzq.add(btnReload);
        panelAccionesIzq.add(Box.createVerticalStrut(8));

        JButton btnExportar = UIUtils.crearBoton("📊 Exportar CSV", UIUtils.COLOR_ACCENT_PURPLE);
        btnExportar.addActionListener(e -> exportarInventarioCSV());
        panelAccionesIzq.add(btnExportar);

        panelAccionesIzq.add(Box.createVerticalGlue());

        // Tablas Centrales
        JTabbedPane subTabbedPane = new JTabbedPane();
        subTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subTabbedPane.setBackground(UIUtils.COLOR_BG_DARK);
        subTabbedPane.setForeground(Color.WHITE);
        subTabbedPane.setOpaque(true);

        tableInventario = new JTable();
        UIUtils.estilarTablaOscura(tableInventario);
        JScrollPane scrollInventario = new JScrollPane(tableInventario);
        scrollInventario.setBackground(UIUtils.COLOR_BG_DARK);
        scrollInventario.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scrollInventario.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));
        subTabbedPane.addTab("Catálogo de Productos y Existencias", scrollInventario);

        JPopupMenu popupInv = new JPopupMenu();
        JMenuItem itemEditar = new JMenuItem("✏️ Editar Producto");
        itemEditar.addActionListener(e -> {
            int row = tableInventario.getSelectedRow();
            if (row >= 0) {
                int id = Integer.parseInt(tableInventario.getValueAt(row, 0).toString());
                Inventario inv = invDao.buscarPorId(id);
                if (inv != null) abrirModalNuevoProducto(inv);
            }
        });

        JMenuItem itemEliminar = new JMenuItem("🗑️ Eliminar Producto");
        itemEliminar.addActionListener(e -> {
            int row = tableInventario.getSelectedRow();
            if (row >= 0) {
                int id = Integer.parseInt(tableInventario.getValueAt(row, 0).toString());
                String nombre = tableInventario.getValueAt(row, 2).toString();
                boolean confirm = ModalAlerta.confirmar(parentFrame,
                        "Confirmar Eliminación",
                        "¿Desea eliminar el producto <b>" + nombre + "</b> del inventario?",
                        "Sí, Eliminar", "Cancelar");
                if (confirm) {
                    if (invDao.eliminar(id)) {
                        ToastNotification.exito(parentFrame, "Producto eliminado exitosamente.");
                        cargarTablaInventario(txtBuscarInventario.getText());
                    }
                }
            }
        });

        JMenuItem itemEntrada10 = new JMenuItem("▲ Entrada Rápida (+10 Stock)");
        itemEntrada10.addActionListener(e -> {
            int row = tableInventario.getSelectedRow();
            if (row >= 0) {
                int id = Integer.parseInt(tableInventario.getValueAt(row, 0).toString());
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";
                if (invDao.ajustarStock(id, "ENTRADA", 10.0, "Entrada rápida +10", usuarioActual)) {
                    ToastNotification.exito(parentFrame, "Stock +10 actualizado.");
                    cargarTablaInventario(txtBuscarInventario.getText());
                }
            }
        });

        JMenuItem itemSalida1 = new JMenuItem("▼ Merma / Salida Rápida (-1)");
        itemSalida1.addActionListener(e -> {
            int row = tableInventario.getSelectedRow();
            if (row >= 0) {
                int id = Integer.parseInt(tableInventario.getValueAt(row, 0).toString());
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";
                if (invDao.ajustarStock(id, "MERMA", 1.0, "Salida rápida -1", usuarioActual)) {
                    ToastNotification.exito(parentFrame, "Stock -1 actualizado.");
                    cargarTablaInventario(txtBuscarInventario.getText());
                }
            }
        });

        popupInv.add(itemEntrada10);
        popupInv.add(itemSalida1);
        popupInv.addSeparator();
        popupInv.add(itemEditar);
        popupInv.add(itemEliminar);
        tableInventario.setComponentPopupMenu(popupInv);

        tableMovimientos = new JTable();
        UIUtils.estilarTablaOscura(tableMovimientos);

        JScrollPane scrollMovimientos = new JScrollPane(tableMovimientos);
        scrollMovimientos.setBackground(UIUtils.COLOR_BG_DARK);
        scrollMovimientos.getViewport().setBackground(UIUtils.COLOR_BG_DARK);
        scrollMovimientos.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1));
        subTabbedPane.addTab("Historial de Entradas, Salidas y Mermas", scrollMovimientos);

        JPanel panelContenido = new JPanel(new BorderLayout(0, 0));
        panelContenido.setOpaque(false);
        panelContenido.add(panelAccionesIzq, BorderLayout.WEST);
        panelContenido.add(subTabbedPane, BorderLayout.CENTER);

        panelInventarioMain.add(panelTop, BorderLayout.NORTH);
        panelInventarioMain.add(panelContenido, BorderLayout.CENTER);

        mainTabbedPane.addTab("Inventario", panelInventarioMain);
        int indexInventario = mainTabbedPane.getTabCount() - 1;

        if (btnNavInventario != null) {
            btnNavInventario.addActionListener(e -> {
                if (onTabSwitchClear != null) onTabSwitchClear.run();
                actualizarComboCategoriasInv();
                cargarTablaInventario("");
                cargarTablaMovimientos("");
                mainTabbedPane.setSelectedIndex(indexInventario);
            });
        }

        actualizarComboCategoriasInv();
        cargarTablaInventario("");
        cargarTablaMovimientos("");
        verificarAlertasStockInicial();
    }

    private void actualizarComboCategoriasInv() {
        if (cbxCategoriaFilter == null) return;
        cbxCategoriaFilter.removeAllItems();
        List<String> cats = invDao.obtenerCategorias();
        for (String c : cats) cbxCategoriaFilter.addItem(c);
    }

    private void cargarTablaInventario(String busqueda) {
        String catSel = cbxCategoriaFilter != null && cbxCategoriaFilter.getSelectedItem() != null ? cbxCategoriaFilter.getSelectedItem().toString() : "Todas";
        List<Inventario> lista = invDao.listar(busqueda, catSel);

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Código/SKU", "Producto", "Categoría", "Stock Actual", "Stock Mínimo", "Unidad", "P. Compra", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        DecimalFormat df = new DecimalFormat("$ #,##0.00");
        int countBajoStock = 0;
        double valorTotal = 0.0;

        for (Inventario item : lista) {
            String estado = "EN STOCK";
            if (item.getStock() <= 0) {
                estado = "AGOTADO";
                countBajoStock++;
            } else if (item.getStock() <= item.getStockMinimo()) {
                estado = "STOCK BAJO";
                countBajoStock++;
            }
            valorTotal += (item.getStock() * item.getPrecioCompra());

            model.addRow(new Object[]{
                item.getId(),
                item.getCodigo() != null ? item.getCodigo() : "N/A",
                item.getNombre(),
                item.getCategoria(),
                item.getStock(),
                item.getStockMinimo(),
                item.getUnidadMedida(),
                df.format(item.getPrecioCompra()),
                estado
            });
        }

        tableInventario.setModel(model);

        tableInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = table.getValueAt(row, 8) != null ? table.getValueAt(row, 8).toString() : "";

                if (!isSelected) {
                    if (column == 8) {
                        if ("AGOTADO".equalsIgnoreCase(estado)) {
                            c.setBackground(new Color(153, 27, 27));
                            c.setForeground(Color.WHITE);
                        } else if ("STOCK BAJO".equalsIgnoreCase(estado)) {
                            c.setBackground(new Color(180, 83, 9));
                            c.setForeground(Color.WHITE);
                        } else {
                            c.setBackground(new Color(6, 95, 70));
                            c.setForeground(Color.WHITE);
                        }
                        setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        if (row % 2 == 0) c.setBackground(UIUtils.COLOR_PANEL_DARK);
                        else c.setBackground(UIUtils.COLOR_BG_DARK);
                        c.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
                        setHorizontalAlignment(column == 0 || column == 4 || column == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    }
                } else {
                    c.setBackground(UIUtils.COLOR_ACCENT_BLUE);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        if (lblTotalProdInv != null) lblTotalProdInv.setText(String.valueOf(lista.size()));
        if (lblAlertasStockInv != null) lblAlertasStockInv.setText(String.valueOf(countBajoStock));
        if (lblValorTotalInv != null) lblValorTotalInv.setText(df.format(valorTotal));
    }

    private void cargarTablaMovimientos(String busqueda) {
        List<MovimientoInventario> lista = invDao.listarMovimientos(busqueda);
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Producto", "Tipo Movimiento", "Cantidad", "Motivo / Notas", "Usuario", "Fecha / Hora"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (MovimientoInventario m : lista) {
            model.addRow(new Object[]{
                m.getId(),
                m.getNombreProducto(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getMotivo(),
                m.getUsuario(),
                m.getFecha()
            });
        }
        tableMovimientos.setModel(model);
    }

    private void verificarAlertasStockInicial() {
        List<Inventario> bajoStock = invDao.obtenerProductosStockBajo();
        if (!bajoStock.isEmpty()) {
            ToastNotification.advertencia(parentFrame, "Atención: Hay " + bajoStock.size() + " producto(s) en stock crítico.");
        }
    }

    private void abrirModalNuevoProducto(Inventario invEditar) {
        JDialog dialog = new JDialog(parentFrame, invEditar == null ? "Nuevo Producto de Inventario" : "Editar Producto de Inventario", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));
        dialog.getContentPane().setBackground(UIUtils.COLOR_PANEL_DARK);
        ((JPanel) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtCod = new JTextField(invEditar != null && invEditar.getCodigo() != null ? invEditar.getCodigo() : "");
        JTextField txtNom = new JTextField(invEditar != null ? invEditar.getNombre() : "");
        JTextField txtCat = new JTextField(invEditar != null ? invEditar.getCategoria() : "General");
        JTextField txtStock = new JTextField(invEditar != null ? String.valueOf(invEditar.getStock()) : "0");
        JTextField txtStockMin = new JTextField(invEditar != null ? String.valueOf(invEditar.getStockMinimo()) : "5");
        JComboBox<String> cbxUnidad = new JComboBox<>(new String[]{"Unidades", "Kg", "Gramos", "Litros", "Cajas", "Paquetes", "Porciones"});
        if (invEditar != null && invEditar.getUnidadMedida() != null) cbxUnidad.setSelectedItem(invEditar.getUnidadMedida());
        JTextField txtPrecio = new JTextField(invEditar != null ? String.valueOf(invEditar.getPrecioCompra()) : "0.0");

        dialog.add(crearLabelDialog("Código / SKU:")); dialog.add(txtCod);
        dialog.add(crearLabelDialog("Nombre Producto:")); dialog.add(txtNom);
        dialog.add(crearLabelDialog("Categoría:")); dialog.add(txtCat);
        dialog.add(crearLabelDialog("Stock Inicial:")); dialog.add(txtStock);
        dialog.add(crearLabelDialog("Stock Mínimo:")); dialog.add(txtStockMin);
        dialog.add(crearLabelDialog("Unidad de Medida:")); dialog.add(cbxUnidad);
        dialog.add(crearLabelDialog("Precio Compra ($):")); dialog.add(txtPrecio);

        JButton btnGuardar = UIUtils.crearBoton("Guardar", UIUtils.COLOR_ACCENT_GREEN);
        btnGuardar.addActionListener(e -> {
            if (txtNom.getText().trim().isEmpty()) {
                ModalAlerta.advertencia(dialog, "Campo Requerido", "El <b>nombre del producto</b> es obligatorio.");
                return;
            }
            try {
                Inventario item = invEditar != null ? invEditar : new Inventario();
                item.setCodigo(txtCod.getText().trim());
                item.setNombre(txtNom.getText().trim());
                item.setCategoria(txtCat.getText().trim());
                item.setStock(Double.parseDouble(txtStock.getText().trim()));
                item.setStockMinimo(Double.parseDouble(txtStockMin.getText().trim()));
                item.setUnidadMedida(cbxUnidad.getSelectedItem().toString());
                item.setPrecioCompra(Double.parseDouble(txtPrecio.getText().trim()));

                boolean ok = invEditar == null ? invDao.registrar(item) : invDao.modificar(item);
                if (ok) {
                    ToastNotification.exito(parentFrame, "Producto guardado con éxito.");
                    actualizarComboCategoriasInv();
                    cargarTablaInventario("");
                    dialog.dispose();
                } else {
                    ModalAlerta.error(dialog, "Error al Guardar", "Error al guardar el producto. Verifica que el código no esté duplicado.");
                }
            } catch (Exception ex) {
                ModalAlerta.error(dialog, "Valores Inválidos", "Verifique los valores numéricos ingresados: " + ex.getMessage());
            }
        });

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(btnGuardar);
        dialog.add(btnCancelar);
        dialog.setVisible(true);
    }

    private void abrirModalAjusteStock(boolean esEntrada) {
        List<Inventario> prods = invDao.listar("", "Todas");
        if (prods.isEmpty()) {
            ModalAlerta.advertencia(parentFrame, "Sin Productos", "No hay productos en inventario para ajustar.");
            return;
        }

        JDialog dialog = new JDialog(parentFrame, esEntrada ? "Registrar Entrada de Stock" : "Registrar Salida / Merma", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.getContentPane().setBackground(UIUtils.COLOR_PANEL_DARK);
        ((JPanel) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<String> cbxProds = new JComboBox<>();
        for (Inventario p : prods) {
            cbxProds.addItem(p.getId() + " - " + p.getNombre() + " (Actual: " + p.getStock() + ")");
        }

        JTextField txtCant = new JTextField("1");
        JTextField txtMotivo = new JTextField(esEntrada ? "Compra a proveedor" : "Descarte / Merma");

        dialog.add(crearLabelDialog("Producto:")); dialog.add(cbxProds);
        dialog.add(crearLabelDialog("Cantidad:")); dialog.add(txtCant);
        dialog.add(crearLabelDialog("Motivo / Notas:")); dialog.add(txtMotivo);

        JButton btnConfirmar = UIUtils.crearBoton("Confirmar", esEntrada ? UIUtils.COLOR_ACCENT_BLUE : UIUtils.COLOR_ACCENT_RED);
        btnConfirmar.addActionListener(e -> {
            try {
                int index = cbxProds.getSelectedIndex();
                if (index < 0) return;
                int idInv = prods.get(index).getId();
                double cant = Double.parseDouble(txtCant.getText().trim());
                String tipo = esEntrada ? "ENTRADA" : "SALIDA";
                String usuarioActual = lblVendedor != null ? lblVendedor.getText().trim() : "Sistema";

                if (invDao.ajustarStock(idInv, tipo, cant, txtMotivo.getText().trim(), usuarioActual)) {
                    ToastNotification.exito(parentFrame, "Ajuste de stock registrado.");
                    cargarTablaInventario("");
                    cargarTablaMovimientos("");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "No se pudo realizar el ajuste.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Valor de cantidad no válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancelar = UIUtils.crearBoton("Cancelar", new Color(107, 114, 128));
        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(btnConfirmar);
        dialog.add(btnCancelar);
        dialog.setVisible(true);
    }

    private JLabel crearLabelDialog(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private void exportarInventarioCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte de Inventario CSV");
            fileChooser.setSelectedFile(new File("Inventario_Comuneros_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
            int userSelection = fileChooser.showSaveDialog(parentFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), "UTF-8"))) {
                    pw.println("ID,Codigo,Producto,Categoria,StockActual,StockMinimo,UnidadMedida,PrecioCompra,Estado");
                    for (int i = 0; i < tableInventario.getRowCount(); i++) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < tableInventario.getColumnCount(); j++) {
                            Object val = tableInventario.getValueAt(i, j);
                            sb.append("\"").append(val != null ? val.toString().replace("\"", "\"\"") : "").append("\"");
                            if (j < tableInventario.getColumnCount() - 1) sb.append(",");
                        }
                        pw.println(sb.toString());
                    }
                }
                ToastNotification.exito(parentFrame, "Inventario exportado a CSV exitosamente.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Error al exportar inventario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
