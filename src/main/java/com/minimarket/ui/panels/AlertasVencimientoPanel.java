package com.minimarket.ui.panels;

import com.minimarket.ui.handlers.AlertasVencimientoHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Panel para mostrar alertas de productos próximos a vencer o ya vencidos - SOLO UI
 * Toda la lógica está delegada a AlertasVencimientoHandler
 */
public class AlertasVencimientoPanel extends JPanel {
    
    private static final Color COLOR_VENCIDO = new Color(244, 67, 54, 100);
    private static final Color COLOR_POR_VENCER = new Color(255, 193, 7, 100);
    private static final Color COLOR_NORMAL = Color.WHITE;
    
    private JTable tablaAlertas;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalProductos;
    private final boolean soloLectura;
    
    // Handler que contiene toda la lógica
    private final AlertasVencimientoHandler handler;
    
    public AlertasVencimientoPanel() {
        this(false); // Por defecto, no es solo lectura
    }
    
    public AlertasVencimientoPanel(boolean soloLectura) {
        this.soloLectura = soloLectura;
        this.handler = new AlertasVencimientoHandler();
        inicializarUI();
        cargarAlertas();
    }
    
    private void inicializarUI() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTablaAlertas(), BorderLayout.CENTER);
        add(crearPiePagina(), BorderLayout.SOUTH);
    }
    
    private JPanel crearEncabezado() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titulo = new JLabel("ALERTAS DE VENCIMIENTO");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setForeground(new Color(44, 62, 80));
        
        panelSuperior.add(titulo, BorderLayout.WEST);
        panelSuperior.add(crearPanelBotones(), BorderLayout.EAST);
        return panelSuperior;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(Color.WHITE);
        
        if (soloLectura) {
            JLabel lblModoLectura = new JLabel("📖 Modo Consulta - Solo lectura");
            lblModoLectura.setFont(UIUtils.BOLD_FONT);
            lblModoLectura.setForeground(new Color(108, 117, 125));
            lblModoLectura.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            
            JButton btnActualizar = new JButton("🔄 Actualizar");
            EstilosApp.estilizarBotonNeutro(btnActualizar);
            btnActualizar.addActionListener(e -> cargarAlertas());
            
            panelBotones.add(lblModoLectura);
            panelBotones.add(btnActualizar);
        } else {
            JButton btnRetirarSeleccionado = new JButton("Retirar Seleccionado");
            EstilosApp.estilizarBotonError(btnRetirarSeleccionado);
            btnRetirarSeleccionado.addActionListener(e -> retirarProductoSeleccionado());
            
            JButton btnRetirarVencidos = new JButton("Retirar Todos los Vencidos");
            EstilosApp.estilizarBotonError(btnRetirarVencidos);
            btnRetirarVencidos.addActionListener(e -> retirarProductosVencidos());
            
            JButton btnActualizar = new JButton("Actualizar");
            EstilosApp.estilizarBotonNeutro(btnActualizar);
            btnActualizar.addActionListener(e -> cargarAlertas());
            
            panelBotones.add(btnRetirarSeleccionado);
            panelBotones.add(btnRetirarVencidos);
            panelBotones.add(btnActualizar);
        }
        return panelBotones;
    }
    
    private JScrollPane crearTablaAlertas() {
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
        UIUtils.configurarTabla(tablaAlertas);
        tablaAlertas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaAlertas.getColumnModel().getColumn(1).setPreferredWidth(300);
        tablaAlertas.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaAlertas.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaAlertas.getColumnModel().getColumn(4).setPreferredWidth(120);
        tablaAlertas.getColumnModel().getColumn(5).setPreferredWidth(150);
        tablaAlertas.setDefaultRenderer(Object.class, new AlertasTableCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tablaAlertas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos con Alertas de Vencimiento"));
        return scrollPane;
    }
    
    private JPanel crearPiePagina() {
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        lblTotalProductos = new JLabel("Total de alertas: 0");
        lblTotalProductos.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalProductos.setForeground(new Color(108, 117, 125));
        panelInferior.add(lblTotalProductos, BorderLayout.WEST);
        panelInferior.add(crearLeyendaColores(), BorderLayout.EAST);
        return panelInferior;
    }
    
    private JPanel crearLeyendaColores() {
        JPanel panelLeyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelLeyenda.setBackground(Color.WHITE);
        
        JLabel leyendaVencido = new JLabel("Vencido");
        leyendaVencido.setForeground(new Color(244, 67, 54));
        leyendaVencido.setFont(new Font("Arial", Font.BOLD, 11));
        
        JLabel leyendaPorVencer = new JLabel("Por vencer");
        leyendaPorVencer.setForeground(new Color(255, 193, 7));
        leyendaPorVencer.setFont(new Font("Arial", Font.BOLD, 11));
        
        panelLeyenda.add(leyendaVencido);
        panelLeyenda.add(Box.createHorizontalStrut(15));
        panelLeyenda.add(leyendaPorVencer);
        return panelLeyenda;
    }
    
    private void cargarAlertas() {
        // Limpiar modelo antes de cargar nuevos datos
        modeloTabla.setRowCount(0);
        
        try {
            // Forzar actualización de la conexión para obtener datos frescos
            var alertas = handler.cargarAlertas();
            
            for (AlertasVencimientoHandler.ProductoAlerta alerta : alertas) {
                Object[] fila = {
                    alerta.id,
                    alerta.nombre,
                    alerta.stock,
                    alerta.fechaVencimiento,
                    alerta.diasTexto,
                    alerta.estado
                };
                modeloTabla.addRow(fila);
            }
            
            lblTotalProductos.setText("Total de alertas: " + alertas.size());
            
            // Forzar repintado de la tabla
            tablaAlertas.revalidate();
            tablaAlertas.repaint();
            
        } catch (RuntimeException e) {
            UIUtils.mostrarError(this, e.getMessage());
        }
    }
    
    private void retirarProductoSeleccionado() {
        int filaSeleccionada = tablaAlertas.getSelectedRow();
        if (filaSeleccionada == -1) {
            UIUtils.mostrarError(this, "Por favor, seleccione un producto de la tabla para retirar.");
            return;
        }
        
        Long productoId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombreProducto = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        
        if (UIUtils.confirmar(this, 
            "¿Está seguro de que desea retirar el producto:\n" +
            nombreProducto + "?\n\n" +
            "Esta acción marcará el producto como inactivo.")) {
            try {
                handler.retirarProductoPorId(this, productoId);
                cargarAlertas();
            } catch (RuntimeException e) {
                // El error ya fue mostrado por el handler
            }
        }
    }
    
    private void retirarProductosVencidos() {
        if (UIUtils.confirmar(this, 
            "¿Está seguro de que desea marcar como inactivos todos los productos vencidos?\n" +
            "Esta acción no se puede deshacer.")) {
            try {
                handler.retirarProductosVencidos(this);
                cargarAlertas();
            } catch (RuntimeException e) {
                // El error ya fue mostrado por el handler
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
                
                if (estado.contains("VENCIDO")) {
                    component.setBackground(COLOR_VENCIDO);
                } else if (estado.contains("POR VENCER") || estado.contains("PRÓXIMO")) {
                    component.setBackground(COLOR_POR_VENCER);
                } else {
                    component.setBackground(COLOR_NORMAL);
                }
            }
            
            return component;
        }
    }
}
