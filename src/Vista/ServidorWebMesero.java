package Vista;

import Modelo.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Servidor Web embebido ultraligero y de alto rendimiento para el POS Karoll Café y Onces.
 * - Diseño móvil gastronómico artesanal único (Mobile First / PWA feel)
 * - Sincronización en tiempo real de estados de mesas (Libre, Ocupada, Preparada)
 * - Identificación precisa de Salones, Mesas y Domicilios
 * - Desglose de Rondas independientes y multi-comanda
 * - KDS de Cocina en vivo (/cocina) y Menú Digital QR (/menu)
 */
public class ServidorWebMesero {

    private static HttpServer server;
    private static boolean corriendo = false;
    private static int puerto = 8080;

    public static void iniciar() {
        if (corriendo) return;
        try {
            limpiarPedidosVacios();
            asegurarReglaFirewall();
            server = HttpServer.create(new InetSocketAddress(puerto), 0);
            
            // Rutas de Vistas Web
            server.createContext("/", ex -> manejarRaiz(ex));
            server.createContext("/cocina", ex -> manejarCocina(ex));
            server.createContext("/menu", ex -> manejarMenuCliente(ex));
            
            // APIs Backend
            server.createContext("/api/info", ex -> manejarInfo(ex));
            server.createContext("/api/salas", ex -> manejarSalas(ex));
            server.createContext("/api/mesas-estado", ex -> manejarMesasEstado(ex));
            server.createContext("/api/mesa-detalle", ex -> manejarMesaDetalle(ex));
            server.createContext("/api/carta", ex -> manejarCarta(ex));
            server.createContext("/api/enviar-comanda", ex -> manejarEnviarComanda(ex));
            server.createContext("/api/preparar", ex -> manejarMarcarPreparado(ex));
            server.createContext("/api/trasladar-mesa", ex -> manejarTrasladarMesa(ex));
            server.createContext("/api/comandas-cocina", ex -> manejarComandasCocina(ex));
            server.createContext("/api/historial", ex -> manejarHistorial(ex));
            server.createContext("/api/resumen-turno", ex -> manejarResumenTurno(ex));
            
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            corriendo = true;
            System.out.println("[ServidorWeb] ✅ Servidor web iniciado en puerto " + puerto);
            System.out.println("[ServidorWeb] 📱 Meseros: http://" + getIpLocal() + ":" + puerto);
            System.out.println("[ServidorWeb] 🍳 Cocina:  http://" + getIpLocal() + ":" + puerto + "/cocina");
            System.out.println("[ServidorWeb] 📲 Menú QR: http://" + getIpLocal() + ":" + puerto + "/menu");
        } catch (Exception e) {
            System.err.println("[ServidorWeb] ❌ Error al iniciar servidor: " + e.getMessage());
        }
    }

    private static void limpiarPedidosVacios() {
        try {
            Conexion cn = new Conexion();
            java.sql.Connection con = cn.getConnection();
            if (con != null) {
                String sql = "DELETE FROM pedidos WHERE total=0 AND id NOT IN (SELECT DISTINCT id_pedido FROM detalle_pedidos)";
                java.sql.PreparedStatement ps = con.prepareStatement(sql);
                ps.executeUpdate();
                ps.close();
                con.close();
            }
        } catch (Exception ignored) {}
    }

    private static void asegurarReglaFirewall() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                String cmd = "netsh advfirewall firewall add rule name=\"Karoll Cafe y Onces POS - Panel Android\" dir=in action=allow protocol=TCP localport=" + puerto;
                Runtime.getRuntime().exec(cmd);
            }
        } catch (Exception ignored) {}
    }

    public static void detener() {
        if (server != null) {
            server.stop(0);
            corriendo = false;
        }
    }

    public static boolean isCorriendo() { return corriendo; }
    
    public static String getUrlAcceso() {
        return corriendo ? "http://" + getIpLocal() + ":" + puerto : "No iniciado";
    }

    public static int getPuerto() { return puerto; }

    // ─────────────────────────────────────────────────────────────────────────
    // VISTAS HTML
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarRaiz(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/cocina")) { manejarCocina(ex); return; }
        if (path.equals("/menu")) { manejarMenuCliente(ex); return; }
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        enviarHtml(ex, buildHtmlMeseros());
    }

    private static void manejarCocina(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        enviarHtml(ex, buildHtmlCocina());
    }

    private static void manejarMenuCliente(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        enviarHtml(ex, buildHtmlMenuCliente());
    }

    private static void enviarHtml(HttpExchange ex, String html) throws IOException {
        byte[] resp = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, resp.length);
        ex.getResponseBody().write(resp);
        ex.getResponseBody().close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APIS BACKEND
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarInfo(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            LoginDao logDao = new LoginDao();
            Config cfg = logDao.datosEmpresa();
            String nombreNegocio = (cfg != null && cfg.getNombre() != null) ? cfg.getNombre() : "Karoll Café y Onces";
            CajaDao cajaDao = new CajaDao();
            boolean cajaAbierta = cajaDao.hayCajaAbierta();
            
            List<Login> usuarios = logDao.ListarUsuarios();
            StringBuilder sbUsers = new StringBuilder("[");
            for (int i = 0; i < usuarios.size(); i++) {
                Login u = usuarios.get(i);
                if (i > 0) sbUsers.append(",");
                sbUsers.append("{\"id\":").append(u.getId())
                       .append(",\"nombre\":\"").append(escape(u.getNombre())).append("\"")
                       .append(",\"rol\":\"").append(escape(u.getRol())).append("\"}");
            }
            sbUsers.append("]");

            String json = "{\"nombre\":\"" + escape(nombreNegocio) + "\",\"caja_abierta\":" + cajaAbierta + ",\"usuarios\":" + sbUsers.toString() + "}";
            responderJson(ex, 200, json);
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarSalas(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            SalasDao slDao = new SalasDao();
            List<Sala> salas = slDao.Listar();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < salas.size(); i++) {
                Sala s = salas.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(s.getId())
                  .append(",\"nombre\":\"").append(escape(s.getNombre())).append("\"")
                  .append(",\"mesas\":").append(s.getMesas())
                  .append("}");
            }
            sb.append("]");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarMesasEstado(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
        int idSala = Integer.parseInt(params.getOrDefault("id_sala", "1"));
        try {
            PedidosDao pedDao = new PedidosDao();
            CajaDao cajaDao = new CajaDao();
            boolean cajaAbierta = cajaDao.hayCajaAbierta();
            Map<Integer, String[]> mapa = pedDao.getMesasOcupadasConEstado(idSala);
            StringBuilder sb = new StringBuilder("{\"caja_abierta\":").append(cajaAbierta).append(",\"mesas\":{");
            boolean first = true;
            for (Map.Entry<Integer, String[]> entry : mapa.entrySet()) {
                int numMesa = entry.getKey();
                String[] info = entry.getValue();
                int idPed = Integer.parseInt(info[0]);
                String estado = info[1];
                List<Pedido> rondas = pedDao.getRondasMesa(numMesa, idSala);
                double totalMesa = 0.0;
                String horaInicio = "";
                String mesero = "Mesero";
                if (!rondas.isEmpty()) {
                    for (Pedido r : rondas) totalMesa += r.getTotal();
                    horaInicio = rondas.get(0).getFecha() != null ? rondas.get(0).getFecha() : "";
                    mesero = rondas.get(0).getUsuario() != null ? rondas.get(0).getUsuario() : "Mesero";
                }

                if (!first) sb.append(",");
                sb.append("\"").append(numMesa).append("\":{")
                  .append("\"id_pedido\":").append(idPed).append(",")
                  .append("\"estado\":\"").append(escape(estado)).append("\",")
                  .append("\"rondas\":").append(rondas.size()).append(",")
                  .append("\"hora\":\"").append(escape(horaInicio)).append("\",")
                  .append("\"mesero\":\"").append(escape(mesero)).append("\",")
                  .append("\"total\":").append(totalMesa)
                  .append("}");
                first = false;
            }
            sb.append("}}");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarMesaDetalle(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
        int numMesa = Integer.parseInt(params.getOrDefault("mesa", "1"));
        int idSala = Integer.parseInt(params.getOrDefault("sala", "1"));
        try {
            PedidosDao pedDao = new PedidosDao();
            List<Pedido> rondas = pedDao.getRondasMesa(numMesa, idSala);
            double granTotal = 0.0;
            StringBuilder sb = new StringBuilder("{\"mesa\":").append(numMesa).append(",\"sala\":").append(idSala).append(",\"rondas\":[");
            for (int rIdx = 0; rIdx < rondas.size(); rIdx++) {
                Pedido r = rondas.get(rIdx);
                granTotal += r.getTotal();
                List<DetallePedido> detalles = pedDao.verPedidoDetalle(r.getId());
                if (rIdx > 0) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(r.getId()).append(",")
                  .append("\"rondaNum\":").append(rIdx + 1).append(",")
                  .append("\"fecha\":\"").append(escape(r.getFecha() != null ? r.getFecha() : "")).append("\",")
                  .append("\"estado\":\"").append(escape(r.getEstado() != null ? r.getEstado() : "PENDIENTE")).append("\",")
                  .append("\"usuario\":\"").append(escape(r.getUsuario() != null ? r.getUsuario() : "Mesero")).append("\",")
                  .append("\"total\":").append(r.getTotal()).append(",")
                  .append("\"items\":[");
                for (int dIdx = 0; dIdx < detalles.size(); dIdx++) {
                    DetallePedido d = detalles.get(dIdx);
                    if (dIdx > 0) sb.append(",");
                    sb.append("{")
                      .append("\"id\":").append(d.getId()).append(",")
                      .append("\"nombre\":\"").append(escape(d.getNombre())).append("\",")
                      .append("\"precio\":").append(d.getPrecio()).append(",")
                      .append("\"cantidad\":").append(d.getCantidad()).append(",")
                      .append("\"comentario\":\"").append(escape(d.getComentario() != null ? d.getComentario() : "")).append("\"")
                      .append("}");
                }
                sb.append("]}");
            }
            sb.append("],\"total\":").append(granTotal).append("}");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarCarta(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            PlatosDao plaDao = new PlatosDao();
            List<Plato> platos = plaDao.Listar("");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < platos.size(); i++) {
                Plato p = platos.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(p.getId())
                  .append(",\"nombre\":\"").append(escape(p.getNombre())).append("\"")
                  .append(",\"precio\":").append(p.getPrecio())
                  .append("}");
            }
            sb.append("]");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarEnviarComanda(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { ex.sendResponseHeaders(405, -1); return; }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            CajaDao cajaDao = new CajaDao();
            if (!cajaDao.hayCajaAbierta()) {
                responderJson(ex, 400, "{\"error\":\"La caja se encuentra CERRADA. Solicite abrir la caja antes de registrar pedidos.\"}");
                return;
            }

            int sala = 1, mesa = 1, idPedido = 0;
            String mesero = "Mesero Móvil";
            boolean forzarNuevaRonda = false;

            Map<String, String> root = parseJson(body);
            if (root.containsKey("sala")) sala = Integer.parseInt(root.get("sala"));
            if (root.containsKey("mesa")) mesa = Integer.parseInt(root.get("mesa"));
            if (root.containsKey("id_pedido")) idPedido = Integer.parseInt(root.get("id_pedido"));
            if (root.containsKey("mesero") && !root.get("mesero").isEmpty()) mesero = root.get("mesero");
            if (root.containsKey("nueva_ronda")) forzarNuevaRonda = Boolean.parseBoolean(root.get("nueva_ronda"));

            List<DetallePedido> listaItems = new ArrayList<>();
            int idxItems = body.indexOf("\"items\":[");
            if (idxItems != -1) {
                int endIdx = body.lastIndexOf("]");
                if (endIdx > idxItems) {
                    String itemsStr = body.substring(idxItems + 8, endIdx + 1).trim();
                    if (itemsStr.startsWith("[") && itemsStr.endsWith("]")) {
                        itemsStr = itemsStr.substring(1, itemsStr.length() - 1).trim();
                        if (!itemsStr.isEmpty()) {
                            String[] itemBlocks = itemsStr.split("\\},\\{");
                            for (String b : itemBlocks) {
                                String clean = b.replace("{", "").replace("}", "");
                                Map<String, String> mapItm = parseJson("{" + clean + "}");
                                DetallePedido det = new DetallePedido();
                                det.setNombre(mapItm.getOrDefault("nombre", ""));
                                det.setPrecio(Double.parseDouble(mapItm.getOrDefault("precio", "0")));
                                det.setCantidad(Integer.parseInt(mapItm.getOrDefault("cantidad", "1")));
                                det.setComentario(mapItm.getOrDefault("comentario", ""));
                                if (!det.getNombre().isEmpty()) {
                                    listaItems.add(det);
                                }
                            }
                        }
                    }
                }
            }

            if (listaItems.isEmpty()) {
                responderJson(ex, 400, "{\"error\":\"La comanda está vacía.\"}");
                return;
            }

            PedidosDao pedDao = new PedidosDao();

            if (!forzarNuevaRonda && idPedido <= 0) {
                idPedido = pedDao.verificarStado(mesa, sala);
            }

            if (forzarNuevaRonda || idPedido <= 0) {
                Pedido ped = new Pedido();
                ped.setId_sala(sala);
                ped.setNum_mesa(mesa);
                ped.setTotal(0);
                ped.setUsuario(mesero);
                ped.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                ped.setEstado("PENDIENTE");
                idPedido = pedDao.RegistrarPedido(ped);
            }

            if (idPedido <= 0) {
                responderJson(ex, 500, "{\"error\":\"No se pudo registrar el pedido. Verifique que la caja esté abierta.\"}");
                return;
            }

            for (DetallePedido itm : listaItems) {
                itm.setId_pedido(idPedido);
                pedDao.RegistrarDetalle(itm);
            }

            Pedido pedActualizado = pedDao.verPedido(idPedido);
            double totalFinal = pedActualizado != null ? pedActualizado.getTotal() : 0.0;

            responderJson(ex, 200, "{\"ok\":true,\"id_pedido\":" + idPedido + ",\"total\":" + totalFinal + ",\"mensaje\":\"Comanda enviada a cocina\"}");
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"Error al procesar comanda: " + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarMarcarPreparado(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { ex.sendResponseHeaders(405, -1); return; }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseJson(body);
        int idPedido = Integer.parseInt(params.getOrDefault("id_pedido", "0"));

        if (idPedido <= 0) {
            responderJson(ex, 400, "{\"error\":\"id_pedido inválido\"}");
            return;
        }

        PedidosDao pedDao = new PedidosDao();
        if (pedDao.marcarPreparado(idPedido)) {
            responderJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Ronda marcada como PREPARADA\"}");
        } else {
            responderJson(ex, 500, "{\"error\":\"Error al marcar como preparada\"}");
        }
    }

    private static void manejarTrasladarMesa(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { ex.sendResponseHeaders(405, -1); return; }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseJson(body);
        int mesaOrigen = Integer.parseInt(params.getOrDefault("mesa_origen", "0"));
        int salaOrigen = Integer.parseInt(params.getOrDefault("sala_origen", "0"));
        int mesaDestino = Integer.parseInt(params.getOrDefault("mesa_destino", "0"));
        int salaDestino = Integer.parseInt(params.getOrDefault("sala_destino", "0"));

        if (mesaOrigen <= 0 || mesaDestino <= 0 || salaOrigen <= 0 || salaDestino <= 0) {
            responderJson(ex, 400, "{\"error\":\"Parámetros de traslado incompletos\"}");
            return;
        }

        try {
            Conexion cn = new Conexion();
            java.sql.Connection con = cn.getConnection();
            if (con != null) {
                String sql = "UPDATE pedidos SET num_mesa = ?, id_sala = ? WHERE num_mesa = ? AND id_sala = ? AND UPPER(TRIM(estado)) IN ('PENDIENTE', 'PREPARADO')";
                java.sql.PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, mesaDestino);
                ps.setInt(2, salaDestino);
                ps.setInt(3, mesaOrigen);
                ps.setInt(4, salaOrigen);
                int filas = ps.executeUpdate();
                ps.close();
                con.close();

                if (filas > 0) {
                    responderJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Mesa trasladada exitosamente\"}");
                } else {
                    responderJson(ex, 400, "{\"error\":\"No se encontraron pedidos activos para trasladar\"}");
                }
            } else {
                responderJson(ex, 500, "{\"error\":\"Sin conexión a base de datos\"}");
            }
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarComandasCocina(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            PedidosDao pedDao = new PedidosDao();
            SalasDao slDao = new SalasDao();
            Map<Integer, String> nombresSalas = new HashMap<>();
            for (Sala s : slDao.Listar()) nombresSalas.put(s.getId(), s.getNombre());

            List<Pedido> lista = pedDao.listarPedidosCocina();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Pedido p = lista.get(i);
                List<DetallePedido> detalles = pedDao.verPedidoDetalle(p.getId());
                String salaNom = nombresSalas.getOrDefault(p.getId_sala(), "Salón " + p.getId_sala());
                if (i > 0) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(p.getId()).append(",")
                  .append("\"mesa\":").append(p.getNum_mesa()).append(",")
                  .append("\"sala\":\"").append(escape(salaNom)).append("\",")
                  .append("\"usuario\":\"").append(escape(p.getUsuario() != null ? p.getUsuario() : "Mesero")).append("\",")
                  .append("\"fecha\":\"").append(escape(p.getFecha() != null ? p.getFecha() : "")).append("\",")
                  .append("\"estado\":\"").append(escape(p.getEstado() != null ? p.getEstado() : "PENDIENTE")).append("\",")
                  .append("\"items\":[");
                for (int d = 0; d < detalles.size(); d++) {
                    DetallePedido det = detalles.get(d);
                    if (d > 0) sb.append(",");
                    sb.append("{")
                      .append("\"nombre\":\"").append(escape(det.getNombre())).append("\",")
                      .append("\"cantidad\":").append(det.getCantidad()).append(",")
                      .append("\"comentario\":\"").append(escape(det.getComentario() != null ? det.getComentario() : "")).append("\"")
                      .append("}");
                }
                sb.append("]}");
            }
            sb.append("]");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarHistorial(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            PedidosDao pedDao = new PedidosDao();
            SalasDao slDao = new SalasDao();
            Map<Integer, String> nombresSalas = new HashMap<>();
            for (Sala s : slDao.Listar()) nombresSalas.put(s.getId(), s.getNombre());

            List<Pedido> lista = pedDao.listarPedidos();
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (Pedido p : lista) {
                if (count >= 30) break;
                String salaNom = nombresSalas.getOrDefault(p.getId_sala(), "Salón " + p.getId_sala());
                if (count > 0) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(p.getId()).append(",")
                  .append("\"mesa\":").append(p.getNum_mesa()).append(",")
                  .append("\"sala\":\"").append(escape(salaNom)).append("\",")
                  .append("\"usuario\":\"").append(escape(p.getUsuario() != null ? p.getUsuario() : "Mesero")).append("\",")
                  .append("\"fecha\":\"").append(escape(p.getFecha() != null ? p.getFecha() : "")).append("\",")
                  .append("\"estado\":\"").append(escape(p.getEstado() != null ? p.getEstado() : "")).append("\",")
                  .append("\"total\":").append(p.getTotal())
                  .append("}");
                count++;
            }
            sb.append("]");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void manejarResumenTurno(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            PedidosDao pedDao = new PedidosDao();
            List<Pedido> todos = pedDao.listarPedidos();
            double totalVentas = 0.0;
            int activas = 0;
            int finalizados = 0;
            for (Pedido p : todos) {
                String st = (p.getEstado() != null) ? p.getEstado().trim().toUpperCase() : "";
                if ("FINALIZADO".equals(st)) {
                    totalVentas += p.getTotal();
                    finalizados++;
                } else if ("PENDIENTE".equals(st) || "PREPARADO".equals(st)) {
                    activas++;
                }
            }
            String json = "{\"totalVentas\":" + totalVentas + ",\"comandasActivas\":" + activas + ",\"comandasFinalizadas\":" + finalizados + "}";
            responderJson(ex, 200, json);
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FRONTEND: Panel Móvil Meseros (Diseño Gastronómico Exclusivo)
    // ─────────────────────────────────────────────────────────────────────────
    private static String buildHtmlMeseros() {
        return "<!DOCTYPE html><html lang='es'><head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
            "<meta name='theme-color' content='#0f1015'>" +
            "<meta name='apple-mobile-web-app-capable' content='yes'>" +
            "<title>Karoll Café — Panel Mesero</title>" +
            "<link rel='preconnect' href='https://fonts.googleapis.com'>" +
            "<link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap' rel='stylesheet'>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box;-webkit-tap-highlight-color:transparent}" +
            ":root{" +
            "  --bg:#0f1015;--surface:#181920;--surface-card:#20222a;--border:#2d313b;--border-light:#3b404d;" +
            "  --amber:#f59e0b;--amber-dark:#d97706;--amber-glow:rgba(245,158,11,0.18);" +
            "  --emerald:#10b981;--emerald-dark:#059669;--emerald-glow:rgba(16,185,129,0.15);" +
            "  --rose:#f43f5e;--rose-dark:#e11d48;--rose-glow:rgba(244,63,94,0.15);" +
            "  --sky:#38bdf8;--sky-glow:rgba(56,189,248,0.15);" +
            "  --text:#f8fafc;--text-muted:#94a3b8;--text-dim:#64748b;" +
            "}" +
            "body{font-family:'Plus Jakarta Sans',-apple-system,sans-serif;background:var(--bg);color:var(--text);min-height:100vh;display:flex;flex-direction:column;overflow-x:hidden;padding-bottom:80px;user-select:none}" +
            
            // Header Top Bar
            ".top-bar{background:rgba(15,16,21,0.96);backdrop-filter:blur(16px);border-bottom:1px solid var(--border);padding:12px 16px;display:flex;align-items:center;justify-content:space-between;position:sticky;top:0;z-index:100}" +
            ".brand-box{display:flex;align-items:center;gap:10px}" +
            ".brand-icon{width:36px;height:36px;border-radius:10px;background:linear-gradient(135deg,var(--amber),#b45309);display:flex;align-items:center;justify-content:center;color:#0f1015;font-weight:800;font-size:18px;box-shadow:0 4px 12px var(--amber-glow)}" +
            ".brand-text h1{font-size:15px;font-weight:800;color:var(--text);line-height:1.1}" +
            ".brand-text span{font-size:11px;font-weight:600;color:var(--amber)}" +
            ".top-actions{display:flex;align-items:center;gap:8px}" +
            ".caja-badge{padding:4px 9px;border-radius:20px;font-size:11px;font-weight:700;display:flex;align-items:center;gap:5px}" +
            ".caja-badge.abierta{background:var(--emerald-glow);color:var(--emerald);border:1px solid rgba(16,185,129,0.3)}" +
            ".caja-badge.cerrada{background:var(--rose-glow);color:var(--rose);border:1px solid rgba(244,63,94,0.3)}" +
            ".user-pill{background:var(--surface);border:1px solid var(--border);color:var(--text);padding:5px 12px;border-radius:20px;font-size:12px;font-weight:700;cursor:pointer;display:flex;align-items:center;gap:6px}" +
            ".user-pill:active{border-color:var(--amber)}" +

            // Main Content Area
            ".main-content{padding:14px 16px;max-width:540px;margin:0 auto;width:100%}" +
            ".section-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}" +
            ".section-title{font-size:12px;font-weight:800;color:var(--text-dim);letter-spacing:1px;text-transform:uppercase}" +

            // Salones Pills
            ".salones-scroll{display:flex;gap:8px;overflow-x:auto;padding-bottom:6px;margin-bottom:14px;-webkit-overflow-scrolling:touch}" +
            ".salones-scroll::-webkit-scrollbar{display:none}" +
            ".salon-tab{background:var(--surface);border:1.5px solid var(--border);color:var(--text-muted);padding:8px 16px;border-radius:12px;font-size:13px;font-weight:700;white-space:nowrap;cursor:pointer;transition:all .15s}" +
            ".salon-tab.active{background:var(--amber);border-color:var(--amber);color:#0f1015;box-shadow:0 4px 14px var(--amber-glow)}" +

            // Mesas Grid
            ".mesas-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px}" +
            ".mesa-card{background:var(--surface-card);border:1.5px solid var(--border);border-radius:16px;padding:14px 10px;text-align:center;cursor:pointer;position:relative;overflow:hidden;transition:all .15s}" +
            ".mesa-card:active{transform:scale(0.96)}" +
            ".mesa-card.libre{border-color:rgba(16,185,129,0.4);background:linear-gradient(180deg,rgba(16,185,129,0.08),transparent)}" +
            ".mesa-card.ocupada{border-color:rgba(244,63,94,0.5);background:linear-gradient(180deg,rgba(244,63,94,0.12),transparent)}" +
            ".mesa-card.preparada{border-color:rgba(56,189,248,0.5);background:linear-gradient(180deg,rgba(56,189,248,0.12),transparent)}" +
            ".mesa-num{font-size:18px;font-weight:800;margin-bottom:2px}" +
            ".mesa-status{font-size:11px;font-weight:700;border-radius:6px;padding:2px 6px;display:inline-block;margin-top:2px}" +
            ".mesa-card.libre .mesa-status{color:var(--emerald);background:var(--emerald-glow)}" +
            ".mesa-card.ocupada .mesa-status{color:var(--rose);background:var(--rose-glow)}" +
            ".mesa-card.preparada .mesa-status{color:var(--sky);background:var(--sky-glow)}" +
            ".mesa-total{font-size:12px;font-weight:800;color:var(--emerald);margin-top:4px}" +
            ".mesa-timer{font-size:10px;font-weight:600;color:var(--amber);margin-top:2px}" +

            // Search & Category Filters
            ".search-container{position:relative;margin-bottom:12px}" +
            ".search-container input{width:100%;background:var(--surface);border:1.5px solid var(--border);border-radius:14px;padding:12px 14px 12px 40px;color:#fff;font-size:14px;font-weight:600;outline:none;font-family:inherit}" +
            ".search-container input:focus{border-color:var(--amber)}" +
            ".search-container .search-icon{position:absolute;left:14px;top:50%;transform:translateY(-50%);color:var(--text-dim)}" +

            // Category Bar
            ".cats-scroll{display:flex;gap:8px;overflow-x:auto;padding-bottom:6px;margin-bottom:14px;-webkit-overflow-scrolling:touch}" +
            ".cats-scroll::-webkit-scrollbar{display:none}" +
            ".cat-pill{background:var(--surface);border:1px solid var(--border);color:var(--text-muted);padding:8px 14px;border-radius:20px;font-size:12px;font-weight:700;white-space:nowrap;cursor:pointer}" +
            ".cat-pill.active{background:var(--surface-card);border-color:var(--amber);color:var(--amber)}" +

            // Dish Items List
            ".dish-card{background:var(--surface-card);border:1px solid var(--border);border-radius:14px;padding:12px 14px;display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;cursor:pointer;transition:background .15s}" +
            ".dish-card:active{background:var(--surface);border-color:var(--border-light)}" +
            ".dish-info .nom{font-size:14px;font-weight:700;color:var(--text);margin-bottom:3px}" +
            ".dish-info .prc{font-size:14px;font-weight:800;color:var(--emerald)}" +
            ".btn-add-circle{width:36px;height:36px;border-radius:10px;background:var(--amber);color:#0f1015;border:none;font-size:18px;font-weight:800;display:flex;align-items:center;justify-content:center;cursor:pointer;box-shadow:0 3px 10px var(--amber-glow)}" +
            ".btn-add-circle:active{transform:scale(0.92)}" +

            // Floating Cart Bar
            ".cart-floating-bar{position:fixed;bottom:70px;left:16px;right:16px;max-width:508px;margin:0 auto;background:rgba(24,25,32,0.96);backdrop-filter:blur(16px);border:1.5px solid var(--amber);border-radius:16px;padding:12px 16px;display:none;align-items:center;justify-content:space-between;z-index:90;box-shadow:0 10px 25px rgba(0,0,0,0.6)}" +
            ".cart-floating-bar.show{display:flex}" +
            ".cart-info-text .title{font-size:13px;font-weight:800;color:#fff}" +
            ".cart-info-text .sub{font-size:11px;font-weight:600;color:var(--amber)}" +
            ".btn-view-cart{background:linear-gradient(135deg,var(--amber),var(--amber-dark));border:none;border-radius:12px;color:#0f1015;padding:10px 18px;font-size:13px;font-weight:800;cursor:pointer;font-family:inherit;box-shadow:0 4px 14px var(--amber-glow)}" +

            // Bottom Navigation Dock
            ".bottom-nav{position:fixed;bottom:0;left:0;right:0;background:rgba(15,16,21,0.98);backdrop-filter:blur(16px);border-top:1px solid var(--border);display:grid;grid-template-columns:repeat(4,1fr);height:64px;z-index:100}" +
            ".nav-item{display:flex;flex-direction:column;align-items:center;justify-content:center;color:var(--text-dim);font-size:11px;font-weight:700;gap:4px;cursor:pointer;border:none;background:none;font-family:inherit;position:relative}" +
            ".nav-item.active{color:var(--amber)}" +
            ".nav-item svg{width:20px;height:20px;stroke-width:2.2}" +
            ".nav-badge{position:absolute;top:6px;right:22%;background:var(--rose);color:#fff;font-size:10px;font-weight:800;padding:1px 5px;border-radius:10px}" +

            // Modal Bottom Sheet
            ".modal-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,0.75);backdrop-filter:blur(6px);z-index:200;align-items:flex-end;justify-content:center}" +
            ".modal-overlay.open{display:flex}" +
            ".sheet-box{background:var(--surface);border:1px solid var(--border);border-top-left-radius:24px;border-top-right-radius:24px;padding:22px 20px 30px 20px;width:100%;max-width:540px;max-height:85vh;overflow-y:auto;box-shadow:0 -10px 30px rgba(0,0,0,0.8)}" +
            ".sheet-handle{width:40px;height:4px;background:var(--border-light);border-radius:4px;margin:0 auto 16px auto}" +
            ".sheet-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}" +
            ".sheet-title{font-size:17px;font-weight:800;color:#fff}" +
            ".btn-close-sheet{background:none;border:none;color:var(--text-muted);font-size:20px;cursor:pointer;padding:4px}" +

            // Stepper & Inputs
            ".stepper{display:flex;align-items:center;gap:14px;background:var(--surface-card);border:1px solid var(--border);border-radius:12px;padding:6px 12px;width:fit-content}" +
            ".btn-step{width:32px;height:32px;border-radius:8px;background:var(--surface);border:1px solid var(--border);color:#fff;font-size:16px;font-weight:800;cursor:pointer}" +
            ".step-val{font-size:16px;font-weight:800;color:var(--amber)}" +
            ".sheet-input{width:100%;background:var(--bg);border:1.5px solid var(--border);border-radius:12px;padding:12px;color:#fff;font-size:14px;font-family:inherit;outline:none;margin-top:8px}" +
            ".sheet-input:focus{border-color:var(--amber)}" +

            // Chips
            ".chips-row{display:flex;flex-wrap:wrap;gap:6px;margin:10px 0 16px 0}" +
            ".chip-tag{background:var(--surface-card);border:1px solid var(--border);color:var(--text-muted);padding:6px 12px;border-radius:20px;font-size:11px;font-weight:700;cursor:pointer}" +
            ".chip-tag:active{background:var(--amber);color:#0f1015}" +

            // Main Action Buttons
            ".btn-primary-action{width:100%;padding:14px;background:linear-gradient(135deg,var(--emerald),var(--emerald-dark));border:none;border-radius:14px;color:#fff;font-size:15px;font-weight:800;cursor:pointer;font-family:inherit;box-shadow:0 4px 14px var(--emerald-glow)}" +
            ".btn-primary-action:active{transform:scale(0.98)}" +
            ".btn-secondary-action{width:100%;padding:12px;background:var(--surface-card);border:1px solid var(--border);border-radius:12px;color:var(--text-muted);font-size:14px;font-weight:700;cursor:pointer;font-family:inherit;margin-top:8px}" +

            // Toast
            ".toast-msg{position:fixed;top:70px;left:50%;transform:translateX(-50%);background:#181920;border:1.5px solid var(--emerald);color:#fff;padding:10px 20px;border-radius:30px;font-size:13px;font-weight:700;opacity:0;transition:opacity .2s;pointer-events:none;z-index:999;box-shadow:0 6px 20px rgba(0,0,0,0.6)}" +
            ".toast-msg.show{opacity:1}" +

            ".tab-view{display:none}" +
            ".tab-view.active{display:block}" +
            ".empty-state{text-align:center;padding:40px 16px;color:var(--text-dim);font-size:13px;font-weight:600}" +
            "</style></head><body>" +

            // TOP BAR
            "<div class='top-bar'>" +
            "  <div class='brand-box'>" +
            "    <div class='brand-icon'>☕</div>" +
            "    <div class='brand-text'>" +
            "      <h1 id='lbl-empresa-name'>Karoll Café</h1>" +
            "      <span id='lbl-app-subtitle'>Sistema Mesero Móvil</span>" +
            "    </div>" +
            "  </div>" +
            "  <div class='top-actions'>" +
            "    <div class='caja-badge abierta' id='badge-caja'>● Caja Abierta</div>" +
            "    <div class='user-pill' id='btn-user-select' onclick='cambiarMesero()'>👤 <span id='txt-user-name'>Mesero</span></div>" +
            "  </div>" +
            "</div>" +

            "<div class='toast-msg' id='toast'></div>" +

            // TAB 1: SALONES & MESAS
            "<div id='tab-mesas' class='tab-view active'>" +
            "  <div class='main-content'>" +
            "    <div class='section-header'>" +
            "      <span class='section-title'>Salones y Áreas</span>" +
            "      <span style='font-size:11px;font-weight:700;color:var(--amber)' onclick='renderMesasActuales()'>🔄 Actualizar</span>" +
            "    </div>" +
            "    <div class='salones-scroll' id='bar-salones'><div class='empty-state'>Cargando salones...</div></div>" +
            "    <div class='section-header'>" +
            "      <span class='section-title' id='lbl-area-name'>Distribución de Mesas</span>" +
            "      <span id='lbl-mesas-conteo' style='font-size:11px;font-weight:700;color:var(--text-muted)'>0 mesas</span>" +
            "    </div>" +
            "    <div class='mesas-grid' id='grid-mesas'></div>" +
            "  </div>" +
            "</div>" +

            // TAB 2: CARTA Y PEDIDO
            "<div id='tab-carta' class='tab-view'>" +
            "  <div class='main-content'>" +
            "    <div style='display:flex;justify-content:space-between;align-items:center;background:var(--surface);padding:10px 14px;border-radius:12px;margin-bottom:12px;border:1px solid var(--border)'>" +
            "      <div><span style='font-size:11px;font-weight:700;color:var(--text-dim)'>MESA ACTIVA</span><div id='lbl-mesa-activa-tag' style='font-size:14px;font-weight:800;color:var(--amber)'>Sin Mesa Seleccionada</div></div>" +
            "      <button style='background:var(--surface-card);border:1px solid var(--border);color:var(--text-muted);padding:6px 12px;border-radius:8px;font-size:11px;font-weight:700;cursor:pointer' onclick='switchTab(\"mesas\")'>Cambiar</button>" +
            "    </div>" +
            "    <div class='search-container'>" +
            "      <span class='search-icon'>🔍</span>" +
            "      <input type='text' id='txt-search-menu' placeholder='Buscar café, onces, platos...' oninput='filtrarMenu()'>" +
            "    </div>" +
            "    <div class='cats-scroll' id='bar-categorias'>" +
            "      <div class='cat-pill active' onclick='filtrarCat(\"TODOS\", this)'>Todos</div>" +
            "      <div class='cat-pill' onclick='filtrarCat(\"CAFETERIA\", this)'>Cafetería & Onces</div>" +
            "      <div class='cat-pill' onclick='filtrarCat(\"COMIDAS\", this)'>Comidas & Platos</div>" +
            "      <div class='cat-pill' onclick='filtrarCat(\"BEBIDAS\", this)'>Bebidas & Jugos</div>" +
            "      <div class='cat-pill' onclick='filtrarCat(\"POSTRES\", this)'>Postres & Dulces</div>" +
            "    </div>" +
            "    <div id='lista-platos'><div class='empty-state'>Cargando carta...</div></div>" +
            "  </div>" +
            "</div>" +

            // TAB 3: COMANDAS DEL TURNO
            "<div id='tab-historial' class='tab-view'>" +
            "  <div class='main-content'>" +
            "    <div class='section-header'>" +
            "      <span class='section-title'>Comandas del Turno</span>" +
            "      <span style='font-size:11px;font-weight:700;color:var(--amber);cursor:pointer' onclick='cargarHistorial()'>🔄 Refrescar</span>" +
            "    </div>" +
            "    <div id='lista-historial'><div class='empty-state'>Cargando historial...</div></div>" +
            "  </div>" +
            "</div>" +

            // TAB 4: RESUMEN TURNO
            "<div id='tab-turno' class='tab-view'>" +
            "  <div class='main-content'>" +
            "    <div class='section-header'><span class='section-title'>Balance Operativo</span></div>" +
            "    <div style='background:var(--surface-card);border:1px solid var(--border);border-radius:16px;padding:16px;margin-bottom:12px'>" +
            "      <span style='font-size:11px;font-weight:700;color:var(--text-dim);text-transform:uppercase'>Ventas del Turno</span>" +
            "      <div style='font-size:26px;font-weight:800;color:var(--emerald);margin-top:4px' id='kpi-turno-total'>$ 0 COP</div>" +
            "    </div>" +
            "    <div style='display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:14px'>" +
            "      <div style='background:var(--surface-card);border:1px solid var(--border);border-radius:14px;padding:14px'><span style='font-size:11px;font-weight:700;color:var(--text-dim)'>MESAS ACTIVAS</span><div style='font-size:20px;font-weight:800;color:var(--amber);margin-top:2px' id='kpi-turno-activas'>0</div></div>" +
            "      <div style='background:var(--surface-card);border:1px solid var(--border);border-radius:14px;padding:14px'><span style='font-size:11px;font-weight:700;color:var(--text-dim)'>COBRADAS</span><div style='font-size:20px;font-weight:800;color:var(--emerald);margin-top:2px' id='kpi-turno-finalizadas'>0</div></div>" +
            "    </div>" +
            "    <button class='btn-secondary-action' style='color:var(--sky)' onclick='location.href=\"/cocina\"'>🍳 Abrir Pantalla Cocina / KDS</button>" +
            "    <button class='btn-secondary-action' style='color:var(--amber)' onclick='location.href=\"/menu\"'>📲 Ver Menú Digital QR</button>" +
            "  </div>" +
            "</div>" +

            // FLOATING CART BAR
            "<div class='cart-floating-bar' id='floating-cart'>" +
            "  <div class='cart-info-text'>" +
            "    <div class='title' id='cart-summary-title'>0 productos</div>" +
            "    <div class='sub' id='cart-summary-total'>$ 0 COP</div>" +
            "  </div>" +
            "  <button class='btn-view-cart' onclick='abrirModalConfirmarComanda()'>Ver Comanda ➔</button>" +
            "</div>" +

            // BOTTOM NAVIGATION
            "<div class='bottom-nav'>" +
            "  <button class='nav-item active' id='nav-btn-mesas' onclick='switchTab(\"mesas\")'>" +
            "    <svg fill='none' stroke='currentColor' viewBox='0 0 24 24'><rect x='3' y='3' width='7' height='7' rx='2'></rect><rect x='14' y='3' width='7' height='7' rx='2'></rect><rect x='14' y='14' width='7' height='7' rx='2'></rect><rect x='3' y='14' width='7' height='7' rx='2'></rect></svg>" +
            "    <span>Salones</span>" +
            "    <span class='nav-badge' id='badge-nav-mesas' style='display:none'>0</span>" +
            "  </button>" +
            "  <button class='nav-item' id='nav-btn-carta' onclick='switchTab(\"carta\")'>" +
            "    <svg fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253'></path></svg>" +
            "    <span>Tomar Pedido</span>" +
            "  </button>" +
            "  <button class='nav-item' id='nav-btn-historial' onclick='switchTab(\"historial\")'>" +
            "    <svg fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2'></path></svg>" +
            "    <span>Comandas</span>" +
            "  </button>" +
            "  <button class='nav-item' id='nav-btn-turno' onclick='switchTab(\"turno\")'>" +
            "    <svg fill='none' stroke='currentColor' viewBox='0 0 24 24'><path d='M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z'></path></svg>" +
            "    <span>Turno</span>" +
            "  </button>" +
            "</div>" +

            // MODAL 1: DETALLE MESA & RONDAS
            "<div class='modal-overlay' id='modal-mesa'>" +
            "  <div class='sheet-box'>" +
            "    <div class='sheet-handle'></div>" +
            "    <div class='sheet-header'>" +
            "      <div><span style='font-size:11px;font-weight:700;color:var(--amber)'>ESTADO DE MESA</span><h3 class='sheet-title' id='lbl-modal-mesa-title'>Mesa 1</h3></div>" +
            "      <button class='btn-close-sheet' onclick='cerrarModalMesa()'>✕</button>" +
            "    </div>" +
            "    <div id='modal-mesa-body'></div>" +
            "    <div style='margin-top:14px;display:flex;flex-direction:column;gap:8px'>" +
            "      <button class='btn-primary-action' id='btn-nueva-ronda-action' onclick='iniciarNuevaRonda()'>+ Pedir Nueva Ronda</button>" +
            "      <button class='btn-secondary-action' onclick='iniciarAgregarARondaActual()'>Añadir a Ronda Actual</button>" +
            "      <div style='display:grid;grid-template-columns:1fr 1fr;gap:8px'>" +
            "        <button class='btn-secondary-action' onclick='abrirSplitModal()'>Dividir Cuenta</button>" +
            "        <button class='btn-secondary-action' onclick='abrirTrasladoModal()'>Trasladar Mesa</button>" +
            "      </div>" +
            "    </div>" +
            "  </div>" +
            "</div>" +

            // MODAL 2: AGREGAR PLATO A COMANDA
            "<div class='modal-overlay' id='modal-add-plato'>" +
            "  <div class='sheet-box'>" +
            "    <div class='sheet-handle'></div>" +
            "    <div class='sheet-header'>" +
            "      <h3 class='sheet-title' id='lbl-add-plato-name'>Nombre del Plato</h3>" +
            "      <button class='btn-close-sheet' onclick='cerrarModalAddPlato()'>✕</button>" +
            "    </div>" +
            "    <div style='display:flex;justify-content:space-between;align-items:center;margin-bottom:14px'>" +
            "      <span style='font-size:13px;font-weight:700;color:var(--text-muted)'>Cantidad:</span>" +
            "      <div class='stepper'>" +
            "        <button class='btn-step' onclick='modAddQty(-1)'>-</button>" +
            "        <span class='step-val' id='lbl-add-qty'>1</span>" +
            "        <button class='btn-step' onclick='modAddQty(1)'>+</button>" +
            "      </div>" +
            "    </div>" +
            "    <span style='font-size:11px;font-weight:700;color:var(--text-dim);text-transform:uppercase'>Notas de Preparación (Opcional):</span>" +
            "    <input type='text' class='sheet-input' id='txt-add-nota' placeholder='Ej. Sin azúcar, bien caliente, para llevar...'>" +
            "    <div class='chips-row'>" +
            "      <span class='chip-tag' onclick='addNotaChip(\"Sin azúcar\")'>Sin azúcar</span>" +
            "      <span class='chip-tag' onclick='addNotaChip(\"Bien caliente\")'>Bien caliente</span>" +
            "      <span class='chip-tag' onclick='addNotaChip(\"Para llevar\")'>Para llevar</span>" +
            "      <span class='chip-tag' onclick='addNotaChip(\"Sin cebolla\")'>Sin cebolla</span>" +
            "      <span class='chip-tag' onclick='addNotaChip(\"Término medio\")'>Término medio</span>" +
            "    </div>" +
            "    <button class='btn-primary-action' onclick='confirmarAddPlato()'>Agregar a la Comanda</button>" +
            "  </div>" +
            "</div>" +

            // MODAL 3: CONFIRMAR Y ENVIAR COMANDA
            "<div class='modal-overlay' id='modal-confirm-comanda'>" +
            "  <div class='sheet-box'>" +
            "    <div class='sheet-handle'></div>" +
            "    <div class='sheet-header'>" +
            "      <div><span style='font-size:11px;font-weight:700;color:var(--amber)' id='lbl-title-comanda-envio'>ENVIAR COMANDA</span><h3 class='sheet-title'>Revisión de Pedido</h3></div>" +
            "      <button class='btn-close-sheet' onclick='cerrarModalComanda()'>✕</button>" +
            "    </div>" +
            "    <div id='comanda-preview-list' style='max-height:220px;overflow-y:auto;margin-bottom:12px'></div>" +
            "    <div style='display:flex;justify-content:space-between;align-items:center;background:var(--surface-card);padding:12px;border-radius:12px;margin-bottom:14px;border:1px solid var(--border)'>" +
            "      <span style='font-size:13px;font-weight:700;color:var(--text-muted)'>Total Comanda:</span>" +
            "      <span style='font-size:20px;font-weight:800;color:var(--emerald)' id='lbl-modal-comanda-total'>$ 0 COP</span>" +
            "    </div>" +
            "    <button class='btn-primary-action' id='btn-submit-comanda' onclick='enviarComandaFinal()'>ENVIAR A COCINA / POS ➔</button>" +
            "    <button class='btn-secondary-action' style='color:var(--rose)' onclick='vaciarComanda()'>Vaciar Comanda</button>" +
            "  </div>" +
            "</div>" +

            // JAVASCRIPT
            "<script>" +
            "let salas = []; let carta = []; let carrito = [];" +
            "let salaActivaId = 1; let salaActivaNombre = 'Salón 1';" +
            "let mesaActivaNum = 0; let idPedidoActivo = 0; let esNuevaRonda = false;" +
            "let meseroActual = 'Mesero Móvil';" +
            "let catFiltroActiva = 'TODOS';" +
            "let platoSelModal = null; let qtyAddModal = 1;" +
            "let ultimasRondasMesa = []; let totalMesaModal = 0;" +

            "function formatCop(val){ return '$ ' + Number(val || 0).toLocaleString('es-CO'); }" +

            "function toast(msg){" +
            "  const t = document.getElementById('toast'); t.textContent = msg; t.classList.add('show');" +
            "  setTimeout(() => t.classList.remove('show'), 2600);" +
            "}" +

            "async function init(){" +
            "  try{" +
            "    const rInf = await fetch('/api/info'); const dInf = await rInf.json();" +
            "    if(dInf.nombre) document.getElementById('lbl-empresa-name').textContent = dInf.nombre;" +
            "    const badgeCaja = document.getElementById('badge-caja');" +
            "    if(dInf.caja_abierta){ badgeCaja.className='caja-badge abierta'; badgeCaja.textContent='● Caja Abierta'; }" +
            "    else { badgeCaja.className='caja-badge cerrada'; badgeCaja.textContent='● Caja Cerrada'; }" +
            "    if(dInf.usuarios && dInf.usuarios.length){ window.listaUsuarios = dInf.usuarios; }" +
            "    const rSalas = await fetch('/api/salas'); salas = await rSalas.json();" +
            "    renderSalones();" +
            "    cargarCarta();" +
            "    setInterval(renderMesasActualesSilencioso, 3500);" +
            "  }catch(e){ toast('Error al conectar con servidor POS'); }" +
            "}" +

            "function renderSalones(){" +
            "  const bar = document.getElementById('bar-salones'); bar.innerHTML = '';" +
            "  if(!salas.length){ bar.innerHTML = '<div class=\"empty-state\">No hay salones</div>'; return; }" +
            "  salas.forEach((s, idx) => {" +
            "    const pill = document.createElement('div');" +
            "    pill.className = 'salon-tab' + (s.id === salaActivaId ? ' active' : '');" +
            "    pill.textContent = s.nombre + ' (' + s.mesas + ')';" +
            "    pill.onclick = () => { salaActivaId = s.id; salaActivaNombre = s.nombre; renderSalones(); renderMesasActuales(); };" +
            "    bar.appendChild(pill);" +
            "  });" +
            "  renderMesasActuales();" +
            "}" +

            "async function renderMesasActuales(){" +
            "  const sala = salas.find(s => s.id === salaActivaId) || salas[0];" +
            "  if(!sala) return;" +
            "  document.getElementById('lbl-area-name').textContent = sala.nombre;" +
            "  const cantMesas = sala.mesas || 10;" +
            "  document.getElementById('lbl-mesas-conteo').textContent = cantMesas + (cantMesas===1?' mesa':' mesas');" +
            "  const grid = document.getElementById('grid-mesas');" +
            "  grid.innerHTML = '';" +
            "  for(let i=1; i<=cantMesas; i++){" +
            "    const etiqueta = (cantMesas >= 4 && i > cantMesas - 4) ? ('Dom. ' + i) : ('Mesa ' + i);" +
            "    const mCard = document.createElement('div');" +
            "    mCard.id = 'mesa-card-' + i;" +
            "    mCard.className = 'mesa-card libre';" +
            "    mCard.innerHTML = '<div class=\"mesa-num\">' + etiqueta + '</div><span class=\"mesa-status\">Libre</span>';" +
            "    mCard.onclick = () => touchMesa(i);" +
            "    grid.appendChild(mCard);" +
            "  }" +
            "  await actualizarEstadosVisualesMesas(cantMesas);" +
            "}" +

            "async function renderMesasActualesSilencioso(){" +
            "  const sala = salas.find(s => s.id === salaActivaId);" +
            "  if(!sala) return;" +
            "  await actualizarEstadosVisualesMesas(sala.mesas || 10);" +
            "}" +

            "async function actualizarEstadosVisualesMesas(cantMesas){" +
            "  try{" +
            "    const r = await fetch('/api/mesas-estado?id_sala=' + salaActivaId);" +
            "    const data = await r.json();" +
            "    const estados = data.mesas || {};" +
            "    let totalOcupadas = 0;" +
            "    for(let i=1; i<=cantMesas; i++){" +
            "      const info = estados[i] || estados[String(i)];" +
            "      const card = document.getElementById('mesa-card-' + i);" +
            "      if(!card) continue;" +
            "      const etiqueta = (cantMesas >= 4 && i > cantMesas - 4) ? ('Dom. ' + i) : ('Mesa ' + i);" +
            "      if(info && info.id_pedido > 0){" +
            "        totalOcupadas++;" +
            "        const esPrep = (info.estado === 'PREPARADO');" +
            "        card.className = 'mesa-card ' + (esPrep ? 'preparada' : 'ocupada');" +
            "        const statusTxt = esPrep ? 'Listo' : ('Ocupada (' + (info.rondas||1) + 'R)');" +
            "        card.innerHTML = '<div class=\"mesa-num\">' + etiqueta + '</div><span class=\"mesa-status\">' + statusTxt + '</span><div class=\"mesa-total\">' + formatCop(info.total) + '</div>';" +
            "      } else {" +
            "        card.className = 'mesa-card libre';" +
            "        card.innerHTML = '<div class=\"mesa-num\">' + etiqueta + '</div><span class=\"mesa-status\">Libre</span>';" +
            "      }" +
            "    }" +
            "    const badgeNav = document.getElementById('badge-nav-mesas');" +
            "    if(totalOcupadas > 0){ badgeNav.textContent = totalOcupadas; badgeNav.style.display = 'inline-block'; }" +
            "    else { badgeNav.style.display = 'none'; }" +
            "  }catch(e){}" +
            "}" +

            "async function touchMesa(num){" +
            "  mesaActivaNum = num;" +
            "  const r = await fetch('/api/mesas-estado?id_sala=' + salaActivaId);" +
            "  const data = await r.json();" +
            "  const estados = data.mesas || {};" +
            "  const info = estados[num] || estados[String(num)];" +
            "  if(info && info.id_pedido > 0){" +
            "    abrirModalDetalleMesa(num, info);" +
            "  } else {" +
            "    idPedidoActivo = 0; esNuevaRonda = false;" +
            "    document.getElementById('lbl-mesa-activa-tag').textContent = salaActivaNombre + ' • Mesa ' + num;" +
            "    actualizarBarraCarrito();" +
            "    switchTab('carta');" +
            "  }" +
            "}" +

            "async function abrirModalDetalleMesa(num, info){" +
            "  mesaActivaNum = num; idPedidoActivo = info.id_pedido;" +
            "  document.getElementById('lbl-modal-mesa-title').textContent = salaActivaNombre + ' • Mesa ' + num;" +
            "  const body = document.getElementById('modal-mesa-body');" +
            "  body.innerHTML = '<div class=\"empty-state\">Cargando rondas y consumo...</div>';" +
            "  document.getElementById('modal-mesa').classList.add('open');" +
            "  try{" +
            "    const r = await fetch('/api/mesa-detalle?mesa=' + num + '&sala=' + salaActivaId);" +
            "    const data = await r.json();" +
            "    totalMesaModal = data.total || 0;" +
            "    ultimasRondasMesa = data.rondas || [];" +
            "    let html = '<div style=\"display:flex;justify-content:space-between;align-items:center;background:var(--surface-card);padding:12px 14px;border-radius:14px;margin-bottom:12px;border:1px solid var(--border)\"><span style=\"font-size:13px;color:var(--text-muted);font-weight:700\">Total Mesa (' + ultimasRondasMesa.length + ' ' + (ultimasRondasMesa.length===1?'Ronda':'Rondas') + '):</span><span style=\"font-size:18px;font-weight:800;color:var(--emerald)\">' + formatCop(data.total) + '</span></div>';" +
            "    html += '<div style=\"max-height:220px;overflow-y:auto\">';" +
            "    ultimasRondasMesa.forEach(rnd => {" +
            "      const stColor = rnd.estado === 'PREPARADO' ? 'var(--sky)' : 'var(--amber)';" +
            "      html += '<div style=\"background:var(--bg);border:1px solid var(--border);border-radius:12px;padding:10px 12px;margin-bottom:8px\">';" +
            "      html += '<div style=\"display:flex;justify-content:space-between;align-items:center;margin-bottom:6px\"><span style=\"font-size:13px;font-weight:800;color:var(--sky)\">🏷️ Ronda ' + rnd.rondaNum + ' (' + rnd.usuario + ')</span><span style=\"font-size:11px;font-weight:800;padding:2px 8px;border-radius:8px;background:rgba(56,189,248,0.15);color:' + stColor + '\">' + rnd.estado + '</span></div>';" +
            "      rnd.items.forEach(it => {" +
            "        html += '<div style=\"display:flex;justify-content:space-between;font-size:13px;padding:3px 0\"><span>' + it.cantidad + 'x ' + it.nombre + (it.comentario ? ' <small style=\"color:var(--amber)\">(' + it.comentario + ')</small>' : '') + '</span><span style=\"font-weight:700\">' + formatCop(it.precio * it.cantidad) + '</span></div>';" +
            "      });" +
            "      html += '<div style=\"display:flex;justify-content:space-between;align-items:center;margin-top:6px;padding-top:4px;border-top:1px solid var(--border)\"><span style=\"font-size:11px;color:var(--text-dim)\">Subtotal:</span><span style=\"font-weight:800;color:var(--emerald)\">' + formatCop(rnd.total) + '</span></div>';" +
            "      if(rnd.estado !== 'PREPARADO'){" +
            "        html += '<button style=\"width:100%;margin-top:6px;padding:6px;background:var(--emerald-dark);color:#fff;border:none;border-radius:6px;font-size:12px;font-weight:800;cursor:pointer\" onclick=\"marcarPreparadoDesdeModal(' + rnd.id + ')\">Marcar Ronda ' + rnd.rondaNum + ' Lista</button>';" +
            "      }" +
            "      html += '</div>';" +
            "    });" +
            "    html += '</div>';" +
            "    body.innerHTML = html;" +
            "    const sigRonda = ultimasRondasMesa.length + 1;" +
            "    document.getElementById('btn-nueva-ronda-action').textContent = '+ Pedir Ronda ' + sigRonda + ' (Nueva Ronda)';" +
            "  }catch(e){ body.innerHTML = '<div class=\"empty-state\">Error al cargar detalle</div>'; }" +
            "}" +

            "async function marcarPreparadoDesdeModal(idPed){" +
            "  await fetch('/api/preparar', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({id_pedido:idPed})});" +
            "  toast('Ronda marcada como lista');" +
            "  cerrarModalMesa();" +
            "  renderMesasActuales();" +
            "}" +

            "function cerrarModalMesa(){ document.getElementById('modal-mesa').classList.remove('open'); }" +

            "function iniciarNuevaRonda(){" +
            "  cerrarModalMesa(); esNuevaRonda = true; idPedidoActivo = 0;" +
            "  const sigRonda = ultimasRondasMesa.length + 1;" +
            "  document.getElementById('lbl-mesa-activa-tag').textContent = salaActivaNombre + ' • Mesa ' + mesaActivaNum + ' (Ronda ' + sigRonda + ')';" +
            "  document.getElementById('lbl-title-comanda-envio').textContent = 'CONFIRMAR RONDA ' + sigRonda;" +
            "  actualizarBarraCarrito();" +
            "  switchTab('carta');" +
            "}" +

            "function iniciarAgregarARondaActual(){" +
            "  cerrarModalMesa(); esNuevaRonda = false;" +
            "  const ultRnd = ultimasRondasMesa[ultimasRondasMesa.length - 1];" +
            "  if(ultRnd) idPedidoActivo = ultRnd.id;" +
            "  document.getElementById('lbl-mesa-activa-tag').textContent = salaActivaNombre + ' • Mesa ' + mesaActivaNum + ' (Ronda ' + (ultRnd ? ultRnd.rondaNum : 1) + ')';" +
            "  document.getElementById('lbl-title-comanda-envio').textContent = 'AÑADIR A RONDA ACTUAL';" +
            "  actualizarBarraCarrito();" +
            "  switchTab('carta');" +
            "}" +

            "function abrirSplitModal(){" +
            "  if(!totalMesaModal){ toast('No hay consumos en la mesa'); return; }" +
            "  const p = prompt('¿Entre cuántas personas dividir la cuenta de ' + formatCop(totalMesaModal) + '?', '2');" +
            "  if(p && Number(p) > 0){" +
            "    const porPersona = totalMesaModal / Number(p);" +
            "    alert('División (' + p + ' personas):\\n• Por persona: ' + formatCop(porPersona));" +
            "  }" +
            "}" +

            "async function abrirTrasladoModal(){" +
            "  const dest = prompt('Ingrese el NÚMERO de mesa destino para la Mesa ' + mesaActivaNum + ':', '');" +
            "  if(dest && Number(dest) > 0){" +
            "    try {" +
            "      const r = await fetch('/api/trasladar-mesa', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({ mesa_origen: mesaActivaNum, sala_origen: salaActivaId, mesa_destino: Number(dest), sala_destino: salaActivaId })});" +
            "      const d = await r.json();" +
            "      if(d.ok){ toast('Mesa trasladada exitosamente'); cerrarModalMesa(); renderMesasActuales(); }" +
            "      else { alert('Error: ' + d.error); }" +
            "    } catch(e){ alert('Error de conexión'); }" +
            "  }" +
            "}" +

            "async function cargarCarta(){" +
            "  const cont = document.getElementById('lista-platos');" +
            "  try{" +
            "    const r = await fetch('/api/carta'); carta = await r.json();" +
            "    renderPlatos(carta);" +
            "  }catch(e){ cont.innerHTML = \"<div class='empty-state'>Error al cargar menú</div>\"; }" +
            "}" +

            "function filtrarCat(cat, elem){" +
            "  catFiltroActiva = cat;" +
            "  document.querySelectorAll('#bar-categorias .cat-pill').forEach(p=>p.classList.remove('active'));" +
            "  if(elem) elem.classList.add('active');" +
            "  filtrarMenu();" +
            "}" +

            "function renderPlatos(lista){" +
            "  const cont = document.getElementById('lista-platos'); cont.innerHTML = '';" +
            "  if(!lista.length){ cont.innerHTML = \"<div class='empty-state'>No se encontraron productos</div>\"; return; }" +
            "  lista.forEach(p => {" +
            "    const card = document.createElement('div');" +
            "    card.className = 'dish-card';" +
            "    card.innerHTML = '<div class=\"dish-info\"><div class=\"nom\">' + p.nombre + '</div><div class=\"prc\">' + formatCop(p.precio) + '</div></div><button class=\"btn-add-circle\">+</button>';" +
            "    card.onclick = () => abrirModalAddPlato(p);" +
            "    cont.appendChild(card);" +
            "  });" +
            "}" +

            "function filtrarMenu(){" +
            "  const q = document.getElementById('txt-search-menu').value.toLowerCase().trim();" +
            "  const f = carta.filter(p => {" +
            "    const matchText = p.nombre.toLowerCase().includes(q);" +
            "    if(!matchText) return false;" +
            "    if(catFiltroActiva === 'TODOS') return true;" +
            "    const nom = p.nombre.toUpperCase();" +
            "    if(catFiltroActiva === 'COMIDAS') return nom.includes('HAMBURGUESA') || nom.includes('PERRO') || nom.includes('SALCHIPAPA') || nom.includes('PIZZA') || nom.includes('CARNE') || nom.includes('POLLO') || nom.includes('COMUNERA') || nom.includes('SANDWICH');" +
            "    if(catFiltroActiva === 'CAFETERIA') return nom.includes('CAFE') || nom.includes('CAPUCCINO') || nom.includes('LATTE') || nom.includes('ONCE') || nom.includes('PAN') || nom.includes('TE') || nom.includes('CHOCOLATE');" +
            "    if(catFiltroActiva === 'BEBIDAS') return nom.includes('JUGO') || nom.includes('GASEOSA') || nom.includes('AGUA') || nom.includes('CERVEZA') || nom.includes('LIMONADA') || nom.includes('SODA');" +
            "    if(catFiltroActiva === 'POSTRES') return nom.includes('POSTRE') || nom.includes('TORTA') || nom.includes('HELADO') || nom.includes('WAFFLE') || nom.includes('MALTEADA');" +
            "    return true;" +
            "  });" +
            "  renderPlatos(f);" +
            "}" +

            "function abrirModalAddPlato(p){" +
            "  if(mesaActivaNum === 0){" +
            "    toast('Seleccione primero una mesa');" +
            "    switchTab('mesas');" +
            "    return;" +
            "  }" +
            "  platoSelModal = p; qtyAddModal = 1;" +
            "  document.getElementById('lbl-add-plato-name').textContent = p.nombre + ' (' + formatCop(p.precio) + ')';" +
            "  document.getElementById('lbl-add-qty').textContent = '1';" +
            "  document.getElementById('txt-add-nota').value = '';" +
            "  document.getElementById('modal-add-plato').classList.add('open');" +
            "}" +

            "function addNotaChip(txt){" +
            "  const inp = document.getElementById('txt-add-nota');" +
            "  if(inp.value.trim().length > 0){ inp.value += ', ' + txt; } else { inp.value = txt; }" +
            "}" +

            "function modAddQty(delta){" +
            "  qtyAddModal = Math.max(1, Math.min(50, qtyAddModal + delta));" +
            "  document.getElementById('lbl-add-qty').textContent = qtyAddModal;" +
            "}" +

            "function cerrarModalAddPlato(){ document.getElementById('modal-add-plato').classList.remove('open'); }" +

            "function confirmarAddPlato(){" +
            "  if(!platoSelModal) return;" +
            "  const nota = document.getElementById('txt-add-nota').value.trim();" +
            "  carrito.push({ id: platoSelModal.id, nombre: platoSelModal.nombre, precio: platoSelModal.precio, cantidad: qtyAddModal, comentario: nota });" +
            "  cerrarModalAddPlato();" +
            "  actualizarBarraCarrito();" +
            "  toast(platoSelModal.nombre + ' (x' + qtyAddModal + ') añadido');" +
            "}" +

            "function actualizarBarraCarrito(){" +
            "  let cnt = 0, tot = 0;" +
            "  carrito.forEach(i => { cnt += i.cantidad; tot += (i.precio * i.cantidad); });" +
            "  const etiquetaMesa = 'Mesa ' + mesaActivaNum;" +
            "  document.getElementById('cart-summary-title').textContent = cnt + (cnt === 1 ? ' producto' : ' productos') + ' para ' + etiquetaMesa;" +
            "  document.getElementById('cart-summary-total').textContent = formatCop(tot);" +
            "  const bar = document.getElementById('floating-cart');" +
            "  if(cnt > 0) bar.classList.add('show'); else bar.classList.remove('show');" +
            "}" +

            "function abrirModalConfirmarComanda(){" +
            "  const cont = document.getElementById('comanda-preview-list'); cont.innerHTML = '';" +
            "  let tot = 0;" +
            "  carrito.forEach((i, idx) => {" +
            "    const sub = i.precio * i.cantidad; tot += sub;" +
            "    const r = document.createElement('div');" +
            "    r.style.cssText = 'display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--border);font-size:13px';" +
            "    r.innerHTML = '<div><div style=\"font-weight:700;color:#fff\">' + i.cantidad + 'x ' + i.nombre + '</div>' + (i.comentario ? '<div style=\"font-size:11px;color:var(--amber)\">' + i.comentario + '</div>' : '') + '</div><div style=\"display:flex;align-items:center;gap:10px\"><span style=\"font-weight:800;color:var(--emerald)\">' + formatCop(sub) + '</span><button style=\"background:none;border:none;color:var(--rose);font-size:16px;cursor:pointer\" onclick=\"quitarDelCarrito(' + idx + ')\">✕</button></div>';" +
            "    cont.appendChild(r);" +
            "  });" +
            "  document.getElementById('lbl-modal-comanda-total').textContent = formatCop(tot);" +
            "  document.getElementById('modal-confirm-comanda').classList.add('open');" +
            "}" +

            "function quitarDelCarrito(idx){" +
            "  carrito.splice(idx, 1);" +
            "  actualizarBarraCarrito();" +
            "  if(carrito.length) abrirModalConfirmarComanda(); else cerrarModalComanda();" +
            "}" +

            "function vaciarComanda(){" +
            "  carrito = []; actualizarBarraCarrito(); cerrarModalComanda();" +
            "}" +

            "function cerrarModalComanda(){ document.getElementById('modal-confirm-comanda').classList.remove('open'); }" +

            "async function enviarComandaFinal(){" +
            "  if(!carrito.length){ toast('La comanda está vacía'); return; }" +
            "  const btn = document.getElementById('btn-submit-comanda');" +
            "  btn.disabled = true; btn.textContent = 'Enviando a Cocina...';" +
            "  try{" +
            "    const payload = {" +
            "      sala: salaActivaId," +
            "      mesa: mesaActivaNum," +
            "      id_pedido: idPedidoActivo," +
            "      nueva_ronda: esNuevaRonda," +
            "      mesero: meseroActual," +
            "      items: carrito" +
            "    };" +
            "    const r = await fetch('/api/enviar-comanda', {" +
            "      method: 'POST'," +
            "      headers: {'Content-Type': 'application/json'}," +
            "      body: JSON.stringify(payload)" +
            "    });" +
            "    const res = await r.json();" +
            "    if(res.ok){" +
            "      toast('¡Comanda enviada a Cocina/POS con éxito!');" +
            "      carrito = []; idPedidoActivo = 0; esNuevaRonda = false;" +
            "      actualizarBarraCarrito(); cerrarModalComanda();" +
            "      setTimeout(() => { switchTab('mesas'); renderMesasActuales(); }, 300);" +
            "    } else {" +
            "      alert('Error: ' + (res.error || 'No se pudo enviar'));" +
            "    }" +
            "  }catch(e){ alert('Error de conexión con el servidor'); }" +
            "  finally{" +
            "    btn.disabled = false; btn.textContent = 'ENVIAR A COCINA / POS ➔';" +
            "  }" +
            "}" +

            "async function cargarHistorial(){" +
            "  const cont = document.getElementById('lista-historial'); cont.innerHTML = '<div class=\"empty-state\">Cargando comandas...</div>';" +
            "  try{" +
            "    const r = await fetch('/api/historial'); const pedidos = await r.json();" +
            "    cont.innerHTML = '';" +
            "    if(!pedidos.length){ cont.innerHTML = \"<div class='empty-state'>No hay comandas registradas hoy</div>\"; return; }" +
            "    pedidos.forEach(p => {" +
            "      const card = document.createElement('div');" +
            "      card.style.cssText = 'background:var(--surface-card);border:1px solid var(--border);border-radius:14px;padding:12px 14px;margin-bottom:8px';" +
            "      const colorSt = p.estado === 'FINALIZADO' ? 'var(--emerald)' : (p.estado === 'PREPARADO' ? 'var(--sky)' : 'var(--amber)');" +
            "      card.innerHTML = '<div style=\"display:flex;justify-content:space-between;align-items:center;margin-bottom:4px\"><span style=\"font-size:14px;font-weight:800;color:#fff\">Pedido #' + p.id + ' • ' + p.sala + ' M' + p.mesa + '</span><span style=\"font-size:11px;font-weight:800;padding:2px 8px;border-radius:8px;background:rgba(245,158,11,0.15);color:' + colorSt + '\">' + p.estado + '</span></div><div style=\"display:flex;justify-content:space-between;font-size:12px;color:var(--text-dim)\"><span>' + p.usuario + ' • ' + (p.fecha ? p.fecha.substring(11,16) : '') + '</span><span style=\"font-weight:800;color:var(--emerald);font-size:14px\">' + formatCop(p.total) + '</span></div>';" +
            "      cont.appendChild(card);" +
            "    });" +
            "  }catch(e){ cont.innerHTML = \"<div class='empty-state'>Error al cargar historial</div>\"; }" +
            "}" +

            "async function cargarTurno(){" +
            "  try{" +
            "    const r = await fetch('/api/resumen-turno'); const t = await r.json();" +
            "    document.getElementById('kpi-turno-total').textContent = formatCop(t.totalVentas) + ' COP';" +
            "    document.getElementById('kpi-turno-activas').textContent = t.comandasActivas;" +
            "    document.getElementById('kpi-turno-finalizadas').textContent = t.comandasFinalizadas;" +
            "  }catch(e){}" +
            "}" +

            "function switchTab(tabId){" +
            "  document.querySelectorAll('.tab-view').forEach(v => v.classList.remove('active'));" +
            "  document.querySelectorAll('.bottom-nav .nav-item').forEach(n => n.classList.remove('active'));" +
            "  const targetTab = document.getElementById('tab-' + tabId);" +
            "  const targetNav = document.getElementById('nav-btn-' + tabId);" +
            "  if(targetTab) targetTab.classList.add('active');" +
            "  if(targetNav) targetNav.classList.add('active');" +
            "  window.scrollTo(0,0);" +
            "  if(tabId === 'mesas') renderMesasActuales();" +
            "  if(tabId === 'historial') cargarHistorial();" +
            "  if(tabId === 'turno') cargarTurno();" +
            "}" +

            "function cambiarMesero(){" +
            "  const users = window.listaUsuarios || [{nombre:'Mesero 1'},{nombre:'Mesero 2'}];" +
            "  const nombres = users.map(u => u.nombre).join('\\n• ');" +
            "  const m = prompt('Seleccione o escriba su nombre de Mesero:\\n• ' + nombres, meseroActual);" +
            "  if(m && m.trim().length > 0){" +
            "    meseroActual = m.trim();" +
            "    document.getElementById('txt-user-name').textContent = meseroActual;" +
            "    toast('Mesero activo: ' + meseroActual);" +
            "  }" +
            "}" +

            "window.onload = init;" +
            "</script>" +
            "</body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FRONTEND: Pantalla de Cocina KDS (/cocina)
    // ─────────────────────────────────────────────────────────────────────────
    private static String buildHtmlCocina() {
        return "<!DOCTYPE html><html lang='es'><head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>" +
            "<meta name='theme-color' content='#0f1015'>" +
            "<title>KDS Cocina & Barismo — Karoll Café</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap' rel='stylesheet'>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Plus Jakarta Sans',sans-serif;background:#0f1015;color:#f8fafc;min-height:100vh;padding:16px}" +
            ".kds-header{display:flex;justify-content:space-between;align-items:center;padding-bottom:14px;border-bottom:1px solid #2d313b;margin-bottom:16px}" +
            ".kds-header h1{font-size:18px;font-weight:800;color:#f59e0b}" +
            ".kds-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:14px}" +
            ".kds-card{background:#181920;border:2px solid #2d313b;border-radius:16px;overflow:hidden;display:flex;flex-direction:column}" +
            ".kds-card.urgente{border-color:#f43f5e}" +
            ".kds-card.medio{border-color:#f59e0b}" +
            ".kds-card.verde{border-color:#10b981}" +
            ".kds-top{padding:12px 14px;background:#20222a;display:flex;justify-content:space-between;align-items:center}" +
            ".kds-table{font-size:15px;font-weight:800;color:#fff}" +
            ".kds-timer{font-size:11px;font-weight:800;padding:3px 8px;border-radius:8px;background:#0f1015}" +
            ".kds-body{padding:14px;flex:1}" +
            ".kds-item{display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #2d313b;font-size:14px}" +
            ".kds-item .qty{font-weight:800;color:#f59e0b;margin-right:6px}" +
            ".kds-item .nota{font-size:12px;color:#f43f5e;font-weight:700;display:block;margin-top:2px}" +
            ".btn-listo{width:100%;padding:14px;background:#10b981;border:none;color:#fff;font-size:15px;font-weight:800;font-family:inherit;cursor:pointer}" +
            ".btn-listo:active{opacity:.85}" +
            ".empty{grid-column:1/-1;text-align:center;padding:60px 20px;color:#64748b;font-size:15px}" +
            "</style></head><body>" +

            "<div class='kds-header'>" +
            "  <div><h1>KDS Cocina & Barismo</h1><span style='font-size:12px;color:#94a3b8'>Comandas en tiempo real</span></div>" +
            "  <button style='background:#181920;border:1px solid #2d313b;color:#fff;padding:8px 14px;border-radius:10px;font-weight:700;cursor:pointer' onclick='location.href=\"/\"'>← Salón</button>" +
            "</div>" +

            "<div class='kds-grid' id='kds-grid'><div class='empty'>Cargando comandas de cocina...</div></div>" +

            "<script>" +
            "async function cargarCocina(){" +
            "  const grid = document.getElementById('kds-grid');" +
            "  try{" +
            "    const r = await fetch('/api/comandas-cocina'); const data = await r.json();" +
            "    grid.innerHTML = '';" +
            "    if(!data.length){ grid.innerHTML = \"<div class='empty'>No hay comandas pendientes en cocina</div>\"; return; }" +
            "    data.forEach(c => {" +
            "      const card = document.createElement('div');" +
            "      const mins = Math.max(1, Math.round((new Date() - new Date(c.fecha.replace(' ', 'T'))) / 60000));" +
            "      const nivel = mins >= 20 ? 'urgente' : (mins >= 10 ? 'medio' : 'verde');" +
            "      card.className = 'kds-card ' + nivel;" +
            "      let itemsHtml = '';" +
            "      c.items.forEach(it => {" +
            "        itemsHtml += '<div class=\"kds-item\"><div><span class=\"qty\">' + it.cantidad + 'x</span><span style=\"font-weight:700\">' + it.nombre + '</span>' + (it.comentario ? '<span class=\"nota\">⚠️ ' + it.comentario + '</span>' : '') + '</div></div>';" +
            "      });" +
            "      card.innerHTML = '<div class=\"kds-top\"><div class=\"kds-table\">' + c.sala + ' • Mesa ' + c.mesa + ' (' + c.usuario + ')</div><div class=\"kds-timer\">⏱ ' + (isNaN(mins) ? '1' : mins) + ' min</div></div><div class=\"kds-body\">' + itemsHtml + '</div><button class=\"btn-listo\" onclick=\"marcarListoCocina(' + c.id + ')\">MARCAR COMO LISTO</button>';" +
            "      grid.appendChild(card);" +
            "    });" +
            "  }catch(e){}" +
            "}" +

            "async function marcarListoCocina(idPed){" +
            "  await fetch('/api/preparar', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({id_pedido:idPed}) });" +
            "  cargarCocina();" +
            "}" +

            "window.onload = () => { cargarCocina(); setInterval(cargarCocina, 4000); };" +
            "</script></body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FRONTEND: Menú Digital para Clientes (/menu)
    // ─────────────────────────────────────────────────────────────────────────
    private static String buildHtmlMenuCliente() {
        return "<!DOCTYPE html><html lang='es'><head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>" +
            "<meta name='theme-color' content='#0f1015'>" +
            "<title>Menú Digital — Karoll Café</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap' rel='stylesheet'>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Plus Jakarta Sans',sans-serif;background:#0f1015;color:#f8fafc;min-height:100vh;padding:16px;max-width:500px;margin:0 auto}" +
            ".menu-header{text-align:center;padding:18px 0;border-bottom:1px solid #2d313b;margin-bottom:16px}" +
            ".menu-header h1{font-size:22px;font-weight:800;color:#f59e0b}" +
            ".menu-header p{font-size:13px;color:#94a3b8;margin-top:4px}" +
            ".search-box{position:relative;margin-bottom:14px}" +
            ".search-box input{width:100%;background:#181920;border:1.5px solid #2d313b;border-radius:12px;padding:12px 14px 12px 38px;color:#fff;font-size:14px;outline:none;font-family:inherit}" +
            ".search-box .icon{position:absolute;left:12px;top:50%;transform:translateY(-50%);color:#64748b;font-size:16px}" +
            ".menu-item{background:#181920;border:1px solid #2d313b;border-radius:14px;padding:14px;display:flex;justify-content:space-between;align-items:center;margin-bottom:10px}" +
            ".menu-item .nom{font-size:15px;font-weight:700;color:#f8fafc}" +
            ".menu-item .prc{font-size:15px;font-weight:800;color:#10b981}" +
            "</style></head><body>" +

            "<div class='menu-header'>" +
            "  <h1 id='lbl-menu-empresa'>Karoll Café y Onces</h1>" +
            "  <p>Carta Digital y Menú de Precios</p>" +
            "</div>" +

            "<div class='search-box'>" +
            "  <span class='icon'>🔍</span>" +
            "  <input type='text' id='txt-buscar-cli' placeholder='Buscar producto o bebida...' oninput='filtrarMenu()'>" +
            "</div>" +

            "<div id='menu-list'></div>" +

            "<script>" +
            "let cartaCliente=[];" +
            "function formatCop(val){ return '$ ' + Number(val).toLocaleString('es-CO'); }" +
            "async function initMenu(){" +
            "  try{" +
            "    const rInf = await fetch('/api/info'); const dInf = await rInf.json();" +
            "    if(dInf.nombre) document.getElementById('lbl-menu-empresa').textContent = dInf.nombre;" +
            "    const r = await fetch('/api/carta'); cartaCliente = await r.json();" +
            "    renderMenu(cartaCliente);" +
            "  }catch(e){}" +
            "}" +
            "function renderMenu(lista){" +
            "  const cont = document.getElementById('menu-list'); cont.innerHTML = '';" +
            "  lista.forEach(p => {" +
            "    const itm = document.createElement('div'); itm.className = 'menu-item';" +
            "    itm.innerHTML = '<span class=\"nom\">' + p.nombre + '</span><span class=\"prc\">' + formatCop(p.precio) + '</span>';" +
            "    cont.appendChild(itm);" +
            "  });" +
            "}" +
            "function filtrarMenu(){" +
            "  const q = document.getElementById('txt-buscar-cli').value.toLowerCase().trim();" +
            "  renderMenu(cartaCliente.filter(p => p.nombre.toLowerCase().includes(q)));" +
            "}" +
            "window.onload = initMenu;" +
            "</script></body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILIDADES HTTP & JSON
    // ─────────────────────────────────────────────────────────────────────────
    private static void responderJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, resp.length);
        ex.getResponseBody().write(resp);
        ex.getResponseBody().close();
    }

    private static void setCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static String getIpLocal() {
        String candidato = "localhost";
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                String name = iface.getName().toLowerCase();
                String displayName = iface.getDisplayName().toLowerCase();
                if (name.contains("docker") || name.contains("vbox") || name.contains("virtual") || displayName.contains("virtual") || displayName.contains("vmware")) continue;
                
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip;
                        }
                        candidato = ip;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return candidato;
    }
}
