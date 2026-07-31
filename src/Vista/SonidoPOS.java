package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Sintetizador y Administrador de Sonidos y Efectos POS.
 * Soporta efectos nativos de caja registradora, campanas, clics, beeps y voz sintetizada en tiempo real.
 */
public class SonidoPOS {

    private static boolean sonidoHabilitado = true;
    private static boolean sonidoClicsHabilitado = true;
    private static boolean vozHabilitada = true;
    private static String tipoSonidoCobro = "CHACHING"; // CHACHING, MONEDAS, BEEP, SILENCIO

    public static boolean isSonidoHabilitado() { return sonidoHabilitado; }
    public static void setSonidoHabilitado(boolean b) { sonidoHabilitado = b; }

    public static boolean isSonidoClicsHabilitado() { return sonidoClicsHabilitado; }
    public static void setSonidoClicsHabilitado(boolean b) { sonidoClicsHabilitado = b; }

    public static boolean isVozHabilitada() { return vozHabilitada; }
    public static void setVozHabilitada(boolean b) { vozHabilitada = b; }

    public static String getTipoSonidoCobro() { return tipoSonidoCobro; }
    public static void setTipoSonidoCobro(String tipo) { tipoSonidoCobro = tipo; }

    /**
     * Reproduce el efecto configurado al cobrar un pedido.
     */
    public static void reproducirCobro() {
        if (!sonidoHabilitado) return;
        switch (tipoSonidoCobro) {
            case "CHACHING":
                reproducirChaching();
                break;
            case "MONEDAS":
                reproducirMonedas();
                break;
            case "BEEP":
                reproducirBeep();
                break;
            default:
                break;
        }
    }

    /**
     * Reproduce un sonido sutil de clic de botón.
     */
    public static void reproducirClic() {
        if (!sonidoHabilitado || !sonidoClicsHabilitado) return;
        new Thread(() -> {
            try {
                float sampleRate = 22050f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 1000);
                line.start();

                int duracion = (int) (sampleRate * 0.03);
                byte[] buffer = new byte[duracion];
                for (int i = 0; i < duracion; i++) {
                    double t = i / sampleRate;
                    double env = Math.exp(-30.0 * t);
                    buffer[i] = (byte) (Math.sin(2 * Math.PI * 1200 * t) * env * 80);
                }
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Reproduce un sonido tipo Beep digital.
     */
    public static void reproducirBeep() {
        if (!sonidoHabilitado) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 4410);
                line.start();

                int duracion = (int) (sampleRate * 0.15);
                byte[] buffer = new byte[duracion];
                for (int i = 0; i < duracion; i++) {
                    double t = i / sampleRate;
                    double env = Math.exp(-6.0 * t);
                    buffer[i] = (byte) (Math.sin(2 * Math.PI * 987.77 * t) * env * 100);
                }
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Reproduce un efecto metálico tipo tintineo de monedas.
     */
    public static void reproducirMonedas() {
        if (!sonidoHabilitado) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 22050);
                line.start();

                int duracion = (int) (sampleRate * 0.35);
                byte[] buffer = new byte[duracion];
                for (int i = 0; i < duracion; i++) {
                    double t = i / sampleRate;
                    double env = Math.exp(-10.0 * t);
                    double val = (Math.sin(2 * Math.PI * 1760 * t) * 0.5 + Math.sin(2 * Math.PI * 2349 * t) * 0.5) * env;
                    buffer[i] = (byte) (val * 120);
                }
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Reproduce el efecto de sonido de caja registradora ("Cha-Ching!").
     */
    public static void reproducirChaching() {
        if (!sonidoHabilitado) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 44100);
                line.start();

                byte[] buffer = new byte[88200];
                int idx = 0;

                int duracionMetalo = (int) (sampleRate * 0.12);
                for (int i = 0; i < duracionMetalo; i++) {
                    double t = i / sampleRate;
                    double env = 1.0 - (double) i / duracionMetalo;
                    double val = (Math.sin(2 * Math.PI * 523 * t) * 0.3
                                + Math.sin(2 * Math.PI * 784 * t) * 0.3
                                + Math.sin(2 * Math.PI * 1174 * t) * 0.4) * env;
                    val += (Math.random() - 0.5) * 0.2 * env;
                    buffer[idx++] = (byte) (val * 127);
                }

                int duracionSilencio = (int) (sampleRate * 0.05);
                for (int i = 0; i < duracionSilencio; i++) { buffer[idx++] = 0; }

                int duracionChing = (int) (sampleRate * 0.6);
                for (int i = 0; i < duracionChing; i++) {
                    double t = i / sampleRate;
                    double env = Math.exp(-6.0 * t);
                    double val = (Math.sin(2 * Math.PI * 1046.5 * t) * 0.4
                                + Math.sin(2 * Math.PI * 1318.5 * t) * 0.35
                                + Math.sin(2 * Math.PI * 1567.98 * t) * 0.25) * env;
                    buffer[idx++] = (byte) (val * 127);
                }

                line.write(buffer, 0, idx);
                line.drain();
                line.close();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Reproduce un sonido suave de timbre.
     */
    public static void reproducirDing() {
        if (!sonidoHabilitado) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 22050);
                line.start();

                int duracion = (int) (sampleRate * 0.3);
                byte[] buffer = new byte[duracion];
                for (int i = 0; i < duracion; i++) {
                    double t = i / sampleRate;
                    double env = Math.exp(-8.0 * t);
                    double val = Math.sin(2 * Math.PI * 880 * t) * env;
                    buffer[i] = (byte) (val * 127);
                }

                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Alerta por Voz Sintetizada (habla por los altavoces de Windows).
     */
    public static void anunciarVoz(String texto) {
        if (!sonidoHabilitado || !vozHabilitada) return;
        new Thread(() -> {
            try {
                String script = "Add-Type -AssemblyName System.Speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; $synth.Speak('" + texto.replace("'", "") + "');";
                ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", script);
                pb.start();
            } catch (Exception e) {}
        }).start();
    }

    /**
     * Modal para configurar las preferencias de sonido del POS.
     */
    public static void abrirModalConfigSonido(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Configuración de Sonidos y Efectos POS", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("Panel de Sonidos y Audio POS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);

        JPanel pnlForm = new JPanel(new GridLayout(5, 1, 10, 15));
        pnlForm.setOpaque(false);

        JCheckBox chkGeneral = new JCheckBox("Habilitar todos los sonidos del sistema", sonidoHabilitado);
        chkGeneral.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkGeneral.setForeground(Color.WHITE);
        chkGeneral.setOpaque(false);

        JCheckBox chkClics = new JCheckBox("Sonido de clics al presionar botones", sonidoClicsHabilitado);
        chkClics.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkClics.setForeground(Color.WHITE);
        chkClics.setOpaque(false);

        JCheckBox chkVoz = new JCheckBox("Anuncios por Voz Sintetizada (ej: 'Mesa finalizada')", vozHabilitada);
        chkVoz.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkVoz.setForeground(Color.WHITE);
        chkVoz.setOpaque(false);

        JPanel pnlCombo = new JPanel(new BorderLayout(10, 0));
        pnlCombo.setOpaque(false);
        JLabel lblCombo = new JLabel("Efecto al Cobrar:");
        lblCombo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCombo.setForeground(Color.WHITE);

        JComboBox<String> cbxEfecto = new JComboBox<>(new String[]{"Caja Registradora (Cha-Ching)", "Tintineo de Monedas", "Beep Digital", "Silencio"});
        if (tipoSonidoCobro.equals("MONEDAS")) cbxEfecto.setSelectedIndex(1);
        else if (tipoSonidoCobro.equals("BEEP")) cbxEfecto.setSelectedIndex(2);
        else if (tipoSonidoCobro.equals("SILENCIO")) cbxEfecto.setSelectedIndex(3);
        else cbxEfecto.setSelectedIndex(0);

        pnlCombo.add(lblCombo, BorderLayout.WEST);
        pnlCombo.add(cbxEfecto, BorderLayout.CENTER);

        JPanel pnlProbador = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        pnlProbador.setOpaque(false);

        JButton btnProbar = new JButton("Probar Audio");
        btnProbar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProbar.setBackground(new Color(30, 64, 175));
        btnProbar.setForeground(Color.WHITE);
        btnProbar.setFocusPainted(false);
        btnProbar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProbar.addActionListener(e -> {
            int sel = cbxEfecto.getSelectedIndex();
            if (sel == 0) reproducirChaching();
            else if (sel == 1) reproducirMonedas();
            else if (sel == 2) reproducirBeep();

            if (chkVoz.isSelected()) {
                anunciarVoz("Mesa 2 finalizada.");
            }
        });

        pnlProbador.add(btnProbar);

        pnlForm.add(chkGeneral);
        pnlForm.add(chkClics);
        pnlForm.add(chkVoz);
        pnlForm.add(pnlCombo);
        pnlForm.add(pnlProbador);

        JPanel pnlBtns = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);

        JButton btnGuardar = new JButton("Guardar Preferencias");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(34, 197, 94));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> {
            sonidoHabilitado = chkGeneral.isSelected();
            sonidoClicsHabilitado = chkClics.isSelected();
            vozHabilitada = chkVoz.isSelected();

            int sel = cbxEfecto.getSelectedIndex();
            if (sel == 1) tipoSonidoCobro = "MONEDAS";
            else if (sel == 2) tipoSonidoCobro = "BEEP";
            else if (sel == 3) tipoSonidoCobro = "SILENCIO";
            else tipoSonidoCobro = "CHACHING";

            ToastNotification.exito(parent, "Preferencias de sonido guardadas.");
            dialog.dispose();
        });

        pnlBtns.add(btnGuardar);

        root.add(lblHeader, BorderLayout.NORTH);
        root.add(pnlForm, BorderLayout.CENTER);
        root.add(pnlBtns, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }
}
