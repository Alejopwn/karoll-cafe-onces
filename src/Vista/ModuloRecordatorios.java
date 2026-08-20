package Vista;

import Modelo.Recordatorio;
import Modelo.RecordatoriosDao;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Módulo de Gestión de Recordatorios adaptado al sistema de diseño dinámico (Oscuro y Claro).
 */
public class ModuloRecordatorios extends JPanel {

    private final RecordatoriosDao recDao = new RecordatoriosDao();
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtTitulo;
    private JTextField txtDescripcion;
    private JTextField txtFecha;
    private JTextField txtHora;
    private JComboBox<String> comboPrioridad;

    public ModuloRecordatorios() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.getBgColor());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        cargarTabla();
    }

    public void abrirComoVentanaModal(Frame owner) {
        removeAll();
        initUI();
        cargarTabla();
        JDialog dialog = new JDialog(owner, "Módulo de Recordatorios POS", true);
        dialog.setContentPane(this);
        dialog.setSize(920, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void initUI() {
        setBackground(UIUtils.getBgColor());

        // Header
        JLabel lblHeader = new JLabel("Módulo de Recordatorios POS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(UIUtils.getTextPrimary());
        add(lblHeader, BorderLayout.NORTH);

        // Panel Izquierdo: Formulario de Creación
        JPanel panelForm = new JPanel(new GridLayout(11, 1, 5, 6));
        panelForm.setOpaque(false);
        panelForm.setPreferredSize(new Dimension(290, 0));

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colText = UIUtils.getTextPrimary();

        JLabel l1 = new JLabel("Título del Recordatorio:");
        l1.setFont(fontLabel); l1.setForeground(colText);
        txtTitulo = new JTextField();
        UIUtils.estilarCampoTexto(txtTitulo);

        JLabel l2 = new JLabel("Descripción / Notas:");
        l2.setFont(fontLabel); l2.setForeground(colText);
        txtDescripcion = new JTextField();
        UIUtils.estilarCampoTexto(txtDescripcion);

        JLabel l3 = new JLabel("Fecha (YYYY-MM-DD):");
        l3.setFont(fontLabel); l3.setForeground(colText);
        txtFecha = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        UIUtils.estilarCampoTexto(txtFecha);

        JLabel l4 = new JLabel("Hora (HH:mm):");
        l4.setFont(fontLabel); l4.setForeground(colText);
        txtHora = new JTextField(new SimpleDateFormat("HH:mm").format(new Date()));
        UIUtils.estilarCampoTexto(txtHora);

        JLabel l5 = new JLabel("Prioridad:");
        l5.setFont(fontLabel); l5.setForeground(colText);
        comboPrioridad = new JComboBox<>(new String[]{"ALTA", "MEDIA", "BAJA"});
        UIUtils.estilarCombo(comboPrioridad);

        JButton btnGuardar = new JButton("Guardar Recordatorio");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(16, 185, 129));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarRecordatorio());

        panelForm.add(l1);
        panelForm.add(txtTitulo);
        panelForm.add(l2);
        panelForm.add(txtDescripcion);
        panelForm.add(l3);
        panelForm.add(txtFecha);
        panelForm.add(l4);
        panelForm.add(txtHora);
        panelForm.add(l5);
        panelForm.add(comboPrioridad);
        panelForm.add(btnGuardar);

        add(panelForm, BorderLayout.WEST);

        // Panel Central: Tabla de Recordatorios
        String[] cols = {"ID", "Título", "Descripción", "Fecha", "Hora", "Prioridad", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modelo);
        UIUtils.estilarTabla(tabla);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String prio = String.valueOf(t.getValueAt(row, 5));
                if (!isSelected) {
                    if ("ALTA".equalsIgnoreCase(prio)) {
                        c.setBackground(UIUtils.IS_DARK ? new Color(127, 29, 29) : new Color(254, 226, 226));
                        c.setForeground(UIUtils.IS_DARK ? new Color(254, 202, 202) : new Color(153, 27, 27));
                    } else if ("MEDIA".equalsIgnoreCase(prio)) {
                        c.setBackground(UIUtils.IS_DARK ? new Color(120, 53, 15) : new Color(254, 243, 199));
                        c.setForeground(UIUtils.IS_DARK ? new Color(253, 230, 138) : new Color(146, 64, 14));
                    } else {
                        c.setBackground(row % 2 == 0 ? UIUtils.getBgColor() : UIUtils.getPanelColor());
                        c.setForeground(UIUtils.getTextPrimary());
                    }
                } else {
                    c.setBackground(UIUtils.IS_DARK ? new Color(59, 130, 246) : new Color(37, 99, 235));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(UIUtils.getBgColor());
        scroll.setBackground(UIUtils.getBgColor());
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.getBorderColor(), 1));
        TouchScrollHelper.aplicar(scroll);
        add(scroll, BorderLayout.CENTER);

        // Panel Inferior: Botones de Acción
        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pBot.setOpaque(false);

        JButton btnCompletar = new JButton("Marcar Completado");
        btnCompletar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompletar.setBackground(new Color(59, 130, 246));
        btnCompletar.setForeground(Color.WHITE);
        btnCompletar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompletar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                int id = Integer.parseInt(tabla.getValueAt(row, 0).toString());
                recDao.marcarCompletado(id, true);
                cargarTabla();
            }
        });

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEliminar.setBackground(new Color(220, 38, 38));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                int id = Integer.parseInt(tabla.getValueAt(row, 0).toString());
                recDao.eliminar(id);
                cargarTabla();
            }
        });

        pBot.add(btnCompletar);
        pBot.add(btnEliminar);
        add(pBot, BorderLayout.SOUTH);
    }

    private void guardarRecordatorio() {
        String tit = txtTitulo.getText().trim();
        if (tit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El título es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Recordatorio r = new Recordatorio();
        r.setTitulo(tit);
        r.setDescripcion(txtDescripcion.getText().trim());
        r.setFecha(txtFecha.getText().trim());
        r.setHora(txtHora.getText().trim());
        r.setPrioridad(comboPrioridad.getSelectedItem().toString());
        r.setCompletado(false);

        if (recDao.guardar(r)) {
            txtTitulo.setText("");
            txtDescripcion.setText("");
            cargarTabla();
        }
    }

    public void cargarTabla() {
        modelo.setRowCount(0);
        List<Recordatorio> lista = recDao.listar();
        for (Recordatorio r : lista) {
            modelo.addRow(new Object[]{
                r.getId(),
                r.getTitulo(),
                r.getDescripcion(),
                r.getFecha(),
                r.getHora(),
                r.getPrioridad(),
                r.isCompletado() ? "● COMPLETADO" : "● PENDIENTE"
            });
        }
    }
}
