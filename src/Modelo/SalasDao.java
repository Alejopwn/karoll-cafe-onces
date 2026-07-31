package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la gestión de salas y ambientes del restaurante.
 */
public class SalasDao {

    private final Conexion cn = new Conexion();

    public boolean RegistrarSala(Sala sl) {
        String sql = "INSERT INTO salas(nombre, mesas) VALUES (?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar sala: " + e.getMessage());
            return false;
        }
    }

    public List<Sala> Listar() {
        List<Sala> Lista = new ArrayList<>();
        String sql = "SELECT * FROM salas";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null) {
                while (rs.next()) {
                    Sala sl = new Sala();
                    sl.setId(rs.getInt("id"));
                    sl.setNombre(rs.getString("nombre"));
                    sl.setMesas(rs.getInt("mesas"));
                    Lista.add(sl);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar salas: " + e.getMessage());
        }
        return Lista;
    }

    public boolean Eliminar(int id) {
        String sql = "DELETE FROM salas WHERE id = ? ";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, id);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar sala: " + e.getMessage());
            return false;
        }
    }

    public boolean Modificar(Sala sl) {
        String sql = "UPDATE salas SET nombre=?, mesas=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.setInt(3, sl.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al modificar sala: " + e.getMessage());
            return false;
        }
    }

    public int buscarIdSalaPorNombre(String nombre) {
        String sql = "SELECT id FROM salas WHERE nombre = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return 0;
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar id de sala por nombre: " + e.getMessage());
        }
        return 0;
    }
}
