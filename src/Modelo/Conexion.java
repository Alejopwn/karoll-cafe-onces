package Modelo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Proveedor de conexiones SQLite directo.
 *
 * No usamos pool de conexiones (HikariCP) porque SQLite es una base de datos
 * de archivo de usuario único. Cada llamada a getConnection() abre una conexión
 * nueva que el DAO debe cerrar en su bloque finally.
 * WAL mode + busy_timeout gestionan la concurrencia internamente.
 */
public class Conexion {

    private static final String DB_PATH;

    static {
        DB_PATH = new File("restaurante.db").getAbsolutePath();
        // Registrar el driver (necesario en algunos entornos)
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver SQLite no encontrado: " + e.getMessage());
        }
        // Verificar y migrar base de datos
        inicializarBaseDatos();
    }

    private static void inicializarBaseDatos() {
        String url = "jdbc:sqlite:" + DB_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Verificar columnas de pedidos
            if (!columnaExiste(conn, "pedidos", "pago_efectivo")) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE pedidos ADD COLUMN pago_efectivo REAL DEFAULT 0.0");
                    System.out.println("Columna 'pago_efectivo' agregada a la tabla 'pedidos'.");
                }
            }
            if (!columnaExiste(conn, "pedidos", "pago_transaccion")) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE pedidos ADD COLUMN pago_transaccion REAL DEFAULT 0.0");
                    System.out.println("Columna 'pago_transaccion' agregada a la tabla 'pedidos'.");
                }
            }
            // Verificar tablas de inventario
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS inventario ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "codigo TEXT UNIQUE, "
                        + "nombre TEXT NOT NULL, "
                        + "categoria TEXT DEFAULT 'General', "
                        + "stock REAL NOT NULL DEFAULT 0, "
                        + "stock_minimo REAL NOT NULL DEFAULT 5, "
                        + "unidad_medida TEXT DEFAULT 'Unidades', "
                        + "precio_compra REAL NOT NULL DEFAULT 0.0, "
                        + "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS movimientos_inventario ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "id_inventario INTEGER NOT NULL, "
                        + "nombre_producto TEXT NOT NULL, "
                        + "tipo_movimiento TEXT CHECK(tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE')) NOT NULL, "
                        + "cantidad REAL NOT NULL, "
                        + "motivo TEXT DEFAULT '', "
                        + "usuario TEXT DEFAULT 'Sistema', "
                        + "fecha TEXT DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY (id_inventario) REFERENCES inventario (id) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS cierre_caja ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "usuario TEXT NOT NULL, "
                        + "monto_inicial REAL NOT NULL DEFAULT 0.0, "
                        + "fecha_apertura TEXT DEFAULT CURRENT_TIMESTAMP, "
                        + "monto_ventas_efectivo REAL DEFAULT 0.0, "
                        + "monto_ventas_transaccion REAL DEFAULT 0.0, "
                        + "monto_gastos REAL DEFAULT 0.0, "
                        + "monto_esperado_efectivo REAL DEFAULT 0.0, "
                        + "monto_real_efectivo REAL DEFAULT 0.0, "
                        + "diferencia REAL DEFAULT 0.0, "
                        + "estado TEXT CHECK(estado IN ('ABIERTA', 'CERRADA')) NOT NULL DEFAULT 'ABIERTA', "
                        + "fecha_cierre TEXT DEFAULT NULL, "
                        + "observaciones TEXT DEFAULT '')");

                stmt.execute("CREATE TABLE IF NOT EXISTS gastos_caja ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "id_cierre INTEGER NOT NULL, "
                        + "monto REAL NOT NULL, "
                        + "descripcion TEXT NOT NULL, "
                        + "categoria TEXT DEFAULT 'Varios', "
                        + "usuario TEXT NOT NULL, "
                        + "fecha TEXT DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY (id_cierre) REFERENCES cierre_caja (id) ON DELETE CASCADE)");
            }

            try (Statement sAlt = conn.createStatement()) {
                sAlt.execute("ALTER TABLE pedidos ADD COLUMN id_cierre INTEGER DEFAULT NULL");
            } catch (SQLException ignored) {
                // Columna ya existe
            }

            poblarInventarioInicial(conn);
        } catch (SQLException e) {
            System.err.println("Error al inicializar o migrar la base de datos: " + e.getMessage());
        }
    }

    private static void poblarInventarioInicial(Connection conn) {
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventario")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[][] datosIniciales = {
                    {"INV-001", "Queso Mozzarella Tajado", "Lácteos / Quesos", "25.0", "5.0", "Kg", "24000.0"},
                    {"INV-002", "Harina de Trigo Especial", "Insumos Base", "50.0", "10.0", "Kg", "4500.0"},
                    {"INV-003", "Salsa de Tomate Napolitana", "Salsas / Sazones", "30.0", "8.0", "Kg", "12000.0"},
                    {"INV-004", "Jamón Pietrán Premium", "Carnes y Embutidos", "15.0", "3.0", "Kg", "32000.0"},
                    {"INV-005", "Pepperoni Tajado", "Carnes y Embutidos", "12.0", "3.0", "Kg", "38000.0"},
                    {"INV-006", "Champignons Frescos", "Vegetales / Verduras", "8.0", "2.0", "Kg", "18000.0"},
                    {"INV-007", "Coca-Cola 1.5L", "Bebidas", "48.0", "12.0", "Unidades", "5500.0"},
                    {"INV-008", "Cerveza Club Colombia 330ml", "Bebidas", "72.0", "24.0", "Unidades", "4200.0"},
                    {"INV-009", "Aceite de Oliva Extra Virgen", "Insumos Base", "10.0", "2.0", "Litros", "45000.0"},
                    {"INV-010", "Cajas para Pizza Grande 35cm", "Empaques", "200.0", "50.0", "Unidades", "1200.0"}
                };

                String insertSql = "INSERT INTO inventario (codigo, nombre, categoria, stock, stock_minimo, unidad_medida, precio_compra) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (String[] row : datosIniciales) {
                        pstmt.setString(1, row[0]);
                        pstmt.setString(2, row[1]);
                        pstmt.setString(3, row[2]);
                        pstmt.setDouble(4, Double.parseDouble(row[3]));
                        pstmt.setDouble(5, Double.parseDouble(row[4]));
                        pstmt.setString(6, row[5]);
                        pstmt.setDouble(7, Double.parseDouble(row[6]));
                        pstmt.executeUpdate();
                    }
                }

                if (tablaExiste(conn, "platos")) {
                    String importPlatos = "INSERT OR IGNORE INTO inventario (codigo, nombre, categoria, stock, stock_minimo, unidad_medida, precio_compra) "
                            + "SELECT 'PLT-' || id, nombre, 'Menú / Platos', 20.0, 5.0, 'Porciones', precio * 0.4 FROM platos";
                    try (java.sql.Statement stmtPlatos = conn.createStatement()) {
                        stmtPlatos.executeUpdate(importPlatos);
                    }
                }
                System.out.println("Inventario inicial cargado exitosamente.");
            }
        } catch (SQLException e) {
            System.err.println("Nota al poblar inventario inicial: " + e.getMessage());
        }
    }

    private static boolean tablaExiste(Connection conn, String tabla) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tabla);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean columnaExiste(Connection conn, String tabla, String columna) {
        String sql = "PRAGMA table_info(" + tabla + ")";
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar columna " + columna + " en " + tabla + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Crea y retorna una nueva conexión SQLite configurada.
     * El invocador es responsable de cerrarla mediante try-with-resources o un bloque finally.
     */
    public Connection getConnection() {
        try {
            String url = "jdbc:sqlite:" + DB_PATH
                    + "?journal_mode=WAL"
                    + "&busy_timeout=5000"
                    + "&foreign_keys=on"
                    + "&synchronous=NORMAL";
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println("Error al conectar con SQLite: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método de compatibilidad para cierre de conexiones.
     */
    public static void cerrarPool() {
        System.out.println("Aplicación cerrada correctamente.");
    }
}
