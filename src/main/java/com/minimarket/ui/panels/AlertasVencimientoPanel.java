package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Panel para mostrar alertas de productos próximos a vencer o ya vencidos
 */
public class AlertasVencimientoPanel extends JPanel {
    
    private JTable tablaAlertas;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalProductos;
    
    // Colores para los estados
    private static final Color COLOR_VENCIDO = new Color(244, 67, 54, 100);      // Rojo claro
    private static final Color COLOR_POR_VENCER = new Color(255, 193, 7, 100);   // Amarillo claro
    private static final Color COLOR_NORMAL = Color.WHITE;
    
    public AlertasVencimientoPanel() {
        initializeComponents();
        setupLayout();
        cargarAlertas();
    }
    
    private void initializeComponents() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
    }
    
    private void setupLayout() {
        // Panel superior con título y botones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Título
        JLabel titulo = new JLabel("⚠️ ALERTAS DE VENCIMIENTO");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(new Color(44, 62, 80));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(Color.WHITE);
        
        JButton btnRetirarVencidos = new JButton("Retirar Vencidos");
        btnRetirarVencidos.setFont(new Font("Arial", Font.BOLD, 12));
        btnRetirarVencidos.setPreferredSize(new Dimension(130, 32));
        btnRetirarVencidos.addActionListener(e -> retirarProductosVencidos());
        
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 12));
        btnActualizar.setPreferredSize(new Dimension(100, 32));
        btnActualizar.addActionListener(e -> cargarAlertas());
        
        panelBotones.add(btnRetirarVencidos);
        panelBotones.add(btnActualizar);
        
        panelSuperior.add(titulo, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);
        
        // Tabla de alertas
        String[] columnas = {"ID", "Producto", "Stock", "Fecha Venc.", "Días Restantes", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaAlertas = new JTable(modeloTabla);
        tablaAlertas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaAlertas.setRowHeight(30);
        tablaAlertas.getTableHeader().setBackground(Color.LIGHT_GRAY);
        tablaAlertas.getTableHeader().setForeground(Color.BLACK);
        tablaAlertas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar ancho de columnas
        tablaAlertas.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tablaAlertas.getColumnModel().getColumn(1).setPreferredWidth(300);  // Producto
        tablaAlertas.getColumnModel().getColumn(2).setPreferredWidth(80);   // Stock
        tablaAlertas.getColumnModel().getColumn(3).setPreferredWidth(120);  // Fecha Venc.
        tablaAlertas.getColumnModel().getColumn(4).setPreferredWidth(120);  // Días Restantes
        tablaAlertas.getColumnModel().getColumn(5).setPreferredWidth(150);  // Estado
        
        // Renderer personalizado para colores
        tablaAlertas.setDefaultRenderer(Object.class, new AlertasTableCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tablaAlertas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos con Alertas de Vencimiento"));
        
        // Panel inferior con información
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        lblTotalProductos = new JLabel("Total de alertas: 0");
        lblTotalProductos.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalProductos.setForeground(new Color(108, 117, 125));
        
        // Leyenda de colores
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelLeyenda.setBackground(Color.WHITE);
        
        JLabel leyendaVencido = new JLabel("■ Vencido");
        leyendaVencido.setForeground(new Color(244, 67, 54));
        leyendaVencido.setFont(new Font("Arial", Font.BOLD, 11));
        
        JLabel leyendaPorVencer = new JLabel("■ Por vencer");
        leyendaPorVencer.setForeground(new Color(255, 193, 7));
        leyendaPorVencer.setFont(new Font("Arial", Font.BOLD, 11));
        
        panelLeyenda.add(leyendaVencido);
        panelLeyenda.add(Box.createHorizontalStrut(15));
        panelLeyenda.add(leyendaPorVencer);
        
        panelInferior.add(lblTotalProductos, BorderLayout.WEST);
        panelInferior.add(panelLeyenda, BorderLayout.EAST);
        
        // Agregar componentes al panel principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void cargarAlertas() {
        modeloTabla.setRowCount(0);
        
        String sql = """
            SELECT p.id, p.nombre, p.stock, p.fecha_vencimiento
            FROM productos p 
            WHERE p.activo = true 
            AND p.fecha_vencimiento IS NOT NULL 
            AND p.fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
            ORDER BY p.fecha_vencimiento ASC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            int totalAlertas = 0;
            LocalDate hoy = LocalDate.now();
            
            while (rs.next()) {
                Long id = rs.getLong("id");
                String nombre = rs.getString("nombre");
                int stock = rs.getInt("stock");
                Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                
                if (fechaVencimiento != null) {
                    LocalDate fechaVenc = fechaVencimiento.toLocalDate();
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVenc);
                    
                    String estado;
                    String diasTexto;
                    
                    if (diasRestantes < 0) {
                        estado = "VENCIDO (" + Math.abs(diasRestantes) + " días atrás)";
                        diasTexto = Math.abs(diasRestantes) + " días atrás";
                    } else if (diasRestantes == 0) {
                        estado = "VENCE HOY";
                        diasTexto = "0 días";
                    } else if (diasRestantes <= 7) {
                        estado = "VENCIDO (" + diasRestantes + " días atrás)";
                        diasTexto = diasRestantes + " días";
                    } else {
                        estado = "Por vencer";
                        diasTexto = diasRestantes + " días";
                    }
                    
                    Object[] fila = {
                        id,
                        nombre,
                        stock,
                        fechaVenc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        diasTexto,
                        estado
                    };
                    
                    modeloTabla.addRow(fila);
                    totalAlertas++;
                }
            }
            
            lblTotalProductos.setText("Total de alertas: " + totalAlertas);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar alertas: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void retirarProductosVencidos() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea marcar como inactivos todos los productos vencidos?\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar Retiro de Productos Vencidos",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = """
                UPDATE productos 
                SET activo = false 
                WHERE fecha_vencimiento < CURRENT_DATE 
                AND activo = true
            """;
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                int productosRetirados = pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this,
                    "Se han retirado " + productosRetirados + " productos vencidos del inventario.",
                    "Productos Retirados",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar la tabla
                cargarAlertas();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al retirar productos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Renderer personalizado para colorear las filas según el estado
     */
    private class AlertasTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component component = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String estado = (String) table.getValueAt(row, 5); // Columna Estado
                
                if (estado.contains("VENCIDO") || estado.contains("VENCE HOY")) {
                    component.setBackground(COLOR_VENCIDO);
                } else if (estado.contains("Por vencer")) {
                    component.setBackground(COLOR_POR_VENCER);
                } else {
                    component.setBackground(COLOR_NORMAL);
                }
            }
            
            return component;
        }
    }
}
