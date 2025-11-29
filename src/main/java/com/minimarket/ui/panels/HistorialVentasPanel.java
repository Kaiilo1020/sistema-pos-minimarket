package com.minimarket.ui.panels;

import com.minimarket.dao.VentaDAO;
import com.minimarket.ui.handlers.HistorialVentaHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de Historial de Ventas - SOLO UI
 * Toda la lógica está delegada a HistorialVentaHandler
 */
public class HistorialVentasPanel extends JPanel {
    
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar;
    private JLabel labelInfo;
    
    // Handler que contiene toda la lógica
    private final HistorialVentaHandler handler;
    
    public HistorialVentasPanel() {
        this.handler = new HistorialVentaHandler();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearTablaHistorial(), BorderLayout.CENTER);
        add(crearPanelInformativo(), BorderLayout.SOUTH);
        cargarHistorialVentas();
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBoton.setBackground(Color.WHITE);
        
        btnActualizar = new JButton("Actualizar");
        EstilosApp.estilizarBotonNeutro(btnActualizar);
        btnActualizar.addActionListener(e -> cargarHistorialVentas());
        
        panelBoton.add(btnActualizar);
        panelSuperior.add(panelBoton, BorderLayout.WEST);
        return panelSuperior;
    }
    
    private JScrollPane crearTablaHistorial() {
        String[] columnas = {"ID", "Producto ID", "Usuario ID", "Cantidad", "Precio Unit.", "Total", "Fecha Venta"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHistorial.setRowHeight(25);
        UIUtils.configurarTablaConFilasAlternadas(tablaHistorial);
        tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaHistorial.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(3).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaHistorial.getColumnModel().getColumn(5).setPreferredWidth(100);
        tablaHistorial.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Historial de Ventas"));
        return scrollPane;
    }
    
    private JPanel crearPanelInformativo() {
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        labelInfo = new JLabel("<html><b>Nota:</b> Este historial muestra todas las ventas realizadas en el negocio.<br>" +
            "Las ventas se ordenan por fecha (más recientes primero).</html>");
        labelInfo.setFont(UIUtils.DEFAULT_FONT);
        labelInfo.setForeground(Color.GRAY);
        panelInferior.add(labelInfo);
        return panelInferior;
    }
    
    private void cargarHistorialVentas() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        try {
            var historial = handler.cargarHistorial();
            
            for (VentaDAO.HistorialVenta item : historial) {
                Object[] fila = {
                    item.id,
                    item.productoId,
                    item.usuarioId,
                    item.cantidad,
                    "S/" + String.format("%.2f", item.precioUnitario),
                    "S/" + String.format("%.2f", item.total),
                    handler.formatearFecha(item.fechaHora)
                };
                modeloTabla.addRow(fila);
            }
            
            // Actualizar información
            int totalRegistros = modeloTabla.getRowCount();
            labelInfo.setText("<html><b>Nota:</b> Este historial muestra todas las ventas realizadas en el negocio (" + 
                             totalRegistros + " registros).<br>" +
                             "Las ventas se ordenan por fecha (más recientes primero).</html>");
            
        } catch (RuntimeException e) {
            UIUtils.mostrarError(this, e.getMessage());
        }
    }
}
