/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Modelo.Config;
import Modelo.DetallePedido;
import Modelo.Eventos;
import Modelo.LoginDao;
import Modelo.Pedido;
import Modelo.PedidosDao;
import Modelo.Plato;
import Modelo.PlatosDao;
import Modelo.Sala;
import Modelo.SalasDao;
import Modelo.EstiloTablas;
import Modelo.Login;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import Modelo.ImpresionTicket;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public final class Sistema extends javax.swing.JFrame {

    Sala sl = new Sala();
    SalasDao slDao = new SalasDao();
    Config conf = new Config();
    Eventos event = new Eventos();
    Plato pla = new Plato();
    PlatosDao plaDao = new PlatosDao();
    Pedido ped = new Pedido();
    PedidosDao pedDao = new PedidosDao();
    DetallePedido detPedido = new DetallePedido();
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel tmp = new DefaultTableModel();
    LoginDao lgDao = new LoginDao();
    int item;
    double Totalpagar = 0.0;
    Date fechaActual = new Date();
    String fechaFormato;
    private String userRol = "Administrador";

    public Sistema(Login priv) {
        if (priv != null && priv.getRol() != null) {
            this.userRol = priv.getRol();
        }
        initComponents();
        cargarPedidosDelDia(); // Cargar pedidos al iniciar la aplicación
        cargarSalasCombo(); // AÃ±ade esta lÃ­nea
        ImageIcon img = new ImageIcon(getClass().getResource("/Img/pizzeria.png"));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        int logoW = labelLogo.getWidth() > 0 ? labelLogo.getWidth() : 199;
        int logoH = labelLogo.getHeight() > 0 ? labelLogo.getHeight() : 150;
        Image igmEscalada = img.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        Icon icono = new ImageIcon(igmEscalada);
        labelLogo.setIcon(icono);
        this.setIconImage(img.getImage());
        this.setLocationRelativeTo(null);
        txtIdHistorialPedido.setVisible(false);
        txtIdConfig.setVisible(false);
        btnEfectivo.setVisible(false);
        btnTransaccion.setVisible(false);
        jComboSalas.setVisible(false);
        this.setTitle("Panel de Administración");

        btnModificarUsua.setEnabled(false);

        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        // Estilos modernos en botones principales (hover visual suave + táctil 55px)
        javax.swing.JButton[] btnsPrincipales = { btnSala, btnCaja, btnInventario, btnUsuarios, btnConfig, btnPlatos,
                btnVentas };
        for (javax.swing.JButton btn : btnsPrincipales) {
            btn.setBackground(new java.awt.Color(60, 63, 65));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            btn.setPreferredSize(new java.awt.Dimension(160, 55));
            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(90, 93, 95));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(60, 63, 65));
                }
            });
        }

        // Configuración visual del rol de usuario
        LabelVendedor.setOpaque(true);
        LabelVendedor.setForeground(java.awt.Color.WHITE);
        LabelVendedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        LabelVendedor.setText(" " + (priv != null ? priv.getNombre() : "Usuario") + " ");

        if (!esAdmin()) {
            btnSala.setEnabled(true); // ✅ PERMITIR TOMAR PEDIDOS Y USAR SALAS
            btnConfig.setEnabled(false);
            btnEliminarPedido.setEnabled(false);
            btnUsuarios.setEnabled(false);
            btnEliminarPlato.setEnabled(false);
            btnEliminarSala.setEnabled(false);
            btnInventario.setEnabled(false);
            btnCaja.setEnabled(true);
            LabelVendedor.setBackground(new java.awt.Color(230, 126, 34)); // Naranja para Asistente
        } else {
            LabelVendedor.setBackground(new java.awt.Color(39, 174, 96)); // Verde para Admin
            btnSala.setEnabled(true);
            btnEliminarPlato.setEnabled(true);
            btnEliminarSala.setEnabled(true);
            btnInventario.setEnabled(true);
            btnCaja.setEnabled(true);
        }

        // Reloj en tiempo real
        javax.swing.Timer timerReloj = new javax.swing.Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            jLabel38.setText("COMUNEROS - " + sdf.format(new java.util.Date()));
        });
        timerReloj.start();

        // 🔔 Gestor de alarmas y notificaciones en segundo plano
        Modelo.AlarmManager alarmManager = new Modelo.AlarmManager();
        alarmManager.iniciar(this);

        txtIdConfig.setVisible(false);
        txtIdHistorialPedido.setVisible(false);
        txtIdPedido.setVisible(false);
        txtIdPlato.setVisible(false);
        txtIdSala.setVisible(false);
        txtTempIdSala.setVisible(false);
        txtTempNumMesa.setVisible(false);
        jTabbedPane1.setEnabled(false);

        // UX: Estilos Finalizar Pedido
        btnFinalizar.setBackground(new java.awt.Color(46, 204, 113)); // Verde esmeralda
        btnFinalizar.setForeground(java.awt.Color.WHITE);
        btnFinalizar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        totalFinalizar.setForeground(new java.awt.Color(46, 204, 113));
        totalFinalizar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // UX: Mejoras en Tablas (Hover + Zebra + Táctil 48px)
        javax.swing.JTable[] tablas = { tableSala, TableUsuarios, TablePlatos, TablePedidos, tableMenu, tableFinalizar,
                tblTemPlatos };
        for (javax.swing.JTable t : tablas) {
            t.setRowHeight(48); // Altura optimizada para pantalla táctil
            t.setSelectionBackground(new java.awt.Color(41, 128, 185)); // Azul brillante al seleccionar
            t.setSelectionForeground(java.awt.Color.WHITE);
            t.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
            if (t.getParent() instanceof javax.swing.JViewport) {
                javax.swing.JViewport vp = (javax.swing.JViewport) t.getParent();
                if (vp.getParent() instanceof javax.swing.JScrollPane) {
                    TouchScrollHelper.aplicar((javax.swing.JScrollPane) vp.getParent());
                }
            }
            // Hover (MouseMotionListener)
            t.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                int lastRow = -1;

                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    int row = t.rowAtPoint(e.getPoint());
                    if (row != lastRow) {
                        lastRow = row;
                        t.repaint();
                    }
                }
            });
            // Restaurar repintado al salir de la tabla
            t.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    t.repaint();
                }
            });
        }

        // Asignar el renderizador de cebra/hover (excepto en TablePedidos que usa
        // Tables.java)
        javax.swing.table.DefaultTableCellRenderer zebraRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (!isSelected) {
                    java.awt.Point p = table.getMousePosition();
                    int hoverRow = p != null ? table.rowAtPoint(p) : -1;
                    if (row == hoverRow) {
                        c.setBackground(new java.awt.Color(51, 65, 85)); // Hover (Slate 700)
                    } else if (row % 2 == 0) {
                        c.setBackground(new java.awt.Color(30, 41, 59)); // Cebra par (Slate 800)
                    } else {
                        c.setBackground(new java.awt.Color(15, 23, 42)); // Cebra impar (Slate 900)
                    }
                    c.setForeground(new java.awt.Color(241, 245, 249)); // Texto claro
                } else {
                    c.setBackground(new java.awt.Color(59, 130, 246)); // Fila seleccionada (Azul)
                    c.setForeground(java.awt.Color.WHITE);
                }
                return c;
            }
        };
        tableSala.setDefaultRenderer(Object.class, zebraRenderer);
        TableUsuarios.setDefaultRenderer(Object.class, zebraRenderer);
        TablePlatos.setDefaultRenderer(Object.class, zebraRenderer);
        tableMenu.setDefaultRenderer(Object.class, zebraRenderer);
        tableFinalizar.setDefaultRenderer(Object.class, zebraRenderer);
        tblTemPlatos.setDefaultRenderer(Object.class, zebraRenderer);

        // Validaciones numÃ©ricas para prevenir crashes
        aplicarFiltroNumerico(txtMesas, false); // Solo enteros
        aplicarFiltroNumerico(txtPrecioPlato, true); // Permite decimales

        // UX: Letras blancas sobre fondo oscuro en inputs
        javax.swing.JTextField[] camposInput = { txtNombreSala, txtMesas, txtTelefonoConfig, txtDireccionConfig,
                txtMensaje, txtRucConfig, txtNombreConfig, txtCorreo, txtPass, txtNombre, txtNombrePlato,
                txtPrecioPlato, txtBuscarPlato };
        for (javax.swing.JTextField campo : camposInput) {
            campo.setForeground(java.awt.Color.WHITE);
        }

        decorarSistemaUI();
        panelSalas();
        initModuloInventario();
        initModuloCaja();
        moduloUsuarios = new ModuloUsuarios(this);
        moduloConfig = new ModuloConfig(this);

        // 📱 Iniciar servidor web para meseros con Android
        new Thread(() -> {
            ServidorWebMesero.iniciar();
            javax.swing.SwingUtilities.invokeLater(() -> {
                String url = ServidorWebMesero.getUrlAcceso();
                System.out.println("[POS] 📱 Panel de meseros Android: " + url);
                // Mostrar toast de bienvenida con la IP
                if (ServidorWebMesero.isCorriendo()) {
                    ToastNotification.mostrar(Sistema.this, "📱 Panel Android activo: " + url,
                            ToastNotification.Tipo.INFO);
                }
            });
        }, "ServidorWebMesero").start();

        KeyMapperPOS.registrarAtajosGlobales(this, new KeyMapperPOS.POSKeyAction() {
            @Override
            public void onF1() {
                btnSala.doClick();
            }

            @Override
            public void onF2() {
                if (moduloCaja != null)
                    moduloCaja.abrirComoVentanaModal(Sistema.this);
            }

            @Override
            public void onF3() {
                if (!esAdmin()) {
                    JOptionPane.showMessageDialog(Sistema.this,
                            "Acceso restringido: Solo el Administrador puede acceder al Inventario.", "Acceso Denegado",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (moduloInventario != null)
                    moduloInventario.abrirComoVentanaModal(Sistema.this);
            }

            @Override
            public void onF4() {
                if (!esAdmin()) {
                    JOptionPane.showMessageDialog(Sistema.this,
                            "Acceso restringido: Solo el Administrador puede ingresar a Administración.",
                            "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (moduloConfig != null)
                    moduloConfig.abrirComoVentanaModal();
            }

            @Override
            public void onF5() {
                if (!esAdmin()) {
                    JOptionPane.showMessageDialog(Sistema.this,
                            "Acceso restringido: Solo el Administrador puede ingresar a Administración.",
                            "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (moduloConfig != null)
                    moduloConfig.abrirComoVentanaModal();
            }

            @Override
            public void onF9() {
                if (jTabbedPane1.getSelectedIndex() == 3)
                    btnGenerarPedido.doClick();
            }

            @Override
            public void onF11() {
                togglePantallaCompleta();
            }

            @Override
            public void onF12() {
                bloquearPantalla();
            }

            @Override
            public void onEscape() {
                jTabbedPane1.setSelectedIndex(0);
            }
        });

        // Optimización POS: Doble clic en plato para agregarlo al instante
        tblTemPlatos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblTemPlatos.getSelectedRow() != -1) {
                    btnAddPlatoActionPerformed(null);
                }
            }
        });

        // Optimización POS: ENTER en el buscador agrega el primer plato de la lista
        txtBuscarPlato.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (tblTemPlatos.getRowCount() > 0) {
                    tblTemPlatos.setRowSelectionInterval(0, 0);
                    btnAddPlatoActionPerformed(null);
                    txtBuscarPlato.setText("");
                    LimpiarTable();
                    ListarPlatos(tblTemPlatos);
                }
            }
        });

        // Optimización POS: Doble clic en la comanda para aumentar cantidad (+1)
        tableMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tableMenu.getSelectedRow() != -1) {
                    int row = tableMenu.getSelectedRow();
                    try {
                        int cantActual = Integer.parseInt(tableMenu.getValueAt(row, 2).toString());
                        double precio = Double.parseDouble(tableMenu.getValueAt(row, 3).toString());
                        int nuevaCant = cantActual + 1;
                        tableMenu.setValueAt(nuevaCant, row, 2);
                        tableMenu.setValueAt(precio * nuevaCant, row, 4);
                        TotalPagar(tableMenu, totalMenu);
                    } catch (Exception ex) {
                    }
                }
            }
        });

        // Optimización POS: Teclas +, - y Suprimir en la comanda seleccionada
        tableMenu.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                int row = tableMenu.getSelectedRow();
                if (row == -1)
                    return;
                int key = evt.getKeyCode();
                char ch = evt.getKeyChar();
                try {
                    int cantActual = Integer.parseInt(tableMenu.getValueAt(row, 2).toString());
                    double precio = Double.parseDouble(tableMenu.getValueAt(row, 3).toString());
                    if (key == java.awt.event.KeyEvent.VK_ADD || ch == '+'
                            || key == java.awt.event.KeyEvent.VK_EQUALS) {
                        int nuevaCant = cantActual + 1;
                        tableMenu.setValueAt(nuevaCant, row, 2);
                        tableMenu.setValueAt(precio * nuevaCant, row, 4);
                        TotalPagar(tableMenu, totalMenu);
                    } else if (key == java.awt.event.KeyEvent.VK_SUBTRACT || ch == '-'
                            || key == java.awt.event.KeyEvent.VK_MINUS) {
                        if (cantActual > 1) {
                            int nuevaCant = cantActual - 1;
                            tableMenu.setValueAt(nuevaCant, row, 2);
                            tableMenu.setValueAt(precio * nuevaCant, row, 4);
                        } else {
                            ((javax.swing.table.DefaultTableModel) tableMenu.getModel()).removeRow(row);
                        }
                        TotalPagar(tableMenu, totalMenu);
                    } else if (key == java.awt.event.KeyEvent.VK_DELETE) {
                        ((javax.swing.table.DefaultTableModel) tableMenu.getModel()).removeRow(row);
                        TotalPagar(tableMenu, totalMenu);
                    }
                } catch (Exception ex) {
                }
            }
        });

        initPanelComandaVisual();
    }

    // MÃ©todo auxiliar para evitar que los usuarios escriban letras en campos
    // numÃ©ricos
    private void aplicarFiltroNumerico(javax.swing.JTextField textField, boolean permiteDecimal) {
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (permiteDecimal) {
                    if (((c < '0') || (c > '9')) && (c != java.awt.event.KeyEvent.VK_BACK_SPACE) && (c != '.')) {
                        e.consume(); // Ignorar tecla
                    }
                    if (c == '.' && textField.getText().contains(".")) {
                        e.consume(); // Evitar multiples puntos
                    }
                } else {
                    if (((c < '0') || (c > '9')) && (c != java.awt.event.KeyEvent.VK_BACK_SPACE)) {
                        e.consume();
                    }
                }
            }
        });
    }

    // MÃ©todo auxiliar para feedback visual de Ã©xito en botones
    private void mostrarExitoEnBoton(javax.swing.JButton boton, String textoOriginal, String textoExito) {
        java.awt.Color colorOriginal = boton.getBackground();
        boton.setBackground(new java.awt.Color(39, 174, 96));
        boton.setText(textoExito);
        boton.setEnabled(false);
        // TambiÃ©n mostrar un Toast para refuerzo visual
        ToastNotification.exito(boton, textoExito.replace("âœ… ", ""));
        new javax.swing.Timer(2000, evt -> {
            boton.setBackground(colorOriginal);
            boton.setText(textoOriginal);
            boton.setEnabled(true);
        }).start();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, new java.awt.Color(15, 23, 42), getWidth(),
                        getHeight(), new java.awt.Color(30, 64, 175));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // CÃ­rculo decorativo
                g2.setColor(new java.awt.Color(255, 255, 255, 10));
                g2.fillOval(-50, 500, 200, 200);
                g2.dispose();
            }
        };
        labelLogo = new javax.swing.JLabel();
        btnSala = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnConfig = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnCaja = new javax.swing.JButton();
        LabelVendedor = new javax.swing.JLabel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tipo = new javax.swing.JLabel();
        btnUsuarios = new javax.swing.JButton();
        btnPlatos = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        PanelSalas = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableSala = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtNombreSala = new javax.swing.JTextField();
        btnRegistrarSala = new javax.swing.JButton();
        btnActualizarSala = new javax.swing.JButton();
        btnNuevoSala = new javax.swing.JButton();
        btnEliminarSala = new javax.swing.JButton();
        txtIdSala = new javax.swing.JTextField();
        jPanel35 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        txtMesas = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        PanelMesas = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        txtBuscarPlato = new javax.swing.JTextField();
        jScrollPane10 = new javax.swing.JScrollPane();
        tblTemPlatos = new javax.swing.JTable();
        btnAddPlato = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        tableMenu = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        txtComentario = new javax.swing.JTextPane();
        jLabel11 = new javax.swing.JLabel();
        totalMenu = new javax.swing.JLabel();
        btnGenerarPedido = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnEliminarTempPlato = new javax.swing.JButton();
        txtTempIdSala = new javax.swing.JTextField();
        txtTempNumMesa = new javax.swing.JTextField();
        jPanel25 = new javax.swing.JPanel();
        btnFinalizar = new javax.swing.JButton();
        totalFinalizar = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        tableFinalizar = new javax.swing.JTable();
        txtIdPedido = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtFechaHora = new javax.swing.JTextField();
        txtSalaFinalizar = new javax.swing.JTextField();
        txtNumMesaFinalizar = new javax.swing.JTextField();
        btnPdfPedido = new javax.swing.JButton();
        txtIdHistorialPedido = new javax.swing.JTextField();
        btnEliminarPlatoFinalizar = new javax.swing.JButton();
        btnAddPlatoFinalizar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jComboSalas = new javax.swing.JComboBox<>();
        btnEfectivo = new javax.swing.JButton();
        btnTransaccion = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TablePedidos = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        txtTotalDia = new javax.swing.JTextField();
        txtTotalDiaTrans = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        btnEliminarPedido = new javax.swing.JButton();
        BtnImprimirDia = new javax.swing.JButton();
        txtPedidosDia = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        txtIdConfig = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtTelefonoConfig = new javax.swing.JTextField();
        txtDireccionConfig = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        txtMensaje = new javax.swing.JTextField();
        btnActualizarConfig = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        txtRucConfig = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtNombreConfig = new javax.swing.JTextField();
        jPanel41 = new javax.swing.JPanel();
        jPanel42 = new javax.swing.JPanel();
        jPanel43 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jPanel45 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        TableUsuarios = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        txtPass = new javax.swing.JPasswordField();
        btnIniciar = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        cbxRol = new javax.swing.JComboBox<>();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        btnModificarUsua = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        txtNombrePlato = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtPrecioPlato = new javax.swing.JTextField();
        btnGuardarPlato = new javax.swing.JButton();
        btnEditarPlato = new javax.swing.JButton();
        btnEliminarPlato = new javax.swing.JButton();
        btnNuevoPlato = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        txtIdPlato = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        TablePlatos = new javax.swing.JTable();
        jLabel_wallpaper = new javax.swing.JLabel();
        BtnCerrarSesion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Panel de AdminstraciÃ³n");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pizzeria.png"))); // NOI18N
        labelLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelLogoMouseClicked(evt);
            }
        });

        btnSala.setBackground(new java.awt.Color(60, 63, 65));
        btnSala.setForeground(new java.awt.Color(255, 255, 255));
        btnSala.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/sala.png"))); // NOI18N
        btnSala.setText("      Salas");
        btnSala.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSala.setEnabled(false);
        btnSala.setFocusable(false);
        btnSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalaActionPerformed(evt);
            }
        });

        btnVentas.setBackground(new java.awt.Color(60, 63, 65));
        btnVentas.setForeground(new java.awt.Color(255, 255, 255));
        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pedidos.png"))); // NOI18N
        btnVentas.setText("      Pedidos");
        btnVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnVentas.setFocusable(false);
        btnVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentasActionPerformed(evt);
            }
        });

        btnConfig.setBackground(new java.awt.Color(60, 63, 65));
        btnConfig.setForeground(new java.awt.Color(255, 255, 255));
        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnConfig.setText("F5  Config");
        btnConfig.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnConfig.setFocusable(false);
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });

        LabelVendedor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelVendedor.setText("Administrador");

        tipo.setForeground(new java.awt.Color(255, 255, 255));

        btnUsuarios.setBackground(new java.awt.Color(60, 63, 65));
        btnUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/usuarios.png"))); // NOI18N
        btnUsuarios.setText("F4  Usuarios");
        btnUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnUsuarios.setFocusable(false);
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });

        btnPlatos.setBackground(new java.awt.Color(60, 63, 65));
        btnPlatos.setForeground(new java.awt.Color(255, 255, 255));
        btnPlatos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/platos.png"))); // NOI18N
        btnPlatos.setText("F1  Carta");
        btnPlatos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnPlatos.setFocusable(false);
        btnPlatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlatosActionPerformed(evt);
            }
        });

        btnInventario.setBackground(new java.awt.Color(60, 63, 65));
        btnInventario.setForeground(new java.awt.Color(255, 255, 255));
        btnInventario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnInventario.setText("F3  Inventario");
        btnInventario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInventario.setFocusable(false);

        btnCaja.setBackground(new java.awt.Color(60, 63, 65));
        btnCaja.setForeground(new java.awt.Color(255, 255, 255));
        btnCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnCaja.setText("F2  Caja / Arqueo");
        btnCaja.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCaja.setFocusable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnSala, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVentas, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCaja, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnInventario, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnConfig, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LabelVendedor, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(tipo)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(btnPlatos, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 199,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(tipo)
                                .addGap(10, 10, 10)
                                .addComponent(LabelVendedor)
                                .addGap(14, 14, 14)
                                .addComponent(btnPlatos, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnSala, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnInventario, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(btnUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(20, Short.MAX_VALUE)));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 720));

        jLabel38.setFont(new java.awt.Font("Zilla Slab", 1, 48)); // NOI18N
        jLabel38.setText("COMUNEROS- PUENTE NACIONAL");
        jLabel38.setFocusable(false);
        jLabel38.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        getContentPane().add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 0, 820, 90));

        jScrollPane8.setBackground(new java.awt.Color(60, 63, 65));

        PanelSalas.setForeground(new java.awt.Color(0, 0, 0));
        PanelSalas.setLayout(new java.awt.GridLayout(0, 5));
        jScrollPane8.setViewportView(PanelSalas);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 1030,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)));
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 540,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)));

        jTabbedPane1.addTab("Panel", jPanel9);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBackground(new java.awt.Color(60, 63, 65));

        tableSala.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "ID", "NOMBRE", "Mesas"
                }));
        tableSala.setRowHeight(23);
        tableSala.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSalaMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tableSala);
        if (tableSala.getColumnModel().getColumnCount() > 0) {
            tableSala.getColumnModel().getColumn(0).setMinWidth(80);
            tableSala.getColumnModel().getColumn(0).setPreferredWidth(80);
            tableSala.getColumnModel().getColumn(0).setMaxWidth(130);
            tableSala.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableSala.getColumnModel().getColumn(2).setMinWidth(80);
            tableSala.getColumnModel().getColumn(2).setPreferredWidth(80);
            tableSala.getColumnModel().getColumn(2).setMaxWidth(150);
        }

        jPanel4.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 80, 490, 470));

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel18.setText("Nombre:");
        jPanel10.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        txtNombreSala.setBackground(new java.awt.Color(204, 204, 204));
        txtNombreSala.setForeground(new java.awt.Color(0, 0, 0));
        txtNombreSala.setBorder(null);
        jPanel10.add(txtNombreSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 190, 30));

        btnRegistrarSala.setBackground(new java.awt.Color(46, 204, 113));
        btnRegistrarSala.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrarSala.setText("REGISTRAR");
        btnRegistrarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnRegistrarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 100, 40));

        btnActualizarSala.setText("ACTUALIZAR");
        btnActualizarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnActualizarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 230, 100, 40));

        btnNuevoSala.setText("NUEVA SALA");
        btnNuevoSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnNuevoSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 300, 100, 40));

        btnEliminarSala.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarSala.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarSala.setText("ELIMINAR");
        btnEliminarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnEliminarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 100, 40));
        jPanel10.add(txtIdSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 24, -1));

        jPanel35.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
                jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 190, Short.MAX_VALUE));
        jPanel35Layout.setVerticalGroup(
                jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE));

        jPanel10.add(jPanel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 190, 2));

        jPanel38.setBackground(new java.awt.Color(0, 0, 0));
        jPanel38.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel33.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Nuevo Sala");
        jPanel38.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 30));

        jPanel10.add(jPanel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 35));

        jPanel36.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
                jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 190, Short.MAX_VALUE));
        jPanel36Layout.setVerticalGroup(
                jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE));

        jPanel10.add(jPanel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 190, 2));

        txtMesas.setBackground(new java.awt.Color(204, 204, 204));
        txtMesas.setForeground(new java.awt.Color(0, 0, 0));
        txtMesas.setBorder(null);
        jPanel10.add(txtMesas, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 190, 30));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel19.setText("Mesas:");
        jPanel10.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        jPanel4.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 140, 310, 370));

        jTabbedPane1.addTab("Salas", jPanel4);

        PanelMesas.setLayout(new java.awt.GridLayout(0, 5));
        jScrollPane9.setViewportView(PanelMesas);

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
                jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel22Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 1068, Short.MAX_VALUE)
                                .addContainerGap()));
        jPanel22Layout.setVerticalGroup(
                jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel22Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                                .addContainerGap()));

        jTabbedPane1.addTab("Mesas", jPanel22);

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder("Platos del Dia"));

        txtBuscarPlato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarPlatoKeyReleased(evt);
            }
        });

        tblTemPlatos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblTemPlatos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "", "Nombre", "Precio"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tblTemPlatos.setRowHeight(23);
        jScrollPane10.setViewportView(tblTemPlatos);
        if (tblTemPlatos.getColumnModel().getColumnCount() > 0) {
            tblTemPlatos.getColumnModel().getColumn(0).setMinWidth(30);
            tblTemPlatos.getColumnModel().getColumn(0).setPreferredWidth(30);
            tblTemPlatos.getColumnModel().getColumn(0).setMaxWidth(50);
            tblTemPlatos.getColumnModel().getColumn(2).setMinWidth(150);
            tblTemPlatos.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblTemPlatos.getColumnModel().getColumn(2).setMaxWidth(200);
        }

        btnAddPlato.setBackground(new java.awt.Color(0, 0, 0));
        btnAddPlato.setFont(new java.awt.Font("Arial Black", 1, 24)); // NOI18N
        btnAddPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPlato.setText("+");
        btnAddPlato.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddPlato.setFocusable(false);
        btnAddPlato.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPlatoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel24Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 0,
                                                Short.MAX_VALUE)
                                        .addGroup(jPanel24Layout.createSequentialGroup()
                                                .addComponent(txtBuscarPlato, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36,
                                                        Short.MAX_VALUE)
                                                .addComponent(btnAddPlato)))
                                .addContainerGap()));
        jPanel24Layout.setVerticalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel24Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel24Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtBuscarPlato, javax.swing.GroupLayout.DEFAULT_SIZE, 32,
                                                Short.MAX_VALUE)
                                        .addComponent(btnAddPlato, javax.swing.GroupLayout.PREFERRED_SIZE, 0,
                                                Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                                .addContainerGap()));

        tableMenu.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "", "Plato", "Cant", "Precio", "SubTotal", "Comentario"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableMenu.setRowHeight(23);
        jScrollPane11.setViewportView(tableMenu);
        if (tableMenu.getColumnModel().getColumnCount() > 0) {
            tableMenu.getColumnModel().getColumn(0).setMinWidth(30);
            tableMenu.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableMenu.getColumnModel().getColumn(0).setMaxWidth(50);
            tableMenu.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableMenu.getColumnModel().getColumn(2).setMinWidth(40);
            tableMenu.getColumnModel().getColumn(2).setPreferredWidth(40);
            tableMenu.getColumnModel().getColumn(2).setMaxWidth(50);
            tableMenu.getColumnModel().getColumn(3).setPreferredWidth(50);
            tableMenu.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jLabel6.setText("Comentario:");

        jScrollPane12.setViewportView(txtComentario);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel11.setText("Total a Pagar");

        totalMenu.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalMenu.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalMenu.setText("00.00");

        btnGenerarPedido.setText("Realizar Pedido");
        btnGenerarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarPedidoActionPerformed(evt);
            }
        });

        jButton2.setText("Agregar");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btnEliminarTempPlato.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarTempPlato.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarTempPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarTempPlatoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
                jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        380, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(jPanel23Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(btnEliminarTempPlato,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                80, Short.MAX_VALUE)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout
                                                .createSequentialGroup()
                                                .addGap(0, 23, Short.MAX_VALUE)
                                                .addGroup(jPanel23Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jScrollPane11,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 570,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addGroup(jPanel23Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                        .addComponent(txtTempIdSala)
                                                                        .addComponent(txtTempNumMesa,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                117, Short.MAX_VALUE))
                                                                .addGap(79, 342, Short.MAX_VALUE)
                                                                .addGroup(jPanel23Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(btnGenerarPedido)
                                                                        .addGroup(jPanel23Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel11)
                                                                                .addGroup(jPanel23Layout
                                                                                        .createSequentialGroup()
                                                                                        .addGap(10, 10, 10)
                                                                                        .addComponent(totalMenu,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                120,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        jPanel23Layout.setVerticalGroup(
                jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                                .addContainerGap(43, Short.MAX_VALUE)
                                .addGroup(jPanel23Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                .addGroup(jPanel23Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel6)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(jButton2,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 30,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(btnEliminarTempPlato,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 35,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jScrollPane12,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 83,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 0,
                                                        Short.MAX_VALUE)
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel23Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(txtTempIdSala,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(14, 14, 14)
                                                                .addComponent(txtTempNumMesa,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 41,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(jLabel11)
                                                                .addGap(14, 14, 14)
                                                                .addComponent(totalMenu)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(btnGenerarPedido,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(39, 39, 39))
                                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(7, Short.MAX_VALUE)));

        jTabbedPane1.addTab("Platos", jPanel23);

        jPanel25.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnFinalizar.setText("Finalizar");
        btnFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 440, 110, 40));

        totalFinalizar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalFinalizar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalFinalizar.setText("00.00");
        jPanel25.add(totalFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 390, 120, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel17.setText("Total a Pagar");
        jPanel25.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 340, -1, -1));

        tableFinalizar.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "", "Plato", "Cant", "Precio", "SubTotal", "Comentario"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableFinalizar.setRowHeight(23);
        jScrollPane13.setViewportView(tableFinalizar);
        if (tableFinalizar.getColumnModel().getColumnCount() > 0) {
            tableFinalizar.getColumnModel().getColumn(0).setMinWidth(30);
            tableFinalizar.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableFinalizar.getColumnModel().getColumn(0).setMaxWidth(50);
            tableFinalizar.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableFinalizar.getColumnModel().getColumn(2).setMinWidth(40);
            tableFinalizar.getColumnModel().getColumn(2).setPreferredWidth(40);
            tableFinalizar.getColumnModel().getColumn(2).setMaxWidth(50);
            tableFinalizar.getColumnModel().getColumn(3).setPreferredWidth(50);
            tableFinalizar.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jPanel25.add(jScrollPane13, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 13, 1030, 316));
        jPanel25.add(txtIdPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 390, 50, -1));

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel7.setText("Fecha y Hora:");
        jPanel25.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 350, -1, -1));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel8.setText("Sala:");
        jPanel25.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, -1, -1));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel9.setText("NÂ° Mesa:");
        jPanel25.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 450, -1, -1));

        txtFechaHora.setEditable(false);
        jPanel25.add(txtFechaHora, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 350, 240, 30));

        txtSalaFinalizar.setEditable(false);
        jPanel25.add(txtSalaFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 400, 150, 30));

        txtNumMesaFinalizar.setEditable(false);
        jPanel25.add(txtNumMesaFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 450, 240, 30));

        btnPdfPedido.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        btnPdfPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfPedidoActionPerformed(evt);
            }
        });
        jPanel25.add(btnPdfPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 440, 110, 40));

        txtIdHistorialPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdHistorialPedidoActionPerformed(evt);
            }
        });
        jPanel25.add(txtIdHistorialPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 450, 50, -1));

        btnEliminarPlatoFinalizar.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarPlatoFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPlatoFinalizar.setText("Eliminar");
        btnEliminarPlatoFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPlatoFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnEliminarPlatoFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 510, 110, 40));

        btnAddPlatoFinalizar.setBackground(new java.awt.Color(46, 204, 113));
        btnAddPlatoFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPlatoFinalizar.setText("Agregar");
        btnAddPlatoFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPlatoFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnAddPlatoFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 510, 110, 40));

        btnImprimir.setText("Imprimir ");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });
        jPanel25.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 440, 90, 40));

        jComboSalas.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Seleccionar", "Efectivo", "Transaccion", "Transaccion-efectivo", " " }));
        jComboSalas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSalasActionPerformed(evt);
            }
        });
        jPanel25.add(jComboSalas, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 400, 90, 30));

        btnEfectivo.setText("Efectivo");
        btnEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEfectivoActionPerformed(evt);
            }
        });
        jPanel25.add(btnEfectivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 510, 110, 40));

        btnTransaccion.setText("Transaccion");
        btnTransaccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransaccionActionPerformed(evt);
            }
        });
        jPanel25.add(btnTransaccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 110, 40));

        jTabbedPane1.addTab("Finalizar Pedido", jPanel25);

        jPanel6.setBackground(new java.awt.Color(70, 73, 75));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TablePedidos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        TablePedidos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "Id", "Sala", "Atendido", "N° Mesa", "Fecha", "Total", "Estado", "PagoEfectivo",
                        "PagoTransaccion"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false, false, false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        UIUtils.estilarTablaOscura(TablePedidos);
        TablePedidos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        TablePedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TablePedidosMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(TablePedidos);
        if (TablePedidos.getColumnModel().getColumnCount() > 0) {
            TablePedidos.getColumnModel().getColumn(0).setMinWidth(80);
            TablePedidos.getColumnModel().getColumn(0).setPreferredWidth(80);
            TablePedidos.getColumnModel().getColumn(0).setMaxWidth(120);
            TablePedidos.getColumnModel().getColumn(2).setPreferredWidth(60);
            TablePedidos.getColumnModel().getColumn(3).setMinWidth(100);
            TablePedidos.getColumnModel().getColumn(3).setPreferredWidth(100);
            TablePedidos.getColumnModel().getColumn(3).setMaxWidth(150);
            TablePedidos.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jPanel6.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 86, 1020, 405));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("");
        jLabel16.setVisible(false); // Ocultar para evitar solapamiento con los botones superiores

        txtTotalDiaTrans.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        txtTotalDiaTrans.setBackground(new java.awt.Color(30, 41, 59));
        txtTotalDiaTrans.setForeground(new java.awt.Color(56, 189, 248));
        txtTotalDiaTrans.setEditable(false);
        txtTotalDiaTrans.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(txtTotalDiaTrans, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 525, 180, 40));

        txtTotalDia.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        txtTotalDia.setBackground(new java.awt.Color(30, 41, 59));
        txtTotalDia.setForeground(new java.awt.Color(52, 211, 153));
        txtTotalDia.setEditable(false);
        txtTotalDia.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(txtTotalDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 525, 180, 40));

        txtPedidosDia.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        txtPedidosDia.setBackground(new java.awt.Color(30, 41, 59));
        txtPedidosDia.setForeground(new java.awt.Color(251, 191, 36));
        txtPedidosDia.setEditable(false);
        txtPedidosDia.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(txtPedidosDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 525, 180, 40));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(148, 163, 184));
        jLabel21.setText("TRANSACCIONES:");
        jPanel6.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 502, 180, 20));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(148, 163, 184));
        jLabel22.setText("EFECTIVO:");
        jPanel6.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 502, 180, 20));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(148, 163, 184));
        jLabel20.setText("N° PEDIDOS:");
        jPanel6.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 502, 180, 20));

        btnEliminarPedido.setBackground(new java.awt.Color(220, 38, 38));
        btnEliminarPedido.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnEliminarPedido.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPedido.setText("Eliminar Pedido");
        btnEliminarPedido.setFocusPainted(false);
        btnEliminarPedido.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPedidoActionPerformed(evt);
            }
        });
        jPanel6.add(btnEliminarPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 525, 135, 40));

        BtnImprimirDia.setBackground(new java.awt.Color(5, 150, 105));
        BtnImprimirDia.setFont(new java.awt.Font("Segoe UI", 1, 13));
        BtnImprimirDia.setForeground(new java.awt.Color(255, 255, 255));
        BtnImprimirDia.setText("Reporte del Día");
        BtnImprimirDia.setFocusPainted(false);
        BtnImprimirDia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnImprimirDia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnImprimirDiaActionPerformed(evt);
            }
        });
        jPanel6.add(BtnImprimirDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(915, 525, 135, 40));

        jTabbedPane1.addTab("Historial Pedidos", jPanel6);

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel32.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel32.setText("DATOS DE LA EMPRESA");
        jPanel7.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, -1, -1));

        jPanel8.setBackground(new java.awt.Color(70, 73, 75));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtIdConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdConfigActionPerformed(evt);
            }
        });
        jPanel8.add(txtIdConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 410, 24, -1));

        jLabel30.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel30.setText("DirecciÃ³n");
        jPanel8.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel29.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel29.setText("TelÃ©fono");
        jPanel8.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 160, -1, -1));

        txtTelefonoConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtTelefonoConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtTelefonoConfig.setBorder(null);
        jPanel8.add(txtTelefonoConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 190, 218, 30));

        txtDireccionConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtDireccionConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtDireccionConfig.setBorder(null);
        jPanel8.add(txtDireccionConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 147, 30));

        jLabel31.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel31.setText("Mensaje");
        jPanel8.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, -1, -1));

        txtMensaje.setBackground(new java.awt.Color(204, 204, 204));
        txtMensaje.setForeground(new java.awt.Color(0, 0, 0));
        txtMensaje.setBorder(null);
        jPanel8.add(txtMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 400, 30));

        btnActualizarConfig.setBackground(new java.awt.Color(255, 255, 255));
        btnActualizarConfig.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnActualizarConfig.setForeground(new java.awt.Color(0, 0, 0));
        btnActualizarConfig.setText("Modificar");
        btnActualizarConfig.setBorder(null);
        btnActualizarConfig.setFocusable(false);
        btnActualizarConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnActualizarConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnActualizarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarConfigActionPerformed(evt);
            }
        });
        jPanel8.add(btnActualizarConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 405, 220, 50));

        jLabel27.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel27.setText("Ruc");
        jPanel8.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        txtRucConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtRucConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtRucConfig.setBorder(null);
        jPanel8.add(txtRucConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 147, 30));

        jLabel28.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel28.setText("Nombre");
        jPanel8.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 30, -1, -1));

        txtNombreConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtNombreConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtNombreConfig.setBorder(null);
        jPanel8.add(txtNombreConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 60, 220, 30));

        jPanel41.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
                jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel41Layout.setVerticalGroup(
                jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel8.add(jPanel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 147, 2));

        jPanel42.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
                jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel42Layout.setVerticalGroup(
                jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel8.add(jPanel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 147, 2));

        jPanel43.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
                jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel43Layout.setVerticalGroup(
                jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel8.add(jPanel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, 400, 2));

        jPanel44.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
                jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel44Layout.setVerticalGroup(
                jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel8.add(jPanel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 90, 220, 2));

        jPanel45.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
                jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel45Layout.setVerticalGroup(
                jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel8.add(jPanel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 220, 220, 2));

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
                jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE));
        jPanel40Layout.setVerticalGroup(
                jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 10, Short.MAX_VALUE));

        jPanel8.add(jPanel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jPanel7.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 420, 470));

        jTabbedPane1.addTab("Datos de la Empresa", jPanel7);

        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableUsuarios.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "Id", "Nombre", "Correo", "Rol"
                }));
        TableUsuarios.setRowHeight(23);
        jScrollPane6.setViewportView(TableUsuarios);
        if (TableUsuarios.getColumnModel().getColumnCount() > 0) {
            TableUsuarios.getColumnModel().getColumn(0).setMinWidth(50);
            TableUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);
            TableUsuarios.getColumnModel().getColumn(0).setMaxWidth(80);
            TableUsuarios.getColumnModel().getColumn(3).setMinWidth(150);
            TableUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150);
            TableUsuarios.getColumnModel().getColumn(3).setMaxWidth(200);
        }

        jPanel12.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 660, 520));

        jPanel15.setBackground(new java.awt.Color(70, 73, 75));
        jPanel15.setForeground(new java.awt.Color(70, 73, 75));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel34.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel34.setText("Correo ElectrÃ³nico");
        jPanel15.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 102, -1, -1));

        jLabel35.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel35.setText("Password");
        jPanel15.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 130, -1));

        txtCorreo.setBackground(new java.awt.Color(204, 204, 204));
        txtCorreo.setForeground(new java.awt.Color(0, 0, 0));
        txtCorreo.setBorder(null);
        txtCorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCorreoActionPerformed(evt);
            }
        });
        jPanel15.add(txtCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 126, 300, 30));

        txtPass.setBackground(new java.awt.Color(204, 204, 204));
        txtPass.setForeground(new java.awt.Color(0, 0, 0));
        txtPass.setBorder(null);
        jPanel15.add(txtPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 300, 30));

        btnIniciar.setBackground(new java.awt.Color(0, 0, 0));
        btnIniciar.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnIniciar.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciar.setText("Registrar");
        btnIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });
        jPanel15.add(btnIniciar, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 450, 130, 40));

        jLabel36.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel36.setText("Nombre:");
        jPanel15.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, -1, -1));

        txtNombre.setBackground(new java.awt.Color(204, 204, 204));
        txtNombre.setForeground(new java.awt.Color(0, 0, 0));
        txtNombre.setBorder(null);
        jPanel15.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 300, 300, 30));

        jLabel37.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel37.setText("Rol:");
        jPanel15.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 360, 90, -1));

        cbxRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Asistente" }));
        jPanel15.add(cbxRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, 300, 30));

        jPanel16.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));
        jPanel16Layout.setVerticalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE));

        jPanel15.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 156, 300, 2));

        jPanel17.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));
        jPanel17Layout.setVerticalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE));

        jPanel15.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 300, 2));

        jPanel18.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
                jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));
        jPanel18Layout.setVerticalGroup(
                jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE));

        jPanel15.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 300, 2));

        jPanel21.setBackground(new java.awt.Color(0, 0, 0));
        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel39.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("Nuevo Usuario");
        jPanel21.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 35));

        jPanel15.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 35));

        btnModificarUsua.setBackground(new java.awt.Color(0, 0, 0));
        btnModificarUsua.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnModificarUsua.setForeground(new java.awt.Color(255, 255, 255));
        btnModificarUsua.setText(" Modificar");
        btnModificarUsua.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnModificarUsua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarUsuaActionPerformed(evt);
            }
        });
        jPanel15.add(btnModificarUsua, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 450, 140, 40));

        jPanel12.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 360, 520));

        jTabbedPane1.addTab("Usuarios", jPanel12);

        jPanel11.setBackground(new java.awt.Color(70, 73, 75));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel23.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel23.setText("Nombre:");
        jPanel11.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        txtNombrePlato.setBackground(new java.awt.Color(204, 204, 204));
        txtNombrePlato.setBorder(null);
        jPanel11.add(txtNombrePlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 170, 30));

        jLabel25.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel25.setText("Precio:");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        txtPrecioPlato.setBackground(new java.awt.Color(204, 204, 204));
        txtPrecioPlato.setBorder(null);
        txtPrecioPlato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPrecioPlatoKeyTyped(evt);
            }
        });
        jPanel11.add(txtPrecioPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 170, 30));

        btnGuardarPlato.setBackground(new java.awt.Color(46, 204, 113));
        btnGuardarPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarPlato.setText("GUARDAR");
        btnGuardarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnGuardarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 110, 50));

        btnEditarPlato.setText("EDITAR");
        btnEditarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnEditarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 270, 100, 50));

        btnEliminarPlato.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPlato.setText("ELIMINAR");
        btnEliminarPlato.setEnabled(false);
        btnEliminarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnEliminarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 110, 50));

        btnNuevoPlato.setText("NUEVO");
        btnNuevoPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnNuevoPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 370, 100, 50));

        jPanel31.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
                jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel31Layout.setVerticalGroup(
                jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel11.add(jPanel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 130, 170, 2));

        jPanel33.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
                jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel33Layout.setVerticalGroup(
                jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        jPanel11.add(jPanel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 200, 170, 2));

        jPanel39.setBackground(new java.awt.Color(0, 0, 0));

        jLabel40.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setText("Platos del DÃ­a");

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
                jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE));
        jPanel39Layout.setVerticalGroup(
                jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel39Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                                .addContainerGap()));

        jPanel11.add(jPanel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 290, 50));
        jPanel11.add(txtIdPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 470, 80, -1));

        TablePlatos.setForeground(new java.awt.Color(255, 255, 255));
        TablePlatos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "ID", "DESCRIPCIÃ“N", "PRECIO"
                }));
        TablePlatos.setRowHeight(23);
        TablePlatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TablePlatosMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(TablePlatos);
        if (TablePlatos.getColumnModel().getColumnCount() > 0) {
            TablePlatos.getColumnModel().getColumn(0).setMinWidth(100);
            TablePlatos.getColumnModel().getColumn(0).setPreferredWidth(100);
            TablePlatos.getColumnModel().getColumn(0).setMaxWidth(150);
            TablePlatos.getColumnModel().getColumn(2).setMinWidth(200);
            TablePlatos.getColumnModel().getColumn(2).setPreferredWidth(200);
            TablePlatos.getColumnModel().getColumn(2).setMaxWidth(300);
        }

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 289,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                                .addContainerGap()));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(jPanel2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jScrollPane4)
                                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 543,
                                                Short.MAX_VALUE))
                                .addContainerGap(22, Short.MAX_VALUE)));

        jTabbedPane1.addTab("Platos", jPanel2);
        jTabbedPane1.addTab("", jLabel_wallpaper);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 95, 1080, 620));

        BtnCerrarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/cerrar-sesion.png"))); // NOI18N
        BtnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCerrarSesionActionPerformed(evt);
            }
        });
        getContentPane().add(BtnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(1180, 10, 90, 90));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSalaActionPerformed
        if (!validarCajaAbierta())
            return;
        LimpiarTableMenu();
        LimpiarPlatos();

        // Abrir diálogo modal con las salas disponibles
        List<Sala> salas = slDao.Listar();
        if (salas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay salas registradas. Cree una sala primero.");
            return;
        }

        // Si solo hay una sala, ir directamente a sus mesas
        if (salas.size() == 1) {
            LimpiarTable();
            PanelMesas.removeAll();
            panelMesas(salas.get(0).getId(), salas.get(0).getMesas());
            jTabbedPane1.setSelectedIndex(2);
            return;
        }

        // Crear diálogo de selección de sala
        JDialog dialogSalas = new JDialog(this, "Seleccionar Sala", true);
        dialogSalas.setSize(600, 400);
        dialogSalas.setLocationRelativeTo(this);
        dialogSalas.setLayout(new java.awt.BorderLayout(10, 10));
        dialogSalas.getContentPane().setBackground(new java.awt.Color(30, 41, 59));

        javax.swing.JLabel titulo = new javax.swing.JLabel("Seleccione una Sala", javax.swing.SwingConstants.CENTER);
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titulo.setForeground(java.awt.Color.WHITE);
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 0, 5, 0));
        dialogSalas.add(titulo, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel panelBotones = new javax.swing.JPanel(new java.awt.GridLayout(0, 3, 15, 15));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 20, 20));

        for (Sala s : salas) {
            JButton boton = new JButton(s.getNombre());
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
            boton.setBackground(new java.awt.Color(6, 78, 59));
            boton.setForeground(new java.awt.Color(52, 211, 153));
            boton.setFocusPainted(false);
            boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            boton.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(5, 150, 105), 2, true),
                    javax.swing.BorderFactory.createEmptyBorder(20, 15, 20, 15)));
            final int idSala = s.getId();
            final int cantMesas = s.getMesas();
            boton.addActionListener(e2 -> {
                dialogSalas.dispose();
                LimpiarTable();
                PanelMesas.removeAll();
                panelMesas(idSala, cantMesas);
                jTabbedPane1.setSelectedIndex(2);
            });
            panelBotones.add(boton);
        }

        dialogSalas.add(panelBotones, java.awt.BorderLayout.CENTER);
        dialogSalas.setVisible(true);
    }// GEN-LAST:event_btnSalaActionPerformed

    private boolean validarCajaAbierta() {
        Modelo.CajaDao cajaDao = new Modelo.CajaDao();
        if (!cajaDao.hayCajaAbierta()) {
            ToastNotification.advertencia(this, "Debe abrir un turno de caja en el módulo de Caja antes de operar.");
            return false;
        }
        return true;
    }

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConfigActionPerformed
        if (moduloConfig != null) {
            moduloConfig.abrirComoVentanaModal();
        }
    }// GEN-LAST:event_btnConfigActionPerformed

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnVentasActionPerformed
        if (!validarCajaAbierta())
            return;
        LimpiarTable();
        ListarPedidos();
        actualizarTotalDia();
        LimpiarTableMenu();
        LimpiarPlatos();

        // Mostrar Dashboard por defecto al entrar a Ventas
        setDashboardVisible(true);
        actualizarEstiloToggle(true);
        actualizarDashboardData();

        jTabbedPane1.setSelectedIndex(5);
    }// GEN-LAST:event_btnVentasActionPerformed

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnUsuariosActionPerformed
        if (moduloUsuarios != null) {
            moduloUsuarios.abrirComoVentanaModal();
        }
    }// GEN-LAST:event_btnUsuariosActionPerformed

    private void labelLogoMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_labelLogoMouseClicked
        jTabbedPane1.setSelectedIndex(0);
        PanelSalas.removeAll();
        LimpiarTableMenu();
        LimpiarPlatos();

        panelSalas();
    }// GEN-LAST:event_labelLogoMouseClicked

    private void btnPlatosActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPlatosActionPerformed
        jTabbedPane1.setSelectedIndex(8);
        LimpiarTable();
        LimpiarTableMenu();
        LimpiarPlatos();

        ListarPlatos(TablePlatos);
    }// GEN-LAST:event_btnPlatosActionPerformed

    private void TablePlatosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_TablePlatosMouseClicked
        int fila = TablePlatos.rowAtPoint(evt.getPoint());
        if (fila >= 0) {
            txtIdPlato.setText(TablePlatos.getValueAt(fila, 0).toString());
            txtNombrePlato.setText(TablePlatos.getValueAt(fila, 1).toString());
            txtPrecioPlato.setText(TablePlatos.getValueAt(fila, 2).toString());
            // Indicar visualmente que estÃ¡ en modo ediciÃ³n
            btnGuardarPlato.setText("âœï¸ Modificar");
            btnGuardarPlato.setBackground(new java.awt.Color(41, 128, 185)); // Azul = editar
        }
    }// GEN-LAST:event_TablePlatosMouseClicked

    private void btnNuevoPlatoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnNuevoPlatoActionPerformed
        LimpiarPlatos();
    }// GEN-LAST:event_btnNuevoPlatoActionPerformed

    private void btnEliminarPlatoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEliminarPlatoActionPerformed
        System.out.println("BotÃ³n Eliminar Plato presionado"); // DepuraciÃ³n

        // Forzar la actualizaciÃ³n de la selecciÃ³n
        int fila = tableMenu.getSelectedRow();
        if (fila != -1) {
            tableMenu.setRowSelectionInterval(fila, fila); // Forzar la selecciÃ³n de la fila actual
        }
        System.out.println("Fila seleccionada: " + fila); // DepuraciÃ³n

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un plato de la tabla",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el ID del plato seleccionado
        int id;
        try {
            id = Integer.parseInt(tableMenu.getValueAt(fila, 0).toString());
            System.out.println("ID del plato seleccionado: " + id); // DepuraciÃ³n
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID de plato invÃ¡lido: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el nombre del plato para el mensaje de confirmaciÃ³n
        String nombrePlato = tableMenu.getValueAt(fila, 1).toString();
        System.out.println("Nombre del plato: " + nombrePlato); // DepuraciÃ³n

        // Confirmar la eliminaciÃ³n
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Â¿Desea eliminar el plato '" + nombrePlato + "'?",
                "Eliminar Plato",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Eliminar el plato usando PlatosDao
            PlatosDao platosDao = new PlatosDao();
            boolean eliminado = platosDao.Eliminar(id);
            System.out.println("Resultado de eliminaciÃ³n: " + eliminado); // DepuraciÃ³n

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Plato eliminado correctamente",
                        "Ã‰xito",
                        JOptionPane.INFORMATION_MESSAGE);
                // Actualizar la tabla
                cargarTablaPlatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar el plato",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_btnEliminarPlatoActionPerformed

    private void btnEditarPlatoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEditarPlatoActionPerformed
        if ("".equals(txtIdPlato.getText())) {
            JOptionPane.showMessageDialog(null, "Primero haga clic en un plato de la tabla", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            // El botÃ³n Guardar ya maneja ediciÃ³n automÃ¡ticamente cuando hay un ID
            // SÃ³lo enfocar el campo nombre para facilitar la ediciÃ³n
            txtNombrePlato.requestFocus();
            txtNombrePlato.selectAll();
        }
    }// GEN-LAST:event_btnEditarPlatoActionPerformed

    private void btnGuardarPlatoActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtNombrePlato.getText().isEmpty() || txtPrecioPlato.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Los campos estÃ¡n vacÃ­os");
            return;
        }
        try {
            pla.setNombre(txtNombrePlato.getText());
            pla.setPrecio(Double.parseDouble(txtPrecioPlato.getText()));
            pla.setFecha(fechaFormato);

            if (!"".equals(txtIdPlato.getText())) {
                // Modo EdiciÃ³n: El usuario hizo clic en la tabla y modificÃ³ algo
                pla.setId(Integer.parseInt(txtIdPlato.getText()));
                if (plaDao.Modificar(pla)) {
                    mostrarExitoEnBoton(btnGuardarPlato, "GUARDAR", "âœ… Modificado");
                    LimpiarTable();
                    ListarPlatos(TablePlatos);
                    LimpiarPlatos();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al modificar el plato");
                }
            } else {
                // Modo Registro: Plato nuevo
                if (plaDao.Registrar(pla)) {
                    mostrarExitoEnBoton(btnGuardarPlato, "GUARDAR", "âœ… Guardado");
                    LimpiarTable();
                    ListarPlatos(TablePlatos);
                    LimpiarPlatos();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar el plato");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El precio debe ser un nÃºmero vÃ¡lido");
        }
    }

    private void txtPrecioPlatoKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txtPrecioPlatoKeyTyped
        event.numberDecimalKeyPress(evt, txtPrecioPlato);
    }// GEN-LAST:event_txtPrecioPlatoKeyTyped

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnIniciarActionPerformed
        if (txtNombre.getText().equals("") || txtCorreo.getText().equals("") || txtPass.getPassword().equals("")) {
            JOptionPane.showMessageDialog(null, "Todo los campos son requeridos");
        } else {
            Login lg = new Login();
            String correo = txtCorreo.getText();
            String pass = String.valueOf(txtPass.getPassword());
            String nom = txtNombre.getText();
            String rol = cbxRol.getSelectedItem().toString();
            lg.setNombre(nom);
            lg.setCorreo(correo);
            lg.setPass(pass);
            lg.setRol(rol);
            lgDao.Registrar(lg);
            JOptionPane.showMessageDialog(null, "Usuario Registrado");
        }
    }// GEN-LAST:event_btnIniciarActionPerformed

    private void txtCorreoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtCorreoActionPerformed
    }// GEN-LAST:event_txtCorreoActionPerformed

    private void btnActualizarConfigActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnActualizarConfigActionPerformed
        if (!"".equals(txtRucConfig.getText()) || !"".equals(txtNombreConfig.getText())
                || !"".equals(txtTelefonoConfig.getText()) || !"".equals(txtDireccionConfig.getText())) {
            conf.setRuc(txtRucConfig.getText());
            conf.setNombre(txtNombreConfig.getText());
            conf.setTelefono(txtTelefonoConfig.getText());
            conf.setDireccion(txtDireccionConfig.getText());
            conf.setMensaje(txtMensaje.getText());
            conf.setId(Integer.parseInt(txtIdConfig.getText()));
            lgDao.ModificarDatos(conf);
            JOptionPane.showMessageDialog(null, "Datos de la empresa modificado");
            // ListarConfig();
        } else {
            JOptionPane.showMessageDialog(null, "Los campos estan vacios");
        }
    }// GEN-LAST:event_btnActualizarConfigActionPerformed

    private void txtIdConfigActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtIdConfigActionPerformed
    }// GEN-LAST:event_txtIdConfigActionPerformed

    private void TablePedidosMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_TablePedidosMouseClicked
        int fila = TablePedidos.rowAtPoint(evt.getPoint());
        if (fila < 0)
            return;
        TablePedidos.setRowSelectionInterval(fila, fila);
        int id_pedido = Integer.parseInt(TablePedidos.getValueAt(fila, 0).toString());

        if (javax.swing.SwingUtilities.isRightMouseButton(evt)) {
            javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();

            javax.swing.JMenuItem itemVer = new javax.swing.JMenuItem("Ver Detalle / Finalizar");
            itemVer.addActionListener(e -> {
                LimpiarTable();
                verPedido(id_pedido);
                verPedidoDetalle(id_pedido);
                jTabbedPane1.setSelectedIndex(4);
                txtIdHistorialPedido.setText("" + id_pedido);
            });

            javax.swing.JMenuItem itemReimprimir = new javax.swing.JMenuItem("Reimprimir Ticket PDF");
            itemReimprimir.addActionListener(e -> {
                pedDao.pdfPedido(id_pedido);
                ToastNotification.exito(this, "Ticket PDF re-generado para pedido #" + id_pedido);
            });

            javax.swing.JMenuItem itemAnular = new javax.swing.JMenuItem("Anular Pedido");
            itemAnular.addActionListener(e -> {
                int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                        "¿Desea anular el pedido #" + id_pedido + "?", "Confirmar Anulación",
                        javax.swing.JOptionPane.YES_NO_OPTION);
                if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                    if (pedDao.actualizarEstado(id_pedido, "ANULADO")) {
                        ToastNotification.exito(this, "Pedido #" + id_pedido + " anulado.");
                        ListarPedidos();
                    }
                }
            });

            javax.swing.JMenuItem itemPreparado = new javax.swing.JMenuItem("🟡 Marcar como PREPARADO (Cocina)");
            itemPreparado.addActionListener(e -> {
                if (pedDao.marcarPreparado(id_pedido)) {
                    ToastNotification.exito(this, "Pedido #" + id_pedido + " marcado como PREPARADO.");
                    ListarPedidos();
                }
            });

            javax.swing.JMenuItem itemFinalizarPed = new javax.swing.JMenuItem("✅ Marcar como FINALIZADO / COBRADO");
            itemFinalizarPed.addActionListener(e -> {
                if (pedDao.actualizarEstado(id_pedido, "FINALIZADO")) {
                    ToastNotification.exito(this, "Pedido #" + id_pedido + " finalizado.");
                    ListarPedidos();
                }
            });

            popup.add(itemVer);
            popup.add(itemPreparado);
            popup.add(itemFinalizarPed);
            popup.add(itemReimprimir);
            popup.addSeparator();
            popup.add(itemAnular);
            popup.show(evt.getComponent(), evt.getX(), evt.getY());
            return;
        }

        LimpiarTable();
        verPedido(id_pedido);
        verPedidoDetalle(id_pedido);
        jTabbedPane1.setSelectedIndex(4);
        txtIdHistorialPedido.setText("" + id_pedido);
    }// GEN-LAST:event_TablePedidosMouseClicked

    private void btnAddPlatoFinalizarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddPlatoFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pedido seleccionado");
            return;
        }

        final int idPedido = Integer.parseInt(txtIdPedido.getText());

        // Custom Dialog for Plate Search
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Buscar y Agregar Plato", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        dialog.setLayout(new java.awt.BorderLayout(10, 10));

        // Colors & Fonts
        final java.awt.Color slate100 = new java.awt.Color(241, 245, 249);
        final java.awt.Color slate300 = new java.awt.Color(203, 213, 225);
        final java.awt.Color slate700 = new java.awt.Color(51, 65, 85);
        final java.awt.Color slate800 = new java.awt.Color(30, 41, 59);
        java.awt.Font fontGeneral = new java.awt.Font("Outfit", java.awt.Font.PLAIN, 14);
        java.awt.Font fontBold = new java.awt.Font("Outfit", java.awt.Font.BOLD, 14);

        // Top search panel
        javax.swing.JPanel pnlTop = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        pnlTop.setBackground(new java.awt.Color(15, 23, 42));
        pnlTop.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        javax.swing.JLabel lblBuscar = new javax.swing.JLabel("Buscar Plato: ");
        lblBuscar.setFont(fontBold);
        lblBuscar.setForeground(slate300);
        pnlTop.add(lblBuscar, java.awt.BorderLayout.WEST);

        final javax.swing.JTextField txtBuscar = new javax.swing.JTextField();
        txtBuscar.setFont(fontGeneral);
        txtBuscar.setBackground(slate800);
        txtBuscar.setForeground(slate100);
        txtBuscar.setCaretColor(slate100);
        txtBuscar.setBorder(javax.swing.BorderFactory.createLineBorder(slate700));
        pnlTop.add(txtBuscar, java.awt.BorderLayout.CENTER);

        dialog.add(pnlTop, java.awt.BorderLayout.NORTH);

        // Center Table panel
        final DefaultTableModel modelPlatos = new DefaultTableModel(new Object[] { "ID", "Nombre", "Precio" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        final javax.swing.JTable tblPlatos = new javax.swing.JTable(modelPlatos);
        tblPlatos.setFont(fontGeneral);
        tblPlatos.setBackground(slate800);
        tblPlatos.setForeground(slate100);
        tblPlatos.setGridColor(slate700);
        tblPlatos.setRowHeight(25);
        tblPlatos.getTableHeader().setBackground(slate700);
        tblPlatos.getTableHeader().setForeground(slate100);
        tblPlatos.getTableHeader().setFont(fontBold);

        // Scroll pane
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(tblPlatos);
        scrollPane.getViewport().setBackground(new java.awt.Color(15, 23, 42));
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        dialog.add(scrollPane, java.awt.BorderLayout.CENTER);

        final PlatosDao platosDao = new PlatosDao();
        final Runnable cargarPlatos = new Runnable() {
            @Override
            public void run() {
                String filtro = txtBuscar.getText().trim();
                java.util.List<Plato> platos = platosDao.Listar(filtro);
                modelPlatos.setRowCount(0);
                for (Plato p : platos) {
                    modelPlatos.addRow(new Object[] { p.getId(), p.getNombre(), p.getPrecio() });
                }
            }
        };

        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                cargarPlatos.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                cargarPlatos.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                cargarPlatos.run();
            }
        });

        // Bottom action panel
        javax.swing.JPanel pnlBottom = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));
        pnlBottom.setBackground(new java.awt.Color(15, 23, 42));

        final javax.swing.JButton btnAgregar = new javax.swing.JButton("Agregar");
        btnAgregar.setBackground(new java.awt.Color(34, 197, 94));
        btnAgregar.setForeground(slate100);
        btnAgregar.setFont(fontBold);

        final javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
        btnCancelar.setBackground(new java.awt.Color(239, 68, 68));
        btnCancelar.setForeground(slate100);
        btnCancelar.setFont(fontBold);

        pnlBottom.add(btnAgregar);
        pnlBottom.add(btnCancelar);
        dialog.add(pnlBottom, java.awt.BorderLayout.SOUTH);

        tblPlatos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btnAgregar.doClick();
                }
            }
        });

        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = tblPlatos.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(dialog, "Selecciona un plato de la lista", "Advertencia",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idPlato = Integer.parseInt(tblPlatos.getValueAt(row, 0).toString());
                String nombrePlato = tblPlatos.getValueAt(row, 1).toString();
                double precioPlato = Double.parseDouble(tblPlatos.getValueAt(row, 2).toString());

                dialog.dispose();

                DetallePedido det = new DetallePedido();
                det.setNombre(nombrePlato);
                det.setPrecio(precioPlato);
                det.setCantidad(1);
                det.setComentario("");
                det.setId_pedido(idPedido);

                if (pedDao.RegistrarDetalle(det)) {
                    verPedidoDetalle(idPedido);
                    TotalPagar(tableFinalizar, totalFinalizar);
                    JOptionPane.showMessageDialog(null, "Plato agregado al pedido");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al agregar el plato");
                }
            }
        });

        cargarPlatos.run();
        dialog.setVisible(true);
    }// GEN-LAST:event_btnAddPlatoFinalizarActionPerformed

    private void btnEliminarPlatoFinalizarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEliminarPlatoFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pedido seleccionado");
            return;
        }
        if (tableFinalizar.getSelectedRow() >= 0) {
            try {
                int idPedido = Integer.parseInt(txtIdPedido.getText());
                int idDetalle = Integer
                        .parseInt(tableFinalizar.getValueAt(tableFinalizar.getSelectedRow(), 0).toString());
                if (pedDao.eliminarDetalle(idDetalle)) {
                    modelo = (DefaultTableModel) tableFinalizar.getModel();
                    modelo.removeRow(tableFinalizar.getSelectedRow());
                    TotalPagar(tableFinalizar, totalFinalizar);
                    JOptionPane.showMessageDialog(null, "Plato eliminado del pedido");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al eliminar el plato");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Error: ID invÃ¡lido");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        }
    }// GEN-LAST:event_btnEliminarPlatoFinalizarActionPerformed

    private void btnPdfPedidoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPdfPedidoActionPerformed
        if (txtIdHistorialPedido.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Selecciona una fila");
        } else {
            final int idPedido = Integer.parseInt(txtIdHistorialPedido.getText());
            btnPdfPedido.setEnabled(false);
            btnPdfPedido.setText("Generando...");
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    pedDao.pdfPedido(idPedido);
                    return null;
                }

                @Override
                protected void done() {
                    btnPdfPedido.setEnabled(true);
                    btnPdfPedido.setText("PDF Pedido");
                    txtIdHistorialPedido.setText("");
                    JOptionPane.showMessageDialog(null, "âœ… PDF generado correctamente.");
                }
            }.execute();
        }
    }// GEN-LAST:event_btnPdfPedidoActionPerformed

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int idPedido = Integer.parseInt(txtIdPedido.getText());
        double totalCons = 0.0;
        for (int i = 0; i < tableFinalizar.getRowCount(); i++) {
            double sub = parseDoubleSafe(tableFinalizar.getValueAt(i, 4).toString());
            totalCons += sub;
        }
        final double totalConsumo = totalCons;

        // Custom Dialog for Checkout (DlgCobro)
        final javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Cobro de Pedido #" + idPedido, true);
        dialog.setLayout(new java.awt.GridBagLayout());
        dialog.getContentPane().setBackground(new java.awt.Color(15, 23, 42)); // Slate 900

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        java.awt.Font fontLabel = new java.awt.Font("Outfit", java.awt.Font.BOLD, 14);
        java.awt.Font fontValue = new java.awt.Font("Outfit", java.awt.Font.PLAIN, 14);
        java.awt.Font fontBig = new java.awt.Font("Outfit", java.awt.Font.BOLD, 22);

        final java.awt.Color slate100 = new java.awt.Color(241, 245, 249);
        final java.awt.Color slate300 = new java.awt.Color(203, 213, 225);
        final java.awt.Color slate700 = new java.awt.Color(51, 65, 85);
        final java.awt.Color slate800 = new java.awt.Color(30, 41, 59);

        // 1. Total Consumo
        gbc.gridx = 0;
        gbc.gridy = 0;
        javax.swing.JLabel lblTotal = new javax.swing.JLabel("Total Consumo:");
        lblTotal.setFont(fontLabel);
        lblTotal.setForeground(slate300);
        dialog.add(lblTotal, gbc);

        gbc.gridx = 1;
        javax.swing.JLabel lblTotalVal = new javax.swing.JLabel(String.format("COP %,.2f", totalConsumo));
        lblTotalVal.setFont(fontBig);
        lblTotalVal.setForeground(new java.awt.Color(34, 197, 94)); // Emerald Green
        dialog.add(lblTotalVal, gbc);

        // 2. Pago Efectivo
        gbc.gridx = 0;
        gbc.gridy = 1;
        javax.swing.JLabel lblEfectivo = new javax.swing.JLabel("Pago Efectivo:");
        lblEfectivo.setFont(fontLabel);
        lblEfectivo.setForeground(slate300);
        dialog.add(lblEfectivo, gbc);

        gbc.gridx = 1;
        final javax.swing.JTextField txtEfectivo = new javax.swing.JTextField(12);
        txtEfectivo.setFont(fontValue);
        txtEfectivo.setBackground(slate800);
        txtEfectivo.setForeground(slate100);
        txtEfectivo.setCaretColor(slate100);
        txtEfectivo.setBorder(javax.swing.BorderFactory.createLineBorder(slate700));
        dialog.add(txtEfectivo, gbc);

        // 3. Pago Transferencia
        gbc.gridx = 0;
        gbc.gridy = 2;
        javax.swing.JLabel lblTrans = new javax.swing.JLabel("Pago Transferencia:");
        lblTrans.setFont(fontLabel);
        lblTrans.setForeground(slate300);
        dialog.add(lblTrans, gbc);

        gbc.gridx = 1;
        final javax.swing.JTextField txtTrans = new javax.swing.JTextField(12);
        txtTrans.setFont(fontValue);
        txtTrans.setBackground(slate800);
        txtTrans.setForeground(slate100);
        txtTrans.setCaretColor(slate100);
        txtTrans.setBorder(javax.swing.BorderFactory.createLineBorder(slate700));
        dialog.add(txtTrans, gbc);

        // 4. Cambio / Vueltos
        gbc.gridx = 0;
        gbc.gridy = 3;
        javax.swing.JLabel lblCambio = new javax.swing.JLabel("Cambio / Vueltos:");
        lblCambio.setFont(fontLabel);
        lblCambio.setForeground(slate300);
        dialog.add(lblCambio, gbc);

        gbc.gridx = 1;
        final javax.swing.JLabel lblCambioVal = new javax.swing.JLabel("COP 0.00");
        lblCambioVal.setFont(fontBig);
        lblCambioVal.setForeground(new java.awt.Color(234, 179, 8)); // Yellow/Amber
        dialog.add(lblCambioVal, gbc);

        // Pre-fill logic based on Room
        String salaActual = txtSalaFinalizar.getText();
        if (salaActual.equalsIgnoreCase("EFECTIVO")) {
            txtEfectivo.setText(String.format(java.util.Locale.US, "%.0f", totalConsumo));
            txtTrans.setText("0");
        } else if (salaActual.equalsIgnoreCase("TRANSACCIONES")) {
            txtEfectivo.setText("0");
            txtTrans.setText(String.format(java.util.Locale.US, "%.0f", totalConsumo));
        } else {
            txtEfectivo.setText("0");
            txtTrans.setText("0");
        }

        final Runnable recalcularCambio = new Runnable() {
            @Override
            public void run() {
                try {
                    double ef = parseDoubleSafe(txtEfectivo.getText());
                    double tr = parseDoubleSafe(txtTrans.getText());
                    double totalPagado = ef + tr;
                    double cambio = totalPagado - totalConsumo;
                    if (cambio < 0) {
                        lblCambioVal.setText("Restante: " + String.format("COP %,.2f", Math.abs(cambio)));
                        lblCambioVal.setForeground(new java.awt.Color(239, 68, 68)); // Red
                    } else {
                        lblCambioVal.setText(String.format("COP %,.2f", cambio));
                        lblCambioVal.setForeground(new java.awt.Color(34, 197, 94)); // Green
                    }
                } catch (NumberFormatException ex) {
                    lblCambioVal.setText("Valor invÃ¡lido");
                    lblCambioVal.setForeground(new java.awt.Color(239, 68, 68));
                }
            }
        };

        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                recalcularCambio.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                recalcularCambio.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                recalcularCambio.run();
            }
        };
        txtEfectivo.getDocument().addDocumentListener(dl);
        txtTrans.getDocument().addDocumentListener(dl);

        recalcularCambio.run();

        // Quick Actions (En 2 filas organizadas para no amontonar botones)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        javax.swing.JPanel pnlQuickContainer = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 5, 5));
        pnlQuickContainer.setBackground(new java.awt.Color(15, 23, 42));

        javax.swing.JPanel pnlQuickMethods = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 2));
        pnlQuickMethods.setOpaque(false);

        javax.swing.JButton btnQuickEf = new javax.swing.JButton("💵 Todo en Efectivo");
        btnQuickEf.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnQuickEf.setBackground(slate700);
        btnQuickEf.setForeground(slate100);
        btnQuickEf.setFocusPainted(false);
        btnQuickEf.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtEfectivo.setText(String.format(java.util.Locale.US, "%.0f", totalConsumo));
                txtTrans.setText("0");
                recalcularCambio.run();
            }
        });
        pnlQuickMethods.add(btnQuickEf);

        javax.swing.JButton btnQuickTr = new javax.swing.JButton("📱 Todo en Transferencia");
        btnQuickTr.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnQuickTr.setBackground(slate700);
        btnQuickTr.setForeground(slate100);
        btnQuickTr.setFocusPainted(false);
        btnQuickTr.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtEfectivo.setText("0");
                txtTrans.setText(String.format(java.util.Locale.US, "%.0f", totalConsumo));
                recalcularCambio.run();
            }
        });
        pnlQuickMethods.add(btnQuickTr);

        // Fila 2: Billetes Rápidos ($10k, $20k, $50k, $100k)
        javax.swing.JPanel pnlQuickDenoms = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 2));
        pnlQuickDenoms.setOpaque(false);
        double[] denomsCobro = { 10000, 20000, 50000, 100000 };
        for (double val : denomsCobro) {
            javax.swing.JButton btnDenom = new javax.swing.JButton(
                    "$" + String.format(java.util.Locale.US, "%,.0f", val));
            btnDenom.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btnDenom.setBackground(slate700);
            btnDenom.setForeground(new java.awt.Color(56, 189, 248)); // Sky blue
            btnDenom.setFocusPainted(false);
            btnDenom.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnDenom.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    txtEfectivo.setText(String.format(java.util.Locale.US, "%.0f", val));
                    txtTrans.setText("0");
                    recalcularCambio.run();
                }
            });
            pnlQuickDenoms.add(btnDenom);
        }

        pnlQuickContainer.add(pnlQuickMethods);
        pnlQuickContainer.add(pnlQuickDenoms);
        dialog.add(pnlQuickContainer, gbc);

        // Action Buttons
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        javax.swing.JPanel pnlActions = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));
        pnlActions.setBackground(new java.awt.Color(15, 23, 42));

        final javax.swing.JButton btnAceptar = new javax.swing.JButton("Finalizar y Facturar");
        btnAceptar.setBackground(new java.awt.Color(34, 197, 94));
        btnAceptar.setForeground(slate100);
        btnAceptar.setFont(fontLabel);

        final javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
        btnCancelar.setBackground(new java.awt.Color(239, 68, 68));
        btnCancelar.setForeground(slate100);
        btnCancelar.setFont(fontLabel);

        pnlActions.add(btnAceptar);
        pnlActions.add(btnCancelar);
        dialog.add(pnlActions, gbc);

        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                try {
                    final double ef = parseDoubleSafe(txtEfectivo.getText());
                    final double tr = parseDoubleSafe(txtTrans.getText());
                    if (ef + tr < totalConsumo) {
                        JOptionPane.showMessageDialog(dialog,
                                "Monto insuficiente. Faltan COP " + String.format("%,.2f", (totalConsumo - (ef + tr))),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    dialog.dispose();

                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setText("⏳ Procesando...");
                    new javax.swing.SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            boolean ok;
                            String tipoPago = (ef > 0 && tr > 0) ? "MIXTO" : (tr > 0 ? "TRANSACCION" : "EFECTIVO");
                            if (idsRondasAcumuladasCobro != null && idsRondasAcumuladasCobro.size() > 1) {
                                ok = pedDao.finalizarMultiplePedidosConPago(idsRondasAcumuladasCobro, tipoPago, ef, tr, 0.0);
                            } else {
                                ok = pedDao.finalizarPedidoConPago(idPedido, ef, tr);
                            }
                            if (ok) {
                                SonidoPOS.reproducirCobro();
                                SonidoPOS.anunciarVoz("Pedido finalizado con éxito.");
                                pedDao.pdfPedido(idPedido);
                                try {
                                    Modelo.ImpresionTicket impresion = new Modelo.ImpresionTicket();
                                    impresion.imprimirTicket(idPedido, tableFinalizar);
                                } catch (Exception ex) {
                                    System.out.println("Error al imprimir ticket: " + ex.getMessage());
                                }
                                PedidosDao pedidosDao = new PedidosDao();
                                pedidosDao.generarReporteDiario();
                            }
                            return ok;
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean ok = get();
                                btnFinalizar.setEnabled(true);
                                btnFinalizar.setText("Finalizar");
                                if (ok) {
                                    LimpiarTable();
                                    ListarPedidos();
                                    actualizarTotalDia();
                                    if (moduloCaja != null)
                                        moduloCaja.actualizarEstadoCajaUI();
                                    double vueltos = (ef + tr) - totalConsumo;
                                    String msg = "✅ Pedido finalizado correctamente.";
                                    if (vueltos > 0) {
                                        msg += "\nCambio/Vueltos a entregar: COP " + String.format("%,.2f", vueltos);
                                    }
                                    JOptionPane.showMessageDialog(Sistema.this, msg, "Éxito",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(Sistema.this, "Error al finalizar el pedido.",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception e) {
                                btnFinalizar.setEnabled(true);
                                btnFinalizar.setText("Finalizar");
                                JOptionPane.showMessageDialog(Sistema.this, "Error inesperado: " + e.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Por favor ingrese valores numéricos válidos.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setSize(580, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }// GEN-LAST:event_btnFinalizarActionPerformed

    private void btnEliminarTempPlatoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEliminarTempPlatoActionPerformed
        modelo = (DefaultTableModel) tableMenu.getModel();
        modelo.removeRow(tableMenu.getSelectedRow());
        TotalPagar(tableMenu, totalMenu);
    }// GEN-LAST:event_btnEliminarTempPlatoActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        if (txtComentario.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "SELECCIONE UNA FILA");
        } else {
            int id = Integer.parseInt(tableMenu.getValueAt(tableMenu.getSelectedRow(), 0).toString());
            for (int i = 0; i < tableMenu.getRowCount(); i++) {
                if (tableMenu.getValueAt(i, 0).equals(id)) {
                    tmp.setValueAt(txtComentario.getText(), i, 5);
                    txtComentario.setText("");
                    tableMenu.clearSelection();
                    return;
                }
            }
        }
    }// GEN-LAST:event_jButton2ActionPerformed

    private void btnGenerarPedidoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnGenerarPedidoActionPerformed
        if (tableMenu.getRowCount() > 0) {
            RegistrarPedido();
            detallePedido();
            LimpiarTableMenu();
            ToastNotification.exito(this, "¡Pedido registrado correctamente!");

            // Retornar al mapa de mesas de la sala actual (con mesas actualizadas en vivo)
            try {
                int idSalaActual = Integer.parseInt(txtTempIdSala.getText());
                int cantMesas = 10;
                for (Sala s : slDao.Listar()) {
                    if (s.getId() == idSalaActual) {
                        cantMesas = s.getMesas();
                        break;
                    }
                }
                PanelMesas.removeAll();
                panelMesas(idSalaActual, cantMesas);
                jTabbedPane1.setSelectedIndex(2);
            } catch (Exception e) {
                btnSala.doClick();
            }

            LimpiarTable();
            ListarPedidos();
            actualizarTotalDia();
        } else {
            ToastNotification.advertencia(this, "No hay productos agregados en la comanda.");
        }
    }// GEN-LAST:event_btnGenerarPedidoActionPerformed

    private void btnAddPlatoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddPlatoActionPerformed
        if (tblTemPlatos.getSelectedRow() >= 0) {
            int id = Integer.parseInt(tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 0).toString());
            String descripcion = tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 1).toString();
            double precio = Double.parseDouble(tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 2).toString());
            double total = 1 * precio;
            item = item + 1;
            tmp = (DefaultTableModel) tableMenu.getModel();
            for (int i = 0; i < tableMenu.getRowCount(); i++) {
                if (tableMenu.getValueAt(i, 0).equals(id)) {
                    int cantActual = Integer.parseInt(tableMenu.getValueAt(i, 2).toString());
                    int nuevoCantidad = cantActual + 1;
                    double nuevoSub = precio * nuevoCantidad;
                    tmp.setValueAt(nuevoCantidad, i, 2);
                    tmp.setValueAt(nuevoSub, i, 4);
                    TotalPagar(tableMenu, totalMenu);
                    return;
                }
            }
            ArrayList lista = new ArrayList();
            lista.add(item);
            lista.add(id);
            lista.add(descripcion);
            lista.add(1);
            lista.add(precio);
            lista.add(total);
            Object[] O = new Object[6];
            O[0] = lista.get(1);
            O[1] = lista.get(2);
            O[2] = lista.get(3);
            O[3] = lista.get(4);
            O[4] = lista.get(5);
            O[5] = "";
            tmp.addRow(O);
            tableMenu.setModel(tmp);
            TotalPagar(tableMenu, totalMenu);
        } else {
            JOptionPane.showMessageDialog(null, "SELECCIONA UNA FILA");
        }
    }// GEN-LAST:event_btnAddPlatoActionPerformed

    private void txtBuscarPlatoKeyReleased(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_txtBuscarPlatoKeyReleased
        LimpiarTable();
        ListarPlatos(tblTemPlatos);
    }// GEN-LAST:event_txtBuscarPlatoKeyReleased

    private void btnEliminarSalaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEliminarSalaActionPerformed
        if (!"".equals(txtIdSala.getText())) {
            int pregunta = JOptionPane.showConfirmDialog(null, "Â¿EstÃ¡ seguro de eliminar esta sala?",
                    "Confirmar eliminaciÃ³n", JOptionPane.YES_NO_OPTION);
            if (pregunta == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(txtIdSala.getText());
                boolean eliminado = slDao.Eliminar(id);
                if (eliminado) {
                    JOptionPane.showMessageDialog(null, "Sala eliminada correctamente");
                    LimpiarSala();
                    ListarSalas();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "No se puede eliminar la sala. AsegÃºrese de que no tenga pedidos asociados.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        }
    }// GEN-LAST:event_btnEliminarSalaActionPerformed

    private void btnNuevoSalaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnNuevoSalaActionPerformed
        LimpiarSala();
    }// GEN-LAST:event_btnNuevoSalaActionPerformed

    private void btnActualizarSalaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnActualizarSalaActionPerformed
        if ("".equals(txtIdSala.getText())) {
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        } else {
            if (!"".equals(txtNombreSala.getText()) && !"".equals(txtMesas.getText())) {
                try {
                    sl.setNombre(txtNombreSala.getText());
                    sl.setMesas(Integer.parseInt(txtMesas.getText()));
                    sl.setId(Integer.parseInt(txtIdSala.getText()));
                    slDao.Modificar(sl);
                    JOptionPane.showMessageDialog(null, "Sala actualizada correctamente");
                    LimpiarSala();
                    ListarSalas();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "El nÃºmero de mesas debe ser un entero vÃ¡lido");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Complete todos los campos (Nombre y Mesas)");
            }
        }
    }// GEN-LAST:event_btnActualizarSalaActionPerformed

    private void btnRegistrarSalaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRegistrarSalaActionPerformed
        if (txtNombreSala.getText().isEmpty() || txtMesas.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Los campos estÃ¡n vacÃ­os");
            return;
        }
        try {
            sl.setNombre(txtNombreSala.getText());
            sl.setMesas(Integer.parseInt(txtMesas.getText()));
            slDao.RegistrarSala(sl);

            // Ã‰xito: Feedback visual en el botÃ³n en lugar de popup bloqueante
            mostrarExitoEnBoton(btnRegistrarSala, "REGISTRAR", "âœ… Registrado");

            LimpiarSala();
            ListarSalas();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El nÃºmero de mesas debe ser un entero vÃ¡lido");
        }
    }// GEN-LAST:event_btnRegistrarSalaActionPerformed

    private void tableSalaMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tableSalaMouseClicked
        int fila = tableSala.rowAtPoint(evt.getPoint());
        txtIdSala.setText(tableSala.getValueAt(fila, 0).toString());
        txtNombreSala.setText(tableSala.getValueAt(fila, 1).toString());
        txtMesas.setText(tableSala.getValueAt(fila, 2).toString());
    }// GEN-LAST:event_tableSalaMouseClicked

    private void txtTotalDiaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtTotalDiaActionPerformed
    }// GEN-LAST:event_txtTotalDiaActionPerformed

    private void txtTotalDiaTransActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtTotalDiaTransActionPerformed
    }// GEN-LAST:event_txtTotalDiaTransActionPerformed

    private void txtIdHistorialPedidoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtIdHistorialPedidoActionPerformed
    }// GEN-LAST:event_txtIdHistorialPedidoActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnImprimirActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un pedido para imprimir");
            return;
        }
        try {
            int idPedido = Integer.parseInt(txtIdPedido.getText());
            if (tableFinalizar.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "El pedido no tiene detalles para imprimir");
                return;
            }
            ImpresionTicket impresion = new ImpresionTicket();
            impresion.imprimirTicket(idPedido, tableFinalizar);
            JOptionPane.showMessageDialog(null, "Ticket impreso correctamente");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID de pedido invÃ¡lido");
        }
    }// GEN-LAST:event_btnImprimirActionPerformed

    private void btnEliminarPedidoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEliminarPedidoActionPerformed
        String idInput = JOptionPane.showInputDialog(this, "Ingrese el ID del pedido a eliminar:", "Eliminar Pedido",
                JOptionPane.PLAIN_MESSAGE);
        if (idInput == null || idInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ingresÃ³ un ID vÃ¡lido.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPedido;
        try {
            idPedido = Integer.parseInt(idInput.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un nÃºmero entero vÃ¡lido.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el pedido existe
        PedidosDao pedidosDao = new PedidosDao();
        Pedido pedido = pedidosDao.verPedido(idPedido);
        if (pedido.getId() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontrÃ³ un pedido con ID " + idPedido + ".", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "Â¿EstÃ¡ seguro de que desea eliminar el pedido con ID " + idPedido + "?",
                "Confirmar EliminaciÃ³n",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = pedidosDao.eliminarPedidoPorId(idPedido);
            if (eliminado) {
                cargarPedidosDelDia(); // Actualizar la tabla con el rango del dÃ­a operativo
                JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.", "Ã‰xito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }// GEN-LAST:event_btnEliminarPedidoActionPerformed

    private void btnTransaccionActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTransaccionActionPerformed
        btnFinalizarActionPerformed(evt);
    }// GEN-LAST:event_btnTransaccionActionPerformed

    private void btnEfectivoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnEfectivoActionPerformed
        btnFinalizarActionPerformed(evt);
    }// GEN-LAST:event_btnEfectivoActionPerformed

    private void jComboSalasActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jComboSalasActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String salaSeleccionada = (String) jComboSalas.getSelectedItem();
        if (salaSeleccionada == null || salaSeleccionada.equals("Seleccionar")) {
            return;
        }
        // Validar si la sala seleccionada es diferente de la actual
        String salaActual = txtSalaFinalizar.getText();
        if (salaSeleccionada.equals(salaActual)) {
            return; // No hacer nada si la sala no cambiÃ³
        }
        try {
            int idPedido = Integer.parseInt(txtIdPedido.getText());
            int numMesa = Integer.parseInt(txtNumMesaFinalizar.getText());
            SalasDao salaDao = new SalasDao();
            int idSala = salaDao.buscarIdSalaPorNombre(salaSeleccionada);
            if (idSala == 0) {
                JOptionPane.showMessageDialog(this, "Sala no encontrada", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Verificar si la mesa estÃ¡ ocupada en la sala seleccionada
            PedidosDao pedDao = new PedidosDao();
            if (pedDao.mesaOcupada(idSala, numMesa, idPedido)) {
                JOptionPane.showMessageDialog(this, "La mesa " + numMesa + " en la sala " + salaSeleccionada
                        + " ya estÃ¡ ocupada por otro pedido pendiente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Actualizar la sala si la mesa estÃ¡ libre
            if (pedDao.actualizarSalaPedido(idPedido, idSala)) {
                txtSalaFinalizar.setText(salaSeleccionada);
                JOptionPane.showMessageDialog(this, "Sala actualizada a " + salaSeleccionada, "Ã‰xito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar la sala", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de pedido o nÃºmero de mesa invÃ¡lido", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_jComboSalasActionPerformed

    private void BtnImprimirDiaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnImprimirDiaActionPerformed
        try {
            ImpresionTicket impresion = new ImpresionTicket();
            impresion.imprimirTotalesDiariosMinimal();
            JOptionPane.showMessageDialog(null, "Ticket de totales diarios impreso correctamente");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al imprimir totales diarios: " + e.getMessage());
        }
    }// GEN-LAST:event_BtnImprimirDiaActionPerformed

    private void BtnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_BtnCerrarSesionActionPerformed
        /// Confirmar si el usuario desea cerrar sesiÃ³n
        Object[] options = { "Volver al Login", "Salir de la AplicaciÃ³n", "Cancelar" };
        int confirmacion = JOptionPane.showOptionDialog(this,
                "Â¿QuÃ© desea hacer?",
                "Cerrar SesiÃ³n",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (confirmacion == 0) { // Volver al Login
            // Limpiar datos de la sesiÃ³n
            LabelVendedor.setText("");
            // Opcional: Totalpagar = 0.0; LimpiarTableMenu(); LimpiarTable();

            // Cerrar la ventana actual
            dispose();

            // Abrir la ventana de login
            try {
                FrmLogin login = new FrmLogin();
                login.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error al abrir la ventana de login: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (confirmacion == 1) { // Salir de la AplicaciÃ³n
            System.exit(0);
        }

    }// GEN-LAST:event_BtnCerrarSesionActionPerformed

    private void btnModificarUsuaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnModificarUsuaActionPerformed
        // Asumiendo que la tabla se llama tablaUsuarios
        int fila = TableUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario de la tabla",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el ID del usuario seleccionado
        int id = Integer.parseInt(TableUsuarios.getValueAt(fila, 0).toString()); // Ajusta segÃºn la columna del ID
        LoginDao loginDao = new LoginDao();
        Login usuario = loginDao.buscarUsuarioPorId(id);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontrÃ³ el usuario",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmar si el usuario desea modificar o eliminar
        String[] options = { "Modificar", "Eliminar" };
        int confirmacion = JOptionPane.showOptionDialog(this,
                "Â¿QuÃ© desea hacer con " + usuario.getNombre() + "?",
                "GestiÃ³n de Usuario",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (confirmacion == 0) { // Modificar
            // Crear un formulario para modificar datos
            JDialog dialog = new JDialog(this, "Modificar Usuario", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10)); // Aumentar filas para botÃ³n extra
            dialog.setSize(400, 380);
            dialog.setLocationRelativeTo(this);

            // Campos para editar datos
            JLabel lblNombre = new JLabel("Nombre:");
            JTextField txtNombre = new JTextField(usuario.getNombre());
            JLabel lblCorreo = new JLabel("Usuario:");
            JTextField txtCorreo = new JTextField(usuario.getCorreo());
            JLabel lblPass = new JLabel("ContraseÃ±a:");
            JPasswordField txtPass = new JPasswordField(usuario.getPass());
            JLabel lblRol = new JLabel("Rol:");
            JComboBox<String> cmbRol = new JComboBox<>(new String[] { "Administrador", "Asistente" });
            cmbRol.setSelectedItem(usuario.getRol()); // Seleccionar el rol actual

            JButton btnGuardar = new JButton("Guardar");
            JButton btnEliminar = new JButton("Eliminar"); // BotÃ³n para eliminar
            JButton btnCancelar = new JButton("Cancelar");

            // Agregar componentes al diÃ¡logo
            dialog.add(lblNombre);
            dialog.add(txtNombre);
            dialog.add(lblCorreo);
            dialog.add(txtCorreo);
            dialog.add(lblPass);
            dialog.add(txtPass);
            dialog.add(lblRol);
            dialog.add(cmbRol);
            dialog.add(new JLabel()); // Espacio vacÃ­o
            dialog.add(btnGuardar);
            dialog.add(new JLabel()); // Espacio vacÃ­o
            dialog.add(btnEliminar);
            dialog.add(new JLabel()); // Espacio vacÃ­o
            dialog.add(btnCancelar);

            // AcciÃ³n del botÃ³n Guardar
            btnGuardar.addActionListener(e -> {
                String nombre = txtNombre.getText().trim();
                String correo = txtCorreo.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                String rol = (String) cmbRol.getSelectedItem();

                // Validar datos
                if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || rol == null) {
                    JOptionPane.showMessageDialog(dialog,
                            "Todos los campos son obligatorios",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar objeto login
                usuario.setNombre(nombre);
                usuario.setCorreo(correo);
                usuario.setPass(pass);
                usuario.setRol(rol);

                // Actualizar en la base de datos
                boolean actualizado = loginDao.actualizarUsuario(usuario);

                if (actualizado) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario actualizado correctamente",
                            "Ã‰xito",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarTablaUsuarios(); // MÃ©todo para recargar la tabla
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // AcciÃ³n del botÃ³n Eliminar
            btnEliminar.addActionListener(e -> {
                int deleteConfirm = JOptionPane.showConfirmDialog(dialog,
                        "Â¿EstÃ¡ seguro de que desea eliminar a " + usuario.getNombre() + "?",
                        "Confirmar EliminaciÃ³n",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (deleteConfirm == JOptionPane.YES_OPTION) {
                    boolean eliminado = loginDao.eliminarUsuario(id); // MÃ©todo en LoginDao
                    if (eliminado) {
                        JOptionPane.showMessageDialog(dialog,
                                "Usuario eliminado correctamente",
                                "Ã‰xito",
                                JOptionPane.INFORMATION_MESSAGE);
                        cargarTablaUsuarios(); // Recargar la tabla
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Error al eliminar el usuario",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // AcciÃ³n del botÃ³n Cancelar
            btnCancelar.addActionListener(e -> dialog.dispose());

            // Mostrar el diÃ¡logo
            dialog.setVisible(true);
        } else if (confirmacion == 1) { // Eliminar
            int deleteConfirm = JOptionPane.showConfirmDialog(this,
                    "Â¿EstÃ¡ seguro de que desea eliminar a " + usuario.getNombre() + "?",
                    "Confirmar EliminaciÃ³n",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (deleteConfirm == JOptionPane.YES_OPTION) {
                boolean eliminado = loginDao.eliminarUsuario(id); // MÃ©todo en LoginDao
                if (eliminado) {
                    JOptionPane.showMessageDialog(this,
                            "Usuario eliminado correctamente",
                            "Ã‰xito",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarTablaUsuarios(); // Recargar la tabla
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar el usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }// GEN-LAST:event_btnModificarUsuaActionPerformed

    private void btnModificarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnIniciar1ActionPerformed
    }// GEN-LAST:event_btnIniciar1ActionPerformed

    private void txtPedidosDiaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtPedidosDiaActionPerformed
    }// GEN-LAST:event_txtPedidosDiaActionPerformed

    // ── Auto-refresh mesas (pedidos desde celular) ──────────────────────────
    private int salaActivaId = 0;
    private int salaActivaCant = 0;
    private javax.swing.JButton[] botonesActuales = new javax.swing.JButton[0];
    private javax.swing.Timer timerAutoRefreshMesas;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCerrarSesion;
    private javax.swing.JButton BtnImprimirDia;
    private javax.swing.JLabel LabelVendedor;
    private javax.swing.JPanel PanelMesas;
    private javax.swing.JPanel PanelSalas;
    private javax.swing.JTable TablePedidos;
    private javax.swing.JTable TablePlatos;
    public javax.swing.JTable TableUsuarios;
    private javax.swing.JButton btnActualizarConfig;
    private javax.swing.JButton btnActualizarSala;
    private javax.swing.JButton btnAddPlato;
    private javax.swing.JButton btnAddPlatoFinalizar;
    private javax.swing.JButton btnConfig;
    private javax.swing.JButton btnEditarPlato;
    private javax.swing.JButton btnEfectivo;
    private javax.swing.JButton btnEliminarPedido;
    private javax.swing.JButton btnEliminarPlato;
    private javax.swing.JButton btnEliminarPlatoFinalizar;
    private javax.swing.JButton btnEliminarSala;
    private javax.swing.JButton btnEliminarTempPlato;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnGenerarPedido;
    private javax.swing.JButton btnGuardarPlato;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnModificarUsua;
    private javax.swing.JButton btnNuevoPlato;
    private javax.swing.JButton btnNuevoSala;
    private javax.swing.JButton btnPdfPedido;
    private javax.swing.JButton btnPlatos;
    private javax.swing.JButton btnRegistrarSala;
    private javax.swing.JButton btnSala;
    private javax.swing.JButton btnTransaccion;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVentas;
    private javax.swing.JComboBox<String> cbxRol;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboSalas;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_wallpaper;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelLogo;
    private javax.swing.JTable tableFinalizar;
    private javax.swing.JTable tableMenu;
    private javax.swing.JTable tableSala;
    private javax.swing.JTable tblTemPlatos;
    private javax.swing.JPanel panelCardPlatos;
    private javax.swing.JPanel gridPanelPlatos;
    private javax.swing.JPanel panelDashboard;
    private javax.swing.JButton btnToggleHistorial;
    private javax.swing.JButton btnToggleDashboard;
    private javax.swing.JLabel lblEfectivoVal;
    private javax.swing.JLabel lblTransVal;
    private javax.swing.JLabel lblTotalVal;
    private javax.swing.JLabel lblEfectivoSub;
    private javax.swing.JLabel lblTransSub;
    private SalesTrendChart chartVentasHora;
    private TopDishesChart chartPlatosMasVendidos;
    private javax.swing.JLabel tipo;
    private javax.swing.JLabel totalFinalizar;
    private javax.swing.JLabel totalMenu;
    private javax.swing.JTextField txtBuscarPlato;
    private javax.swing.JTextField txtBuscarHistorial;
    private javax.swing.JButton btnReimprimirTicketHistorial;
    private javax.swing.JTextPane txtComentario;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccionConfig;
    private javax.swing.JTextField txtFechaHora;
    private javax.swing.JTextField txtIdConfig;
    private javax.swing.JTextField txtIdHistorialPedido;
    private javax.swing.JTextField txtIdPedido;
    private javax.swing.JTextField txtIdPlato;
    private javax.swing.JTextField txtIdSala;
    private javax.swing.JTextField txtMensaje;
    private javax.swing.JTextField txtMesas;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreConfig;
    private javax.swing.JTextField txtNombrePlato;
    private javax.swing.JTextField txtNombreSala;
    private javax.swing.JTextField txtNumMesaFinalizar;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtPedidosDia;
    private javax.swing.JTextField txtPrecioPlato;
    private javax.swing.JTextField txtRucConfig;
    private javax.swing.JTextField txtSalaFinalizar;
    private javax.swing.JTextField txtTelefonoConfig;
    private javax.swing.JTextField txtTempIdSala;
    private javax.swing.JTextField txtTempNumMesa;
    private javax.swing.JTextField txtTotalDia;
    private javax.swing.JTextField txtTotalDiaTrans;
    // End of variables declaration//GEN-END:variables

    private void TotalPagar(JTable tabla, JLabel label) {
        double consumo = 0.0;
        double pagado = 0.0;
        int numFila = tabla.getRowCount();
        boolean esFinalizar = (tabla == tableFinalizar);

        for (int i = 0; i < numFila; i++) {
            String nombre = tabla.getValueAt(i, 1).toString();
            double subtotal = Double.parseDouble(tabla.getValueAt(i, 4).toString());
            if (nombre.equals("PAGO EFECTIVO") || nombre.equals("PAGO TRANSACCION")) {
                pagado += subtotal;
            } else {
                consumo += subtotal;
            }
        }

        if (esFinalizar) {
            label.setText(String.format("%,.2f", consumo));
            Totalpagar = consumo;
        } else {
            Totalpagar = consumo;
            label.setText(String.format("%.2f", Totalpagar));
        }
    }

    private void LimpiarTableMenu() {
        tmp = (DefaultTableModel) tableMenu.getModel();
        int fila = tableMenu.getRowCount();
        for (int i = 0; i < fila; i++) {
            tmp.removeRow(0);
        }
    }

    public void ListarConfig() {
        conf = lgDao.datosEmpresa();
        txtIdConfig.setText("" + conf.getId());
        txtRucConfig.setText("" + conf.getRuc());
        txtNombreConfig.setText("" + conf.getNombre());
        txtTelefonoConfig.setText("" + conf.getTelefono());
        txtDireccionConfig.setText("" + conf.getDireccion());
        txtMensaje.setText("" + conf.getMensaje());
    }

    private void ListarPedidos() {
        new javax.swing.SwingWorker<java.util.List<Pedido>, Void>() {
            Timestamp fechaInicio, fechaFin;

            @Override
            protected java.util.List<Pedido> doInBackground() {
                LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
                LocalDateTime inicio, fin;
                if (ahora.getHour() < 4) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else if (ahora.getHour() < 16) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else {
                    inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                }
                fechaInicio = Timestamp.valueOf(inicio);
                fechaFin = Timestamp.valueOf(fin);
                System.out.println("ListarPedidos usando rango: " + fechaInicio + " - " + fechaFin);
                return pedDao.listarPedidosDelDia(fechaInicio, fechaFin);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<Pedido> Listar = get();
                    EstiloTablas color = new EstiloTablas();
                    modelo = (DefaultTableModel) TablePedidos.getModel();
                    modelo.setRowCount(0);
                    txtPedidosDia.setText(String.valueOf(Listar.size()));
                    Object[] ob = new Object[9];
                    for (int i = 0; i < Listar.size(); i++) {
                        ob[0] = Listar.get(i).getId();
                        ob[1] = Listar.get(i).getSala();
                        ob[2] = Listar.get(i).getUsuario();
                        ob[3] = Listar.get(i).getNum_mesa();
                        ob[4] = Listar.get(i).getFecha();
                        ob[5] = String.format("%.2f", Listar.get(i).getTotal());
                        ob[6] = Listar.get(i).getEstado();
                        ob[7] = Listar.get(i).getPago_efectivo();
                        ob[8] = Listar.get(i).getPago_transaccion();
                        modelo.addRow(ob);
                        System.out.println("Pedido " + i + ": Fecha = " + ob[4] + ", Total = " + ob[5]);
                    }
                    // Ocultar columnas auxiliares de pago (col 7 y 8)
                    TablePedidos.getColumnModel().getColumn(7).setMinWidth(0);
                    TablePedidos.getColumnModel().getColumn(7).setMaxWidth(0);
                    TablePedidos.getColumnModel().getColumn(7).setWidth(0);
                    TablePedidos.getColumnModel().getColumn(8).setMinWidth(0);
                    TablePedidos.getColumnModel().getColumn(8).setMaxWidth(0);
                    TablePedidos.getColumnModel().getColumn(8).setWidth(0);
                    colorHeader(TablePedidos);
                    TablePedidos.setDefaultRenderer(Object.class, color);
                    // Actualizar totales DESPUÃ‰S de que la conexiÃ³n anterior ya fue liberada
                    actualizarTotalDia();
                } catch (Exception e) {
                    System.out.println("Error al listar pedidos: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void LimpiarTable() {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            modelo.removeRow(i);
            i = i - 1;
        }
    }

    private void ListarUsuarios() {
        List<Login> Listar = lgDao.ListarUsuarios();
        modelo = (DefaultTableModel) TableUsuarios.getModel();
        modelo.setRowCount(0);
        Object[] ob = new Object[4];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getCorreo();
            ob[3] = Listar.get(i).getRol();
            modelo.addRow(ob);
        }
        colorHeader(TableUsuarios);
    }

    private void ListarSalas() {
        List<Sala> Listar = slDao.Listar();
        modelo = (DefaultTableModel) tableSala.getModel();
        modelo.setRowCount(0);
        Object[] ob = new Object[3];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getMesas();
            modelo.addRow(ob);
        }
        colorHeader(tableSala);
    }

    private void colorHeader(JTable tabla) {
        tabla.setModel(modelo);
        JTableHeader header = tabla.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 110, 255));
        header.setForeground(Color.white);
    }

    private void LimpiarSala() {
        txtIdSala.setText("");
        txtNombreSala.setText("");
        txtMesas.setText("");
    }

    private void LimpiarPlatos() {
        txtIdPlato.setText("");
        txtNombrePlato.setText("");
        txtPrecioPlato.setText("");
        // Restaurar botÃ³n GUARDAR a modo Registro (verde)
        btnGuardarPlato.setText("GUARDAR");
        btnGuardarPlato.setBackground(new java.awt.Color(46, 204, 113));
    }

    private void decorarSistemaUI() {
        // --- 1. Cabecera (Header) y Reloj ---
        jLabel38.setFont(getFontBold(26f));
        jLabel38.setForeground(new java.awt.Color(241, 245, 249)); // Slate claro
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // --- 2. Barra Lateral (Sidebar) Botones con Iconos Dedicados y Atajos
        // Elegantes ---
        btnPlatos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/platos.png")));
        btnSala.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/sala.png")));
        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pedidos.png")));
        btnCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png")));
        btnInventario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/inventario.png")));
        btnUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/usuarios.png")));
        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png")));

        // Textos estructurados en HTML para alineación uniforme de nombres y badges de
        // teclado
        btnSala.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Salas</b></font></td><td align='right'><font face='Consolas' size='2' color='#38BDF8'><b>F1</b></font></td></tr></table></html>");
        btnCaja.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Caja</b></font></td><td align='right'><font face='Consolas' size='2' color='#38BDF8'><b>F2</b></font></td></tr></table></html>");
        btnInventario.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Inventario</b></font></td><td align='right'><font face='Consolas' size='2' color='#38BDF8'><b>F3</b></font></td></tr></table></html>");
        btnUsuarios.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Usuarios</b></font></td><td align='right'><font face='Consolas' size='2' color='#38BDF8'><b>F4</b></font></td></tr></table></html>");
        btnConfig.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Config</b></font></td><td align='right'><font face='Consolas' size='2' color='#38BDF8'><b>F5</b></font></td></tr></table></html>");
        btnPlatos.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Carta</b></font></td><td align='right'></td></tr></table></html>");
        btnVentas.setText(
                "<html><table width='125' border='0' cellspacing='0' cellpadding='0'><tr><td align='left'><font face='Segoe UI' size='3' color='#FFFFFF'><b>Pedidos</b></font></td><td align='right'></td></tr></table></html>");

        // Tooltips con atajo de teclado para cada botón
        btnSala.setToolTipText("<html><b>F1</b> — Mapa de Salas y Mesas</html>");
        btnCaja.setToolTipText("<html><b>F2</b> — Caja / Arqueo</html>");
        btnInventario.setToolTipText("<html><b>F3</b> — Inventario</html>");
        btnUsuarios.setToolTipText("<html><b>F4</b> — Gestión de Usuarios</html>");
        btnConfig.setToolTipText("<html><b>F5</b> — Configuración y Carta</html>");
        btnPlatos.setToolTipText("<html>Carta / Menú del Día</html>");
        btnVentas.setToolTipText("<html>Pedidos activos y Dashboard</html>");
        BtnCerrarSesion.setToolTipText("<html><b>F11/F12</b> — Bloquear Pantalla</html>");

        javax.swing.JButton[] btns = { btnPlatos, btnSala, btnVentas, btnCaja, btnInventario, btnConfig, btnUsuarios };
        for (javax.swing.JButton btn : btns) {
            btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            btn.setIconTextGap(12);
            btn.setMargin(new java.awt.Insets(0, 12, 0, 10));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btn.isEnabled()) {
                        btn.setOpaque(true);
                        btn.setBackground(new java.awt.Color(255, 255, 255, 25)); // Blanco traslúcido
                        btn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0,
                                new java.awt.Color(56, 189, 248))); // Línea vertical azul
                        btn.getParent().repaint();
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setOpaque(false);
                    btn.setBorder(null);
                    btn.getParent().repaint();
                }
            });
        }

        // Cerrar sesiÃ³n
        BtnCerrarSesion.setFont(getFontBold(12f));
        BtnCerrarSesion.setForeground(new java.awt.Color(254, 226, 226)); // Rojo claro
        BtnCerrarSesion.setBorderPainted(false);
        BtnCerrarSesion.setContentAreaFilled(false);
        BtnCerrarSesion.setFocusPainted(false);
        BtnCerrarSesion.setOpaque(false);
        BtnCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                BtnCerrarSesion.setOpaque(true);
                BtnCerrarSesion.setBackground(new java.awt.Color(239, 68, 68, 60)); // Hover rojo traslÃºcido
                BtnCerrarSesion.getParent().repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                BtnCerrarSesion.setOpaque(false);
                BtnCerrarSesion.getParent().repaint();
            }
        });

        // --- 3. Cabeceras de Tablas Personalizadas (JTableHeader) ---
        javax.swing.JTable[] tablas = { tableSala, TableUsuarios, TablePlatos, TablePedidos, tableMenu, tableFinalizar,
                tblTemPlatos };
        for (javax.swing.JTable t : tablas) {
            if (t == null)
                continue;
            t.setBackground(new java.awt.Color(15, 23, 42));
            t.setFillsViewportHeight(true);
            if (t.getParent() instanceof javax.swing.JViewport) {
                t.getParent().setBackground(new java.awt.Color(15, 23, 42));
            }
            javax.swing.table.JTableHeader header = t.getTableHeader();
            if (header != null) {
                header.setFont(getFontBold(13f));
                header.setBackground(new java.awt.Color(30, 41, 59)); // Azul oscuro pizarra
                header.setForeground(java.awt.Color.WHITE);
                header.setReorderingAllowed(false);

                header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                row, column);
                        c.setBackground(new java.awt.Color(30, 41, 59));
                        c.setForeground(java.awt.Color.WHITE);
                        setFont(getFontBold(13f));
                        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                        setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1,
                                new java.awt.Color(51, 65, 85)));
                        return c;
                    }
                });
            }
        }

        // --- 4. Inputs (JTextFields) Modernos en Modo Oscuro ---
        javax.swing.JTextField[] camposInput = { txtNombreSala, txtMesas, txtTelefonoConfig, txtDireccionConfig,
                txtMensaje, txtRucConfig, txtNombreConfig, txtCorreo, txtPass, txtNombre, txtNombrePlato,
                txtPrecioPlato, txtBuscarPlato };
        for (javax.swing.JTextField tf : camposInput) {
            tf.setFont(getFontRegular(14f));
            tf.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
            tf.setForeground(java.awt.Color.WHITE);
            tf.setCaretColor(new java.awt.Color(96, 165, 250)); // Azul brillante
            tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, new java.awt.Color(71, 85, 105), new java.awt.Insets(6, 10, 6, 10)),
                    javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));

            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new RoundedBorder(8, new java.awt.Color(96, 165, 250), new java.awt.Insets(6, 10, 6, 10)), // Azul
                                                                                                                       // brillante
                                                                                                                       // al
                                                                                                                       // enfocar
                            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new RoundedBorder(8, new java.awt.Color(71, 85, 105), new java.awt.Insets(6, 10, 6, 10)),
                            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));
                }
            });
        }

        // --- 5. JComboBox Moderno ---
        cbxRol.setFont(getFontRegular(14f));
        cbxRol.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
        cbxRol.setForeground(java.awt.Color.WHITE);

        // --- 6. Paneles estilo "Card" (Tarjetas) ---
        javax.swing.JPanel[] cards = { jPanel10, jPanel15, jPanel2, jPanel8 };
        for (javax.swing.JPanel p : cards) {
            p.setBackground(new java.awt.Color(30, 41, 59)); // Slate 800
            p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new RoundedBorder(12, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)), // Borde
                                                                                                                // Slate
                                                                                                                // 600
                    javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        }

        // --- 7. Botones SemÃ¡nticos en Formularios CRUD ---
        // Registrar/Guardar (Verde)
        javax.swing.JButton[] successBtns = { btnRegistrarSala, btnIniciar, btnGuardarPlato, btnActualizarConfig };
        for (javax.swing.JButton btn : successBtns) {
            btn.setFont(getFontBold(13f));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(16, 185, 129)); // Emerald 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(16, 185, 129), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(5, 150, 105)); // Emerald 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(16, 185, 129));
                }
            });
        }

        // Modificar/Editar (Azul)
        javax.swing.JButton[] infoBtns = { btnActualizarSala, btnEditarPlato, btnNuevoPlato, btnNuevoSala,
                btnModificarUsua };
        for (javax.swing.JButton btn : infoBtns) {
            btn.setFont(getFontBold(13f));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(59, 130, 246)); // Blue 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(59, 130, 246), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(37, 99, 235)); // Blue 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(59, 130, 246));
                }
            });
        }

        // Eliminar (Rojo)
        javax.swing.JButton[] dangerBtns = { btnEliminarSala, btnEliminarPlato, btnEliminarPlatoFinalizar,
                btnEliminarPedido };
        for (javax.swing.JButton btn : dangerBtns) {
            btn.setFont(getFontBold(13f));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(239, 68, 68)); // Red 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(239, 68, 68), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(220, 38, 38)); // Red 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(239, 68, 68));
                }
            });
        }

        // Botones Especiales de Venta (Efectivo / TransacciÃ³n)
        btnEfectivo.setFont(getFontBold(14f));
        btnEfectivo.setForeground(java.awt.Color.WHITE);
        btnEfectivo.setBackground(new java.awt.Color(16, 185, 129));
        btnEfectivo
                .setBorder(new RoundedBorder(10, new java.awt.Color(16, 185, 129), new java.awt.Insets(8, 16, 8, 16)));
        btnEfectivo.setOpaque(true);
        btnEfectivo.setContentAreaFilled(false);

        btnTransaccion.setFont(getFontBold(14f));
        btnTransaccion.setForeground(java.awt.Color.WHITE);
        btnTransaccion.setBackground(new java.awt.Color(59, 130, 246));
        btnTransaccion
                .setBorder(new RoundedBorder(10, new java.awt.Color(59, 130, 246), new java.awt.Insets(8, 16, 8, 16)));
        btnTransaccion.setOpaque(true);
        btnTransaccion.setContentAreaFilled(false);

        // --- 8. Ocultar PestaÃ±as del TabbedPane ---
        jTabbedPane1.putClientProperty("JTabbedPane.showTabArea", false);
        jTabbedPane1.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int verticalPadding) {
                return 0; // Oculta las pestaÃ±as superiores
            }

            @Override
            protected void paintTabArea(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
                // No pintar nada
            }
        });

        // --- 9. Estilo General de Fondos y Transparencias (Modo Oscuro Slate) ---
        java.awt.Color darkSlate = new java.awt.Color(15, 23, 42); // Slate 900
        this.getContentPane().setBackground(darkSlate);
        jTabbedPane1.setBackground(darkSlate);
        jTabbedPane1.setOpaque(true);
        labelLogo.setOpaque(false);
        LabelVendedor.setOpaque(false);

        // Iterar de forma segura sobre todas las pestaÃ±as registradas para cambiar su
        // fondo y estilizar subcomponentes
        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
            java.awt.Component tabComponent = jTabbedPane1.getComponentAt(i);
            if (tabComponent != null) {
                tabComponent.setBackground(darkSlate);
                if (tabComponent instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent) tabComponent).setOpaque(true);
                }
                if (tabComponent instanceof java.awt.Container) {
                    styleComponentsRecursively((java.awt.Container) tabComponent);
                }
            }
        }

        // --- 10. CatÃ¡logo Visual de Platos (Grid de Tarjetas) ---
        panelCardPlatos = new javax.swing.JPanel(new java.awt.BorderLayout());
        panelCardPlatos.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        gridPanelPlatos = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        gridPanelPlatos.setBackground(new java.awt.Color(15, 23, 42));
        gridPanelPlatos.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panelCardPlatos.add(gridPanelPlatos, java.awt.BorderLayout.NORTH);
        jScrollPane10.setViewportView(panelCardPlatos);
        jScrollPane10.getVerticalScrollBar().setUnitIncrement(16);

        // --- 11. Dashboard de Ventas ---
        initDashboard();

        // --- 12. Estructurar Layout Responsivo Multi-Monitor (BorderLayout) ---
        if (jPanel1 != null)
            jPanel1.setVisible(false);
        if (jLabel38 != null)
            jLabel38.setVisible(false);
        if (BtnCerrarSesion != null)
            BtnCerrarSesion.setVisible(false);

        getContentPane().removeAll();
        getContentPane().setLayout(new java.awt.BorderLayout());

        construirTopNavbarPOS();

        getContentPane().add(topNavbarPOS, java.awt.BorderLayout.NORTH);
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        // --- 13. Inicializar Pantalla Principal Tipo Landing POS ---
        inicializarLandingPanel();

        // Estilizar globalmente toda la ventana
        styleComponentsRecursively(this.getContentPane());
    }

    private void styleComponentsRecursively(java.awt.Container container) {
        java.awt.Color bgDark = new java.awt.Color(15, 23, 42); // Slate 900
        java.awt.Color bgCard = new java.awt.Color(30, 41, 59); // Slate 800
        java.awt.Color bgInput = new java.awt.Color(51, 65, 85); // Slate 700
        java.awt.Color textLight = new java.awt.Color(241, 245, 249); // Slate 100

        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof javax.swing.JPanel) {
                javax.swing.JPanel p = (javax.swing.JPanel) child;
                if (p != jPanel10 && p != jPanel15 && p != jPanel2 && p != jPanel8 && p != jPanel1) {
                    p.setBackground(bgDark);
                    p.setOpaque(true);
                } else if (p != jPanel1) {
                    p.setBackground(bgCard);
                    p.setOpaque(true);
                }
            } else if (child instanceof javax.swing.JTabbedPane) {
                javax.swing.JTabbedPane tp = (javax.swing.JTabbedPane) child;
                tp.setBackground(bgDark);
                tp.setForeground(textLight);
                tp.setOpaque(true);
            } else if (child instanceof javax.swing.JLabel) {
                if (child.getParent() != jPanel1 && child != jLabel38) {
                    child.setForeground(textLight);
                    child.setFont(getFontBold(13f));
                }
            } else if (child instanceof javax.swing.JScrollPane) {
                child.setBackground(bgDark);
                javax.swing.JScrollPane sp = (javax.swing.JScrollPane) child;
                sp.getViewport().setBackground(bgDark);
                sp.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85), 1));
            } else if (child instanceof javax.swing.JViewport) {
                child.setBackground(bgDark);
            } else if (child instanceof javax.swing.JTable) {
                javax.swing.JTable t = (javax.swing.JTable) child;
                t.setBackground(bgDark);
                t.setForeground(textLight);
                t.setFont(getFontRegular(14f));
                t.setGridColor(new java.awt.Color(51, 65, 85));
                t.setFillsViewportHeight(true);
                t.setSelectionBackground(new java.awt.Color(30, 64, 175));
                t.setSelectionForeground(java.awt.Color.WHITE);
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(bgCard);
                    t.getTableHeader().setForeground(java.awt.Color.WHITE);
                }
            } else if (child instanceof javax.swing.JTextField || child instanceof javax.swing.JPasswordField) {
                child.setBackground(bgInput);
                child.setForeground(textLight);
                child.setFont(getFontRegular(14f));
                if (child instanceof javax.swing.JTextField) {
                    ((javax.swing.JTextField) child).setCaretColor(new java.awt.Color(96, 165, 250));
                }
            } else if (child instanceof javax.swing.JTextPane) {
                child.setBackground(bgInput);
                child.setForeground(textLight);
                child.setFont(getFontRegular(14f));
                ((javax.swing.JTextPane) child).setCaretColor(new java.awt.Color(96, 165, 250));
            } else if (child instanceof javax.swing.JComboBox) {
                child.setBackground(bgInput);
                child.setForeground(textLight);
                child.setFont(getFontRegular(14f));
            }

            if (child instanceof java.awt.Container) {
                styleComponentsRecursively((java.awt.Container) child);
            }
        }
    }

    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private java.awt.Color color;
        private java.awt.Insets insets;

        RoundedBorder(int radius, java.awt.Color color) {
            this.radius = radius;
            this.color = color;
            this.insets = new java.awt.Insets(6, 10, 6, 10);
        }

        RoundedBorder(int radius, java.awt.Color color, java.awt.Insets insets) {
            this.radius = radius;
            this.color = color;
            this.insets = insets;
        }

        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return insets;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new java.awt.geom.RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }

    private void panelSalas() {
        List<Sala> Listar = slDao.Listar();
        for (int i = 0; i < Listar.size(); i++) {
            int id = Listar.get(i).getId();
            int cantidad = Listar.get(i).getMesas();
            JButton boton = new JButton(Listar.get(i).getNombre(),
                    new ImageIcon(getClass().getResource("/Img/salas.png"))) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new java.awt.Color(15, 23, 42)); // Slate 900
                    } else if (getModel().isRollover()) {
                        g2.setColor(new java.awt.Color(51, 65, 85)); // Slate 700
                    } else {
                        g2.setColor(new java.awt.Color(30, 41, 59)); // Slate 800
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            boton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            boton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            boton.setForeground(new java.awt.Color(241, 245, 249)); // Slate 100
            boton.setBorder(new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)));
            boton.setFocusPainted(false);
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);

            // Hover effect for border
            boton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    boton.setBorder(new RoundedBorder(16, new java.awt.Color(59, 130, 246),
                            new java.awt.Insets(12, 12, 12, 12)));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    boton.setBorder(
                            new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)));
                }
            });

            PanelSalas.add(boton);
            boton.addActionListener((ActionEvent e) -> {
                LimpiarTable();
                PanelMesas.removeAll();
                panelMesas(id, cantidad);
                jTabbedPane1.setSelectedIndex(2);
            });
        }
    }

    // crear mesas
    private void panelMesas(int id_sala, int cant) {
        salaActivaId = id_sala;
        salaActivaCant = cant;
        final int cantFinal = (cant < 4) ? 4 : cant;

        // 1. Crear todos los botones primero (sin consultas a BD)
        JButton[] botones = new JButton[cantFinal];
        for (int i = 0; i < cantFinal; i++) {
            int num_mesa = i + 1;
            String etiqueta = (num_mesa > cantFinal - 4) ? "DOMICILIO N\u00b0: " + num_mesa
                    : "MESA N\u00b0: " + num_mesa;
            JButton boton = new JButton(etiqueta, new ImageIcon(getClass().getResource(""))) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);
            boton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            boton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            boton.setBackground(new java.awt.Color(30, 41, 59)); // Slate 800
            boton.setForeground(new java.awt.Color(148, 163, 184)); // Slate 400
            boton.setBorder(new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(10, 10, 10, 10)));
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            boton.setFocusable(false);
            boton.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            boton.setToolTipText("Cargando estado...");
            botones[i] = boton;
            PanelMesas.add(boton);
        }
        botonesActuales = botones;
        PanelMesas.revalidate();
        PanelMesas.repaint();

        // 2. Verificar estado de mesas en hilo de fondo
        new javax.swing.SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() {
                String[][] estados = new String[cantFinal][2];
                for (int i = 0; i < cantFinal; i++) {
                    estados[i] = pedDao.verificarStadoInfo(i + 1, id_sala);
                }
                return estados;
            }

            @Override
            protected void done() {
                try {
                    String[][] infoEstados = get();
                    for (int i = 0; i < cantFinal; i++) {
                        final String[] info = infoEstados[i];
                        final int verificar = Integer.parseInt(info[0]);
                        final String estadoPed = info[1];
                        final int num_mesa = i + 1;
                        final String etiqueta = (num_mesa > cantFinal - 4) ? "DOMICILIO N\u00b0: " + num_mesa
                                : "MESA N\u00b0: " + num_mesa;
                        JButton boton = botones[i];

                        if (verificar > 0) {
                            if ("PREPARADO".equalsIgnoreCase(estadoPed)) {
                                // 🟡 Estado AMARILLO (Pedido preparado / Entregado / Esperando cobro)
                                boton.setBackground(new java.awt.Color(120, 53, 15)); // Amber oscuro
                                boton.setForeground(new java.awt.Color(252, 211, 77)); // Amber claro
                                boton.setBorder(new RoundedBorder(16, new java.awt.Color(245, 158, 11),
                                        new java.awt.Insets(10, 10, 10, 10)));
                                boton.setToolTipText("Preparado / Esperando pago - Click para ver o cobrar");
                            } else {
                                // 🔴 Estado ROJO (Pedido activo en cocina)
                                boton.setBackground(new java.awt.Color(127, 29, 29));
                                boton.setForeground(new java.awt.Color(252, 165, 165));
                                boton.setBorder(new RoundedBorder(16, new java.awt.Color(220, 38, 38),
                                        new java.awt.Insets(10, 10, 10, 10)));
                                boton.setToolTipText("Ocupada en cocina - Click para ver pedido");
                            }

                            // ⏱️ Cronómetro: mostrar tiempo transcurrido desde que se abrió el pedido
                            try {
                                Modelo.Pedido p = pedDao.verPedido(verificar);
                                if (p != null && p.getFecha() != null) {
                                    java.util.Date fechaApertura = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                            .parse(p.getFecha());
                                    long minutos = (System.currentTimeMillis() - fechaApertura.getTime()) / 60000;
                                    String tiempoStr = minutos < 60 ? minutos + " min"
                                            : (minutos / 60) + "h " + (minutos % 60) + "m";
                                    String tagEstado = "PREPARADO".equalsIgnoreCase(estadoPed) ? " 🟡 PREPARADO" : "";
                                    boton.setText("<html><center>" + etiqueta + "<br><font size='2' color='#FCD34D'>⏱ "
                                            + tiempoStr + tagEstado + "</font></center></html>");
                                }
                            } catch (Exception ex) {
                                boton.setText(etiqueta);
                            }
                        } else {
                            // 🟢 Estado VERDE (Mesa libre)
                            boton.setBackground(new java.awt.Color(6, 78, 59));
                            boton.setForeground(new java.awt.Color(52, 211, 153));
                            boton.setBorder(new RoundedBorder(16, new java.awt.Color(5, 150, 105),
                                    new java.awt.Insets(10, 10, 10, 10)));
                            boton.setToolTipText("Libre - Click para tomar pedido");
                        }
                        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                        for (java.awt.event.ActionListener al : boton.getActionListeners()) {
                            boton.removeActionListener(al);
                        }
                        for (java.awt.event.MouseListener ml : boton.getMouseListeners()) {
                            boton.removeMouseListener(ml);
                        }

                        boton.addActionListener((ActionEvent e) -> {
                            if (verificar > 0) {
                                ModalCobrarMesa modalCobro = new ModalCobrarMesa(Sistema.this, Sistema.this, num_mesa, id_sala, etiqueta);
                                modalCobro.setVisible(true);
                            } else {
                                if (id_sala == 3) {
                                    txtTempIdSala.setText("" + id_sala);
                                    txtTempNumMesa.setText("" + num_mesa);
                                    ped.setId_sala(id_sala);
                                    ped.setNum_mesa(num_mesa);
                                    ped.setTotal(0.0);
                                    ped.setUsuario(LabelVendedor.getText());
                                    int id_pedido = pedDao.RegistrarPedido(ped);
                                    if (id_pedido != -1) {
                                        LimpiarTable();
                                        verPedido(id_pedido);
                                        verPedidoDetalle(id_pedido);
                                        btnFinalizar.setEnabled(true);
                                        btnPdfPedido.setEnabled(false);
                                        jTabbedPane1.setSelectedIndex(4);
                                        JOptionPane.showMessageDialog(null,
                                                "Pedido creado para " + etiqueta + " en Sala 3");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Error al crear el pedido");
                                    }
                                } else {
                                    LimpiarTable();
                                    ListarPlatos(tblTemPlatos);
                                    jTabbedPane1.setSelectedIndex(3);
                                    txtTempIdSala.setText("" + id_sala);
                                    txtTempNumMesa.setText("" + num_mesa);
                                }
                            }
                        });
                    }
                    PanelMesas.revalidate();
                    PanelMesas.repaint();
                } catch (Exception e) {
                    System.out.println("Error al cargar estados de mesas: " + e.getMessage());
                }
            }
        }.execute();
    }

    private java.util.List<Integer> idsRondasAcumuladasCobro = new java.util.ArrayList<>();

    public void abrirCobroPedido(int idPedido) {
        idsRondasAcumuladasCobro.clear();
        idsRondasAcumuladasCobro.add(idPedido);
        txtIdPedido.setText("" + idPedido);
        LimpiarTable();
        verPedido(idPedido);
        verPedidoDetalle(idPedido);
        btnFinalizar.setEnabled(true);
        btnPdfPedido.setEnabled(false);
        jTabbedPane1.setSelectedIndex(4);
    }

    public void abrirCobroMesaAcumulado(int numMesa, int idSala) {
        idsRondasAcumuladasCobro.clear();
        java.util.List<Modelo.Pedido> rondas = pedDao.getRondasMesa(numMesa, idSala);
        if (rondas.isEmpty()) return;

        int primerId = rondas.get(0).getId();
        txtIdPedido.setText("" + primerId);

        for (Modelo.Pedido r : rondas) {
            idsRondasAcumuladasCobro.add(r.getId());
        }

        // Cargamos el primer pedido para datos de cabecera
        verPedido(primerId);

        // Cargamos TODOS los detalles acumulados de todas las rondas en tableFinalizar
        java.util.List<Modelo.DetallePedido> detalles = pedDao.getDetallesAcumuladosMesa(numMesa, idSala);
        DefaultTableModel model = (DefaultTableModel) tableFinalizar.getModel();
        model.setRowCount(0);
        for (Modelo.DetallePedido d : detalles) {
            model.addRow(new Object[]{
                d.getId(),
                d.getNombre(),
                d.getCantidad(),
                d.getPrecio(),
                d.getCantidad() * d.getPrecio(),
                d.getComentario() != null ? d.getComentario() : ""
            });
        }
        colorHeader(tableFinalizar);
        TotalPagar(tableFinalizar, totalFinalizar);
        btnFinalizar.setEnabled(true);
        btnPdfPedido.setEnabled(false);
        jTabbedPane1.setSelectedIndex(4);
    }

    public void crearNuevaRondaMesa(int numMesa, int idSala) {
        LimpiarTable();
        ListarPlatos(tblTemPlatos);
        jTabbedPane1.setSelectedIndex(3);
        txtTempIdSala.setText("" + idSala);
        txtTempNumMesa.setText("" + numMesa);
    }

    public void refrescarVistaMesas() {
        if (salaActivaId > 0 && salaActivaCant > 0) {
            PanelMesas.removeAll();
            panelMesas(salaActivaId, salaActivaCant);
        }
    }

    /**
     * Refresca el estado de mesas con UNA sola query a la BD (en lugar de N).
     * Solo actúa si la pestaña de Mesas es visible. El timer se auto-pausa
     * cuando la ventana pierde foco (ver inicializarFooterEstado).
     */
    private void refreshEstadosMesas() {
        if (salaActivaId <= 0 || botonesActuales.length == 0) return;
        if (jTabbedPane1.getSelectedIndex() != 2) return;

        final int idSala = salaActivaId;
        final int cantFinal = botonesActuales.length;
        final javax.swing.JButton[] botonesRef = botonesActuales;

        new javax.swing.SwingWorker<java.util.Map<Integer, Integer>, Void>() {
            @Override
            protected java.util.Map<Integer, Integer> doInBackground() {
                // UNA sola query para toda la sala
                return pedDao.getMesasOcupadas(idSala);
            }

            @Override
            protected void done() {
                try {
                    java.util.Map<Integer, Integer> ocupadas = get();

                    // Comparar snapshot actual vs nuevo: ¿cambió algo?
                    boolean cambio = false;
                    for (int i = 0; i < cantFinal; i++) {
                        int numMesa = i + 1;
                        boolean debeEstarOcupada = ocupadas.containsKey(numMesa);
                        javax.swing.JButton boton = botonesRef[i];
                        if (boton == null) continue;
                        java.awt.Color bg = boton.getBackground();
                        boolean estaOcupada = bg != null && !bg.equals(new java.awt.Color(6, 78, 59));
                        if (estaOcupada != debeEstarOcupada) { cambio = true; break; }
                    }

                    if (cambio) {
                        // Cambio de estado detectado → reconstruir panel
                        PanelMesas.removeAll();
                        panelMesas(idSala, salaActivaCant);
                    }
                } catch (Exception e) { /* ignorar */ }
            }
        }.execute();
    }

    // platos
    private void ListarPlatos(JTable tabla) {
        List<Plato> Listar = plaDao.Listar(txtBuscarPlato.getText());
        modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0); // Limpiar la tabla antes de cargar
        Object[] ob = new Object[3];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getPrecio();
            modelo.addRow(ob);
        }
        colorHeader(tabla);

        // Poblar el Grid de Tarjetas Visuales
        if (gridPanelPlatos != null) {
            gridPanelPlatos.removeAll();
            for (Plato pl : Listar) {
                gridPanelPlatos.add(new TarjetaPlato(pl.getId(), pl.getNombre(), pl.getPrecio(),
                        () -> agregarPlatoDirecto(pl.getId(), pl.getNombre(), pl.getPrecio())));
            }
            gridPanelPlatos.revalidate();
            gridPanelPlatos.repaint();
        }
    }

    // registrar pedido
    private void RegistrarPedido() {
        int id_sala = Integer.parseInt(txtTempIdSala.getText());
        int num_mesa = Integer.parseInt(txtTempNumMesa.getText());
        double monto = Totalpagar;
        ped.setId_sala(id_sala);
        ped.setNum_mesa(num_mesa);
        ped.setTotal(monto);
        ped.setUsuario(LabelVendedor.getText());
        pedDao.RegistrarPedido(ped);
    }

    private void detallePedido() {
        int id = pedDao.IdPedido();
        for (int i = 0; i < tableMenu.getRowCount(); i++) {
            String nombre = tableMenu.getValueAt(i, 1).toString();
            int cant = Integer.parseInt(tableMenu.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(tableMenu.getValueAt(i, 3).toString());
            String comentario = tableMenu.getValueAt(i, 5).toString(); // Obtener el comentario de tableMenu
            detPedido.setNombre(nombre);
            detPedido.setCantidad(cant);
            detPedido.setPrecio(precio);
            detPedido.setComentario(comentario);
            detPedido.setId_pedido(id);
            pedDao.RegistrarDetalle(detPedido);
        }
        LimpiarTable();
        LimpiarPlatos();

    }

    private void actualizarTotalMenu() {
        double total = 0.0;
        for (int i = 0; i < tableMenu.getRowCount(); i++) {
            double totalFila = Double.parseDouble(tableMenu.getValueAt(i, 4).toString());
            total += totalFila;
        }
        totalMenu.setText(String.format("%.2f", total));
    }

    public void verPedidoDetalle(int id_pedido) {
        List<DetallePedido> Listar = pedDao.verPedidoDetalle(id_pedido);
        modelo = (DefaultTableModel) tableFinalizar.getModel();
        modelo.setRowCount(0); // Limpiar tabla antes de recargar
        Object[] ob = new Object[6];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getCantidad();
            ob[3] = Listar.get(i).getPrecio();
            ob[4] = Listar.get(i).getCantidad() * Listar.get(i).getPrecio();
            ob[5] = Listar.get(i).getComentario();
            modelo.addRow(ob);
        }
        colorHeader(tableFinalizar);
        TotalPagar(tableFinalizar, totalFinalizar);
    }

    public void verPedido(int id_pedido) {
        txtSalaFinalizar.setText(ped.getSala());
        // Deshabilitar el listener temporalmente
        ActionListener[] listeners = jComboSalas.getActionListeners();
        for (ActionListener listener : listeners) {
            jComboSalas.removeActionListener(listener);
        }
        // Configurar la sala actual
        jComboSalas.setSelectedItem(ped.getSala());
        // Restaurar los listeners
        for (ActionListener listener : listeners) {
            jComboSalas.addActionListener(listener);
        }
        ped = pedDao.verPedido(id_pedido);
        totalFinalizar.setText("" + ped.getTotal());
        txtFechaHora.setText("" + ped.getFecha());
        txtSalaFinalizar.setText("" + ped.getSala());
        txtSalaFinalizar.setText(ped.getSala());
        jComboSalas.setSelectedItem(ped.getSala());
        txtNumMesaFinalizar.setText("" + ped.getNum_mesa());
        txtIdPedido.setText("" + ped.getId());
    }

    private void actualizarTotalDia() {
        new javax.swing.SwingWorker<double[], Void>() {
            String rangoTexto;

            @Override
            protected double[] doInBackground() {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
                LocalDateTime inicio, fin;
                if (ahora.getHour() < 4) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else if (ahora.getHour() < 16) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else {
                    inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                }
                Timestamp inicioTS = Timestamp.valueOf(inicio);
                Timestamp finTS = Timestamp.valueOf(fin);
                rangoTexto = sdf.format(inicioTS) + " a " + sdf.format(finTS);
                PedidosDao pedidosDao = new PedidosDao();
                return pedidosDao.calcularTotalesDia(inicioTS, finTS, 0);
            }

            @Override
            protected void done() {
                try {
                    double[] totales = get();
                    txtTotalDia.setText(String.format("%.2f", totales[0]));
                    txtTotalDiaTrans.setText(String.format("%.2f", totales[1]));
                    System.out.println("Total EFECTIVO (" + rangoTexto + "): S/ " + String.format("%.2f", totales[0]));
                    System.out
                            .println("Total TRANSACCION (" + rangoTexto + "): S/ " + String.format("%.2f", totales[1]));
                } catch (Exception e) {
                    System.out.println("Error al actualizar totales: " + e.getMessage());
                }
            }
        }.execute();
    }

    private double parseDoubleSafe(String text) {
        if (text == null)
            return 0.0;
        text = text.trim();
        if (text.isEmpty())
            return 0.0;
        try {
            if (text.contains(",") && (text.indexOf(",") > text.indexOf("."))) {
                text = text.replace(".", "").replace(",", ".");
            } else {
                text = text.replace(",", "");
            }
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void cargarPedidosDelDia() {
        // Obtener la fecha y hora actual
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
        LocalDateTime inicio, fin;

        // Determinar el rango del dÃ­a operativo
        if (ahora.getHour() < 16) {
            // Rango: 16:00 del dÃ­a anterior a la hora actual
            inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
            fin = ahora; // Usar la hora actual en lugar de 04:00
        } else {
            // Rango: 16:00 del dÃ­a actual a la hora actual (o hasta 04:00 del dÃ­a
            // siguiente si prefieres)
            inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
            fin = ahora; // Usar la hora actual para incluir pedidos recientes
        }

        // Convertir a Timestamp
        Timestamp fechaInicio = Timestamp.valueOf(inicio);
        Timestamp fechaFin = Timestamp.valueOf(fin);

        // DepuraciÃ³n
        System.out.println("Rango de bÃºsqueda de pedidos: " + fechaInicio + " a " + fechaFin);

        // Resto del cÃ³digo (consultar pedidos y llenar la tabla)
        PedidosDao pedidosDao = new PedidosDao();
        List<Pedido> listaPedidos = pedidosDao.listarPedidosDelDia(fechaInicio, fechaFin);
        System.out.println("Pedidos encontrados: " + listaPedidos.size());

        DefaultTableModel modelo = (DefaultTableModel) TablePedidos.getModel();
        modelo.setRowCount(0);
        for (Pedido ped : listaPedidos) {
            modelo.addRow(new Object[] {
                    ped.getId(),
                    ped.getSala(),
                    ped.getUsuario(),
                    ped.getNum_mesa(),
                    ped.getFecha(),
                    String.format("%.2f", ped.getTotal()),
                    ped.getEstado(),
                    ped.getPago_efectivo(),
                    ped.getPago_transaccion()
            });
            System.out.println("Pedido ID: " + ped.getId() + ", Fecha: " + ped.getFecha());
        }
        // Renderizador con color para la columna Estado en TablePedidos (Índice 6)
        TablePedidos.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String estadoStr = String.valueOf(value);
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                if ("PREPARADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(new java.awt.Color(120, 53, 15)); // Amber oscuro
                    c.setForeground(new java.awt.Color(252, 211, 77)); // Amber claro
                } else if ("FINALIZADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(new java.awt.Color(6, 78, 59)); // Verde oscuro
                    c.setForeground(new java.awt.Color(52, 211, 153)); // Verde claro
                } else if ("ANULADO".equalsIgnoreCase(estadoStr)) {
                    c.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
                    c.setForeground(new java.awt.Color(148, 163, 184)); // Slate 400
                } else {
                    c.setBackground(new java.awt.Color(127, 29, 29)); // Rojo oscuro
                    c.setForeground(new java.awt.Color(252, 165, 165)); // Rojo claro
                }
                return c;
            }
        });

        // Ocultar columnas auxiliares de pago
        TablePedidos.getColumnModel().getColumn(7).setMinWidth(0);
        TablePedidos.getColumnModel().getColumn(7).setMaxWidth(0);
        TablePedidos.getColumnModel().getColumn(7).setWidth(0);
        TablePedidos.getColumnModel().getColumn(8).setMinWidth(0);
        TablePedidos.getColumnModel().getColumn(8).setMaxWidth(0);
        TablePedidos.getColumnModel().getColumn(8).setWidth(0);
    }

    // En Sistema.java
    private void cargarSalasCombo() {
        ActionListener[] listeners = jComboSalas.getActionListeners();
        for (ActionListener listener : listeners) {
            jComboSalas.removeActionListener(listener);
        }
        jComboSalas.removeAllItems();
        SalasDao salaDao = new SalasDao();
        List<Sala> lista = salaDao.Listar();
        jComboSalas.addItem("Seleccionar");
        for (Sala sala : lista) {
            jComboSalas.addItem(sala.getNombre());
        }
        for (ActionListener listener : listeners) {
            jComboSalas.addActionListener(listener);
        }
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {
        if (jTabbedPane1.getSelectedIndex() == 4) { // Ãndice 4 es jPanel25 (Finalizar Pedido)
            cargarSalasCombo();
        }
    }

    private void cargarTablaUsuarios() {
        DefaultTableModel modelo = (DefaultTableModel) TableUsuarios.getModel();
        modelo.setRowCount(0); // Limpiar tabla
        LoginDao loginDao = new LoginDao();
        List<Login> usuarios = loginDao.ListarUsuarios();
        for (Login lg : usuarios) {
            Object[] fila = { lg.getId(), lg.getNombre(), lg.getCorreo(), lg.getRol() };
            modelo.addRow(fila);
        }
    }

    private void cargarTablaPlatos() {
        DefaultTableModel modelo = (DefaultTableModel) tableMenu.getModel();
        modelo.setRowCount(0); // Limpiar tabla
        PlatosDao platosDao = new PlatosDao();
        List<Plato> platos = platosDao.Listar(""); // Lista todos los platos
        System.out.println("NÃºmero de platos cargados: " + platos.size()); // DepuraciÃ³n
        for (Plato pl : platos) {
            Object[] fila = { pl.getId(), pl.getNombre(), pl.getPrecio() };
            modelo.addRow(fila);
        }
    }

    // Agregar plato directamente desde tarjeta del catÃ¡logo
    private void agregarPlatoDirecto(int id, String descripcion, double precio) {
        double total = 1 * precio;
        item = item + 1;
        tmp = (DefaultTableModel) tableMenu.getModel();
        for (int i = 0; i < tableMenu.getRowCount(); i++) {
            if (tableMenu.getValueAt(i, 0).equals(id)) {
                int cantActual = Integer.parseInt(tableMenu.getValueAt(i, 2).toString());
                int nuevoCantidad = cantActual + 1;
                double nuevoSub = precio * nuevoCantidad;
                tmp.setValueAt(nuevoCantidad, i, 2);
                tmp.setValueAt(nuevoSub, i, 4);
                TotalPagar(tableMenu, totalMenu);
                return;
            }
        }
        Object[] O = new Object[6];
        O[0] = id;
        O[1] = descripcion;
        O[2] = 1;
        O[3] = precio;
        O[4] = total;
        O[5] = "";
        tmp.addRow(O);
        tableMenu.setModel(tmp);
        TotalPagar(tableMenu, totalMenu);
    }

    private static Font outfitRegular = null;
    private static Font outfitBold = null;
    private static boolean fontsAttempted = false;

    private static void loadFonts() {
        if (fontsAttempted)
            return;
        fontsAttempted = true;
        try {
            java.io.InputStream regStream = Sistema.class.getResourceAsStream("/Img/Outfit-Regular.ttf");
            if (regStream != null) {
                outfitRegular = Font.createFont(Font.TRUETYPE_FONT, regStream);
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(outfitRegular);
            }
            java.io.InputStream boldStream = Sistema.class.getResourceAsStream("/Img/Outfit-Bold.ttf");
            if (boldStream != null) {
                outfitBold = Font.createFont(Font.TRUETYPE_FONT, boldStream);
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(outfitBold);
            }
        } catch (Exception e) {
            System.out.println("Fuentes Outfit no disponibles, usando fuente del sistema: " + e.getMessage());
        }
    }

    public static Font getFontRegular(float size) {
        loadFonts();
        if (outfitRegular != null) {
            return outfitRegular.deriveFont(Font.PLAIN, size);
        }
        return new Font("Segoe UI", Font.PLAIN, (int) size);
    }

    public static Font getFontBold(float size) {
        loadFonts();
        if (outfitBold != null) {
            return outfitBold.deriveFont(Font.BOLD, size);
        }
        return new Font("Segoe UI", Font.BOLD, (int) size);
    }

    // ==================== DASHBOARD DE VENTAS ====================

    private javax.swing.JPanel crearCardMetrica(final Color accentColor) {
        javax.swing.JPanel card = new javax.swing.JPanel(null) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 150));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 20));
                g2.fillOval(getWidth() - 55, getHeight() - 55, 80, 80);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private void initDashboard() {
        panelDashboard = new javax.swing.JPanel(null);
        panelDashboard.setOpaque(false);

        // --- Tarjeta Efectivo ---
        javax.swing.JPanel cEf = crearCardMetrica(new Color(16, 185, 129));
        cEf.setBounds(0, 0, 320, 100);
        JLabel t1 = new JLabel("\u2B50 VENTAS EFECTIVO");
        t1.setFont(getFontRegular(11f));
        t1.setForeground(new Color(148, 163, 184));
        t1.setBounds(20, 12, 280, 18);
        cEf.add(t1);
        lblEfectivoVal = new JLabel("$0 COP");
        lblEfectivoVal.setFont(getFontBold(22f));
        lblEfectivoVal.setForeground(Color.WHITE);
        lblEfectivoVal.setBounds(20, 34, 280, 28);
        cEf.add(lblEfectivoVal);
        lblEfectivoSub = new JLabel("0% de las ventas");
        lblEfectivoSub.setFont(getFontRegular(11f));
        lblEfectivoSub.setForeground(new Color(148, 163, 184));
        lblEfectivoSub.setBounds(20, 68, 280, 16);
        cEf.add(lblEfectivoSub);
        panelDashboard.add(cEf);

        // --- Tarjeta TransacciÃ³n ---
        javax.swing.JPanel cTr = crearCardMetrica(new Color(59, 130, 246));
        cTr.setBounds(350, 0, 320, 100);
        JLabel t2 = new JLabel("\u2B50 VENTAS TRANSFERENCIA");
        t2.setFont(getFontRegular(11f));
        t2.setForeground(new Color(148, 163, 184));
        t2.setBounds(20, 12, 280, 18);
        cTr.add(t2);
        lblTransVal = new JLabel("$0 COP");
        lblTransVal.setFont(getFontBold(22f));
        lblTransVal.setForeground(Color.WHITE);
        lblTransVal.setBounds(20, 34, 280, 28);
        cTr.add(lblTransVal);
        lblTransSub = new JLabel("0% de las ventas");
        lblTransSub.setFont(getFontRegular(11f));
        lblTransSub.setForeground(new Color(148, 163, 184));
        lblTransSub.setBounds(20, 68, 280, 16);
        cTr.add(lblTransSub);
        panelDashboard.add(cTr);

        // --- Tarjeta Total ---
        javax.swing.JPanel cTo = crearCardMetrica(new Color(245, 158, 11));
        cTo.setBounds(700, 0, 320, 100);
        JLabel t3 = new JLabel("\u2B50 TOTAL VENTAS TURNO");
        t3.setFont(getFontRegular(11f));
        t3.setForeground(new Color(148, 163, 184));
        t3.setBounds(20, 12, 280, 18);
        cTo.add(t3);
        lblTotalVal = new JLabel("$0 COP");
        lblTotalVal.setFont(getFontBold(22f));
        lblTotalVal.setForeground(Color.WHITE);
        lblTotalVal.setBounds(20, 34, 280, 28);
        cTo.add(lblTotalVal);
        JLabel t3s = new JLabel("Acumulado del turno activo");
        t3s.setFont(getFontRegular(11f));
        t3s.setForeground(new Color(148, 163, 184));
        t3s.setBounds(20, 68, 280, 16);
        cTo.add(t3s);
        panelDashboard.add(cTo);

        // --- GrÃ¡ficos ---
        chartVentasHora = new SalesTrendChart();
        chartVentasHora.setBounds(0, 120, 560, 380);
        panelDashboard.add(chartVentasHora);

        chartPlatosMasVendidos = new TopDishesChart();
        chartPlatosMasVendidos.setBounds(580, 120, 440, 380);
        panelDashboard.add(chartPlatosMasVendidos);

        // Agregar dashboard a jPanel6
        jPanel6.add(panelDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 1020, 520));
        panelDashboard.setVisible(false);

        // --- Toggle Buttons ---
        javax.swing.JPanel toggleBar = new javax.swing.JPanel(null);
        toggleBar.setOpaque(false);

        btnToggleDashboard = new JButton("Estadísticas");
        btnToggleDashboard.setFont(getFontBold(12f));
        btnToggleDashboard.setFocusPainted(false);
        btnToggleDashboard.setBorderPainted(false);
        btnToggleDashboard.setContentAreaFilled(false);
        btnToggleDashboard.setOpaque(true);
        btnToggleDashboard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleDashboard.setBounds(0, 0, 155, 30);

        btnToggleHistorial = new JButton("Historial");
        btnToggleHistorial.setFont(getFontBold(12f));
        btnToggleHistorial.setFocusPainted(false);
        btnToggleHistorial.setBorderPainted(false);
        btnToggleHistorial.setContentAreaFilled(false);
        btnToggleHistorial.setOpaque(true);
        btnToggleHistorial.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleHistorial.setBounds(160, 0, 155, 30);

        actualizarEstiloToggle(false);

        btnToggleDashboard.addActionListener(e -> {
            setDashboardVisible(true);
            actualizarEstiloToggle(true);
            actualizarDashboardData();
        });
        btnToggleHistorial.addActionListener(e -> {
            setDashboardVisible(false);
            actualizarEstiloToggle(false);
            ListarPedidos();
        });

        JButton btnExportCSV = new JButton("Exportar CSV");
        btnExportCSV.setFont(getFontBold(11f));
        btnExportCSV.setBackground(new Color(30, 41, 59));
        btnExportCSV.setForeground(new Color(56, 189, 248));
        btnExportCSV.setFocusPainted(false);
        btnExportCSV.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportCSV.setBounds(330, 0, 130, 30);
        btnExportCSV.addActionListener(e -> {
            if (!esAdmin()) {
                JOptionPane.showMessageDialog(this,
                        "Acceso restringido: Solo el Administrador puede exportar reportes en CSV.", "Acceso Denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            exportarHistorialPedidosCSV();
        });

        toggleBar.add(btnToggleDashboard);
        toggleBar.add(btnToggleHistorial);
        toggleBar.add(btnExportCSV);
        jPanel6.add(toggleBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 12, 480, 30));

        // 🏷️ Chips de Filtro Rápido
        javax.swing.JPanel filtroBar = new javax.swing.JPanel(null);
        filtroBar.setOpaque(false);

        String[][] chips = {
                { "Todos", "TODOS" },
                { "Pendientes", "PENDIENTE" },
                { "Preparados", "PREPARADO" },
                { "Pagados", "FINALIZADO" },
                { "Anulados", "ANULADO" }
        };
        int chipX = 0;
        for (String[] chip : chips) {
            JButton btnChip = new JButton(chip[0]);
            btnChip.setFont(getFontBold(11f));
            btnChip.setBackground(new Color(30, 41, 59));
            btnChip.setForeground(new Color(203, 213, 225));
            btnChip.setFocusPainted(false);
            btnChip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChip.setBounds(chipX, 0, 115, 24);
            final String filtroEstado = chip[1];
            btnChip.addActionListener(e -> {
                DefaultTableModel mdl = (DefaultTableModel) TablePedidos.getModel();
                int rows = mdl.getRowCount();
                if (filtroEstado.equals("TODOS")) {
                    ListarPedidos();
                    return;
                }
                new javax.swing.SwingWorker<java.util.List<Pedido>, Void>() {
                    @Override
                    protected java.util.List<Pedido> doInBackground() {
                        java.time.LocalDateTime ahora = java.time.LocalDateTime
                                .now(java.time.ZoneId.of("America/Lima"));
                        java.time.LocalDateTime inicio;
                        if (ahora.getHour() < 16) {
                            inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                        } else {
                            inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
                        }
                        return pedDao.listarPedidosDelDia(
                                java.sql.Timestamp.valueOf(inicio),
                                java.sql.Timestamp.valueOf(ahora));
                    }

                    @Override
                    protected void done() {
                        try {
                            java.util.List<Pedido> todos = get();
                            DefaultTableModel model2 = (DefaultTableModel) TablePedidos.getModel();
                            model2.setRowCount(0);
                            for (Pedido p : todos) {
                                String estado = p.getEstado() != null ? p.getEstado() : "";
                                if (estado.equalsIgnoreCase(filtroEstado)) {
                                    model2.addRow(new Object[] {
                                            p.getId(), p.getSala(), p.getUsuario(), p.getNum_mesa(),
                                            p.getFecha(), String.format("%.2f", p.getTotal()),
                                            p.getEstado(), p.getPago_efectivo(), p.getPago_transaccion()
                                    });
                                }
                            }
                        } catch (Exception ex) {
                        }
                    }
                }.execute();
            });
            filtroBar.add(btnChip);
            chipX += 120;
        }
        filtroBarHistorial = filtroBar;
        jPanel6.add(filtroBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(495, 15, 530, 24));

        // 🔍 Buscador Inteligente en Tiempo Real en Historial
        txtBuscarHistorial = new javax.swing.JTextField();
        UIUtils.estilarCampoTexto(txtBuscarHistorial);
        txtBuscarHistorial.setToolTipText("Escribe mesa, mesero, estado o ID para filtrar al instante...");
        txtBuscarHistorial.setText("Buscar por Mesa, Atendido, Estado o ID...");
        txtBuscarHistorial.setForeground(new Color(148, 163, 184));

        txtBuscarHistorial.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtBuscarHistorial.getText().startsWith("Buscar por")) {
                    txtBuscarHistorial.setText("");
                    txtBuscarHistorial.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtBuscarHistorial.getText().trim().isEmpty()) {
                    txtBuscarHistorial.setForeground(new Color(148, 163, 184));
                    txtBuscarHistorial.setText("Buscar por Mesa, Atendido, Estado o ID...");
                }
            }
        });

        javax.swing.table.TableRowSorter<DefaultTableModel> sorterHistorial = new javax.swing.table.TableRowSorter<>((DefaultTableModel) TablePedidos.getModel());
        TablePedidos.setRowSorter(sorterHistorial);

        txtBuscarHistorial.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtBuscarHistorial.getText().trim();
                if (text.isEmpty() || text.startsWith("Buscar por")) {
                    sorterHistorial.setRowFilter(null);
                } else {
                    sorterHistorial.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
        });

        jPanel6.add(txtBuscarHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 48, 1020, 32));

        // Botón Marcar Preparado (Cocina)
        javax.swing.JButton btnMarcarPreparadoHistorial = UIUtils.crearBoton("🟡 Marcar Preparado", new java.awt.Color(217, 119, 6));
        btnMarcarPreparadoHistorial.setFont(getFontBold(12f));
        btnMarcarPreparadoHistorial.setToolTipText("Marcar pedido como PREPARADO en cocina (Mesa cambia a amarillo)");
        btnMarcarPreparadoHistorial.addActionListener(e -> {
            int row = TablePedidos.getSelectedRow();
            if (row < 0 && txtIdHistorialPedido.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int idPed = row >= 0 ? Integer.parseInt(TablePedidos.getValueAt(row, 0).toString()) : Integer.parseInt(txtIdHistorialPedido.getText());
            if (pedDao.marcarPreparado(idPed)) {
                ToastNotification.exito(this, "¡Pedido #" + idPed + " marcado como PREPARADO en cocina!");
                ListarPedidos();
            }
        });
        jPanel6.add(btnMarcarPreparadoHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 525, 155, 40));

        // Botón Ir a Cobrar
        javax.swing.JButton btnCobrarHistorial = UIUtils.crearBoton("💰 Ir a Cobrar", new java.awt.Color(22, 163, 74));
        btnCobrarHistorial.setFont(getFontBold(12f));
        btnCobrarHistorial.setToolTipText("Abrir pantalla de cobro para el pedido seleccionado");
        btnCobrarHistorial.addActionListener(e -> {
            int row = TablePedidos.getSelectedRow();
            if (row < 0 && txtIdHistorialPedido.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para cobrar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int idPed = row >= 0 ? Integer.parseInt(TablePedidos.getValueAt(row, 0).toString()) : Integer.parseInt(txtIdHistorialPedido.getText());
            abrirCobroPedido(idPed);
        });
        jPanel6.add(btnCobrarHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(465, 525, 145, 40));

        // Botón Reimprimir Ticket
        btnReimprimirTicketHistorial = UIUtils.crearBoton("Reimprimir Ticket", UIUtils.COLOR_ACCENT_BLUE);
        btnReimprimirTicketHistorial.setFont(getFontBold(12f));
        btnReimprimirTicketHistorial.addActionListener(e -> {
            if (txtIdHistorialPedido.getText().isEmpty() && TablePedidos.getSelectedRow() < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para reimprimir el ticket.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int idPed = TablePedidos.getSelectedRow() >= 0 ? Integer.parseInt(TablePedidos.getValueAt(TablePedidos.getSelectedRow(), 0).toString()) : Integer.parseInt(txtIdHistorialPedido.getText());
                pedDao.pdfPedido(idPed);
                ToastNotification.exito(this, "¡Comprobante PDF re-generado para el pedido #" + idPed + "!");
            } catch (Exception ex) {
                ToastNotification.error(this, "Error al generar comprobante PDF.");
            }
        });
        jPanel6.add(btnReimprimirTicketHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 525, 145, 40));

        // 📊 Footer de Estado Global
        inicializarFooterEstado();
    }

    private void actualizarEstiloToggle(boolean dashActivo) {
        Color actBg = new Color(30, 64, 175);
        Color inBg = new Color(30, 41, 59);
        Color actFg = Color.WHITE;
        Color inFg = new Color(148, 163, 184);
        btnToggleDashboard.setBackground(dashActivo ? actBg : inBg);
        btnToggleDashboard.setForeground(dashActivo ? actFg : inFg);
        btnToggleHistorial.setBackground(dashActivo ? inBg : actBg);
        btnToggleHistorial.setForeground(dashActivo ? inFg : actFg);
    }

    private javax.swing.JPanel filtroBarHistorial;

    private void setDashboardVisible(boolean visible) {
        java.awt.Component[] histComps = { jScrollPane5, txtTotalDia, txtTotalDiaTrans, txtPedidosDia,
                jLabel20, jLabel21, jLabel22, btnEliminarPedido, BtnImprimirDia, filtroBarHistorial,
                txtBuscarHistorial, btnReimprimirTicketHistorial };
        for (java.awt.Component c : histComps) {
            if (c != null)
                c.setVisible(!visible);
        }
        if (panelDashboard != null)
            panelDashboard.setVisible(visible);
        if (visible) {
            jLabel16.setText("Estadísticas de Ventas");
        } else {
            jLabel16.setText("Historial de Pedidos del Día");
        }
        jPanel6.repaint();
    }

    private void exportarHistorialPedidosCSV() {
        try {
            java.io.File dir = new java.io.File("reportes");
            if (!dir.exists())
                dir.mkdirs();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            String path = "reportes/reporte_pedidos_" + sdf.format(new Date()) + ".csv";

            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
                pw.println("ID;Sala;Atendido;Mesa;Fecha;Total;Estado;PagoEfectivo;PagoTransaccion");
                DefaultTableModel model = (DefaultTableModel) TablePedidos.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        sb.append(val != null ? val.toString() : "");
                        if (j < model.getColumnCount() - 1)
                            sb.append(";");
                    }
                    pw.println(sb.toString());
                }
            }
            ToastNotification.exito(this, "¡Reporte CSV exportado en " + path + "!");
        } catch (Exception ex) {
            ToastNotification.error(this, "Error al exportar CSV: " + ex.getMessage());
        }
    }

    public void actualizarDashboardData() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar inicio = (java.util.Calendar) now.clone();
        java.util.Calendar fin = (java.util.Calendar) now.clone();
        if (now.get(java.util.Calendar.HOUR_OF_DAY) < 4) {
            inicio.add(java.util.Calendar.DAY_OF_MONTH, -1);
            inicio.set(java.util.Calendar.HOUR_OF_DAY, 16);
            inicio.set(java.util.Calendar.MINUTE, 0);
            inicio.set(java.util.Calendar.SECOND, 0);
            fin.set(java.util.Calendar.HOUR_OF_DAY, 4);
            fin.set(java.util.Calendar.MINUTE, 0);
            fin.set(java.util.Calendar.SECOND, 0);
        } else {
            inicio.set(java.util.Calendar.HOUR_OF_DAY, 16);
            inicio.set(java.util.Calendar.MINUTE, 0);
            inicio.set(java.util.Calendar.SECOND, 0);
            fin.add(java.util.Calendar.DAY_OF_MONTH, 1);
            fin.set(java.util.Calendar.HOUR_OF_DAY, 4);
            fin.set(java.util.Calendar.MINUTE, 0);
            fin.set(java.util.Calendar.SECOND, 0);
        }
        Timestamp fInicio = new Timestamp(inicio.getTimeInMillis());
        Timestamp fFin = new Timestamp(fin.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strInicio = sdf.format(fInicio);
        String strFin = sdf.format(fFin);

        double[] totales = pedDao.calcularTotalesDia(fInicio, fFin, 0);
        double totalEfectivo = totales[0];
        double totalTransaccion = totales[1];
        double totalGeneral = totalEfectivo + totalTransaccion;

        DecimalFormat df = new DecimalFormat("$#,##0");
        lblEfectivoVal.setText(df.format(totalEfectivo) + " COP");
        lblTransVal.setText(df.format(totalTransaccion) + " COP");
        lblTotalVal.setText(df.format(totalGeneral) + " COP");

        double pctE = totalGeneral > 0 ? (totalEfectivo * 100.0 / totalGeneral) : 0;
        double pctT = totalGeneral > 0 ? (totalTransaccion * 100.0 / totalGeneral) : 0;
        lblEfectivoSub.setText(String.format("%.1f%% de las ventas", pctE));
        lblTransSub.setText(String.format("%.1f%% de las ventas", pctT));

        java.util.Map<String, Double> ventasHora = pedDao.obtenerVentasPorHora(strInicio, strFin);
        chartVentasHora.setData(ventasHora);

        java.util.List<Object[]> topPlatos = pedDao.obtenerPlatosMasVendidos(strInicio, strFin);
        chartPlatosMasVendidos.setData(topPlatos);
    }

    // =========================================================================
    // MODULOS DELEGADOS: INVENTARIO Y CAJA
    // =========================================================================

    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnCaja;
    private ModuloInventario moduloInventario;
    private ModuloCaja moduloCaja;
    private ModuloUsuarios moduloUsuarios;
    private ModuloConfig moduloConfig;

    private boolean pantallaEstaBloqueada = false;
    private javax.swing.JPanel topNavbarPOS;

    public void bloquearPantalla() {
        if (pantallaEstaBloqueada)
            return;
        pantallaEstaBloqueada = true;
        String usuario = LabelVendedor != null ? LabelVendedor.getText().trim() : "Usuario";
        FrmLockScreen lockScreen = new FrmLockScreen(this, usuario);
        lockScreen.setVisible(true);
        pantallaEstaBloqueada = false;
    }

    private boolean enPantallaCompleta = false;

    public void togglePantallaCompleta() {
        if (pantallaEstaBloqueada)
            return;
        if (!enPantallaCompleta) {
            setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
            enPantallaCompleta = true;
        } else {
            setExtendedState(javax.swing.JFrame.NORMAL);
            enPantallaCompleta = false;
        }
    }

    private void construirTopNavbarPOS() {
        topNavbarPOS = new javax.swing.JPanel(new java.awt.BorderLayout(15, 0));
        topNavbarPOS.setBackground(UIUtils.COLOR_PANEL_DARK);
        topNavbarPOS.setPreferredSize(new java.awt.Dimension(1280, 50));
        topNavbarPOS.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER_DARK),
                javax.swing.BorderFactory.createEmptyBorder(6, 15, 6, 15)));

        // 1. Izquierda: Logo + Botones de Navegación POS
        javax.swing.JPanel navBtnsPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        navBtnsPanel.setOpaque(false);

        // Logo pequeño
        javax.swing.JLabel lblNavLogo = new javax.swing.JLabel();
        try {
            java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource("/Img/pizzeria.png")).getImage()
                    .getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
            lblNavLogo.setIcon(new javax.swing.ImageIcon(img));
        } catch (Exception e) {
        }
        lblNavLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblNavLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                jTabbedPane1.setSelectedIndex(0);
            }
        });
        navBtnsPanel.add(lblNavLogo);

        // Botón Inicio (ESC)
        javax.swing.JButton btnNavHome = UIUtils.crearBoton("Inicio", new java.awt.Color(30, 41, 59));
        btnNavHome.setFont(getFontBold(12f));
        btnNavHome.setToolTipText("Regresar a la Pantalla Principal (ESC)");
        btnNavHome.addActionListener(e -> jTabbedPane1.setSelectedIndex(0));
        navBtnsPanel.add(btnNavHome);

        // Botón Salas [F1]
        javax.swing.JButton btnNavSalas = UIUtils.crearBoton("Salas [F1]", new java.awt.Color(30, 41, 59));
        btnNavSalas.setFont(getFontBold(12f));
        btnNavSalas.addActionListener(e -> btnSala.doClick());
        navBtnsPanel.add(btnNavSalas);

        // Botón Pedidos
        javax.swing.JButton btnNavPedidos = UIUtils.crearBoton("Pedidos", new java.awt.Color(30, 41, 59));
        btnNavPedidos.setFont(getFontBold(12f));
        btnNavPedidos.addActionListener(e -> {
            ListarPedidos();
            jTabbedPane1.setSelectedIndex(5);
        });
        navBtnsPanel.add(btnNavPedidos);

        // Separador
        javax.swing.JLabel sep1 = new javax.swing.JLabel(" | ");
        sep1.setForeground(UIUtils.COLOR_BORDER_DARK);
        navBtnsPanel.add(sep1);

        // Botón Caja [F2]
        javax.swing.JButton btnNavCaja = UIUtils.crearBoton("Caja [F2]", new java.awt.Color(30, 41, 59));
        btnNavCaja.setFont(getFontBold(12f));
        btnNavCaja.addActionListener(e -> {
            if (moduloCaja != null)
                moduloCaja.abrirComoVentanaModal(this);
        });
        navBtnsPanel.add(btnNavCaja);

        // Botón Inventario [F3]
        javax.swing.JButton btnNavInv = UIUtils.crearBoton("Inventario [F3]", new java.awt.Color(30, 41, 59));
        btnNavInv.setFont(getFontBold(12f));
        btnNavInv.addActionListener(e -> {
            if (!esAdmin()) {
                JOptionPane.showMessageDialog(this,
                        "Acceso restringido: Solo el Administrador puede ingresar al Inventario.", "Acceso Denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (moduloInventario != null)
                moduloInventario.abrirComoVentanaModal(this);
        });
        navBtnsPanel.add(btnNavInv);

        // Botón Admin [F4/F5] (Config Unificada: Empresa + Salas + Carta + Usuarios -
        // Solo Admin)
        javax.swing.JButton btnNavAdmin = UIUtils.crearBoton("Admin [F5]", new java.awt.Color(30, 41, 59));
        btnNavAdmin.setFont(getFontBold(12f));
        btnNavAdmin.addActionListener(e -> {
            if (!esAdmin()) {
                JOptionPane.showMessageDialog(this,
                        "Acceso restringido: Solo el Administrador puede ingresar a la configuración del sistema.",
                        "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (moduloConfig != null)
                moduloConfig.abrirComoVentanaModal();
        });
        navBtnsPanel.add(btnNavAdmin);

        // Separador
        javax.swing.JLabel sep2 = new javax.swing.JLabel(" | ");
        sep2.setForeground(UIUtils.COLOR_BORDER_DARK);
        navBtnsPanel.add(sep2);

        // Botón Recordatorios
        javax.swing.JButton btnNavRecordatorios = UIUtils.crearBoton("Recordatorios", new java.awt.Color(234, 179, 8));
        btnNavRecordatorios.setForeground(new java.awt.Color(15, 23, 42));
        btnNavRecordatorios.setFont(getFontBold(12f));
        btnNavRecordatorios.setToolTipText("Recordatorios POS con Fecha, Hora y Prioridad");
        btnNavRecordatorios.addActionListener(e -> {
            ModuloRecordatorios mr = new ModuloRecordatorios();
            mr.abrirComoVentanaModal(this);
        });
        navBtnsPanel.add(btnNavRecordatorios);

        // Botón Tareas Empleados
        javax.swing.JButton btnNavTareas = UIUtils.crearBoton("Tareas", new java.awt.Color(99, 102, 241));
        btnNavTareas.setForeground(java.awt.Color.WHITE);
        btnNavTareas.setFont(getFontBold(12f));
        btnNavTareas.setToolTipText("Asignación de Tareas y Checklists para Empleados");
        btnNavTareas.addActionListener(e -> {
            ModuloTareas mt = new ModuloTareas();
            mt.abrirComoVentanaModal(this);
        });
        navBtnsPanel.add(btnNavTareas);

        // Botón Sonidos POS (Exclusivo Administrador)
        javax.swing.JButton btnNavSonido = UIUtils.crearBoton("Sonidos", new java.awt.Color(30, 41, 59));
        btnNavSonido.setFont(getFontBold(12f));
        btnNavSonido.setToolTipText("Configurar Sonidos de Cobro, Clics y Voz Sintetizada (Solo Admin)");
        btnNavSonido.addActionListener(e -> {
            if (!esAdmin()) {
                JOptionPane.showMessageDialog(this,
                        "Acceso restringido: Solo el Administrador puede modificar los sonidos del POS.",
                        "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SonidoPOS.abrirModalConfigSonido(this);
        });
        navBtnsPanel.add(btnNavSonido);

        // 2. Derecha: Operador, Reloj, Cambiar Usuario y Salir
        javax.swing.JPanel rightPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        javax.swing.JLabel lblNavUser = new javax.swing.JLabel(
                (LabelVendedor != null ? LabelVendedor.getText().trim() : "Admin"));
        lblNavUser.setFont(getFontBold(12f));
        lblNavUser.setForeground(new java.awt.Color(56, 189, 248));

        javax.swing.JLabel lblNavClock = new javax.swing.JLabel();
        lblNavClock.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 13));
        lblNavClock.setForeground(java.awt.Color.WHITE);
        new javax.swing.Timer(1000, e -> {
            lblNavClock.setText(new java.text.SimpleDateFormat("hh:mm:ss a", new java.util.Locale("es", "ES"))
                    .format(new java.util.Date()));
        }).start();
        lblNavClock.setText(new java.text.SimpleDateFormat("hh:mm:ss a", new java.util.Locale("es", "ES"))
                .format(new java.util.Date()));

        // Botón Cambiar Usuario / Cerrar Sesión
        javax.swing.JButton btnCambiarCuenta = UIUtils.crearBoton("Cambiar Usuario", new java.awt.Color(30, 41, 59));
        btnCambiarCuenta.setFont(getFontBold(11f));
        btnCambiarCuenta.setToolTipText("Cerrar sesión actual e ingresar con otra cuenta");
        btnCambiarCuenta.addActionListener(e -> cerrarSesionYCambiarUsuario());

        // Botón Salir del Sistema
        javax.swing.JButton btnSalirApp = UIUtils.crearBoton("Salir", new java.awt.Color(185, 28, 28));
        btnSalirApp.setFont(getFontBold(11f));
        btnSalirApp.setToolTipText("Cerrar completamente la aplicación POS");
        btnSalirApp.addActionListener(e -> salirDelSistema());

        rightPanel.add(lblNavUser);
        rightPanel.add(lblNavClock);
        rightPanel.add(btnCambiarCuenta);
        rightPanel.add(btnSalirApp);

        topNavbarPOS.add(navBtnsPanel, java.awt.BorderLayout.WEST);
        topNavbarPOS.add(rightPanel, java.awt.BorderLayout.EAST);
    }

    private void initModuloInventario() {
        moduloInventario = new ModuloInventario(this, jTabbedPane1, btnInventario, LabelVendedor, () -> {
            LimpiarTable();
            LimpiarTableMenu();
            LimpiarPlatos();
        });
        moduloInventario.inicializar();
        if (btnInventario != null) {
            btnInventario.addActionListener(e -> {
                if (moduloInventario != null)
                    moduloInventario.abrirComoVentanaModal(this);
            });
        }
    }

    private void initModuloCaja() {
        moduloCaja = new ModuloCaja(this, jTabbedPane1, btnCaja, LabelVendedor, () -> {
            LimpiarTable();
            LimpiarTableMenu();
            LimpiarPlatos();
        });
        moduloCaja.inicializar();
        if (btnCaja != null) {
            btnCaja.addActionListener(e -> {
                if (moduloCaja != null)
                    moduloCaja.abrirComoVentanaModal(this);
            });
        }
    }

    private void inicializarLandingPanel() {
        if (jPanel9 == null)
            return;

        jPanel9.removeAll();
        jPanel9.setLayout(new java.awt.BorderLayout(20, 20));
        jPanel9.setBackground(UIUtils.COLOR_BG_DARK);
        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 25, 25));

        // 1. Hero Header Banner
        javax.swing.JPanel heroHeader = new javax.swing.JPanel(new java.awt.BorderLayout(15, 0));
        heroHeader.setBackground(UIUtils.COLOR_PANEL_DARK);
        heroHeader.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1, true),
                javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Left Branding (Logo + Titles)
        javax.swing.JPanel brandPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        brandPanel.setOpaque(false);

        javax.swing.JLabel lblLogoHero = new javax.swing.JLabel();
        try {
            java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource("/Img/pizzeria.png")).getImage()
                    .getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH);
            lblLogoHero.setIcon(new javax.swing.ImageIcon(img));
        } catch (Exception e) {
        }

        javax.swing.JPanel titleBox = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        javax.swing.JLabel lblMainTitle = new javax.swing.JLabel("RESTAURANTE Y PIZZERÍA COMUNEROS");
        lblMainTitle.setFont(getFontBold(20f));
        lblMainTitle.setForeground(java.awt.Color.WHITE);

        javax.swing.JLabel lblSubTitle = new javax.swing.JLabel("Sistema POS & Gestión — Puente Nacional");
        lblSubTitle.setFont(getFontRegular(13f));
        lblSubTitle.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBox.add(lblMainTitle);
        titleBox.add(lblSubTitle);

        brandPanel.add(lblLogoHero);
        brandPanel.add(titleBox);

        // Right Info Box (User Badge + Reloj)
        javax.swing.JPanel rightText = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        rightText.setOpaque(false);

        javax.swing.JLabel lblUserBadge = new javax.swing.JLabel(
                "Operador: " + (LabelVendedor != null ? LabelVendedor.getText().trim() : "Usuario"),
                javax.swing.SwingConstants.RIGHT);
        lblUserBadge.setFont(getFontBold(13f));
        lblUserBadge.setForeground(new java.awt.Color(56, 189, 248)); // Sky blue

        javax.swing.JLabel lblClock = new javax.swing.JLabel("", javax.swing.SwingConstants.RIGHT);
        lblClock.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 14));
        lblClock.setForeground(java.awt.Color.WHITE);

        // Reloj en vivo
        new javax.swing.Timer(1000, e -> {
            lblClock.setText(new java.text.SimpleDateFormat("EEEE, dd MMMM yyyy  |  hh:mm:ss a",
                    new java.util.Locale("es", "ES")).format(new java.util.Date()));
        }).start();
        lblClock.setText(
                new java.text.SimpleDateFormat("EEEE, dd MMMM yyyy  |  hh:mm:ss a", new java.util.Locale("es", "ES"))
                        .format(new java.util.Date()));

        rightText.add(lblUserBadge);
        rightText.add(lblClock);

        heroHeader.add(brandPanel, java.awt.BorderLayout.WEST);
        heroHeader.add(rightText, java.awt.BorderLayout.EAST);

        // 2. Rejilla de Hero Cards (Landing Layout)
        javax.swing.JPanel cardsGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 3, 20, 20));
        cardsGrid.setOpaque(false);

        // Card 1: Mesas (operación directa)
        cardsGrid.add(crearHeroCard("/Img/sala.png", "Mesas & Comandas",
                "Mapa visual de mesas, atención a comensales y control de ocupación en vivo.", "F1",
                () -> btnSala.doClick()));

        // Card 2: Pedidos Activos
        cardsGrid.add(crearHeroCard("/Img/pedidos.png", "Pedidos & Historial",
                "Listado general de comandas del turno, entregas y estados.", null, () -> {
                    ListarPedidos();
                    jTabbedPane1.setSelectedIndex(5);
                }));

        // Card 3: Caja & Arqueo
        cardsGrid.add(crearHeroCard("/Img/money.png", "Caja & Arqueo",
                "Control de apertura, movimientos de efectivo, registro de gastos y cierre.", "F2", () -> {
                    if (moduloCaja != null)
                        moduloCaja.abrirComoVentanaModal(Sistema.this);
                }));

        // Card 4: Inventario
        cardsGrid.add(crearHeroCard("/Img/inventario.png", "Inventario & Stock",
                "Control de existencias, kardex de insumos, entradas y mermas.", "F3", () -> {
                    if (!esAdmin()) {
                        JOptionPane.showMessageDialog(Sistema.this,
                                "Acceso restringido: Solo el Administrador puede ingresar al Inventario.",
                                "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (moduloInventario != null)
                        moduloInventario.abrirComoVentanaModal(Sistema.this);
                }));

        // Card 5: Administración General (Config F5 = Empresa + Salas + Carta +
        // Usuarios)
        cardsGrid.add(crearHeroCard("/Img/config.png", "Administración",
                "Datos del restaurante, salas, carta de platos, usuarios y roles del sistema.", "F5", () -> {
                    if (!esAdmin()) {
                        JOptionPane.showMessageDialog(Sistema.this,
                                "Acceso restringido: Solo el Administrador puede ingresar al módulo de Administración.",
                                "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (moduloConfig != null)
                        moduloConfig.abrirComoVentanaModal();
                }));

        jPanel9.add(heroHeader, java.awt.BorderLayout.NORTH);
        jPanel9.add(cardsGrid, java.awt.BorderLayout.CENTER);

        jPanel9.revalidate();
        jPanel9.repaint();
    }

    private javax.swing.JPanel crearHeroCard(String iconPath, String titulo, String descripcion, String badgeTecla,
            Runnable onClick) {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        card.setBackground(UIUtils.COLOR_PANEL_DARK);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1, true),
                javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Header de la Card (Icono + Título + Badge Tecla)
        javax.swing.JPanel topPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        topPanel.setOpaque(false);

        javax.swing.JLabel lblIcon = new javax.swing.JLabel();
        try {
            java.awt.Image img = new javax.swing.ImageIcon(getClass().getResource(iconPath)).getImage()
                    .getScaledInstance(36, 36, java.awt.Image.SCALE_SMOOTH);
            lblIcon.setIcon(new javax.swing.ImageIcon(img));
        } catch (Exception e) {
        }

        javax.swing.JLabel lblTitle = new javax.swing.JLabel(titulo);
        lblTitle.setFont(getFontBold(16f));
        lblTitle.setForeground(java.awt.Color.WHITE);

        topPanel.add(lblIcon, java.awt.BorderLayout.WEST);
        topPanel.add(lblTitle, java.awt.BorderLayout.CENTER);

        if (badgeTecla != null && !badgeTecla.isEmpty()) {
            javax.swing.JLabel lblBadge = new javax.swing.JLabel(" " + badgeTecla + " ");
            lblBadge.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 12));
            lblBadge.setForeground(new java.awt.Color(56, 189, 248));
            lblBadge.setBackground(new java.awt.Color(15, 23, 42));
            lblBadge.setOpaque(true);
            lblBadge.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(56, 189, 248, 100), 1));
            topPanel.add(lblBadge, java.awt.BorderLayout.EAST);
        }

        javax.swing.JLabel lblDesc = new javax.swing.JLabel(
                "<html><body style='width:180px;'>" + descripcion + "</body></html>");
        lblDesc.setFont(getFontRegular(12f));
        lblDesc.setForeground(UIUtils.COLOR_TEXT_MUTED);

        card.add(topPanel, java.awt.BorderLayout.NORTH);
        card.add(lblDesc, java.awt.BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new java.awt.Color(40, 53, 76));
                card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(56, 189, 248), 1, true),
                        javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UIUtils.COLOR_PANEL_DARK);
                card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_DARK, 1, true),
                        javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onClick != null)
                    onClick.run();
            }
        });

        return card;
    }

    private void initPanelComandaVisual() {
        if (jPanel23 == null)
            return;

        jPanel23.removeAll();
        jPanel23.setLayout(new java.awt.BorderLayout(10, 10));
        jPanel23.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        jPanel23.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // --- 1. BARRA SUPERIOR DE ACCIONES RÁPIDAS POS ---
        javax.swing.JPanel barraPOS = new javax.swing.JPanel(new java.awt.BorderLayout(10, 5));
        barraPOS.setOpaque(false);

        // 1A. Filtro por Categorías (Izquierda)
        javax.swing.JPanel pCats = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));
        pCats.setOpaque(false);
        javax.swing.JLabel lblCat = new javax.swing.JLabel("Categorías: ");
        lblCat.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblCat.setForeground(new java.awt.Color(148, 163, 184));
        pCats.add(lblCat);

        String[] categorias = { "Todos", "Pizzas", "Bebidas", "Entradas", "Postres" };
        for (String cat : categorias) {
            javax.swing.JButton bCat = new javax.swing.JButton(cat);
            bCat.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            bCat.setBackground(new java.awt.Color(30, 41, 59));
            bCat.setForeground(new java.awt.Color(56, 189, 248)); // Sky blue
            bCat.setFocusPainted(false);
            bCat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            bCat.addActionListener(e -> {
                if (cat.equals("Todos")) {
                    txtBuscarPlato.setText("");
                } else {
                    txtBuscarPlato.setText(cat);
                }
                LimpiarTable();
                ListarPlatos(tblTemPlatos);
            });
            pCats.add(bCat);
        }

        // 1B. Botones de Cantidad (+1, -1, Vaciar) (Derecha)
        javax.swing.JPanel pCant = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 2));
        pCant.setOpaque(false);

        javax.swing.JButton bMas = new javax.swing.JButton(" +1 Cantidad ");
        bMas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        bMas.setBackground(new java.awt.Color(16, 185, 129)); // Emerald Green
        bMas.setForeground(java.awt.Color.WHITE);
        bMas.setFocusPainted(false);
        bMas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bMas.addActionListener(e -> {
            int row = tableMenu.getSelectedRow();
            if (row != -1) {
                try {
                    int cant = Integer.parseInt(tableMenu.getValueAt(row, 2).toString());
                    double precio = Double.parseDouble(tableMenu.getValueAt(row, 3).toString());
                    int nCant = cant + 1;
                    tableMenu.setValueAt(nCant, row, 2);
                    tableMenu.setValueAt(precio * nCant, row, 4);
                    TotalPagar(tableMenu, totalMenu);
                } catch (Exception ex) {
                }
            } else {
                ToastNotification.advertencia(this, "Seleccione un producto en la tabla de comanda.");
            }
        });

        javax.swing.JButton bMenos = new javax.swing.JButton(" -1 Cantidad ");
        bMenos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        bMenos.setBackground(new java.awt.Color(245, 158, 11)); // Amber
        bMenos.setForeground(java.awt.Color.WHITE);
        bMenos.setFocusPainted(false);
        bMenos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bMenos.addActionListener(e -> {
            int row = tableMenu.getSelectedRow();
            if (row != -1) {
                try {
                    int cant = Integer.parseInt(tableMenu.getValueAt(row, 2).toString());
                    double precio = Double.parseDouble(tableMenu.getValueAt(row, 3).toString());
                    if (cant > 1) {
                        int nCant = cant - 1;
                        tableMenu.setValueAt(nCant, row, 2);
                        tableMenu.setValueAt(precio * nCant, row, 4);
                    } else {
                        ((javax.swing.table.DefaultTableModel) tableMenu.getModel()).removeRow(row);
                    }
                    TotalPagar(tableMenu, totalMenu);
                } catch (Exception ex) {
                }
            } else {
                ToastNotification.advertencia(this, "Seleccione un producto en la tabla de comanda.");
            }
        });

        javax.swing.JButton bVaciar = new javax.swing.JButton(" Vaciar Comanda ");
        bVaciar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        bVaciar.setBackground(new java.awt.Color(220, 38, 38)); // Red
        bVaciar.setForeground(java.awt.Color.WHITE);
        bVaciar.setFocusPainted(false);
        bVaciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bVaciar.addActionListener(e -> {
            LimpiarTableMenu();
            TotalPagar(tableMenu, totalMenu);
        });

        pCant.add(bMas);
        pCant.add(bMenos);
        pCant.add(bVaciar);

        barraPOS.add(pCats, java.awt.BorderLayout.WEST);
        barraPOS.add(pCant, java.awt.BorderLayout.EAST);

        // 1C. Chips de Notas Rápidas
        javax.swing.JPanel pChips = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 2));
        pChips.setOpaque(false);
        javax.swing.JLabel lblObs = new javax.swing.JLabel("Notas Rápidas: ");
        lblObs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblObs.setForeground(new java.awt.Color(148, 163, 184));
        pChips.add(lblObs);

        String[] observaciones = { "Para llevar", "Sin cebolla", "Sin hielo", "Bien cocido", "Salsa aparte",
                "Extra queso" };
        for (String obs : observaciones) {
            javax.swing.JButton bObs = new javax.swing.JButton("+ " + obs);
            bObs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
            bObs.setBackground(new java.awt.Color(51, 65, 85));
            bObs.setForeground(java.awt.Color.WHITE);
            bObs.setFocusPainted(false);
            bObs.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            bObs.addActionListener(e -> {
                String actual = txtComentario.getText().trim();
                if (actual.isEmpty()) {
                    txtComentario.setText(obs);
                } else if (!actual.contains(obs)) {
                    txtComentario.setText(actual + ", " + obs);
                }
            });
            pChips.add(bObs);
        }

        javax.swing.JPanel contenedorHeader = new javax.swing.JPanel(new java.awt.BorderLayout(0, 5));
        contenedorHeader.setOpaque(false);
        contenedorHeader.add(barraPOS, java.awt.BorderLayout.NORTH);
        contenedorHeader.add(pChips, java.awt.BorderLayout.SOUTH);

        jPanel23.add(contenedorHeader, java.awt.BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL (TABLA COMANDA + CATÁLOGO PLATOS) ---
        javax.swing.JPanel panelCentro = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 15, 0));
        panelCentro.setOpaque(false);

        // Lado Izquierdo: Comanda Actual + Comentarios + Total
        javax.swing.JPanel pIzquierda = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        pIzquierda.setOpaque(false);
        pIzquierda.add(jScrollPane11, java.awt.BorderLayout.CENTER);

        // Pie Izquierdo (Comentarios + Botón Realizar Pedido)
        javax.swing.JPanel pPieComanda = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        pPieComanda.setOpaque(false);

        javax.swing.JPanel pBoxObs = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        pBoxObs.setOpaque(false);
        pBoxObs.add(jLabel6, java.awt.BorderLayout.WEST);
        pBoxObs.add(jScrollPane12, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel pBoxBtnsObs = new javax.swing.JPanel(new java.awt.GridLayout(2, 1, 0, 5));
        pBoxBtnsObs.setOpaque(false);
        pBoxBtnsObs.add(jButton2);
        pBoxBtnsObs.add(btnEliminarTempPlato);
        pBoxObs.add(pBoxBtnsObs, java.awt.BorderLayout.EAST);

        javax.swing.JPanel pTotal = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 5));
        pTotal.setOpaque(false);
        pTotal.add(jLabel11);
        pTotal.add(totalMenu);

        btnGenerarPedido.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        btnGenerarPedido.setBackground(new java.awt.Color(16, 185, 129));
        btnGenerarPedido.setForeground(java.awt.Color.WHITE);
        pTotal.add(btnGenerarPedido);

        pPieComanda.add(pBoxObs, java.awt.BorderLayout.NORTH);
        pPieComanda.add(pTotal, java.awt.BorderLayout.SOUTH);
        pIzquierda.add(pPieComanda, java.awt.BorderLayout.SOUTH);

        panelCentro.add(pIzquierda);
        panelCentro.add(jPanel24); // Catálogo a la derecha

        jPanel23.add(panelCentro, java.awt.BorderLayout.CENTER);

        jPanel23.revalidate();
        jPanel23.repaint();
    }

    private void inicializarFooterEstado() {
        javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 4));
        footer.setBackground(new java.awt.Color(15, 23, 42));
        footer.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(51, 65, 85)));
        footer.setPreferredSize(new java.awt.Dimension(getWidth(), 32));

        java.awt.Font fntFooter = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12);

        // Ventas Hoy
        final javax.swing.JLabel lblFooterVentas = new javax.swing.JLabel("Ventas Hoy: $---");
        lblFooterVentas.setFont(fntFooter);
        lblFooterVentas.setForeground(new java.awt.Color(52, 211, 153));

        // Mesas Ocupadas
        final javax.swing.JLabel lblFooterMesas = new javax.swing.JLabel("Mesas: -/-");
        lblFooterMesas.setFont(fntFooter);
        lblFooterMesas.setForeground(new java.awt.Color(251, 191, 36));

        // Stock Bajo
        final javax.swing.JLabel lblFooterStock = new javax.swing.JLabel("Stock Bajo: -");
        lblFooterStock.setFont(fntFooter);
        lblFooterStock.setForeground(new java.awt.Color(248, 113, 113));

        // Usuario
        javax.swing.JLabel lblFooterUser = new javax.swing.JLabel(
                (LabelVendedor != null ? LabelVendedor.getText().trim() : "Usuario"));
        lblFooterUser.setFont(fntFooter);
        lblFooterUser.setForeground(new java.awt.Color(148, 163, 184));

        footer.add(lblFooterVentas);
        footer.add(new javax.swing.JLabel(" | ") {
            {
                setForeground(new java.awt.Color(51, 65, 85));
            }
        });
        footer.add(lblFooterMesas);
        footer.add(new javax.swing.JLabel(" | ") {
            {
                setForeground(new java.awt.Color(51, 65, 85));
            }
        });
        footer.add(lblFooterStock);
        footer.add(new javax.swing.JLabel(" | ") {
            {
                setForeground(new java.awt.Color(51, 65, 85));
            }
        });
        footer.add(lblFooterUser);

        getContentPane().add(footer, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 710, 1290, 32));

        // Actualizar footer cada 30 segundos
        new javax.swing.Timer(30000, e -> actualizarFooter(lblFooterVentas, lblFooterMesas, lblFooterStock)) {
            {
                setInitialDelay(1000);
                start();
            }
        };
        actualizarFooter(lblFooterVentas, lblFooterMesas, lblFooterStock);

        // ── Auto-refresh mesas cada 8s (detecta pedidos del celular) ──────────
        // UNA sola query por tick. Se pausa automáticamente si la ventana pierde foco.
        timerAutoRefreshMesas = new javax.swing.Timer(8000, e -> refreshEstadosMesas());
        timerAutoRefreshMesas.setInitialDelay(8000);
        timerAutoRefreshMesas.start();

        // Pausar timer cuando la ventana no tiene foco (minimizada / otra app)
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (timerAutoRefreshMesas != null) timerAutoRefreshMesas.restart();
            }
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                if (timerAutoRefreshMesas != null) timerAutoRefreshMesas.stop();
            }
        });
    }

    private void actualizarFooter(javax.swing.JLabel lblVentas, javax.swing.JLabel lblMesas,
            javax.swing.JLabel lblStock) {
        new javax.swing.SwingWorker<double[], Void>() {
            @Override
            protected double[] doInBackground() {
                try {
                    java.time.LocalDateTime ahora = java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
                    java.time.LocalDateTime inicio = ahora.getHour() < 16
                            ? ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0)
                            : ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
                    double[] totales = pedDao.calcularTotalesDia(
                            java.sql.Timestamp.valueOf(inicio), java.sql.Timestamp.valueOf(ahora), 0);
                    double total = totales[0] + totales[1];

                    // Mesas ocupadas: contar salas
                    java.util.List<Modelo.Sala> salas = slDao.Listar();
                    int totalMesas = 0, ocupadas = 0;
                    for (Modelo.Sala s : salas) {
                        totalMesas += s.getMesas();
                    }
                    // Stock bajo
                    Modelo.InventarioDao invDao = new Modelo.InventarioDao();
                    int stockBajo = invDao.obtenerProductosStockBajo().size();
                    return new double[] { total, ocupadas, totalMesas, stockBajo };
                } catch (Exception ex) {
                    return new double[] { 0, 0, 0, 0 };
                }
            }

            @Override
            protected void done() {
                try {
                    double[] data = get();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("$#,##0");
                    lblVentas.setText("Ventas Hoy: " + df.format(data[0]));
                    lblMesas.setText("Mesas: " + (int) data[1] + "/" + (int) data[2]);
                    lblStock.setText("Stock Bajo: " + (int) data[3]);
                    lblStock.setForeground(data[3] > 0
                            ? new java.awt.Color(248, 113, 113)
                            : new java.awt.Color(52, 211, 153));
                } catch (Exception ex) {
                }
            }
        }.execute();
    }

    public boolean esAdmin() {
        if (userRol == null)
            return true;
        String text = userRol.trim().toLowerCase();
        return text.equals("administrador") || text.equals("admin");
    }

    private void cerrarSesionYCambiarUsuario() {
        String nombreUser = LabelVendedor != null ? LabelVendedor.getText().trim() : "usuario";
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar la sesión de \"" + nombreUser + "\" e ingresar con otra cuenta?",
                "Cambiar Usuario", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            this.dispose();
            FrmLogin loginWin = new FrmLogin();
            loginWin.setVisible(true);
        }
    }

    private void salirDelSistema() {
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas salir del sistema POS?",
                "Salir del Sistema", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
