package com.minimarket.ui.panels;

import com.minimarket.model.DetalleVenta;
import com.minimarket.model.Producto;
import com.minimarket.ui.handlers.VentaHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panel de ventas y facturación - SOLO UI
 * Toda la lógica de negocio está delegada a VentaHandler
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
    
    // Handler que contiene toda la lógica
    private final VentaHandler ventaHandler;
    
    public VentasPanel() {
        this.ventaHandler = new VentaHandler();
        
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(buildMainPanel(), BorderLayout.CENTER);
        cargarProductos();
        actualizarVistaCarrito();
    }
    
    /* ========================== UI BUILDERS ========================== */
    
    private JPanel buildMainPanel() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(crearPanelCliente(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelCentral(), BorderLayout.CENTER);
        panelPrincipal.add(crearPanelBotones(), BorderLayout.SOUTH);
        return panelPrincipal;
    }
    
    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente / Comprobante"));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Tipo de Comprobante
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.15; gbc.fill = GridBagConstraints.HORIZONTAL;
        comboTipoComprobante = new JComboBox<>(new String[]{"BOLETA", "FACTURA"});
        comboTipoComprobante.setSelectedIndex(0); // Por defecto BOLETA (venta rápida)
        comboTipoComprobante.addActionListener(e -> actualizarCamposCliente());
        panel.add(comboTipoComprobante, gbc);
        
        // Cliente/Razón Social
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Cliente / Razón Social:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoCliente = new JTextField();
        campoCliente.setEnabled(false); // Deshabilitado por defecto (BOLETA)
        panel.add(campoCliente, gbc);
        
        // DNI/RUC
        gbc.gridx = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("DNI / RUC:"), gbc);
        gbc.gridx = 5; gbc.weightx = 0.25; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoDNI = new JTextField();
        campoDNI.setEnabled(false); // Deshabilitado por defecto (BOLETA)
        panel.add(campoDNI, gbc);
        
        return panel;
    }
    
    private void actualizarCamposCliente() {
        boolean esFactura = "FACTURA".equals(comboTipoComprobante.getSelectedItem());
        campoCliente.setEnabled(esFactura);
        campoDNI.setEnabled(esFactura);
        
        if (!esFactura) {
            // Si cambia a BOLETA, limpiar campos (se usarán valores por defecto)
            campoCliente.setText("");
            campoDNI.setText("");
        }
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.add(crearPanelCatalogo());
        panel.add(crearPanelCarrito());
        return panel;
    }
    
    private JPanel crearPanelCatalogo() {
        JPanel panelCatalogo = new JPanel(new BorderLayout());
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("Catálogo de Productos"));
        
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
        UIUtils.configurarTabla(tablaProductos);
        
        btnAgregarCarrito = new JButton("Agregar al Carrito");
        EstilosApp.estilizarBoton(btnAgregarCarrito);
        btnAgregarCarrito.addActionListener(e -> agregarAlCarrito());
        
        JPanel panelBotonAgregar = UIUtils.crearPanelBotones(FlowLayout.CENTER);
        panelBotonAgregar.add(btnAgregarCarrito);
        
        panelCatalogo.add(panelBusqueda, BorderLayout.NORTH);
        panelCatalogo.add(UIUtils.configurarScrollPane(tablaProductos), BorderLayout.CENTER);
        panelCatalogo.add(panelBotonAgregar, BorderLayout.SOUTH);
        return panelCatalogo;
    }
    
    private JPanel crearPanelCarrito() {
        JPanel panelCarrito = new JPanel(new BorderLayout());
        panelCarrito.setBorder(BorderFactory.createTitledBorder("Carrito de Compras"));
        
        String[] columnasCarrito = {"ID", "Producto", "Cant.", "Precio U.", "Subtotal"};
        modeloCarrito = new DefaultTableModel(columnasCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.setRowHeight(25);
        UIUtils.configurarTabla(tablaCarrito);
        
        // Carrito sin decoración de colores - fondo blanco simple
        tablaCarrito.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    // Fondo blanco para todas las filas sin seleccionar
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
        tablaCarrito.getModel().addTableModelListener(e -> manejarCambioCantidad(e.getColumn(), e.getFirstRow()));
        
        JPanel panelTotalCarrito = new JPanel(new BorderLayout());
        panelTotalCarrito.setBackground(Color.WHITE);
        
        labelTotal = new JLabel("TOTAL: S/0.00", JLabel.RIGHT);
        labelTotal.setFont(UIUtils.HEADER_FONT);
        labelTotal.setForeground(EstilosApp.COLOR_PRIMARIO);
        
        JPanel panelBotonesCarrito = UIUtils.crearPanelBotones(FlowLayout.CENTER);
        btnQuitar = new JButton("Quitar");
        btnLimpiar = new JButton("Anular Venta");
        EstilosApp.estilizarBotonNeutro(btnQuitar);
        EstilosApp.estilizarBotonNeutro(btnLimpiar);
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        btnLimpiar.addActionListener(e -> limpiarCarrito());
        panelBotonesCarrito.add(btnQuitar);
        panelBotonesCarrito.add(btnLimpiar);
        
        panelTotalCarrito.add(labelTotal, BorderLayout.NORTH);
        panelTotalCarrito.add(panelBotonesCarrito, BorderLayout.SOUTH);
        
        panelCarrito.add(UIUtils.configurarScrollPane(tablaCarrito), BorderLayout.CENTER);
        panelCarrito.add(panelTotalCarrito, BorderLayout.SOUTH);
        return panelCarrito;
    }
    
    private void manejarCambioCantidad(int columna, int fila) {
        if (columna != 2 || fila < 0) {
            return;
        }
        try {
            int cantidad = Integer.parseInt(modeloCarrito.getValueAt(fila, 2).toString());
            if (cantidad <= 0) {
                modeloCarrito.setValueAt(1, fila, 2);
                cantidad = 1;
            }
            Long productoId = (Long) modeloCarrito.getValueAt(fila, 0);
            try {
                ventaHandler.actualizarCantidadCarrito(productoId, cantidad);
                actualizarVistaCarrito();
            } catch (IllegalStateException e) {
                UIUtils.mostrarError(this, e.getMessage());
                actualizarVistaCarrito(); // Revertir a estado anterior
            }
        } catch (NumberFormatException ex) {
            modeloCarrito.setValueAt(1, fila, 2);
        }
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = UIUtils.crearPanelBotones(FlowLayout.RIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnRegistrarVenta = new JButton("Registrar Venta / Emitir Comprobante");
        EstilosApp.estilizarBoton(btnRegistrarVenta);
        btnRegistrarVenta.setPreferredSize(new Dimension(280, 40));
        btnRegistrarVenta.addActionListener(e -> registrarVenta());
        
        panel.add(btnRegistrarVenta);
        
        return panel;
    }
    
    /* ========================== DELEGACIÓN A HANDLER ========================== */
    
    private void cargarProductos() {
        try {
            String filtro = campoBusquedaProductos != null ? campoBusquedaProductos.getText().trim() : null;
            if (filtro != null && filtro.isEmpty()) {
                filtro = null;
            }
            
            List<Producto> productos = ventaHandler.cargarProductos(filtro);
            modeloProductos.setRowCount(0);
            
            for (Producto producto : productos) {
                BigDecimal precio = producto.getPrecio() != null ? producto.getPrecio() : BigDecimal.ZERO;
                Object[] fila = {
                    producto.getId(),
                    producto.getNombre(),
                    "S/" + String.format("%.2f", precio),
                    producto.getStock()
                };
                modeloProductos.addRow(fila);
            }
        } catch (Exception e) {
            UIUtils.mostrarError(this, "Error al cargar productos: " + e.getMessage());
        }
    }
    
    private void buscarProductos() {
        cargarProductos();
    }
    
    private void agregarAlCarrito() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            UIUtils.mostrarError(this, "Por favor, selecciona un producto para agregar al carrito.");
            return;
        }
        
        Long productoId = (Long) modeloProductos.getValueAt(filaSeleccionada, 0);
        String nombreProducto = (String) modeloProductos.getValueAt(filaSeleccionada, 1);
        int stockDisponible = (Integer) modeloProductos.getValueAt(filaSeleccionada, 3);
        
        Producto producto = ventaHandler.obtenerProducto(productoId);
        if (producto == null) {
            UIUtils.mostrarError(this, "No se encontró la información completa del producto seleccionado.");
            return;
        }
        
        // Calcular stock disponible considerando lo que ya está en el carrito
        int cantidadEnCarrito = obtenerCantidadEnCarrito(productoId);
        int stockRestante = stockDisponible - cantidadEnCarrito;
        
        if (stockRestante <= 0) {
            UIUtils.mostrarError(this, "No hay más stock disponible para este producto.");
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
            
            try {
                ventaHandler.agregarAlCarrito(producto, cantidadSeleccionada);
                actualizarVistaCarrito();
            } catch (IllegalStateException e) {
                UIUtils.mostrarError(this, e.getMessage());
            }
        }
    }
    
    private void quitarDelCarrito() {
        int filaSeleccionada = tablaCarrito.getSelectedRow();
        if (filaSeleccionada == -1) {
            UIUtils.mostrarError(this, "Por favor, selecciona un producto para quitar del carrito.");
            return;
        }
        
        Long productoId = (Long) modeloCarrito.getValueAt(filaSeleccionada, 0);
        ventaHandler.quitarDelCarrito(productoId);
        actualizarVistaCarrito();
    }
    
    private void limpiarCarrito() {
        ventaHandler.ejecutarAnular(this);
        actualizarVistaCarrito();
    }
    
    private void registrarVenta() {
        if (ventaHandler.getCarrito().isEmpty()) {
            UIUtils.mostrarError(this, "El carrito está vacío. Agrega productos antes de registrar la venta.");
            return;
        }
        
        MetodoPagoDialog dialog = new MetodoPagoDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            String tipoComprobante = (String) comboTipoComprobante.getSelectedItem();
            String nombreCliente = campoCliente.getText().trim();
            String documentoCliente = campoDNI.getText().trim();
            String metodoPago = dialog.getMetodoPagoSeleccionado();
            String observaciones = "";
            
            try {
                ventaHandler.registrarVenta(tipoComprobante, nombreCliente, documentoCliente, metodoPago, observaciones, this);
                actualizarDashboardSiExiste();
                limpiarFormulario();
            } catch (RuntimeException e) {
                // El error ya fue mostrado por el handler
            }
        }
    }
    
    private void actualizarVistaCarrito() {
        modeloCarrito.setRowCount(0);
        List<DetalleVenta> carrito = ventaHandler.getCarrito();
        
        for (DetalleVenta detalle : carrito) {
            Producto producto = detalle.getProducto();
            if (producto == null) continue;
            
            BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal subtotal = detalle.getSubtotal() != null ? detalle.getSubtotal() : BigDecimal.ZERO;
            
            Object[] fila = {
                producto.getId(),
                producto.getNombre(),
                detalle.getCantidad(),
                precio.doubleValue(),
                subtotal.doubleValue()
            };
            modeloCarrito.addRow(fila);
        }
        
        BigDecimal total = ventaHandler.getTotalVenta();
        labelTotal.setText("TOTAL: S/" + String.format("%.2f", total.doubleValue()));
    }
    
    private int obtenerCantidadEnCarrito(Long productoId) {
        return ventaHandler.getCarrito().stream()
            .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(productoId))
            .mapToInt(DetalleVenta::getCantidad)
            .sum();
    }
    
    private void limpiarFormulario() {
        comboTipoComprobante.setSelectedIndex(0); // Resetear a BOLETA
        campoCliente.setText("");
        campoDNI.setText("");
        campoBusquedaProductos.setText("");
        actualizarCamposCliente(); // Asegurar que los campos estén deshabilitados
        limpiarCarrito();
        cargarProductos();
    }
    
    /**
     * Actualiza el dashboard si existe, buscando el DashboardFrame padre
     */
    private void actualizarDashboardSiExiste() {
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof com.minimarket.ui.swing.DashboardFrame) {
            com.minimarket.ui.swing.DashboardFrame dashboard = (com.minimarket.ui.swing.DashboardFrame) window;
            dashboard.actualizarDashboard();
        }
    }
}
