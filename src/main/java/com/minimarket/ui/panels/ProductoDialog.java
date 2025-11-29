package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.model.Producto;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.Timer;

/**
 * Diálogo para agregar o editar productos
 */
public class ProductoDialog extends JDialog {
    
    private JTextField campoNombre;
    private JList<String> listaSugerencias;
    private JPopupMenu popupSugerencias;
    private JTextArea campoDescripcion;
    private JTextField campoPrecio;
    private JTextField campoStock;
    private JTextField campoFechaVencimiento;
    
    private boolean confirmado = false;
    private final boolean esEdicion;
    private final Long productoId;
    private final ProductoDAO productoDAO;
    private Timer timerDebounce; // Timer para debounce del autocompletado
    
    public ProductoDialog(JFrame parent, String titulo, boolean esNuevo, Long productoId) {
        super(parent, titulo, true);
        this.esEdicion = !esNuevo;
        this.productoId = productoId;
        this.productoDAO = new ProductoDAO();
        
        UIUtils.configurarDialogo(this, titulo, 500, 400);
        buildUI();
        
        if (esEdicion && productoId != null) {
            cargarDatosProducto();
        } else if (!esEdicion) {
            // Solo activar autocompletado para productos nuevos
            configurarAutocompletado();
        }
    }
    
    @Override
    public void dispose() {
        // Cancelar el timer al cerrar el diálogo para evitar memory leaks
        if (timerDebounce != null) {
            timerDebounce.stop();
        }
        super.dispose();
    }
    
    /* ============================== UI ============================== */
    
    private void buildUI() {
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonsPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = baseGbc();
        
        campoNombre = new JTextField(20);
        addField(panel, gbc, 0, "Nombre:", campoNombre);
        
        // Configurar popup de sugerencias (solo para productos nuevos)
        if (!esEdicion) {
            listaSugerencias = new JList<>();
            listaSugerencias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listaSugerencias.setVisibleRowCount(5);
            popupSugerencias = new JPopupMenu();
            popupSugerencias.add(new JScrollPane(listaSugerencias));
        }
        
        campoDescripcion = new JTextArea(3, 20);
        campoDescripcion.setLineWrap(true);
        campoDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(campoDescripcion);
        addField(panel, gbc, 1, "Descripción:", scrollDesc, GridBagConstraints.BOTH, 0.3);
        
        campoPrecio = new JTextField(20);
        addField(panel, gbc, 2, "Precio (S/):", campoPrecio);
        
        campoStock = new JTextField(20);
        addField(panel, gbc, 3, "Stock:", campoStock);
        
        campoFechaVencimiento = new JTextField(20);
        campoFechaVencimiento.setToolTipText("Formato: dd/MM/yyyy (opcional)");
        addField(panel, gbc, 4, "Fecha Vencimiento (dd/MM/yyyy):", campoFechaVencimiento);
        
        return panel;
    }
    
    private JPanel buildButtonsPanel() {
        JPanel panelBotones = UIUtils.crearPanelBotones(FlowLayout.RIGHT);
        JButton btnGuardar = new JButton(esEdicion ? "Actualizar" : "Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        EstilosApp.estilizarBoton(btnGuardar);
        EstilosApp.estilizarBotonNeutro(btnCancelar);
        
        btnGuardar.addActionListener(e -> guardarProducto());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        return panelBotones;
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
        addField(panel, gbc, row, label, component, GridBagConstraints.HORIZONTAL, 0);
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label,
                          Component component, int fill, double weightY) {
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; gbc.weighty = 0;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1; gbc.fill = fill; gbc.weightx = 1.0; gbc.weighty = weightY;
        panel.add(component, gbc);
    }
    
    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
    
    private void cargarDatosProducto() {
        String sql = """
            SELECT nombre, descripcion, precio, stock, fecha_vencimiento
            FROM productos
            WHERE id = ?
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
                
                // Fecha de vencimiento
                Date fechaVenc = rs.getDate("fecha_vencimiento");
                if (fechaVenc != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    campoFechaVencimiento.setText(sdf.format(fechaVenc));
                }
            }
            
        } catch (SQLException e) {
            UIUtils.mostrarError(this, "Error al cargar datos del producto: " + e.getMessage());
        }
    }
    
    private void guardarProducto() {
        ProductoFormData datos = recopilarDatosFormulario();
        if (datos == null) {
            return; // ya se mostró el mensaje de error
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (esEdicion) {
                // Actualizar producto existente
                actualizarProducto(conn, datos);
            } else {
                // Crear nuevo producto o reactivar uno desactivado
                crearOReactivarProducto(conn, datos);
            }
        } catch (SQLException e) {
            UIUtils.mostrarError(this,
                "Error al " + (esEdicion ? "actualizar" : "guardar") + " producto: " + e.getMessage());
        }
    }
    
    private void actualizarProducto(Connection conn, ProductoFormData datos) throws SQLException {
        // Verificar si el nuevo nombre ya existe en otro producto activo
        if (productoDAO.existeProductoActivoPorNombre(conn, datos.nombre(), productoId)) {
            UIUtils.mostrarError(this, 
                "Ya existe otro producto activo con el nombre '" + datos.nombre() + "'.\n" +
                "Por favor, use un nombre diferente.");
            return;
        }
        
        String sql = """
            UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, stock = ?, 
                   fecha_vencimiento = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, datos.nombre());
            pstmt.setString(2, datos.descripcion());
            pstmt.setDouble(3, datos.precio());
            pstmt.setInt(4, datos.stock());
            
            if (datos.fechaVencimiento() != null) {
                pstmt.setDate(5, datos.fechaVencimiento());
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            
            pstmt.setLong(6, productoId);
            
            if (pstmt.executeUpdate() > 0) {
                confirmado = true;
                UIUtils.mostrarExito(this, "Producto actualizado exitosamente.");
                dispose();
            } else {
                UIUtils.mostrarError(this, "No se pudo actualizar el producto.");
            }
        }
    }
    
    private void crearOReactivarProducto(Connection conn, ProductoFormData datos) throws SQLException {
        // PRIMERO: Verificar si ya existe un producto ACTIVO con el mismo nombre (evitar duplicados)
        if (productoDAO.existeProductoActivoPorNombre(conn, datos.nombre(), null)) {
            UIUtils.mostrarError(this, 
                "Ya existe un producto activo con el nombre '" + datos.nombre() + "'.\n" +
                "Por favor, edite el producto existente o use un nombre diferente.");
            return;
        }
        
        // SEGUNDO: Verificar si existe un producto desactivado con el mismo nombre
        Producto productoDesactivado = productoDAO.buscarProductoDesactivadoPorNombre(conn, datos.nombre());
        
        if (productoDesactivado != null) {
            // Reactivar y actualizar el producto existente
            productoDAO.reactivarYActualizarProducto(conn, productoDesactivado.getId(), 
                datos.nombre(), datos.descripcion(), datos.precio(), datos.stock(), 
                datos.fechaVencimiento());
            
            confirmado = true;
            UIUtils.mostrarExito(this, 
                "Producto reactivado y actualizado exitosamente.\n" +
                "Se mantuvo el mismo ID: " + productoDesactivado.getId());
            dispose();
        } else {
            // Crear nuevo producto
            String sql = """
                INSERT INTO productos (nombre, descripcion, precio, stock, 
                                     fecha_vencimiento, activo) 
                VALUES (?, ?, ?, ?, ?, true)
            """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, datos.nombre());
                pstmt.setString(2, datos.descripcion());
                pstmt.setDouble(3, datos.precio());
                pstmt.setInt(4, datos.stock());
                
                if (datos.fechaVencimiento() != null) {
                    pstmt.setDate(5, datos.fechaVencimiento());
                } else {
                    pstmt.setNull(5, Types.DATE);
                }
                
                if (pstmt.executeUpdate() > 0) {
                    confirmado = true;
                    UIUtils.mostrarExito(this, "Producto guardado exitosamente.");
                    dispose();
                } else {
                    UIUtils.mostrarError(this, "No se pudo guardar el producto.");
                }
            }
        }
    }
    
    private ProductoFormData recopilarDatosFormulario() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            UIUtils.mostrarError(this, "El nombre es obligatorio.");
            return null;
        }
        
        double precio;
        try {
            precio = Double.parseDouble(campoPrecio.getText().trim());
            if (precio < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            UIUtils.mostrarError(this, "El precio debe ser un número válido mayor o igual a 0.");
            return null;
        }
        
        int stock;
        try {
            stock = Integer.parseInt(campoStock.getText().trim());
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            UIUtils.mostrarError(this, "El stock debe ser un número entero mayor o igual a 0.");
            return null;
        }
        
        Date fechaVencimiento = null;
        String textoFecha = campoFechaVencimiento.getText().trim();
        if (!textoFecha.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                java.util.Date fecha = sdf.parse(textoFecha);
                fechaVencimiento = new Date(fecha.getTime());
            } catch (Exception e) {
                UIUtils.mostrarError(this, "La fecha de vencimiento debe tener el formato dd/MM/yyyy.");
                return null;
            }
        }
        
        return new ProductoFormData(
            nombre,
            campoDescripcion.getText().trim(),
            precio,
            stock,
            fechaVencimiento
        );
    }
    
    /**
     * Configura el autocompletado para el campo nombre (solo productos nuevos)
     */
    private void configurarAutocompletado() {
        if (listaSugerencias == null || campoNombre == null) {
            return;
        }
        
        // Timer para debounce: espera 300ms después de que el usuario deje de escribir
        timerDebounce = new Timer(300, e -> {
            actualizarSugerencias();
        });
        timerDebounce.setRepeats(false); // Solo ejecutar una vez
        
        campoNombre.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Reiniciar el timer cada vez que se escribe
                timerDebounce.restart();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                // Reiniciar el timer cada vez que se borra
                timerDebounce.restart();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Reiniciar el timer
                timerDebounce.restart();
            }
        });
        
        // Listener para seleccionar con doble clic o Enter
        listaSugerencias.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarSugerencia();
                }
            }
        });
        
        campoNombre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupSugerencias.isVisible()) {
                    listaSugerencias.requestFocus();
                    if (listaSugerencias.getModel().getSize() > 0) {
                        listaSugerencias.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && popupSugerencias.isVisible() 
                          && listaSugerencias.getSelectedValue() != null) {
                    seleccionarSugerencia();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupSugerencias.setVisible(false);
                }
            }
        });
    }
    
    /**
     * Actualiza las sugerencias del autocompletado basado en el texto ingresado
     */
    private void actualizarSugerencias() {
        if (campoNombre == null || listaSugerencias == null || popupSugerencias == null) {
            return;
        }
        
        String texto = campoNombre.getText().trim();
        
        // Solo buscar si hay al menos 2 caracteres
        if (texto.length() < 2) {
            popupSugerencias.setVisible(false);
            return;
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            List<Producto> productos = productoDAO.buscarProductosDesactivadosPorNombre(conn, texto);
            
            if (productos.isEmpty()) {
                popupSugerencias.setVisible(false);
                return;
            }
            
            // Actualizar lista de sugerencias
            DefaultListModel<String> modelo = new DefaultListModel<>();
            for (Producto producto : productos) {
                modelo.addElement(producto.getNombre());
            }
            listaSugerencias.setModel(modelo);
            
            // Mostrar popup debajo del campo
            Point ubicacionCampo = campoNombre.getLocationOnScreen();
            Point ubicacionDialogo = this.getLocationOnScreen();
            int x = ubicacionCampo.x - ubicacionDialogo.x;
            int y = ubicacionCampo.y - ubicacionDialogo.y + campoNombre.getHeight();
            popupSugerencias.show(this, x, y);
            
        } catch (SQLException e) {
            // Error silencioso - no mostrar al usuario
            popupSugerencias.setVisible(false);
        }
    }
    
    /**
     * Selecciona la sugerencia actual y carga sus datos
     */
    private void seleccionarSugerencia() {
        String seleccionado = listaSugerencias.getSelectedValue();
        if (seleccionado != null) {
            popupSugerencias.setVisible(false);
            cargarDatosProductoSeleccionado(seleccionado);
        }
    }
    
    /**
     * Carga los datos del producto seleccionado en el autocompletado
     */
    private void cargarDatosProductoSeleccionado(String nombreProducto) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            Producto producto = productoDAO.buscarProductoDesactivadoPorNombre(conn, nombreProducto);
            
            if (producto != null) {
                // Rellenar todos los campos con los datos del producto
                campoNombre.setText(producto.getNombre());
                campoDescripcion.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "");
                campoPrecio.setText(producto.getPrecio() != null ? String.valueOf(producto.getPrecio()) : "0.00");
                // Productos desactivados siempre muestran stock 0
                campoStock.setText("0");
                
                // Fecha de vencimiento
                if (producto.getFechaVencimiento() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    campoFechaVencimiento.setText(sdf.format(
                        java.sql.Date.valueOf(producto.getFechaVencimiento())));
                } else {
                    campoFechaVencimiento.setText("");
                }
                
                if (popupSugerencias != null) {
                    popupSugerencias.setVisible(false);
                }
            }
        } catch (SQLException e) {
            // Error silencioso
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }

    private static class ProductoFormData {
        private final String nombre;
        private final String descripcion;
        private final double precio;
        private final int stock;
        private final Date fechaVencimiento;

        ProductoFormData(String nombre, String descripcion, double precio, int stock,
                         Date fechaVencimiento) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.stock = stock;
            this.fechaVencimiento = fechaVencimiento;
        }

        public String nombre() { return nombre; }
        public String descripcion() { return descripcion; }
        public double precio() { return precio; }
        public int stock() { return stock; }
        public Date fechaVencimiento() { return fechaVencimiento; }
    }
}
