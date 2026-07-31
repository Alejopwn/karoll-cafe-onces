package Modelo;

import Vista.ModalAlarma;
import java.awt.Frame;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Gestor en segundo plano para verificación periódica de alarmas y notificaciones.
 */
public class AlarmManager {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final RecordatoriosDao recDao = new RecordatoriosDao();
    private final Set<Integer> notificados = new HashSet<>();
    private Frame mainFrame;

    public void iniciar(Frame mainFrame) {
        this.mainFrame = mainFrame;
        scheduler.scheduleAtFixedRate(this::verificarAlarmas, 5, 30, TimeUnit.SECONDS);
    }

    private void verificarAlarmas() {
        try {
            String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String ahoraHora = new SimpleDateFormat("HH:mm").format(new Date());

            List<Recordatorio> lista = recDao.listar();
            for (Recordatorio r : lista) {
                if (!r.isCompletado() && !notificados.contains(r.getId())) {
                    if (r.getFecha() != null && r.getFecha().equals(hoy)) {
                        if (r.getHora() != null && r.getHora().compareTo(ahoraHora) <= 0) {
                            notificados.add(r.getId());
                            SwingUtilities.invokeLater(() -> {
                                ModalAlarma modal = new ModalAlarma(
                                    mainFrame,
                                    r.getTitulo(),
                                    r.getDescripcion(),
                                    () -> recDao.marcarCompletado(r.getId(), true),
                                    () -> notificados.remove(r.getId())
                                );
                                modal.setVisible(true);
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en verifiación de alarmas: " + e.getMessage());
        }
    }

    public void detener() {
        scheduler.shutdown();
    }
}
