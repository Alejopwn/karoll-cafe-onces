package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para el catálogo de platos y productos del menú.
 */
public class PlatosDao {

    private final Conexion cn = new Conexion();

    /**
     * Registra un nuevo plato en la base de datos.
     */
    public boolean Registrar(Plato pla) {
        String sql = "INSERT INTO platos (nombre, precio, fecha) VALUES (?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setString(3, pla.getFecha());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar plato: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la lista de platos, permitiendo filtrado por nombre o ID.
     */
    public List<Plato> Listar(String valor) {
        List<Plato> lista = new ArrayList<>();
        String sql = (valor == null || valor.trim().isEmpty())
                ? "SELECT * FROM platos"
                : "SELECT * FROM platos WHERE nombre LIKE ? OR CAST(id AS TEXT) LIKE ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return lista;
            if (valor != null && !valor.trim().isEmpty()) {
                String filtro = "%" + valor.trim() + "%";
                ps.setString(1, filtro);
                ps.setString(2, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Plato pl = new Plato();
                    pl.setId(rs.getInt("id"));
                    pl.setNombre(rs.getString("nombre"));
                    pl.setPrecio(rs.getDouble("precio"));
                    lista.add(pl);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar platos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Elimina un plato por su ID.
     */
    public boolean Eliminar(int id) {
        String sql = "DELETE FROM platos WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar plato con ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Modifica el nombre y precio de un plato existente.
     */
    public boolean Modificar(Plato pla) {
        String sql = "UPDATE platos SET nombre=?, precio=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setInt(3, pla.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al modificar plato: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un plato específico por su ID.
     */
    public Plato buscarPorId(int id) {
        Plato pl = null;
        String sql = "SELECT * FROM platos WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return null;
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pl = new Plato();
                    pl.setId(rs.getInt("id"));
                    pl.setNombre(rs.getString("nombre"));
                    pl.setPrecio(rs.getDouble("precio"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar plato por ID: " + e.getMessage());
        }
        return pl;
    }
}
