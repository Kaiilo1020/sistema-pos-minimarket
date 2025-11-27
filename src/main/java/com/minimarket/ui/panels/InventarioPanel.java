package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de gestión de inventario con CRUD de productos
 */
public class InventarioPanel extends JPanel {
    
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;
    private TableRowSorter<DefaultTableModel> sorter;
    
    public InventarioPanel() {
        initializeComponents();
        cargarProductos();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con búsqueda y botones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.add(new JLabel("Buscar:"));
        
        campoBusqueda = new JTextField(20);
        campoBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarTabla();
            }
        });
        panelBusqueda.add(campoBusqueda);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(Color.WHITE);
        
        JButton btnAgregar = new JButton("Agregar Producto");
        JButton btnEditar = new JButton("Editar Producto");
        JButton btnEliminar = new JButton("Eliminar Producto");
        JButton btnActualizar = new JButton("Actualizar");
        
        // Estilo de botones usando UIUtils
        UIUtils.configurarBotonExito(btnAgregar);
        UIUtils.configurarBotonPrimario(btnEditar);
        UIUtils.configurarBotonPeligro(btnEliminar);
        UIUtils.configurarBotonSecundario(btnActualizar);
        
        // Eventos de botones
        btnAgregar.addActionListener(e -> abrirDialogoAgregarProducto());
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        btnActualizar.addActionListener(e -> cargarProductos());
        
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);
        
        // Tabla de productos
        String[] columnas = {"ID", "Nombre", "Descripción", "Precio", "Stock", "Fecha Vencimiento"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setRowHeight(25);
        UIUtils.configurarTabla(tablaProductos);
        
        // Configurar colores alternados en las filas
        tablaProductos.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Obtener la fecha de vencimiento para colorear las filas
                    String fechaVenc = (String) table.getValueAt(row, 5);
                    if (fechaVenc != null && !fechaVenc.equals("N/A")) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            java.util.Date fechaVencimiento = sdf.parse(fechaVenc);
                            java.util.Date hoy = new java.util.Date();
                            long diasRestantes = (fechaVencimiento.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24);
                            
                            if (diasRestantes < 0) {
                                c.setBackground(new Color(255, 205, 210)); // Rojo claro - vencido
                            } else if (diasRestantes <= 7) {
                                c.setBackground(new Color(255, 243, 224)); // Naranja claro - próximo a vencer
                            } else {
                                c.setBackground(new Color(232, 245, 233)); // Verde claro - normal
                            }
                        } catch (Exception e) {
                            c.setBackground(Color.WHITE);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        // Configurar filtro
        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Productos"));
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    
    private void cargarProductos() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        String sql = """
            SELECT p.id, p.nombre, p.descripcion, p.precio, p.stock, p.fecha_vencimiento
            FROM productos p
            WHERE p.activo = true
            ORDER BY p.nombre
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                Object[] fila = {
                    rs.getLong("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    "S/" + String.format("%.2f", rs.getDouble("precio")),
                    rs.getInt("stock"),
                    rs.getDate("fecha_vencimiento") != null ? 
                        sdf.format(rs.getDate("fecha_vencimiento")) : "N/A"
                };
                modeloTabla.addRow(fila);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarTabla() {
        String texto = campoBusqueda.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
    
    private void abrirDialogoAgregarProducto() {
        ProductoDialog dialog = new ProductoDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                   "Agregar Producto", true, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            cargarProductos(); // Recargar la tabla
        }
    }
    
    private void editarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un producto para editar.", 
                "Selección Requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener ID del producto seleccionado
        Long productoId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
        
        ProductoDialog dialog = new ProductoDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                   "Editar Producto", false, productoId);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            cargarProductos(); // Recargar la tabla
        }
    }
    
    private void eliminarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un producto para eliminar.", 
                "Selección Requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombreProducto = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Estás seguro de que deseas eliminar el producto '" + nombreProducto + "'?", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            Long productoId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            
            String sql = "UPDATE productos SET activo = false WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, productoId);
                int filasAfectadas = pstmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Producto eliminado exitosamente.", 
                        "Eliminación Exitosa", 
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarProductos(); // Recargar la tabla
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No se pudo eliminar el producto.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar producto: " + e.getMessage(), 
                    "Error de Base de Datos", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
