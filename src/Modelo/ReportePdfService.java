package Modelo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Servicio encargado de la generación de comprobantes y reportes en formato PDF con iText.
 */
public class ReportePdfService {

    private final Conexion cn = new Conexion();
    private final PedidosDao pedidosDao = new PedidosDao();

    /**
     * Genera un comprobante PDF del pedido.
     */
    public void pdfPedido(int id_pedido) {
        String fechaPedido = null, usuario = null, total = null, sala = null, num_mesa = null;
        double pagoEfectivo = 0.0, pagoTransaccion = 0.0;
        try {
            String appDataPath = System.getenv("APPDATA");
            if (appDataPath == null) {
                appDataPath = System.getProperty("java.io.tmpdir");
            }
            String fechaStr = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
            File pedidosDir = new File(appDataPath, ".pedidos_ocultos");
            File fechaDir = new File(pedidosDir, fechaStr);
            if (!fechaDir.exists()) {
                fechaDir.mkdirs();
            }

            File salida = new File(fechaDir, "pedido_" + id_pedido + "_" + fechaStr + ".pdf");
            try (FileOutputStream archivo = new FileOutputStream(salida)) {
                Document doc = new Document();
                PdfWriter.getInstance(doc, archivo);
                doc.open();

                String informacion = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(informacion) : null) {
                    if (ps != null) {
                        ps.setInt(1, id_pedido);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                num_mesa = rs.getString("num_mesa");
                                sala = rs.getString("nombre");
                                fechaPedido = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("fecha"));
                                usuario = rs.getString("usuario");
                                total = rs.getString("total");
                                pagoEfectivo = rs.getDouble("pago_efectivo");
                                pagoTransaccion = rs.getDouble("pago_transaccion");
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error al obtener datos del pedido para PDF: " + e.getMessage());
                }

                PdfPTable Encabezado = new PdfPTable(2);
                Encabezado.setWidthPercentage(100);
                Encabezado.getDefaultCell().setBorder(0);
                float[] columnWidthsEncabezado = new float[]{20f, 80f};
                Encabezado.setWidths(columnWidthsEncabezado);
                Encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);

                try {
                    Image img = Image.getInstance(getClass().getResource("/Img/pizzeria.png"));
                    Encabezado.addCell(img);
                } catch (Exception e) {
                    Encabezado.addCell("");
                }

                String ruc = "", nom = "", tel = "", dir = "", razon = "";
                String configSql = "SELECT * FROM config";
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(configSql) : null;
                     ResultSet rs = ps != null ? ps.executeQuery() : null) {
                    if (rs != null && rs.next()) {
                        ruc = rs.getString("ruc");
                        nom = rs.getString("nombre");
                        tel = rs.getString("telefono");
                        dir = rs.getString("direccion");
                        razon = rs.getString("mensaje");
                    }
                } catch (SQLException e) {
                    System.err.println("Error al obtener config para PDF: " + e.getMessage());
                }

                Paragraph p = new Paragraph();
                Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
                p.add(new Chunk(nom + "\nNIT: " + ruc + "\nTeléfono: " + tel + "\nDirección: " + dir + "\n" + razon, negrita));
                Encabezado.addCell(p);
                doc.add(Encabezado);

                Paragraph cliente = new Paragraph();
                cliente.add(new Chunk("\nDatos de la Venta:\n", negrita));
                doc.add(cliente);

                PdfPTable tablaCliente = new PdfPTable(4);
                tablaCliente.setWidthPercentage(100);
                tablaCliente.getDefaultCell().setBorder(0);
                float[] columnWidthsCliente = new float[]{25f, 25f, 25f, 25f};
                tablaCliente.setWidths(columnWidthsCliente);

                PdfPCell cellMesa = new PdfPCell(new Phrase("N° Mesa: " + num_mesa, negrita));
                PdfPCell cellSala = new PdfPCell(new Phrase("Sala: " + sala, negrita));
                PdfPCell cellFecha = new PdfPCell(new Phrase("Fecha: " + fechaPedido, negrita));
                PdfPCell cellVendedor = new PdfPCell(new Phrase("Atendido por: " + usuario, negrita));

                cellMesa.setBorder(0); cellSala.setBorder(0); cellFecha.setBorder(0); cellVendedor.setBorder(0);
                tablaCliente.addCell(cellMesa); tablaCliente.addCell(cellSala); tablaCliente.addCell(cellFecha); tablaCliente.addCell(cellVendedor);
                doc.add(tablaCliente);

                Paragraph space = new Paragraph(" ");
                doc.add(space);

                PdfPTable tablaProducto = new PdfPTable(5);
                tablaProducto.setWidthPercentage(100);
                tablaProducto.getDefaultCell().setBorder(0);
                float[] columnWidthsProducto = new float[]{15f, 40f, 15f, 15f, 15f};
                tablaProducto.setWidths(columnWidthsProducto);

                PdfPCell pr1 = new PdfPCell(new Phrase("Cant.", negrita));
                PdfPCell pr2 = new PdfPCell(new Phrase("Descripción", negrita));
                PdfPCell pr3 = new PdfPCell(new Phrase("Precio U.", negrita));
                PdfPCell pr4 = new PdfPCell(new Phrase("Precio T.", negrita));
                PdfPCell pr5 = new PdfPCell(new Phrase("Comentario", negrita));

                pr1.setBorder(0); pr2.setBorder(0); pr3.setBorder(0); pr4.setBorder(0); pr5.setBorder(0);
                pr1.setBackgroundColor(BaseColor.LIGHT_GRAY); pr2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pr3.setBackgroundColor(BaseColor.LIGHT_GRAY); pr4.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pr5.setBackgroundColor(BaseColor.LIGHT_GRAY);

                tablaProducto.addCell(pr1); tablaProducto.addCell(pr2); tablaProducto.addCell(pr3); tablaProducto.addCell(pr4); tablaProducto.addCell(pr5);

                String productoSql = "SELECT d.id, d.precio, d.cantidad, d.sub_total, d.comentario, p.nombre FROM detalle_pedidos d INNER JOIN platos p ON d.id_pedido = ? AND d.nombre = p.nombre";
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(productoSql) : null) {
                    if (ps != null) {
                        ps.setInt(1, id_pedido);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String cantidad = rs.getString("cantidad");
                                String producto = rs.getString("nombre");
                                String precio = rs.getString("precio");
                                String subTotal = rs.getString("sub_total");
                                String comentario = rs.getString("comentario");
                                if (comentario == null) comentario = "";

                                tablaProducto.addCell(cantidad);
                                tablaProducto.addCell(producto);
                                tablaProducto.addCell(precio);
                                tablaProducto.addCell(subTotal);
                                tablaProducto.addCell(comentario);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error al obtener detalles del pedido para PDF: " + e.getMessage());
                }

                doc.add(tablaProducto);

                Paragraph infoTotal = new Paragraph();
                infoTotal.add(new Chunk("\nTotal a Pagar: " + total, negrita));
                doc.add(infoTotal);

                Paragraph gr = new Paragraph();
                gr.add(new Chunk("\n¡Gracias por su compra!\n", negrita));
                doc.add(gr);

                doc.close();
            }
        } catch (Exception e) {
            System.err.println("Error al generar PDF del pedido: " + e.getMessage());
        }
    }
}
