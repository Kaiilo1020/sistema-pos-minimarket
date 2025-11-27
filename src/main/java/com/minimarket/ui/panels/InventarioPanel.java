package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

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
        JPanel panelBusqueda = UIUtils.crearPanelBotones(FlowLayout.LEFT);
        panelBusqueda.add(new JLabel("Buscar:"));
        
        campoBusqueda = new JTextField(20);
        campoBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarTabla();
            }
        });
        panelBusqueda.add(campoBusqueda);
        
        // Panel de botones
        JPanel panelBotones = UIUtils.crearPanelBotones(FlowLayout.RIGHT);
        
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
        UIUtils.configurarTablaConVencimiento(tablaProductos, 5); // Columna 5 = Fecha Vencimiento
        
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
            UIUtils.mostrarError(this, "Error al cargar productos: " + e.getMessage());
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
            UIUtils.mostrarError(this, "Por favor, selecciona un producto para editar.");
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
            UIUtils.mostrarError(this, "Por favor, selecciona un producto para eliminar.");
            return;
        }
        
        String nombreProducto = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        
        if (UIUtils.confirmar(this, "¿Estás seguro de que deseas eliminar el producto '" + nombreProducto + "'?")) {
            Long productoId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            
            String sql = "UPDATE productos SET activo = false WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, productoId);
                int filasAfectadas = pstmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    UIUtils.mostrarExito(this, "Producto eliminado exitosamente.");
                    cargarProductos(); // Recargar la tabla
                } else {
                    UIUtils.mostrarError(this, "No se pudo eliminar el producto.");
                }
                
            } catch (SQLException e) {
                UIUtils.mostrarError(this, "Error al eliminar producto: " + e.getMessage());
            }
        }
    }
}
