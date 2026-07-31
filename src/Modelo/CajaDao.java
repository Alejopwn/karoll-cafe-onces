package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CajaDao {

    Conexion cn = new Conexion();

    /**
     * Verifica si existe una caja con estado 'ABIERTA'.
     */
    public boolean hayCajaAbierta() {
        String sql = "SELECT COUNT(*) FROM cierre_caja WHERE estado = 'ABIERTA'";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar caja abierta: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene la caja actualmente abierta, o null si no hay ninguna.
     */
    public CierreCaja obtenerCajaActiva() {
        String sql = "SELECT * FROM cierre_caja WHERE estado = 'ABIERTA' ORDER BY id DESC LIMIT 1";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapearCierreCaja(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener caja activa: " + e.getMessage());
        }
        return null;
    }

    /**
     * Abre un nuevo turno de caja.
     */
    public boolean abrirCaja(String usuario, double montoInicial) {
        if (hayCajaAbierta()) {
            System.err.println("Ya existe un turno de caja abierto.");
            return false;
        }
        String fechaActual = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String sql = "INSERT INTO cierre_caja (usuario, monto_inicial, fecha_apertura, estado) VALUES (?, ?, ?, 'ABIERTA')";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setDouble(2, montoInicial);
            ps.setString(3, fechaActual);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al abrir caja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra un egreso/gasto de caja chica en la caja activa.
     */
    public boolean registrarGasto(int idCierre, double monto, String descripcion, String categoria, String usuario) {
        String sql = "INSERT INTO gastos_caja (id_cierre, monto, descripcion, categoria, usuario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            ps.setDouble(2, monto);
            ps.setString(3, descripcion);
            ps.setString(4, (categoria == null || categoria.trim().isEmpty()) ? "Varios" : categoria);
            ps.setString(5, usuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar gasto de caja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista los gastos de una caja específica.
     */
    public List<GastoCaja> listarGastosPorCierre(int idCierre) {
        List<GastoCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos_caja WHERE id_cierre = ? ORDER BY id DESC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GastoCaja g = new GastoCaja();
                    g.setId(rs.getInt("id"));
                    g.setIdCierre(rs.getInt("id_cierre"));
                    g.setMonto(rs.getDouble("monto"));
                    g.setDescripcion(rs.getString("descripcion"));
                    g.setCategoria(rs.getString("categoria"));
                    g.setUsuario(rs.getString("usuario"));
                    g.setFecha(rs.getString("fecha"));
                    lista.add(g);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar gastos de caja: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcula las ventas en efectivo generadas desde la fecha de apertura o id_cierre.
     */
    public double calcularVentasEfectivo(int idCierre, String fechaApertura) {
        String sql = "SELECT COALESCE(SUM(pago_efectivo), 0) FROM pedidos WHERE (id_cierre = ? OR fecha >= ?) AND estado = 'FINALIZADO'";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            ps.setString(2, fechaApertura != null ? fechaApertura : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular ventas efectivo: " + e.getMessage());
        }
        return 0.0;
    }

    public double calcularVentasEfectivo(String fechaApertura) {
        CierreCaja c = obtenerCajaActiva();
        int idC = c != null ? c.getId() : -1;
        return calcularVentasEfectivo(idC, fechaApertura);
    }

    /**
     * Calcula las ventas electrónicas (transacción) generadas desde la fecha de apertura o id_cierre.
     */
    public double calcularVentasTransaccion(int idCierre, String fechaApertura) {
        String sql = "SELECT COALESCE(SUM(pago_transaccion), 0) FROM pedidos WHERE (id_cierre = ? OR fecha >= ?) AND estado = 'FINALIZADO'";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            ps.setString(2, fechaApertura != null ? fechaApertura : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular ventas transacción: " + e.getMessage());
        }
        return 0.0;
    }

    public double calcularVentasTransaccion(String fechaApertura) {
        CierreCaja c = obtenerCajaActiva();
        int idC = c != null ? c.getId() : -1;
        return calcularVentasTransaccion(idC, fechaApertura);
    }

    /**
     * Suma total de gastos registrados durante el turno.
     */
    public double calcularTotalGastos(int idCierre) {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM gastos_caja WHERE id_cierre = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular total gastos: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Cierra el turno de caja realizando el arqueo final.
     */
    public boolean cerrarCaja(int idCierre, double montoRealEfectivo, String observaciones) {
        CierreCaja caja = null;
        String queryBusqueda = "SELECT * FROM cierre_caja WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(queryBusqueda)) {
            ps.setInt(1, idCierre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    caja = mapearCierreCaja(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar caja para cierre: " + e.getMessage());
            return false;
        }

        if (caja == null) return false;

        double ventasEfectivo = calcularVentasEfectivo(idCierre, caja.getFechaApertura());
        double ventasTransaccion = calcularVentasTransaccion(idCierre, caja.getFechaApertura());
        double gastos = calcularTotalGastos(idCierre);
        double esperadoEfectivo = caja.getMontoInicial() + ventasEfectivo - gastos;
        double diferencia = montoRealEfectivo - esperadoEfectivo;
        String fechaCierreActual = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        String updateSql = "UPDATE cierre_caja SET monto_ventas_efectivo = ?, monto_ventas_transaccion = ?, "
                + "monto_gastos = ?, monto_esperado_efectivo = ?, monto_real_efectivo = ?, diferencia = ?, "
                + "estado = 'CERRADA', fecha_cierre = ?, observaciones = ? WHERE id = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setDouble(1, ventasEfectivo);
            ps.setDouble(2, ventasTransaccion);
            ps.setDouble(3, gastos);
            ps.setDouble(4, esperadoEfectivo);
            ps.setDouble(5, montoRealEfectivo);
            ps.setDouble(6, diferencia);
            ps.setString(7, fechaCierreActual);
            ps.setString(8, observaciones);
            ps.setInt(9, idCierre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cerrar caja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el historial de todos los cierres de caja registrados.
     */
    public List<CierreCaja> listarHistorialCajas() {
        List<CierreCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM cierre_caja ORDER BY id DESC";
        try (Connection con = cn.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearCierreCaja(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historial de cajas: " + e.getMessage());
        }
        return lista;
    }

    private CierreCaja mapearCierreCaja(ResultSet rs) throws SQLException {
        CierreCaja c = new CierreCaja();
        c.setId(rs.getInt("id"));
        c.setUsuario(rs.getString("usuario"));
        c.setMontoInicial(rs.getDouble("monto_inicial"));
        c.setFechaApertura(rs.getString("fecha_apertura"));
        c.setMontoVentasEfectivo(rs.getDouble("monto_ventas_efectivo"));
        c.setMontoVentasTransaccion(rs.getDouble("monto_ventas_transaccion"));
        c.setMontoGastos(rs.getDouble("monto_gastos"));
        c.setMontoEsperadoEfectivo(rs.getDouble("monto_esperado_efectivo"));
        c.setMontoRealEfectivo(rs.getDouble("monto_real_efectivo"));
        c.setDiferencia(rs.getDouble("diferencia"));
        c.setEstado(rs.getString("estado"));
        c.setFechaCierre(rs.getString("fecha_cierre"));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }
}
