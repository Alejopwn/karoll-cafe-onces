package Modelo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Objeto de acceso a datos (DAO) para la gestión de pedidos, detalles, totales y reportes PDF.
 */
public class PedidosDao {

    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final Conexion cn = new Conexion();

    /**
     * Obtiene el ID máximo de la tabla pedidos.
     */
    public int IdPedido() {
        int id = 0;
        String sql = "SELECT MAX(id) FROM pedidos";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último ID de pedido: " + e.getMessage());
        }
        return id;
    }

    /**
     * Verifica si una mesa en una sala específica tiene un pedido en estado PENDIENTE.
     */
    public int verificarStado(int mesa, int id_sala) {
        int id_pedido = 0;
        String sql = "SELECT id FROM pedidos WHERE num_mesa=? AND id_sala=? AND estado = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return 0;
            ps.setInt(1, mesa);
            ps.setInt(2, id_sala);
            ps.setString(3, "PENDIENTE");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id_pedido = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado de mesa: " + e.getMessage());
        }
        return id_pedido;
    }

    /**
     * Obtiene en UNA sola query el mapa de num_mesa -> id_pedido para todas las
     * mesas PENDIENTES de una sala. Mucho mas eficiente que llamar a
     * verificarStado() por cada mesa individualmente.
     */
    public java.util.Map<Integer, Integer> getMesasOcupadas(int id_sala) {
        java.util.Map<Integer, Integer> mapa = new java.util.HashMap<>();
        String sql = "SELECT num_mesa, id FROM pedidos WHERE id_sala=? AND estado='PENDIENTE'";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return mapa;
            ps.setInt(1, id_sala);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getInt("num_mesa"), rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener mesas ocupadas: " + e.getMessage());
        }
        return mapa;
    }

    /**
     * Obtiene la suma total consumida en una sala dentro de un rango de fechas.
     */
    public double getTotalPedidosDia(String fechaInicio, String fechaFin, int idSala) {
        double total = 0.0;
        String sql = "SELECT SUM(total) AS total FROM pedidos WHERE id_sala = ? AND fecha BETWEEN ? AND ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return 0.0;
            ps.setInt(1, idSala);
            ps.setString(2, fechaInicio);
            ps.setString(3, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total");
                    if (rs.wasNull()) {
                        total = 0.0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el total del día para sala ID " + idSala + ": " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al consultar el total del día para sala ID " + idSala);
        }
        return total;
    }

    /**
     * Registra un nuevo pedido y retorna el ID autogenerado.
     */
    public int RegistrarPedido(Pedido ped) {
        int idGenerado = -1;
        CajaDao cajaDao = new CajaDao();
        CierreCaja activa = cajaDao.obtenerCajaActiva();
        Integer idCierre = (activa != null) ? activa.getId() : null;

        String sql = "INSERT INTO pedidos (id_sala, num_mesa, total, usuario, fecha, id_cierre) VALUES (?,?,?,?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : null) {
            if (ps == null) return -1;
            ps.setInt(1, ped.getId_sala());
            ps.setInt(2, ped.getNum_mesa());
            ps.setDouble(3, ped.getTotal());
            ps.setString(4, ped.getUsuario());
            ps.setString(5, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            if (idCierre != null) {
                ps.setInt(6, idCierre);
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
        }
        return idGenerado;
    }

    /**
     * Lista los detalles de ítems agregados a un pedido.
     */
    public List<DetallePedido> verPedidoDetalle(int id_pedido) {
        List<DetallePedido> lista = new ArrayList<>();
        String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return lista;
            ps.setInt(1, id_pedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido det = new DetallePedido();
                    det.setId(rs.getInt("id"));
                    det.setNombre(rs.getString("nombre"));
                    det.setPrecio(rs.getDouble("precio"));
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setComentario(rs.getString("comentario"));
                    lista.add(det);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles del pedido: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene la información cabecera de un pedido por su ID.
     */
    public Pedido verPedido(int id_pedido) {
        Pedido ped = new Pedido();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return ped;
            ps.setInt(1, id_pedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ped.setId(rs.getInt("id"));
                    ped.setFecha(rs.getString("fecha"));
                    ped.setSala(rs.getString("nombre"));
                    ped.setNum_mesa(rs.getInt("num_mesa"));
                    ped.setTotal(rs.getDouble("total"));
                    ped.setPago_efectivo(rs.getDouble("pago_efectivo"));
                    ped.setPago_transaccion(rs.getDouble("pago_transaccion"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar pedido: " + e.getMessage());
        }
        return ped;
    }

    public boolean actualizarEstado(int id_pedido, String estado) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, estado);
            ps.setInt(2, id_pedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retorna el detalle de un pedido al finalizarlo.
     */
    public List<DetallePedido> finalizarPedido(int id_pedido) {
        return verPedidoDetalle(id_pedido);
    }

    /**
     * Genera un comprobante PDF del pedido en el directorio de la aplicación.
     */
    public void pdfPedido(int id_pedido) {
        String fechaPedido = null, usuario = null, total = null, sala = null, num_mesa = null;
        double pagoEfectivo = 0.0, pagoTransaccion = 0.0;
        try {
            String appDataPath = System.getenv("APPDATA");
            if (appDataPath == null) {
                appDataPath = System.getProperty("java.io.tmpdir");
            }
            String fechaStr = new SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date());
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
                Encabezado.setWidths(new float[]{50f, 50f});
                Encabezado.setHorizontalAlignment(0);

                String config = "SELECT * FROM config";
                String mensaje = "";
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(config) : null;
                     ResultSet rs = ps != null ? ps.executeQuery() : null) {
                    if (rs != null && rs.next()) {
                        mensaje = rs.getString("mensaje");
                        Encabezado.addCell("NIT:    " + rs.getString("ruc") + "\nNombre: " + rs.getString("nombre")
                                + "\nTeléfono: " + rs.getString("telefono") + "\nDirección: " + rs.getString("direccion"));
                    }
                } catch (SQLException e) {
                    System.err.println("Error al obtener config para PDF: " + e.getMessage());
                }

                Paragraph info = new Paragraph();
                Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
                info.add("Atendido: " + usuario + "\nN° Pedido: " + id_pedido + "\nFecha: " + fechaPedido
                        + "\nSala: " + sala + "\nN° Mesa: " + num_mesa);
                Encabezado.addCell(info);
                doc.add(Encabezado);
                doc.add(Chunk.NEWLINE);

                PdfPTable tabla = new PdfPTable(5);
                tabla.setWidthPercentage(100);
                tabla.getDefaultCell().setBorder(0);
                tabla.setWidths(new float[]{8f, 35f, 15f, 15f, 27f});
                tabla.setHorizontalAlignment(0);

                PdfPCell c1 = new PdfPCell(new Phrase("Cant.", negrita));
                PdfPCell c2 = new PdfPCell(new Phrase("Plato.", negrita));
                PdfPCell c3 = new PdfPCell(new Phrase("P. unt.", negrita));
                PdfPCell c4 = new PdfPCell(new Phrase("P. Total", negrita));
                PdfPCell c5 = new PdfPCell(new Phrase("Comentario", negrita));
                c1.setBorder(0); c2.setBorder(0); c3.setBorder(0); c4.setBorder(0); c5.setBorder(0);
                c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
                c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
                c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
                c5.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabla.addCell(c1); tabla.addCell(c2); tabla.addCell(c3); tabla.addCell(c4); tabla.addCell(c5);

                String product = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(product) : null) {
                    if (ps != null) {
                        ps.setInt(1, id_pedido);
                        try (ResultSet rs = ps.executeQuery()) {
                            Font comentFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.ITALIC, BaseColor.DARK_GRAY);
                            while (rs.next()) {
                                double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
                                tabla.addCell(rs.getString("cantidad"));
                                tabla.addCell(rs.getString("nombre"));
                                tabla.addCell(String.format("%.2f COP", rs.getDouble("precio")));
                                tabla.addCell(String.format("%.2f COP", subTotal));
                                String comentario = rs.getString("comentario");
                                if (comentario != null && !comentario.trim().isEmpty()) {
                                    PdfPCell commentCell = new PdfPCell(new Phrase(comentario.trim(), comentFont));
                                    commentCell.setBorder(0);
                                    commentCell.setBackgroundColor(new BaseColor(255, 255, 204));
                                    tabla.addCell(commentCell);
                                } else {
                                    PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                                    emptyCell.setBorder(0);
                                    tabla.addCell(emptyCell);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error al obtener detalles del pedido para PDF: " + e.getMessage());
                }

                doc.add(tabla);

                double totalValor = (total != null && !total.isEmpty()) ? Double.parseDouble(total) : 0.0;
                double cambio = (pagoEfectivo + pagoTransaccion) - totalValor;
                if (cambio < 0) cambio = 0.0;

                Paragraph agra = new Paragraph();
                agra.add(Chunk.NEWLINE);
                agra.add(String.format("Total Consumo: %.2f COP\n", totalValor));
                agra.add(String.format("Pago Efectivo: %.2f COP\n", pagoEfectivo));
                agra.add(String.format("Pago Transacción: %.2f COP\n", pagoTransaccion));
                agra.add(String.format("Cambio/Vueltos: %.2f COP\n", cambio));
                agra.setAlignment(2);
                doc.add(agra);

                Paragraph firma = new Paragraph();
                firma.add(Chunk.NEWLINE);
                firma.add("Cancelacion \n\n------------------------------------\nFirma \n");
                firma.setAlignment(1);
                doc.add(firma);

                Paragraph gr = new Paragraph();
                gr.add(Chunk.NEWLINE);
                gr.add(mensaje);
                gr.setAlignment(1);
                doc.add(gr);

                doc.close();
            }
        } catch (DocumentException | IOException e) {
            System.err.println("Error al generar PDF del pedido: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al generar el PDF del pedido: " + e.getMessage());
        }
    }

    /**
     * Actualiza el estado del pedido a FINALIZADO y descuenta el inventario.
     */
    public boolean actualizarEstado(int id_pedido) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, ESTADO_FINALIZADO);
            ps.setInt(2, id_pedido);
            ps.execute();
            descontarStockDePedido(id_pedido);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finaliza un pedido registrando los montos pagados en efectivo y/o transacción.
     */
    public boolean finalizarPedidoConPago(int id_pedido, double pago_efectivo, double pago_transaccion) {
        CajaDao cajaDao = new CajaDao();
        CierreCaja activa = cajaDao.obtenerCajaActiva();
        Integer idCierre = (activa != null) ? activa.getId() : null;

        String sql = "UPDATE pedidos SET estado = ?, pago_efectivo = ?, pago_transaccion = ?, id_cierre = COALESCE(id_cierre, ?) WHERE id = ?";
        Connection con = null;
        try {
            con = cn.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ESTADO_FINALIZADO);
                ps.setDouble(2, pago_efectivo);
                ps.setDouble(3, pago_transaccion);
                if (idCierre != null) {
                    ps.setInt(4, idCierre);
                } else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setInt(5, id_pedido);
                ps.executeUpdate();
            }

            descontarStockDePedido(id_pedido);
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Error al finalizar pedido con pago: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Descuenta del inventario los componentes consumidos por el pedido.
     */
    public void descontarStockDePedido(int id_pedido) {
        String sql = "SELECT nombre, cantidad FROM detalle_pedidos WHERE id_pedido = ?";
        InventarioDao invDao = new InventarioDao();
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return;
            ps.setInt(1, id_pedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String item = rs.getString("nombre");
                    double cant = rs.getDouble("cantidad");
                    invDao.descontarStockPorNombre(item, cant, "Caja/Venta");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al descontar stock para el pedido " + id_pedido + ": " + e.getMessage());
        }
    }

    /**
     * Lista todos los pedidos registrados ordenados descendentemente por fecha.
     */
    public List<Pedido> listarPedidos() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id ORDER BY p.fecha DESC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null) {
                while (rs.next()) {
                    Pedido ped = new Pedido();
                    ped.setId(rs.getInt("id"));
                    ped.setSala(rs.getString("nombre"));
                    ped.setNum_mesa(rs.getInt("num_mesa"));
                    ped.setFecha(rs.getString("fecha"));
                    ped.setTotal(rs.getDouble("total"));
                    ped.setUsuario(rs.getString("usuario"));
                    ped.setEstado(rs.getString("estado"));
                    lista.add(ped);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pedidos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra una línea de detalle a un pedido y recalcula el total acumulado.
     */
    public boolean RegistrarDetalle(DetallePedido det) {
        String sql = "INSERT INTO detalle_pedidos (nombre, precio, cantidad, comentario, id_pedido) VALUES (?,?,?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, det.getNombre());
            ps.setDouble(2, det.getPrecio());
            ps.setInt(3, det.getCantidad());
            ps.setString(4, det.getComentario());
            ps.setInt(5, det.getId_pedido());
            ps.executeUpdate();
            return actualizarTotalPedido(det.getId_pedido());
        } catch (SQLException e) {
            System.err.println("Error al registrar detalle de pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un ítem específico de la comanda de un pedido.
     */
    public boolean eliminarDetalle(int idDetalle) {
        String sqlSelect = "SELECT id_pedido FROM detalle_pedidos WHERE id = ?";
        int idPedido = 0;
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sqlSelect) : null) {
            if (ps == null) return false;
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idPedido = rs.getInt("id_pedido");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar detalle para eliminar: " + e.getMessage());
            return false;
        }

        if (idPedido != 0) {
            String sqlDelete = "DELETE FROM detalle_pedidos WHERE id = ?";
            try (Connection con = cn.getConnection();
                 PreparedStatement ps = con != null ? con.prepareStatement(sqlDelete) : null) {
                if (ps == null) return false;
                ps.setInt(1, idDetalle);
                ps.executeUpdate();
                return actualizarTotalPedido(idPedido);
            } catch (SQLException e) {
                System.err.println("Error al eliminar detalle de pedido: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Recalcula y actualiza la suma total del pedido.
     */
    public boolean actualizarTotalPedido(int idPedido) {
        String sqlSelect = "SELECT SUM(cantidad * precio) as total FROM detalle_pedidos WHERE id_pedido = ? AND nombre NOT IN ('PAGO EFECTIVO', 'PAGO TRANSACCION')";
        double nuevoTotal = 0.0;
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sqlSelect) : null) {
            if (ps != null) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nuevoTotal = rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total del pedido: " + e.getMessage());
            return false;
        }

        String sqlUpdate = "UPDATE pedidos SET total = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sqlUpdate) : null) {
            if (ps == null) return false;
            ps.setDouble(1, nuevoTotal);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar total del pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un pedido y todas sus dependencias en detalles e historial.
     */
    public boolean eliminarPedidoPorId(int idPedido) {
        try (Connection con = cn.getConnection()) {
            if (con == null) return false;
            try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM historial_ventas WHERE id_pedido = ?");
                 PreparedStatement ps2 = con.prepareStatement("DELETE FROM detalle_pedidos WHERE id_pedido = ?");
                 PreparedStatement ps3 = con.prepareStatement("DELETE FROM pedidos WHERE id = ?")) {
                ps1.setInt(1, idPedido);
                ps1.executeUpdate();
                ps2.setInt(1, idPedido);
                ps2.executeUpdate();
                ps3.setInt(1, idPedido);
                return ps3.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera un reporte PDF diario con todas las ventas del turno.
     */
    public void generarReporteDiario() {
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

        double[] totales = calcularTotalesDia(fechaInicio, fechaFin, 0);
        double totalEfectivo = totales[0];
        double totalTransaccion = totales[1];
        double totalGeneral = 0.0;

        String sqlTotal = "SELECT SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sqlTotal) : null) {
            if (ps != null) {
                ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
                ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalGeneral = rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total general para reporte: " + e.getMessage());
        }

        try {
            String appDataPath = System.getenv("APPDATA");
            if (appDataPath == null) appDataPath = System.getProperty("java.io.tmpdir");
            String fechaStr = new SimpleDateFormat("MM-dd-yyyy").format(now.getTime());
            File fechaDir = new File(new File(appDataPath, ".reporte_oculto"), fechaStr);
            if (!fechaDir.exists()) fechaDir.mkdirs();

            File salida = new File(fechaDir, fechaStr + ".pdf");
            try (FileOutputStream archivo = new FileOutputStream(salida)) {
                Document doc = new Document();
                PdfWriter.getInstance(doc, archivo);
                doc.open();

                Font tituloFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLACK);
                Paragraph titulo = new Paragraph("Historial de Pedidos del Día - Todas las Salas", tituloFont);
                titulo.setAlignment(1);
                doc.add(titulo);
                doc.add(Chunk.NEWLINE);

                PdfPTable tabla = new PdfPTable(6);
                tabla.setWidthPercentage(100);
                tabla.setWidths(new float[]{10f, 20f, 10f, 25f, 20f, 15f});
                Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
                String[] headers = {"ID", "Sala", "Mesa", "Fecha", "Usuario", "Total"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    tabla.addCell(cell);
                }

                Font dataFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.BLACK);
                String sql = "SELECT p.id, s.nombre AS sala, p.num_mesa, p.fecha, p.usuario, p.total "
                        + "FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id "
                        + "WHERE p.estado = 'FINALIZADO' AND p.fecha BETWEEN ? AND ? ORDER BY p.fecha";
                boolean hayDatos = false;
                try (Connection con = cn.getConnection();
                     PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
                    if (ps != null) {
                        ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
                        ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                hayDatos = true;
                                tabla.addCell(new Phrase(String.valueOf(rs.getInt("id")), dataFont));
                                tabla.addCell(new Phrase(rs.getString("sala"), dataFont));
                                tabla.addCell(new Phrase(String.valueOf(rs.getInt("num_mesa")), dataFont));
                                tabla.addCell(new Phrase(rs.getString("fecha"), dataFont));
                                tabla.addCell(new Phrase(rs.getString("usuario"), dataFont));
                                tabla.addCell(new Phrase(String.format("%.2f COP", rs.getDouble("total")), dataFont));
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error al consultar pedidos para tabla PDF: " + e.getMessage());
                }

                if (!hayDatos) {
                    doc.add(new Paragraph("No se encontraron pedidos finalizados en este rango de tiempo.", dataFont));
                } else {
                    doc.add(tabla);
                }

                doc.add(Chunk.NEWLINE);
                Paragraph totalesParrafo = new Paragraph();
                totalesParrafo.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK));
                totalesParrafo.add(String.format("Total Efectivo: COP %.2f\n", totalEfectivo));
                totalesParrafo.add(String.format("Total Transacción: COP %.2f\n", totalTransaccion));
                totalesParrafo.add(String.format("Total General: COP %.2f\n", totalGeneral));
                if (Math.abs((totalEfectivo + totalTransaccion) - totalGeneral) > 0.01) {
                    totalesParrafo.add(new Phrase("Advertencia: La suma de efectivo y transacción no coincide con el total general.",
                            new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED)));
                }
                totalesParrafo.setAlignment(2);
                doc.add(totalesParrafo);

                doc.close();
            }
        } catch (DocumentException | IOException e) {
            System.err.println("Error al generar reporte diario PDF: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al generar el reporte diario: " + e.getMessage());
        }
    }

    /**
     * Lista los pedidos procesados en una jornada entre dos marcas de tiempo.
     */
    public List<Pedido> listarPedidosDelDia(Timestamp fechaInicio, Timestamp fechaFin) {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT p.id, s.nombre AS sala, p.num_mesa, p.fecha, p.total, p.usuario, p.estado, p.pago_efectivo, p.pago_transaccion "
                + "FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id "
                + "WHERE p.fecha BETWEEN ? AND ? AND p.estado IN ('FINALIZADO', 'PENDIENTE') ORDER BY p.fecha";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return lista;
            ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido ped = new Pedido();
                    ped.setId(rs.getInt("id"));
                    ped.setSala(rs.getString("sala"));
                    ped.setNum_mesa(rs.getInt("num_mesa"));
                    ped.setFecha(rs.getString("fecha"));
                    ped.setTotal(rs.getDouble("total"));
                    ped.setUsuario(rs.getString("usuario"));
                    ped.setEstado(rs.getString("estado"));
                    ped.setPago_efectivo(rs.getDouble("pago_efectivo"));
                    ped.setPago_transaccion(rs.getDouble("pago_transaccion"));
                    lista.add(ped);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pedidos del día: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcula la suma acumulada de efectivo y pagos electrónicos en un turno.
     */
    public double[] calcularTotalesDia(Timestamp fechaInicio, Timestamp fechaFin, int idSala) {
        double totalEfectivo = 0.0;
        double totalTransaccion = 0.0;
        String sql = "SELECT SUM(pago_efectivo) AS efectivo, SUM(pago_transaccion) AS transaccion FROM pedidos "
                + "WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ?" + (idSala != 0 ? " AND id_sala = ?" : "");
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return new double[]{0.0, 0.0};
            ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            if (idSala != 0) {
                ps.setInt(3, idSala);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalEfectivo = rs.getDouble("efectivo");
                    totalTransaccion = rs.getDouble("transaccion");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular totales del día: " + e.getMessage());
        }
        return new double[]{totalEfectivo, totalTransaccion};
    }

    /**
     * Traslada un pedido a una nueva sala.
     */
    public boolean actualizarSalaPedido(int idPedido, int idSala) {
        String sql = "UPDATE pedidos SET id_sala = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, idSala);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar sala del pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si una mesa se encuentra ocupada por un pedido pendiente distinto.
     */
    public boolean mesaOcupada(int idSala, int numMesa, int idPedidoActual) {
        String sql = "SELECT id FROM pedidos WHERE id_sala = ? AND num_mesa = ? AND estado = 'Pendiente' AND id != ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, idSala);
            ps.setInt(2, numMesa);
            ps.setInt(3, idPedidoActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar estado de mesa ocupada: " + e.getMessage());
            return false;
        }
    }

    /**
     * Agrupa el acumulado de ventas por hora dentro de un intervalo de fechas.
     */
    public Map<String, Double> obtenerVentasPorHora(String fechaInicio, String fechaFin) {
        Map<String, Double> ventas = new LinkedHashMap<>();
        String sql = "SELECT strftime('%H', fecha) AS hora, SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ? GROUP BY hora ORDER BY hora";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return ventas;
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.put(rs.getString("hora") + ":00", rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasPorHora: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Obtiene el acumulado diario de ventas finalizadas de los últimos 7 días.
     */
    public Map<String, Double> obtenerVentasUltimos7Dias() {
        Map<String, Double> ventas = new LinkedHashMap<>();
        String sql = "SELECT DATE(fecha) AS dia, SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' GROUP BY dia ORDER BY dia DESC LIMIT 7";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null) {
                List<String> dias = new ArrayList<>();
                List<Double> totales = new ArrayList<>();
                while (rs.next()) {
                    dias.add(rs.getString("dia"));
                    totales.add(rs.getDouble("total"));
                }
                for (int i = dias.size() - 1; i >= 0; i--) {
                    ventas.put(dias.get(i), totales.get(i));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasUltimos7Dias: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Obtiene el top 5 de platos con mayores ventas.
     */
    public List<Object[]> obtenerPlatosMasVendidos(String fechaInicio, String fechaFin) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT dp.nombre, SUM(dp.cantidad) AS total_vendido FROM detalle_pedidos dp INNER JOIN pedidos p ON dp.id_pedido = p.id WHERE p.estado = 'FINALIZADO' AND p.fecha BETWEEN ? AND ? GROUP BY dp.nombre ORDER BY total_vendido DESC LIMIT 5";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps != null) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new Object[]{rs.getString("nombre"), rs.getInt("total_vendido")});
                    }
                }
            }
            if (lista.isEmpty()) {
                String sqlFallback = "SELECT dp.nombre, SUM(dp.cantidad) AS total_vendido FROM detalle_pedidos dp INNER JOIN pedidos p ON dp.id_pedido = p.id WHERE p.estado = 'FINALIZADO' AND p.fecha >= datetime('now', '-30 days') GROUP BY dp.nombre ORDER BY total_vendido DESC LIMIT 5";
                try (Connection con2 = cn.getConnection();
                     PreparedStatement psFallback = con2 != null ? con2.prepareStatement(sqlFallback) : null;
                     ResultSet rs = psFallback != null ? psFallback.executeQuery() : null) {
                    if (rs != null) {
                        while (rs.next()) {
                            lista.add(new Object[]{rs.getString("nombre"), rs.getInt("total_vendido")});
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerPlatosMasVendidos: " + e.getMessage());
        }
        return lista;
    }
}
