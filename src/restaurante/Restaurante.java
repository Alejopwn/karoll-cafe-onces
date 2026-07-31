package restaurante;

import Modelo.BackupService;
import Vista.FrmLogin;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;

public class Restaurante {

    public static void main(String[] args) {

        // Iniciar el servicio de backups automáticos al arrancar
        BackupService.iniciar();

        // Cerrar el pool de conexiones y el servicio de backup al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BackupService.detener();
            Modelo.Conexion.cerrarPool();
            System.out.println("Aplicación cerrada correctamente.");
        }));

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Mostrar Splash Screen y luego el Login
        java.awt.EventQueue.invokeLater(() -> {
            Vista.Splash splash = new Vista.Splash();
            splash.startSplash(() -> {
                FrmLogin iniciar = new FrmLogin();
                iniciar.setVisible(true);
            });
        });
    }

}
