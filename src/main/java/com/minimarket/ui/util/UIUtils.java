package com.minimarket.ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Utilidades para la interfaz de usuario
 * Centraliza la configuración común de componentes Swing
 */
public class UIUtils {
    
    // Colores estándar del sistema
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    public static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    public static final Color DANGER_COLOR = new Color(211, 47, 47);
    public static final Color SECONDARY_COLOR = new Color(117, 117, 117);
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    
    // Fuentes estándar
    public static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    
    /**
     * Configura un panel con el estilo estándar del sistema
     */
    public static void configurarPanel(JPanel panel) {
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    }
    
    /**
     * Configura un botón con el estilo estándar
     */
    public static void configurarBoton(JButton boton, Color backgroundColor) {
        boton.setBackground(backgroundColor);
        boton.setForeground(Color.WHITE);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setFont(BOLD_FONT);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(120, 35));
    }
    
    /**
     * Configura un botón primario (azul)
     */
    public static void configurarBotonPrimario(JButton boton) {
        configurarBoton(boton, PRIMARY_COLOR);
    }
    
    /**
     * Configura un botón de éxito (verde)
     */
    public static void configurarBotonExito(JButton boton) {
        configurarBoton(boton, SUCCESS_COLOR);
    }
    
    /**
     * Configura un botón de peligro (rojo)
     */
    public static void configurarBotonPeligro(JButton boton) {
        configurarBoton(boton, DANGER_COLOR);
    }
    
    /**
     * Configura un botón secundario (gris)
     */
    public static void configurarBotonSecundario(JButton boton) {
        configurarBoton(boton, SECONDARY_COLOR);
    }
    
    /**
     * Configura una tabla con el estilo estándar
     */
    public static void configurarTabla(JTable tabla) {
        tabla.getTableHeader().setBackground(Color.LIGHT_GRAY);
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.getTableHeader().setFont(BOLD_FONT);
        tabla.setRowHeight(25);
        tabla.setFont(DEFAULT_FONT);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    /**
     * Crea un panel de botones con FlowLayout
     */
    public static JPanel crearPanelBotones(int alignment) {
        JPanel panel = new JPanel(new FlowLayout(alignment));
        panel.setBackground(BACKGROUND_COLOR);
        return panel;
    }
    
    /**
     * Crea un panel de búsqueda estándar
     */
    public static JPanel crearPanelBusqueda(JTextField campoBusqueda) {
        JPanel panel = crearPanelBotones(FlowLayout.LEFT);
        panel.add(new JLabel("Buscar:"));
        campoBusqueda.setPreferredSize(new Dimension(200, 25));
        panel.add(campoBusqueda);
        return panel;
    }
    
    /**
     * Configura un diálogo con el estilo estándar
     */
    public static void configurarDialogo(JDialog dialogo, String titulo, int width, int height) {
        dialogo.setTitle(titulo);
        dialogo.setSize(width, height);
        dialogo.setLocationRelativeTo(null);
        dialogo.setModal(true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Muestra un mensaje de error estándar
     */
    public static void mostrarError(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Muestra un mensaje de éxito estándar
     */
    public static void mostrarExito(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Muestra un diálogo de confirmación estándar
     */
    public static boolean confirmar(Component parent, String mensaje) {
        return JOptionPane.showConfirmDialog(parent, mensaje, "Confirmar", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    /**
     * Configura una tabla con colores alternados en las filas
     */
    public static void configurarTablaConFilasAlternadas(JTable tabla) {
        configurarTabla(tabla);
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 249, 250));
                    }
                }
                return c;
            }
        });
    }
    
    /**
     * Configura una tabla con colores basados en fechas de vencimiento
     * @param tabla La tabla a configurar
     * @param columnaFecha Índice de la columna que contiene la fecha de vencimiento
     */
    public static void configurarTablaConVencimiento(JTable tabla, int columnaFecha) {
        configurarTabla(tabla);
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            private final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String fechaVenc = (String) table.getValueAt(row, columnaFecha);
                    if (fechaVenc != null && !fechaVenc.equals("N/A")) {
                        try {
                            java.util.Date fechaVencimiento = sdf.parse(fechaVenc);
                            java.util.Date hoy = new java.util.Date();
                            long diasRestantes = (fechaVencimiento.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24);
                            
                            if (diasRestantes < 0) {
                                c.setBackground(new Color(255, 205, 210)); // Rojo claro - vencido
                            } else if (diasRestantes <= 7) {
                                c.setBackground(new Color(255, 243, 224)); // Naranja claro - próximo a vencer
                            } else {
                                c.setBackground(new Color(232, 245, 233)); // Verde claro - normal
                            }
                        } catch (Exception e) {
                            c.setBackground(Color.WHITE);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
    }
}
