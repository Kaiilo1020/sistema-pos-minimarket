package com.minimarket.gui.panels;

import com.minimarket.patterns.creational.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel de ventas y facturación
 */
public class VentasPanel extends JPanel {
    
    // Componentes de datos del cliente
    private JComboBox<String> comboTipoComprobante;
    private JTextField campoCliente;
    private JTextField campoDNI;
    
    // Componentes del catálogo de productos
    private JTextField campoBusquedaProductos;
    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    
    // Componentes del carrito de compras
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel labelTotal;
    
    // Botones de acción
    private JButton btnAgregarCarrito;
    private JButton btnQuitar;
    private JButton btnLimpiar;
    private JButton btnRegistrarVenta;
    
    private double totalVenta = 0.0;
    
    public VentasPanel() {
        initializeComponents();
        cargarProductos();
        actualizarTotal();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel principal con tres secciones
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 1. Panel superior - Datos del cliente
        JPanel panelCliente = crearPanelCliente();
        
        // 2. Panel central - Catálogo y carrito
        JPanel panelCentral = crearPanelCentral();
        
        // 3. Panel inferior - Botones de acción
        JPanel panelBotones = crearPanelBotones();
        
        panelPrincipal.add(panelCliente, BorderLayout.NORTH);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente / Comprobante"));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Tipo de comprobante
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        comboTipoComprobante = new JComboBox<>(new String[]{"BOLETA", "FACTURA"});
        panel.add(comboTipoComprobante, gbc);
        
        // Cliente/Razón Social
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Cliente / Razón Social:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoCliente = new JTextField();
        panel.add(campoCliente, gbc);
        
        // DNI/RUC
        gbc.gridx = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("DNI / RUC:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoDNI = new JTextField();
        panel.add(campoDNI, gbc);
        
        return panel;
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);
        
        // Panel izquierdo - Catálogo de productos
        JPanel panelCatalogo = new JPanel(new BorderLayout());
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("Catálogo de Productos"));
        
        // Búsqueda de productos
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.add(new JLabel("Buscar:"));
        campoBusquedaProductos = new JTextField(20);
        campoBusquedaProductos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buscarProductos();
            }
        });
        panelBusqueda.add(campoBusquedaProductos);
        
        // Tabla de productos
        String[] columnasProductos = {"ID", "Nombre", "Precio", "Stock"};
        modeloProductos = new DefaultTableModel(columnasProductos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setRowHeight(25);
        configurarTabla(tablaProductos);
        
        // Botón agregar al carrito
        btnAgregarCarrito = new JButton("Agregar al Carrito");
        btnAgregarCarrito.setFont(new Font("Arial", Font.BOLD, 12));
        btnAgregarCarrito.setPreferredSize(new Dimension(150, 35));
        btnAgregarCarrito.addActionListener(e -> agregarAlCarrito());
        
        JPanel panelBotonAgregar = new JPanel(new FlowLayout());
        panelBotonAgregar.setBackground(Color.WHITE);
        panelBotonAgregar.add(btnAgregarCarrito);
        
        panelCatalogo.add(panelBusqueda, BorderLayout.NORTH);
        panelCatalogo.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        panelCatalogo.add(panelBotonAgregar, BorderLayout.SOUTH);
        
        // Panel derecho - Carrito de compras
        JPanel panelCarrito = new JPanel(new BorderLayout());
        panelCarrito.setBorder(BorderFactory.createTitledBorder("Carrito de Compras"));
        
        // Tabla del carrito
        String[] columnasCarrito = {"ID", "Producto", "Cant.", "Precio U.", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnasCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Solo la cantidad es editable
            }
        };
        
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.setRowHeight(25);
        configurarTabla(tablaCarrito);
        
        // Listener para cambios en cantidad
        tablaCarrito.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Columna de cantidad
                // Validar que la cantidad sea válida
                try {
                    int fila = e.getFirstRow();
                    Object valorCantidad = modeloCarrito.getValueAt(fila, 2);
                    int cantidad = Integer.parseInt(valorCantidad.toString());
                    
                    if (cantidad <= 0) {
                        modeloCarrito.setValueAt(1, fila, 2);
                    }
                    
                    actualizarSubtotalFila(fila);
                    actualizarTotal();
                } catch (NumberFormatException ex) {
                    // Si no es un número válido, restaurar a 1
                    modeloCarrito.setValueAt(1, e.getFirstRow(), 2);
                }
            }
        });
        
        // Panel de total y botones del carrito
        JPanel panelTotalCarrito = new JPanel(new BorderLayout());
        panelTotalCarrito.setBackground(Color.WHITE);
        
        // Total
        labelTotal = new JLabel("TOTAL: S/0.00", JLabel.RIGHT);
        labelTotal.setFont(new Font("Arial", Font.BOLD, 16));
        labelTotal.setForeground(new Color(46, 125, 50));
        
        // Botones del carrito
        JPanel panelBotonesCarrito = new JPanel(new FlowLayout());
        panelBotonesCarrito.setBackground(Color.WHITE);
        
        btnQuitar = new JButton("Quitar");
        btnLimpiar = new JButton("Limpiar");
        
        btnQuitar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnQuitar.setPreferredSize(new Dimension(80, 30));
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        
        btnLimpiar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLimpiar.setPreferredSize(new Dimension(80, 30));
        btnLimpiar.addActionListener(e -> limpiarCarrito());
        
        panelBotonesCarrito.add(btnQuitar);
        panelBotonesCarrito.add(btnLimpiar);
        
        panelTotalCarrito.add(labelTotal, BorderLayout.NORTH);
        panelTotalCarrito.add(panelBotonesCarrito, BorderLayout.SOUTH);
        
        panelCarrito.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        panelCarrito.add(panelTotalCarrito, BorderLayout.SOUTH);
        
        panel.add(panelCatalogo);
        panel.add(panelCarrito);
        
        return panel;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnRegistrarVenta = new JButton("Registrar Venta / Emitir Comprobante");
        btnRegistrarVenta.setFont(new Font("Arial", Font.BOLD, 13));
        btnRegistrarVenta.setPreferredSize(new Dimension(280, 40));
        btnRegistrarVenta.addActionListener(e -> registrarVenta());
        
        panel.add(btnRegistrarVenta);
        
        return panel;
    }
    
    private void configurarTabla(JTable tabla) {
        tabla.getTableHeader().setBackground(Color.LIGHT_GRAY);
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
    }
    
    private void cargarProductos() {
        modeloProductos.setRowCount(0);
        
        String sql = """
            SELECT id, nombre, precio, stock 
            FROM productos 
            WHERE activo = true AND stock > 0
            ORDER BY nombre
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] fila = {
                    rs.getLong("id"),
                    rs.getString("nombre"),
                    "S/" + String.format("%.2f", rs.getDouble("precio")),
                    rs.getInt("stock")
                };
                modeloProductos.addRow(fila);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarProductos() {
        String textoBusqueda = campoBusquedaProductos.getText().trim();
        modeloProductos.setRowCount(0);
        
        String sql = """
            SELECT id, nombre, precio, stock 
            FROM productos 
            WHERE activo = true AND stock > 0 
            AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(descripcion) LIKE LOWER(?))
            ORDER BY nombre
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String patron = "%" + textoBusqueda + "%";
            pstmt.setString(1, patron);
            pstmt.setString(2, patron);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] fila = {
                    rs.getLong("id"),
                    rs.getString("nombre"),
                    "S/" + String.format("%.2f", rs.getDouble("precio")),
                    rs.getInt("stock")
                };
                modeloProductos.addRow(fila);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al buscar productos: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void agregarAlCarrito() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un producto para agregar al carrito.", 
                "Selección Requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long productoId = (Long) modeloProductos.getValueAt(filaSeleccionada, 0);
        String nombreProducto = (String) modeloProductos.getValueAt(filaSeleccionada, 1);
        String precioStr = (String) modeloProductos.getValueAt(filaSeleccionada, 2);
        int stockDisponible = (Integer) modeloProductos.getValueAt(filaSeleccionada, 3);
        
        // Extraer el precio numérico
        double precio = Double.parseDouble(precioStr.replace("S/", ""));
        
        // Verificar si el producto ya está en el carrito
        int filaExistente = -1;
        int cantidadEnCarrito = 0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            if (productoId.equals(modeloCarrito.getValueAt(i, 0))) {
                filaExistente = i;
                cantidadEnCarrito = (Integer) modeloCarrito.getValueAt(i, 2);
                break;
            }
        }
        
        // Calcular stock disponible considerando lo que ya está en el carrito
        int stockRestante = stockDisponible - cantidadEnCarrito;
        
        if (stockRestante <= 0) {
            JOptionPane.showMessageDialog(this, 
                "No hay más stock disponible para este producto.", 
                "Stock Agotado", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Mostrar diálogo para seleccionar cantidad
        CantidadDialog dialog = new CantidadDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            nombreProducto, 
            stockRestante
        );
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            int cantidadSeleccionada = dialog.getCantidadSeleccionada();
            
            if (filaExistente != -1) {
                // Actualizar cantidad existente
                int nuevaCantidad = cantidadEnCarrito + cantidadSeleccionada;
                modeloCarrito.setValueAt(nuevaCantidad, filaExistente, 2);
                actualizarSubtotalFila(filaExistente);
            } else {
                // Agregar nuevo producto al carrito
                double subtotal = Math.round((cantidadSeleccionada * precio) * 100.0) / 100.0;
                Object[] filaCarrito = {
                    productoId,
                    nombreProducto,
                    cantidadSeleccionada,
                    precio,
                    subtotal
                };
                modeloCarrito.addRow(filaCarrito);
            }
            
            actualizarTotal();
        }
    }
    
    private void actualizarSubtotalFila(int fila) {
        try {
            int cantidad = (Integer) modeloCarrito.getValueAt(fila, 2);
            double precioUnitario = (Double) modeloCarrito.getValueAt(fila, 3);
            double subtotal = Math.round((cantidad * precioUnitario) * 100.0) / 100.0;
            modeloCarrito.setValueAt(subtotal, fila, 4);
        } catch (Exception e) {
            // Manejar errores de conversión
        }
    }
    
    private void actualizarTotal() {
        totalVenta = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            try {
                double subtotal = (Double) modeloCarrito.getValueAt(i, 4);
                totalVenta += subtotal;
            } catch (Exception e) {
                // Manejar errores
            }
        }
        // Redondear el total a 2 decimales
        totalVenta = Math.round(totalVenta * 100.0) / 100.0;
        labelTotal.setText("TOTAL: S/" + String.format("%.2f", totalVenta));
    }
    
    private void quitarDelCarrito() {
        int filaSeleccionada = tablaCarrito.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un producto para quitar del carrito.", 
                "Selección Requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        modeloCarrito.removeRow(filaSeleccionada);
        actualizarTotal();
    }
    
    private void limpiarCarrito() {
        modeloCarrito.setRowCount(0);
        actualizarTotal();
    }
    
    private void registrarVenta() {
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "El carrito está vacío. Agrega productos antes de registrar la venta.", 
                "Carrito Vacío", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (totalVenta <= 0) {
            JOptionPane.showMessageDialog(this, 
                "El total de la venta debe ser mayor a 0.", 
                "Total Inválido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Mostrar diálogo de confirmación con método de pago
        MetodoPagoDialog dialog = new MetodoPagoDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            procesarVenta(dialog.getMetodoPagoSeleccionado());
        }
    }
    
    private void procesarVenta(String metodoPago) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // 1. Insertar la venta
            String sqlVenta = """
                INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, 
                                   subtotal, igv, total, estado) 
                VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, 'ACTIVA')
            """;
            
            PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            
            String numeroVenta = generarNumeroVenta();
            double subtotal = totalVenta / 1.18; // Calcular subtotal sin IGV
            double igv = totalVenta - subtotal;
            
            pstmtVenta.setString(1, numeroVenta);
            pstmtVenta.setLong(2, 1L); // ID del usuario actual (simplificado)
            pstmtVenta.setString(3, metodoPago);
            pstmtVenta.setDouble(4, subtotal);
            pstmtVenta.setDouble(5, igv);
            pstmtVenta.setDouble(6, totalVenta);
            
            pstmtVenta.executeUpdate();
            
            // Obtener ID de la venta generada
            ResultSet rsVenta = pstmtVenta.getGeneratedKeys();
            long ventaId = 0;
            if (rsVenta.next()) {
                ventaId = rsVenta.getLong(1);
            }
            
            // 2. Insertar detalles de venta y actualizar stock
            String sqlDetalle = """
                INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id = ?";
            
            PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle);
            PreparedStatement pstmtStock = conn.prepareStatement(sqlActualizarStock);
            
            for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                Long productoId = (Long) modeloCarrito.getValueAt(i, 0);
                int cantidad = (Integer) modeloCarrito.getValueAt(i, 2);
                double precioUnitario = (Double) modeloCarrito.getValueAt(i, 3);
                double subtotalDetalle = (Double) modeloCarrito.getValueAt(i, 4);
                
                // Insertar detalle
                pstmtDetalle.setLong(1, ventaId);
                pstmtDetalle.setLong(2, productoId);
                pstmtDetalle.setInt(3, cantidad);
                pstmtDetalle.setDouble(4, precioUnitario);
                pstmtDetalle.setDouble(5, subtotalDetalle);
                pstmtDetalle.executeUpdate();
                
                // Actualizar stock
                pstmtStock.setInt(1, cantidad);
                pstmtStock.setLong(2, productoId);
                pstmtStock.executeUpdate();
            }
            
            conn.commit(); // Confirmar transacción
            
            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(this, 
                "Venta registrada exitosamente.\nNúmero de venta: " + numeroVenta + 
                "\nTotal: S/" + String.format("%.2f", totalVenta), 
                "Venta Exitosa", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Limpiar formulario
            limpiarFormulario();
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                // Log error
            }
            
            JOptionPane.showMessageDialog(this, 
                "Error al registrar la venta: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                // Log error
            }
        }
    }
    
    private String generarNumeroVenta() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fecha = sdf.format(new Date());
        long timestamp = System.currentTimeMillis() % 10000;
        return "VTA-" + fecha + "-" + String.format("%04d", timestamp);
    }
    
    private void limpiarFormulario() {
        campoCliente.setText("");
        campoDNI.setText("");
        campoBusquedaProductos.setText("");
        limpiarCarrito();
        cargarProductos(); // Recargar productos para actualizar stock
    }
}
