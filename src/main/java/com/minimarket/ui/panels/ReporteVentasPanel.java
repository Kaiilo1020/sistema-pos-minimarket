package com.minimarket.ui.panels;

import com.minimarket.ui.handlers.ReporteVentaHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel de Reporte de Ventas - SOLO UI
 * Toda la lógica está delegada a ReporteVentaHandler
 */
public class ReporteVentasPanel extends JPanel {
    
    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizarReporte;
    private JLabel labelTitulo;
    private JLabel labelUltimaActualizacion;
    private JLabel labelTotalTransacciones;
    private JLabel labelTotalProductos;
    private JLabel labelIngresosTotales;
    
    // Handler que contiene toda la lógica
    private final ReporteVentaHandler handler;
    
    public ReporteVentasPanel() {
        this.handler = new ReporteVentaHandler();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(crearPanelEncabezado(), BorderLayout.NORTH);
        add(crearPanelResumen(), BorderLayout.CENTER);
        add(crearPanelTotales(), BorderLayout.SOUTH);
        cargarReporteVentas();
    }
    
    private JPanel crearPanelEncabezado() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(Color.WHITE);
        labelTitulo = new JLabel("REPORTE DE VENTAS DEL DÍA", JLabel.CENTER);
        labelTitulo.setFont(UIUtils.HEADER_FONT);
        labelTitulo.setForeground(Color.DARK_GRAY);
        labelUltimaActualizacion = new JLabel("", JLabel.CENTER);
        labelUltimaActualizacion.setFont(UIUtils.DEFAULT_FONT);
        labelUltimaActualizacion.setForeground(Color.GRAY);
        panelTitulo.add(labelTitulo, BorderLayout.NORTH);
        panelTitulo.add(labelUltimaActualizacion, BorderLayout.SOUTH);
        
        JPanel panelBoton = UIUtils.crearPanelBotones(FlowLayout.RIGHT);
        btnActualizarReporte = new JButton("Actualizar Reporte");
        EstilosApp.estilizarBotonSecundario(btnActualizarReporte);
        btnActualizarReporte.setPreferredSize(new Dimension(160, 40));
        btnActualizarReporte.addActionListener(e -> cargarReporteVentas());
        panelBoton.add(btnActualizarReporte);
        
        panelSuperior.add(panelTitulo, BorderLayout.CENTER);
        panelSuperior.add(panelBoton, BorderLayout.EAST);
        return panelSuperior;
    }
    
    private JPanel crearPanelResumen() {
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        JLabel labelSeccion = new JLabel("Resumen por Trabajador");
        labelSeccion.setFont(UIUtils.HEADER_FONT);
        labelSeccion.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        String[] columnas = {"Trabajador", "Transacciones", "Productos Vendidos", "Total Recaudado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaReporte.setRowHeight(30);
        UIUtils.configurarTablaConAlineacionNumerica(tablaReporte, 1);
        tablaReporte.getColumnModel().getColumn(0).setPreferredWidth(200);
        tablaReporte.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaReporte.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaReporte.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        panelCentral.add(labelSeccion, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);
        return panelCentral;
    }
    
    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Título de la sección
        JLabel labelTituloTotales = new JLabel("TOTALES DEL DÍA");
        labelTituloTotales.setFont(UIUtils.HEADER_FONT);
        labelTituloTotales.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel con los totales
        JPanel panelMetricas = new JPanel(new GridLayout(1, 3, 20, 0));
        panelMetricas.setBackground(Color.WHITE);
        panelMetricas.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        panelMetricas.setPreferredSize(new Dimension(0, 80));
        
        // Total Transacciones
        JPanel panelTransacciones = crearPanelMetrica("Total Transacciones:", "0", new Color(25, 118, 210));
        labelTotalTransacciones = (JLabel) ((JPanel) panelTransacciones.getComponent(1)).getComponent(0);
        
        // Productos Vendidos
        JPanel panelProductos = crearPanelMetrica("Productos Vendidos:", "0", new Color(255, 152, 0));
        labelTotalProductos = (JLabel) ((JPanel) panelProductos.getComponent(1)).getComponent(0);
        
        // Ingresos Totales
        JPanel panelIngresos = crearPanelMetrica("Ingresos Totales:", "S/0.00", new Color(76, 175, 80));
        labelIngresosTotales = (JLabel) ((JPanel) panelIngresos.getComponent(1)).getComponent(0);
        
        panelMetricas.add(panelTransacciones);
        panelMetricas.add(panelProductos);
        panelMetricas.add(panelIngresos);
        
        panel.add(labelTituloTotales, BorderLayout.NORTH);
        panel.add(panelMetricas, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelMetrica(String titulo, String valor, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel labelTitulo = new JLabel(titulo, JLabel.CENTER);
        labelTitulo.setFont(UIUtils.DEFAULT_FONT);
        labelTitulo.setForeground(Color.GRAY);
        
        JPanel panelValor = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        panelValor.setBackground(Color.WHITE);
        
        JLabel labelValor = new JLabel(valor, JLabel.CENTER);
        labelValor.setFont(new Font("Arial", Font.BOLD, 20)); // Mantener tamaño especial
        labelValor.setForeground(color);
        
        panelValor.add(labelValor);
        
        panel.add(labelTitulo, BorderLayout.NORTH);
        panel.add(panelValor, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void cargarReporteVentas() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        // Actualizar fecha y hora
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy - HH:mm:ss");
        labelUltimaActualizacion.setText("Última actualización: " + sdf.format(new Date()));
        
        try {
            var reportes = handler.obtenerReportePorTrabajador();
            var totales = handler.calcularTotalesDia(reportes);
            
            // Llenar tabla
            for (ReporteVentaHandler.ReporteTrabajador reporte : reportes) {
                Object[] fila = {
                    reporte.trabajador,
                    String.valueOf(reporte.transacciones),
                    String.valueOf(reporte.productosVendidos),
                    "S/" + String.format("%.2f", reporte.totalRecaudado)
                };
                modeloTabla.addRow(fila);
            }
            
            // Actualizar totales del día
            labelTotalTransacciones.setText(String.valueOf(totales.totalTransacciones));
            labelTotalProductos.setText(String.valueOf(totales.totalProductos));
            labelIngresosTotales.setText("S/" + String.format("%.2f", totales.totalIngresos));
            
            // Cambiar color de los totales según el valor
            if (totales.totalIngresos > 0) {
                labelIngresosTotales.setForeground(EstilosApp.COLOR_PRIMARIO); // Verde
            } else {
                labelIngresosTotales.setForeground(EstilosApp.COLOR_NEUTRO); // Gris
            }
            
        } catch (RuntimeException e) {
            UIUtils.mostrarError(this, e.getMessage());
        }
    }
}
