package com.minimarket.gui.panels;

import com.minimarket.patterns.creational.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Panel de Historial de Ventas - Muestra todas las ventas realizadas
 * Replica la funcionalidad mostrada en la imagen
 */
public class HistorialVentasPanel extends JPanel {
    
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar;
    private JLabel labelInfo;
    
    public HistorialVentasPanel() {
        initializeComponents();
        cargarHistorialVentas();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con botón actualizar
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Botón actualizar
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBoton.setBackground(Color.WHITE);
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setBackground(new Color(117, 117, 117));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 11));
        btnActualizar.setPreferredSize(new Dimension(100, 35));
        btnActualizar.addActionListener(e -> cargarHistorialVentas());
        
        panelBoton.add(btnActualizar);
        panelSuperior.add(panelBoton, BorderLayout.WEST);
        
        // Tabla de historial de ventas
        String[] columnas = {"ID", "Producto ID", "Usuario ID", "Cantidad", "Precio Unit.", "Total", "Fecha Venta"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHistorial.setRowHeight(25);
        tablaHistorial.getTableHeader().setBackground(new Color(63, 81, 181));
        tablaHistorial.getTableHeader().setForeground(Color.WHITE);
        tablaHistorial.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar ancho de columnas
        tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaHistorial.getColumnModel().getColumn(1).setPreferredWidth(80);  // Producto ID
        tablaHistorial.getColumnModel().getColumn(2).setPreferredWidth(80);  // Usuario ID
        tablaHistorial.getColumnModel().getColumn(3).setPreferredWidth(80);  // Cantidad
        tablaHistorial.getColumnModel().getColumn(4).setPreferredWidth(100); // Precio Unit.
        tablaHistorial.getColumnModel().getColumn(5).setPreferredWidth(100); // Total
        tablaHistorial.getColumnModel().getColumn(6).setPreferredWidth(150); // Fecha
        
        // Configurar colores alternados en las filas
        tablaHistorial.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 249, 250));
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Historial de Ventas"));
        
        // Panel inferior con información
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        labelInfo = new JLabel("<html><b>Nota:</b> Este historial muestra todas las ventas realizadas en el negocio.<br>" +
                              "Las ventas se ordenan por fecha (más recientes primero).</html>");
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        labelInfo.setForeground(new Color(100, 100, 100));
        
        panelInferior.add(labelInfo);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void cargarHistorialVentas() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        String sql = """
            SELECT dv.id, dv.producto_id, v.cajera_id as usuario_id, 
                   dv.cantidad, dv.precio_unitario, dv.subtotal as total,
                   v.fecha_hora
            FROM detalle_ventas dv
            INNER JOIN ventas v ON dv.venta_id = v.id
            WHERE v.estado = 'ACTIVA'
            ORDER BY v.fecha_hora DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                Object[] fila = {
                    rs.getLong("id"),
                    rs.getLong("producto_id"),
                    rs.getLong("usuario_id"),
                    rs.getInt("cantidad"),
                    "S/" + String.format("%.2f", rs.getDouble("precio_unitario")),
                    "S/" + String.format("%.2f", rs.getDouble("total")),
                    sdf.format(rs.getTimestamp("fecha_hora"))
                };
                modeloTabla.addRow(fila);
            }
            
            // Actualizar información
            int totalRegistros = modeloTabla.getRowCount();
            labelInfo.setText("<html><b>Nota:</b> Este historial muestra todas las ventas realizadas en el negocio (" + 
                             totalRegistros + " registros).<br>" +
                             "Las ventas se ordenan por fecha (más recientes primero).</html>");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar historial de ventas: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
