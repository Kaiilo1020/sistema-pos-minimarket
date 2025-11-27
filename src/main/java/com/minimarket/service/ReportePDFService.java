package com.minimarket.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import com.minimarket.config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * Servicio para generar reportes PDF de ventas
 */
public class ReportePDFService {
    
    public static class VentaDelDia {
        public String numeroVenta;
        public String fechaHora;
        public String cajera;
        public String metodoPago;
        public double total;
        public List<DetalleVenta> detalles;
        
        public VentaDelDia() {
            this.detalles = new ArrayList<>();
        }
    }
    
    public static class DetalleVenta {
        public String producto;
        public int cantidad;
        public double precioUnitario;
        public double subtotal;
    }
    
    public static class ResumenVentas {
        public List<VentaDelDia> ventas;
        public int totalTransacciones;
        public int totalProductos;
        public double totalIngresos;
        public String metodoPagoMasUsado;
        
        public ResumenVentas() {
            this.ventas = new ArrayList<>();
        }
    }
    
    /**
     * Genera un reporte PDF de las ventas del día
     */
    public static String generarReporteVentasDelDia() {
        try {
            // Obtener datos de ventas
            ResumenVentas resumen = obtenerVentasDelDia();
            
            // Crear archivo PDF
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreArchivo = "reporte_ventas_" + fechaHoy + ".pdf";
            String rutaArchivo = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + nombreArchivo;
            
            // Crear el PDF
            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Fuentes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            // Título
            Paragraph titulo = new Paragraph("REPORTE DE VENTAS DEL DÍA")
                .setFont(fontBold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
            document.add(titulo);
            
            // Fecha
            Paragraph fecha = new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(fontRegular)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(fecha);
            
            // Resumen general
            document.add(new Paragraph("RESUMEN GENERAL")
                .setFont(fontBold)
                .setFontSize(14)
                .setMarginBottom(10));
            
            Table resumenTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .setWidth(UnitValue.createPercentValue(100));
            
            resumenTable.addCell(new Cell().add(new Paragraph("Total de Transacciones:").setFont(fontRegular)));
            resumenTable.addCell(new Cell().add(new Paragraph(String.valueOf(resumen.totalTransacciones)).setFont(fontBold)));
            
            resumenTable.addCell(new Cell().add(new Paragraph("Total de Productos Vendidos:").setFont(fontRegular)));
            resumenTable.addCell(new Cell().add(new Paragraph(String.valueOf(resumen.totalProductos)).setFont(fontBold)));
            
            resumenTable.addCell(new Cell().add(new Paragraph("Ingresos Totales:").setFont(fontRegular)));
            resumenTable.addCell(new Cell().add(new Paragraph("S/ " + String.format("%.2f", resumen.totalIngresos)).setFont(fontBold)));
            
            resumenTable.addCell(new Cell().add(new Paragraph("Método de Pago Más Usado:").setFont(fontRegular)));
            resumenTable.addCell(new Cell().add(new Paragraph(resumen.metodoPagoMasUsado).setFont(fontBold)));
            
            document.add(resumenTable);
            document.add(new Paragraph("\n"));
            
            // Detalle de ventas
            if (!resumen.ventas.isEmpty()) {
                document.add(new Paragraph("DETALLE DE VENTAS")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setMarginBottom(10));
                
                Table ventasTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));
                
                // Encabezados
                ventasTable.addHeaderCell(new Cell().add(new Paragraph("N° Venta").setFont(fontBold)));
                ventasTable.addHeaderCell(new Cell().add(new Paragraph("Fecha/Hora").setFont(fontBold)));
                ventasTable.addHeaderCell(new Cell().add(new Paragraph("Cajera").setFont(fontBold)));
                ventasTable.addHeaderCell(new Cell().add(new Paragraph("Método Pago").setFont(fontBold)));
                ventasTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(fontBold)));
                
                // Datos de ventas
                for (VentaDelDia venta : resumen.ventas) {
                    ventasTable.addCell(new Cell().add(new Paragraph(venta.numeroVenta).setFont(fontRegular)));
                    ventasTable.addCell(new Cell().add(new Paragraph(venta.fechaHora).setFont(fontRegular)));
                    ventasTable.addCell(new Cell().add(new Paragraph(venta.cajera).setFont(fontRegular)));
                    ventasTable.addCell(new Cell().add(new Paragraph(venta.metodoPago).setFont(fontRegular)));
                    ventasTable.addCell(new Cell().add(new Paragraph("S/ " + String.format("%.2f", venta.total)).setFont(fontRegular)));
                }
                
                document.add(ventasTable);
            } else {
                document.add(new Paragraph("No se registraron ventas en el día de hoy.")
                    .setFont(fontRegular)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));
            }
            
            // Pie de página
            document.add(new Paragraph("\n\nReporte generado automáticamente por Sistema POS MiniMarket")
                .setFont(fontRegular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
            
            document.close();
            
            return rutaArchivo;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene todas las ventas del día actual
     */
    private static ResumenVentas obtenerVentasDelDia() throws SQLException {
        ResumenVentas resumen = new ResumenVentas();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn == null) {
                return resumen;
            }
            
            // Obtener ventas del día
            String sqlVentas = """
                SELECT v.numero, v.fecha_hora, u.nombre_completo as cajera, 
                       v.metodo_pago, v.total
                FROM ventas v
                LEFT JOIN usuarios u ON v.cajera_id = u.id
                WHERE DATE(v.fecha_hora) = CURRENT_DATE AND v.estado = 'ACTIVA'
                ORDER BY v.fecha_hora DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlVentas);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    VentaDelDia venta = new VentaDelDia();
                    venta.numeroVenta = rs.getString("numero");
                    venta.fechaHora = rs.getTimestamp("fecha_hora").toString();
                    venta.cajera = rs.getString("cajera") != null ? rs.getString("cajera") : "N/A";
                    venta.metodoPago = rs.getString("metodo_pago");
                    venta.total = rs.getDouble("total");
                    
                    resumen.ventas.add(venta);
                }
            }
            
            // Calcular totales
            resumen.totalTransacciones = resumen.ventas.size();
            resumen.totalIngresos = resumen.ventas.stream().mapToDouble(v -> v.total).sum();
            
            // Obtener total de productos vendidos
            String sqlProductos = """
                SELECT COALESCE(SUM(dv.cantidad), 0) as total_productos
                FROM detalle_ventas dv
                INNER JOIN ventas v ON dv.venta_id = v.id
                WHERE DATE(v.fecha_hora) = CURRENT_DATE AND v.estado = 'ACTIVA'
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlProductos);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    resumen.totalProductos = rs.getInt("total_productos");
                }
            }
            
            // Obtener método de pago más usado
            String sqlMetodoPago = """
                SELECT metodo_pago, COUNT(*) as cantidad
                FROM ventas
                WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'
                GROUP BY metodo_pago
                ORDER BY cantidad DESC
                LIMIT 1
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlMetodoPago);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    resumen.metodoPagoMasUsado = rs.getString("metodo_pago");
                } else {
                    resumen.metodoPagoMasUsado = "N/A";
                }
            }
        }
        
        return resumen;
    }
}
