package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class ImpresionTicket {

    private static final int ANCHO_TICKET = 29;
    private static final String SEPARADOR = "-----------------------------\n";

    private String rucRestaurante;
    private String nombreRestaurante;
    private String telefonoRestaurante;
    private String direccionRestaurante;
    private String mensajeRestaurante;
    private String sala;
    private int numMesa;
    private String usuario;
    private double pagoEfectivo;
    private double pagoTransaccion;
    private Conexion cn = new Conexion();
    private PedidosDao pedidosDao = new PedidosDao();

    // Método para formatear valores monetarios sin decimales si son enteros, con separador de miles
    private String formatearMoneda(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df;
        if (valor == (long) valor) {
            df = new DecimalFormat("$#,##0", symbols);
        } else {
            df = new DecimalFormat("$#,##0.00", symbols);
        }
        return df.format(valor);
    }

    public void datosRestaurante() {
        String sql = "SELECT * FROM config WHERE id = 1";
        try (Connection con = cn.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                rucRestaurante = rs.getString("ruc");
                nombreRestaurante = rs.getString("nombre");
                telefonoRestaurante = rs.getString("telefono");
                direccionRestaurante = rs.getString("direccion");
                mensajeRestaurante = rs.getString("mensaje");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener datos del restaurante: " + e.getMessage());
        }
    }

    public void datosPedido(int idPedido) {
        String sql = "SELECT s.nombre AS sala, p.num_mesa, p.usuario, p.pago_efectivo, p.pago_transaccion FROM pedidos p "
                + "INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
        try (Connection con = cn.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sala = rs.getString("sala");
                    numMesa = rs.getInt("num_mesa");
                    usuario = rs.getString("usuario");
                    pagoEfectivo = rs.getDouble("pago_efectivo");
                    pagoTransaccion = rs.getDouble("pago_transaccion");
                } else {
                    System.out.println("No se encontró el pedido con ID: " + idPedido);
                    sala = "Desconocida";
                    numMesa = 0;
                    usuario = "Desconocido";
                    pagoEfectivo = 0.0;
                    pagoTransaccion = 0.0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener datos del pedido: " + e.getMessage());
            sala = "Error";
            numMesa = 0;
            usuario = "Error";
            pagoEfectivo = 0.0;
            pagoTransaccion = 0.0;
        }
    }

    public void imprimirTicket(int idPedido, JTable tableFinalizar) {
        try {
            datosRestaurante();
            datosPedido(idPedido);
            StringBuilder sb = new StringBuilder();
            String fechaActual = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date());
            double totalGeneral = 0.0;

            // ENCABEZADO
            sb.append(String.format("%s\n", centrarTexto(nombreRestaurante, ANCHO_TICKET)));
            sb.append(String.format("NIT: %s\n", rucRestaurante));
            sb.append(String.format("Tel: %s\n", telefonoRestaurante));
            sb.append(String.format("Dir: %s\n", cortar(direccionRestaurante, ANCHO_TICKET)));
            sb.append(String.format("Sala: %s\n", cortar(sala, ANCHO_TICKET)));
            sb.append(String.format("Mesa: %d\n", numMesa));
            sb.append(SEPARADOR);
            sb.append(String.format("Factura: %04d\n", idPedido));
            sb.append(String.format("Fecha: %s\n", fechaActual));
            sb.append(SEPARADOR);

            // PRODUCTOS
            sb.append("C. Descripción         Total\n");
            for (int i = 0; i < tableFinalizar.getRowCount(); i++) {
                // Convertir la cantidad a entero
                int cantidad;
                try {
                    Object valorCantidad = tableFinalizar.getValueAt(i, 2);
                    if (valorCantidad instanceof Number) {
                        cantidad = ((Number) valorCantidad).intValue();
                    } else {
                        cantidad = Integer.parseInt(valorCantidad.toString());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error en cantidad en fila " + i + ": " + e.getMessage());
                    continue;
                }
                String producto = tableFinalizar.getValueAt(i, 1).toString();
                String total = tableFinalizar.getValueAt(i, 4).toString();
                double totalItem = Double.parseDouble(total);
                totalGeneral += totalItem;
                // Formatear: Cantidad(2 chars), espacio(1 char), Producto(16 chars), espacio(1 char), Precio(9 chars de ancho derecho)
                // Ancho total: 2 + 1 + 16 + 1 + 9 = 29
                sb.append(String.format("%-2d %-16s %9s\n", cantidad, cortar(producto, 16), formatearMoneda(totalItem)));
                // Imprimir comentario si existe
                Object comentarioObj = tableFinalizar.getValueAt(i, 5);
                if (comentarioObj != null && !comentarioObj.toString().trim().isEmpty()) {
                    sb.append(String.format("  >> %s\n", cortar(comentarioObj.toString().trim(), 25)));
                }
            }
            sb.append(SEPARADOR);

            // TOTAL
            // Formatear líneas de totales: etiqueta(16 chars), espacio(1 char), precio(12 chars de ancho derecho)
            // Ancho total: 16 + 1 + 12 = 29
            sb.append(String.format("%-16s %12s\n", "TOTAL CONSUMO:", formatearMoneda(totalGeneral)));
            sb.append(String.format("%-16s %12s\n", "PAGO EFECTIVO:", formatearMoneda(pagoEfectivo)));
            sb.append(String.format("%-16s %12s\n", "PAGO TRANSF.:", formatearMoneda(pagoTransaccion)));
            double cambio = (pagoEfectivo + pagoTransaccion) - totalGeneral;
            if (cambio < 0) cambio = 0.0;
            sb.append(String.format("%-16s %12s\n", "CAMBIO/VUELTOS:", formatearMoneda(cambio)));
            sb.append(SEPARADOR);

            // MENSAJE
            sb.append("\n").append(cortar(mensajeRestaurante, ANCHO_TICKET)).append("\n");
            sb.append(SEPARADOR);
            sb.append("¡Gracias por su visita!\n\n");

            // COMANDO DE CORTE (ESC/POS)
            byte[] corte = new byte[]{0x1D, 'V', 1};
            byte[] datosTexto = sb.toString().getBytes("CP437");
            byte[] datosFinal = new byte[datosTexto.length + corte.length];
            System.arraycopy(datosTexto, 0, datosFinal, 0, datosTexto.length);
            System.arraycopy(corte, 0, datosFinal, datosTexto.length, corte.length);

            // ENVIAR A LA IMPRESORA
            PrintService impresora = PrintServiceLookup.lookupDefaultPrintService();
            if (impresora == null) {
                JOptionPane.showMessageDialog(null, "No se encontró impresora predeterminada.");
                return;
            }

            DocPrintJob job = impresora.createPrintJob();
            Doc doc = new SimpleDoc(datosFinal, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, null);

            System.out.println("Ticket enviado a impresión para pedido ID: " + idPedido);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al imprimir el ticket: " + e.getMessage());
        }
    }

    public void imprimirTotalesDiariosMinimal() {
        try {
            // Determinar el rango de fechas
            Calendar now = Calendar.getInstance();
            Calendar inicio = (Calendar) now.clone();
            Calendar fin = (Calendar) now.clone();
            if (now.get(Calendar.HOUR_OF_DAY) < 4) {
                inicio.add(Calendar.DAY_OF_MONTH, -1);
                inicio.set(Calendar.HOUR_OF_DAY, 16);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                fin.set(Calendar.HOUR_OF_DAY, 4);
                fin.set(Calendar.MINUTE, 0);
                fin.set(Calendar.SECOND, 0);
            } else {
                inicio.set(Calendar.HOUR_OF_DAY, 16);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                fin.add(Calendar.DAY_OF_MONTH, 1);
                fin.set(Calendar.HOUR_OF_DAY, 4);
                fin.set(Calendar.MINUTE, 0);
                fin.set(Calendar.SECOND, 0);
            }
            Timestamp fechaInicio = new Timestamp(inicio.getTimeInMillis());
            Timestamp fechaFin = new Timestamp(fin.getTimeInMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            System.out.println("Imprimiendo totales diarios entre:");
            System.out.println("  Desde: " + sdf.format(fechaInicio));
            System.out.println("  Hasta: " + sdf.format(fechaFin));

            // Calcular totales
            double[] totales = pedidosDao.calcularTotalesDia(fechaInicio, fechaFin, 0);
            double totalEfectivo = totales[0];
            double totalTransaccion = totales[1];
            double totalGeneral = 0.0;
            String sqlTotal = "SELECT SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ?";
            try (Connection con = cn.getConnection(); PreparedStatement ps = con.prepareStatement(sqlTotal)) {
                ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
                ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalGeneral = rs.getDouble("total");
                        if (rs.wasNull()) {
                            totalGeneral = 0.0;
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error al calcular total general: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al calcular totales: " + e.getMessage());
                return;
            }

            // Formatear el ticket minimalista
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Total Efectivo:    %s\n", formatearMoneda(totalEfectivo)));
            sb.append(String.format("Total Transacción: %s\n", formatearMoneda(totalTransaccion)));
            sb.append(String.format("Total General:     %s\n", formatearMoneda(totalGeneral)));
            if (Math.abs((totalEfectivo + totalTransaccion) - totalGeneral) > 0.01) {
                sb.append("Advertencia: Suma no coincide\n");
            }
            sb.append("\n"); // Espacio para el corte

            // COMANDO DE CORTE (ESC/POS)
            byte[] corte = new byte[]{0x1D, 'V', 1};
            byte[] datosTexto = sb.toString().getBytes("CP437");
            byte[] datosFinal = new byte[datosTexto.length + corte.length];
            System.arraycopy(datosTexto, 0, datosFinal, 0, datosTexto.length);
            System.arraycopy(corte, 0, datosFinal, datosTexto.length, corte.length);

            // ENVIAR A LA IMPRESORA
            PrintService impresora = PrintServiceLookup.lookupDefaultPrintService();
            if (impresora == null) {
                JOptionPane.showMessageDialog(null, "No se encontró impresora predeterminada.");
                return;
            }

            DocPrintJob job = impresora.createPrintJob();
            Doc doc = new SimpleDoc(datosFinal, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, null);

            System.out.println("Ticket de totales diarios minimalista enviado a impresión");
            JOptionPane.showMessageDialog(null, "Ticket de totales diarios impreso correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al imprimir totales diarios minimalista: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al imprimir totales diarios: " + e.getMessage());
        }
    }

    private String cortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() > max ? texto.substring(0, max - 1) + "." : texto;
    }

    private String centrarTexto(String texto, int ancho) {
        if (texto == null) {
            texto = "";
        }
        int espacios = (ancho - texto.length()) / 2;
        return " ".repeat(Math.max(0, espacios)) + texto;
    }
}
