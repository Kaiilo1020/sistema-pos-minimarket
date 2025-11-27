package com.minimarket.patterns.behavioral.command;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Gestor de comandos que implementa el patrón Command
 * Permite ejecutar, deshacer y gestionar el historial de comandos
 */
public class GestorComandos {
    
    private Stack<Comando> historialComandos;
    private Stack<Comando> comandosDeshecho;
    private int maxHistorial;
    
    public GestorComandos() {
        this(100); // Por defecto, mantener 100 comandos en historial
    }
    
    public GestorComandos(int maxHistorial) {
        this.maxHistorial = maxHistorial;
        this.historialComandos = new Stack<>();
        this.comandosDeshecho = new Stack<>();
    }
    
    /**
     * Ejecuta un comando y lo agrega al historial
     */
    public boolean ejecutarComando(Comando comando) {
        boolean exitoso = comando.ejecutar();
        
        if (exitoso) {
            // Agregar al historial
            historialComandos.push(comando);
            
            // Limpiar comandos deshechos (ya no se pueden rehacer)
            comandosDeshecho.clear();
            
            // Mantener el tamaño del historial
            if (historialComandos.size() > maxHistorial) {
                // Remover el comando más antiguo
                Stack<Comando> nuevoHistorial = new Stack<>();
                List<Comando> comandos = new ArrayList<>(historialComandos);
                for (int i = 1; i < comandos.size(); i++) {
                    nuevoHistorial.push(comandos.get(i));
                }
                historialComandos = nuevoHistorial;
            }
            
            System.out.println("✅ Comando ejecutado: " + comando.getDescripcion());
        } else {
            System.out.println("❌ Error al ejecutar comando: " + comando.getResultado());
        }
        
        return exitoso;
    }
    
    /**
     * Deshace el último comando ejecutado
     */
    public boolean deshacerUltimoComando() {
        if (historialComandos.isEmpty()) {
            System.out.println("⚠️ No hay comandos para deshacer");
            return false;
        }
        
        Comando ultimoComando = historialComandos.pop();
        
        if (!ultimoComando.puedeDeshacer()) {
            System.out.println("⚠️ El comando no puede deshacerse: " + ultimoComando.getDescripcion());
            historialComandos.push(ultimoComando); // Volver a agregarlo
            return false;
        }
        
        boolean deshecho = ultimoComando.deshacer();
        
        if (deshecho) {
            comandosDeshecho.push(ultimoComando);
            System.out.println("↶ Comando deshecho: " + ultimoComando.getDescripcion());
        } else {
            historialComandos.push(ultimoComando); // Volver a agregarlo si falló
            System.out.println("❌ Error al deshacer comando: " + ultimoComando.getDescripcion());
        }
        
        return deshecho;
    }
    
    /**
     * Rehace el último comando deshecho
     */
    public boolean rehacerComando() {
        if (comandosDeshecho.isEmpty()) {
            System.out.println("⚠️ No hay comandos para rehacer");
            return false;
        }
        
        Comando comando = comandosDeshecho.pop();
        return ejecutarComando(comando);
    }
    
    /**
     * Deshace múltiples comandos
     */
    public int deshacerComandos(int cantidad) {
        int comandosDeshecho = 0;
        
        for (int i = 0; i < cantidad && !historialComandos.isEmpty(); i++) {
            if (deshacerUltimoComando()) {
                comandosDeshecho++;
            } else {
                break; // Si uno falla, detener
            }
        }
        
        return comandosDeshecho;
    }
    
    /**
     * Obtiene el historial de comandos ejecutados
     */
    public List<Comando> getHistorialComandos() {
        return new ArrayList<>(historialComandos);
    }
    
    /**
     * Obtiene los comandos que pueden rehacerse
     */
    public List<Comando> getComandosDeshecho() {
        return new ArrayList<>(comandosDeshecho);
    }
    
    /**
     * Muestra el historial de comandos
     */
    public void mostrarHistorial() {
        System.out.println("=== HISTORIAL DE COMANDOS ===");
        
        if (historialComandos.isEmpty()) {
            System.out.println("No hay comandos en el historial");
            return;
        }
        
        List<Comando> comandos = new ArrayList<>(historialComandos);
        Collections.reverse(comandos); // Mostrar los más recientes primero
        
        for (int i = 0; i < comandos.size(); i++) {
            Comando comando = comandos.get(i);
            System.out.printf("%d. [%s] %s - %s%n", 
                            i + 1,
                            comando.getFechaEjecucion().toLocalTime(),
                            comando.getDescripcion(),
                            comando.getUsuarioEjecutor());
        }
    }
    
    /**
     * Muestra estadísticas del gestor de comandos
     */
    public void mostrarEstadisticas() {
        System.out.println("=== ESTADÍSTICAS DE COMANDOS ===");
        System.out.println("Comandos en historial: " + historialComandos.size());
        System.out.println("Comandos deshechos: " + comandosDeshecho.size());
        System.out.println("Máximo historial: " + maxHistorial);
        
        if (!historialComandos.isEmpty()) {
            Map<String, Integer> tiposComando = new HashMap<>();
            
            for (Comando comando : historialComandos) {
                String tipo = comando.getClass().getSimpleName();
                tiposComando.put(tipo, tiposComando.getOrDefault(tipo, 0) + 1);
            }
            
            System.out.println("\nTipos de comandos ejecutados:");
            tiposComando.forEach((tipo, cantidad) -> 
                System.out.println("- " + tipo + ": " + cantidad));
        }
    }
    
    /**
     * Busca comandos por usuario
     */
    public List<Comando> buscarComandosPorUsuario(String usuario) {
        List<Comando> resultado = new ArrayList<>();
        
        for (Comando comando : historialComandos) {
            if (comando.getUsuarioEjecutor().equals(usuario)) {
                resultado.add(comando);
            }
        }
        
        return resultado;
    }
    
    /**
     * Busca comandos por rango de fechas
     */
    public List<Comando> buscarComandosPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        List<Comando> resultado = new ArrayList<>();
        
        for (Comando comando : historialComandos) {
            LocalDateTime fechaComando = comando.getFechaEjecucion();
            if (fechaComando != null && 
                !fechaComando.isBefore(desde) && 
                !fechaComando.isAfter(hasta)) {
                resultado.add(comando);
            }
        }
        
        return resultado;
    }
    
    /**
     * Limpia el historial de comandos
     */
    public void limpiarHistorial() {
        historialComandos.clear();
        comandosDeshecho.clear();
        System.out.println("🗑️ Historial de comandos limpiado");
    }
    
    /**
     * Verifica si hay comandos para deshacer
     */
    public boolean hayComandosParaDeshacer() {
        return !historialComandos.isEmpty();
    }
    
    /**
     * Verifica si hay comandos para rehacer
     */
    public boolean hayComandosParaRehacer() {
        return !comandosDeshecho.isEmpty();
    }
}
