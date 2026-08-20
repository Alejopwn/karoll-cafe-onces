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
            boolean isDark = Vista.UIUtils.IS_DARK;
            if (isDark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            }
            // Personalización estética global AS Business Systems
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("CheckBox.arc", 8);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("OptionPane.arc", 20);
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
