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
 * Servidor Web embebido para que los meseros tomen pedidos desde Android/iPhone.
 * Inicia en el puerto 8080. Abrir desde el celular: http://[IP-PC]:8080
 */
public class ServidorWebMesero {

    private static HttpServer server;
    private static boolean corriendo = false;
    private static int puerto = 8080;

    public static void iniciar() {
        if (corriendo) return;
        try {
            asegurarReglaFirewall();
            server = HttpServer.create(new InetSocketAddress(puerto), 0);
            server.createContext("/", ex -> manejarRaiz(ex));
            server.createContext("/api/salas", ex -> manejarSalas(ex));
            server.createContext("/api/carta", ex -> manejarCarta(ex));
            server.createContext("/api/pedido", ex -> manejarCrearPedido(ex));
            server.createContext("/api/agregar", ex -> manejarAgregarItem(ex));
            server.createContext("/api/estado", ex -> manejarEstadoMesa(ex));
            server.createContext("/api/items", ex -> manejarItems(ex));
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            corriendo = true;
            System.out.println("[ServidorWeb] ✅ Servidor de meseros iniciado en puerto " + puerto);
            System.out.println("[ServidorWeb] 📱 Accede desde Android: http://" + getIpLocal() + ":" + puerto);
        } catch (Exception e) {
            System.err.println("[ServidorWeb] ❌ Error al iniciar servidor: " + e.getMessage());
        }
    }

    private static void asegurarReglaFirewall() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                String cmd = "netsh advfirewall firewall add rule name=\"Comuneros POS - Panel Android\" dir=in action=allow protocol=TCP localport=" + puerto;
                Runtime.getRuntime().exec(cmd);
            }
        } catch (Exception ignored) {}
    }

    public static void detener() {
        if (server != null) { server.stop(0); corriendo = false; }
    }

    public static boolean isCorriendo() { return corriendo; }
    public static String getUrlAcceso() {
        return corriendo ? "http://" + getIpLocal() + ":" + puerto : "No iniciado";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Página principal — App web móvil
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarRaiz(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }
        String html = buildHtml();
        byte[] resp = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, resp.length);
        ex.getResponseBody().write(resp);
        ex.getResponseBody().close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API: salas
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarSalas(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
            SalasDao slDao = new SalasDao();
            List<Sala> salas = slDao.Listar();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < salas.size(); i++) {
                Sala s = salas.get(i);
                sb.append("{\"id\":").append(s.getId())
                  .append(",\"nombre\":\"").append(escape(s.getNombre()))
                  .append("\",\"mesas\":").append(s.getMesas()).append("}");
                if (i < salas.size() - 1) sb.append(",");
            }
            sb.append("]");
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[API/salas] Error: " + e.getMessage());
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API: carta / menú
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarCarta(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        try {
        PlatosDao plaDao = new PlatosDao();
        List<Plato> platos = plaDao.Listar(null);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < platos.size(); i++) {
            Plato p = platos.get(i);
            sb.append("{\"id\":").append(p.getId())
              .append(",\"nombre\":\"").append(escape(p.getNombre()))
              .append("\",\"precio\":").append(p.getPrecio()).append("}");
            if (i < platos.size() - 1) sb.append(",");
        }
        sb.append("]");
        responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[API/carta] Error: " + e.getMessage());
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API: estado de mesa (¿tiene pedido abierto?)
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarEstadoMesa(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
        int mesa = Integer.parseInt(params.getOrDefault("mesa", "0"));
        int sala = Integer.parseInt(params.getOrDefault("sala", "0"));
        PedidosDao pedDao = new PedidosDao();
        int idPedido = pedDao.verificarStado(mesa, sala);
        responderJson(ex, 200, "{\"id_pedido\":" + idPedido + "}");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API: crear nuevo pedido
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarCrearPedido(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { ex.sendResponseHeaders(405, -1); return; }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseJson(body);

        int mesa = Integer.parseInt(params.getOrDefault("mesa", "1"));
        int sala = Integer.parseInt(params.getOrDefault("sala", "1"));
        String mesero = params.getOrDefault("mesero", "Mesero");

        PedidosDao pedDao = new PedidosDao();

        // Verificar si ya existe un pedido abierto para esta mesa
        int idExistente = pedDao.verificarStado(mesa, sala);
        if (idExistente > 0) {
            responderJson(ex, 200, "{\"id_pedido\":" + idExistente + ",\"nuevo\":false}");
            return;
        }

        // Crear nuevo pedido
        Pedido ped = new Pedido();
        ped.setId_sala(sala);
        ped.setNum_mesa(mesa);
        ped.setTotal(0);
        ped.setUsuario(mesero);
        ped.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        int idNuevo = pedDao.RegistrarPedido(ped);
        if (idNuevo > 0) {
            responderJson(ex, 200, "{\"id_pedido\":" + idNuevo + ",\"nuevo\":true}");
        } else {
            responderJson(ex, 500, "{\"error\":\"No se pudo crear el pedido\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API: agregar ítem al pedido
    // ─────────────────────────────────────────────────────────────────────────
    private static void manejarAgregarItem(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        if (!ex.getRequestMethod().equals("POST")) { ex.sendResponseHeaders(405, -1); return; }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseJson(body);

        int idPedido = Integer.parseInt(params.getOrDefault("id_pedido", "0"));
        String nombre = params.getOrDefault("nombre", "");
        double precio = Double.parseDouble(params.getOrDefault("precio", "0"));
        int cantidad = Integer.parseInt(params.getOrDefault("cantidad", "1"));
        String comentario = params.getOrDefault("comentario", "");

        if (idPedido <= 0 || nombre.isEmpty()) {
            responderJson(ex, 400, "{\"error\":\"Datos incompletos\"}");
            return;
        }

        PedidosDao pedDao = new PedidosDao();

        // Insertar detalle usando SQL directo vía Conexion
        try {
            Conexion cn = new Conexion();
            java.sql.Connection con = cn.getConnection();
            if (con == null) throw new Exception("Sin conexión");

            // Insertar ítem
            String sqlItem = "INSERT INTO detalle_pedidos (id_pedido, nombre, precio, cantidad, comentario) VALUES (?,?,?,?,?)";
            java.sql.PreparedStatement ps = con.prepareStatement(sqlItem);
            ps.setInt(1, idPedido);
            ps.setString(2, nombre);
            ps.setDouble(3, precio);
            ps.setInt(4, cantidad);
            ps.setString(5, comentario);
            ps.executeUpdate();
            ps.close();

            // Actualizar total del pedido
            String sqlTotal = "UPDATE pedidos SET total = (SELECT SUM(precio*cantidad) FROM detalle_pedidos WHERE id_pedido=?) WHERE id=?";
            java.sql.PreparedStatement ps2 = con.prepareStatement(sqlTotal);
            ps2.setInt(1, idPedido);
            ps2.setInt(2, idPedido);
            ps2.executeUpdate();
            ps2.close();
            con.close();

            responderJson(ex, 200, "{\"ok\":true,\"mensaje\":\"" + escape(nombre) + " x" + cantidad + " agregado\"}");
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ───────────────────────────────────────────────────────────────────────────
    // API: obtener ítems de un pedido abierto (para restaurar carrito en celular)
    // ───────────────────────────────────────────────────────────────────────────
    private static void manejarItems(HttpExchange ex) throws IOException {
        setCors(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
        int idPedido = Integer.parseInt(params.getOrDefault("id_pedido", "0"));
        if (idPedido <= 0) {
            responderJson(ex, 400, "{\"error\":\"id_pedido requerido\"}");
            return;
        }
        try {
            Conexion cn = new Conexion();
            java.sql.Connection con = cn.getConnection();
            if (con == null) throw new Exception("Sin conexi\u00f3n");
            String sql = "SELECT nombre, precio, cantidad, comentario FROM detalle_pedidos WHERE id_pedido=?";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idPedido);
            java.sql.ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                sb.append("{");
                sb.append("\"nombre\":\"").append(escape(rs.getString("nombre"))).append("\",");
                sb.append("\"precio\":").append(rs.getDouble("precio")).append(",");
                sb.append("\"cantidad\":").append(rs.getInt("cantidad")).append(",");
                sb.append("\"comentario\":\"").append(escape(rs.getString("comentario"))).append("\"");
                sb.append("}");
                first = false;
            }
            sb.append("]");
            rs.close(); ps.close(); con.close();
            responderJson(ex, 200, sb.toString());
        } catch (Exception e) {
            responderJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTML de la App Móvil (PWA)
    // ─────────────────────────────────────────────────────────────────────────
    private static String buildHtml() {
        return "<!DOCTYPE html><html lang='es'><head>" +
            "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<meta name='theme-color' content='#0f172a'>" +
            "<title>Comuneros — Pedidos</title>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#0f172a;color:#f1f5f9;min-height:100vh}" +
            ".navbar{background:#1e293b;border-bottom:1px solid #334155;padding:14px 16px;display:flex;align-items:center;gap:12px;position:sticky;top:0;z-index:100}" +
            ".navbar h1{font-size:18px;font-weight:700;color:#38bdf8}" +
            ".navbar .badge{background:#ef4444;color:#fff;border-radius:999px;padding:2px 8px;font-size:12px;font-weight:700}" +
            ".section{padding:16px}" +
            ".section-title{font-size:13px;color:#94a3b8;font-weight:600;letter-spacing:.5px;text-transform:uppercase;margin-bottom:10px}" +
            ".grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}" +
            ".grid-2{display:grid;grid-template-columns:repeat(2,1fr);gap:10px}" +
            ".card{background:#1e293b;border:1px solid #334155;border-radius:12px;padding:14px 10px;text-align:center;cursor:pointer;transition:all .15s}" +
            ".card:active{transform:scale(.96);background:#273548}" +
            ".card.active{border-color:#38bdf8;background:#1c3a54}" +
            ".card.ocupada{border-color:#f59e0b;background:#1e1b10}" +
            ".card h3{font-size:14px;font-weight:700;margin-bottom:4px}" +
            ".card p{font-size:12px;color:#94a3b8}" +
            ".carta-item{background:#1e293b;border:1px solid #334155;border-radius:12px;padding:14px;display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;cursor:pointer;transition:all .15s}" +
            ".carta-item:active{transform:scale(.98);background:#273548}" +
            ".carta-item .nombre{font-size:15px;font-weight:600}" +
            ".carta-item .precio{font-size:14px;font-weight:700;color:#34d399}" +
            ".btn-add{background:#38bdf8;border:none;border-radius:999px;width:34px;height:34px;font-size:22px;color:#0f172a;font-weight:900;cursor:pointer;display:flex;align-items:center;justify-content:center}" +
            ".footer-cart{position:fixed;bottom:0;left:0;right:0;background:#1e293b;border-top:1px solid #334155;padding:16px;z-index:200}" +
            ".btn-primary{width:100%;padding:16px;background:linear-gradient(135deg,#0ea5e9,#6366f1);border:none;border-radius:14px;color:#fff;font-size:17px;font-weight:700;cursor:pointer;letter-spacing:.3px}" +
            ".btn-primary:active{opacity:.85}" +
            ".btn-secondary{width:100%;padding:12px;background:#334155;border:none;border-radius:14px;color:#94a3b8;font-size:15px;font-weight:600;cursor:pointer;margin-top:8px}" +
            ".cart-preview{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}" +
            ".cart-count{font-size:14px;color:#94a3b8}" +
            ".cart-total{font-size:18px;font-weight:700;color:#34d399}" +
            ".cart-items{max-height:200px;overflow-y:auto;margin-bottom:12px}" +
            ".cart-row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #334155;font-size:14px}" +
            ".toast{position:fixed;top:80px;left:50%;transform:translateX(-50%);background:#22c55e;color:#fff;padding:12px 24px;border-radius:999px;font-size:14px;font-weight:600;opacity:0;transition:opacity .3s;pointer-events:none;z-index:999}" +
            ".toast.show{opacity:1}" +
            ".tag-mesa{display:inline-block;background:#1c3a54;border:1px solid #38bdf8;border-radius:999px;padding:4px 14px;font-size:13px;color:#38bdf8;font-weight:700;margin-left:auto}" +
            "input[type=number]{background:#0f172a;border:1px solid #475569;border-radius:8px;color:#fff;padding:8px 12px;font-size:15px;width:60px;text-align:center}" +
            ".modal{display:none;position:fixed;inset:0;background:rgba(0,0,0,.7);z-index:300;align-items:center;justify-content:center}" +
            ".modal.open{display:flex}" +
            ".modal-box{background:#1e293b;border:1px solid #334155;border-radius:18px;padding:24px;width:90%;max-width:340px}" +
            ".modal-box h3{font-size:17px;font-weight:700;margin-bottom:16px;color:#f1f5f9}" +
            ".modal-box label{display:block;font-size:13px;color:#94a3b8;margin-bottom:4px}" +
            ".modal-box input[type=text]{width:100%;background:#0f172a;border:1px solid #475569;border-radius:8px;color:#fff;padding:10px 12px;font-size:15px;margin-bottom:16px}" +
            ".modal-row{display:flex;gap:10px;align-items:center;margin-bottom:16px}" +
            "#step-salas,#step-mesas,#step-carta,#step-cart{display:none}" +
            "#step-salas.active,#step-mesas.active,#step-carta.active,#step-cart.active{display:block}" +
            ".back-btn{background:none;border:none;color:#94a3b8;font-size:14px;cursor:pointer;padding:4px 0;display:flex;align-items:center;gap:6px}" +
            ".empty{text-align:center;padding:40px 20px;color:#64748b;font-size:14px}" +
            ".search-bar{background:#1e293b;border:1px solid #334155;border-radius:10px;padding:10px 14px;color:#f1f5f9;font-size:15px;width:100%;margin-bottom:14px}" +
            "</style>" +
            "</head><body>" +
            "<div class='navbar'>" +
            "  <div>🍴</div>" +
            "  <h1>Comuneros POS</h1>" +
            "  <div id='mesa-tag'></div>" +
            "</div>" +
            "<div class='toast' id='toast'></div>" +

            "<!-- PASO 1: SALAS -->" +
            "<div id='step-salas' class='active'>" +
            "  <div class='section'><div class='section-title'>Selecciona tu sala</div>" +
            "  <div class='grid' id='lista-salas'><div class='empty'>⏳ Cargando...</div></div></div>" +
            "</div>" +

            "<!-- PASO 2: MESAS -->" +
            "<div id='step-mesas'>" +
            "  <div class='section'><button class='back-btn' onclick='goStep(\"salas\")'>← Volver</button>" +
            "  <div class='section-title' style='margin-top:12px'>Selecciona la mesa</div>" +
            "  <div class='grid' id='lista-mesas'></div></div>" +
            "</div>" +

            "<!-- PASO 3: CARTA -->" +
            "<div id='step-carta'>" +
            "  <div class='section'><button class='back-btn' onclick='goStep(\"mesas\")'>← Volver</button>" +
            "  <div class='section-title' style='margin-top:12px'>Carta del menú</div>" +
            "  <input type='text' class='search-bar' placeholder='🔍 Buscar plato...' id='buscador' oninput='filtrarCarta()'>" +
            "  <div id='lista-carta'><div class='empty'>⏳ Cargando carta...</div></div>" +
            "  </div>" +
            "  <div style='height:140px'></div>" +
            "  <div class='footer-cart' id='footer-cart'>" +
            "    <div class='cart-preview'><span class='cart-count' id='lbl-count'>Carrito vacío</span><span class='cart-total' id='lbl-total'>$0</span></div>" +
            "    <button class='btn-primary' onclick='goStep(\"cart\")'>Ver pedido →</button>" +
            "  </div>" +
            "</div>" +

            "<!-- PASO 4: RESUMEN Y CONFIRMAR -->" +
            "<div id='step-cart'>" +
            "  <div class='section'><button class='back-btn' onclick='goStep(\"carta\")'>← Agregar más</button>" +
            "  <div class='section-title' style='margin-top:12px'>Resumen del pedido</div>" +
            "  <div class='cart-items' id='resumen-items'></div>" +
            "  <div style='display:flex;justify-content:space-between;padding:12px 0;border-top:1px solid #334155;font-weight:700;font-size:16px'>" +
            "    <span>Total</span><span id='resumen-total' style='color:#34d399'></span></div>" +
            "  <button class='btn-primary' id='btn-confirmar' onclick='confirmarPedido()'>✅ Confirmar Pedido</button>" +
            "  <button class='btn-secondary' onclick='cancelarCarrito()'>🗑 Cancelar</button>" +
            "  </div>" +
            "</div>" +

            "<!-- MODAL CANTIDAD -->" +
            "<div class='modal' id='modal-cantidad'>" +
            "  <div class='modal-box'>" +
            "    <h3 id='modal-nombre'>Agregar plato</h3>" +
            "    <div class='modal-row'><label>Cantidad:</label><input type='number' id='modal-qty' value='1' min='1' max='20'></div>" +
            "    <label>Nota/Comentario (opcional):</label>" +
            "    <input type='text' id='modal-nota' placeholder='Ej: sin cebolla...'>" +
            "    <button class='btn-primary' onclick='agregarAlCarrito()'>Agregar al carrito</button>" +
            "    <button class='btn-secondary' onclick='cerrarModal()'>Cancelar</button>" +
            "  </div>" +
            "</div>" +

            "<script>" +
            "let salaId=0,salaNombre='',mesaNum=0,idPedido=0,meseroNombre='Mesero';" +
            "let carrito=[],todaLaCarta=[];" +
            "let platoActual=null;" +

            "function goStep(s){" +
            "  document.querySelectorAll('[id^=step-]').forEach(e=>e.classList.remove('active'));" +
            "  document.getElementById('step-'+s).classList.add('active');" +
            "  if(s==='carta'&&todaLaCarta.length===0) cargarCarta();" +
            "}" +

            "function toast(msg,color='#22c55e'){" +
            "  const t=document.getElementById('toast');t.textContent=msg;t.style.background=color;" +
            "  t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2500);" +
            "}" +

            // Cargar salas
            "async function cargarSalas(){" +
            "  const c=document.getElementById('lista-salas');" +
            "  c.innerHTML=\"<div class='empty'>⏳ Conectando al servidor...</div>\";" +
            "  try{" +
            "    const r=await fetch('/api/salas');const data=await r.json();" +
            "    c.innerHTML='';" +
            "    if(!data.length){c.innerHTML=\"<div class='empty'>No hay salas registradas.<br>Créalas en el POS (Admin)</div>\";return;}" +
            "    data.forEach(s=>{const d=document.createElement('div');" +
            "      d.className='card';d.innerHTML='<h3>'+s.nombre+'</h3><p>'+s.mesas+' mesas</p>';" +
            "      d.onclick=()=>selSala(s.id,s.nombre,s.mesas);c.appendChild(d);});" +
            "  }catch(e){" +
            "    c.innerHTML=\"<div class='empty' style='color:#f87171'>❌ No se pudo conectar al POS.<br><small>Asegúrate que el programa esté abierto</small><br><br><button class='btn-primary' style='width:auto;padding:10px 20px;font-size:14px' onclick='cargarSalas()'>🔄 Reintentar</button></div>\";" +
            "  }" +
            "}" +

            "function selSala(id,nom,cant){" +
            "  salaId=id;salaNombre=nom;" +
            "  const c=document.getElementById('lista-mesas');c.innerHTML='';" +
            "  for(let i=1;i<=cant;i++){" +
            "    const d=document.createElement('div');" +
            "    d.className='card';d.id='mesa-card-'+i;" +
            "    d.innerHTML='<h3>Mesa '+i+'</h3><p>Toca para abrir</p>';" +
            "    d.onclick=()=>selMesa(i);c.appendChild(d);" +
            "  }" +
            "  for(let i=1;i<=cant;i++){" +
            "    fetch('/api/estado?mesa='+i+'&sala='+id).then(r=>r.json()).then(d=>{" +
            "      const card=document.getElementById('mesa-card-'+i);" +
            "      if(d.id_pedido>0&&card){card.classList.add('ocupada');card.querySelector('p').textContent='Ocupada';}" +
            "    });" +
            "  }" +
            "  goStep('mesas');" +
            "}" +

            "async function selMesa(num){" +
            "  mesaNum=num;" +
            "  carrito=[];" +
            "  document.getElementById('mesa-tag').innerHTML='<span class=tag-mesa>'+salaNombre+' \u00b7 Mesa '+num+'</span>';" +
            "  const r=await fetch('/api/pedido',{method:'POST',headers:{'Content-Type':'application/json'}," +
            "    body:JSON.stringify({mesa:num,sala:salaId,mesero:meseroNombre})});" +
            "  const data=await r.json();" +
            "  if(data.id_pedido){" +
            "    idPedido=data.id_pedido;" +
            "    if(!data.nuevo){" +
            "      try{" +
            "        const ri=await fetch('/api/items?id_pedido='+idPedido);" +
            "        const items=await ri.json();" +
            "        if(items&&items.length>0){" +
            "          items.forEach(it=>{" +
            "            carrito.push({id:0,nombre:it.nombre,precio:Number(it.precio),cantidad:it.cantidad,comentario:it.comentario||''});" +
            "          });" +
            "          actualizarCarrito();" +
            "          toast('\u2705 Comanda restaurada: '+items.length+' \u00edtem(s)','#22c55e');" +
            "        } else {" +
            "          toast('Mesa con pedido abierto \u2014 agregando m\u00e1s \u00edtems','#f59e0b');" +
            "        }" +
            "      }catch(e){toast('Mesa con pedido abierto','#f59e0b');}" +
            "    } else {" +
            "      toast('Pedido nuevo creado','#22c55e');" +
            "    }" +
            "  }" +
            "  goStep('carta');" +
            "}" +

            "async function cargarCarta(){" +
            "  const r=await fetch('/api/carta');todaLaCarta=await r.json();" +
            "  renderCarta(todaLaCarta);" +
            "}" +

            "function renderCarta(lista){" +
            "  const c=document.getElementById('lista-carta');c.innerHTML='';" +
            "  if(!lista.length){c.innerHTML='<div class=empty>No hay platos</div>';return;}" +
            "  lista.forEach(p=>{const d=document.createElement('div');" +
            "    d.className='carta-item';d.innerHTML=" +
            "      '<div><div class=nombre>'+p.nombre+'</div></div>'+" +
            "      '<div style=\"display:flex;align-items:center;gap:12px\">'+" +
            "        '<span class=precio>$'+p.precio.toLocaleString('es-CO')+'</span>'+" +
            "        '<button class=btn-add onclick=\"abrirModal('+p.id+')\">+</button>'+" +
            "      '</div>';" +
            "    c.appendChild(d);});" +
            "}" +

            "function filtrarCarta(){" +
            "  const q=document.getElementById('buscador').value.toLowerCase();" +
            "  renderCarta(todaLaCarta.filter(p=>p.nombre.toLowerCase().includes(q)));" +
            "}" +

            "function abrirModal(id){" +
            "  platoActual=todaLaCarta.find(x=>x.id===id);" +
            "  document.getElementById('modal-nombre').textContent='➕ '+platoActual.nombre;" +
            "  document.getElementById('modal-qty').value=1;" +
            "  document.getElementById('modal-nota').value='';" +
            "  document.getElementById('modal-cantidad').classList.add('open');" +
            "}" +

            "function cerrarModal(){document.getElementById('modal-cantidad').classList.remove('open');}" +

            "function agregarAlCarrito(){" +
            "  const qty=parseInt(document.getElementById('modal-qty').value)||1;" +
            "  const nota=document.getElementById('modal-nota').value;" +
            "  carrito.push({...platoActual,cantidad:qty,comentario:nota});" +
            "  actualizarCarrito();" +
            "  toast('✅ '+platoActual.nombre+' x'+qty+' en el carrito');" +
            "  cerrarModal();" +
            "}" +

            "function actualizarCarrito(){" +
            "  const total=carrito.reduce((s,i)=>s+i.precio*i.cantidad,0);" +
            "  document.getElementById('lbl-count').textContent=carrito.length+' ítem(s)';" +
            "  document.getElementById('lbl-total').textContent='$'+total.toLocaleString('es-CO');" +
            "  const rs=document.getElementById('resumen-items');rs.innerHTML='';" +
            "  carrito.forEach(item=>{const d=document.createElement('div');d.className='cart-row';" +
            "    d.innerHTML='<span>'+item.nombre+(item.comentario?' <small>('+item.comentario+')</small>':'')+'</span>'+" +
            "      '<span>x'+item.cantidad+' $'+(item.precio*item.cantidad).toLocaleString('es-CO')+'</span>';" +
            "    rs.appendChild(d);});" +
            "  document.getElementById('resumen-total').textContent='$'+total.toLocaleString('es-CO');" +
            "}" +

            "async function confirmarPedido(){" +
            "  if(!carrito.length){toast('El carrito está vacío','#ef4444');return;}" +
            "  if(!idPedido){toast('Error: no hay pedido activo','#ef4444');return;}" +
            "  const btn=document.getElementById('btn-confirmar');" +
            "  btn.textContent='⏳ Enviando...';btn.disabled=true;" +
            "  let ok=true;" +
            "  for(const item of carrito){" +
            "    const r=await fetch('/api/agregar',{method:'POST',headers:{'Content-Type':'application/json'}," +
            "      body:JSON.stringify({id_pedido:idPedido,nombre:item.nombre,precio:item.precio,cantidad:item.cantidad,comentario:item.comentario})});" +
            "    const d=await r.json();if(!d.ok){ok=false;toast('Error: '+d.error,'#ef4444');break;}" +
            "  }" +
            "  if(ok){" +
            "    toast('🎉 Pedido enviado a cocina!','#22c55e');" +
            "    carrito=[];idPedido=0;mesaNum=0;" +
            "    document.getElementById('mesa-tag').innerHTML='';" +
            "    actualizarCarrito();" +
            "    setTimeout(()=>{goStep('salas');cargarSalas();},1500);" +
            "  }" +
            "  btn.textContent='✅ Confirmar Pedido';btn.disabled=false;" +
            "}" +

            "function cancelarCarrito(){\n" +
            "  if(confirm('¿Cancelar el carrito?')){carrito=[];actualizarCarrito();goStep('carta');}\n" +
            "}\n" +
            "cargarSalas();\n" +
            "</script></body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
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
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return map;
    }

    /** Parser JSON mínimo para objetos planos {"key":"val"} */
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim().replaceAll("^\\{|\\}$", "");
        for (String tok : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = tok.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replaceAll("\"", "");
                String v = kv[1].trim().replaceAll("\"", "");
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
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "localhost";
    }
}
