package Vista;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * Helper para permitir scroll por arrastre táctil en cualquier JScrollPane de Swing.
 */
public class TouchScrollHelper {

    public static void aplicar(JScrollPane scrollPane) {
        if (scrollPane == null) return;
        JViewport viewport = scrollPane.getViewport();
        if (viewport == null) return;

        MouseAdapter adapter = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin == null) return;
                Point viewPosition = viewport.getViewPosition();
                int deltaX = origin.x - e.getX();
                int deltaY = origin.y - e.getY();

                int newX = Math.max(0, Math.min(viewPosition.x + deltaX, viewport.getView().getWidth() - viewport.getWidth()));
                int newY = Math.max(0, Math.min(viewPosition.y + deltaY, viewport.getView().getHeight() - viewport.getHeight()));

                viewport.setViewPosition(new Point(newX, newY));
            }
        };

        viewport.addMouseListener(adapter);
        viewport.addMouseMotionListener(adapter);
    }
}
