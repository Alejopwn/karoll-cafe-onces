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
            // Personalización estética global de FlatLaf y Diálogos
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("CheckBox.arc", 8);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("OptionPane.arc", 20);
            UIManager.put("OptionPane.background", new java.awt.Color(15, 23, 42));
            UIManager.put("Panel.background", new java.awt.Color(15, 23, 42));
            UIManager.put("OptionPane.messageForeground", new java.awt.Color(241, 245, 249));
            UIManager.put("OptionPane.yesButtonText", "Sí");
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.cancelButtonText", "Cancelar");
            UIManager.put("OptionPane.okButtonText", "Aceptar");
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
