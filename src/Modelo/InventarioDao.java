package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioDao {

    private final Conexion cn = new Conexion();

    public boolean registrar(Inventario inv) {
        String sql = "INSERT INTO inventario (codigo, nombre, categoria, stock, stock_minimo, unidad_medida, precio_compra, fecha_actualizacion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, inv.getCodigo());
            ps.setString(2, inv.getNombre());
            ps.setString(3, inv.getCategoria() == null || inv.getCategoria().trim().isEmpty() ? "General" : inv.getCategoria());
            ps.setDouble(4, inv.getStock());
            ps.setDouble(5, inv.getStockMinimo());
            ps.setString(6, inv.getUnidadMedida() == null || inv.getUnidadMedida().trim().isEmpty() ? "Unidades" : inv.getUnidadMedida());
            ps.setDouble(7, inv.getPrecioCompra());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar producto en inventario: " + e.getMessage());
            return false;
        }
    }

    public List<Inventario> listar(String busqueda, String categoria) {
        List<Inventario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM inventario WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append("AND (nombre LIKE ? OR codigo LIKE ? OR CAST(id AS TEXT) LIKE ?) ");
            String term = "%" + busqueda.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (categoria != null && !categoria.trim().isEmpty() && !categoria.equalsIgnoreCase("Todas")) {
            sql.append("AND categoria = ? ");
            params.add(categoria.trim());
        }

        sql.append("ORDER BY nombre ASC");

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Inventario inv = new Inventario();
                    inv.setId(rs.getInt("id"));
                    inv.setCodigo(rs.getString("codigo"));
                    inv.setNombre(rs.getString("nombre"));
                    inv.setCategoria(rs.getString("categoria"));
                    inv.setStock(rs.getDouble("stock"));
                    inv.setStockMinimo(rs.getDouble("stock_minimo"));
                    inv.setUnidadMedida(rs.getString("unidad_medida"));
                    inv.setPrecioCompra(rs.getDouble("precio_compra"));
                    inv.setFechaActualizacion(rs.getString("fecha_actualizacion"));
                    lista.add(inv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar inventario: " + e.getMessage());
        }
        return lista;
    }

    public boolean modificar(Inventario inv) {
        String sql = "UPDATE inventario SET codigo = ?, nombre = ?, categoria = ?, stock = ?, stock_minimo = ?, "
                   + "unidad_medida = ?, precio_compra = ?, fecha_actualizacion = datetime('now', 'localtime') WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, inv.getCodigo());
            ps.setString(2, inv.getNombre());
            ps.setString(3, inv.getCategoria());
            ps.setDouble(4, inv.getStock());
            ps.setDouble(5, inv.getStockMinimo());
            ps.setString(6, inv.getUnidadMedida());
            ps.setDouble(7, inv.getPrecioCompra());
            ps.setInt(8, inv.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar inventario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM inventario WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar del inventario: " + e.getMessage());
            return false;
        }
    }

    public Inventario buscarPorId(int id) {
        String sql = "SELECT * FROM inventario WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Inventario(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("stock"),
                        rs.getDouble("stock_minimo"),
                        rs.getString("unidad_medida"),
                        rs.getDouble("precio_compra"),
                        rs.getString("fecha_actualizacion")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar inventario por ID: " + e.getMessage());
        }
        return null;
    }

    public boolean ajustarStock(int idInventario, String tipoMovimiento, double cantidad, String motivo, String usuario) {
        if (cantidad <= 0) return false;
        Inventario inv = buscarPorId(idInventario);
        if (inv == null) return false;

        double nuevoStock = inv.getStock();
        if (tipoMovimiento.equalsIgnoreCase("ENTRADA")) {
            nuevoStock += cantidad;
        } else if (tipoMovimiento.equalsIgnoreCase("SALIDA")) {
            nuevoStock = Math.max(0, nuevoStock - cantidad);
        } else if (tipoMovimiento.equalsIgnoreCase("AJUSTE")) {
            nuevoStock = cantidad;
        }

        String sqlUpdate = "UPDATE inventario SET stock = ?, fecha_actualizacion = datetime('now', 'localtime') WHERE id = ?";
        String sqlMov = "INSERT INTO movimientos_inventario (id_inventario, nombre_producto, tipo_movimiento, cantidad, motivo, usuario) "
                      + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = cn.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                 PreparedStatement psMov = con.prepareStatement(sqlMov)) {
                
                psUpdate.setDouble(1, nuevoStock);
                psUpdate.setInt(2, idInventario);
                psUpdate.executeUpdate();

                psMov.setInt(1, idInventario);
                psMov.setString(2, inv.getNombre());
                psMov.setString(3, tipoMovimiento.toUpperCase());
                psMov.setDouble(4, cantidad);
                psMov.setString(5, motivo);
                psMov.setString(6, usuario != null ? usuario : "Sistema");
                psMov.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                System.err.println("Error en transacción de ajuste de stock: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión al ajustar stock: " + e.getMessage());
            return false;
        }
    }

    public List<MovimientoInventario> listarMovimientos(String busqueda) {
        List<MovimientoInventario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, i.nombre AS prod_nombre FROM movimientos_inventario m "
          + "LEFT JOIN inventario i ON m.id_inventario = i.id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            sql.append("AND (m.nombre_producto LIKE ? OR m.tipo_movimiento LIKE ? OR m.motivo LIKE ? OR m.usuario LIKE ?) ");
            String term = "%" + busqueda.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY m.id DESC LIMIT 200");

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoInventario mov = new MovimientoInventario();
                    mov.setId(rs.getInt("id"));
                    mov.setIdInventario(rs.getInt("id_inventario"));
                    mov.setNombreProducto(rs.getString("nombre_producto"));
                    mov.setTipoMovimiento(rs.getString("tipo_movimiento"));
                    mov.setCantidad(rs.getDouble("cantidad"));
                    mov.setMotivo(rs.getString("motivo"));
                    mov.setUsuario(rs.getString("usuario"));
                    mov.setFecha(rs.getString("fecha"));
                    lista.add(mov);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar movimientos: " + e.getMessage());
        }
        return lista;
    }

    public List<Inventario> obtenerProductosStockBajo() {
        List<Inventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM inventario WHERE stock <= stock_minimo ORDER BY stock ASC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Inventario inv = new Inventario(
                    rs.getInt("id"),
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getDouble("stock"),
                    rs.getDouble("stock_minimo"),
                    rs.getString("unidad_medida"),
                    rs.getDouble("precio_compra"),
                    rs.getString("fecha_actualizacion")
                );
                lista.add(inv);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerCategorias() {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todas");
        String sql = "SELECT DISTINCT categoria FROM inventario WHERE categoria IS NOT NULL AND categoria != '' ORDER BY categoria ASC";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String cat = rs.getString("categoria");
                if (!cat.equalsIgnoreCase("Todas")) {
                    categorias.add(cat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }
        return categorias;
    }

    public boolean descontarStockPorNombre(String nombreItem, double cantidad, String usuario) {
        if (nombreItem == null || nombreItem.trim().isEmpty() || cantidad <= 0) return false;
        String sqlSearch = "SELECT id FROM inventario WHERE UPPER(nombre) = UPPER(?) OR UPPER(codigo) = UPPER(?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlSearch)) {
            ps.setString(1, nombreItem.trim());
            ps.setString(2, nombreItem.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idInv = rs.getInt("id");
                    return ajustarStock(idInv, "SALIDA", cantidad, "Venta realizada", usuario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al descontar por venta: " + e.getMessage());
        }
        return false;
    }
}
