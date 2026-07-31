package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Módulo de Notas Adhesivas y Recordatorios Internos (Sticky Notes POS).
 * Permite dejar avisos entre turnos y recordatorios operativos persistentes en disco.
 */
public class ModuloNotas {

    private static final String ARCHIVO_NOTAS = "notas_pos.txt";

    public static class Nota {
        private String id;
        private String texto;
        private String fecha;
        private boolean completada;

        public Nota(String id, String texto, String fecha, boolean completada) {
            this.id = id;
            this.texto = texto;
            this.fecha = fecha;
            this.completada = completada;
        }

        public String getId() { return id; }
        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
        public String getFecha() { return fecha; }
        public boolean isCompletada() { return completada; }
        public void setCompletada(boolean completada) { this.completada = completada; }
    }

    private final JFrame parentFrame;
    private final List<Nota> listaNotas = new ArrayList<>();
    private JPanel panelNotasContainer;

    public ModuloNotas(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        cargarNotas();
    }

    public void abrirModalNotas() {
        JDialog dialog = new JDialog(parentFrame, "Notas Adhesivas & Recordatorios del Turno", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(new Color(15, 23, 42)); // Slate 900
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel lblTitulo = new JLabel("Notas Adhesivas & Recordatorios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Deja avisos para el siguiente turno o notas de preparación");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(lblTitulo);
        headerText.add(lblSub);

        header.add(headerText, BorderLayout.WEST);

        // Input para nueva nota
        JPanel pnlNuevaNota = new JPanel(new BorderLayout(8, 8));
        pnlNuevaNota.setOpaque(false);
        pnlNuevaNota.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextField txtNuevaNota = new JTextField();
        txtNuevaNota.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNuevaNota.setBackground(new Color(30, 41, 59));
        txtNuevaNota.setForeground(Color.WHITE);
        txtNuevaNota.setCaretColor(Color.WHITE);
        txtNuevaNota.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtNuevaNota.setToolTipText("Escribe un recordatorio y presiona ENTER o clic en Agregar...");

        JButton btnAgregar = new JButton("📌 Agregar Nota");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAgregar.setBackground(new Color(234, 179, 8)); // Amber/Yellow
        btnAgregar.setForeground(new Color(15, 23, 42));
        btnAgregar.setFocusPainted(false);
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable agregarAccion = () -> {
            String texto = txtNuevaNota.getText().trim();
            if (texto.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Escribe el texto de la nota primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = "N_" + System.currentTimeMillis();
            String fecha = new SimpleDateFormat("dd/MM HH:mm").format(new Date());
            Nota nueva = new Nota(id, texto, fecha, false);
            listaNotas.add(0, nueva);
            guardarNotas();
            txtNuevaNota.setText("");
            renderizarNotas();
        };

        btnAgregar.addActionListener(e -> agregarAccion.run());
        txtNuevaNota.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    agregarAccion.run();
                }
            }
        });

        pnlNuevaNota.add(txtNuevaNota, BorderLayout.CENTER);
        pnlNuevaNota.add(btnAgregar, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));
        pnlTop.setOpaque(false);
        pnlTop.add(header, BorderLayout.NORTH);
        pnlTop.add(pnlNuevaNota, BorderLayout.SOUTH);

        root.add(pnlTop, BorderLayout.NORTH);

        // Grid / Lista de Notas en ScrollPane
        panelNotasContainer = new JPanel();
        panelNotasContainer.setLayout(new BoxLayout(panelNotasContainer, BoxLayout.Y_AXIS));
        panelNotasContainer.setBackground(new Color(15, 23, 42));

        JScrollPane scroll = new JScrollPane(panelNotasContainer);
        scroll.getViewport().setBackground(new Color(15, 23, 42));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(scroll, BorderLayout.CENTER);

        renderizarNotas();

        dialog.add(root);
        dialog.setVisible(true);
    }

    private void renderizarNotas() {
        if (panelNotasContainer == null) return;
        panelNotasContainer.removeAll();

        if (listaNotas.isEmpty()) {
            JLabel lblVacio = new JLabel("<html><center>📌 No hay notas registradas.<br><font size='3' color='#94A3B8'>Escribe recordatorios como 'Sacar la masa a las 7:00 PM' o 'Proveedor llega mañana 9:00 AM'</font></center></html>");
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblVacio.setForeground(new Color(148, 163, 184));
            lblVacio.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            lblVacio.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
            panelNotasContainer.add(lblVacio);
        } else {
            for (Nota n : listaNotas) {
                panelNotasContainer.add(crearCardNota(n));
                panelNotasContainer.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        panelNotasContainer.revalidate();
        panelNotasContainer.repaint();
    }

    private JPanel crearCardNota(Nota nota) {
        JPanel card = new JPanel(new BorderLayout(8, 5));
        
        // Estilo Post-it amarillo / amber
        Color bgNormal = new Color(254, 240, 138); // Yellow 200
        Color bgCompletada = new Color(241, 245, 249); // Slate 100
        Color fgNormal = new Color(113, 63, 18); // Yellow 900
        Color fgCompletada = new Color(148, 163, 184);

        card.setBackground(nota.isCompletada() ? bgCompletada : bgNormal);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(nota.isCompletada() ? new Color(203, 213, 225) : new Color(234, 179, 8), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(580, 80));

        JCheckBox chk = new JCheckBox();
        chk.setSelected(nota.isCompletada());
        chk.setOpaque(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chk.addActionListener(e -> {
            nota.setCompletada(chk.isSelected());
            guardarNotas();
            renderizarNotas();
        });

        JTextArea txt = new JTextArea(nota.getTexto());
        txt.setFont(new Font("Segoe UI", nota.isCompletada() ? Font.ITALIC : Font.BOLD, 13));
        txt.setForeground(nota.isCompletada() ? fgCompletada : fgNormal);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);

        JLabel lblFecha = new JLabel("🕒 " + nota.getFecha());
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFecha.setForeground(nota.isCompletada() ? fgCompletada : new Color(146, 64, 14));

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(chk, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(0, 2));
        center.setOpaque(false);
        center.add(txt, BorderLayout.CENTER);
        center.add(lblFecha, BorderLayout.SOUTH);

        JButton btnEliminar = new JButton("❌");
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnEliminar.setBorderPainted(false);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> {
            listaNotas.remove(nota);
            guardarNotas();
            renderizarNotas();
        });

        card.add(left, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(btnEliminar, BorderLayout.EAST);

        return card;
    }

    private synchronized void cargarNotas() {
        listaNotas.clear();
        File file = new File(ARCHIVO_NOTAS);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";;;");
                if (parts.length >= 4) {
                    listaNotas.add(new Nota(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar notas: " + e.getMessage());
        }
    }

    private synchronized void guardarNotas() {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ARCHIVO_NOTAS), "UTF-8"))) {
            for (Nota n : listaNotas) {
                pw.println(n.getId() + ";;;" + n.getTexto().replace("\n", " ") + ";;;" + n.getFecha() + ";;;" + n.isCompletada());
            }
        } catch (Exception e) {
            System.err.println("Error al guardar notas: " + e.getMessage());
        }
    }

    public int getCantidadNotasPendientes() {
        int c = 0;
        for (Nota n : listaNotas) {
            if (!n.isCompletada()) c++;
        }
        return c;
    }
}
