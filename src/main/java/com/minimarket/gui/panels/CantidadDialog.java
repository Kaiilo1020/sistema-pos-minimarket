package com.minimarket.gui.panels;

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
        initializeComponents(nombreProducto, stockDisponible);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents(String nombreProducto, int stockDisponible) {
        setLayout(new BorderLayout());
        setSize(350, 180);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Título del producto
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelProducto = new JLabel("<html><b>" + nombreProducto + "</b></html>");
        labelProducto.setFont(new Font("Arial", Font.PLAIN, 14));
        panelPrincipal.add(labelProducto, gbc);
        
        // Stock disponible
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JLabel labelStock = new JLabel("Stock disponible: " + stockDisponible + " unidades");
        labelStock.setFont(new Font("Arial", Font.PLAIN, 12));
        labelStock.setForeground(new Color(100, 100, 100));
        panelPrincipal.add(labelStock, gbc);
        
        // Cantidad
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setFont(new Font("Arial", Font.BOLD, 12));
        panelPrincipal.add(labelCantidad, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, stockDisponible, 1));
        spinnerCantidad.setFont(new Font("Arial", Font.PLAIN, 12));
        ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        panelPrincipal.add(spinnerCantidad, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAgregar = new JButton("Agregar al Carrito");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAgregar.setBackground(new Color(46, 125, 50));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 11));
        
        btnCancelar.setBackground(new Color(158, 158, 158));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 11));
        
        btnAgregar.addActionListener(e -> {
            cantidadSeleccionada = (Integer) spinnerCantidad.getValue();
            confirmado = true;
            dispose();
        });
        
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCancelar);
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
