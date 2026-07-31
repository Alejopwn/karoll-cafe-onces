package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de recordatorios en SQLite.
 */
public class RecordatoriosDao {

    private final Conexion cn = new Conexion();

    public RecordatoriosDao() {
        crearTabla();
    }

    private void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS recordatorios ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "titulo TEXT NOT NULL, "
                   + "descripcion TEXT DEFAULT '', "
                   + "fecha TEXT DEFAULT NULL, "
                   + "hora TEXT DEFAULT NULL, "
                   + "prioridad TEXT CHECK(prioridad IN ('ALTA','MEDIA','BAJA')) DEFAULT 'MEDIA', "
                   + "completado INTEGER DEFAULT 0)";
        try (Connection con = cn.getConnection();
             Statement st = con != null ? con.createStatement() : null) {
            if (st != null) st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla recordatorios: " + e.getMessage());
        }
    }

    public boolean guardar(Recordatorio r) {
        String sql = "INSERT INTO recordatorios (titulo, descripcion, fecha, hora, prioridad, completado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, r.getTitulo());
            ps.setString(2, r.getDescripcion());
            ps.setString(3, r.getFecha());
            ps.setString(4, r.getHora());
            ps.setString(5, r.getPrioridad() != null ? r.getPrioridad() : "MEDIA");
            ps.setInt(6, r.isCompletado() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar recordatorio: " + e.getMessage());
            return false;
        }
    }

    public List<Recordatorio> listar() {
        List<Recordatorio> lista = new ArrayList<>();
        String sql = "SELECT * FROM recordatorios ORDER BY completado ASC, fecha ASC, hora ASC";
        try (Connection con = cn.getConnection();
             Statement st = con != null ? con.createStatement() : null;
             ResultSet rs = st != null ? st.executeQuery(sql) : null) {
            if (rs == null) return lista;
            while (rs.next()) {
                Recordatorio r = new Recordatorio();
                r.setId(rs.getInt("id"));
                r.setTitulo(rs.getString("titulo"));
                r.setDescripcion(rs.getString("descripcion"));
                r.setFecha(rs.getString("fecha"));
                r.setHora(rs.getString("hora"));
                r.setPrioridad(rs.getString("prioridad"));
                r.setCompletado(rs.getInt("completado") == 1);
                lista.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar recordatorios: " + e.getMessage());
        }
        return lista;
    }

    public boolean marcarCompletado(int id, boolean completado) {
        String sql = "UPDATE recordatorios SET completado = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, completado ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar completado de recordatorio: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM recordatorios WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar recordatorio: " + e.getMessage());
            return false;
        }
    }
}
