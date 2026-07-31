package Vista;

import Modelo.Tarea;
import Modelo.TareasDao;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel de Gestión de Tareas de Empleados adaptado al sistema de diseño Slate.
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
        setBackground(new Color(15, 23, 42));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        cargarTabla();
    }

    public void abrirComoVentanaModal(Frame owner) {
        JDialog dialog = new JDialog(owner, "📋 Módulo de Tareas de Empleados POS", true);
        dialog.setContentPane(this);
        dialog.setSize(920, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void initUI() {
        // Header
        JLabel lblHeader = new JLabel("📋 Módulo de Tareas de Empleados");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(Color.WHITE);
        add(lblHeader, BorderLayout.NORTH);

        // Formulario Izquierda
        JPanel panelForm = new JPanel(new GridLayout(9, 1, 5, 6));
        panelForm.setOpaque(false);
        panelForm.setPreferredSize(new Dimension(290, 0));

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Color colText = new Color(226, 232, 240);

        JLabel l1 = new JLabel("Título de la Tarea:");
        l1.setFont(fontLabel); l1.setForeground(colText);
        txtTitulo = new JTextField();
        txtTitulo.setBackground(new Color(30, 41, 59));
        txtTitulo.setForeground(Color.WHITE);
        txtTitulo.setCaretColor(Color.WHITE);

        JLabel l2 = new JLabel("Descripción:");
        l2.setFont(fontLabel); l2.setForeground(colText);
        txtDescripcion = new JTextField();
        txtDescripcion.setBackground(new Color(30, 41, 59));
        txtDescripcion.setForeground(Color.WHITE);
        txtDescripcion.setCaretColor(Color.WHITE);

        JLabel l3 = new JLabel("Asignado a:");
        l3.setFont(fontLabel); l3.setForeground(colText);
        txtAsignadoA = new JTextField("Todos");
        txtAsignadoA.setBackground(new Color(30, 41, 59));
        txtAsignadoA.setForeground(Color.WHITE);
        txtAsignadoA.setCaretColor(Color.WHITE);

        JLabel l4 = new JLabel("Repetición:");
        l4.setFont(fontLabel); l4.setForeground(colText);
        comboRepeticion = new JComboBox<>(new String[]{"NINGUNA", "DIARIA", "SEMANAL", "MENSUAL"});
        comboRepeticion.setBackground(new Color(30, 41, 59));
        comboRepeticion.setForeground(Color.WHITE);

        JButton btnGuardar = new JButton("➕ Agregar Tarea");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        tabla.setRowHeight(44);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setBackground(new Color(30, 41, 59));
        tabla.setForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(new Color(15, 23, 42));
        tabla.getTableHeader().setForeground(new Color(226, 232, 240));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(30, 41, 59) : new Color(15, 23, 42));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(59, 130, 246));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        TouchScrollHelper.aplicar(new JScrollPane(tabla));
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Footer Botones
        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pBot.setOpaque(false);

        JButton btnCompletar = new JButton("☑️ Marcar Realizada");
        btnCompletar.setBackground(new Color(59, 130, 246));
        btnCompletar.setForeground(Color.WHITE);
        btnCompletar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCompletar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row != -1) {
                int id = Integer.parseInt(tabla.getValueAt(row, 0).toString());
                tareasDao.marcarCompletada(id, true);
                cargarTabla();
            }
        });

        JButton btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setBackground(new Color(220, 38, 38));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
                t.isCompletada() ? "REALIZADA ✅" : "PENDIENTE ⏳"
            });
        }
    }
}
