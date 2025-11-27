package com.minimarket.gui.panels;

import com.minimarket.patterns.creational.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel de Reporte de Ventas - Reporte diario por trabajador
 * Replica exactamente la funcionalidad mostrada en la imagen
 */
public class ReporteVentasPanel extends JPanel {
    
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizarReporte;
    private JLabel labelTitulo;
    private JLabel labelUltimaActualizacion;
    private JLabel labelTotalTransacciones;
    private JLabel labelTotalProductos;
    private JLabel labelIngresosTotales;
    
    public ReporteVentasPanel() {
        initializeComponents();
        cargarReporteVentas();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con título y botón
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Título del reporte
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(Color.WHITE);
        
        labelTitulo = new JLabel("REPORTE DE VENTAS DEL DÍA", JLabel.CENTER);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        labelTitulo.setForeground(new Color(33, 33, 33));
        
        labelUltimaActualizacion = new JLabel("", JLabel.CENTER);
        labelUltimaActualizacion.setFont(new Font("Arial", Font.PLAIN, 12));
        labelUltimaActualizacion.setForeground(new Color(100, 100, 100));
        
        panelTitulo.add(labelTitulo, BorderLayout.NORTH);
        panelTitulo.add(labelUltimaActualizacion, BorderLayout.SOUTH);
        
        // Botón actualizar
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setBackground(Color.WHITE);
        
        btnActualizarReporte = new JButton("Actualizar Reporte");
        btnActualizarReporte.setBackground(new Color(25, 118, 210));
        btnActualizarReporte.setForeground(Color.WHITE);
        btnActualizarReporte.setFocusPainted(false);
        btnActualizarReporte.setBorderPainted(false);
        btnActualizarReporte.setFont(new Font("Arial", Font.BOLD, 11));
        btnActualizarReporte.setPreferredSize(new Dimension(140, 35));
        btnActualizarReporte.addActionListener(e -> cargarReporteVentas());
        
        panelBoton.add(btnActualizarReporte);
        
        panelSuperior.add(panelTitulo, BorderLayout.CENTER);
        panelSuperior.add(panelBoton, BorderLayout.EAST);
        
        // Panel central con tabla de resumen por trabajador
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        // Título de la sección
        JLabel labelSeccion = new JLabel("Resumen por Trabajador");
        labelSeccion.setFont(new Font("Arial", Font.BOLD, 14));
        labelSeccion.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Tabla de reporte por trabajador
        String[] columnas = {"Trabajador", "Transacciones", "Productos Vendidos", "Total Recaudado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaReporte.setRowHeight(30);
        tablaReporte.getTableHeader().setBackground(new Color(63, 81, 181));
        tablaReporte.getTableHeader().setForeground(Color.WHITE);
        tablaReporte.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar ancho de columnas
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(200); // Trabajador
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(120); // Transacciones
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(150); // Productos Vendidos
        tablaReporte.getColumnModel().getColumn(3).setPreferredWidth(150); // Total Recaudado
        
        // Configurar colores alternados en las filas
        tablaReporte.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                
                // Alinear números a la derecha
                if (column > 0) {
                    setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        panelCentral.add(labelSeccion, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con totales del día
        JPanel panelTotales = crearPanelTotales();
        
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelTotales, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Título de la sección
        JLabel labelTituloTotales = new JLabel("TOTALES DEL DÍA");
        labelTituloTotales.setFont(new Font("Arial", Font.BOLD, 14));
        labelTituloTotales.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel con los totales
        JPanel panelMetricas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelMetricas.setBackground(Color.WHITE);
        panelMetricas.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        panelMetricas.setPreferredSize(new Dimension(0, 80));
        
        // Total Transacciones
        JPanel panelTransacciones = crearPanelMetrica("Total Transacciones:", "0", new Color(25, 118, 210));
        labelTotalTransacciones = (JLabel) ((JPanel) panelTransacciones.getComponent(1)).getComponent(0);
        
        // Productos Vendidos
        JPanel panelProductos = crearPanelMetrica("Productos Vendidos:", "0", new Color(255, 152, 0));
        labelTotalProductos = (JLabel) ((JPanel) panelProductos.getComponent(1)).getComponent(0);
        
        // Ingresos Totales
        JPanel panelIngresos = crearPanelMetrica("Ingresos Totales:", "S/0.00", new Color(76, 175, 80));
        labelIngresosTotales = (JLabel) ((JPanel) panelIngresos.getComponent(1)).getComponent(0);
        
        panelMetricas.add(panelTransacciones);
        panelMetricas.add(panelProductos);
        panelMetricas.add(panelIngresos);
        
        panel.add(labelTituloTotales, BorderLayout.NORTH);
        panel.add(panelMetricas, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelMetrica(String titulo, String valor, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel labelTitulo = new JLabel(titulo, JLabel.CENTER);
        labelTitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        labelTitulo.setForeground(new Color(100, 100, 100));
        
        JPanel panelValor = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        panelValor.setBackground(Color.WHITE);
        
        JLabel labelValor = new JLabel(valor, JLabel.CENTER);
        labelValor.setFont(new Font("Arial", Font.BOLD, 20));
        labelValor.setForeground(color);
        
        panelValor.add(labelValor);
        
        panel.add(labelTitulo, BorderLayout.NORTH);
        panel.add(panelValor, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void cargarReporteVentas() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        // Actualizar fecha y hora
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy - HH:mm:ss");
        labelUltimaActualizacion.setText("Última actualización: " + sdf.format(new Date()));
        
        // Consulta para obtener reporte por trabajador del día actual
        String sql = """
            SELECT u.username as trabajador,
                   COUNT(DISTINCT v.id) as transacciones,
                   COALESCE(SUM(dv.cantidad), 0) as productos_vendidos,
                   COALESCE(SUM(v.total), 0) as total_recaudado
            FROM usuarios u
            LEFT JOIN ventas v ON u.id = v.cajera_id 
                              AND DATE(v.fecha_hora) = CURRENT_DATE 
                              AND v.estado = 'ACTIVA'
            LEFT JOIN detalle_ventas dv ON v.id = dv.venta_id
            WHERE u.activo = true AND u.rol IN ('CAJERO', 'SUPERVISOR', 'ADMINISTRADOR')
            GROUP BY u.id, u.username
            HAVING COUNT(DISTINCT v.id) > 0
            ORDER BY total_recaudado DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            int totalTransacciones = 0;
            int totalProductos = 0;
            double totalIngresos = 0.0;
            
            while (rs.next()) {
                String trabajador = rs.getString("trabajador");
                int transacciones = rs.getInt("transacciones");
                int productosVendidos = rs.getInt("productos_vendidos");
                double totalRecaudado = rs.getDouble("total_recaudado");
                
                // Acumular totales
                totalTransacciones += transacciones;
                totalProductos += productosVendidos;
                totalIngresos += totalRecaudado;
                
                Object[] fila = {
                    trabajador,
                    String.valueOf(transacciones),
                    String.valueOf(productosVendidos),
                    "S/" + String.format("%.2f", totalRecaudado)
                };
                modeloTabla.addRow(fila);
            }
            
            // Actualizar totales del día
            labelTotalTransacciones.setText(String.valueOf(totalTransacciones));
            labelTotalProductos.setText(String.valueOf(totalProductos));
            labelIngresosTotales.setText("S/" + String.format("%.2f", totalIngresos));
            
            // Cambiar color de los totales según el valor
            if (totalIngresos > 0) {
                labelIngresosTotales.setForeground(new Color(76, 175, 80)); // Verde
            } else {
                labelIngresosTotales.setForeground(new Color(158, 158, 158)); // Gris
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar reporte de ventas: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
