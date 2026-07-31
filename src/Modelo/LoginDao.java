package Modelo;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la autenticación y gestión de usuarios.
 */
public class LoginDao {

    private final Conexion cn = new Conexion();

    /**
     * Autentica un usuario verificando correo y contraseña (BCrypt o texto plano legado).
     */
    public Login log(String correoONombre, String pass) {
        Login l = new Login();
        String sql = "SELECT * FROM usuarios WHERE correo = ? OR nombre = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return l;
            ps.setString(1, correoONombre);
            ps.setString(2, correoONombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("pass");
                    boolean passwordValida;
                    if (hashGuardado != null && hashGuardado.startsWith("$2a$")) {
                        passwordValida = BCrypt.checkpw(pass, hashGuardado);
                    } else {
                        passwordValida = pass.equals(hashGuardado);
                        if (passwordValida) {
                            migrarPasswordBCrypt(rs.getInt("id"), pass);
                            System.out.println("Contraseña migrada a BCrypt para usuario: " + correoONombre);
                        }
                    }
                    if (passwordValida) {
                        l.setId(rs.getInt("id"));
                        l.setNombre(rs.getString("nombre"));
                        l.setCorreo(rs.getString("correo"));
                        l.setPass(rs.getString("pass"));
                        l.setRol(rs.getString("rol"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en inicio de sesión: " + e.getMessage());
        }
        return l;
    }

    /**
     * Migra una contraseña de texto plano a hash BCrypt en la base de datos.
     */
    private void migrarPasswordBCrypt(int userId, String passPlano) {
        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(12));
        String sql = "UPDATE usuarios SET pass = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return;
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error migrando contraseña: " + e.getMessage());
        }
    }

    /**
     * Registra un nuevo usuario cifrando su contraseña.
     */
    public boolean Registrar(Login reg) {
        String hashPassword = BCrypt.hashpw(reg.getPass(), BCrypt.gensalt(12));
        String sql = "INSERT INTO usuarios (nombre, correo, pass, rol) VALUES (?,?,?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, reg.getNombre());
            ps.setString(2, reg.getCorreo());
            ps.setString(3, hashPassword);
            ps.setString(4, reg.getRol());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario por su ID si no tiene pedidos asociados.
     */
    public boolean eliminarUsuario(int id) {
        Login u = buscarUsuarioPorId(id);
        if (u == null) return false;

        String checkQuery = "SELECT COUNT(*) FROM pedidos WHERE usuario = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(checkQuery) : null) {
            if (ps == null) return false;
            ps.setString(1, u.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("No se puede eliminar el usuario " + u.getNombre() + " porque tiene pedidos registrados.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar pedidos del usuario: " + e.getMessage());
            return false;
        }

        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     */
    public List<Login> ListarUsuarios() {
        List<Login> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null) {
                while (rs.next()) {
                    Login lg = new Login();
                    lg.setId(rs.getInt("id"));
                    lg.setNombre(rs.getString("nombre"));
                    lg.setCorreo(rs.getString("correo"));
                    lg.setRol(rs.getString("rol"));
                    lista.add(lg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Modifica los datos de configuración de la empresa.
     */
    public boolean ModificarDatos(Config conf) {
        String sql = "UPDATE config SET ruc=?, nombre=?, telefono=?, direccion=?, mensaje=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, conf.getRuc());
            ps.setString(2, conf.getNombre());
            ps.setString(3, conf.getTelefono());
            ps.setString(4, conf.getDireccion());
            ps.setString(5, conf.getMensaje());
            ps.setInt(6, conf.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al modificar datos de la empresa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los datos de configuración de la empresa.
     */
    public Config datosEmpresa() {
        Config conf = new Config();
        String sql = "SELECT * FROM config";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null;
             ResultSet rs = ps != null ? ps.executeQuery() : null) {
            if (rs != null && rs.next()) {
                conf.setId(rs.getInt("id"));
                conf.setRuc(rs.getString("ruc"));
                conf.setNombre(rs.getString("nombre"));
                conf.setTelefono(rs.getString("telefono"));
                conf.setDireccion(rs.getString("direccion"));
                conf.setMensaje(rs.getString("mensaje"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener datos de la empresa: " + e.getMessage());
        }
        return conf;
    }

    /**
     * Actualiza la información de un usuario existente.
     */
    public boolean actualizarUsuario(Login lg) {
        String passParaGuardar = lg.getPass();
        if (passParaGuardar != null && !passParaGuardar.startsWith("$2a$")) {
            passParaGuardar = BCrypt.hashpw(passParaGuardar, BCrypt.gensalt(12));
        }
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, pass = ?, rol = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return false;
            ps.setString(1, lg.getNombre());
            ps.setString(2, lg.getCorreo());
            ps.setString(3, passParaGuardar);
            ps.setString(4, lg.getRol());
            ps.setInt(5, lg.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un usuario por su ID.
     */
    public Login buscarUsuarioPorId(int id) {
        Login l = new Login();
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con != null ? con.prepareStatement(sql) : null) {
            if (ps == null) return null;
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    l.setId(rs.getInt("id"));
                    l.setNombre(rs.getString("nombre"));
                    l.setCorreo(rs.getString("correo"));
                    l.setPass(rs.getString("pass"));
                    l.setRol(rs.getString("rol"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }
        return l.getId() == 0 ? null : l;
    }
}
