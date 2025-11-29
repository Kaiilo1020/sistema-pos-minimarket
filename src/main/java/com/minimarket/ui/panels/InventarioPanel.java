package com.minimarket.ui.panels;

import com.minimarket.model.Producto;
import com.minimarket.ui.handlers.ProductoHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel de gestión de inventario - SOLO UI
 * Toda la lógica de negocio está delegada a ProductoHandler
 */
public class InventarioPanel extends JPanel {
    
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField campoBusqueda;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean soloLectura;
    
    // Handler que contiene toda la lógica
    private final ProductoHandler productoHandler;
    
    public InventarioPanel() {
        this(false);
    }
    
    public InventarioPanel(boolean soloLectura) {
        this.soloLectura = soloLectura;
        this.productoHandler = new ProductoHandler();
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
        
        if (soloLectura) {
            // MODO SOLO LECTURA (Cajeros): Solo botón actualizar y mensaje informativo
            JLabel lblModoLectura = new JLabel("📖 Modo Consulta - Solo lectura");
            lblModoLectura.setFont(UIUtils.BOLD_FONT);
            lblModoLectura.setForeground(new Color(108, 117, 125));
            lblModoLectura.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            
            JButton btnActualizar = new JButton("🔄 Actualizar");
            EstilosApp.estilizarBotonNeutro(btnActualizar);
            btnActualizar.addActionListener(e -> cargarProductos());
            
            panelBotones.add(lblModoLectura);
            panelBotones.add(btnActualizar);
            
        } else {
            // MODO COMPLETO (Supervisor/Admin): Todos los botones
            JButton btnAgregar = new JButton("Agregar Producto");
            JButton btnEditar = new JButton("Editar Producto");
            JButton btnEliminar = new JButton("Eliminar Producto");
            JButton btnActualizar = new JButton("Actualizar");
            
            EstilosApp.estilizarBoton(btnAgregar);
            EstilosApp.estilizarBotonSecundario(btnEditar);
            EstilosApp.estilizarBotonError(btnEliminar);
            EstilosApp.estilizarBotonNeutro(btnActualizar);
            
            // Eventos de botones - Delegación al handler
            btnAgregar.addActionListener(e -> abrirDialogoAgregarProducto());
            btnEditar.addActionListener(e -> editarProductoSeleccionado());
            btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
            btnActualizar.addActionListener(e -> cargarProductos());
            
            panelBotones.add(btnAgregar);
            panelBotones.add(btnEditar);
            panelBotones.add(btnEliminar);
            panelBotones.add(btnActualizar);
        }
        
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelBotones, BorderLayout.EAST);
        
        // Tabla de productos
        String[] columnas = {"ID", "Nombre", "Descripción", "Precio", "Stock", "Fecha Vencimiento"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setRowHeight(25);
        UIUtils.configurarTablaConVencimiento(tablaProductos, 5);
        
        // Configurar filtro
        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Productos"));
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /* ========================== DELEGACIÓN A HANDLER ========================== */
    
    private void cargarProductos() {
        try {
            List<Producto> productos = productoHandler.cargarProductos();
            modeloTabla.setRowCount(0);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Producto producto : productos) {
                Object[] fila = {
                    producto.getId(),
                    producto.getNombre(),
                    producto.getDescripcion(),
                    "S/" + String.format("%.2f", producto.getPrecio() != null ? producto.getPrecio().doubleValue() : 0.0),
                    producto.getStock(),
                    producto.getFechaVencimiento() != null ? 
                        sdf.format(java.sql.Date.valueOf(producto.getFechaVencimiento())) : "N/A"
                };
                modeloTabla.addRow(fila);
            }
        } catch (Exception e) {
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
            cargarProductos();
        }
    }
    
    private void editarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            UIUtils.mostrarError(this, "Por favor, selecciona un producto para editar.");
            return;
        }
        
        Long productoId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
        
        ProductoDialog dialog = new ProductoDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                   "Editar Producto", false, productoId);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            cargarProductos();
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
            try {
                productoHandler.eliminarProducto(productoId, this);
                cargarProductos();
            } catch (RuntimeException e) {
                // El error ya fue mostrado por el handler
            }
        }
    }
}
