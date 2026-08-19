package Vista;

import Modelo.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Pantalla de Cobro y Facturación Táctil POS moderna y estilizada.
 * - Sin emojis ni caracteres no soportados en botones (cero cajas vacías)
 * - Tarjetas KPI con renderizado anti-solapamiento (antialiasing y base sólida)
 * - Métodos de pago rápidos: Efectivo, Nequi, Daviplata, Tarjeta, Transferencia, Pago Mixto
 * - Teclado numérico táctil (Numpad) con botón de Pago Exacto y Borrar (C)
 * - Vueltas / Cambio calculados en tiempo real
 */
public class ModalCobroPOS extends JDialog {

    private final Window parentWindow;
    private final Sistema sistemaInstance;
    private final int idPedido;
    private final List<Integer> idsRondasAcumuladas;
    private final double totalConsumo;
    private final String etiquetaMesa;
    private final String nombreSala;
    private final JTable tableFinalizarRef;

    public enum MetodoPago {
        EFECTIVO, NEQUI, DAVIPLATA, TARJETA, TRANSFERENCIA, MIXTO
    }
    private MetodoPago metodoActual = MetodoPago.EFECTIVO;

    // Componentes dinámicos
    private JLabel lblTotalDisplay;
    private JLabel lblRecibidoDisplay;
    private JLabel lblCambioDisplay;
    private JLabel lblCambioTitulo;
    private JPanel cardCambio;
    private Color cambioColorActual = new Color(16, 185, 129);

    private JTextField txtMontoEfectivo;
    private JTextField txtMontoDigital;
    private JTextField txtNombreCliente;
    private JTextField txtDocumentoCliente;

    private JPanel pnlCamposPago;
    private JTextField campoActivoNumpad;

    private JButton btnMetodoEfectivo;
    private JButton btnMetodoNequi;
    private JButton btnMetodoDaviplata;
    private JButton btnMetodoTarjeta;
    private JButton btnMetodoTransferencia;
    private JButton btnMetodoMixto;

    private final DecimalFormat dfCop = new DecimalFormat("$ #,##0 COP", new DecimalFormatSymbols(new Locale("es", "CO")));

    public ModalCobroPOS(Window parent, Sistema sistema, int idPedido, List<Integer> idsRondas, double total, String mesa, String sala, JTable tableRef) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.parentWindow = parent;
        this.sistemaInstance = sistema;
        this.idPedido = idPedido;
        this.idsRondasAcumuladas = idsRondas;
        this.totalConsumo = total;
        this.etiquetaMesa = (mesa != null && !mesa.isEmpty()) ? mesa : "Mesa";
        this.nombreSala = (sala != null && !sala.isEmpty()) ? sala : "Salón";
        this.tableFinalizarRef = tableRef;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setResizable(false);

        construirInterfaz();
        seleccionarMetodo(MetodoPago.EFECTIVO);
    }

    private void construirInterfaz() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo Dark Slate (#0f172a)
                g2.setColor(new Color(15, 23, 42));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));

                // Borde suave
                g2.setColor(new Color(30, 41, 59));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 21, 21));

                // Acento luminoso esmeralda
                g2.setColor(new Color(16, 185, 129, 180));
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawRoundRect(30, 1, Math.max(30, getWidth() - 60), 3, 2, 2);

                g2.dispose();
            }
        };
        rootPanel.setOpaque(false);
        rootPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. ENCABEZADO
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel pnlInfoMesa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlInfoMesa.setOpaque(false);
        
        JLabel badgeMesa = new JLabel("  " + etiquetaMesa + " • " + nombreSala + " (Pedido #" + idPedido + ")  ");
        badgeMesa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badgeMesa.setForeground(new Color(56, 189, 248)); // Cyan
        badgeMesa.setOpaque(true);
        badgeMesa.setBackground(new Color(15, 42, 66));
        badgeMesa.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248, 120), 1, true));
        pnlInfoMesa.add(badgeMesa);

        JLabel lblTitulo = new JLabel("FACTURACIÓN Y COBRO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(248, 250, 252));
        pnlInfoMesa.add(lblTitulo);

        JButton btnCerrar = new JButton("X");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCerrar.setForeground(new Color(148, 163, 184));
        btnCerrar.setBackground(new Color(0, 0, 0, 0));
        btnCerrar.setBorder(null);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        header.add(pnlInfoMesa, BorderLayout.WEST);
        header.add(btnCerrar, BorderLayout.EAST);

        // 2. BARRA DE TOTALES (3 Tarjetas KPI sin solapamiento)
        JPanel pnlKpis = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlKpis.setOpaque(false);

        // Card Total
        pnlKpis.add(crearCardKpi("TOTAL A PAGAR", dfCop.format(totalConsumo), new Color(16, 185, 129)));

        // Card Recibido
        lblRecibidoDisplay = new JLabel("$ 0 COP");
        pnlKpis.add(crearCardKpiDinamica("MONTO RECIBIDO", lblRecibidoDisplay, new Color(56, 189, 248)));

        // Card Vueltas / Cambio
        lblCambioDisplay = new JLabel("$ 0 COP");
        lblCambioTitulo = new JLabel("VUELTAS / CAMBIO");
        cardCambio = crearCardCambio(lblCambioTitulo, lblCambioDisplay);
        pnlKpis.add(cardCambio);

        // 3. CUERPO CENTRAL (Izquierda: Métodos + Montos + Cliente | Derecha: Numpad)
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);

        // ── Columna Izquierda ──
        JPanel colIzquierda = new JPanel();
        colIzquierda.setLayout(new BoxLayout(colIzquierda, BoxLayout.Y_AXIS));
        colIzquierda.setOpaque(false);
        colIzquierda.setPreferredSize(new Dimension(410, 360));

        JLabel lblSecMetodos = new JLabel("MÉTODO DE PAGO:");
        lblSecMetodos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSecMetodos.setForeground(new Color(148, 163, 184));
        lblSecMetodos.setAlignmentX(Component.LEFT_ALIGNMENT);
        colIzquierda.add(lblSecMetodos);
        colIzquierda.add(Box.createRigidArea(new Dimension(0, 6)));

        // Grid Métodos de Pago
        JPanel gridMetodos = new JPanel(new GridLayout(2, 3, 8, 8));
        gridMetodos.setOpaque(false);
        gridMetodos.setMaximumSize(new Dimension(410, 84));
        gridMetodos.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnMetodoEfectivo = crearBotonMetodo("Efectivo", () -> seleccionarMetodo(MetodoPago.EFECTIVO));
        btnMetodoNequi = crearBotonMetodo("Nequi", () -> seleccionarMetodo(MetodoPago.NEQUI));
        btnMetodoDaviplata = crearBotonMetodo("Daviplata", () -> seleccionarMetodo(MetodoPago.DAVIPLATA));
        btnMetodoTarjeta = crearBotonMetodo("Tarjeta", () -> seleccionarMetodo(MetodoPago.TARJETA));
        btnMetodoTransferencia = crearBotonMetodo("Transferencia", () -> seleccionarMetodo(MetodoPago.TRANSFERENCIA));
        btnMetodoMixto = crearBotonMetodo("Pago Mixto", () -> seleccionarMetodo(MetodoPago.MIXTO));

        gridMetodos.add(btnMetodoEfectivo);
        gridMetodos.add(btnMetodoNequi);
        gridMetodos.add(btnMetodoDaviplata);
        gridMetodos.add(btnMetodoTarjeta);
        gridMetodos.add(btnMetodoTransferencia);
        gridMetodos.add(btnMetodoMixto);
        colIzquierda.add(gridMetodos);

        colIzquierda.add(Box.createRigidArea(new Dimension(0, 12)));

        // Panel de Montos (Dinámico según método)
        pnlCamposPago = new JPanel();
        pnlCamposPago.setLayout(new BoxLayout(pnlCamposPago, BoxLayout.Y_AXIS));
        pnlCamposPago.setOpaque(false);
        pnlCamposPago.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCamposPago.setMaximumSize(new Dimension(410, 110));

        txtMontoEfectivo = crearTextFieldMonto();
        txtMontoDigital = crearTextFieldMonto();

        colIzquierda.add(pnlCamposPago);

        colIzquierda.add(Box.createRigidArea(new Dimension(0, 10)));

        // Datos del Cliente
        JLabel lblSecCliente = new JLabel("DATOS DEL CLIENTE (OPCIONAL):");
        lblSecCliente.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSecCliente.setForeground(new Color(148, 163, 184));
        lblSecCliente.setAlignmentX(Component.LEFT_ALIGNMENT);
        colIzquierda.add(lblSecCliente);
        colIzquierda.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel pnlCliente = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlCliente.setOpaque(false);
        pnlCliente.setMaximumSize(new Dimension(410, 48));
        pnlCliente.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNombreCliente = new JTextField("Consumidor Final");
        estilizarTextField(txtNombreCliente);
        txtDocumentoCliente = new JTextField("222222222222");
        estilizarTextField(txtDocumentoCliente);

        pnlCliente.add(crearInputConLabel("Nombre / Razón Social:", txtNombreCliente));
        pnlCliente.add(crearInputConLabel("C.C. / NIT:", txtDocumentoCliente));

        colIzquierda.add(pnlCliente);

        centerPanel.add(colIzquierda, BorderLayout.WEST);

        // ── Columna Derecha: Teclado Numérico Táctil ──
        JPanel colDerecha = new JPanel(new BorderLayout(0, 8));
        colDerecha.setOpaque(false);
        colDerecha.setPreferredSize(new Dimension(350, 360));

        JLabel lblSecTeclado = new JLabel("TECLADO NUMÉRICO TÁCTIL:");
        lblSecTeclado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSecTeclado.setForeground(new Color(148, 163, 184));
        colDerecha.add(lblSecTeclado, BorderLayout.NORTH);

        JPanel pnlNumpad = new JPanel(new GridLayout(4, 3, 8, 8));
        pnlNumpad.setOpaque(false);

        String[] teclas = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "C", "0", "000"
        };

        for (String k : teclas) {
            JButton btnKey = new JButton(k);
            btnKey.setFont(new Font("Segoe UI", Font.BOLD, 22));
            boolean esBorrar = k.equals("C");
            btnKey.setBackground(esBorrar ? new Color(127, 29, 29) : new Color(30, 41, 59));
            btnKey.setForeground(esBorrar ? new Color(254, 202, 202) : Color.WHITE);
            btnKey.setBorder(BorderFactory.createLineBorder(esBorrar ? new Color(239, 68, 68) : new Color(51, 65, 85), 1, true));
            btnKey.setFocusPainted(false);
            btnKey.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnKey.addActionListener(e -> procesarNumpad(k));
            pnlNumpad.add(btnKey);
        }

        colDerecha.add(pnlNumpad, BorderLayout.CENTER);

        // Botón "PAGO EXACTO"
        JButton btnExacto = new JButton("PAGO EXACTO (" + dfCop.format(totalConsumo) + ")");
        btnExacto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExacto.setBackground(new Color(16, 185, 129));
        btnExacto.setForeground(Color.WHITE);
        btnExacto.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        btnExacto.setFocusPainted(false);
        btnExacto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExacto.addActionListener(e -> asignarPagoExacto());

        colDerecha.add(btnExacto, BorderLayout.SOUTH);

        centerPanel.add(colDerecha, BorderLayout.CENTER);

        // 4. FOOTER CON BOTONES DE ACCIÓN
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setForeground(new Color(203, 213, 225));
        btnCancelar.setBackground(new Color(30, 41, 59));
        btnCancelar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());
        footer.add(btnCancelar, BorderLayout.WEST);

        JPanel pnlAccionesDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlAccionesDerecha.setOpaque(false);

        JButton btnCobrarSinImprimir = new JButton("Cobrar sin Imprimir");
        btnCobrarSinImprimir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCobrarSinImprimir.setForeground(Color.WHITE);
        btnCobrarSinImprimir.setBackground(new Color(2, 132, 199)); // Blue
        btnCobrarSinImprimir.setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        btnCobrarSinImprimir.setFocusPainted(false);
        btnCobrarSinImprimir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrarSinImprimir.addActionListener(e -> procesarCobro(false));
        pnlAccionesDerecha.add(btnCobrarSinImprimir);

        JButton btnCobrarEImprimir = new JButton("COBRAR E IMPRIMIR TICKET");
        btnCobrarEImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCobrarEImprimir.setForeground(Color.WHITE);
        btnCobrarEImprimir.setBackground(new Color(16, 185, 129));
        btnCobrarEImprimir.setBorder(BorderFactory.createEmptyBorder(11, 24, 11, 24));
        btnCobrarEImprimir.setFocusPainted(false);
        btnCobrarEImprimir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrarEImprimir.addActionListener(e -> procesarCobro(true));
        pnlAccionesDerecha.add(btnCobrarEImprimir);

        footer.add(pnlAccionesDerecha, BorderLayout.EAST);

        // Estructura general
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(pnlKpis, BorderLayout.SOUTH);

        rootPanel.add(topContainer, BorderLayout.NORTH);
        rootPanel.add(centerPanel, BorderLayout.CENTER);
        rootPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setSize(820, 580);
        setLocationRelativeTo(parentWindow);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LÓGICA DE MÉTODOS DE PAGO Y TECLADO
    // ─────────────────────────────────────────────────────────────────────────

    private void seleccionarMetodo(MetodoPago metodo) {
        this.metodoActual = metodo;

        // Resetear estilos de botones
        JButton[] btns = { btnMetodoEfectivo, btnMetodoNequi, btnMetodoDaviplata, btnMetodoTarjeta, btnMetodoTransferencia, btnMetodoMixto };
        for (JButton b : btns) {
            b.setBackground(new Color(30, 41, 59));
            b.setForeground(new Color(203, 213, 225));
            b.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true));
        }

        // Resaltar seleccionado
        JButton btnActivo = btnMetodoEfectivo;
        switch (metodo) {
            case NEQUI: btnActivo = btnMetodoNequi; break;
            case DAVIPLATA: btnActivo = btnMetodoDaviplata; break;
            case TARJETA: btnActivo = btnMetodoTarjeta; break;
            case TRANSFERENCIA: btnActivo = btnMetodoTransferencia; break;
            case MIXTO: btnActivo = btnMetodoMixto; break;
            default: btnActivo = btnMetodoEfectivo; break;
        }
        btnActivo.setBackground(new Color(16, 185, 129, 60));
        btnActivo.setForeground(new Color(52, 211, 153));
        btnActivo.setBorder(BorderFactory.createLineBorder(new Color(16, 185, 129), 2, true));

        // Actualizar campos de entrada
        pnlCamposPago.removeAll();

        if (metodo == MetodoPago.MIXTO) {
            txtMontoEfectivo.setText("0");
            txtMontoDigital.setText("0");
            campoActivoNumpad = txtMontoEfectivo;

            pnlCamposPago.add(crearFilaMonto("Monto en Efectivo ($):", txtMontoEfectivo));
            pnlCamposPago.add(Box.createRigidArea(new Dimension(0, 8)));
            pnlCamposPago.add(crearFilaMonto("Monto Digital / Tarjeta ($):", txtMontoDigital));
        } else if (metodo == MetodoPago.EFECTIVO) {
            txtMontoEfectivo.setText(String.format(Locale.US, "%.0f", totalConsumo));
            txtMontoDigital.setText("0");
            campoActivoNumpad = txtMontoEfectivo;
            pnlCamposPago.add(crearFilaMonto("Monto Entregado (Efectivo):", txtMontoEfectivo));
        } else {
            txtMontoEfectivo.setText("0");
            txtMontoDigital.setText(String.format(Locale.US, "%.0f", totalConsumo));
            campoActivoNumpad = txtMontoDigital;
            pnlCamposPago.add(crearFilaMonto("Monto " + metodo.name() + " ($):", txtMontoDigital));
        }

        pnlCamposPago.revalidate();
        pnlCamposPago.repaint();
        recalcularVueltas();
    }

    private void procesarNumpad(String tecla) {
        if (campoActivoNumpad == null) return;

        String actual = campoActivoNumpad.getText().trim().replace("$", "").replace(".", "").replace(",", "").trim();
        if (actual.equals("0")) actual = "";

        if (tecla.equals("C")) {
            actual = "0";
        } else if (tecla.equals("000")) {
            if (!actual.isEmpty() && !actual.equals("0")) {
                actual += "000";
            }
        } else {
            actual += tecla;
        }

        if (actual.isEmpty()) actual = "0";
        campoActivoNumpad.setText(actual);
        recalcularVueltas();
    }

    private void asignarPagoExacto() {
        if (metodoActual == MetodoPago.MIXTO) {
            txtMontoEfectivo.setText(String.format(Locale.US, "%.0f", totalConsumo));
            txtMontoDigital.setText("0");
        } else if (metodoActual == MetodoPago.EFECTIVO) {
            txtMontoEfectivo.setText(String.format(Locale.US, "%.0f", totalConsumo));
            txtMontoDigital.setText("0");
        } else {
            txtMontoEfectivo.setText("0");
            txtMontoDigital.setText(String.format(Locale.US, "%.0f", totalConsumo));
        }
        recalcularVueltas();
    }

    private void recalcularVueltas() {
        double ef = parseMonto(txtMontoEfectivo.getText());
        double dig = parseMonto(txtMontoDigital.getText());
        double totalRecibido = ef + dig;

        lblRecibidoDisplay.setText(dfCop.format(totalRecibido));

        double cambio = totalRecibido - totalConsumo;

        if (cambio < 0) {
            double falta = Math.abs(cambio);
            lblCambioTitulo.setText("FALTA POR PAGAR");
            lblCambioDisplay.setText(dfCop.format(falta));
            lblCambioDisplay.setForeground(new Color(248, 113, 113)); // Red
            cambioColorActual = new Color(239, 68, 68);
        } else {
            lblCambioTitulo.setText("VUELTAS / CAMBIO");
            lblCambioDisplay.setText(dfCop.format(cambio));
            lblCambioDisplay.setForeground(new Color(52, 211, 153)); // Emerald
            cambioColorActual = new Color(16, 185, 129);
        }
        cardCambio.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROCESAMIENTO DEL PAGO
    // ─────────────────────────────────────────────────────────────────────────

    private void procesarCobro(boolean imprimirTicket) {
        double ef = parseMonto(txtMontoEfectivo.getText());
        double dig = parseMonto(txtMontoDigital.getText());
        double totalPagado = ef + dig;

        if (totalPagado < totalConsumo) {
            double falta = totalConsumo - totalPagado;
            ModalAlerta.advertencia(this, "Monto Insuficiente", "El monto ingresado no cubre el total.<br>Falta por pagar: <b>" + dfCop.format(falta) + "</b>");
            return;
        }

        String tipoPagoBD;
        double pagoEf = ef;
        double pagoTrans = 0.0;
        double pagoTarj = 0.0;

        switch (metodoActual) {
            case TARJETA:
                tipoPagoBD = "TARJETA";
                pagoTarj = dig;
                break;
            case NEQUI:
                tipoPagoBD = "NEQUI";
                pagoTrans = dig;
                break;
            case DAVIPLATA:
                tipoPagoBD = "DAVIPLATA";
                pagoTrans = dig;
                break;
            case TRANSFERENCIA:
                tipoPagoBD = "TRANSACCION";
                pagoTrans = dig;
                break;
            case MIXTO:
                tipoPagoBD = "MIXTO";
                pagoTrans = dig;
                break;
            default:
                tipoPagoBD = "EFECTIVO";
                break;
        }

        dispose();

        final String finalTipoPagoBD = tipoPagoBD;
        final double finalPagoEf = pagoEf;
        final double finalPagoTrans = pagoTrans;
        final double finalPagoTarj = pagoTarj;
        final double finalTotalPagado = totalPagado;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                PedidosDao pedDao = new PedidosDao();
                boolean ok;

                if (idsRondasAcumuladas != null && idsRondasAcumuladas.size() > 1) {
                    ok = pedDao.finalizarMultiplePedidosConPago(idsRondasAcumuladas, finalTipoPagoBD, finalPagoEf, finalPagoTrans, finalPagoTarj);
                } else {
                    ok = pedDao.finalizarPedidoConPago(idPedido, finalTipoPagoBD, finalPagoEf, finalPagoTrans, finalPagoTarj);
                }

                if (ok) {
                    SonidoPOS.reproducirCobro();
                    SonidoPOS.anunciarVoz("Pedido finalizado con éxito.");
                    pedDao.pdfPedido(idPedido);

                    if (imprimirTicket && tableFinalizarRef != null) {
                        try {
                            ImpresionTicket impresion = new ImpresionTicket();
                            impresion.imprimirTicket(idPedido, tableFinalizarRef);
                        } catch (Exception ex) {
                            System.err.println("Error al imprimir ticket: " + ex.getMessage());
                        }
                    }
                    pedDao.generarReporteDiario();
                }
                return ok;
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        sistemaInstance.notificarCobroCompletado(idPedido, finalTotalPagado - totalConsumo);
                    } else {
                        ModalAlerta.error(parentWindow, "Error al Finalizar", "No se pudo registrar el pago en la base de datos.");
                    }
                } catch (Exception ex) {
                    ModalAlerta.error(parentWindow, "Error", "Ocurrió un error inesperado: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS DE INTERFAZ
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel crearCardKpi(String titulo, String valor, Color fgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo sólido Dark Slate 800
                g2.setColor(new Color(24, 33, 53));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Tinte suave
                g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Borde
                g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(148, 163, 184));

        JLabel lblV = new JLabel(valor);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblV.setForeground(fgColor);

        card.add(lblT, BorderLayout.NORTH);
        card.add(lblV, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardKpiDinamica(String titulo, JLabel lblValor, Color fgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo sólido Dark Slate 800
                g2.setColor(new Color(24, 33, 53));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Tinte suave
                g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Borde
                g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(148, 163, 184));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValor.setForeground(fgColor);

        card.add(lblT, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearCardCambio(JLabel lblT, JLabel lblV) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo sólido Dark Slate 800
                g2.setColor(new Color(24, 33, 53));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Tinte suave según cambioColorActual
                g2.setColor(new Color(cambioColorActual.getRed(), cambioColorActual.getGreen(), cambioColorActual.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Borde
                g2.setColor(new Color(cambioColorActual.getRed(), cambioColorActual.getGreen(), cambioColorActual.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(148, 163, 184));

        lblV.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblV.setForeground(new Color(16, 185, 129));

        card.add(lblT, BorderLayout.NORTH);
        card.add(lblV, BorderLayout.CENTER);
        return card;
    }

    private JButton crearBotonMetodo(String texto, Runnable onClick) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(30, 41, 59));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    private JTextField crearTextFieldMonto() {
        JTextField tf = new JTextField("0");
        tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tf.setForeground(Color.WHITE);
        tf.setBackground(new Color(11, 17, 32));
        tf.setCaretColor(new Color(56, 189, 248));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 189, 248, 140), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campoActivoNumpad = tf;
                tf.selectAll();
            }
        });
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                recalcularVueltas();
            }
        });
        return tf;
    }

    private JPanel crearFilaMonto(String label, JTextField tf) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(203, 213, 225));
        p.add(lbl, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearInputConLabel(String label, JTextField tf) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(148, 163, 184));
        p.add(lbl, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private void estilizarTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(Color.WHITE);
        tf.setBackground(new Color(11, 17, 32));
        tf.setCaretColor(new Color(56, 189, 248));
        tf.setPreferredSize(new Dimension(180, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private double parseMonto(String txt) {
        if (txt == null) return 0.0;
        try {
            String clean = txt.replace("$", "").replace(".", "").replace(",", "").trim();
            if (clean.isEmpty()) return 0.0;
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
