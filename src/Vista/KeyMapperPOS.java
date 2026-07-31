package Vista;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/**
 * Gestor global de atajos de teclado POS (F1 - F12, ESC) para operación estilo Supermercado / Retail.
 */
public class KeyMapperPOS {

    public interface POSKeyAction {
        void onF1();
        void onF2();
        void onF3();
        void onF4();
        void onF5();
        void onF9();
        void onF11();
        void onF12();
        void onEscape();
    }

    private static KeyEventDispatcher activeDispatcher;

    public static void registrarAtajosGlobales(JFrame frame, POSKeyAction actions) {
        if (activeDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(activeDispatcher);
        }

        activeDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    int code = e.getKeyCode();
                    switch (code) {
                        case KeyEvent.VK_F1:
                            actions.onF1();
                            return true;
                        case KeyEvent.VK_F2:
                            actions.onF2();
                            return true;
                        case KeyEvent.VK_F3:
                            actions.onF3();
                            return true;
                        case KeyEvent.VK_F4:
                            actions.onF4();
                            return true;
                        case KeyEvent.VK_F5:
                            actions.onF5();
                            return true;
                        case KeyEvent.VK_F9:
                            actions.onF9();
                            return true;
                        case KeyEvent.VK_F11:
                            actions.onF11();
                            return true;
                        case KeyEvent.VK_F12:
                            actions.onF12();
                            return true;
                        case KeyEvent.VK_ESCAPE:
                            actions.onEscape();
                            return false;
                    }
                }
                return false;
            }
        };

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(activeDispatcher);
    }
}
