package Modelo;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para gestión de tareas de empleados en SQLite.
 */
public class TareasDao {

    private final Conexion cn = new Conexion();

    public TareasDao() {
        crearTabla();
    }

    private void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS tareas ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "titulo TEXT NOT NULL, "
                   + "descripcion TEXT DEFAULT '', "
                   + "asignado_a TEXT DEFAULT 'Todos', "
                   + "completada INTEGER DEFAULT 0, "
                   + "repeticion TEXT CHECK(repeticion IN ('NINGUNA','DIARIA','SEMANAL','MENSUAL')) DEFAULT 'NINGUNA', "
                   + "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP, "
                   + "fecha_completado TEXT DEFAULT NULL)";
        try (Connection con = cn.getConnection();
             Statement st = con != null ? con.createStatement() : null) {
            if (st != null) st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla tareas: " + e.getMessage());
        }
    }

    public boolean guardar(Tarea t) {
        String sql = "INSERT INTO tareas (titulo, descripcion, asignado_a, completada, repeticion, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, t.getTitulo());
            ps.setString(2, t.getDescripcion());
            ps.setString(3, t.getAsignadoA() != null ? t.getAsignadoA() : "Todos");
            ps.setInt(4, t.isCompletada() ? 1 : 0);
            ps.setString(5, t.getRepeticion() != null ? t.getRepeticion() : "NINGUNA");
            ps.setString(6, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar tarea: " + e.getMessage());
            return false;
        }
    }

    public List<Tarea> listar() {
        List<Tarea> lista = new ArrayList<>();
        String sql = "SELECT * FROM tareas ORDER BY completada ASC, id DESC";
        try (Connection con = cn.getConnection();
             Statement st = con != null ? con.createStatement() : null;
             ResultSet rs = st != null ? st.executeQuery(sql) : null) {
            if (rs == null) return lista;
            while (rs.next()) {
                Tarea t = new Tarea();
                t.setId(rs.getInt("id"));
                t.setTitulo(rs.getString("titulo"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setAsignadoA(rs.getString("asignado_a"));
                t.setCompletada(rs.getInt("completada") == 1);
                t.setRepeticion(rs.getString("repeticion"));
                t.setFechaCreacion(rs.getString("fecha_creacion"));
                t.setFechaCompletado(rs.getString("fecha_completado"));
                lista.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar tareas: " + e.getMessage());
        }
        return lista;
    }

    public boolean marcarCompletada(int id, boolean completada) {
        String fecha = completada ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : null;
        String sql = "UPDATE tareas SET completada = ?, fecha_completado = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, completada ? 1 : 0);
            ps.setString(2, fecha);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al marcar tarea: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM tareas WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar tarea: " + e.getMessage());
            return false;
        }
    }
}
