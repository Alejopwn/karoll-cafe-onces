package Modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de Backups Automáticos para la base de datos SQLite.
 * Guarda copias de seguridad diarias en la carpeta .backups/ dentro del directorio de la app.
 * Al iniciar, programa un backup automático cada 24 horas.
 */
public class BackupService {

    private static final String DB_ORIGEN = "restaurante.db";
    private static final String CARPETA_BACKUP = ".backups";
    private static final int MAX_BACKUPS = 7; // Conservar los últimos 7 días

    private static ScheduledExecutorService scheduler;

    /**
     * Inicia el servicio de backups automáticos.
     * Realiza un backup inmediato al iniciar y luego uno cada 24 horas.
     */
    public static void iniciar() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BackupService-Thread");
            t.setDaemon(true); // Termina cuando la app se cierra
            return t;
        });

        // Primer backup al iniciar la app (después de 30 segundos para dejar que cargue)
        scheduler.scheduleAtFixedRate(() -> {
            realizarBackup();
            limpiarBackupsAntiguos();
        }, 30, 24 * 60 * 60, TimeUnit.SECONDS); // Cada 24 horas

        System.out.println("Servicio de backups automáticos iniciado (cada 24h).");
    }

    /**
     * Copia el archivo restaurante.db con nombre y fecha de hoy.
     */
    public static boolean realizarBackup() {
        try {
            File origen = new File(DB_ORIGEN);
            if (!origen.exists()) {
                System.out.println("Backup: No se encontró el archivo de base de datos.");
                return false;
            }

            File carpetaBackup = new File(CARPETA_BACKUP);
            if (!carpetaBackup.exists()) {
                carpetaBackup.mkdirs();
            }

            String fechaStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
            File destino = new File(carpetaBackup, "backup_" + fechaStr + ".db");

            try (FileChannel src = new FileInputStream(origen).getChannel();
                 FileChannel dst = new FileOutputStream(destino).getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }

            System.out.println("Backup realizado: " + destino.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Error al realizar el backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina los backups más antiguos, conservando solo los últimos MAX_BACKUPS.
     */
    private static void limpiarBackupsAntiguos() {
        File carpetaBackup = new File(CARPETA_BACKUP);
        if (!carpetaBackup.exists()) return;

        File[] backups = carpetaBackup.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
        if (backups == null || backups.length <= MAX_BACKUPS) return;

        java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));

        int eliminar = backups.length - MAX_BACKUPS;
        for (int i = 0; i < eliminar; i++) {
            if (backups[i].delete()) {
                System.out.println("Backup antiguo eliminado: " + backups[i].getName());
            }
        }
    }

    /**
     * Detiene el servicio de backups.
     */
    public static void detener() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
