package com.minimarket.ui.panels;

import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para seleccionar cantidad de productos
 */
public class CantidadDialog extends JDialog {
    
    private JSpinner spinnerCantidad;
    private boolean confirmado = false;
    private int cantidadSeleccionada = 1;
    
    public CantidadDialog(JFrame parent, String nombreProducto, int stockDisponible) {
        super(parent, "Seleccionar Cantidad", true);
        UIUtils.configurarDialogo(this, "Seleccionar Cantidad", 450, 250);
        initializeComponents(nombreProducto, stockDisponible);
    }
    
    private void initializeComponents(String nombreProducto, int stockDisponible) {
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Título del producto (sin HTML para evitar saltos de línea)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelProducto = new JLabel(nombreProducto);
        labelProducto.setFont(UIUtils.HEADER_FONT);
        panelPrincipal.add(labelProducto, gbc);
        
        // Stock disponible
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelStock = new JLabel("Stock disponible: " + stockDisponible + " unidades");
        labelStock.setFont(UIUtils.DEFAULT_FONT);
        labelStock.setForeground(Color.GRAY);
        panelPrincipal.add(labelStock, gbc);
        
        // Cantidad
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setFont(UIUtils.BOLD_FONT);
        panelPrincipal.add(labelCantidad, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, stockDisponible, 1));
        spinnerCantidad.setFont(UIUtils.DEFAULT_FONT);
        spinnerCantidad.setPreferredSize(new Dimension(100, 28));
        ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        panelPrincipal.add(spinnerCantidad, gbc);
        
        // Panel de botones
        JPanel panelBotones = UIUtils.crearPanelBotones(FlowLayout.CENTER);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAgregar = new JButton("Agregar al Carrito");
        
        EstilosApp.estilizarBotonNeutro(btnCancelar);
        EstilosApp.estilizarBoton(btnAgregar);
        btnAgregar.setPreferredSize(new Dimension(150, 35));
        
        btnAgregar.addActionListener(e -> {
            cantidadSeleccionada = (Integer) spinnerCantidad.getValue();
            confirmado = true;
            dispose();
        });
        
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCancelar);
        panelBotones.add(Box.createHorizontalStrut(10));
        panelBotones.add(btnAgregar);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar para que Enter confirme
        getRootPane().setDefaultButton(btnAgregar);
        
        // Seleccionar el texto del spinner
        SwingUtilities.invokeLater(() -> {
            ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().selectAll();
            spinnerCantidad.requestFocus();
        });
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public int getCantidadSeleccionada() {
        return cantidadSeleccionada;
    }
}
