package com.minimarket.ui.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Utilidades para la interfaz de usuario
 * Centraliza la configuración común de componentes Swing
 */
public class UIUtils {
    
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
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
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
                                c.setBackground(new Color(255, 235, 238));
                                c.setForeground(new Color(198, 40, 40));
                            } else if (diasRestantes <= 30) {
                                c.setBackground(new Color(255, 249, 196));
                                c.setForeground(new Color(245, 124, 0));
                            } else if (diasRestantes <= 120) {
                                c.setBackground(new Color(255, 249, 196));
                                c.setForeground(new Color(245, 124, 0));
                            } else {
                                c.setBackground(new Color(232, 245, 233));
                                c.setForeground(new Color(27, 94, 32));
                            }
                        } catch (Exception e) {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.DARK_GRAY);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.DARK_GRAY);
                    }
                } else {
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });
    }
    
    /**
     * Configura una tabla con filas alternadas y alineación numérica
     * @param tabla La tabla a configurar
     * @param primeraColumnaNumerica Índice de la primera columna numérica (las siguientes también serán numéricas)
     */
    public static void configurarTablaConAlineacionNumerica(JTable tabla, int primeraColumnaNumerica) {
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
                
                // Alinear números a la derecha
                if (column >= primeraColumnaNumerica) {
                    setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                
                return c;
            }
        });
    }
    
    /**
     * Configura un panel con layout y padding específicos
     */
    public static JPanel configurarPanel(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        if (padding > 0) {
            panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        }
        return panel;
    }
    
    /**
     * Configura anchos de columnas de tabla de forma flexible
     */
    public static void configurarColumnasTabla(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }
    
    /**
     * Configura un JScrollPane con scrollbars más amigables y modernos
     * - Scrollbars más anchos y suaves
     * - Incremento de scroll personalizado
     * - Sin bordes visibles
     * - Política de scroll mejorada
     */
    public static JScrollPane configurarScrollPane(JComponent componente) {
        JScrollPane scrollPane = new JScrollPane(componente);
        
        // Configurar scrollbars más amigables
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave
        scrollPane.getVerticalScrollBar().setBlockIncrement(64); // Scroll por bloques más grande
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setBlockIncrement(64);
        
        // Hacer scrollbars más anchos y visibles
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        
        // Política de scrollbars
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Sin bordes visibles para un look más limpio
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        
        // Mejorar el renderizado
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        return scrollPane;
    }

}
