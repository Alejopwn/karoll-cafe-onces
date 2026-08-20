package Vista;

import Modelo.Tarea;
import Modelo.TareasDao;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de Gestión de Tareas de Empleados adaptado al sistema de diseño dinámico (Oscuro y Claro).
 */
public class ModuloTareas extends JPanel {

    private final TareasDao tareasDao = new TareasDao();
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtTitulo;
    private JTextField txtDescripcion;
    private JTextField txtAsignadoA;
    private JComboBox<String> comboRepeticion;

    public ModuloTareas() {
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
        JDialog dialog = new JDialog(owner, "Módulo de Tareas de Empleados POS", true);
        dialog.setContentPane(this);
        dialog.setSize(920, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void initUI() {
        setBackground(UIUtils.getBgColor());

        // Header
        JLabel lblHeader = new JLabel("Módulo de Tareas de Empleados");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(UIUtils.getTextPrimary());
        add(lblHeader, BorderLayout.NORTH);

        // Formulario Izquierda
        JPanel panelForm = new JPanel(new GridLayout(9, 1, 5, 6));
        panelForm.setOpaque(false);
        panelForm.setPreferredSize(new Dimension(290, 0));

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colText = UIUtils.getTextPrimary();

        JLabel l1 = new JLabel("Título de la Tarea:");
        l1.setFont(fontLabel); l1.setForeground(colText);
        txtTitulo = new JTextField();
        UIUtils.estilarCampoTexto(txtTitulo);

        JLabel l2 = new JLabel("Descripción:");
        l2.setFont(fontLabel); l2.setForeground(colText);
        txtDescripcion = new JTextField();
        UIUtils.estilarCampoTexto(txtDescripcion);

        JLabel l3 = new JLabel("Asignado a:");
        l3.setFont(fontLabel); l3.setForeground(colText);
        txtAsignadoA = new JTextField("Todos");
        UIUtils.estilarCampoTexto(txtAsignadoA);

        JLabel l4 = new JLabel("Repetición:");
        l4.setFont(fontLabel); l4.setForeground(colText);
        comboRepeticion = new JComboBox<>(new String[]{"NINGUNA", "DIARIA", "SEMANAL", "MENSUAL"});
        UIUtils.estilarCombo(comboRepeticion);

        JButton btnGuardar = new JButton("Agregar Tarea");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(16, 185, 129));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarTarea());

        panelForm.add(l1);
        panelForm.add(txtTitulo);
        panelForm.add(l2);
        panelForm.add(txtDescripcion);
        panelForm.add(l3);
        panelForm.add(txtAsignadoA);
        panelForm.add(l4);
        panelForm.add(comboRepeticion);
        panelForm.add(btnGuardar);

        add(panelForm, BorderLayout.WEST);

        // Tabla Central
        String[] cols = {"ID", "Título", "Descripción", "Asignado A", "Repetición", "Estado"};
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
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UIUtils.getBgColor() : UIUtils.getPanelColor());
                    c.setForeground(UIUtils.getTextPrimary());
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

        // Footer Botones
        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pBot.setOpaque(false);

        JButton btnCompletar = new JButton("Marcar Realizada");
        btnCompletar.setBackground(new Color(59, 130, 246));
        btnCompletar.setForeground(Color.WHITE);
        btnCompletar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompletar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompletar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                int id = Integer.parseInt(tabla.getValueAt(row, 0).toString());
                tareasDao.marcarCompletada(id, true);
                cargarTabla();
            }
        });

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBackground(new Color(220, 38, 38));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                int id = Integer.parseInt(tabla.getValueAt(row, 0).toString());
                tareasDao.eliminar(id);
                cargarTabla();
            }
        });

        pBot.add(btnCompletar);
        pBot.add(btnEliminar);
        add(pBot, BorderLayout.SOUTH);
    }

    private void guardarTarea() {
        String tit = txtTitulo.getText().trim();
        if (tit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El título es requerido.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Tarea t = new Tarea();
        t.setTitulo(tit);
        t.setDescripcion(txtDescripcion.getText().trim());
        t.setAsignadoA(txtAsignadoA.getText().trim());
        t.setRepeticion(comboRepeticion.getSelectedItem().toString());
        t.setCompletada(false);

        if (tareasDao.guardar(t)) {
            txtTitulo.setText("");
            txtDescripcion.setText("");
            cargarTabla();
        }
    }

    public void cargarTabla() {
        modelo.setRowCount(0);
        List<Tarea> lista = tareasDao.listar();
        for (Tarea t : lista) {
            modelo.addRow(new Object[]{
                t.getId(),
                t.getTitulo(),
                t.getDescripcion(),
                t.getAsignadoA(),
                t.getRepeticion(),
                t.isCompletada() ? "● REALIZADA" : "● PENDIENTE"
            });
        }
    }
}
