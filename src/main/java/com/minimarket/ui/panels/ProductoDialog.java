package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * Diálogo para agregar o editar productos
 */
public class ProductoDialog extends JDialog {
    
    private JTextField campoNombre;
    private JTextArea campoDescripcion;
    private JTextField campoPrecio;
    private JTextField campoStock;
    private JTextField campoFechaVencimiento;
    private JCheckBox checkRequiereLote;
    private JComboBox<String> comboCategoria;
    
    private boolean confirmado = false;
    private boolean esEdicion;
    private Long productoId;
    
    public ProductoDialog(JFrame parent, String titulo, boolean esNuevo, Long productoId) {
        super(parent, titulo, true);
        this.esEdicion = !esNuevo;
        this.productoId = productoId;
        
        initializeComponents();
        
        if (esEdicion && productoId != null) {
            cargarDatosProducto();
        }
        
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoNombre = new JTextField(20);
        panelPrincipal.add(campoNombre, gbc);
        
        // Descripción
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        campoDescripcion = new JTextArea(3, 20);
        campoDescripcion.setLineWrap(true);
        campoDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(campoDescripcion);
        panelPrincipal.add(scrollDesc, gbc);
        
        // Precio
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        panelPrincipal.add(new JLabel("Precio (S/):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoPrecio = new JTextField(20);
        panelPrincipal.add(campoPrecio, gbc);
        
        // Stock
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoStock = new JTextField(20);
        panelPrincipal.add(campoStock, gbc);
        
        // Categoría
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comboCategoria = new JComboBox<>();
        cargarCategorias();
        panelPrincipal.add(comboCategoria, gbc);
        
        // Fecha de vencimiento
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Fecha Vencimiento (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoFechaVencimiento = new JTextField(20);
        campoFechaVencimiento.setToolTipText("Formato: dd/MM/yyyy (opcional)");
        panelPrincipal.add(campoFechaVencimiento, gbc);
        
        // Requiere lote
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        checkRequiereLote = new JCheckBox("Requiere manejo de lotes");
        panelPrincipal.add(checkRequiereLote, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton(esEdicion ? "Actualizar" : "Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.setBackground(new Color(46, 125, 50));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(158, 158, 158));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnGuardar.addActionListener(e -> guardarProducto());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private void cargarCategorias() {
        comboCategoria.addItem("Seleccionar categoría...");
        
        String sql = "SELECT nombre FROM categorias WHERE activo = true ORDER BY nombre";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                comboCategoria.addItem(rs.getString("nombre"));
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar categorías: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDatosProducto() {
        String sql = """
            SELECT p.nombre, p.descripcion, p.precio, p.stock, p.fecha_vencimiento, 
                   p.requiere_lote, c.nombre as categoria_nombre
            FROM productos p
            LEFT JOIN categorias c ON p.categoria_id = c.id
            WHERE p.id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                campoNombre.setText(rs.getString("nombre"));
                campoDescripcion.setText(rs.getString("descripcion"));
                campoPrecio.setText(String.valueOf(rs.getDouble("precio")));
                campoStock.setText(String.valueOf(rs.getInt("stock")));
                checkRequiereLote.setSelected(rs.getBoolean("requiere_lote"));
                
                // Fecha de vencimiento
                Date fechaVenc = rs.getDate("fecha_vencimiento");
                if (fechaVenc != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    campoFechaVencimiento.setText(sdf.format(fechaVenc));
                }
                
                // Categoría
                String categoriaNombre = rs.getString("categoria_nombre");
                if (categoriaNombre != null) {
                    comboCategoria.setSelectedItem(categoriaNombre);
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar datos del producto: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarProducto() {
        // Validaciones
        if (campoNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double precio;
        try {
            precio = Double.parseDouble(campoPrecio.getText().trim());
            if (precio < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido mayor o igual a 0.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int stock;
        try {
            stock = Integer.parseInt(campoStock.getText().trim());
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero mayor o igual a 0.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar fecha de vencimiento si se proporciona
        Date fechaVencimiento = null;
        if (!campoFechaVencimiento.getText().trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                java.util.Date fecha = sdf.parse(campoFechaVencimiento.getText().trim());
                fechaVencimiento = new Date(fecha.getTime());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "La fecha de vencimiento debe tener el formato dd/MM/yyyy.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Obtener ID de categoría
        Long categoriaId = null;
        String categoriaSeleccionada = (String) comboCategoria.getSelectedItem();
        if (categoriaSeleccionada != null && !categoriaSeleccionada.equals("Seleccionar categoría...")) {
            categoriaId = obtenerCategoriaId(categoriaSeleccionada);
        }
        
        // Guardar en base de datos
        String sql;
        if (esEdicion) {
            sql = """
                UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, stock = ?, 
                       categoria_id = ?, fecha_vencimiento = ?, requiere_lote = ?, 
                       updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """;
        } else {
            sql = """
                INSERT INTO productos (nombre, descripcion, precio, stock, categoria_id, 
                                     fecha_vencimiento, requiere_lote, activo) 
                VALUES (?, ?, ?, ?, ?, ?, ?, true)
            """;
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, campoNombre.getText().trim());
            pstmt.setString(2, campoDescripcion.getText().trim());
            pstmt.setDouble(3, precio);
            pstmt.setInt(4, stock);
            
            if (categoriaId != null) {
                pstmt.setLong(5, categoriaId);
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }
            
            if (fechaVencimiento != null) {
                pstmt.setDate(6, fechaVencimiento);
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            pstmt.setBoolean(7, checkRequiereLote.isSelected());
            
            if (esEdicion) {
                pstmt.setLong(8, productoId);
            }
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                confirmado = true;
                JOptionPane.showMessageDialog(this, 
                    "Producto " + (esEdicion ? "actualizado" : "guardado") + " exitosamente.", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo " + (esEdicion ? "actualizar" : "guardar") + " el producto.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al " + (esEdicion ? "actualizar" : "guardar") + " producto: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Long obtenerCategoriaId(String nombreCategoria) {
        String sql = "SELECT id FROM categorias WHERE nombre = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreCategoria);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("id");
            }
            
        } catch (SQLException e) {
            // Log error but don't show to user
        }
        
        return null;
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}
